package com.officemeong.api.app;

import com.officemeong.common.config.AppPolicyProperties;
import com.officemeong.common.config.SecurityConfig;
import com.officemeong.common.security.JwtAuthenticationFilter;
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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = AppController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@Import(AppControllerTest.TestSecurityConfig.class)
@DisplayName("AppController 웹 MVC 테스트")
class AppControllerTest {

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
    @MockBean AppPolicyProperties appPolicyProperties;

    @Test
    @DisplayName("정책 링크 조회 - 인증 없이 200 OK")
    void getPolicies_200() throws Exception {
        when(appPolicyProperties.getTermsUrl()).thenReturn("https://officemeong.example.com/terms");
        when(appPolicyProperties.getPrivacyUrl()).thenReturn("https://officemeong.example.com/privacy");
        when(appPolicyProperties.getInquiryUrl()).thenReturn("https://officemeong.example.com/inquiry");

        mockMvc.perform(get("/api/v1/app/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.termsUrl").value("https://officemeong.example.com/terms"))
                .andExpect(jsonPath("$.data.privacyUrl").value("https://officemeong.example.com/privacy"))
                .andExpect(jsonPath("$.data.inquiryUrl").value("https://officemeong.example.com/inquiry"));
    }
}
