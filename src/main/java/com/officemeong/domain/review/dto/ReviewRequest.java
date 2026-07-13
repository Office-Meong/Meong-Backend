package com.officemeong.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "리뷰 작성 요청")
@Getter
@NoArgsConstructor
public class ReviewRequest {

    @Schema(description = "별점 (1~5)", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Min(1) @Max(5)
    private Integer score;

    @Schema(description = "리뷰 내용 (최대 500자)", example = "반려견과 함께 방문하기 좋았습니다!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String content;
}
