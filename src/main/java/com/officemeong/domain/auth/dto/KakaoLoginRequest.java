package com.officemeong.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
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

    @Schema(description = "인가코드 발급 시 사용한 client_id. 생략 시 서버 기본값(네이티브 앱 키) 사용. 브라우저 테스트 시 REST API 키를 명시적으로 전달.", example = "014f0b58da4749b59745aca73a0e623f")
    private String clientId;

    @Schema(description = "인가코드 발급 시 사용한 redirect_uri (플랫폼별 상이). Android: kakao{appKey}://oauth, 웹: http://...", example = "kakao014f0b58da4749b59745aca73a0e623f://oauth")
    @NotBlank(message = "redirect_uri는 필수입니다.")
    private String redirectUri;

    @Schema(description = "서비스 이용약관 동의 여부 (신규 가입 시 필수)", example = "true")
    private Boolean termsAgreed;

    @Schema(description = "개인정보 처리방침 동의 여부 (신규 가입 시 필수)", example = "true")
    private Boolean privacyAgreed;
}
