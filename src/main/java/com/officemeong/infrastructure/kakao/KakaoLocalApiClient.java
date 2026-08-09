package com.officemeong.infrastructure.kakao;

import com.officemeong.infrastructure.kakao.dto.KakaoLocalSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Component
public class KakaoLocalApiClient {

    private final WebClient localClient;

    // 카카오 로컬(지도) API는 로그인과 동일한 REST API 키를 Authorization 헤더로 사용
    @Value("${kakao.client-id}")
    private String restApiKey;

    public KakaoLocalApiClient(WebClient.Builder webClientBuilder) {
        this.localClient = webClientBuilder.clone()
                .baseUrl("https://dapi.kakao.com")
                .build();
    }

    /**
     * 키워드 장소 검색. 최대 15건(1페이지)만 조회한다.
     */
    public List<KakaoLocalSearchResponse.Document> searchKeyword(String query) {
        try {
            KakaoLocalSearchResponse response = localClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/keyword.json")
                            .queryParam("query", query)
                            .queryParam("size", 15)
                            .build())
                    .header("Authorization", "KakaoAK " + restApiKey)
                    .retrieve()
                    .bodyToMono(KakaoLocalSearchResponse.class)
                    .block();
            return response != null && response.getDocuments() != null ? response.getDocuments() : List.of();
        } catch (Exception e) {
            log.error("카카오 로컬 검색 실패 - query={}", query, e);
            return List.of();
        }
    }
}
