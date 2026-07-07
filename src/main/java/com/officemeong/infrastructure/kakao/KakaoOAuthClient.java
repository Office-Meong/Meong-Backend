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

@Slf4j
@Component
public class KakaoOAuthClient {

    private final WebClient authClient;
    private final WebClient apiClient;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

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

        return authClient.post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class)
                .doOnError(e -> log.error("카카오 토큰 발급 실패", e))
                .block();
    }

    public KakaoUserInfoResponse getUserInfo(String kakaoAccessToken) {
        return apiClient.get()
                .uri("/v2/user/me")
                .header("Authorization", "Bearer " + kakaoAccessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfoResponse.class)
                .doOnError(e -> log.error("카카오 사용자 정보 조회 실패", e))
                .block();
    }
}
