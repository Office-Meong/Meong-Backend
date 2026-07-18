package com.officemeong.api.upload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.officemeong.api.upload.dto.UploadPresignedResponse;
import com.officemeong.common.config.SecurityConfig;
import com.officemeong.common.security.JwtAuthenticationFilter;
import com.officemeong.infrastructure.s3.S3UploadService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = UploadController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@Import(UploadControllerTest.TestSecurityConfig.class)
@DisplayName("UploadController Web MVC 테스트")
class UploadControllerTest {

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
    @MockBean S3UploadService s3UploadService;

    private static RequestPostProcessor authAs(Long userId) {
        return SecurityMockMvcRequestPostProcessors.authentication(
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList()));
    }

    @Test
    @DisplayName("Presigned URL 발급 - 200 OK")
    void generatePresignedUrl_200() throws Exception {
        when(s3UploadService.generatePresignedUrl(eq(1L), eq("dog.jpg"), eq("image/jpeg")))
                .thenReturn(UploadPresignedResponse.builder()
                        .presignedUrl("https://bucket.s3.ap-northeast-2.amazonaws.com/dogs/1/uuid.jpg?X-Amz-Signature=abc")
                        .imageUrl("https://bucket.s3.ap-northeast-2.amazonaws.com/dogs/1/uuid.jpg")
                        .build());

        String body = """
                {
                  "filename": "dog.jpg",
                  "contentType": "image/jpeg"
                }
                """;

        mockMvc.perform(post("/api/v1/upload/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON).content(body)
                        .with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.presignedUrl").exists())
                .andExpect(jsonPath("$.data.imageUrl").value(
                        "https://bucket.s3.ap-northeast-2.amazonaws.com/dogs/1/uuid.jpg"));
    }

    @Test
    @DisplayName("Presigned URL 발급 - filename 누락 400")
    void generatePresignedUrl_filename누락_400() throws Exception {
        String body = """
                {
                  "contentType": "image/jpeg"
                }
                """;

        mockMvc.perform(post("/api/v1/upload/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON).content(body)
                        .with(authAs(1L)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Presigned URL 발급 - 지원하지 않는 형식 400")
    void generatePresignedUrl_지원않는형식_400() throws Exception {
        when(s3UploadService.generatePresignedUrl(eq(1L), eq("doc.pdf"), eq("application/pdf")))
                .thenThrow(new IllegalArgumentException("지원하지 않는 이미지 형식입니다."));

        String body = """
                {
                  "filename": "doc.pdf",
                  "contentType": "application/pdf"
                }
                """;

        mockMvc.perform(post("/api/v1/upload/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON).content(body)
                        .with(authAs(1L)))
                .andExpect(status().isBadRequest());
    }
}
