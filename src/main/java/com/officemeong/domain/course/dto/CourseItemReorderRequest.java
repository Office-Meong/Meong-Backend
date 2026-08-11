package com.officemeong.domain.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "코스 아이템 순서 변경 요청 (드래그 앤 드롭 결과 반영)")
@Getter
@NoArgsConstructor
public class CourseItemReorderRequest {

    @Schema(description = "순서를 변경할 일차", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "일차는 필수입니다.")
    private Integer dayNumber;

    @Schema(description = "해당 일차에 속한 전체 아이템 ID를 원하는 순서대로 나열한 목록 (해당 일차의 기존 아이템 구성과 정확히 일치해야 함)",
            example = "[15, 13, 14, 11, 12]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "아이템 목록은 필수입니다.")
    private List<Long> itemIds;
}
