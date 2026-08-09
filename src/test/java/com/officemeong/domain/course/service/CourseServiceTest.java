package com.officemeong.domain.course.service;

import com.officemeong.domain.congestion.entity.CongestionForecast;
import com.officemeong.domain.congestion.repository.CongestionForecastRepository;
import com.officemeong.domain.course.dto.ChecklistItemRequest;
import com.officemeong.domain.course.dto.ChecklistItemUpdateRequest;
import com.officemeong.domain.course.dto.CourseCreateRequest;
import com.officemeong.domain.course.dto.CourseItemResponse;
import com.officemeong.domain.course.dto.CourseSummaryResponse;
import com.officemeong.domain.course.dto.CourseResponse;
import com.officemeong.domain.course.entity.Course;
import com.officemeong.domain.course.entity.CourseChecklistItem;
import com.officemeong.domain.course.entity.CourseItem;
import com.officemeong.domain.course.enums.WorkFocusLevel;
import com.officemeong.domain.course.repository.CourseChecklistItemRepository;
import com.officemeong.domain.course.repository.CourseItemRepository;
import com.officemeong.domain.course.repository.CourseRepository;
import com.officemeong.domain.dog.repository.DogRepository;
import com.officemeong.domain.place.entity.Place;
import com.officemeong.domain.place.entity.PlaceScore;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.place.repository.PlaceRepository;
import com.officemeong.domain.user.entity.User;
import com.officemeong.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService 단위 테스트")
class CourseServiceTest {

    @Mock CourseRepository courseRepository;
    @Mock CourseItemRepository courseItemRepository;
    @Mock CourseChecklistItemRepository checklistItemRepository;
    @Mock UserRepository userRepository;
    @Mock DogRepository dogRepository;
    @Mock PlaceRepository placeRepository;
    @Mock CongestionForecastRepository congestionForecastRepository;

    @InjectMocks CourseService courseService;

    private User mockUser;
    private CourseCreateRequest request;

    @BeforeEach
    void setUp() {
        mockUser = mock(User.class);
        lenient().when(mockUser.getId()).thenReturn(1L);

        request = mock(CourseCreateRequest.class);
        lenient().when(request.getRegion()).thenReturn(Region.GANGNEUNG);
        lenient().when(request.getStartDate()).thenReturn(LocalDate.of(2026, 8, 1));
        lenient().when(request.getEndDate()).thenReturn(LocalDate.of(2026, 8, 1));
        lenient().when(request.getWorkStartTime()).thenReturn(LocalTime.of(9, 0));
        lenient().when(request.getWorkEndTime()).thenReturn(LocalTime.of(18, 0));
        lenient().when(request.getWorkFocusLevel()).thenReturn(WorkFocusLevel.MEDIUM);
        lenient().when(request.getDogId()).thenReturn(null);
        lenient().when(request.getName()).thenReturn(null);
    }

