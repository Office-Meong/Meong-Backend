package com.officemeong.domain.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Schema(description = "코스 아이템 추가 요청")
@Getter
@NoArgsConstructor
public class CourseItemCreateRequest {

    @Schema(description = "추가할 일차", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "일차는 필수입니다.")
    private Integer dayNumber;

    @Schema(description = "추가할 장소 ID", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "장소 ID는 필수입니다.")
    private Long placeId;

    @Schema(description = "해당 일차 내 삽입 위치 (1부터 시작, null이거나 범위를 벗어나면 맨 뒤에 추가)", example = "3")
    private Integer visitOrder;

    @Schema(description = "방문 시작 시간 (선택)", example = "14:00")
    private LocalTime startTime;

    @Schema(description = "방문 종료 시간 (선택)", example = "15:00")
    private LocalTime endTime;

    @Schema(description = "슬롯 레이블 (선택, 예: 오후 관광)", example = "오후 관광")
    private String slotLabel;
}
