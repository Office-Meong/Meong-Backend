package com.officemeong.domain.user.entity;

import com.officemeong.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "users",
    indexes = @Index(name = "idx_kakao_id", columnList = "kakao_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kakao_id", nullable = false, unique = true)
    private Long kakaoId;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(length = 100)
    private String email;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Column(name = "terms_agreed", nullable = false)
    private Boolean termsAgreed = false;

    @Column(name = "privacy_agreed", nullable = false)
    private Boolean privacyAgreed = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public User(Long kakaoId, String nickname, String profileImageUrl, String email,
                Boolean termsAgreed, Boolean privacyAgreed) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.email = email;
        this.termsAgreed = Boolean.TRUE.equals(termsAgreed);
        this.privacyAgreed = Boolean.TRUE.equals(privacyAgreed);
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.refreshToken = null;
    }

    public void restore(String nickname, String profileImageUrl, String email,
                        Boolean termsAgreed, Boolean privacyAgreed) {
        this.nickname = nickname != null ? nickname : "사용자";
        this.profileImageUrl = profileImageUrl;
        this.email = email;
        this.termsAgreed = Boolean.TRUE.equals(termsAgreed);
        this.privacyAgreed = Boolean.TRUE.equals(privacyAgreed);
        this.deletedAt = null;
        this.refreshToken = null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
