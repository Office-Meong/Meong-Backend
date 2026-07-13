package com.officemeong.api.auth;

import com.officemeong.common.dto.ApiResponse;
import com.officemeong.domain.auth.dto.KakaoLoginRequest;
import com.officemeong.domain.auth.dto.RefreshRequest;
import com.officemeong.domain.auth.dto.TokenResponse;
import com.officemeong.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "카카오 OAuth 로그인 및 JWT 토큰 관리 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "카카오 로그인",
            description = "카카오 인가코드로 로그인하여 Access/Refresh Token을 발급합니다. " +
                    "신규 사용자는 자동으로 회원가입됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공 — accessToken, refreshToken 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인가코드 누락 또는 형식 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "카카오 API 호출 실패")
    })
    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<TokenResponse>> kakaoLogin(
            @Valid @RequestBody KakaoLoginRequest request) {
        TokenResponse token = authService.kakaoLogin(request.getCode());
        return ResponseEntity.ok(ApiResponse.ok(token));
    }

    @Operation(
            summary = "Access Token 재발급",
            description = "만료된 Access Token을 Refresh Token으로 재발급합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Refresh Token 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh Token 만료 또는 유효하지 않음")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody RefreshRequest request) {
        TokenResponse token = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok(token));
    }

    @Operation(
            summary = "로그아웃",
            description = "현재 사용자의 Refresh Token을 만료시킵니다. 이후 재발급이 불가합니다.",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 없음 또는 만료")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
