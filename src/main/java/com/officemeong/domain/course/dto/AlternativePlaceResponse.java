package com.officemeong.domain.course.dto;

import com.officemeong.domain.place.entity.Place;
import com.officemeong.domain.place.enums.PlaceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Schema(description = "대체 장소 추천 응답")
@Getter
@Builder
public class AlternativePlaceResponse {

    @Schema(description = "장소 ID", example = "55")
    private Long placeId;

    @Schema(description = "장소명", example = "춘천 반려견 카페")
    private String placeName;

    @Schema(description = "장소 유형", example = "FOOD")
    private PlaceType placeType;

    @Schema(description = "주소", example = "강원도 춘천시 ...")
    private String address;

    @Schema(description = "위도", example = "37.8800")
    private BigDecimal latitude;

    @Schema(description = "경도", example = "127.7300")
    private BigDecimal longitude;

    @Schema(description = "펫워크 총점", example = "72")
    private Integer totalScore;

    @Schema(description = "펫워크 등급", example = "B")
    private String grade;

    @Schema(description = "케이지 불필요 여부", example = "true")
    private Boolean cageNotRequired;

    public static AlternativePlaceResponse from(Place place) {
        String grade = place.getScore() != null && place.getScore().getGrade() != null
                ? place.getScore().getGrade().name() : null;
        int score = place.getScore() != null ? place.getScore().getTotalScore() : 0;
        Boolean cageNotRequired = place.getPetCondition() != null
                ? !Boolean.TRUE.equals(place.getPetCondition().getIsCageRequired()) : null;

        return AlternativePlaceResponse.builder()
                .placeId(place.getId())
                .placeName(place.getName())
                .placeType(place.getPlaceType())
                .address(place.getAddress())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .totalScore(score)
                .grade(grade)
                .cageNotRequired(cageNotRequired)
                .build();
    }
}
