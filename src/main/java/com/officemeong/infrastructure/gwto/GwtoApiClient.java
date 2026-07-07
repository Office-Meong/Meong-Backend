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

    private static final int PAGE_SIZE = 50;

    /**
     * 장소 목록 전체 조회 (전 강원도 → areaName으로 필터링)
     * @param partCode PC01=식음료, PC05=동물병원
     * @param areaName 필터링할 지역명 (예: "강릉시")
     */
    public List<GwtoListItem> fetchList(String partCode, String areaName) {
        List<GwtoListItem> result = new ArrayList<>();
        int page = 1;

        while (true) {
            final int currentPage = page;
            try {
                String raw = gwtoWebClient.get()
                        .uri(u -> u.path("/listPart.do")
                                .queryParam("partCode", partCode)
                                .queryParam("page", currentPage)
                                .queryParam("pageBlock", PAGE_SIZE)
                                .build())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                JsonNode root = objectMapper.readTree(raw);
                JsonNode dataNode = root.isArray() ? root.get(0) : root;
                JsonNode listNode = dataNode.path("resultList");

                if (listNode.isMissingNode() || listNode.isEmpty()) break;

                int totalCount = dataNode.path("totalCount").asInt(0);
                for (JsonNode item : listNode) {
                    GwtoListItem listItem = objectMapper.treeToValue(item, GwtoListItem.class);
                    if (areaName == null || (listItem.getAreaName() != null && listItem.getAreaName().contains(areaName))) {
                        result.add(listItem);
                    }
                }

                if (page * PAGE_SIZE >= totalCount) break;
                page++;
            } catch (Exception e) {
                log.error("GWTO listPart.do 호출 실패 - partCode={}, page={}", partCode, page, e);
                break;
            }
        }

        return result;
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
            return objectMapper.treeToValue(dataNode, GwtoDetailItem.class);
        } catch (Exception e) {
            log.error("GWTO detailSeqPart.do 호출 실패 - contentSeq={}", contentSeq, e);
            return null;
        }
    }
}
