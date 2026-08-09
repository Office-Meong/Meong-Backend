package com.officemeong.batch.step;

import com.officemeong.domain.place.entity.*;
import com.officemeong.domain.place.enums.*;
import com.officemeong.domain.place.repository.*;
import com.officemeong.infrastructure.gwto.GwtoApiClient;
import com.officemeong.infrastructure.gwto.dto.GwtoDetailItem;
import com.officemeong.infrastructure.gwto.dto.GwtoListItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GwtoPlaceCollectTasklet implements Tasklet {

    private final GwtoApiClient gwtoApiClient;
    private final PlaceRepository placeRepository;
    private final PlaceImageRepository placeImageRepository;
    private final PlacePetConditionRepository petConditionRepository;
    private final PlaceOperationRepository operationRepository;
    private final PlaceAccessibilityRepository accessibilityRepository;
    private final PlaceScoreRepository scoreRepository;

    private static final String FOOD_PART_CODE = "PC01";

    // 강원 반려동물 동반관광 API 분야 코드 (활용 가이드 기준): PC01 식음료, PC02 숙박, PC03 관광지, PC04 체험, PC05 동물병원
    private static final Map<String, PlaceType> PART_TYPE_MAP = Map.of(
            "PC01", PlaceType.FOOD,
            "PC02", PlaceType.STAY,
            "PC03", PlaceType.TOUR,
            "PC04", PlaceType.TOUR,
            "PC05", PlaceType.HOSPITAL
    );

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        int total = 0;

        for (Region region : Region.values()) {
            String areaName = extractAreaName(region);
            for (String partCode : PART_TYPE_MAP.keySet()) {
                total += collectByPartCode(partCode, region, areaName);
            }
        }

        log.info("GWTO 수집 완료 - 총: {}개", total);
        return RepeatStatus.FINISHED;
    }

    private int collectByPartCode(String partCode, Region region, String areaName) {
        List<GwtoListItem> listItems = gwtoApiClient.fetchList(partCode, areaName);
        log.info("GWTO 목록: {} {} - {}개", region, partCode, listItems.size());

        int count = 0, skipped = 0;
        for (GwtoListItem listItem : listItems) {
            GwtoDetailItem detail = gwtoApiClient.fetchDetail(partCode, listItem.getContentSeq());
            if (detail == null) continue;

            PlaceType placeType = resolveType(detail, partCode);
            boolean isNew = !placeRepository.existsBySourceTypeAndSourceId(SourceType.GWTO, detail.getContentSeq());

            // KTO에서 이미 동일 이름으로 수집된 장소면 중복 생성하지 않고 스킵
            if (isNew && isDuplicateOfOtherSource(region, detail.getTitle(), SourceType.GWTO)) {
                log.info("GWTO 중복 스킵 (KTO 기존 수집): {} - {}", region, detail.getTitle());
                skipped++;
                continue;
            }

            Place place = placeRepository.findBySourceTypeAndSourceId(SourceType.GWTO, detail.getContentSeq())
                    .orElseGet(() -> Place.builder()
                            .sourceType(SourceType.GWTO)
                            .sourceId(detail.getContentSeq())
                            .placeType(placeType)
                            .region(region)
                            .name(detail.getTitle())
                            .address(detail.getAddress())
                            .latitude(parseDecimal(detail.getLatitude()))
                            .longitude(parseDecimal(detail.getLongitude()))
                            .tel(detail.getTel())
                            .homepage(detail.getHomePage())
                            .overview(detail.getContent())
                            .build());

            place = placeRepository.save(place);
            Place finalPlace = place;

            // 이미지
            if (detail.getImageList() != null && !detail.getImageList().isEmpty()) {
                placeImageRepository.deleteByPlaceId(place.getId());
                for (int i = 0; i < detail.getImageList().size(); i++) {
                    GwtoDetailItem.ImageItem img = detail.getImageList().get(i);
                    if (hasValue(img.getImage())) {
                        placeImageRepository.save(PlaceImage.builder()
                                .place(place).imageUrl(img.getImage())
                                .isThumbnail(i == 0).displayOrder(i).build());
                    }
                }
            }

            // 반려동물 조건
            AcmpyType acmpyType = AcmpyType.fromGwtoInOutFlag(detail.getInOutFlag());
            Integer petWeightLimitKg = parseWeight(detail.getPetWeight());
            boolean catAllowed = "Y".equalsIgnoreCase(detail.getCatFlag());
            boolean bathAvailable = "Y".equalsIgnoreCase(detail.getBathFlag());
            String availableFacilities = mergeText(detail.getMainFacility(), detail.getPetFacility());
            petConditionRepository.findById(place.getId())
                    .ifPresentOrElse(
                            existing -> existing.update(acmpyType, petWeightLimitKg, catAllowed, bathAvailable,
                                    detail.getPolicyCautions(), availableFacilities, detail.getPolicyCautions()),
                            () -> petConditionRepository.save(PlacePetCondition.builder()
                                    .place(finalPlace)
                                    .acmpyType(acmpyType)
                                    .petWeightLimitKg(petWeightLimitKg)
                                    .catAllowed(catAllowed)
                                    .bathAvailable(bathAvailable)
                                    .companionConditions(detail.getPolicyCautions())
                                    .availableFacilities(availableFacilities)
                                    .cautions(detail.getPolicyCautions())
                                    .build()));

            // 운영 정보
            operationRepository.findById(place.getId())
                    .ifPresentOrElse(
                            existing -> existing.update(detail.getUsedTime(), null, detail.getUsedCost(),
                                    detail.isParkingAvailable(), detail.getInOutFlag()),
                            () -> operationRepository.save(PlaceOperation.builder()
                                    .place(finalPlace)
                                    .operatingHours(detail.getUsedTime())
                                    .usageFee(detail.getUsedCost())
                                    .parkingAvailable(detail.isParkingAvailable())
                                    .indoorOutdoorType(detail.getInOutFlag())
                                    .build()));

            if (isNew) {
                accessibilityRepository.save(PlaceAccessibility.createDefault(place));
                scoreRepository.save(PlaceScore.createDefault(place));
            }
            count++;
        }
        log.info("GWTO 수집 - {} {} 완료: 처리 {}개, 중복 스킵 {}개", region, partCode, count, skipped);
        return count;
    }

    private boolean isDuplicateOfOtherSource(Region region, String name, SourceType sourceType) {
        if (!hasValue(name)) return false;
        return placeRepository.findFirstByRegionAndNameIgnoreCaseAndSourceTypeNot(region, name.trim(), sourceType)
                .isPresent();
    }

    private PlaceType resolveType(GwtoDetailItem detail, String partCode) {
        PlaceType baseType = PART_TYPE_MAP.getOrDefault(partCode, PlaceType.TOUR);
        if (FOOD_PART_CODE.equals(partCode) && detail.isCafeKeyword()) return PlaceType.WORK_PLACE;
        return baseType;
    }

    private String extractAreaName(Region region) {
        return switch (region) {
            case GANGNEUNG -> "강릉시";
            case CHUNCHEON -> "춘천시";
            case WONJU -> "원주시";
        };
    }

    private BigDecimal parseDecimal(String value) {
        if (!hasValue(value)) return null;
        try { return new BigDecimal(value); } catch (Exception e) { return null; }
    }

    private Integer parseWeight(String petWeight) {
        if (!hasValue(petWeight)) return null;
        try { return Integer.parseInt(petWeight.replaceAll("[^0-9]", "")); }
        catch (Exception e) { return null; }
    }

    private String mergeText(String a, String b) {
        if (!hasValue(a) && !hasValue(b)) return null;
        if (!hasValue(a)) return b;
        if (!hasValue(b)) return a;
        return a + "\n" + b;
    }

    private boolean hasValue(String s) { return s != null && !s.isBlank(); }
}
