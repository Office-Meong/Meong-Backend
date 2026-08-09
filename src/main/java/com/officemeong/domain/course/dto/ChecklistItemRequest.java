package com.officemeong.domain.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "체크리스트 항목 추가 요청")
@Getter
@NoArgsConstructor
public class ChecklistItemRequest {

    @Schema(description = "체크리스트 항목 내용", example = "강아지 목줄 챙기기", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "체크리스트 내용은 필수입니다.")
    @Size(max = 200)
    private String content;
}
