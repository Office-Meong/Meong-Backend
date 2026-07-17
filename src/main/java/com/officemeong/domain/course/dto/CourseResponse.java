package com.officemeong.domain.course.dto;

import com.officemeong.domain.course.entity.Course;
import com.officemeong.domain.course.enums.WorkFocusLevel;
import com.officemeong.domain.place.enums.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Schema(description = "워케이션 코스 응답")
@Getter
@Builder
public class CourseResponse {

    @Schema(description = "코스 ID", example = "1")
    private Long id;

    @Schema(description = "코스 이름", example = "강릉 3일 워케이션")
    private String name;

    @Schema(description = "여행 지역", example = "GANGNEUNG")
    private Region region;

    @Schema(description = "여행 시작일", example = "2026-08-01")
    private LocalDate startDate;

    @Schema(description = "여행 종료일", example = "2026-08-03")
    private LocalDate endDate;

    @Schema(description = "업무 시작 시간", example = "09:00")
    private LocalTime workStartTime;

    @Schema(description = "업무 종료 시간", example = "18:00")
    private LocalTime workEndTime;

    @Schema(description = "업무 집중도", example = "MEDIUM")
    private WorkFocusLevel workFocusLevel;

    @Schema(description = "총 여행 일수", example = "3")
    private Integer totalDays;

    @Schema(description = "일별 코스 아이템 목록 (키: 1일차, 2일차, ...)")
    private Map<Integer, List<CourseItemResponse>> dayItems;

    @Schema(description = "코스 생성 시각")
    private LocalDateTime createdAt;

    public static CourseResponse from(Course course) {
        Map<Integer, List<CourseItemResponse>> dayItems = course.getItems().stream()
                .map(CourseItemResponse::from)
                .collect(Collectors.groupingBy(CourseItemResponse::getDayNumber));

        int totalDays = (int) course.getStartDate().until(course.getEndDate(),
                java.time.temporal.ChronoUnit.DAYS) + 1;

        return CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .region(course.getRegion())
                .startDate(course.getStartDate())
                .endDate(course.getEndDate())
                .workStartTime(course.getWorkStartTime())
                .workEndTime(course.getWorkEndTime())
                .workFocusLevel(course.getWorkFocusLevel())
                .totalDays(totalDays)
                .dayItems(dayItems)
                .createdAt(course.getCreatedAt())
                .build();
    }
}
