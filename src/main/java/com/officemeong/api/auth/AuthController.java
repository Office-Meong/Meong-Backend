package com.officemeong.api.auth;

import com.officemeong.common.dto.ApiResponse;
import com.officemeong.domain.auth.dto.KakaoLoginRequest;
import com.officemeong.domain.auth.dto.RefreshRequest;
import com.officemeong.domain.auth.dto.TokenResponse;
import com.officemeong.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 카카오 로그인
     * POST /api/v1/auth/kakao
     * Body: { "code": "인가코드" }
     */
    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<TokenResponse>> kakaoLogin(
            @Valid @RequestBody KakaoLoginRequest request) {
        TokenResponse token = authService.kakaoLogin(request.getCode());
        return ResponseEntity.ok(ApiResponse.ok(token));
    }

    /**
     * Access Token 재발급
     * POST /api/v1/auth/refresh
     * Body: { "refreshToken": "..." }
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody RefreshRequest request) {
        TokenResponse token = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok(token));
    }

    /**
     * 로그아웃 (Refresh Token 만료)
     * POST /api/v1/auth/logout
     * Authorization: Bearer <accessToken>
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
