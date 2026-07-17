package com.officemeong.domain.course.dto;

import com.officemeong.domain.course.enums.WorkFocusLevel;
import com.officemeong.domain.place.enums.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "워케이션 코스 생성 요청")
@Getter
public class CourseCreateRequest {

    @Schema(description = "여행 지역", example = "GANGNEUNG", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Region region;

    @Schema(description = "여행 시작일", example = "2026-08-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private LocalDate startDate;

    @Schema(description = "여행 종료일", example = "2026-08-03", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private LocalDate endDate;

    @Schema(description = "하루 업무 시작 시간", example = "09:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private LocalTime workStartTime;

    @Schema(description = "하루 업무 종료 시간", example = "18:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private LocalTime workEndTime;

    @Schema(description = "업무 집중도 (LOW=관광 중심 / MEDIUM=균형 / HIGH=업무 집중)", example = "MEDIUM",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private WorkFocusLevel workFocusLevel;

    @Schema(description = "반려견 ID (선택). 지정 시 반려견 체중으로 장소 크기 제한 필터링", example = "1")
    private Long dogId;

    @Schema(description = "코스 이름 (선택). 미입력 시 자동 생성 (예: 강릉 3일 워케이션)", example = "우리 가족 강릉 여행")
    @jakarta.validation.constraints.Size(max = 100)
    private String name;
}
