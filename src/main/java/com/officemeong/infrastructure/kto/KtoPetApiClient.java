package com.officemeong.infrastructure.kto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.officemeong.infrastructure.kto.dto.KtoAreaBasedItem;
import com.officemeong.infrastructure.kto.dto.KtoIntroItem;
import com.officemeong.infrastructure.kto.dto.KtoPetDetailItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KtoPetApiClient {

    private final WebClient ktoWebClient;
    private final ObjectMapper objectMapper;

    @Value("${api.kto.service-key}")
    private String serviceKey;

    @Value("${api.kto.base-url}")
    private String baseUrl;

    @Value("${api.kto.mobile-os}")
    private String mobileOs;

    @Value("${api.kto.mobile-app}")
    private String mobileApp;

    private static final String AREA_CODE = "32";
    private static final int PAGE_SIZE = 100;

    public List<KtoAreaBasedItem> fetchAreaBasedList(String sigunguCode, String contentTypeId) {
        List<KtoAreaBasedItem> result = new ArrayList<>();
        int page = 1;

        while (true) {
            URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/KorPetTourService2/areaBasedList2")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", mobileOs)
                    .queryParam("MobileApp", mobileApp)
                    .queryParam("_type", "json")
                    .queryParam("areaCode", AREA_CODE)
                    .queryParam("sigunguCode", sigunguCode)
                    .queryParam("contentTypeId", contentTypeId)
                    .queryParam("numOfRows", PAGE_SIZE)
                    .queryParam("pageNo", page)
                    .build(true)
                    .toUri();

            try {
                String raw = ktoWebClient.get().uri(uri).retrieve().bodyToMono(String.class).block();
                List<KtoAreaBasedItem> items = parseItems(raw, KtoAreaBasedItem.class);
                if (items.isEmpty()) break;
                result.addAll(items);
                if (result.size() >= getTotalCount(raw)) break;
                page++;
            } catch (Exception e) {
                log.error("KTO areaBasedList2 호출 실패 - sigungu={}, type={}", sigunguCode, contentTypeId, e);
                break;
            }
        }

        return result;
    }

    public KtoPetDetailItem fetchPetDetail(String contentId) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/KorPetTourService2/detailPetTour2")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", mobileOs)
                .queryParam("MobileApp", mobileApp)
                .queryParam("_type", "json")
                .queryParam("contentId", contentId)
                .build(true)
                .toUri();

        try {
            String raw = ktoWebClient.get().uri(uri).retrieve().bodyToMono(String.class).block();
            List<KtoPetDetailItem> items = parseItems(raw, KtoPetDetailItem.class);
            return items.isEmpty() ? null : items.get(0);
        } catch (Exception e) {
            log.error("KTO detailPetTour2 호출 실패 - contentId={}", contentId, e);
            return null;
        }
    }

    public KtoIntroItem fetchIntro(String contentId, String contentTypeId) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/KorService2/detailIntro2")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", mobileOs)
                .queryParam("MobileApp", mobileApp)
                .queryParam("_type", "json")
                .queryParam("contentId", contentId)
                .queryParam("contentTypeId", contentTypeId)
                .build(true)
                .toUri();

        try {
            String raw = ktoWebClient.get().uri(uri).retrieve().bodyToMono(String.class).block();
            List<KtoIntroItem> items = parseItems(raw, KtoIntroItem.class);
            return items.isEmpty() ? null : items.get(0);
        } catch (Exception e) {
            log.error("KTO detailIntro2 호출 실패 - contentId={}", contentId, e);
            return null;
        }
    }

    private <T> List<T> parseItems(String raw, Class<T> type) {
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode itemNode = root.path("response").path("body").path("items").path("item");
            if (itemNode.isMissingNode() || itemNode.isNull()) return Collections.emptyList();
            if (itemNode.isArray()) {
                return objectMapper.convertValue(itemNode, objectMapper.getTypeFactory().constructCollectionType(List.class, type));
            }
            return List.of(objectMapper.treeToValue(itemNode, type));
        } catch (Exception e) {
            log.warn("KTO 응답 파싱 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private int getTotalCount(String raw) {
        try {
            JsonNode root = objectMapper.readTree(raw);
            return root.path("response").path("body").path("totalCount").asInt(0);
        } catch (Exception e) {
            return 0;
        }
    }
}
