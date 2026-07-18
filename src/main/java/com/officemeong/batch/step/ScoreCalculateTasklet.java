package com.officemeong.batch.step;

import com.officemeong.domain.place.entity.Place;
import com.officemeong.domain.place.entity.PlaceAccessibility;
import com.officemeong.domain.place.entity.PlaceOperation;
import com.officemeong.domain.place.entity.PlacePetCondition;
import com.officemeong.domain.place.entity.PlaceScore;
import com.officemeong.domain.place.enums.AcmpyType;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.place.repository.PlaceRepository;
import com.officemeong.domain.place.repository.PlaceScoreRepository;
import com.officemeong.domain.walk.entity.WalkCourse;
import com.officemeong.domain.walk.repository.WalkCourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScoreCalculateTasklet implements Tasklet {

    private final PlaceRepository placeRepository;
    private final PlaceScoreRepository scoreRepository;
    private final WalkCourseRepository walkCourseRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        Map<Region, List<WalkCourse>> coursesByRegion = Map.of(
                Region.GANGNEUNG, walkCourseRepository.findByRegion(Region.GANGNEUNG),
                Region.CHUNCHEON, walkCourseRepository.findByRegion(Region.CHUNCHEON),
                Region.WONJU, walkCourseRepository.findByRegion(Region.WONJU)
        );
        Map<Region, List<Place>> hospitalsByRegion = Map.of(
                Region.GANGNEUNG, placeRepository.findByRegionAndPlaceType(Region.GANGNEUNG, PlaceType.HOSPITAL),
                Region.CHUNCHEON, placeRepository.findByRegionAndPlaceType(Region.CHUNCHEON, PlaceType.HOSPITAL),
                Region.WONJU, placeRepository.findByRegionAndPlaceType(Region.WONJU, PlaceType.HOSPITAL)
        );

        List<Place> places = placeRepository.findAll();
        int calculated = 0;

        for (Place place : places) {
            if (place.getScore() == null) continue;
            PlaceScore score = place.getScore();

            int petScore = calcPetCompanionScore(place.getPetCondition());
            int workcationScore = getWorkcationScore(place.getPlaceType());
            int walkScore = calcWalkScore(place, coursesByRegion.get(place.getRegion()));
            int emergencyScore = calcEmergencyScore(place, hospitalsByRegion.get(place.getRegion()));
            int accessibilityScore = calcAccessibilityScore(place);

            score.updatePetAndWorkcationScores(petScore, workcationScore);
            score.updateWalkAccessibilityScore(walkScore);
            score.updateEmergencyScore(emergencyScore);
            score.updateAccessibilityScore(accessibilityScore);
            scoreRepository.save(score);
            calculated++;
        }

        log.info("점수 계산 완료 - {}개", calculated);
        return RepeatStatus.FINISHED;
    }

    /**
     * 반려견 동반 적합도 (0~30점)
     * acmpyType: INDOOR_OUTDOOR=15, INDOOR/OUTDOOR=10, DESIGNATED=5
     * 목욕 시설: +5, 반려동물 시설 있음: +5, 케이지 불필요: +5
     */
    private int calcPetCompanionScore(PlacePetCondition cond) {
        if (cond == null) return 0;
        int score = 0;

        AcmpyType type = cond.getAcmpyType();
        if (type == AcmpyType.INDOOR_OUTDOOR) score += 15;
        else if (type == AcmpyType.INDOOR || type == AcmpyType.OUTDOOR) score += 10;
        else if (type == AcmpyType.DESIGNATED) score += 5;

        if (Boolean.TRUE.equals(cond.getBathAvailable())) score += 5;
        if (cond.getAvailableFacilities() != null && !cond.getAvailableFacilities().isBlank()) score += 5;
        if (!Boolean.TRUE.equals(cond.getIsCageRequired())) score += 5;

        return Math.min(score, 30);
    }

    /** 워케이션 적합도 (PlaceType 고정값) */
    private int getWorkcationScore(PlaceType type) {
        return switch (type) {
            case WORK_PLACE -> 25;
            case STAY -> 18;
            case FOOD -> 8;
            case TOUR -> 5;
            default -> 0;
        };
    }

    /** 산책 접근성 (0~20점): 가장 가까운 두루누비 코스까지 Haversine 거리 기준 */
    private int calcWalkScore(Place place, List<WalkCourse> courses) {
        if (place.getLatitude() == null || place.getLongitude() == null) return 5;
        if (courses == null || courses.isEmpty()) return 5;

        double minKm = courses.stream()
                .filter(c -> c.getStartLatitude() != null && c.getStartLongitude() != null)
                .mapToDouble(c -> haversineKm(place.getLatitude(), place.getLongitude(),
                        c.getStartLatitude(), c.getStartLongitude()))
                .min().orElse(Double.MAX_VALUE);

        if (minKm == Double.MAX_VALUE) return 5;
        if (minKm <= 0.3) return 20;
        if (minKm <= 0.5) return 17;
        if (minKm <= 1.0) return 14;
        if (minKm <= 2.0) return 10;
        if (minKm <= 5.0) return 7;
        return 5;
    }

    /** 응급 접근성 (0~7점): 가장 가까운 동물병원까지 Haversine 거리 기준 */
    private int calcEmergencyScore(Place place, List<Place> hospitals) {
        if (place.getLatitude() == null || place.getLongitude() == null) return 0;
        if (hospitals == null || hospitals.isEmpty()) return 0;

        double minKm = hospitals.stream()
                .filter(h -> h.getLatitude() != null && h.getLongitude() != null)
                .mapToDouble(h -> haversineKm(place.getLatitude(), place.getLongitude(),
                        h.getLatitude(), h.getLongitude()))
                .min().orElse(Double.MAX_VALUE);

        if (minKm == Double.MAX_VALUE) return 0;
        if (minKm <= 1.0) return 7;
        if (minKm <= 3.0) return 5;
        if (minKm <= 5.0) return 3;
        if (minKm <= 10.0) return 1;
        return 0;
    }

    /**
     * 접근성 점수 (0~8점)
     * 주차 가능(PlaceOperation): +5, 유모차/휠체어 접근(PlaceAccessibility): +2, 경사로: +1
     * 주차 데이터는 KTO/GWTO 수집 시 PlaceOperation.parkingAvailable에 저장됨.
     * 유모차·경사로 데이터는 향후 확보 시 PlaceAccessibility에서 반영.
     */
    private int calcAccessibilityScore(Place place) {
        int score = 0;
        PlaceOperation op = place.getOperation();
        if (op != null && Boolean.TRUE.equals(op.getParkingAvailable())) score += 5;
        PlaceAccessibility acc = place.getAccessibility();
        if (acc != null && Boolean.TRUE.equals(acc.getStrollerAccessible())) score += 2;
        if (acc != null && Boolean.TRUE.equals(acc.getHasRamp())) score += 1;
        return score;
    }

    private double haversineKm(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue()))
                * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
