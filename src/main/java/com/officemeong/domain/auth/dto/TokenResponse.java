package com.officemeong.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "로그인/토큰 재발급 응답")
@Getter
@Builder
public class TokenResponse {

    @Schema(description = "API 인증에 사용하는 Access Token (Authorization 헤더에 Bearer 형식으로 사용)")
    private final String accessToken;

    @Schema(description = "Access Token 만료 시 재발급에 사용하는 Refresh Token")
    private final String refreshToken;

    @Schema(description = "토큰 타입 (항상 Bearer)", example = "Bearer")
    private final String tokenType;

    @Schema(description = "Access Token 만료까지 남은 시간(초)", example = "1800")
    private final long accessTokenExpiresIn;

    public static TokenResponse of(String accessToken, String refreshToken, long expiresInSeconds) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .accessTokenExpiresIn(expiresInSeconds)
                .build();
    }
}
