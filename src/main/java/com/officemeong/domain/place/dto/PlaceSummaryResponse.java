package com.officemeong.domain.place.dto;

import com.officemeong.domain.congestion.enums.CongestionLevel;
import com.officemeong.domain.place.entity.Place;
import com.officemeong.domain.place.entity.PlaceImage;
import com.officemeong.domain.place.entity.PlaceScore;
import com.officemeong.domain.place.enums.AcmpyType;
import com.officemeong.domain.place.enums.Grade;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "장소 목록 항목 응답")
@Getter
@Builder
public class PlaceSummaryResponse {

    @Schema(description = "장소 ID", example = "1")
    private Long id;

    @Schema(description = "장소명", example = "강릉 펫 코워킹")
    private String name;

    @Schema(description = "지역 (GANGNEUNG: 강릉 / CHUNCHEON: 춘천 / WONJU: 원주)", example = "GANGNEUNG")
    private Region region;

    @Schema(description = "장소 유형 (STAY: 숙박 / WORK_PLACE: 업무공간 / FOOD: 식당 / TOUR: 관광지 / WALK: 산책로 / HOSPITAL: 동물병원)", example = "WORK_PLACE")
    private PlaceType placeType;

    @Schema(description = "주소", example = "강원특별자치도 강릉시 창해로14번길 48-1")
    private String address;

    @Schema(description = "썸네일 이미지 URL (없으면 null)", example = "http://example.com/thumb.jpg")
    private String thumbnailUrl;

    @Schema(description = "펫-워크 지수 등급 (A: 85점+ / B: 70점+ / C: 55점+ / D: 40점+ / E: 그 미만, 점수 미확보 시 null)", example = "B")
    private Grade grade;

    @Schema(description = "펫-워크 지수 총점 (0~100점)", example = "78")
    private int totalScore;

    @Schema(description = "동반 가능 구역 (INDOOR/OUTDOOR/INDOOR_OUTDOOR/DESIGNATED/UNKNOWN, 정보 없으면 null)", example = "INDOOR_OUTDOOR")
    private AcmpyType acmpyType;

    @Schema(description = "혼잡도 역방향 점수 (0~15점, 낮을수록 붐빔)", example = "9")
    private int congestionScore;

    @Schema(description = "혼잡도 수준 (RELAXED: 여유 / NORMAL: 보통 / CROWDED: 붐빔 / VERY_CROWDED: 매우붐빔 / UNKNOWN: 정보없음)", example = "NORMAL")
    private CongestionLevel congestionLevel;

    @Schema(description = "현재 로그인한 사용자의 즐겨찾기 여부", example = "false")
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
