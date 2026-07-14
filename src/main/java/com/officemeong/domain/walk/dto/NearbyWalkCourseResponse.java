package com.officemeong.domain.walk.dto;

import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.walk.entity.WalkCourse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Builder
@Schema(description = "장소 주변 산책 코스 응답")
public class NearbyWalkCourseResponse {

    @Schema(description = "산책 코스 ID")
    private Long id;

    @Schema(description = "코스명")
    private String courseName;

    @Schema(description = "지역")
    private Region region;

    @Schema(description = "코스 총 거리(km)")
    private BigDecimal distanceKm;

    @Schema(description = "코스 시작 위도")
    private BigDecimal startLatitude;

    @Schema(description = "코스 시작 경도")
    private BigDecimal startLongitude;

    @Schema(description = "장소에서 코스 시작점까지 직선 거리(km)")
    private double distanceFromPlaceKm;

    public static NearbyWalkCourseResponse from(WalkCourse course, double distanceFromPlaceKm) {
        return NearbyWalkCourseResponse.builder()
                .id(course.getId())
                .courseName(course.getCourseName())
                .region(course.getRegion())
                .distanceKm(course.getDistanceKm())
                .startLatitude(course.getStartLatitude())
                .startLongitude(course.getStartLongitude())
                .distanceFromPlaceKm(BigDecimal.valueOf(distanceFromPlaceKm)
                        .setScale(2, RoundingMode.HALF_UP).doubleValue())
                .build();
    }
}
