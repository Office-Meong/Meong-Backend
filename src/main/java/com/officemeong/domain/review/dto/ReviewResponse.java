package com.officemeong.domain.review.dto;

import com.officemeong.domain.review.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "리뷰 응답")
@Getter
@Builder
public class ReviewResponse {

    @Schema(description = "리뷰 ID", example = "1")
    private Long id;

    @Schema(description = "작성자 ID", example = "7")
    private Long userId;

    @Schema(description = "작성자 닉네임", example = "산책왕")
    private String userNickname;

    @Schema(description = "별점 (1~5)", example = "5")
    private Integer score;

    @Schema(description = "리뷰 내용", example = "강아지랑 오기 정말 좋았어요!")
    private String content;

    @Schema(description = "작성 시각")
    private LocalDateTime createdAt;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userNickname(review.getUser().getNickname())
                .score(review.getScore())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
