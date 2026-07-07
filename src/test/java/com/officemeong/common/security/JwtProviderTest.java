package com.officemeong.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtProvider 단위 테스트")
class JwtProviderTest {

    private JwtProvider jwtProvider;

    private static final String SECRET = "test-jwt-secret-key-minimum-32-characters!!";
    private static final long ACCESS_EXPIRY = 1800000L;  // 30분
    private static final long REFRESH_EXPIRY = 1209600000L; // 14일

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(SECRET, ACCESS_EXPIRY, REFRESH_EXPIRY);
    }

    @Test
    @DisplayName("액세스 토큰 생성 후 유효성 검증 통과")
    void createAccessToken_유효한_토큰_생성() {
        String token = jwtProvider.createAccessToken(1L);

        assertThat(jwtProvider.isValid(token)).isTrue();
        assertThat(jwtProvider.getUserId(token)).isEqualTo(1L);
    }

    @Test
    @DisplayName("리프레시 토큰 생성 후 유효성 검증 통과")
    void createRefreshToken_유효한_토큰_생성() {
        String token = jwtProvider.createRefreshToken(99L);

        assertThat(jwtProvider.isValid(token)).isTrue();
        assertThat(jwtProvider.getUserId(token)).isEqualTo(99L);
    }

    @Test
    @DisplayName("만료된 토큰은 유효성 검증 실패")
    void isValid_만료된_토큰_false_반환() {
        JwtProvider expiredProvider = new JwtProvider(SECRET, -1000L, -1000L);
        String token = expiredProvider.createAccessToken(1L);

        assertThat(expiredProvider.isValid(token)).isFalse();
    }

    @Test
    @DisplayName("변조된 토큰은 유효성 검증 실패")
    void isValid_변조된_토큰_false_반환() {
        String token = jwtProvider.createAccessToken(1L) + "tampered";

        assertThat(jwtProvider.isValid(token)).isFalse();
    }

    @Test
    @DisplayName("다른 시크릿으로 생성된 토큰은 유효성 검증 실패")
    void isValid_다른_시크릿으로_생성된_토큰_false_반환() {
        JwtProvider otherProvider = new JwtProvider("other-secret-key-minimum-32-chars!!", ACCESS_EXPIRY, REFRESH_EXPIRY);
        String token = otherProvider.createAccessToken(1L);

        assertThat(jwtProvider.isValid(token)).isFalse();
    }

    @Test
    @DisplayName("액세스 토큰 만료 시간(초) 정확히 반환")
    void getAccessTokenExpirySeconds_올바른_값_반환() {
        assertThat(jwtProvider.getAccessTokenExpirySeconds()).isEqualTo(ACCESS_EXPIRY / 1000);
    }

    @Test
    @DisplayName("토큰에서 올바른 userId 추출")
    void getUserId_올바른_userId_추출() {
        Long expectedUserId = 42L;
        String token = jwtProvider.createAccessToken(expectedUserId);

        assertThat(jwtProvider.getUserId(token)).isEqualTo(expectedUserId);
    }
}
