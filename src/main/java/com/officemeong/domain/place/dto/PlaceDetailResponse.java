package com.officemeong.domain.place.dto;

import com.officemeong.domain.congestion.enums.CongestionLevel;
import com.officemeong.domain.place.entity.*;
import com.officemeong.domain.place.enums.AcmpyType;
import com.officemeong.domain.place.enums.Grade;
import com.officemeong.domain.place.enums.LodgingType;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Schema(description = "장소 상세 응답")
@Getter
@Builder
public class PlaceDetailResponse {

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

    @Schema(description = "전화번호", example = "033-123-4567")
    private String tel;

    @Schema(description = "홈페이지 URL", example = "http://example.com")
    private String homepage;

    @Schema(description = "소개/설명", example = "반려견과 함께 일하기 좋은 카페입니다.")
    private String overview;

    @Schema(description = "이미지 URL 목록")
    private List<String> imageUrls;

    @Schema(description = "현재 로그인한 사용자의 즐겨찾기 여부", example = "false")
    private boolean isFavorite;

    @Schema(description = "펫-워크 지수 점수 정보 (데이터 미확보 시 null)")
    private ScoreDto score;

    @Schema(description = "반려동물 동반 조건 정보 (데이터 미확보 시 null)")
    private PetConditionDto petCondition;

    @Schema(description = "운영 정보 (데이터 미확보 시 null)")
    private OperationDto operation;

    @Schema(description = "접근성 정보 (데이터 미확보 시 null)")
    private AccessibilityDto accessibility;

    @Schema(description = "펫-워크 지수 점수 상세 (총 100점 만점)")
    @Getter
    @Builder
    public static class ScoreDto {
        @Schema(description = "반려동물 동반적합도 (0~30점)", example = "20")
        private int petCompanionScore;

        @Schema(description = "워케이션 적합도 (0~25점, 장소 유형 기준 고정값)", example = "25")
        private int workcationScore;

        @Schema(description = "산책 접근성 (0~20점, 가까운 산책로까지 거리 기준)", example = "14")
        private int walkAccessibilityScore;

        @Schema(description = "혼잡도 역방향 점수 (0~15점, 낮을수록 붐빔)", example = "9")
        private int congestionScore;

        @Schema(description = "응급 접근성 (0~7점, 가까운 동물병원까지 거리 기준)", example = "5")
        private int emergencyScore;

        @Schema(description = "접근성 점수 (0~8점, 주차/유모차/경사로 여부)", example = "5")
        private int accessibilityScore;

        @Schema(description = "총점 (0~100점)", example = "78")
        private int totalScore;

        @Schema(description = "등급 (A: 85점+ / B: 70점+ / C: 55점+ / D: 40점+ / E: 그 미만)", example = "B")
        private Grade grade;

        @Schema(description = "혼잡도 수준 (RELAXED: 여유 / NORMAL: 보통 / CROWDED: 붐빔 / VERY_CROWDED: 매우붐빔 / UNKNOWN: 정보없음)", example = "NORMAL")
        private CongestionLevel congestionLevel;
    }

    @Schema(description = "반려동물 동반 조건 상세")
    @Getter
    @Builder
    public static class PetConditionDto {
        @Schema(description = "동반 가능 구역 (INDOOR: 실내만 / OUTDOOR: 실외만 / INDOOR_OUTDOOR: 전구역 / DESIGNATED: 지정구역만 / UNKNOWN: 정보없음)", example = "INDOOR_OUTDOOR")
        private AcmpyType acmpyType;

        @Schema(description = "케이지(이동장) 필수 여부", example = "false")
        private Boolean isCageRequired;

        @Schema(description = "리드줄 필수 여부", example = "true")
        private Boolean isLeashRequired;

        @Schema(description = "동반 가능 체중 제한(kg), 제한 없으면 null", example = "10")
        private Integer petWeightLimitKg;

        @Schema(description = "고양이 동반 가능 여부", example = "false")
        private Boolean catAllowed;

        @Schema(description = "목욕시설 보유 여부", example = "false")
        private Boolean bathAvailable;

        @Schema(description = "동반 조건 안내 (원문 텍스트)", example = "- 목줄필수")
        private String companionConditions;

        @Schema(description = "이용 가능한 반려동물 시설 안내", example = "야외 테라스, 배변봉투 비치")
        private String availableFacilities;

        @Schema(description = "주의사항", example = "우천 시 동반 불가")
        private String cautions;
    }

