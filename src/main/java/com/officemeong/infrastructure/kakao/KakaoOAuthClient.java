package com.officemeong.infrastructure.kakao;

import com.officemeong.infrastructure.kakao.dto.KakaoTokenResponse;
import com.officemeong.infrastructure.kakao.dto.KakaoUserInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
public class KakaoOAuthClient {

    private final WebClient authClient;
    private final WebClient apiClient;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    // 카카오 개발자 콘솔 > 카카오 로그인 > 보안 탭에서 Client Secret을 활성화한 경우에만 필요
    @Value("${kakao.client-secret:}")
    private String clientSecret;

    public KakaoOAuthClient(WebClient.Builder webClientBuilder) {
        this.authClient = webClientBuilder.clone()
                .baseUrl("https://kauth.kakao.com")
                .build();
        this.apiClient = webClientBuilder.clone()
                .baseUrl("https://kapi.kakao.com")
                .build();
    }

    public KakaoTokenResponse getToken(String authorizationCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", authorizationCode);
        if (clientSecret != null && !clientSecret.isBlank()) {
            params.add("client_secret", clientSecret);
        }

        return authClient.post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class)
                .doOnError(WebClientResponseException.class,
                        e -> log.error("카카오 토큰 발급 실패 - status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString()))
                .block();
    }

    public KakaoUserInfoResponse getUserInfo(String kakaoAccessToken) {
        return apiClient.get()
                .uri("/v2/user/me")
                .header("Authorization", "Bearer " + kakaoAccessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfoResponse.class)
                .doOnError(WebClientResponseException.class,
                        e -> log.error("카카오 사용자 정보 조회 실패 - status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString()))
                .block();
    }
}
