package com.officemeong.domain.user.dto;

import com.officemeong.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String nickname;
    private String profileImageUrl;
    private String email;
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
