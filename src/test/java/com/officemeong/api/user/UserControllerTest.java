package com.officemeong.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.officemeong.common.config.SecurityConfig;
import com.officemeong.common.security.JwtAuthenticationFilter;
import com.officemeong.domain.user.dto.UserResponse;
import com.officemeong.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = UserController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@Import(UserControllerTest.TestSecurityConfig.class)
@DisplayName("UserController Web MVC 테스트")
class UserControllerTest {

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                    .build();
        }
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean UserService userService;

    @Test
    @DisplayName("내 정보 조회 - 200 OK")
    void getMe_200_반환() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1L).nickname("테스터").email("test@test.com")
                .createdAt(LocalDateTime.now()).build();

        when(userService.getMe(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/me")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("테스터"));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 - 404 반환")
    void getMe_없는_사용자_404() throws Exception {
        when(userService.getMe(99L))
                .thenThrow(new NoSuchElementException("사용자를 찾을 수 없습니다: 99"));

        mockMvc.perform(get("/api/v1/users/me")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(99L, null, Collections.emptyList()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("프로필 수정 - 200 OK")
    void updateMe_200_반환() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1L).nickname("새닉네임").createdAt(LocalDateTime.now()).build();

        when(userService.updateMe(eq(1L), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/users/me")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\": \"새닉네임\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("새닉네임"));
    }

    @Test
    @DisplayName("프로필 수정 - 닉네임 누락 400 반환")
    void updateMe_닉네임_누락_400() throws Exception {
        mockMvc.perform(patch("/api/v1/users/me")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원 탈퇴 - 200 OK")
    void deleteMe_200_반환() throws Exception {
        doNothing().when(userService).deleteMe(1L);

        mockMvc.perform(delete("/api/v1/users/me")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