    @Schema(description = "운영 정보 상세")
    @Getter
    @Builder
    public static class OperationDto {
        @Schema(description = "영업시간", example = "매일 09:00-18:00")
        private String operatingHours;

        @Schema(description = "휴무일", example = "매주 월요일")
        private String closedDays;

        @Schema(description = "이용요금 안내", example = "아메리카노 4,500원")
        private String usageFee;

        @Schema(description = "주차 가능 여부", example = "true")
        private Boolean parkingAvailable;

        @Schema(description = "실내외 구분 (IN/OUT 등 원본 값)", example = "IN")
        private String indoorOutdoorType;

        @Schema(description = "숙소 유형 (STAY 장소에만 해당, 그 외 null). " +
                "PENSION=펜션, GUESTHOUSE=민박/게스트하우스/한옥, CAMPING=캠핑장, GLAMPING=글램핑장, HOTEL=호텔/모텔/리조트, CARAVAN=카라반. " +
                "원본 데이터에 유형이 명시되지 않은 숙소는 null(미분류)",
                example = "PENSION")
        private LodgingType lodgingType;
    }

    @Schema(description = "접근성 정보 상세")
    @Getter
    @Builder
    public static class AccessibilityDto {
        @Schema(description = "주차 가능 여부", example = "true")
        private Boolean hasParking;

        @Schema(description = "유모차 접근 가능 여부", example = "false")
        private Boolean strollerAccessible;

        @Schema(description = "경사로 보유 여부", example = "false")
        private Boolean hasRamp;

        @Schema(description = "접근성 데이터 확보 여부 (false면 위 항목들이 기본값)", example = "false")
        private Boolean dataAvailable;
    }

    public static PlaceDetailResponse from(Place place) {
        return from(place, false);
    }

    public static PlaceDetailResponse from(Place place, boolean isFavorite) {
        PlaceScore score = place.getScore();
        PlacePetCondition pet = place.getPetCondition();
        PlaceOperation op = place.getOperation();
        PlaceAccessibility acc = place.getAccessibility();

        List<String> images = place.getImages().stream()
                .map(PlaceImage::getImageUrl)
                .toList();

        int congestionScore = score != null ? score.getCongestionScore() : 8;

        return PlaceDetailResponse.builder()
                .id(place.getId())
                .name(place.getName())
                .region(place.getRegion())
                .placeType(place.getPlaceType())
                .address(place.getAddress())
                .tel(place.getTel())
                .homepage(place.getHomepage())
                .overview(place.getOverview())
                .imageUrls(images)
                .isFavorite(isFavorite)
                .score(score != null ? ScoreDto.builder()
                        .petCompanionScore(score.getPetCompanionScore())
                        .workcationScore(score.getWorkcationScore())
                        .walkAccessibilityScore(score.getWalkAccessibilityScore())
                        .congestionScore(congestionScore)
                        .emergencyScore(score.getEmergencyScore())
                        .accessibilityScore(score.getAccessibilityScore())
                        .totalScore(score.getTotalScore())
                        .grade(score.getGrade())
                        .congestionLevel(CongestionLevel.fromScore(congestionScore))
                        .build() : null)
                .petCondition(pet != null ? PetConditionDto.builder()
                        .acmpyType(pet.getAcmpyType())
                        .isCageRequired(pet.getIsCageRequired())
                        .isLeashRequired(pet.getIsLeashRequired())
                        .petWeightLimitKg(pet.getPetWeightLimitKg())
                        .catAllowed(pet.getCatAllowed())
                        .bathAvailable(pet.getBathAvailable())
                        .companionConditions(pet.getCompanionConditions())
                        .availableFacilities(pet.getAvailableFacilities())
                        .cautions(pet.getCautions())
                        .build() : null)
                .operation(op != null ? OperationDto.builder()
                        .operatingHours(op.getOperatingHours())
                        .closedDays(op.getClosedDays())
                        .usageFee(op.getUsageFee())
                        .parkingAvailable(op.getParkingAvailable())
                        .indoorOutdoorType(op.getIndoorOutdoorType())
                        .lodgingType(op.getLodgingType())
                        .build() : null)
                .accessibility(acc != null ? AccessibilityDto.builder()
                        .hasParking(acc.getHasParking())
                        .strollerAccessible(acc.getStrollerAccessible())
                        .hasRamp(acc.getHasRamp())
                        .dataAvailable(acc.getDataAvailable())
                        .build() : null)
                .build();
    }
}
