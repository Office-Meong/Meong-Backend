package com.officemeong.domain.favorite.service;

import com.officemeong.domain.favorite.dto.FavoriteResponse;
import com.officemeong.domain.favorite.entity.Favorite;
import com.officemeong.domain.favorite.repository.FavoriteRepository;
import com.officemeong.domain.place.entity.Place;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.place.repository.PlaceRepository;
import com.officemeong.domain.user.entity.User;
import com.officemeong.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FavoriteService 단위 테스트")
class FavoriteServiceTest {

    @Mock FavoriteRepository favoriteRepository;
    @Mock UserRepository userRepository;
    @Mock PlaceRepository placeRepository;

    @InjectMocks FavoriteService favoriteService;

    @Test
    @DisplayName("즐겨찾기 목록 조회")
    void getMyFavorites_목록_반환() {
        Favorite fav = mockFavorite(1L, 10L, "강릉 카페");
        when(favoriteRepository.findByUserIdWithPlace(1L)).thenReturn(List.of(fav));

        List<FavoriteResponse> result = favoriteService.getMyFavorites(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlaceName()).isEqualTo("강릉 카페");
    }

    @Test
    @DisplayName("즐겨찾기 목록 - 빈 결과")
    void getMyFavorites_빈_결과() {
        when(favoriteRepository.findByUserIdWithPlace(1L)).thenReturn(List.of());

        List<FavoriteResponse> result = favoriteService.getMyFavorites(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("즐겨찾기 추가 성공")
    void addFavorite_추가_성공() {
        when(favoriteRepository.existsByUserIdAndPlaceId(1L, 10L)).thenReturn(false);
        User user = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Favorite saved = mockFavorite(1L, 10L, "강릉 카페");
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(saved);

        Place place = mock(Place.class);
        when(placeRepository.findById(10L)).thenReturn(Optional.of(place));

        FavoriteResponse response = favoriteService.addFavorite(1L, 10L);

        assertThat(response.getPlaceName()).isEqualTo("강릉 카페");
    }

    @Test
    @DisplayName("이미 즐겨찾기한 장소 추가 시 예외")
    void addFavorite_중복_예외() {
        when(favoriteRepository.existsByUserIdAndPlaceId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> favoriteService.addFavorite(1L, 10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 즐겨찾기");
    }

    @Test
    @DisplayName("존재하지 않는 장소 즐겨찾기 시 예외")
    void addFavorite_없는_장소_예외() {
        when(favoriteRepository.existsByUserIdAndPlaceId(1L, 99L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mock(User.class)));
        when(placeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteService.addFavorite(1L, 99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("즐겨찾기 취소 성공")
    void removeFavorite_취소_성공() {
        Favorite fav = mockFavorite(1L, 10L, "강릉 카페");
        when(favoriteRepository.findByUserIdAndPlaceId(1L, 10L)).thenReturn(Optional.of(fav));

        favoriteService.removeFavorite(1L, 10L);

        verify(favoriteRepository).delete(fav);
    }

    @Test
    @DisplayName("없는 즐겨찾기 취소 시 예외")
    void removeFavorite_없는_즐겨찾기_예외() {
        when(favoriteRepository.findByUserIdAndPlaceId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteService.removeFavorite(1L, 99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    private Favorite mockFavorite(Long userId, Long placeId, String placeName) {
        Place place = mock(Place.class);
        lenient().when(place.getId()).thenReturn(placeId);
        lenient().when(place.getName()).thenReturn(placeName);
        lenient().when(place.getPlaceType()).thenReturn(PlaceType.FOOD);
        lenient().when(place.getRegion()).thenReturn(Region.GANGNEUNG);
        lenient().when(place.getAddress()).thenReturn("강원도 강릉시 테스트로 1");

        User user = mock(User.class);
        lenient().when(user.getId()).thenReturn(userId);

        Favorite fav = mock(Favorite.class);
        lenient().when(fav.getPlace()).thenReturn(place);
        lenient().when(fav.getUser()).thenReturn(user);
        lenient().when(fav.getCreatedAt()).thenReturn(null);
        return fav;
    }
}
