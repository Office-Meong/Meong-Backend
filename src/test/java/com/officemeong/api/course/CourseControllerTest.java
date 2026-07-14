package com.officemeong.api.course;

import com.officemeong.common.config.SecurityConfig;
import com.officemeong.common.security.JwtAuthenticationFilter;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.walk.dto.CourseRecommendResponse;
import com.officemeong.domain.walk.service.CourseService;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = CourseController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@Import(CourseControllerTest.TestSecurityConfig.class)
@DisplayName("CourseController Web MVC 테스트")
class CourseControllerTest {

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
    @MockBean CourseService courseService;

    private static RequestPostProcessor authAs(Long userId) {
        return SecurityMockMvcRequestPostProcessors.authentication(
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList())
        );
    }

    @Test
    @DisplayName("코스 추천 조회 - dogId 없이 200 OK")
    void recommend_dogId없음_200() throws Exception {
        CourseRecommendResponse resp = CourseRecommendResponse.builder()
                .id(1L).courseName("경포 해변 산책로").region(Region.GANGNEUNG)
                .distanceKm(BigDecimal.valueOf(2.5))
                .startLatitude(BigDecimal.valueOf(37.797))
                .startLongitude(BigDecimal.valueOf(128.901))
                .distanceFromUserKm(0.15)
                .build();
        when(courseService.recommend(37.796, 128.900, 1L, null)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/v1/courses")
                        .param("lat", "37.796")
                        .param("lng", "128.900")
                        .with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].courseName").value("경포 해변 산책로"))
                .andExpect(jsonPath("$.data[0].distanceFromUserKm").value(0.15));
    }

    @Test
    @DisplayName("코스 추천 조회 - dogId 포함 200 OK")
    void recommend_dogId포함_200() throws Exception {
        CourseRecommendResponse resp = CourseRecommendResponse.builder()
                .id(2L).courseName("춘천 호반 산책로").region(Region.CHUNCHEON)
                .distanceKm(BigDecimal.valueOf(1.8))
                .startLatitude(BigDecimal.valueOf(37.880))
                .startLongitude(BigDecimal.valueOf(127.730))
                .distanceFromUserKm(0.30)
                .build();
        when(courseService.recommend(37.796, 128.900, 1L, 5L)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/v1/courses")
                        .param("lat", "37.796")
                        .param("lng", "128.900")
                        .param("dogId", "5")
                        .with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(2));
    }

    @Test
    @DisplayName("코스 없을 때 빈 배열 반환")
    void recommend_빈_결과() throws Exception {
        when(courseService.recommend(37.796, 128.900, 1L, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/courses")
                        .param("lat", "37.796")
                        .param("lng", "128.900")
                        .with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 반려견 ID - 404 반환")
    void recommend_없는_반려견_404() throws Exception {
        when(courseService.recommend(37.796, 128.900, 1L, 99L))
                .thenThrow(new NoSuchElementException("반려견을 찾을 수 없습니다. id=99"));

        mockMvc.perform(get("/api/v1/courses")
                        .param("lat", "37.796")
                        .param("lng", "128.900")
                        .param("dogId", "99")
                        .with(authAs(1L)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("lat/lng 파라미터 누락 - 400 Bad Request")
    void recommend_파라미터_누락_400() throws Exception {
        mockMvc.perform(get("/api/v1/courses")
                        .with(authAs(1L)))
                .andExpect(status().isBadRequest());
    }
}
