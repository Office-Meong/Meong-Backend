package com.officemeong.domain.user.dto;

import com.officemeong.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "내 계정 정보 응답")
@Getter
@Builder
public class UserResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "닉네임", example = "댕댕이집사")
    private String nickname;

    @Schema(description = "프로필 이미지 URL (카카오 제공, 없으면 null)", example = "http://k.kakaocdn.net/dn/profile.jpg")
    private String profileImageUrl;

    @Schema(description = "이메일 (카카오 동의항목 미제공 시 null)", example = "user@example.com")
    private String email;

    @Schema(description = "가입일시", example = "2026-01-01T12:00:00")
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
