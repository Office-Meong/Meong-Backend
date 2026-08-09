package com.officemeong.batch.step;

import com.officemeong.domain.place.entity.Place;
import com.officemeong.domain.place.entity.PlaceAccessibility;
import com.officemeong.domain.place.entity.PlaceScore;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.place.enums.SourceType;
import com.officemeong.domain.place.repository.PlaceAccessibilityRepository;
import com.officemeong.domain.place.repository.PlaceRepository;
import com.officemeong.domain.place.repository.PlaceScoreRepository;
import com.officemeong.infrastructure.kakao.KakaoLocalApiClient;
import com.officemeong.infrastructure.kakao.dto.KakaoLocalSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * KTO/GWTO 원주 식음료(FOOD) 데이터가 대부분 카페로 분류되어 순수 식당이 사실상 1건뿐인 공백을
 * 카카오 로컬(키워드 검색) API로 보강한다. 카카오는 GWTO/KTO와 달리 반려동물 동반 정책이
 * 구조화된 데이터로 제공되지 않으므로, "애견동반"이 명시된 검색어로만 수집하고
 * 음식점(FD6) 카테고리·대상 지역 주소로 한 번 더 걸러 신뢰도를 높인다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoFoodSupplementTasklet implements Tasklet {

    private static final Region TARGET_REGION = Region.WONJU;
    private static final String SEARCH_QUERY = "원주 애견동반 식당";
    private static final String ADDRESS_FILTER = "원주시";
    private static final String RESTAURANT_CATEGORY_CODE = "FD6";
    private static final String UNVERIFIED_PET_POLICY_NOTICE =
            "카카오맵 키워드 검색으로 수집된 장소입니다. 반려동물 동반 가능 여부는 GWTO/KTO처럼 공식 검증된 정보가 아니므로 방문 전 업체에 직접 확인해주세요.";

    private final KakaoLocalApiClient kakaoLocalApiClient;
    private final PlaceRepository placeRepository;
    private final PlaceAccessibilityRepository accessibilityRepository;
    private final PlaceScoreRepository scoreRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<KakaoLocalSearchResponse.Document> documents = kakaoLocalApiClient.searchKeyword(SEARCH_QUERY);
        int saved = 0, skipped = 0;

        for (KakaoLocalSearchResponse.Document doc : documents) {
            if (!RESTAURANT_CATEGORY_CODE.equals(doc.getCategoryGroupCode())) {
                skipped++;
                continue;
            }
            String address = hasValue(doc.getRoadAddressName()) ? doc.getRoadAddressName() : doc.getAddressName();
            if (!hasValue(address) || !address.contains(ADDRESS_FILTER)) {
                skipped++;
                continue;
            }

            boolean isNew = !placeRepository.existsBySourceTypeAndSourceId(SourceType.KAKAO, doc.getId());
            if (isNew && isDuplicateOfOtherSource(doc.getPlaceName())) {
                log.info("카카오 식당 보강 중복 스킵 (기존 수집): {}", doc.getPlaceName());
                skipped++;
                continue;
            }

            Place place = placeRepository.findBySourceTypeAndSourceId(SourceType.KAKAO, doc.getId())
                    .orElseGet(() -> Place.builder()
                            .sourceType(SourceType.KAKAO)
                            .sourceId(doc.getId())
                            .placeType(PlaceType.FOOD)
                            .region(TARGET_REGION)
                            .name(doc.getPlaceName())
                            .address(address)
                            .latitude(parseDecimal(doc.getY()))
                            .longitude(parseDecimal(doc.getX()))
                            .tel(doc.getPhone())
                            .homepage(doc.getPlaceUrl())
                            .overview(UNVERIFIED_PET_POLICY_NOTICE)
                            .build());

            if (!isNew) {
                place.update(doc.getPlaceName(), address, parseDecimal(doc.getY()), parseDecimal(doc.getX()),
                        doc.getPhone(), doc.getPlaceUrl(), UNVERIFIED_PET_POLICY_NOTICE);
            }
            place = placeRepository.save(place);

            if (isNew) {
                accessibilityRepository.save(PlaceAccessibility.createDefault(place));
                scoreRepository.save(PlaceScore.createDefault(place));
                saved++;
            }
        }

        log.info("카카오 원주 식당 보강 완료 - 신규: {}, 스킵: {}", saved, skipped);
        return RepeatStatus.FINISHED;
    }

    private boolean isDuplicateOfOtherSource(String name) {
        if (!hasValue(name)) return false;
        return placeRepository.findFirstByRegionAndNameIgnoreCaseAndSourceTypeNot(TARGET_REGION, name.trim(), SourceType.KAKAO)
                .isPresent();
    }

    private BigDecimal parseDecimal(String value) {
        if (!hasValue(value)) return null;
        try { return new BigDecimal(value); } catch (Exception e) { return null; }
    }

    private boolean hasValue(String s) { return s != null && !s.isBlank(); }
}
