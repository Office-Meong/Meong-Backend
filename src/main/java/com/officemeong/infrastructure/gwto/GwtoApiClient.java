package com.officemeong.infrastructure.gwto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.officemeong.infrastructure.gwto.dto.GwtoDetailItem;
import com.officemeong.infrastructure.gwto.dto.GwtoListItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GwtoApiClient {

    private final WebClient gwtoWebClient;
    private final ObjectMapper objectMapper;

    // GWTO listPart.do의 page 파라미터 기반 페이징이 서버 측에서 정상 동작하지 않아
    // (page를 올려도 앞부분과 겹치는 항목만 반복 반환되어 상당수 시군이 누락됨),
    // 전체 건수를 먼저 조회한 뒤 pageBlock을 그 값으로 잡아 한 번에 모두 가져온다.
    private static final int SAFETY_MAX_PAGE_BLOCK = 1000;

    /**
     * 장소 목록 전체 조회 (전 강원도 → areaName으로 필터링)
     * @param partCode PC01=식음료, PC02=숙박, PC03=관광지, PC04=체험, PC05=동물병원
     * @param areaName 필터링할 지역명 (예: "강릉시")
     */
    public List<GwtoListItem> fetchList(String partCode, String areaName) {
        List<GwtoListItem> result = new ArrayList<>();

        int totalCount = fetchTotalCount(partCode);
        if (totalCount <= 0) return result;

        int pageBlock = Math.min(totalCount, SAFETY_MAX_PAGE_BLOCK);
        int page = 1;

        while (true) {
            final int currentPage = page;
            final int currentPageBlock = pageBlock;
            try {
                String raw = gwtoWebClient.get()
                        .uri(u -> u.path("/listPart.do")
                                .queryParam("partCode", partCode)
                                .queryParam("page", currentPage)
                                .queryParam("pageBlock", currentPageBlock)
                                .build())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                JsonNode root = objectMapper.readTree(raw);
                JsonNode dataNode = root.isArray() ? root.get(0) : root;
                JsonNode listNode = dataNode.path("resultList");

                if (listNode.isMissingNode() || listNode.isEmpty()) break;

                for (JsonNode item : listNode) {
                    GwtoListItem listItem = objectMapper.treeToValue(item, GwtoListItem.class);
                    if (areaName == null || (listItem.getAreaName() != null && listItem.getAreaName().contains(areaName))) {
                        result.add(listItem);
                    }
                }

                if ((long) page * pageBlock >= totalCount) break;
                page++;
            } catch (Exception e) {
                log.error("GWTO listPart.do 호출 실패 - partCode={}, page={}", partCode, page, e);
                break;
            }
        }

        return result;
    }

    private int fetchTotalCount(String partCode) {
        try {
            String raw = gwtoWebClient.get()
                    .uri(u -> u.path("/listPart.do")
                            .queryParam("partCode", partCode)
                            .queryParam("page", 1)
                            .queryParam("pageBlock", 1)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(raw);
            JsonNode dataNode = root.isArray() ? root.get(0) : root;
            return dataNode.path("totalCount").asInt(0);
        } catch (Exception e) {
            log.error("GWTO totalCount 조회 실패 - partCode={}", partCode, e);
            return 0;
        }
    }

    public GwtoDetailItem fetchDetail(String partCode, String contentSeq) {
        try {
            String raw = gwtoWebClient.get()
                    .uri(u -> u.path("/detailSeqPart.do")
                            .queryParam("partCode", partCode)
                            .queryParam("contentNum", contentSeq)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(raw);
            JsonNode dataNode = root.isArray() ? root.get(0) : root;
            JsonNode resultNode = dataNode.path("resultList");
            if (resultNode.isMissingNode() || resultNode.isNull()) return null;
            return objectMapper.treeToValue(resultNode, GwtoDetailItem.class);
        } catch (Exception e) {
            log.error("GWTO detailSeqPart.do 호출 실패 - contentSeq={}", contentSeq, e);
            return null;
        }
    }
}
