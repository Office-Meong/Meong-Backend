package com.officemeong.api.review;

import com.officemeong.common.config.SecurityConfig;
import com.officemeong.common.security.JwtAuthenticationFilter;
import com.officemeong.domain.review.dto.ReviewResponse;
import com.officemeong.domain.review.service.ReviewService;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = ReviewController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@Import(ReviewControllerTest.TestSecurityConfig.class)
@DisplayName("ReviewController Web MVC 테스트")
class ReviewControllerTest {

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
    @MockBean ReviewService reviewService;

    private static RequestPostProcessor authAs(Long userId) {
        return SecurityMockMvcRequestPostProcessors.authentication(
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList())
        );
    }

    @Test
    @DisplayName("리뷰 목록 조회 - 200 OK")
    void getReviews_200_반환() throws Exception {
        ReviewResponse resp = ReviewResponse.builder()
                .id(1L).userId(1L).userNickname("산책왕").score(5).content("좋았어요!").build();
        when(reviewService.getReviews(10L)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/v1/places/10/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].score").value(5));
    }

    @Test
    @DisplayName("리뷰 작성 - 200 OK")
    void addReview_200_반환() throws Exception {
        ReviewResponse resp = ReviewResponse.builder()
                .id(1L).userId(1L).userNickname("산책왕").score(4).content("좋았어요!").build();
        when(reviewService.addReview(eq(1L), eq(10L), any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/places/10/reviews")
                        .with(authAs(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\": 4, \"content\": \"좋았어요!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.score").value(4));
    }

    @Test
    @DisplayName("리뷰 작성 - 별점 누락 400 반환")
    void addReview_유효성_오류_400() throws Exception {
        mockMvc.perform(post("/api/v1/places/10/reviews")
                        .with(authAs(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\": \"내용만 있음\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("중복 리뷰 작성 - 409 반환")
    void addReview_중복_409() throws Exception {
        when(reviewService.addReview(eq(1L), eq(10L), any()))
                .thenThrow(new IllegalStateException("이미 이 장소에 리뷰를 작성하셨습니다."));

        mockMvc.perform(post("/api/v1/places/10/reviews")
                        .with(authAs(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\": 5, \"content\": \"내용\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("리뷰 삭제 - 200 OK")
    void deleteReview_200_반환() throws Exception {
        doNothing().when(reviewService).deleteReview(1L, 1L);

        mockMvc.perform(delete("/api/v1/reviews/1").with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("타인 리뷰 삭제 시 404 반환")
    void deleteReview_권한없음_404() throws Exception {
        doThrow(new NoSuchElementException("리뷰를 찾을 수 없습니다: 1"))
                .when(reviewService).deleteReview(2L, 1L);

        mockMvc.perform(delete("/api/v1/reviews/1").with(authAs(2L)))
                .andExpect(status().isNotFound());
    }
}
