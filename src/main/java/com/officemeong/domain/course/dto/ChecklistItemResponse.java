package com.officemeong.domain.course.dto;

import com.officemeong.domain.course.entity.CourseChecklistItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "체크리스트 항목 응답")
@Getter
@Builder
public class ChecklistItemResponse {

    @Schema(description = "항목 ID", example = "1")
    private Long id;

    @Schema(description = "내용", example = "강아지 목줄 챙기기")
    private String content;

    @Schema(description = "체크 여부", example = "false")
    private boolean checked;

    @Schema(description = "표시 순서", example = "1")
    private int displayOrder;

    public static ChecklistItemResponse from(CourseChecklistItem item) {
        return ChecklistItemResponse.builder()
                .id(item.getId())
                .content(item.getContent())
                .checked(item.isChecked())
                .displayOrder(item.getDisplayOrder())
                .build();
    }
}
