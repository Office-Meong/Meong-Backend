package com.officemeong.domain.walk.service;

import com.officemeong.domain.dog.entity.Dog;
import com.officemeong.domain.dog.repository.DogRepository;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.walk.dto.CourseRecommendResponse;
import com.officemeong.domain.walk.entity.WalkCourse;
import com.officemeong.domain.walk.repository.WalkCourseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService 단위 테스트")
class CourseServiceTest {

    @Mock WalkCourseRepository walkCourseRepository;
    @Mock DogRepository dogRepository;

    @InjectMocks CourseService courseService;

    // 강릉 경포 인근 좌표 (기준점)
    private static final double BASE_LAT = 37.796;
    private static final double BASE_LNG = 128.900;

    @Test
    @DisplayName("dogId 없이 위치 기반 코스 추천 - 반경 내 코스 반환")
    void recommend_dogId없음_위치기반_반환() {
        WalkCourse near = course(1L, 37.797, 128.901, 2.0);  // 약 0.15km
        WalkCourse far  = course(2L, 38.200, 129.300, 3.0);  // 약 60km → 반경 초과
        when(walkCourseRepository.findAll()).thenReturn(List.of(near, far));

        List<CourseRecommendResponse> result = courseService.recommend(BASE_LAT, BASE_LNG, 1L, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("소형견 (5kg) - 3km 이하 코스만 반환")
    void recommend_소형견_3km이하_필터() {
        WalkCourse short_ = course(1L, 37.797, 128.901, 2.5);
        WalkCourse long_  = course(2L, 37.798, 128.902, 4.0);
        when(walkCourseRepository.findAll()).thenReturn(List.of(short_, long_));

        Dog dog = mockDog(1L, 5.0);
        when(dogRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(dog));

        List<CourseRecommendResponse> result = courseService.recommend(BASE_LAT, BASE_LNG, 1L, 10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("중형견 (15kg) - 6km 이하 코스만 반환")
    void recommend_중형견_6km이하_필터() {
        WalkCourse c1 = course(1L, 37.797, 128.901, 5.0);
        WalkCourse c2 = course(2L, 37.798, 128.902, 7.0);
        when(walkCourseRepository.findAll()).thenReturn(List.of(c1, c2));

        Dog dog = mockDog(2L, 15.0);
        when(dogRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(dog));

        List<CourseRecommendResponse> result = courseService.recommend(BASE_LAT, BASE_LNG, 1L, 20L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("대형견 (30kg) - 거리 제한 없이 모든 코스 반환")
    void recommend_대형견_제한없음() {
        WalkCourse c1 = course(1L, 37.797, 128.901, 2.0);
        WalkCourse c2 = course(2L, 37.798, 128.902, 8.0);
        when(walkCourseRepository.findAll()).thenReturn(List.of(c1, c2));

        Dog dog = mockDog(3L, 30.0);
        when(dogRepository.findByIdAndUserId(30L, 1L)).thenReturn(Optional.of(dog));

        List<CourseRecommendResponse> result = courseService.recommend(BASE_LAT, BASE_LNG, 1L, 30L);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("결과는 사용자로부터 가까운 순 정렬")
    void recommend_가까운순_정렬() {
        WalkCourse farFirst  = course(1L, 37.800, 128.910, 3.0);  // 더 멀리
        WalkCourse nearFirst = course(2L, 37.797, 128.901, 3.0);  // 더 가까이
        when(walkCourseRepository.findAll()).thenReturn(List.of(farFirst, nearFirst));

        List<CourseRecommendResponse> result = courseService.recommend(BASE_LAT, BASE_LNG, 1L, null);

        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(1).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("존재하지 않는 반려견 ID - NoSuchElementException 발생")
    void recommend_없는_반려견_예외() {
        when(dogRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.recommend(BASE_LAT, BASE_LNG, 1L, 99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("코스 없을 때 빈 리스트 반환")
    void recommend_코스없음_빈리스트() {
        when(walkCourseRepository.findAll()).thenReturn(List.of());

        List<CourseRecommendResponse> result = courseService.recommend(BASE_LAT, BASE_LNG, 1L, null);

        assertThat(result).isEmpty();
    }

    private WalkCourse course(Long id, double lat, double lng, double distKm) {
        WalkCourse c = mock(WalkCourse.class);
        lenient().when(c.getId()).thenReturn(id);
        lenient().when(c.getCourseName()).thenReturn("테스트 코스 " + id);
        lenient().when(c.getRegion()).thenReturn(Region.GANGNEUNG);
        lenient().when(c.getStartLatitude()).thenReturn(BigDecimal.valueOf(lat));
        lenient().when(c.getStartLongitude()).thenReturn(BigDecimal.valueOf(lng));
        lenient().when(c.getDistanceKm()).thenReturn(BigDecimal.valueOf(distKm));
        return c;
    }

    private Dog mockDog(Long id, double weightKg) {
        Dog dog = mock(Dog.class);
        lenient().when(dog.getId()).thenReturn(id);
        lenient().when(dog.getWeightKg()).thenReturn(BigDecimal.valueOf(weightKg));
        return dog;
    }
}
