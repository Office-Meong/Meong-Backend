package com.officemeong.domain.place.dto;

import com.officemeong.domain.congestion.enums.CongestionLevel;
import com.officemeong.domain.place.entity.Place;
import com.officemeong.domain.place.entity.PlaceImage;
import com.officemeong.domain.place.entity.PlaceScore;
import com.officemeong.domain.place.enums.AcmpyType;
import com.officemeong.domain.place.enums.Grade;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlaceSummaryResponse {

    private Long id;
    private String name;
    private Region region;
    private PlaceType placeType;
    private String address;
    private String thumbnailUrl;
    private Grade grade;
    private int totalScore;
    private AcmpyType acmpyType;
    private int congestionScore;
    private CongestionLevel congestionLevel;
    private boolean isFavorite;

    public static PlaceSummaryResponse from(Place place) {
        return from(place, false);
    }

    public static PlaceSummaryResponse from(Place place, boolean isFavorite) {
        PlaceScore score = place.getScore();
        int congestionScore = score != null ? score.getCongestionScore() : 8;

        String thumbnail = place.getImages().stream()
                .filter(PlaceImage::isThumbnail)
                .map(PlaceImage::getImageUrl)
                .findFirst()
                .orElseGet(() -> place.getImages().isEmpty() ? null
                        : place.getImages().get(0).getImageUrl());

        return PlaceSummaryResponse.builder()
                .id(place.getId())
                .name(place.getName())
                .region(place.getRegion())
                .placeType(place.getPlaceType())
                .address(place.getAddress())
                .thumbnailUrl(thumbnail)
                .grade(score != null ? score.getGrade() : null)
                .totalScore(score != null ? score.getTotalScore() : 0)
                .acmpyType(place.getPetCondition() != null ? place.getPetCondition().getAcmpyType() : null)
                .congestionScore(congestionScore)
                .congestionLevel(CongestionLevel.fromScore(congestionScore))
                .isFavorite(isFavorite)
                .build();
    }
}
