package com.officemeong.domain.auth.service;

import com.officemeong.common.security.JwtProvider;
import com.officemeong.domain.auth.dto.TokenResponse;
import com.officemeong.domain.user.entity.User;
import com.officemeong.domain.user.repository.UserRepository;
import com.officemeong.domain.user.service.UserService;
import com.officemeong.infrastructure.kakao.KakaoOAuthClient;
import com.officemeong.infrastructure.kakao.dto.KakaoTokenResponse;
import com.officemeong.infrastructure.kakao.dto.KakaoUserInfoResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @Mock KakaoOAuthClient kakaoClient;
    @Mock UserRepository userRepository;
    @Mock UserService userService;
    @Mock JwtProvider jwtProvider;

    @InjectMocks AuthService authService;

    @Test
    @DisplayName("신규 사용자 카카오 로그인 시 회원가입 후 토큰 발급")
    void kakaoLogin_신규_사용자_가입_및_토큰_발급() {
        KakaoTokenResponse kakaoToken = mock(KakaoTokenResponse.class);
        KakaoUserInfoResponse userInfo = mock(KakaoUserInfoResponse.class);
        User newUser = mockUser(1L);

        when(kakaoClient.getToken("auth-code", null, "kakao1234://oauth")).thenReturn(kakaoToken);
        when(kakaoToken.getAccessToken()).thenReturn("kakao-access-token");
        when(kakaoClient.getUserInfo("kakao-access-token")).thenReturn(userInfo);
        when(userInfo.getId()).thenReturn(123456L);
        when(userInfo.getNickname()).thenReturn("테스트유저");
        when(userInfo.getProfileImageUrl()).thenReturn(null);
        when(userInfo.getEmail()).thenReturn("test@test.com");
        when(userRepository.findByKakaoId(123456L)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(jwtProvider.createAccessToken(1L)).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(1L)).thenReturn("refresh-token");
        when(jwtProvider.getAccessTokenExpirySeconds()).thenReturn(1800L);

        TokenResponse response = authService.kakaoLogin("auth-code", null, "kakao1234://oauth", true, true);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        verify(userRepository, atLeast(1)).save(any(User.class));
    }

    @Test
    @DisplayName("기존 사용자 카카오 로그인 시 토큰만 발급")
    void kakaoLogin_기존_사용자_토큰_발급() {
        KakaoTokenResponse kakaoToken = mock(KakaoTokenResponse.class);
        KakaoUserInfoResponse userInfo = mock(KakaoUserInfoResponse.class);
        User existingUser = mockUser(1L);

        when(kakaoClient.getToken("auth-code", null, "kakao1234://oauth")).thenReturn(kakaoToken);
        when(kakaoToken.getAccessToken()).thenReturn("kakao-access-token");
        when(kakaoClient.getUserInfo("kakao-access-token")).thenReturn(userInfo);
        when(userInfo.getId()).thenReturn(123456L);
        when(userRepository.findByKakaoId(123456L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(jwtProvider.createAccessToken(1L)).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(1L)).thenReturn("refresh-token");
        when(jwtProvider.getAccessTokenExpirySeconds()).thenReturn(1800L);

        TokenResponse response = authService.kakaoLogin("auth-code", null, "kakao1234://oauth", null, null);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 액세스 토큰 재발급")
    void refresh_유효한_토큰으로_액세스_토큰_재발급() {
        User user = mock(User.class);
        when(user.getRefreshToken()).thenReturn("valid-refresh-token");

        when(jwtProvider.isValid("valid-refresh-token")).thenReturn(true);
        when(jwtProvider.getUserId("valid-refresh-token")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtProvider.createAccessToken(1L)).thenReturn("new-access-token");
        when(jwtProvider.getAccessTokenExpirySeconds()).thenReturn(1800L);

        TokenResponse response = authService.refresh("valid-refresh-token");

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("valid-refresh-token");
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰은 예외 발생")
    void refresh_유효하지_않은_토큰_예외_발생() {
        when(jwtProvider.isValid("invalid-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh("invalid-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 리프레시 토큰");
    }

    @Test
    @DisplayName("DB 저장 토큰과 불일치하는 리프레시 토큰은 예외 발생")
    void refresh_DB_불일치_토큰_예외_발생() {
        User user = mock(User.class);
        when(user.getRefreshToken()).thenReturn("stored-token");

        when(jwtProvider.isValid("different-token")).thenReturn(true);
        when(jwtProvider.getUserId("different-token")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.refresh("different-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("리프레시 토큰이 일치하지 않습니다");
    }

    @Test
    @DisplayName("로그아웃 시 리프레시 토큰 초기화")
    void logout_리프레시_토큰_초기화() {
        User user = User.builder().kakaoId(123L).nickname("유저").build();
        user.updateRefreshToken("refresh-token");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        authService.logout(1L);

        assertThat(user.getRefreshToken()).isNull();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("탈퇴한 사용자가 약관 동의 시 재가입(신규 회원 생성) 후 토큰 발급")
    void kakaoLogin_탈퇴_사용자_재가입_성공() {
        KakaoTokenResponse kakaoToken = mock(KakaoTokenResponse.class);
        KakaoUserInfoResponse userInfo = mock(KakaoUserInfoResponse.class);
        User deletedUser = mock(User.class);
        User newUser = mockUser(2L);
        when(deletedUser.isDeleted()).thenReturn(true);
        when(deletedUser.getId()).thenReturn(1L);

        when(kakaoClient.getToken("auth-code", null, "kakao1234://oauth")).thenReturn(kakaoToken);
        when(kakaoToken.getAccessToken()).thenReturn("kakao-access-token");
        when(kakaoClient.getUserInfo("kakao-access-token")).thenReturn(userInfo);
        when(userInfo.getId()).thenReturn(123456L);
        when(userInfo.getNickname()).thenReturn("테스트유저");
        when(userRepository.findByKakaoId(123456L)).thenReturn(Optional.of(deletedUser));
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(jwtProvider.createAccessToken(2L)).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(2L)).thenReturn("refresh-token");
        when(jwtProvider.getAccessTokenExpirySeconds()).thenReturn(1800L);

        TokenResponse response = authService.kakaoLogin("auth-code", null, "kakao1234://oauth", true, true);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        verify(userService).purgeDeletedUser(1L);
        verify(userRepository, atLeast(1)).save(any(User.class));
        verify(deletedUser, never()).restore(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("탈퇴한 사용자가 약관 미동의 시 재가입 불가")
    void kakaoLogin_탈퇴_사용자_약관_미동의_예외() {
        KakaoTokenResponse kakaoToken = mock(KakaoTokenResponse.class);
        KakaoUserInfoResponse userInfo = mock(KakaoUserInfoResponse.class);
        User deletedUser = mock(User.class);
        when(deletedUser.isDeleted()).thenReturn(true);

        when(kakaoClient.getToken("auth-code", null, "kakao1234://oauth")).thenReturn(kakaoToken);
        when(kakaoToken.getAccessToken()).thenReturn("kakao-access-token");
        when(kakaoClient.getUserInfo("kakao-access-token")).thenReturn(userInfo);
        when(userInfo.getId()).thenReturn(123456L);
        when(userRepository.findByKakaoId(123456L)).thenReturn(Optional.of(deletedUser));

        assertThatThrownBy(() -> authService.kakaoLogin("auth-code", null, "kakao1234://oauth", false, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("동의");
    }

    @Test
    @DisplayName("신규 사용자가 약관 미동의 시 예외 발생")
    void kakaoLogin_신규_사용자_약관_미동의_예외() {
        KakaoTokenResponse kakaoToken = mock(KakaoTokenResponse.class);
        KakaoUserInfoResponse userInfo = mock(KakaoUserInfoResponse.class);

        when(kakaoClient.getToken("auth-code", null, "kakao1234://oauth")).thenReturn(kakaoToken);
        when(kakaoToken.getAccessToken()).thenReturn("kakao-access-token");
        when(kakaoClient.getUserInfo("kakao-access-token")).thenReturn(userInfo);
        when(userInfo.getId()).thenReturn(99999L);
        when(userRepository.findByKakaoId(99999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.kakaoLogin("auth-code", null, "kakao1234://oauth", false, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("동의");
    }

    private User mockUser(Long id) {
        User user = mock(User.class);
        lenient().when(user.getId()).thenReturn(id);
        lenient().when(user.isDeleted()).thenReturn(false);
        return user;
    }
}
