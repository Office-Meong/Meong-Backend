package com.officemeong.domain.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "체크리스트 항목 수정 요청")
@Getter
@NoArgsConstructor
public class ChecklistItemUpdateRequest {

    @Schema(description = "수정할 내용 (null이면 변경 안 함)", example = "강아지 사료 챙기기")
    @Size(max = 200)
    private String content;

    @Schema(description = "체크 여부 (null이면 변경 안 함)", example = "true")
    private Boolean checked;
}
