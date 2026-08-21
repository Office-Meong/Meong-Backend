package com.officemeong.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.officemeong.common.config.SecurityConfig;
import com.officemeong.common.security.JwtAuthenticationFilter;
import com.officemeong.domain.auth.dto.TokenResponse;
import com.officemeong.domain.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@DisplayName("AuthController Web MVC 테스트")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AuthService authService;

    @Test
    @DisplayName("카카오 로그인 - 200 OK")
    void kakaoLogin_200_반환() throws Exception {
        TokenResponse tokenResponse = TokenResponse.of("access-token", "refresh-token", 1800L);
        when(authService.kakaoLogin("auth-code", null, "kakao014f0b58da4749b59745aca73a0e623f://oauth", true, true)).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/v1/auth/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"auth-code\", \"redirectUri\": \"kakao014f0b58da4749b59745aca73a0e623f://oauth\", \"termsAgreed\": true, \"privacyAgreed\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("카카오 로그인 - 인가코드 누락 시 400 반환")
    void kakaoLogin_인가코드_누락_400_반환() throws Exception {
        mockMvc.perform(post("/api/v1/auth/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("토큰 재발급 - 200 OK")
    void refresh_200_반환() throws Exception {
        TokenResponse tokenResponse = TokenResponse.of("new-access-token", "refresh-token", 1800L);
        when(authService.refresh("refresh-token")).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @Test
    @DisplayName("토큰 재발급 - 유효하지 않은 토큰 시 400 반환")
    void refresh_유효하지_않은_토큰_400_반환() throws Exception {
        when(authService.refresh("bad-token"))
                .thenThrow(new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"bad-token\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
