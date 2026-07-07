package com.officemeong.batch.step;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.officemeong.domain.congestion.entity.CongestionForecast;
import com.officemeong.domain.congestion.repository.CongestionForecastRepository;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.infrastructure.kto.dto.CongestionForecastItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CongestionCollectTasklet implements Tasklet {

    private final WebClient ktoWebClient;
    private final ObjectMapper objectMapper;
    private final CongestionForecastRepository forecastRepository;

    @Value("${api.kto.service-key}")
    private String serviceKey;

    @Value("${api.kto.base-url}")
    private String baseUrl;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        int total = 0;
        for (Region region : Region.values()) {
            total += collectForRegion(region);
        }
        log.info("관광지집중률 수집 완료 - 총 {}건", total);
        return RepeatStatus.FINISHED;
    }

    private int collectForRegion(Region region) {
        List<CongestionForecastItem> items = fetchItems(region);
        int count = 0;

        for (CongestionForecastItem item : items) {
            if (item.getTAtsNm() == null || item.getBaseYmd() == null) continue;

            LocalDate baseDate;
            try {
                baseDate = LocalDate.parse(item.getBaseYmd(), DATE_FMT);
            } catch (Exception e) {
                log.warn("날짜 파싱 실패: {}", item.getBaseYmd());
                continue;
            }

            BigDecimal rate = parseBigDecimal(item.getCnctrRate());

            forecastRepository.findByTatsNmAndBaseYmd(item.getTAtsNm(), baseDate)
                    .ifPresentOrElse(
                            existing -> existing.updateRate(rate),
                            () -> forecastRepository.save(CongestionForecast.builder()
                                    .tatsNm(item.getTAtsNm())
                                    .region(region)
                                    .areaCd(item.getAreaCd())
                                    .signguCd(item.getSignguCd())
                                    .baseYmd(baseDate)
                                    .cnctrRate(rate)
                                    .build())
                    );
            count++;
        }
        log.info("혼잡도 수집: {} - {}건", region, count);
        return count;
    }

    private List<CongestionForecastItem> fetchItems(Region region) {
        List<CongestionForecastItem> result = new ArrayList<>();
        int page = 1;

        while (true) {
            URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/TarRtheDatInq/getCnctrRateInqs")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "OfficeMeong")
                    .queryParam("_type", "json")
                    .queryParam("areaCd", region.getCongestionAreaCd())
                    .queryParam("signguCd", region.getCongestionSignguCd())
                    .queryParam("numOfRows", 100)
                    .queryParam("pageNo", page)
                    .build(true)
                    .toUri();

            try {
                String raw = ktoWebClient.get().uri(uri).retrieve().bodyToMono(String.class).block();
                JsonNode root = objectMapper.readTree(raw);
                JsonNode itemNode = root.path("response").path("body").path("items").path("item");
                int total = root.path("response").path("body").path("totalCount").asInt(0);

                List<CongestionForecastItem> items = new ArrayList<>();
                if (itemNode.isArray()) {
                    for (JsonNode n : itemNode) items.add(objectMapper.treeToValue(n, CongestionForecastItem.class));
                } else if (!itemNode.isMissingNode() && !itemNode.isNull()) {
                    items.add(objectMapper.treeToValue(itemNode, CongestionForecastItem.class));
                }

                if (items.isEmpty()) break;
                result.addAll(items);
                if (result.size() >= total) break;
                page++;
            } catch (Exception e) {
                log.error("관광지집중률 API 호출 실패 - region={}, page={}", region, page, e);
                break;
            }
        }
        return result;
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) return null;
        try { return new BigDecimal(value); }
        catch (Exception e) { return null; }
    }
}
