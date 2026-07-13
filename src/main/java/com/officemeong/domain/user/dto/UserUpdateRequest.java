package com.officemeong.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "프로필 수정 요청")
@Getter
@NoArgsConstructor
public class UserUpdateRequest {

    @Schema(description = "닉네임 (필수, 최대 50자)", example = "산책왕멍이아빠", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 50)
    private String nickname;

    @Schema(description = "프로필 이미지 URL (선택)", example = "https://k.kakaocdn.net/dn/XXXXXX/profile.jpg")
    private String profileImageUrl;
}
