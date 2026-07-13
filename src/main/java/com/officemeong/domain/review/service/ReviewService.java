package com.officemeong.domain.review.service;

import com.officemeong.domain.place.entity.Place;
import com.officemeong.domain.place.repository.PlaceRepository;
import com.officemeong.domain.review.dto.ReviewRequest;
import com.officemeong.domain.review.dto.ReviewResponse;
import com.officemeong.domain.review.entity.Review;
import com.officemeong.domain.review.repository.ReviewRepository;
import com.officemeong.domain.user.entity.User;
import com.officemeong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;

    public List<ReviewResponse> getReviews(Long placeId) {
        return reviewRepository.findByPlaceIdWithUser(placeId).stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Transactional
    public ReviewResponse addReview(Long userId, Long placeId, ReviewRequest request) {
        if (reviewRepository.existsByUserIdAndPlaceId(userId, placeId)) {
            throw new IllegalStateException("이미 이 장소에 리뷰를 작성하셨습니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId));
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new NoSuchElementException("장소를 찾을 수 없습니다: " + placeId));

        Review review = reviewRepository.save(Review.builder()
                .user(user)
                .place(place)
                .score(request.getScore())
                .content(request.getContent())
                .build());
        return ReviewResponse.from(review);
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new NoSuchElementException("리뷰를 찾을 수 없습니다: " + reviewId));
        reviewRepository.delete(review);
    }
}
