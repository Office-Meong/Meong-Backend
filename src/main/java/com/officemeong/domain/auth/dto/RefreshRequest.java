package com.officemeong.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "토큰 재발급 요청")
@Getter
@NoArgsConstructor
public class RefreshRequest {

    @Schema(description = "로그인 시 발급받은 Refresh Token", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "리프레시 토큰은 필수입니다.")
    private String refreshToken;
}