    @Test
    @DisplayName("코스 생성 - 1일, MEDIUM, 장소 충분 → 코스 반환")
    void createCourse_1일_MEDIUM_성공() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        stubPlacesByType(Region.GANGNEUNG);
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseResponse result = courseService.createCourse(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getRegion()).isEqualTo(Region.GANGNEUNG);
        // MEDIUM 1일: 아침(FOOD) + 오전업무(WORK_PLACE) + 점심(FOOD) + 점심산책(WALK) + 오후업무(WORK_PLACE) + 저녁산책(WALK) + 저녁(FOOD)
        // WALK는 placeType이 중복되지 않도록 usedPlaceIds로 관리되므로 장소가 있는 슬롯만 포함됨
        assertThat(result.getDayItems()).containsKey(1);
    }

    @Test
    @DisplayName("코스 생성 - 여행 기간 혼잡도 예측이 있으면 장소 순위에 반영")
    void createCourse_여행일혼잡도_반영() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(request.getWorkFocusLevel()).thenReturn(WorkFocusLevel.LOW);
        stubPlacesByType(Region.GANGNEUNG);

        // "오늘 기준" DB 정렬 순서: B(총점 60, 혼잡도점수 15=한산) > A(총점 50, 혼잡도점수 1=매우혼잡)
        Place placeA = mockPlaceWithScore(201L, PlaceType.TOUR, 50, 1);
        Place placeB = mockPlaceWithScore(202L, PlaceType.TOUR, 60, 15);
        when(placeRepository.findByRegionAndPlaceTypeOrderByScore(Region.GANGNEUNG, PlaceType.TOUR))
                .thenReturn(List.of(placeB, placeA));

        // 여행 기간 예측: 매우 한산(10%) → 혼잡도점수 15로 환산
        CongestionForecast forecast = mock(CongestionForecast.class);
        when(forecast.getCnctrRate()).thenReturn(BigDecimal.valueOf(10));
        when(congestionForecastRepository.findByRegionAndBaseYmdBetween(
                eq(Region.GANGNEUNG), any(), any()))
                .thenReturn(List.of(forecast));

        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseResponse result = courseService.createCourse(1L, request);

        // 여행일 기준 재계산 점수: A = 50-1+15=64, B = 60-15+15=60 → A가 먼저 선택되어야 함
        CourseItemResponse firstTourItem = result.getDayItems().get(1).stream()
                .filter(i -> i.getPlaceType() == PlaceType.TOUR)
                .findFirst()
                .orElseThrow();
        assertThat(firstTourItem.getPlaceId()).isEqualTo(201L);
    }

    @Test
    @DisplayName("코스 생성 - 사용자 없으면 NoSuchElementException")
    void createCourse_사용자없음_예외() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.createCourse(99L, request))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("코스 생성 - 해당 유형 장소 없어도 슬롯 스킵하고 코스 반환")
    void createCourse_장소없는슬롯_스킵() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        // 모든 타입 빈 리스트 반환
        for (PlaceType type : PlaceType.values()) {
            lenient().when(placeRepository.findByRegionAndPlaceTypeOrderByScore(Region.GANGNEUNG, type))
                    .thenReturn(List.of());
        }
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseResponse result = courseService.createCourse(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getDayItems().getOrDefault(1, List.of())).isEmpty();
    }

    @Test
    @DisplayName("코스 조회 - 존재하지 않는 코스 NoSuchElementException")
    void getCourse_없는코스_예외() {
        when(courseRepository.findByIdAndUserIdWithItems(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourse(1L, 99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("코스 삭제 - 성공")
    void deleteCourse_성공() {
        Course course = mockCourse(1L);
        when(courseRepository.findByIdAndUserIdWithItems(1L, 1L)).thenReturn(Optional.of(course));

        courseService.deleteCourse(1L, 1L);

        verify(courseRepository).delete(course);
    }

    @Test
    @DisplayName("대체 장소 추천 - 같은 타입, 최대 5개")
    void getAlternatives_추천_성공() {
        Course course = mockCourseWithItem(1L, 42L, PlaceType.FOOD);
        when(courseRepository.findByIdAndUserIdWithItems(1L, 1L)).thenReturn(Optional.of(course));

        Place alt1 = mockPlace(100L, PlaceType.FOOD);
        Place alt2 = mockPlace(101L, PlaceType.FOOD);
        when(placeRepository.findByRegionAndPlaceTypeOrderByScore(Region.GANGNEUNG, PlaceType.FOOD))
                .thenReturn(List.of(alt1, alt2));

        var result = courseService.getAlternatives(1L, 1L, 10L);

        assertThat(result).hasSize(2);
    }

    // ──── 체크리스트 ────

    @Test
    @DisplayName("체크리스트 조회 - 표시 순서대로 반환")
    void getChecklist_성공() {
        Course course = mockCourse(1L);
        when(courseRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(course));
        CourseChecklistItem item = mockChecklistItem(5L, "목줄 챙기기", false, 1);
        when(checklistItemRepository.findByCourseIdOrderByDisplayOrderAsc(1L)).thenReturn(List.of(item));

        var result = courseService.getChecklist(1L, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("목줄 챙기기");
    }

    @Test
    @DisplayName("체크리스트 조회 - 코스 없으면 예외")
    void getChecklist_코스없음_예외() {
        when(courseRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getChecklist(1L, 99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("체크리스트 항목 추가 - 다음 순서로 저장")
    void addChecklistItem_성공() {
        Course course = mockCourse(1L);
        when(courseRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(course));
        when(checklistItemRepository.countByCourseId(1L)).thenReturn(2);

        ChecklistItemRequest request = mock(ChecklistItemRequest.class);
        when(request.getContent()).thenReturn("사료 챙기기");

        var result = courseService.addChecklistItem(1L, 1L, request);

        assertThat(result.getContent()).isEqualTo("사료 챙기기");
        assertThat(result.getDisplayOrder()).isEqualTo(3);
        assertThat(result.isChecked()).isFalse();
        verify(checklistItemRepository).save(any(CourseChecklistItem.class));
    }

    @Test
    @DisplayName("체크리스트 항목 수정 - 체크 토글")
    void updateChecklistItem_체크토글_성공() {
        Course course = mockCourse(1L);
        when(courseRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(course));
        CourseChecklistItem item = mockChecklistItem(5L, "목줄 챙기기", false, 1);
        when(item.getCourse()).thenReturn(course);
        when(checklistItemRepository.findById(5L)).thenReturn(Optional.of(item));

        ChecklistItemUpdateRequest request = mock(ChecklistItemUpdateRequest.class);
        lenient().when(request.getContent()).thenReturn(null);
        when(request.getChecked()).thenReturn(true);

        courseService.updateChecklistItem(1L, 1L, 5L, request);

        verify(item).toggleChecked(true);
        verify(item, never()).updateContent(any());
    }

    @Test
    @DisplayName("체크리스트 항목 수정 - 다른 코스 소속이면 예외")
    void updateChecklistItem_다른코스_예외() {
        Course course = mockCourse(1L);
        when(courseRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(course));
        CourseChecklistItem item = mockChecklistItem(5L, "목줄 챙기기", false, 1);
        Course otherCourse = mockCourse(2L);
        when(item.getCourse()).thenReturn(otherCourse);
        when(checklistItemRepository.findById(5L)).thenReturn(Optional.of(item));

        ChecklistItemUpdateRequest request = mock(ChecklistItemUpdateRequest.class);

        assertThatThrownBy(() -> courseService.updateChecklistItem(1L, 1L, 5L, request))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("체크리스트 항목 삭제 - 성공")
    void deleteChecklistItem_성공() {
        Course course = mockCourse(1L);
        when(courseRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(course));
        CourseChecklistItem item = mockChecklistItem(5L, "목줄 챙기기", false, 1);
        when(item.getCourse()).thenReturn(course);
        when(checklistItemRepository.findById(5L)).thenReturn(Optional.of(item));

        courseService.deleteChecklistItem(1L, 1L, 5L);

        verify(checklistItemRepository).delete(item);
    }

    private CourseChecklistItem mockChecklistItem(Long id, String content, boolean checked, int order) {
        CourseChecklistItem item = mock(CourseChecklistItem.class);
        lenient().when(item.getId()).thenReturn(id);
        lenient().when(item.getContent()).thenReturn(content);
        lenient().when(item.isChecked()).thenReturn(checked);
        lenient().when(item.getDisplayOrder()).thenReturn(order);
        return item;
    }

    // ──── 헬퍼 ────

    private void stubPlacesByType(Region region) {
        for (PlaceType type : PlaceType.values()) {
            Place p1 = mockPlace((long) type.ordinal() * 10 + 1, type);
            Place p2 = mockPlace((long) type.ordinal() * 10 + 2, type);
            lenient().when(placeRepository.findByRegionAndPlaceTypeOrderByScore(region, type))
                    .thenReturn(List.of(p1, p2));
        }
    }

    private Place mockPlace(Long id, PlaceType type) {
        Place p = mock(Place.class);
        lenient().when(p.getId()).thenReturn(id);
        lenient().when(p.getName()).thenReturn("장소 " + id);
        lenient().when(p.getPlaceType()).thenReturn(type);
        lenient().when(p.getAddress()).thenReturn("강원도 강릉시 테스트로 " + id);
        lenient().when(p.getLatitude()).thenReturn(null);
        lenient().when(p.getLongitude()).thenReturn(null);
        lenient().when(p.getPetCondition()).thenReturn(null);
        lenient().when(p.getScore()).thenReturn(null);
        return p;
    }

    private Place mockPlaceWithScore(Long id, PlaceType type, int totalScore, int congestionScore) {
        Place p = mockPlace(id, type);
        PlaceScore score = mock(PlaceScore.class);
        lenient().when(score.getTotalScore()).thenReturn(totalScore);
        lenient().when(score.getCongestionScore()).thenReturn(congestionScore);
        lenient().when(p.getScore()).thenReturn(score);
        return p;
    }

    private Course mockCourse(Long id) {
        Course c = mock(Course.class);
        lenient().when(c.getId()).thenReturn(id);
        lenient().when(c.getName()).thenReturn("강릉 1일 워케이션");
        lenient().when(c.getRegion()).thenReturn(Region.GANGNEUNG);
        lenient().when(c.getStartDate()).thenReturn(LocalDate.of(2026, 8, 1));
        lenient().when(c.getEndDate()).thenReturn(LocalDate.of(2026, 8, 1));
        lenient().when(c.getItems()).thenReturn(List.of());
        return c;
    }

    private Course mockCourseWithItem(Long courseId, Long placeId, PlaceType placeType) {
        Place place = mockPlace(placeId, placeType);
        CourseItem item = mock(CourseItem.class);
        lenient().when(item.getId()).thenReturn(10L);
        lenient().when(item.getPlace()).thenReturn(place);

        Course course = mock(Course.class);
        lenient().when(course.getId()).thenReturn(courseId);
        lenient().when(course.getName()).thenReturn("강릉 1일 워케이션");
        lenient().when(course.getRegion()).thenReturn(Region.GANGNEUNG);
        lenient().when(course.getStartDate()).thenReturn(LocalDate.of(2026, 8, 1));
        lenient().when(course.getEndDate()).thenReturn(LocalDate.of(2026, 8, 1));
        lenient().when(course.getItems()).thenReturn(List.of(item));
        return course;
    }
}
