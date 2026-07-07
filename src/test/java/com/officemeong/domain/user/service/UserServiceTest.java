package com.officemeong.domain.user.service;

import com.officemeong.domain.user.dto.UserResponse;
import com.officemeong.domain.user.dto.UserUpdateRequest;
import com.officemeong.domain.user.entity.User;
import com.officemeong.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock UserRepository userRepository;

    @InjectMocks UserService userService;

    @Test
    @DisplayName("내 정보 조회")
    void getMe_정상_반환() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getNickname()).thenReturn("테스터");
        when(user.isDeleted()).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getMe(1L);

        assertThat(response.getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("탈퇴한 사용자 조회 시 예외 발생")
    void getMe_탈퇴_사용자_예외() {
        User user = mock(User.class);
        when(user.isDeleted()).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.getMe(1L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 예외 발생")
    void getMe_없는_사용자_예외() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMe(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("프로필 수정 성공")
    void updateMe_정상_수정() {
        User user = User.builder().kakaoId(123L).nickname("기존닉네임").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserUpdateRequest request = mock(UserUpdateRequest.class);
        when(request.getNickname()).thenReturn("새닉네임");
        when(request.getProfileImageUrl()).thenReturn("https://img.example.com/new.jpg");

        UserResponse response = userService.updateMe(1L, request);

        assertThat(response.getNickname()).isEqualTo("새닉네임");
        assertThat(response.getProfileImageUrl()).isEqualTo("https://img.example.com/new.jpg");
    }

    @Test
    @DisplayName("회원 탈퇴 시 deletedAt 설정")
    void deleteMe_삭제_처리() {
        User user = User.builder().kakaoId(123L).nickname("유저").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteMe(1L);

        assertThat(user.isDeleted()).isTrue();
        assertThat(user.getRefreshToken()).isNull();
    }
}
