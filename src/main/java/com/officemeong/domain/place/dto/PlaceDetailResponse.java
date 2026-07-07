package com.officemeong.domain.place.dto;

import com.officemeong.domain.congestion.enums.CongestionLevel;
import com.officemeong.domain.place.entity.*;
import com.officemeong.domain.place.enums.AcmpyType;
import com.officemeong.domain.place.enums.Grade;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PlaceDetailResponse {

    private Long id;
    private String name;
    private Region region;
    private PlaceType placeType;
    private String address;
    private String tel;
    private String homepage;
    private String overview;
    private List<String> imageUrls;
    private ScoreDto score;
    private PetConditionDto petCondition;
    private OperationDto operation;
    private AccessibilityDto accessibility;

    @Getter
    @Builder
    public static class ScoreDto {
        private int petCompanionScore;
        private int workcationScore;
        private int walkAccessibilityScore;
        private int congestionScore;
        private int emergencyScore;
        private int accessibilityScore;
        private int totalScore;
        private Grade grade;
        private CongestionLevel congestionLevel;
    }

    @Getter
    @Builder
    public static class PetConditionDto {
        private AcmpyType acmpyType;
        private Boolean isCageRequired;
        private Boolean isLeashRequired;
        private Integer petWeightLimitKg;
        private Boolean catAllowed;
        private Boolean bathAvailable;
        private String companionConditions;
        private String availableFacilities;
        private String cautions;
    }

    @Getter
    @Builder
    public static class OperationDto {
        private String operatingHours;
        private String closedDays;
        private String usageFee;
        private Boolean parkingAvailable;
        private String indoorOutdoorType;
    }

    @Getter
    @Builder
    public static class AccessibilityDto {
        private Boolean hasParking;
        private Boolean strollerAccessible;
        private Boolean hasRamp;
        private Boolean dataAvailable;
    }

    public static PlaceDetailResponse from(Place place) {
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
