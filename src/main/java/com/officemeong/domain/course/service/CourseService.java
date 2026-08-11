package com.officemeong.domain.course.service;

import com.officemeong.domain.congestion.entity.CongestionForecast;
import com.officemeong.domain.congestion.repository.CongestionForecastRepository;
import com.officemeong.domain.course.dto.*;
import com.officemeong.domain.course.entity.Course;
import com.officemeong.domain.course.entity.CourseChecklistItem;
import com.officemeong.domain.course.entity.CourseItem;
import com.officemeong.domain.course.enums.WorkFocusLevel;
import com.officemeong.domain.course.repository.CourseChecklistItemRepository;
import com.officemeong.domain.course.repository.CourseItemRepository;
import com.officemeong.domain.course.repository.CourseRepository;
import com.officemeong.domain.dog.entity.Dog;
import com.officemeong.domain.dog.repository.DogRepository;
import com.officemeong.domain.place.entity.Place;
import com.officemeong.domain.place.entity.PlaceOperation;
import com.officemeong.domain.place.entity.PlacePetCondition;
import com.officemeong.domain.place.entity.PlaceScore;
import com.officemeong.domain.place.enums.LodgingType;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.place.repository.PlaceRepository;
import com.officemeong.domain.user.entity.User;
import com.officemeong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private static final int MAX_ALTERNATIVES = 5;

    private final CourseRepository courseRepository;
    private final CourseItemRepository courseItemRepository;
    private final CourseChecklistItemRepository checklistItemRepository;
    private final UserRepository userRepository;
    private final DogRepository dogRepository;
    private final PlaceRepository placeRepository;
    private final CongestionForecastRepository congestionForecastRepository;

    // ──────────────────── 코스 생성 ────────────────────

    @Transactional
    public CourseResponse createCourse(Long userId, CourseCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        BigDecimal dogWeightKg = resolveDogWeight(userId, request.getDogId());

        int days = (int) request.getStartDate().until(request.getEndDate(),
                java.time.temporal.ChronoUnit.DAYS) + 1;
        String courseName = request.getName() != null && !request.getName().isBlank()
                ? request.getName()
                : request.getRegion().name() + " " + days + "일 워케이션";

        Course course = Course.builder()
                .user(user)
                .region(request.getRegion())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .workStartTime(request.getWorkStartTime())
                .workEndTime(request.getWorkEndTime())
                .workFocusLevel(request.getWorkFocusLevel())
                .name(courseName)
                .build();

        Integer travelCongestionScore = resolveTravelCongestionScore(
                request.getRegion(), request.getStartDate(), request.getEndDate());
        Map<PlaceType, List<Place>> candidatesByType = loadCandidates(
                request.getRegion(), dogWeightKg, travelCongestionScore, request.getPreferredLodgingTypes());
        Set<Long> usedPlaceIds = new HashSet<>();

        for (int day = 1; day <= days; day++) {
            boolean includeStay = (day == 1) && days > 1;
            List<PlaceSlot> slots = buildDaySlots(
                    request.getWorkStartTime(), request.getWorkEndTime(),
                    request.getWorkFocusLevel(), includeStay);

            Place prevPlace = null;
            int order = 1;
            for (PlaceSlot slot : slots) {
                List<Place> candidates = candidatesByType.getOrDefault(slot.placeType(), List.of());
                Place place = pickPlace(candidates, usedPlaceIds);
                if (place == null) continue;

                usedPlaceIds.add(place.getId());
                BigDecimal dist = calcDistFromPrev(prevPlace, place);

                CourseItem item = CourseItem.builder()
                        .course(course)
                        .place(place)
                        .dayNumber(day)
                        .visitOrder(order++)
                        .startTime(slot.start())
                        .endTime(slot.end())
                        .slotLabel(slot.label())
                        .distanceFromPrevKm(dist)
                        .build();
                course.addItem(item);
                prevPlace = place;
            }
        }

        courseRepository.save(course);
        return CourseResponse.from(course);
    }

    // ──────────────────── 코스 조회 ────────────────────

    public CourseResponse getCourse(Long userId, Long courseId) {
        Course course = courseRepository.findByIdAndUserIdWithItems(courseId, userId)
                .orElseThrow(() -> new NoSuchElementException("코스를 찾을 수 없습니다. id=" + courseId));
        return CourseResponse.from(course);
    }

    public List<CourseSummaryResponse> getMyCourses(Long userId) {
        return courseRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(CourseSummaryResponse::from)
                .toList();
    }

    // ──────────────────── 코스 아이템 수정 ────────────────────

    @Transactional
    public CourseResponse updateCourseItem(Long userId, Long courseId, Long itemId,
                                           CourseItemUpdateRequest request) {
        Course course = courseRepository.findByIdAndUserIdWithItems(courseId, userId)
                .orElseThrow(() -> new NoSuchElementException("코스를 찾을 수 없습니다. id=" + courseId));

        CourseItem item = course.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("코스 아이템을 찾을 수 없습니다. id=" + itemId));

        if (request.getStartTime() != null || request.getEndTime() != null) {
            item.updateTime(
                    request.getStartTime() != null ? request.getStartTime() : item.getStartTime(),
                    request.getEndTime() != null ? request.getEndTime() : item.getEndTime());
        }

        if (request.getNewPlaceId() != null) {
            Place newPlace = placeRepository.findById(request.getNewPlaceId())
                    .orElseThrow(() -> new NoSuchElementException("장소를 찾을 수 없습니다. id=" + request.getNewPlaceId()));
            BigDecimal dist = calcDistFromPrevItem(course, item, newPlace);
            item.updatePlace(newPlace, dist);
        }

        return CourseResponse.from(course);
    }

    // ──────────────────── 코스 아이템 순서 변경 ────────────────────

    @Transactional
    public CourseResponse reorderCourseItems(Long userId, Long courseId, CourseItemReorderRequest request) {
        Course course = courseRepository.findByIdAndUserIdWithItems(courseId, userId)
                .orElseThrow(() -> new NoSuchElementException("코스를 찾을 수 없습니다. id=" + courseId));

        Map<Long, CourseItem> dayItemsById = course.getItems().stream()
                .filter(i -> i.getDayNumber().equals(request.getDayNumber()))
                .collect(Collectors.toMap(CourseItem::getId, i -> i));

        Set<Long> requestedIds = new LinkedHashSet<>(request.getItemIds());
        if (requestedIds.size() != request.getItemIds().size()) {
            throw new IllegalArgumentException("아이템 ID가 중복되었습니다.");
        }
        if (!requestedIds.equals(dayItemsById.keySet())) {
            throw new IllegalArgumentException(
                    request.getDayNumber() + "일차의 실제 아이템 구성과 요청한 목록이 일치하지 않습니다.");
        }

        Place prevPlace = null;
        int order = 1;
        for (Long itemId : request.getItemIds()) {
            CourseItem item = dayItemsById.get(itemId);
            item.updateOrder(order++, calcDistFromPrev(prevPlace, item.getPlace()));
            prevPlace = item.getPlace();
        }

        return CourseResponse.from(course);
    }

    // ──────────────────── 코스 아이템 추가 ────────────────────

    @Transactional
    public CourseResponse addCourseItem(Long userId, Long courseId, CourseItemCreateRequest request) {
        Course course = courseRepository.findByIdAndUserIdWithItems(courseId, userId)
                .orElseThrow(() -> new NoSuchElementException("코스를 찾을 수 없습니다. id=" + courseId));

        int totalDays = (int) course.getStartDate().until(course.getEndDate(),
                java.time.temporal.ChronoUnit.DAYS) + 1;
        if (request.getDayNumber() < 1 || request.getDayNumber() > totalDays) {
            throw new IllegalArgumentException(
                    "코스 여행 기간(1~" + totalDays + "일차)을 벗어난 일차입니다. dayNumber=" + request.getDayNumber());
        }

        Place place = placeRepository.findById(request.getPlaceId())
                .orElseThrow(() -> new NoSuchElementException("장소를 찾을 수 없습니다. id=" + request.getPlaceId()));

        List<CourseItem> dayItems = new ArrayList<>(course.getItems().stream()
                .filter(i -> i.getDayNumber().equals(request.getDayNumber()))
                .sorted(Comparator.comparingInt(CourseItem::getVisitOrder))
                .toList());

        int insertIndex = (request.getVisitOrder() == null
                || request.getVisitOrder() < 1
                || request.getVisitOrder() > dayItems.size() + 1)
                ? dayItems.size()
                : request.getVisitOrder() - 1;

        CourseItem newItem = CourseItem.builder()
                .course(course)
                .place(place)
                .dayNumber(request.getDayNumber())
                .visitOrder(insertIndex + 1)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .slotLabel(request.getSlotLabel())
                .build();
        dayItems.add(insertIndex, newItem);
        course.addItem(newItem);

        resequence(dayItems);

        return CourseResponse.from(course);
    }

    // ──────────────────── 코스 아이템 삭제 ────────────────────

    @Transactional
    public CourseResponse deleteCourseItem(Long userId, Long courseId, Long itemId) {
        Course course = courseRepository.findByIdAndUserIdWithItems(courseId, userId)
                .orElseThrow(() -> new NoSuchElementException("코스를 찾을 수 없습니다. id=" + courseId));

        CourseItem target = course.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("코스 아이템을 찾을 수 없습니다. id=" + itemId));

        Integer dayNumber = target.getDayNumber();
        course.getItems().remove(target);

        List<CourseItem> remaining = course.getItems().stream()
                .filter(i -> i.getDayNumber().equals(dayNumber))
                .sorted(Comparator.comparingInt(CourseItem::getVisitOrder))
                .toList();
        resequence(remaining);

        return CourseResponse.from(course);
    }

    /** 같은 날짜 아이템 목록을 주어진 순서대로 1부터 재번호하고, 거리(distanceFromPrevKm)를 재계산 */
    private void resequence(List<CourseItem> orderedDayItems) {
        Place prevPlace = null;
        int order = 1;
        for (CourseItem item : orderedDayItems) {
            item.updateOrder(order++, calcDistFromPrev(prevPlace, item.getPlace()));
            prevPlace = item.getPlace();
        }
    }

    // ──────────────────── 코스 이름 수정 ────────────────────

    @Transactional
    public CourseResponse updateCourseName(Long userId, Long courseId, String name) {
        Course course = courseRepository.findByIdAndUserIdWithItems(courseId, userId)
                .orElseThrow(() -> new NoSuchElementException("코스를 찾을 수 없습니다. id=" + courseId));
        course.updateName(name);
        return CourseResponse.from(course);
    }

    // ──────────────────── 코스 삭제 ────────────────────

    @Transactional
    public void deleteCourse(Long userId, Long courseId) {
        Course course = courseRepository.findByIdAndUserIdWithItems(courseId, userId)
                .orElseThrow(() -> new NoSuchElementException("코스를 찾을 수 없습니다. id=" + courseId));
        courseRepository.delete(course);
    }

    // ──────────────────── 대체 장소 추천 ────────────────────

    public List<AlternativePlaceResponse> getAlternatives(Long userId, Long courseId, Long itemId) {
        Course course = courseRepository.findByIdAndUserIdWithItems(courseId, userId)
                .orElseThrow(() -> new NoSuchElementException("코스를 찾을 수 없습니다. id=" + courseId));

        CourseItem target = course.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("코스 아이템을 찾을 수 없습니다. id=" + itemId));

        Set<Long> usedIds = course.getItems().stream()
                .map(i -> i.getPlace().getId())
                .filter(id -> !id.equals(target.getPlace().getId()))
                .collect(Collectors.toSet());

        PlaceType targetType = target.getPlace().getPlaceType();

        return placeRepository.findByRegionAndPlaceTypeOrderByScore(course.getRegion(), targetType)
                .stream()
                .filter(p -> !usedIds.contains(p.getId()))
                .limit(MAX_ALTERNATIVES)
                .map(AlternativePlaceResponse::from)
                .toList();
    }

    // ──────────────────── 준비 체크리스트 ────────────────────

    public List<ChecklistItemResponse> getChecklist(Long userId, Long courseId) {
        Course course = courseRepository.findByIdAndUserId(courseId, userId)
                .orElseThrow(() -> new NoSuchElementException("코스를 찾을 수 없습니다. id=" + courseId));
        return checklistItemRepository.findByCourseIdOrderByDisplayOrderAsc(course.getId()).stream()
                .map(ChecklistItemResponse::from)
                .toList();
    }

    @Transactional
    public ChecklistItemResponse addChecklistItem(Long userId, Long courseId, ChecklistItemRequest request) {
        Course course = courseRepository.findByIdAndUserId(courseId, userId)
                .orElseThrow(() -> new NoSuchElementException("코스를 찾을 수 없습니다. id=" + courseId));

        int nextOrder = checklistItemRepository.countByCourseId(course.getId()) + 1;
        CourseChecklistItem item = CourseChecklistItem.builder()
                .course(course)
                .content(request.getContent())
                .displayOrder(nextOrder)
                .build();
        checklistItemRepository.save(item);
        return ChecklistItemResponse.from(item);
    }

    @Transactional
    public ChecklistItemResponse updateChecklistItem(Long userId, Long courseId, Long itemId,
                                                       ChecklistItemUpdateRequest request) {
        courseRepository.findByIdAndUserId(courseId, userId)
                .orElseThrow(() -> new NoSuchElementException("코스를 찾을 수 없습니다. id=" + courseId));
        CourseChecklistItem item = findChecklistItem(courseId, itemId);

        if (request.getContent() != null) item.updateContent(request.getContent());
        if (request.getChecked() != null) item.toggleChecked(request.getChecked());
        return ChecklistItemResponse.from(item);
    }

    @Transactional
    public void deleteChecklistItem(Long userId, Long courseId, Long itemId) {
        courseRepository.findByIdAndUserId(courseId, userId)
                .orElseThrow(() -> new NoSuchElementException("코스를 찾을 수 없습니다. id=" + courseId));
        checklistItemRepository.delete(findChecklistItem(courseId, itemId));
    }

    private CourseChecklistItem findChecklistItem(Long courseId, Long itemId) {
        return checklistItemRepository.findById(itemId)
                .filter(i -> i.getCourse().getId().equals(courseId))
                .orElseThrow(() -> new NoSuchElementException("체크리스트 항목을 찾을 수 없습니다. id=" + itemId));
    }

    // ──────────────────── 내부 유틸 ────────────────────

    private BigDecimal resolveDogWeight(Long userId, Long dogId) {
        if (dogId == null) return null;
        Dog dog = dogRepository.findByIdAndUserId(dogId, userId)
                .orElseThrow(() -> new NoSuchElementException("반려견을 찾을 수 없습니다. id=" + dogId));
        return dog.getWeightKg();
    }

    private Map<PlaceType, List<Place>> loadCandidates(
            Region region, BigDecimal dogWeightKg, Integer travelCongestionScore,
            List<LodgingType> preferredLodgingTypes) {
        Map<PlaceType, List<Place>> map = new EnumMap<>(PlaceType.class);
        for (PlaceType type : PlaceType.values()) {
            List<Place> places = placeRepository.findByRegionAndPlaceTypeOrderByScore(region, type)
                    .stream()
                    .filter(p -> isWeightAllowed(p, dogWeightKg))
                    .collect(Collectors.toList());
            if (travelCongestionScore != null) {
                places.sort(Comparator.comparingInt(
                        (Place p) -> effectiveScore(p, travelCongestionScore)).reversed());
            }
            // 숙소 유형 선호 반영: 선호 유형 일치 숙소를 앞으로 (안정 정렬이라 기존 순위는 그룹 내에서 유지됨).
            // 일치하는 숙소가 없어도 후순위로 밀릴 뿐 후보에서 제외되지는 않아 코스 생성이 실패하지 않음.
            if (type == PlaceType.STAY && preferredLodgingTypes != null && !preferredLodgingTypes.isEmpty()) {
                places.sort(Comparator.comparingInt(
                        (Place p) -> matchesPreferredLodging(p, preferredLodgingTypes) ? 0 : 1));
            }
            map.put(type, places);
        }
        return map;
    }

    private boolean matchesPreferredLodging(Place place, List<LodgingType> preferredTypes) {
        PlaceOperation op = place.getOperation();
        return op != null && op.getLodgingType() != null && preferredTypes.contains(op.getLodgingType());
    }

    /**
     * 여행 기간(startDate~endDate)의 관광지집중률 예측 평균을 조회해 혼잡도 점수(0~15)로 환산.
     * congestion_forecasts는 지역 단위로만 집계되므로 지역 내 모든 장소에 동일하게 적용됨.
     * 예측 데이터가 없으면 null을 반환해 place_scores에 저장된 "오늘 기준" 점수를 그대로 사용한다.
     */
    private Integer resolveTravelCongestionScore(Region region, LocalDate startDate, LocalDate endDate) {
        List<CongestionForecast> forecasts = congestionForecastRepository
                .findByRegionAndBaseYmdBetween(region, startDate, endDate);
        List<BigDecimal> rates = forecasts.stream()
                .map(CongestionForecast::getCnctrRate)
                .filter(Objects::nonNull)
                .toList();
        if (rates.isEmpty()) return null;

        double avgRate = rates.stream().mapToDouble(BigDecimal::doubleValue).average().orElseThrow();
        return mapRateToCongestionScore(avgRate);
    }

    // CongestionScoreUpdateTasklet.mapRateToScore와 동일한 기준 (SYSTEM_DESIGN.md 3.5)
    private int mapRateToCongestionScore(double rate) {
        if (rate < 30) return 15;
        if (rate < 45) return 12;
        if (rate < 55) return 9;
        if (rate < 65) return 6;
        if (rate < 75) return 3;
        return 1;
    }

    /** 여행 날짜 기준 혼잡도로 congestionScore를 치환한 정렬용 점수 */
    private int effectiveScore(Place place, int travelCongestionScore) {
        PlaceScore score = place.getScore();
        if (score == null) return 0;
        return score.getTotalScore() - score.getCongestionScore() + travelCongestionScore;
    }

    private boolean isWeightAllowed(Place place, BigDecimal dogWeightKg) {
        if (dogWeightKg == null) return true;
        PlacePetCondition cond = place.getPetCondition();
        if (cond == null || cond.getPetWeightLimitKg() == null) return true;
        return dogWeightKg.compareTo(BigDecimal.valueOf(cond.getPetWeightLimitKg())) <= 0;
    }

    private Place pickPlace(List<Place> candidates, Set<Long> usedIds) {
        return candidates.stream()
                .filter(p -> !usedIds.contains(p.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 하루 일정 슬롯 생성.
     * workStart/workEnd는 사용자 입력 시간, workFocusLevel로 오전/오후 구성 결정.
     */
    private List<PlaceSlot> buildDaySlots(LocalTime workStart, LocalTime workEnd,
                                           WorkFocusLevel level, boolean includeStay) {
        List<PlaceSlot> slots = new ArrayList<>();
        long workMinutes = Duration.between(workStart, workEnd).toMinutes();
        LocalTime workMid = workStart.plusMinutes(workMinutes / 2);

        if (includeStay) {
            slots.add(new PlaceSlot(PlaceType.STAY, LocalTime.of(15, 0), LocalTime.of(16, 0), "숙박"));
        }

        // 아침 식사 (업무 시작 1시간 전, 단 07:00 이후에만)
        if (workStart.isAfter(LocalTime.of(7, 0))) {
            slots.add(new PlaceSlot(PlaceType.FOOD, workStart.minusHours(1), workStart, "아침"));
        }

        switch (level) {
            case LOW -> {
                slots.add(new PlaceSlot(PlaceType.WORK_PLACE, workStart, workMid, "오전 업무"));
                slots.add(new PlaceSlot(PlaceType.FOOD, workMid, workMid.plusHours(1), "점심"));
                slots.add(new PlaceSlot(PlaceType.TOUR, workMid.plusHours(1), workEnd, "오후 관광"));
            }
            case MEDIUM -> {
                slots.add(new PlaceSlot(PlaceType.WORK_PLACE, workStart, workMid, "오전 업무"));
                slots.add(new PlaceSlot(PlaceType.FOOD, workMid, workMid.plusHours(1), "점심"));
                slots.add(new PlaceSlot(PlaceType.WALK, workMid.plusHours(1), workMid.plusHours(2), "점심 산책"));
                slots.add(new PlaceSlot(PlaceType.WORK_PLACE, workMid.plusHours(2), workEnd, "오후 업무"));
            }
            case HIGH -> {
                slots.add(new PlaceSlot(PlaceType.WORK_PLACE, workStart, workMid.minusMinutes(30), "집중 오전"));
                slots.add(new PlaceSlot(PlaceType.FOOD, workMid.minusMinutes(30), workMid.plusMinutes(30), "점심"));
                slots.add(new PlaceSlot(PlaceType.WORK_PLACE, workMid.plusMinutes(30), workEnd, "집중 오후"));
            }
        }

        slots.add(new PlaceSlot(PlaceType.WALK, workEnd, workEnd.plusHours(1), "저녁 산책"));
        slots.add(new PlaceSlot(PlaceType.FOOD, workEnd.plusHours(1), workEnd.plusHours(2), "저녁"));

        if (level == WorkFocusLevel.LOW) {
            slots.add(new PlaceSlot(PlaceType.TOUR, workEnd.plusHours(2), workEnd.plusHours(3), "저녁 후 관광"));
        }

        return slots;
    }

    private BigDecimal calcDistFromPrev(Place prev, Place curr) {
        if (prev == null || prev.getLatitude() == null || prev.getLongitude() == null) return null;
        if (curr.getLatitude() == null || curr.getLongitude() == null) return null;
        double km = haversineKm(prev.getLatitude().doubleValue(), prev.getLongitude().doubleValue(),
                curr.getLatitude().doubleValue(), curr.getLongitude().doubleValue());
        return BigDecimal.valueOf(km).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcDistFromPrevItem(Course course, CourseItem target, Place newPlace) {
        List<CourseItem> sortedItems = course.getItems().stream()
                .filter(i -> i.getDayNumber().equals(target.getDayNumber()))
                .sorted(Comparator.comparingInt(CourseItem::getVisitOrder))
                .toList();
        int idx = sortedItems.indexOf(target);
        if (idx <= 0) return null;
        Place prevPlace = sortedItems.get(idx - 1).getPlace();
        return calcDistFromPrev(prevPlace, newPlace);
    }

    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private record PlaceSlot(PlaceType placeType, LocalTime start, LocalTime end, String label) {}
}
