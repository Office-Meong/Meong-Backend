package com.officemeong.domain.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalTime;

@Schema(description = "코스 아이템 수정 요청")
@Getter
public class CourseItemUpdateRequest {

    @Schema(description = "방문 시작 시간 (null이면 변경 안 함)", example = "10:00")
    private LocalTime startTime;

    @Schema(description = "방문 종료 시간 (null이면 변경 안 함)", example = "11:30")
    private LocalTime endTime;

    @Schema(description = "교체할 장소 ID (null이면 장소 변경 안 함)", example = "42")
    private Long newPlaceId;
}
