package com.officemeong.batch.step;

import com.officemeong.domain.place.entity.*;
import com.officemeong.domain.place.enums.*;
import com.officemeong.domain.place.repository.*;
import com.officemeong.infrastructure.kto.KtoPetApiClient;
import com.officemeong.infrastructure.kto.dto.KtoAreaBasedItem;
import com.officemeong.infrastructure.kto.dto.KtoIntroItem;
import com.officemeong.infrastructure.kto.dto.KtoPetDetailItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KtoPlaceCollectTasklet implements Tasklet {

    private final KtoPetApiClient ktoApiClient;
    private final PlaceRepository placeRepository;
    private final PlaceImageRepository placeImageRepository;
    private final PlacePetConditionRepository petConditionRepository;
    private final PlaceOperationRepository operationRepository;
    private final PlaceAccessibilityRepository accessibilityRepository;
    private final PlaceScoreRepository scoreRepository;

    private static final Map<String, PlaceType> TYPE_MAP = Map.of(
            "32", PlaceType.STAY,
            "39", PlaceType.FOOD,
            "12", PlaceType.TOUR,
            "14", PlaceType.TOUR,
            "28", PlaceType.TOUR,
            "38", PlaceType.TOUR
    );

    // 숙소(STAY) 세부 유형 추론용 키워드. 원본 API에 유형 필드가 없어 제목 텍스트 매칭으로 판별
    private static final Map<String, LodgingType> LODGING_KEYWORDS = new LinkedHashMap<>();
    static {
        LODGING_KEYWORDS.put("글램핑", LodgingType.GLAMPING);
        LODGING_KEYWORDS.put("카라반", LodgingType.CARAVAN);
        LODGING_KEYWORDS.put("캠핑", LodgingType.CAMPING);
        LODGING_KEYWORDS.put("펜션", LodgingType.PENSION);
        LODGING_KEYWORDS.put("호텔", LodgingType.HOTEL);
        LODGING_KEYWORDS.put("리조트", LodgingType.HOTEL);
        LODGING_KEYWORDS.put("모텔", LodgingType.HOTEL);
        LODGING_KEYWORDS.put("게스트하우스", LodgingType.GUESTHOUSE);
        LODGING_KEYWORDS.put("민박", LodgingType.GUESTHOUSE);
        LODGING_KEYWORDS.put("한옥", LodgingType.GUESTHOUSE);
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        int saved = 0, updated = 0, skipped = 0;

        for (Region region : Region.values()) {
            String sigunguCode = region.getKtoSigunguCode();

            for (Map.Entry<String, PlaceType> entry : TYPE_MAP.entrySet()) {
                String contentTypeId = entry.getKey();
                PlaceType baseType = entry.getValue();

                List<KtoAreaBasedItem> items = ktoApiClient.fetchAreaBasedList(sigunguCode, contentTypeId);
                log.info("KTO 수집: {} {} - {}개", region, baseType, items.size());

                for (KtoAreaBasedItem item : items) {
                    PlaceType placeType = resolveWorkPlace(item, baseType);
                    boolean isNew = !placeRepository.existsBySourceTypeAndSourceId(SourceType.KTO, item.getContentid());

                    // GWTO에서 이미 동일 이름으로 수집된 장소면 중복 생성하지 않고 스킵 (GWTO 동반조건 데이터가 더 정확함)
                    if (isNew && isDuplicateOfOtherSource(region, item.getTitle(), SourceType.KTO)) {
                        log.info("KTO 중복 스킵 (GWTO 기존 수집): {} - {}", region, item.getTitle());
                        skipped++;
                        continue;
                    }

                    Place place = placeRepository.findBySourceTypeAndSourceId(SourceType.KTO, item.getContentid())
                            .orElseGet(() -> Place.builder()
                                    .sourceType(SourceType.KTO)
                                    .sourceId(item.getContentid())
                                    .placeType(placeType)
                                    .region(region)
                                    .name(item.getTitle())
                                    .address(buildAddress(item))
                                    .latitude(parseDecimal(item.getMapy()))
                                    .longitude(parseDecimal(item.getMapx()))
                                    .tel(item.getTel())
                                    .build());

                    if (!isNew) {
                        place.update(item.getTitle(), buildAddress(item),
                                parseDecimal(item.getMapy()), parseDecimal(item.getMapx()),
                                item.getTel(), null, null);
                    }
                    place = placeRepository.save(place);

                    // 썸네일 이미지
                    if (hasValue(item.getFirstimage())) {
                        placeImageRepository.deleteByPlaceId(place.getId());
                        placeImageRepository.save(PlaceImage.builder()
                                .place(place).imageUrl(item.getFirstimage())
                                .isThumbnail(true).displayOrder(0).build());
                    }

                    // 펫 조건
                    KtoPetDetailItem pet = ktoApiClient.fetchPetDetail(item.getContentid());
                    if (pet != null) {
                        AcmpyType acmpyType = AcmpyType.fromKto(pet.getAcmpyTypeCd());
                        String availableFacilities = mergeText(pet.getRelaPosesFclty(), pet.getRelaFrnshPrdlst());
                        String cautions = mergeText(pet.getRelaAcdntRiskMtr(), pet.getEtcAcmpyInfo());
                        Place finalPlace = place;
                        petConditionRepository.findById(place.getId())
                                .ifPresentOrElse(
                                        existing -> existing.update(acmpyType, existing.getPetWeightLimitKg(),
                                                existing.getCatAllowed(), existing.getBathAvailable(),
                                                pet.getAcmpyNeedMtr(), availableFacilities, cautions),
                                        () -> petConditionRepository.save(PlacePetCondition.builder()
                                                .place(finalPlace)
                                                .acmpyType(acmpyType)
                                                .companionConditions(pet.getAcmpyNeedMtr())
                                                .availableFacilities(availableFacilities)
                                                .cautions(cautions)
                                                .build()));
                    }

                    // 운영 정보
                    KtoIntroItem intro = ktoApiClient.fetchIntro(item.getContentid(), contentTypeId);
                    if (intro != null) {
                        Place finalPlace = place;
                        LodgingType lodgingType = resolveLodgingType(placeType, item.getTitle());
                        operationRepository.findById(place.getId())
                                .ifPresentOrElse(
                                        existing -> existing.update(intro.getOperatingHours(), intro.getClosedDays(),
                                                existing.getUsageFee(), intro.getParkingAvailable(),
                                                existing.getIndoorOutdoorType(), lodgingType),
                                        () -> operationRepository.save(PlaceOperation.builder()
                                                .place(finalPlace)
                                                .operatingHours(intro.getOperatingHours())
                                                .closedDays(intro.getClosedDays())
                                                .parkingAvailable(intro.getParkingAvailable())
                                                .lodgingType(lodgingType)
                                                .build()));
                    }

                    // 접근성/점수 초기값 (신규만)
                    if (isNew) {
                        accessibilityRepository.save(PlaceAccessibility.createDefault(place));
                        scoreRepository.save(PlaceScore.createDefault(place));
                    }

                    if (isNew) saved++; else updated++;
                }
            }
        }

        log.info("KTO 수집 완료 - 신규: {}, 업데이트: {}, 중복 스킵: {}", saved, updated, skipped);
        return RepeatStatus.FINISHED;
    }

    private boolean isDuplicateOfOtherSource(Region region, String name, SourceType sourceType) {
        if (!hasValue(name)) return false;
        return placeRepository.findFirstByRegionAndNameIgnoreCaseAndSourceTypeNot(region, name.trim(), sourceType)
                .isPresent();
    }

    private PlaceType resolveWorkPlace(KtoAreaBasedItem item, PlaceType baseType) {
        if (baseType == PlaceType.FOOD && "A05020900".equals(item.getCat3())) return PlaceType.WORK_PLACE;
        return baseType;
    }

    /** 숙소 유형 텍스트 매칭. STAY가 아니면 null, 매칭되는 키워드가 없어도 null(미분류) */
    private LodgingType resolveLodgingType(PlaceType placeType, String... texts) {
        if (placeType != PlaceType.STAY) return null;
        StringBuilder combined = new StringBuilder();
        for (String text : texts) {
            if (hasValue(text)) combined.append(text).append(' ');
        }
        for (Map.Entry<String, LodgingType> entry : LODGING_KEYWORDS.entrySet()) {
            if (combined.indexOf(entry.getKey()) >= 0) return entry.getValue();
        }
        return null;
    }

    private String buildAddress(KtoAreaBasedItem item) {
        if (hasValue(item.getAddr2())) return item.getAddr1() + " " + item.getAddr2();
        return item.getAddr1();
    }

    private BigDecimal parseDecimal(String value) {
        if (!hasValue(value)) return null;
        try { return new BigDecimal(value); } catch (Exception e) { return null; }
    }

    private String mergeText(String a, String b) {
        if (!hasValue(a) && !hasValue(b)) return null;
        if (!hasValue(a)) return b;
        if (!hasValue(b)) return a;
        return a + "\n" + b;
    }

    private boolean hasValue(String s) { return s != null && !s.isBlank(); }
}
