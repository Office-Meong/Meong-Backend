package com.officemeong.domain.place.service;

import com.officemeong.common.dto.PageResponse;
import com.officemeong.domain.place.dto.PlaceDetailResponse;
import com.officemeong.domain.place.dto.PlaceSummaryResponse;
import com.officemeong.domain.place.entity.Place;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;

    public enum SortType { SCORE, LATEST }

    public PageResponse<PlaceSummaryResponse> getPlaces(Region region, PlaceType placeType,
                                                         SortType sort, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Long> idPage = sort == SortType.LATEST
                ? placeRepository.findIdsByFilterOrderByLatest(region, placeType, pageRequest)
                : placeRepository.findIdsByFilterOrderByScore(region, placeType, pageRequest);

        if (idPage.isEmpty()) {
            return PageResponse.of(List.of(), page, size, 0);
        }

        List<Long> ids = idPage.getContent();
        List<Place> places = placeRepository.findByIdsWithSummaryDetails(ids);

        // ID 순서 보존 (DB 쿼리 결과는 IN 절 순서를 보장하지 않음)
        Map<Long, Place> placeMap = places.stream().collect(Collectors.toMap(Place::getId, Function.identity()));
        List<PlaceSummaryResponse> content = ids.stream()
                .filter(placeMap::containsKey)
                .map(id -> PlaceSummaryResponse.from(placeMap.get(id)))
                .toList();

        return PageResponse.of(content, page, size, idPage.getTotalElements());
    }

    public PlaceDetailResponse getPlace(Long id) {
        Place place = placeRepository.findByIdWithAllDetails(id)
                .orElseThrow(() -> new NoSuchElementException("장소를 찾을 수 없습니다: " + id));
        return PlaceDetailResponse.from(place);
    }
}
