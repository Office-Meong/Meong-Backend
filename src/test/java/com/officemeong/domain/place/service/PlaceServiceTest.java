package com.officemeong.domain.place.service;

import com.officemeong.common.dto.PageResponse;
import com.officemeong.domain.place.dto.PlaceDetailResponse;
import com.officemeong.domain.place.dto.PlaceSummaryResponse;
import com.officemeong.domain.place.entity.Place;
import com.officemeong.domain.place.enums.CongestionLevel;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.place.repository.PlaceRepository;
import com.officemeong.domain.walk.dto.NearbyWalkCourseResponse;
import com.officemeong.domain.walk.entity.WalkCourse;
import com.officemeong.domain.walk.repository.WalkCourseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlaceService 단위 테스트")
class PlaceServiceTest {

    @Mock PlaceRepository placeRepository;
    @Mock WalkCourseRepository walkCourseRepository;

    @InjectMocks PlaceService placeService;

    @Test
    @DisplayName("존재하지 않는 장소 조회 시 예외 발생")
    void getPlace_존재하지_않는_장소_예외_발생() {
        when(placeRepository.findByIdWithAllDetails(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placeService.getPlace(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("존재하는 장소 상세 조회")
    void getPlace_장소_상세_반환() {
        Place place = mockPlace(1L, "테스트 카페", Region.GANGNEUNG, PlaceType.WORK_PLACE);
        when(placeRepository.findByIdWithAllDetails(1L)).thenReturn(Optional.of(place));

        PlaceDetailResponse response = placeService.getPlace(1L);

        assertThat(response.getName()).isEqualTo("테스트 카페");
        assertThat(response.getRegion()).isEqualTo(Region.GANGNEUNG);
        assertThat(response.getPlaceType()).isEqualTo(PlaceType.WORK_PLACE);
    }

    @Test
    @DisplayName("장소 목록 - 점수순 정렬, 빈 결과")
    void getPlaces_빈_결과_반환() {
        when(placeRepository.findIdsByFilterOrderByScore(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        PageResponse<PlaceSummaryResponse> response =
                placeService.getPlaces(null, null, PlaceService.SortType.SCORE, null, 0, 20);

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("장소 목록 - 점수순 정렬, 결과 반환")
    void getPlaces_점수순_정렬_결과_반환() {
        Place place1 = mockPlace(1L, "카페A", Region.GANGNEUNG, PlaceType.WORK_PLACE);
        Place place2 = mockPlace(2L, "카페B", Region.GANGNEUNG, PlaceType.FOOD);

        when(placeRepository.findIdsByFilterOrderByScore(eq(Region.GANGNEUNG), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(1L, 2L), PageRequest.of(0, 20), 2));
        when(placeRepository.findByIdsWithSummaryDetails(List.of(1L, 2L)))
                .thenReturn(List.of(place1, place2));

        PageResponse<PlaceSummaryResponse> response =
                placeService.getPlaces(Region.GANGNEUNG, null, PlaceService.SortType.SCORE, null, 0, 20);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getContent().get(0).getName()).isEqualTo("카페A");
    }

    @Test
    @DisplayName("장소 목록 - 최신순 정렬")
    void getPlaces_최신순_정렬_결과_반환() {
        Place place = mockPlace(1L, "최신 장소", Region.CHUNCHEON, PlaceType.STAY);

        when(placeRepository.findIdsByFilterOrderByLatest(isNull(), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(1L), PageRequest.of(0, 20), 1));
        when(placeRepository.findByIdsWithSummaryDetails(List.of(1L)))
                .thenReturn(List.of(place));

        PageResponse<PlaceSummaryResponse> response =
                placeService.getPlaces(null, null, PlaceService.SortType.LATEST, null, 0, 20);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getName()).isEqualTo("최신 장소");
    }

    @Test
    @DisplayName("점수 없는 장소 목록 조회 시 기본값 사용")
    void getPlaces_점수없는_장소_기본값_반환() {
        Place place = mockPlace(1L, "점수없는 장소", Region.WONJU, PlaceType.TOUR);

        when(placeRepository.findIdsByFilterOrderByScore(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(1L), PageRequest.of(0, 20), 1));
        when(placeRepository.findByIdsWithSummaryDetails(List.of(1L)))
                .thenReturn(List.of(place));

        PageResponse<PlaceSummaryResponse> response =
                placeService.getPlaces(null, null, PlaceService.SortType.SCORE, null, 0, 20);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getTotalScore()).isZero();
    }

    @Test
    @DisplayName("장소 목록 - 혼잡도 RELAXED 필터 적용")
    void getPlaces_혼잡도_RELAXED_필터_적용() {
        Place place = mockPlace(1L, "여유로운 카페", Region.GANGNEUNG, PlaceType.WORK_PLACE);

        when(placeRepository.findIdsByFilterWithCongestionOrderByScore(
                isNull(), isNull(), eq(12), eq(15), any()))
                .thenReturn(new PageImpl<>(List.of(1L), PageRequest.of(0, 20), 1));
        when(placeRepository.findByIdsWithSummaryDetails(List.of(1L)))
                .thenReturn(List.of(place));

        PageResponse<PlaceSummaryResponse> response =
                placeService.getPlaces(null, null, PlaceService.SortType.SCORE, CongestionLevel.RELAXED, 0, 20);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getName()).isEqualTo("여유로운 카페");
    }

    @Test
    @DisplayName("장소 목록 - 혼잡도 VERY_CROWDED 필터 + 최신순")
    void getPlaces_혼잡도_VERY_CROWDED_최신순() {
        when(placeRepository.findIdsByFilterWithCongestionOrderByLatest(
                isNull(), isNull(), eq(0), eq(5), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        PageResponse<PlaceSummaryResponse> response =
                placeService.getPlaces(null, null, PlaceService.SortType.LATEST, CongestionLevel.VERY_CROWDED, 0, 20);

        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("장소 주변 산책 코스 - 10km 이내 코스 반환")
    void getNearbyWalkCourses_10km이내_반환() {
        Place place = mockPlaceWithCoords(1L, Region.GANGNEUNG, 37.796, 128.900);
        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));

        WalkCourse near = mockWalkCourse(10L, 37.797, 128.901, 2.5);
        WalkCourse far  = mockWalkCourse(11L, 38.200, 129.500, 3.0);
        when(walkCourseRepository.findByRegion(Region.GANGNEUNG)).thenReturn(List.of(near, far));

        List<NearbyWalkCourseResponse> result = placeService.getNearbyWalkCourses(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("장소 주변 산책 코스 - 존재하지 않는 장소 예외")
    void getNearbyWalkCourses_없는장소_예외() {
        when(placeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placeService.getNearbyWalkCourses(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("장소 주변 산책 코스 - 좌표 없는 장소는 빈 리스트")
    void getNearbyWalkCourses_좌표없는장소_빈리스트() {
        // latitude/longitude 미설정 → mock 기본값 null
        Place place = mock(Place.class);
        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));

        List<NearbyWalkCourseResponse> result = placeService.getNearbyWalkCourses(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("장소 주변 산책 코스 - 거리순 정렬")
    void getNearbyWalkCourses_거리순_정렬() {
        Place place = mockPlaceWithCoords(1L, Region.GANGNEUNG, 37.796, 128.900);
        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));

        WalkCourse far   = mockWalkCourse(10L, 37.800, 128.910, 3.0);
        WalkCourse near  = mockWalkCourse(11L, 37.797, 128.901, 2.0);
        when(walkCourseRepository.findByRegion(Region.GANGNEUNG)).thenReturn(List.of(far, near));

        List<NearbyWalkCourseResponse> result = placeService.getNearbyWalkCourses(1L);

        assertThat(result.get(0).getId()).isEqualTo(11L);
        assertThat(result.get(1).getId()).isEqualTo(10L);
    }

    private Place mockPlaceWithCoords(Long id, Region region, double lat, double lng) {
        Place place = mockPlace(id, "테스트 장소 " + id, region, PlaceType.TOUR);
        lenient().when(place.getLatitude()).thenReturn(BigDecimal.valueOf(lat));
        lenient().when(place.getLongitude()).thenReturn(BigDecimal.valueOf(lng));
        return place;
    }

    private WalkCourse mockWalkCourse(Long id, double lat, double lng, double distKm) {
        WalkCourse c = mock(WalkCourse.class);
        lenient().when(c.getId()).thenReturn(id);
        lenient().when(c.getCourseName()).thenReturn("코스 " + id);
        lenient().when(c.getRegion()).thenReturn(Region.GANGNEUNG);
        lenient().when(c.getStartLatitude()).thenReturn(BigDecimal.valueOf(lat));
        lenient().when(c.getStartLongitude()).thenReturn(BigDecimal.valueOf(lng));
        lenient().when(c.getDistanceKm()).thenReturn(BigDecimal.valueOf(distKm));
        return c;
    }

    /**
     * Place.getId()가 null이면 Collectors.toMap에서 duplicate key null 오류 발생.
     * mock 객체를 사용해 getId()가 실제 ID를 반환하도록 한다.
     */
    private Place mockPlace(Long id, String name, Region region, PlaceType placeType) {
        Place place = mock(Place.class);
        lenient().when(place.getId()).thenReturn(id);
        lenient().when(place.getName()).thenReturn(name);
        lenient().when(place.getRegion()).thenReturn(region);
        lenient().when(place.getPlaceType()).thenReturn(placeType);
        lenient().when(place.getAddress()).thenReturn("강원도 테스트 주소");
        lenient().when(place.getImages()).thenReturn(List.of());
        lenient().when(place.getScore()).thenReturn(null);
        lenient().when(place.getPetCondition()).thenReturn(null);
        return place;
    }
}
