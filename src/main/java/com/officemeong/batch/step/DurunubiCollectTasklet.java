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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
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

            BigDecimal[] coords = fetchGpxStartCoords(course.getGpxpath());
            if (coords == null) continue;

            walkCourseRepository.findBySourceId(course.getCrsIdx()).ifPresentOrElse(
                    existing -> log.debug("두루누비 코스 이미 존재: {}", course.getCrsIdx()),
                    () -> walkCourseRepository.save(WalkCourse.builder()
                            .sourceId(course.getCrsIdx())
                            .region(region)
                            .courseName(course.getCrsKorNm())
                            .startLatitude(coords[0])
                            .startLongitude(coords[1])
                            .distanceKm(parseDistance(course.getCrsDstnc()))
                            .build())
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
                } else if (!itemNode.isMissingNode() && !itemNode.isNull()) {
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

    /**
     * GPX 파일 URL을 다운로드하고 첫 번째 trkpt 좌표를 반환.
     * [0]=latitude, [1]=longitude. 파싱 실패 시 null 반환.
     */
    private BigDecimal[] fetchGpxStartCoords(String gpxPath) {
        if (gpxPath == null || gpxPath.isBlank()) return null;

        try {
            String content = ktoWebClient.get()
                    .uri(URI.create(gpxPath))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (content == null || content.isBlank()) return null;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(new StringReader(content)));

            NodeList trkpts = doc.getElementsByTagNameNS("*", "trkpt");
            if (trkpts.getLength() == 0) {
                trkpts = doc.getElementsByTagName("trkpt");
            }
            if (trkpts.getLength() == 0) return null;

            Element first = (Element) trkpts.item(0);
            String lat = first.getAttribute("lat");
            String lon = first.getAttribute("lon");
            if (lat.isBlank() || lon.isBlank()) return null;

            return new BigDecimal[]{new BigDecimal(lat), new BigDecimal(lon)};
        } catch (Exception e) {
            log.warn("GPX 파싱 실패 - {}: {}", gpxPath, e.getMessage());
            return null;
        }
    }

    private Region resolveRegion(String sigun) {
        if (sigun == null) return null;
        for (Region region : Region.values()) {
            if (sigun.equals(region.getDurunubiSigun())) return region;
        }
        return null;
    }

    private BigDecimal parseDistance(String crsDstnc) {
        if (crsDstnc == null || crsDstnc.isBlank()) return null;
        try { return new BigDecimal(crsDstnc.replaceAll("[^0-9.]", "")); }
        catch (Exception e) { return null; }
    }
}
