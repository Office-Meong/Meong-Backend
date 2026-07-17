package com.officemeong.api.favorite;

import com.officemeong.common.config.SecurityConfig;
import com.officemeong.common.security.JwtAuthenticationFilter;
import com.officemeong.domain.favorite.dto.FavoriteResponse;
import com.officemeong.domain.favorite.service.FavoriteService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = FavoriteController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@Import(FavoriteControllerTest.TestSecurityConfig.class)
@DisplayName("FavoriteController Web MVC 테스트")
class FavoriteControllerTest {

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
    @MockBean FavoriteService favoriteService;

    private static RequestPostProcessor authAs(Long userId) {
        return SecurityMockMvcRequestPostProcessors.authentication(
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList())
        );
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회 - 200 OK")
    void getMyFavorites_200_반환() throws Exception {
        FavoriteResponse resp = FavoriteResponse.builder()
                .placeId(10L).placeName("강릉 카페").placeType(PlaceType.FOOD)
                .region(Region.GANGNEUNG).address("강원도 강릉시").build();
        when(favoriteService.getMyFavorites(eq(1L), isNull(), isNull())).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/v1/favorites").with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].placeName").value("강릉 카페"));
    }

    @Test
    @DisplayName("즐겨찾기 추가 - 200 OK")
    void addFavorite_200_반환() throws Exception {
        FavoriteResponse resp = FavoriteResponse.builder()
                .placeId(10L).placeName("강릉 카페").placeType(PlaceType.FOOD)
                .region(Region.GANGNEUNG).build();
        when(favoriteService.addFavorite(1L, 10L)).thenReturn(resp);

        mockMvc.perform(post("/api/v1/favorites/10").with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.placeId").value(10));
    }

    @Test
    @DisplayName("이미 즐겨찾기한 장소 추가 - 409 반환")
    void addFavorite_중복_409() throws Exception {
        when(favoriteService.addFavorite(1L, 10L))
                .thenThrow(new IllegalStateException("이미 즐겨찾기한 장소입니다."));

        mockMvc.perform(post("/api/v1/favorites/10").with(authAs(1L)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("즐겨찾기 취소 - 200 OK")
    void removeFavorite_200_반환() throws Exception {
        doNothing().when(favoriteService).removeFavorite(1L, 10L);

        mockMvc.perform(delete("/api/v1/favorites/10").with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("없는 즐겨찾기 취소 - 404 반환")
    void removeFavorite_없음_404() throws Exception {
        doThrow(new NoSuchElementException("즐겨찾기를 찾을 수 없습니다."))
                .when(favoriteService).removeFavorite(1L, 99L);

        mockMvc.perform(delete("/api/v1/favorites/99").with(authAs(1L)))
                .andExpect(status().isNotFound());
    }
}
