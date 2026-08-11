package com.officemeong.api.place;

import com.officemeong.common.config.SecurityConfig;
import com.officemeong.common.dto.PageResponse;
import com.officemeong.common.security.JwtAuthenticationFilter;
import com.officemeong.domain.place.dto.PlaceDetailResponse;
import com.officemeong.domain.place.dto.PlaceSummaryResponse;
import com.officemeong.domain.place.enums.CongestionLevel;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.place.service.PlaceService;
import com.officemeong.domain.walk.dto.NearbyWalkCourseResponse;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = PlaceController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@DisplayName("PlaceController Web MVC 테스트")
class PlaceControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean PlaceService placeService;

    @Test
    @DisplayName("장소 목록 조회 - 200 OK")
    void getPlaces_200_반환() throws Exception {
        PlaceSummaryResponse summary = PlaceSummaryResponse.builder()
                .id(1L).name("테스트 카페").region(Region.GANGNEUNG)
                .placeType(PlaceType.WORK_PLACE).totalScore(70).build();

        when(placeService.getPlaces(any(), any(), any(), any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(PageResponse.of(List.of(summary), 0, 20, 1));

        mockMvc.perform(get("/api/v1/places")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("테스트 카페"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("지역 필터 적용 장소 목록 조회 - 200 OK")
    void getPlaces_지역_필터_200_반환() throws Exception {
        when(placeService.getPlaces(eq(Region.GANGNEUNG), isNull(),
                eq(PlaceService.SortType.SCORE), isNull(), isNull(), eq(0), eq(20), isNull()))
                .thenReturn(PageResponse.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/places")
                        .param("region", "GANGNEUNG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("타입 필터 + 최신순 정렬 장소 목록 조회 - 200 OK")
    void getPlaces_타입_필터_최신순_200_반환() throws Exception {
        when(placeService.getPlaces(isNull(), eq(PlaceType.STAY),
                eq(PlaceService.SortType.LATEST), isNull(), isNull(), eq(0), eq(10), isNull()))
                .thenReturn(PageResponse.of(List.of(), 0, 10, 0));

        mockMvc.perform(get("/api/v1/places")
                        .param("type", "STAY")
                        .param("sort", "LATEST")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("키워드 검색 적용 장소 목록 조회 - 200 OK")
    void getPlaces_키워드_검색_200_반환() throws Exception {
        when(placeService.getPlaces(isNull(), isNull(),
                eq(PlaceService.SortType.SCORE), isNull(), eq("펫카페"), eq(0), eq(20), isNull()))
                .thenReturn(PageResponse.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/places")
                        .param("keyword", "펫카페"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("장소 상세 조회 - 200 OK")
    void getPlace_200_반환() throws Exception {
        PlaceDetailResponse detail = PlaceDetailResponse.builder()
                .id(1L).name("테스트 숙소").region(Region.CHUNCHEON)
                .placeType(PlaceType.STAY).address("강원도 춘천시 테스트로 1")
                .imageUrls(List.of()).build();

        when(placeService.getPlace(1L, null)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/places/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("테스트 숙소"))
                .andExpect(jsonPath("$.data.region").value("CHUNCHEON"));
    }

    @Test
    @DisplayName("존재하지 않는 장소 상세 조회 - 404 반환")
    void getPlace_존재하지_않는_id_404_반환() throws Exception {
        when(placeService.getPlace(999L, null))
                .thenThrow(new NoSuchElementException("장소를 찾을 수 없습니다: 999"));

        mockMvc.perform(get("/api/v1/places/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("장소를 찾을 수 없습니다: 999"));
    }

    @Test
    @DisplayName("잘못된 정렬 파라미터 - 400 반환")
    void getPlaces_잘못된_sort_파라미터_400_반환() throws Exception {
        mockMvc.perform(get("/api/v1/places")
                        .param("sort", "INVALID_SORT"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("혼잡도 필터 RELAXED 적용 장소 목록 조회 - 200 OK")
    void getPlaces_혼잡도_RELAXED_필터_200_반환() throws Exception {
        when(placeService.getPlaces(isNull(), isNull(),
                eq(PlaceService.SortType.SCORE), eq(CongestionLevel.RELAXED), isNull(), eq(0), eq(20), isNull()))
                .thenReturn(PageResponse.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/places")
                        .param("congestion", "RELAXED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("장소 주변 산책 코스 조회 - 200 OK")
    void getNearbyWalkCourses_200() throws Exception {
        NearbyWalkCourseResponse resp = NearbyWalkCourseResponse.builder()
                .id(1L).courseName("경포 해변 산책로").region(Region.GANGNEUNG)
                .distanceKm(BigDecimal.valueOf(2.5))
                .startLatitude(BigDecimal.valueOf(37.797))
                .startLongitude(BigDecimal.valueOf(128.901))
                .distanceFromPlaceKm(0.8)
                .build();
        when(placeService.getNearbyWalkCourses(1L)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/v1/places/1/walk-courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].courseName").value("경포 해변 산책로"))
                .andExpect(jsonPath("$.data[0].distanceFromPlaceKm").value(0.8));
    }

    @Test
    @DisplayName("장소 주변 산책 코스 조회 - 존재하지 않는 장소 404")
    void getNearbyWalkCourses_없는장소_404() throws Exception {
        when(placeService.getNearbyWalkCourses(999L))
                .thenThrow(new NoSuchElementException("장소를 찾을 수 없습니다: 999"));

        mockMvc.perform(get("/api/v1/places/999/walk-courses"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("장소 주변 산책 코스 없을 때 빈 배열 반환")
    void getNearbyWalkCourses_빈결과() throws Exception {
        when(placeService.getNearbyWalkCourses(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/places/1/walk-courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
