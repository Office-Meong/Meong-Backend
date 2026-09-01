package com.officemeong.domain.user.service;

import com.officemeong.domain.course.repository.CourseChecklistItemRepository;
import com.officemeong.domain.course.repository.CourseRepository;
import com.officemeong.domain.dog.repository.DogRepository;
import com.officemeong.domain.favorite.repository.FavoriteRepository;
import com.officemeong.domain.review.repository.ReviewRepository;
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
    private final CourseChecklistItemRepository courseChecklistItemRepository;
    private final CourseRepository courseRepository;
    private final FavoriteRepository favoriteRepository;
    private final ReviewRepository reviewRepository;
    private final DogRepository dogRepository;

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
        courseChecklistItemRepository.deleteByUserId(userId);
        courseRepository.deleteByUserId(userId);
        favoriteRepository.deleteByUserId(userId);
        reviewRepository.deleteByUserId(userId);
        dogRepository.deleteByUserId(userId);
        user.delete();
    }

    @Transactional
    public void purgeDeletedUser(Long userId) {
        courseChecklistItemRepository.deleteByUserId(userId);
        courseRepository.deleteByUserId(userId);
        favoriteRepository.deleteByUserId(userId);
        reviewRepository.deleteByUserId(userId);
        dogRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId));
    }
}
