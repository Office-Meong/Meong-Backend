package com.officemeong.batch.step;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.walk.entity.WalkCourse;
import com.officemeong.domain.walk.repository.WalkCourseRepository;
import com.officemeong.infrastructure.kto.dto.DurunubiCourseItem;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DurunubiCollectTasklet implements Tasklet {

    private final WebClient ktoWebClient;
    private final WalkCourseRepository walkCourseRepository;
    private final ObjectMapper objectMapper;

    @Value("${api.kto.service-key}")
    private String serviceKey;

    @Value("${api.kto.base-url}")
    private String baseUrl;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<DurunubiCourseItem> allCourses = fetchAllCourses();
        int saved = 0;

        for (DurunubiCourseItem course : allCourses) {
            Region region = resolveRegion(course.getSigun());
            if (region == null) continue;

            // GPX에서 시작점 좌표 추출 (현재는 GPX path만 저장, 실제 구현 시 GPX 파싱 필요)
            BigDecimal startLat = extractStartLatFromGpx(course.getGpxpath());
            BigDecimal startLon = extractStartLonFromGpx(course.getGpxpath());
            if (startLat == null || startLon == null) continue;

            walkCourseRepository.findBySourceId(course.getCrsIdx()).ifPresentOrElse(
                    existing -> log.debug("두루누비 코스 이미 존재: {}", course.getCrsIdx()),
                    () -> {
                        BigDecimal distanceKm = parseDistance(course.getCrsDstnc());
                        walkCourseRepository.save(WalkCourse.builder()
                                .sourceId(course.getCrsIdx())
                                .region(region)
                                .courseName(course.getCrsKorNm())
                                .startLatitude(startLat)
                                .startLongitude(startLon)
                                .distanceKm(distanceKm)
                                .build());
                    }
            );
            saved++;
        }

        log.info("두루누비 코스 수집 완료: {}개", saved);
        return RepeatStatus.FINISHED;
    }

    private List<DurunubiCourseItem> fetchAllCourses() {
        List<DurunubiCourseItem> result = new ArrayList<>();
        int page = 1;

        while (true) {
            URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/Durunubi/courseList")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "OfficeMeong")
                    .queryParam("_type", "json")
                    .queryParam("brdDiv", "DNWW")
                    .queryParam("numOfRows", 50)
                    .queryParam("pageNo", page)
                    .build(true)
                    .toUri();

            try {
                String raw = ktoWebClient.get().uri(uri).retrieve().bodyToMono(String.class).block();
                JsonNode root = objectMapper.readTree(raw);
                JsonNode itemNode = root.path("response").path("body").path("items").path("item");
                int total = root.path("response").path("body").path("totalCount").asInt(0);

                List<DurunubiCourseItem> items = new ArrayList<>();
                if (itemNode.isArray()) {
                    for (JsonNode n : itemNode) items.add(objectMapper.treeToValue(n, DurunubiCourseItem.class));
                } else if (!itemNode.isMissingNode()) {
                    items.add(objectMapper.treeToValue(itemNode, DurunubiCourseItem.class));
                }

                if (items.isEmpty()) break;
                result.addAll(items);
                if (result.size() >= total) break;
                page++;
            } catch (Exception e) {
                log.error("두루누비 courseList 호출 실패 - page={}", page, e);
                break;
            }
        }
        return result;
    }

    private Region resolveRegion(String sigun) {
        if (sigun == null) return null;
        for (Region region : Region.values()) {
            if (sigun.equals(region.getDurunubiSigun())) return region;
        }
        return null;
    }

    /**
     * GPX URL에서 시작점 좌표 추출.
     * 실제 구현: GPX 파일 다운로드 → XML 파싱 → 첫 번째 trkpt 좌표 추출
     * 현재는 null 반환 (추후 구현)
     */
    private BigDecimal extractStartLatFromGpx(String gpxPath) {
        // TODO: GPX 파일 파싱 구현
        return null;
    }

    private BigDecimal extractStartLonFromGpx(String gpxPath) {
        // TODO: GPX 파일 파싱 구현
        return null;
    }

    private BigDecimal parseDistance(String crsDstnc) {
        if (crsDstnc == null || crsDstnc.isBlank()) return null;
        try { return new BigDecimal(crsDstnc.replaceAll("[^0-9.]", "")); }
        catch (Exception e) { return null; }
    }
}
