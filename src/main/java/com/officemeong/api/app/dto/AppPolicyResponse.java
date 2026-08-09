package com.officemeong.api.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "앱 정책/문의 링크 응답")
@Getter
@Builder
public class AppPolicyResponse {

    @Schema(description = "이용약관 URL", example = "https://officemeong.example.com/terms")
    private String termsUrl;

    @Schema(description = "개인정보 처리방침 URL", example = "https://officemeong.example.com/privacy")
    private String privacyUrl;

    @Schema(description = "문의하기 URL", example = "https://officemeong.example.com/inquiry")
    private String inquiryUrl;
}
