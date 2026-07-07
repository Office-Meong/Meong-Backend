package com.officemeong.api.dog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.officemeong.common.config.SecurityConfig;
import com.officemeong.common.security.JwtAuthenticationFilter;
import com.officemeong.domain.dog.dto.DogResponse;
import com.officemeong.domain.dog.service.DogService;
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
        value = DogController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@Import(DogControllerTest.TestSecurityConfig.class)
@DisplayName("DogController Web MVC 테스트")
class DogControllerTest {

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

    @MockBean DogService dogService;

    private static RequestPostProcessor authAs(Long userId) {
        return SecurityMockMvcRequestPostProcessors.authentication(
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList())
        );
    }

    @Test
    @DisplayName("반려견 목록 조회 - 200 OK")
    void getMyDogs_200_반환() throws Exception {
        DogResponse dog = DogResponse.builder().id(1L).name("뽀삐").isNeutered(false).build();
        when(dogService.getMyDogs(1L)).thenReturn(List.of(dog));

        mockMvc.perform(get("/api/v1/dogs")
                        .with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("뽀삐"));
    }

    @Test
    @DisplayName("반려견 등록 - 200 OK")
    void addDog_200_반환() throws Exception {
        DogResponse dog = DogResponse.builder().id(1L).name("뽀삐").isNeutered(false).build();
        when(dogService.addDog(eq(1L), any())).thenReturn(dog);

        mockMvc.perform(post("/api/v1/dogs")
                        .with(authAs(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"뽀삐\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("뽀삐"));
    }

    @Test
    @DisplayName("반려견 등록 - 이름 누락 400 반환")
    void addDog_이름_누락_400() throws Exception {
        mockMvc.perform(post("/api/v1/dogs")
                        .with(authAs(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("반려견 수정 - 200 OK")
    void updateDog_200_반환() throws Exception {
        DogResponse dog = DogResponse.builder().id(1L).name("초코").isNeutered(true).build();
        when(dogService.updateDog(eq(1L), eq(1L), any())).thenReturn(dog);

        mockMvc.perform(put("/api/v1/dogs/1")
                        .with(authAs(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"초코\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("초코"));
    }

    @Test
    @DisplayName("타인의 반려견 수정 시 404 반환")
    void updateDog_없는_반려견_404() throws Exception {
        when(dogService.updateDog(eq(2L), eq(1L), any()))
                .thenThrow(new NoSuchElementException("반려견을 찾을 수 없습니다: 1"));

        mockMvc.perform(put("/api/v1/dogs/1")
                        .with(authAs(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"초코\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("반려견 삭제 - 200 OK")
    void deleteDog_200_반환() throws Exception {
        doNothing().when(dogService).deleteDog(1L, 1L);

        mockMvc.perform(delete("/api/v1/dogs/1")
                        .with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
