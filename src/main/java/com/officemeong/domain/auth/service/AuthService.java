package com.officemeong.domain.auth.service;

import com.officemeong.common.security.JwtProvider;
import com.officemeong.domain.auth.dto.TokenResponse;
import com.officemeong.domain.user.entity.User;
import com.officemeong.domain.user.repository.UserRepository;
import com.officemeong.domain.user.service.UserService;
import com.officemeong.infrastructure.kakao.KakaoOAuthClient;
import com.officemeong.infrastructure.kakao.dto.KakaoTokenResponse;
import com.officemeong.infrastructure.kakao.dto.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoOAuthClient kakaoClient;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtProvider jwtProvider;

    @Transactional
    public TokenResponse kakaoLogin(String authorizationCode, String clientId, String redirectUri, Boolean termsAgreed, Boolean privacyAgreed) {
        KakaoTokenResponse kakaoToken = kakaoClient.getToken(authorizationCode, clientId, redirectUri);
        KakaoUserInfoResponse userInfo = kakaoClient.getUserInfo(kakaoToken.getAccessToken());

        User user = userRepository.findByKakaoId(userInfo.getId()).orElse(null);

        if (user == null) {
            // 신규 가입
            if (!Boolean.TRUE.equals(termsAgreed) || !Boolean.TRUE.equals(privacyAgreed)) {
                throw new IllegalArgumentException("서비스 이용약관 및 개인정보 처리방침에 동의해야 합니다.");
            }
            user = userRepository.save(User.builder()
                    .kakaoId(userInfo.getId())
                    .nickname(userInfo.getNickname() != null ? userInfo.getNickname() : "사용자")
                    .profileImageUrl(userInfo.getProfileImageUrl())
                    .email(userInfo.getEmail())
                    .termsAgreed(termsAgreed)
                    .privacyAgreed(privacyAgreed)
                    .build());
        } else if (user.isDeleted()) {
            // 재가입: 기존 데이터 완전 삭제 후 신규 회원으로 생성
            if (!Boolean.TRUE.equals(termsAgreed) || !Boolean.TRUE.equals(privacyAgreed)) {
                throw new IllegalArgumentException("서비스 이용약관 및 개인정보 처리방침에 동의해야 합니다.");
            }
            userService.purgeDeletedUser(user.getId());
            userRepository.flush();
            user = userRepository.save(User.builder()
                    .kakaoId(userInfo.getId())
                    .nickname(userInfo.getNickname() != null ? userInfo.getNickname() : "사용자")
                    .profileImageUrl(userInfo.getProfileImageUrl())
                    .email(userInfo.getEmail())
                    .termsAgreed(termsAgreed)
                    .privacyAgreed(privacyAgreed)
                    .build());
        }

        return issueTokens(user);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        if (!jwtProvider.isValid(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        Long userId = jwtProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new IllegalArgumentException("리프레시 토큰이 일치하지 않습니다.");
        }

        String newAccessToken = jwtProvider.createAccessToken(userId);
        return TokenResponse.of(newAccessToken, refreshToken, jwtProvider.getAccessTokenExpirySeconds());
    }

    @Transactional
    public void logout(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.updateRefreshToken(null);
            userRepository.save(user);
        });
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        user.updateRefreshToken(refreshToken);
        userRepository.save(user);
        return TokenResponse.of(accessToken, refreshToken, jwtProvider.getAccessTokenExpirySeconds());
    }
}
