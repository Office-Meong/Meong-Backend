package com.officemeong.domain.place.service;

import com.officemeong.common.dto.PageResponse;
import com.officemeong.domain.place.dto.PlaceDetailResponse;
import com.officemeong.domain.place.dto.PlaceSummaryResponse;
import com.officemeong.domain.place.entity.Place;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.place.repository.PlaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
                placeService.getPlaces(null, null, PlaceService.SortType.SCORE, 0, 20);

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
                placeService.getPlaces(Region.GANGNEUNG, null, PlaceService.SortType.SCORE, 0, 20);

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
                placeService.getPlaces(null, null, PlaceService.SortType.LATEST, 0, 20);

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
                placeService.getPlaces(null, null, PlaceService.SortType.SCORE, 0, 20);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getTotalScore()).isZero();
    }

    /**
     * Place.getId()가 null이면 Collectors.toMap에서 duplicate key null 오류 발생.
     * mock 객체를 사용해 getId()가 실제 ID를 반환하도록 한다.
     */
    private Place mockPlace(Long id, String name, Region region, PlaceType placeType) {
        Place place = mock(Place.class);
        when(place.getId()).thenReturn(id);
        when(place.getName()).thenReturn(name);
        when(place.getRegion()).thenReturn(region);
        when(place.getPlaceType()).thenReturn(placeType);
        when(place.getAddress()).thenReturn("강원도 테스트 주소");
        when(place.getImages()).thenReturn(List.of());
        when(place.getScore()).thenReturn(null);
        when(place.getPetCondition()).thenReturn(null);
        return place;
    }
}
