package com.officemeong.api.course;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.officemeong.common.config.SecurityConfig;
import com.officemeong.common.security.JwtAuthenticationFilter;
import com.officemeong.domain.course.dto.CourseCreateRequest;
import com.officemeong.domain.course.dto.CourseItemResponse;
import com.officemeong.domain.course.dto.CourseResponse;
import com.officemeong.domain.course.enums.WorkFocusLevel;
import com.officemeong.domain.course.service.CourseService;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
            return http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(a -> a.anyRequest().permitAll()).build();
        }
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean CourseService courseService;

    private static RequestPostProcessor authAs(Long userId) {
        return SecurityMockMvcRequestPostProcessors.authentication(
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList()));
    }

    private CourseResponse sampleCourse() {
        CourseItemResponse item = CourseItemResponse.builder()
                .id(1L).dayNumber(1).visitOrder(1).slotLabel("아침")
                .placeId(10L).placeName("강릉 카페").placeType(PlaceType.FOOD)
                .startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(9, 0))
                .build();
        return CourseResponse.builder()
                .id(1L).region(Region.GANGNEUNG)
                .startDate(LocalDate.of(2026, 8, 1)).endDate(LocalDate.of(2026, 8, 1))
                .workStartTime(LocalTime.of(9, 0)).workEndTime(LocalTime.of(18, 0))
                .workFocusLevel(WorkFocusLevel.MEDIUM).totalDays(1)
                .dayItems(Map.of(1, List.of(item)))
                .build();
    }

    @Test
    @DisplayName("코스 생성 - 200 OK")
    void createCourse_200() throws Exception {
        when(courseService.createCourse(eq(1L), any(CourseCreateRequest.class)))
                .thenReturn(sampleCourse());

        String body = """
                {
                  "region": "GANGNEUNG",
                  "startDate": "2026-08-01",
                  "endDate": "2026-08-01",
                  "workStartTime": "09:00",
                  "workEndTime": "18:00",
                  "workFocusLevel": "MEDIUM"
                }
                """;

        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON).content(body)
                        .with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.region").value("GANGNEUNG"))
                .andExpect(jsonPath("$.data.dayItems.1[0].slotLabel").value("아침"));
    }

    @Test
    @DisplayName("코스 생성 - region 누락 400 Bad Request")
    void createCourse_region누락_400() throws Exception {
        String body = """
                {
                  "startDate": "2026-08-01",
                  "endDate": "2026-08-01",
                  "workStartTime": "09:00",
                  "workEndTime": "18:00",
                  "workFocusLevel": "MEDIUM"
                }
                """;

        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON).content(body)
                        .with(authAs(1L)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("내 코스 목록 조회 - 200 OK")
    void getMyCourses_200() throws Exception {
        when(courseService.getMyCourses(1L)).thenReturn(List.of(sampleCourse()));

        mockMvc.perform(get("/api/v1/courses").with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @DisplayName("코스 상세 조회 - 200 OK")
    void getCourse_200() throws Exception {
        when(courseService.getCourse(1L, 1L)).thenReturn(sampleCourse());

        mockMvc.perform(get("/api/v1/courses/1").with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalDays").value(1));
    }

    @Test
    @DisplayName("코스 상세 조회 - 없는 코스 404")
    void getCourse_없음_404() throws Exception {
        when(courseService.getCourse(1L, 99L))
                .thenThrow(new NoSuchElementException("코스를 찾을 수 없습니다. id=99"));

        mockMvc.perform(get("/api/v1/courses/99").with(authAs(1L)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("코스 삭제 - 200 OK")
    void deleteCourse_200() throws Exception {
        doNothing().when(courseService).deleteCourse(1L, 1L);

        mockMvc.perform(delete("/api/v1/courses/1").with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("대체 장소 추천 - 200 OK")
    void getAlternatives_200() throws Exception {
        when(courseService.getAlternatives(1L, 1L, 3L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/courses/1/items/3/alternatives").with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
