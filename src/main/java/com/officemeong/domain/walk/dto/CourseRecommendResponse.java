package com.officemeong.domain.walk.dto;

import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.walk.entity.WalkCourse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Schema(description = "코스 추천 응답")
@Getter
@Builder
public class CourseRecommendResponse {

    @Schema(description = "코스 ID", example = "1")
    private Long id;

    @Schema(description = "코스명", example = "강릉 경포 해변 산책로")
    private String courseName;

    @Schema(description = "지역", example = "GANGNEUNG")
    private Region region;

    @Schema(description = "코스 거리 (km)", example = "2.50")
    private BigDecimal distanceKm;

    @Schema(description = "코스 시작 위도", example = "37.7960")
    private BigDecimal startLatitude;

    @Schema(description = "코스 시작 경도", example = "128.9000")
    private BigDecimal startLongitude;

    @Schema(description = "사용자 위치로부터의 거리 (km)", example = "1.23")
    private Double distanceFromUserKm;

    public static CourseRecommendResponse from(WalkCourse course, double distanceFromUserKm) {
        return CourseRecommendResponse.builder()
                .id(course.getId())
                .courseName(course.getCourseName())
                .region(course.getRegion())
                .distanceKm(course.getDistanceKm())
                .startLatitude(course.getStartLatitude())
                .startLongitude(course.getStartLongitude())
                .distanceFromUserKm(BigDecimal.valueOf(distanceFromUserKm)
                        .setScale(2, RoundingMode.HALF_UP).doubleValue())
                .build();
    }
}
