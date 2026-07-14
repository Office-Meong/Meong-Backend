package com.officemeong.ai.dto;

import com.officemeong.domain.place.entity.Place;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "AI 장소 추천 응답")
public class PlaceRecommendResponse {

    @Schema(description = "장소 ID")
    private Long placeId;

    @Schema(description = "장소명")
    private String placeName;

    @Schema(description = "지역")
    private Region region;

    @Schema(description = "장소 유형")
    private PlaceType placeType;

    @Schema(description = "주소")
    private String address;

    @Schema(description = "펫워크 지수 (0~100)")
    private int totalScore;

    @Schema(description = "등급 (A~E)")
    private String grade;

    @Schema(description = "AI 추천 이유")
    private String reason;

    public static PlaceRecommendResponse of(Place place, String reason) {
        int score = 0;
        String grade = null;
        if (place.getScore() != null) {
            score = place.getScore().getTotalScore();
            grade = place.getScore().getGrade() != null ? place.getScore().getGrade().name() : null;
        }
        return PlaceRecommendResponse.builder()
                .placeId(place.getId())
                .placeName(place.getName())
                .region(place.getRegion())
                .placeType(place.getPlaceType())
                .address(place.getAddress())
                .totalScore(score)
                .grade(grade)
                .reason(reason)
                .build();
    }
}
