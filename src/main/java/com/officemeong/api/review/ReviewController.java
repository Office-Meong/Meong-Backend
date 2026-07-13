package com.officemeong.api.review;

import com.officemeong.common.dto.ApiResponse;
import com.officemeong.domain.review.dto.ReviewRequest;
import com.officemeong.domain.review.dto.ReviewResponse;
import com.officemeong.domain.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "리뷰", description = "장소 리뷰 작성·조회·삭제 API")
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
            summary = "장소 리뷰 목록 조회",
            description = "특정 장소의 리뷰 목록을 최신순으로 반환합니다. 인증 없이 사용 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음")
    })
    @GetMapping("/api/v1/places/{placeId}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviews(
            @Parameter(description = "장소 ID", required = true) @PathVariable Long placeId) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getReviews(placeId)));
    }

    @Operation(
            summary = "리뷰 작성",
            description = "특정 장소에 리뷰를 작성합니다. 별점(1~5)과 내용은 필수입니다. 장소당 리뷰 1개만 작성 가능합니다.",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 작성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "별점 또는 내용 유효성 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 없음 또는 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "장소 또는 사용자를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 이 장소에 리뷰 작성함")
    })
    @PostMapping("/api/v1/places/{placeId}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "장소 ID", required = true) @PathVariable Long placeId,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.addReview(userId, placeId, request)));
    }

    @Operation(
            summary = "내 리뷰 삭제",
            description = "작성한 리뷰를 삭제합니다. 본인 리뷰만 삭제할 수 있습니다.",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 없음 또는 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없거나 권한 없음")
    })
    @DeleteMapping("/api/v1/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "삭제할 리뷰 ID", required = true) @PathVariable Long reviewId) {
        reviewService.deleteReview(userId, reviewId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
