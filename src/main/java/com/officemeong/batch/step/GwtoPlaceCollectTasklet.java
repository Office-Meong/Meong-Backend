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
    private static final String HOSPITAL_PART_CODE = "PC05";

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        int total = 0;

        for (Region region : Region.values()) {
            String areaName = extractAreaName(region);
            total += collectByPartCode(FOOD_PART_CODE, region, areaName);
            total += collectByPartCode(HOSPITAL_PART_CODE, region, areaName);
        }

        log.info("GWTO 수집 완료 - 총: {}개", total);
        return RepeatStatus.FINISHED;
    }

    private int collectByPartCode(String partCode, Region region, String areaName) {
        List<GwtoListItem> listItems = gwtoApiClient.fetchList(partCode, areaName);
        log.info("GWTO 목록: {} {} - {}개", region, partCode, listItems.size());

        int count = 0;
        for (GwtoListItem listItem : listItems) {
            GwtoDetailItem detail = gwtoApiClient.fetchDetail(partCode, listItem.getContentSeq());
            if (detail == null) continue;

            PlaceType placeType = resolveType(detail, partCode);
            boolean isNew = !placeRepository.existsBySourceTypeAndSourceId(SourceType.GWTO, detail.getContentSeq());

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
            petConditionRepository.save(PlacePetCondition.builder()
                    .place(place)
                    .acmpyType(AcmpyType.fromGwtoInOutFlag(detail.getInOutFlag()))
                    .petWeightLimitKg(parseWeight(detail.getPetWeight()))
                    .catAllowed("Y".equalsIgnoreCase(detail.getCatFlag()))
                    .bathAvailable("Y".equalsIgnoreCase(detail.getBathFlag()))
                    .companionConditions(detail.getPolicyCautions())
                    .availableFacilities(mergeText(detail.getMainFacility(), detail.getPetFacility()))
                    .cautions(detail.getPolicyCautions())
                    .build());

            // 운영 정보
            operationRepository.save(PlaceOperation.builder()
                    .place(place)
                    .operatingHours(detail.getUsedTime())
                    .usageFee(detail.getUsedCost())
                    .parkingAvailable(detail.isParkingAvailable())
                    .indoorOutdoorType(detail.getInOutFlag())
                    .build());

            if (isNew) {
                accessibilityRepository.save(PlaceAccessibility.createDefault(place));
                scoreRepository.save(PlaceScore.createDefault(place));
            }
            count++;
        }
        return count;
    }

    private PlaceType resolveType(GwtoDetailItem detail, String partCode) {
        if (HOSPITAL_PART_CODE.equals(partCode)) return PlaceType.HOSPITAL;
        return detail.isCafeKeyword() ? PlaceType.WORK_PLACE : PlaceType.FOOD;
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
