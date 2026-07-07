package com.officemeong.domain.user.service;

import com.officemeong.domain.user.dto.UserResponse;
import com.officemeong.domain.user.dto.UserUpdateRequest;
import com.officemeong.domain.user.entity.User;
import com.officemeong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getMe(Long userId) {
        User user = findUser(userId);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateMe(Long userId, UserUpdateRequest request) {
        User user = findUser(userId);
        user.updateProfile(request.getNickname(), request.getProfileImageUrl());
        return UserResponse.from(user);
    }

    @Transactional
    public void deleteMe(Long userId) {
        User user = findUser(userId);
        user.delete();
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId));
    }
}
