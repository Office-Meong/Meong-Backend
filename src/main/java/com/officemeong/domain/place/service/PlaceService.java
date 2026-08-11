package com.officemeong.domain.place.service;

import com.officemeong.common.dto.PageResponse;
import com.officemeong.domain.favorite.repository.FavoriteRepository;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private static final double NEARBY_WALK_RADIUS_KM = 10.0;
    private static final int NEARBY_WALK_MAX = 10;

    private final PlaceRepository placeRepository;
    private final WalkCourseRepository walkCourseRepository;
    private final FavoriteRepository favoriteRepository;

    public enum SortType { SCORE, LATEST }

    public PageResponse<PlaceSummaryResponse> getPlaces(Region region, PlaceType placeType,
                                                         SortType sort, CongestionLevel congestion,
                                                         String keyword, int page, int size, Long userId) {
        PageRequest pageRequest = PageRequest.of(page, size);
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Page<Long> idPage;
        if (congestion != null) {
            Integer minScore = congestion.getMinScore();
            Integer maxScore = congestion.getMaxScore();
            idPage = sort == SortType.LATEST
                    ? placeRepository.findIdsByFilterWithCongestionOrderByLatest(region, placeType, minScore, maxScore, normalizedKeyword, pageRequest)
                    : placeRepository.findIdsByFilterWithCongestionOrderByScore(region, placeType, minScore, maxScore, normalizedKeyword, pageRequest);
        } else {
            idPage = sort == SortType.LATEST
                    ? placeRepository.findIdsByFilterOrderByLatest(region, placeType, normalizedKeyword, pageRequest)
                    : placeRepository.findIdsByFilterOrderByScore(region, placeType, normalizedKeyword, pageRequest);
        }

        if (idPage.isEmpty()) {
            return PageResponse.of(List.of(), page, size, 0);
        }

        List<Long> ids = idPage.getContent();
        List<Place> places = placeRepository.findByIdsWithSummaryDetails(ids);

        Set<Long> favoritedIds = userId != null
                ? Set.copyOf(favoriteRepository.findFavoritedPlaceIds(userId, ids))
                : Set.of();

        Map<Long, Place> placeMap = places.stream().collect(Collectors.toMap(Place::getId, Function.identity()));
        List<PlaceSummaryResponse> content = ids.stream()
                .filter(placeMap::containsKey)
                .map(id -> PlaceSummaryResponse.from(placeMap.get(id), favoritedIds.contains(id)))
                .toList();

        return PageResponse.of(content, page, size, idPage.getTotalElements());
    }

    public PlaceDetailResponse getPlace(Long id, Long userId) {
        Place place = placeRepository.findByIdWithAllDetails(id)
                .orElseThrow(() -> new NoSuchElementException("장소를 찾을 수 없습니다: " + id));
        boolean isFavorite = userId != null && favoriteRepository.existsByUserIdAndPlaceId(userId, id);
        return PlaceDetailResponse.from(place, isFavorite);
    }

    public List<NearbyWalkCourseResponse> getNearbyWalkCourses(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new NoSuchElementException("장소를 찾을 수 없습니다: " + placeId));

        if (place.getLatitude() == null || place.getLongitude() == null) {
            return List.of();
        }

        double placeLat = place.getLatitude().doubleValue();
        double placeLng = place.getLongitude().doubleValue();

        return walkCourseRepository.findByRegion(place.getRegion()).stream()
                .filter(c -> c.getStartLatitude() != null && c.getStartLongitude() != null)
                .map(c -> {
                    double dist = haversineKm(placeLat, placeLng,
                            c.getStartLatitude().doubleValue(),
                            c.getStartLongitude().doubleValue());
                    return new WalkCourseWithDistance(c, dist);
                })
                .filter(cd -> cd.distance() <= NEARBY_WALK_RADIUS_KM)
                .sorted(Comparator.comparingDouble(WalkCourseWithDistance::distance))
                .limit(NEARBY_WALK_MAX)
                .map(cd -> NearbyWalkCourseResponse.from(cd.course(),
                        BigDecimal.valueOf(cd.distance()).setScale(2, RoundingMode.HALF_UP).doubleValue()))
                .toList();
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

    private record WalkCourseWithDistance(WalkCourse course, double distance) {}
}
