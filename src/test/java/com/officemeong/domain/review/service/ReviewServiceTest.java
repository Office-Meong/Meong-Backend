package com.officemeong.domain.review.service;

import com.officemeong.domain.place.entity.Place;
import com.officemeong.domain.place.repository.PlaceRepository;
import com.officemeong.domain.review.dto.ReviewRequest;
import com.officemeong.domain.review.dto.ReviewResponse;
import com.officemeong.domain.review.entity.Review;
import com.officemeong.domain.review.repository.ReviewRepository;
import com.officemeong.domain.user.entity.User;
import com.officemeong.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 단위 테스트")
class ReviewServiceTest {

    @Mock ReviewRepository reviewRepository;
    @Mock UserRepository userRepository;
    @Mock PlaceRepository placeRepository;

    @InjectMocks ReviewService reviewService;

    @Test
    @DisplayName("리뷰 목록 조회")
    void getReviews_목록_반환() {
        Review review = mockReview(1L, 1L, "산책왕", 5, "좋았어요!");
        when(reviewRepository.findByPlaceIdWithUser(10L)).thenReturn(List.of(review));

        List<ReviewResponse> result = reviewService.getReviews(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getScore()).isEqualTo(5);
        assertThat(result.get(0).getContent()).isEqualTo("좋았어요!");
    }

    @Test
    @DisplayName("리뷰 목록 - 빈 결과")
    void getReviews_빈_결과() {
        when(reviewRepository.findByPlaceIdWithUser(10L)).thenReturn(List.of());

        List<ReviewResponse> result = reviewService.getReviews(10L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("리뷰 작성 성공")
    void addReview_작성_성공() {
        when(reviewRepository.existsByUserIdAndPlaceId(1L, 10L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mock(User.class)));
        when(placeRepository.findById(10L)).thenReturn(Optional.of(mock(Place.class)));

        ReviewRequest request = mockRequest(4, "좋았어요!");
        Review saved = mockReview(1L, 1L, "산책왕", 4, "좋았어요!");
        when(reviewRepository.save(any(Review.class))).thenReturn(saved);

        ReviewResponse response = reviewService.addReview(1L, 10L, request);

        assertThat(response.getScore()).isEqualTo(4);
        assertThat(response.getContent()).isEqualTo("좋았어요!");
    }

    @Test
    @DisplayName("중복 리뷰 작성 시 예외")
    void addReview_중복_예외() {
        when(reviewRepository.existsByUserIdAndPlaceId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.addReview(1L, 10L, mockRequest(5, "내용")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미");
    }

    @Test
    @DisplayName("존재하지 않는 장소 리뷰 작성 시 예외")
    void addReview_없는_장소_예외() {
        when(reviewRepository.existsByUserIdAndPlaceId(1L, 99L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mock(User.class)));
        when(placeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.addReview(1L, 99L, mockRequest(5, "내용")))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void deleteReview_삭제_성공() {
        Review review = mockReview(1L, 1L, "산책왕", 5, "좋았어요!");
        when(reviewRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(review));

        reviewService.deleteReview(1L, 1L);

        verify(reviewRepository).delete(review);
    }

    @Test
    @DisplayName("타인의 리뷰 삭제 시 예외")
    void deleteReview_권한_없음_예외() {
        when(reviewRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteReview(2L, 1L))
                .isInstanceOf(NoSuchElementException.class);
    }

    private Review mockReview(Long reviewId, Long userId, String nickname, int score, String content) {
        User user = mock(User.class);
        lenient().when(user.getId()).thenReturn(userId);
        lenient().when(user.getNickname()).thenReturn(nickname);

        Review review = mock(Review.class);
        lenient().when(review.getId()).thenReturn(reviewId);
        lenient().when(review.getUser()).thenReturn(user);
        lenient().when(review.getScore()).thenReturn(score);
        lenient().when(review.getContent()).thenReturn(content);
        lenient().when(review.getCreatedAt()).thenReturn(null);
        return review;
    }

    private ReviewRequest mockRequest(int score, String content) {
        ReviewRequest req = mock(ReviewRequest.class);
        lenient().when(req.getScore()).thenReturn(score);
        lenient().when(req.getContent()).thenReturn(content);
        return req;
    }
}
