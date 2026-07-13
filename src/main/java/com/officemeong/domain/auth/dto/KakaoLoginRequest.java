package com.officemeong.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "카카오 로그인 요청")
@Getter
@NoArgsConstructor
public class KakaoLoginRequest {

    @Schema(description = "카카오 OAuth 인가코드", example = "8XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXQ")
    @NotBlank(message = "인가 코드는 필수입니다.")
    private String code;
}
