package com.officemeong.domain.course.dto;

import com.officemeong.domain.course.entity.Course;
import com.officemeong.domain.course.entity.CourseItem;
import com.officemeong.domain.place.entity.PlaceScore;
import com.officemeong.domain.place.enums.Grade;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Schema(description = "워케이션 코스 요약 응답 (목록 조회용)")
@Getter
@Builder
public class CourseSummaryResponse {

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

    @Schema(description = "총 여행 일수", example = "3")
    private int totalDays;

    @Schema(description = "코스 내 장소들의 평균 펫-워크 등급", example = "B")
    private Grade averageGrade;

    @Schema(description = "업무장소 수", example = "3")
    private long workPlaceCount;

    @Schema(description = "음식점 수", example = "4")
    private long foodCount;

    @Schema(description = "관광·산책 장소 수 (TOUR + WALK)", example = "2")
    private long tourWalkCount;

    @Schema(description = "기타 장소 수 (숙소, 병원 등)", example = "1")
    private long otherCount;

    @Schema(description = "총 장소 수", example = "10")
    private long totalPlaceCount;

    public static CourseSummaryResponse from(Course course) {
        List<CourseItem> items = course.getItems();

        int totalDays = (int) course.getStartDate().until(course.getEndDate(),
                java.time.temporal.ChronoUnit.DAYS) + 1;

        OptionalDouble avgScore = items.stream()
                .map(i -> i.getPlace().getScore())
                .filter(s -> s != null)
                .mapToInt(PlaceScore::getTotalScore)
                .average();
        Grade averageGrade = avgScore.isPresent() ? Grade.from((int) Math.round(avgScore.getAsDouble())) : null;

        Map<PlaceType, Long> countByType = items.stream()
                .collect(Collectors.groupingBy(i -> i.getPlace().getPlaceType(), Collectors.counting()));

        long workPlaceCount = countByType.getOrDefault(PlaceType.WORK_PLACE, 0L);
        long foodCount = countByType.getOrDefault(PlaceType.FOOD, 0L);
        long tourWalkCount = countByType.getOrDefault(PlaceType.TOUR, 0L)
                + countByType.getOrDefault(PlaceType.WALK, 0L);
        long otherCount = countByType.getOrDefault(PlaceType.STAY, 0L)
                + countByType.getOrDefault(PlaceType.HOSPITAL, 0L);

        return CourseSummaryResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .region(course.getRegion())
                .startDate(course.getStartDate())
                .endDate(course.getEndDate())
                .totalDays(totalDays)
                .averageGrade(averageGrade)
                .workPlaceCount(workPlaceCount)
                .foodCount(foodCount)
                .tourWalkCount(tourWalkCount)
                .otherCount(otherCount)
                .totalPlaceCount(items.size())
                .build();
    }
}
