package com.officemeong.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_AUTH = "BearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OfficeMeong API")
                        .description("""
                                ## 반려동물 동반 장소 추천 서비스 OfficeMeong API 명세서

                                ### 인증 방법
                                1. `POST /api/v1/auth/kakao` 로 카카오 로그인하여 `accessToken` 획득
                                2. 우측 상단 **Authorize** 버튼 클릭 → `Bearer {accessToken}` 입력
                                3. 인증이 필요한 API는 자물쇠(🔒) 아이콘으로 표시됨

                                ### 공통 응답 형식
                                ```json
                                {
                                  "success": true,
                                  "data": { ... },
                                  "message": null
                                }
                                ```

                                ### 에러 응답 형식
                                ```json
                                {
                                  "success": false,
                                  "data": null,
                                  "message": "오류 메시지"
                                }
                                ```
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("OfficeMeong 개발팀")
                                .email("a01057571492@gmail.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("로컬 개발 서버"),
                        new Server().url("https://api.officemeong.com").description("운영 서버")
                ))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("로그인 후 발급받은 Access Token을 입력하세요 (Bearer 접두사 없이 토큰만 입력)"))
                )
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
