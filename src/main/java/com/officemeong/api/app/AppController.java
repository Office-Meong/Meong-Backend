package com.officemeong.api.app;

import com.officemeong.api.app.dto.AppPolicyResponse;
import com.officemeong.common.config.AppPolicyProperties;
import com.officemeong.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "앱 설정", description = "약관/정책/문의 링크 등 앱 공통 설정 API")
@RestController
@RequestMapping("/api/v1/app")
@RequiredArgsConstructor
public class AppController {

    private final AppPolicyProperties appPolicyProperties;

    @Operation(summary = "정책/문의 링크 조회",
            description = "이용약관, 개인정보 처리방침, 문의하기 URL을 반환합니다. 인증이 필요하지 않습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/policies")
    public ResponseEntity<ApiResponse<AppPolicyResponse>> getPolicies() {
        AppPolicyResponse response = AppPolicyResponse.builder()
                .termsUrl(appPolicyProperties.getTermsUrl())
                .privacyUrl(appPolicyProperties.getPrivacyUrl())
                .inquiryUrl(appPolicyProperties.getInquiryUrl())
                .build();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
