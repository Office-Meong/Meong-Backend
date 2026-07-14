package com.officemeong.api.recommend;

import com.officemeong.ai.dto.PlaceRecommendResponse;
import com.officemeong.ai.service.RecommendService;
import com.officemeong.common.config.SecurityConfig;
import com.officemeong.common.security.JwtAuthenticationFilter;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = RecommendController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@Import(RecommendControllerTest.TestSecurityConfig.class)
@DisplayName("RecommendController Web MVC 테스트")
class RecommendControllerTest {

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            return http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(a -> a.anyRequest().permitAll()).build();
        }
    }

    @Autowired MockMvc mockMvc;
    @MockBean RecommendService recommendService;

    private static RequestPostProcessor authAs(Long userId) {
        return SecurityMockMvcRequestPostProcessors.authentication(
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList()));
    }

    @Test
    @DisplayName("AI 장소 추천 - 200 OK")
    void recommend_200() throws Exception {
        PlaceRecommendResponse resp = PlaceRecommendResponse.builder()
                .placeId(42L).placeName("경포 펫카페")
                .region(Region.GANGNEUNG).placeType(PlaceType.FOOD)
                .address("강원도 강릉시").totalScore(78).grade("B")
                .reason("소형견과 함께 방문하기 좋은 펫카페입니다.")
                .build();
        when(recommendService.recommend(eq(Region.GANGNEUNG), isNull(), eq(1L)))
                .thenReturn(List.of(resp));

        mockMvc.perform(get("/api/v1/recommend")
                        .param("region", "GANGNEUNG")
                        .with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].placeName").value("경포 펫카페"))
                .andExpect(jsonPath("$.data[0].reason").value("소형견과 함께 방문하기 좋은 펫카페입니다."));
    }

    @Test
    @DisplayName("AI 장소 추천 - dogId 포함 200 OK")
    void recommend_dogId포함_200() throws Exception {
        when(recommendService.recommend(eq(Region.CHUNCHEON), eq(5L), eq(1L)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/recommend")
                        .param("region", "CHUNCHEON")
                        .param("dogId", "5")
                        .with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("region 파라미터 누락 - 400 Bad Request")
    void recommend_region누락_400() throws Exception {
        mockMvc.perform(get("/api/v1/recommend").with(authAs(1L)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 반려견 ID - 404")
    void recommend_없는반려견_404() throws Exception {
        when(recommendService.recommend(eq(Region.GANGNEUNG), eq(99L), eq(1L)))
                .thenThrow(new NoSuchElementException("반려견을 찾을 수 없습니다. id=99"));

        mockMvc.perform(get("/api/v1/recommend")
                        .param("region", "GANGNEUNG")
                        .param("dogId", "99")
                        .with(authAs(1L)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("후보 장소 없을 때 빈 배열 반환")
    void recommend_후보없음_빈배열() throws Exception {
        when(recommendService.recommend(eq(Region.WONJU), isNull(), eq(1L)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/recommend")
                        .param("region", "WONJU")
                        .with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
