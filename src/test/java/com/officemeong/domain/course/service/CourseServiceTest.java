package com.officemeong.domain.course.service;

import com.officemeong.domain.course.dto.CourseCreateRequest;
import com.officemeong.domain.course.dto.CourseResponse;
import com.officemeong.domain.course.entity.Course;
import com.officemeong.domain.course.entity.CourseItem;
import com.officemeong.domain.course.enums.WorkFocusLevel;
import com.officemeong.domain.course.repository.CourseItemRepository;
import com.officemeong.domain.course.repository.CourseRepository;
import com.officemeong.domain.dog.repository.DogRepository;
import com.officemeong.domain.place.entity.Place;
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
    @Mock UserRepository userRepository;
    @Mock DogRepository dogRepository;
    @Mock PlaceRepository placeRepository;

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

    private Course mockCourse(Long id) {
        Course c = mock(Course.class);
        lenient().when(c.getId()).thenReturn(id);
        lenient().when(c.getRegion()).thenReturn(Region.GANGNEUNG);
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
        lenient().when(course.getRegion()).thenReturn(Region.GANGNEUNG);
        lenient().when(course.getItems()).thenReturn(List.of(item));
        return course;
    }
}
