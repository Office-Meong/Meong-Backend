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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;

    public List<FavoriteResponse> getMyFavorites(Long userId, Region region, PlaceType placeType) {
        return favoriteRepository.findByUserIdWithPlace(userId, region, placeType).stream()
                .map(FavoriteResponse::from)
                .toList();
    }

    @Transactional
    public FavoriteResponse addFavorite(Long userId, Long placeId) {
        if (favoriteRepository.existsByUserIdAndPlaceId(userId, placeId)) {
            throw new IllegalStateException("이미 즐겨찾기한 장소입니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId));
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new NoSuchElementException("장소를 찾을 수 없습니다: " + placeId));

        Favorite favorite = favoriteRepository.save(Favorite.builder()
                .user(user)
                .place(place)
                .build());
        return FavoriteResponse.from(favorite);
    }

    @Transactional
    public void removeFavorite(Long userId, Long placeId) {
        Favorite favorite = favoriteRepository.findByUserIdAndPlaceId(userId, placeId)
                .orElseThrow(() -> new NoSuchElementException("즐겨찾기를 찾을 수 없습니다."));
        favoriteRepository.delete(favorite);
    }
}
