package com.officemeong.domain.course.dto;

import com.officemeong.domain.course.entity.Course;
import com.officemeong.domain.course.entity.CourseItem;
import com.officemeong.domain.course.enums.WorkFocusLevel;
import com.officemeong.domain.place.entity.Place;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

    @Schema(description = "일차별 마지막 일정 → 숙소 복귀 거리(km). 숙소가 없는 1박 미만 코스는 빈 맵. 키: 일차(1,2,...)")
    private Map<Integer, BigDecimal> dayReturnToAccommKm;

    @Schema(description = "코스 생성 시각")
    private LocalDateTime createdAt;

    public static CourseResponse from(Course course) {
        Map<Integer, List<CourseItemResponse>> dayItems = course.getItems().stream()
                .map(CourseItemResponse::from)
                .collect(Collectors.groupingBy(CourseItemResponse::getDayNumber));

        int totalDays = (int) course.getStartDate().until(course.getEndDate(),
                java.time.temporal.ChronoUnit.DAYS) + 1;

        Map<Integer, BigDecimal> returnDistances = calcReturnDistances(course);

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
                .dayReturnToAccommKm(returnDistances)
                .createdAt(course.getCreatedAt())
                .build();
    }

    private static Map<Integer, BigDecimal> calcReturnDistances(Course course) {
        // 코스 내 숙소 아이템 찾기 (Day 1의 STAY 슬롯)
        Place stayPlace = course.getItems().stream()
                .filter(i -> i.getPlace().getPlaceType() == PlaceType.STAY)
                .map(CourseItem::getPlace)
                .findFirst()
                .orElse(null);

        if (stayPlace == null || stayPlace.getLatitude() == null || stayPlace.getLongitude() == null) {
            return Map.of();
        }

        Map<Integer, List<CourseItem>> byDay = course.getItems().stream()
                .collect(Collectors.groupingBy(CourseItem::getDayNumber));

        Map<Integer, BigDecimal> result = new TreeMap<>();
        byDay.forEach((day, items) -> {
            // 해당 날의 마지막 아이템 (STAY 제외 — Day 1의 체크인 슬롯이 first이므로 last는 항상 활동)
            items.stream()
                    .filter(i -> i.getPlace().getPlaceType() != PlaceType.STAY)
                    .max(Comparator.comparingInt(CourseItem::getVisitOrder))
                    .ifPresent(last -> {
                        Place lastPlace = last.getPlace();
                        if (lastPlace.getLatitude() == null || lastPlace.getLongitude() == null) return;
                        double km = haversineKm(
                                lastPlace.getLatitude().doubleValue(), lastPlace.getLongitude().doubleValue(),
                                stayPlace.getLatitude().doubleValue(), stayPlace.getLongitude().doubleValue());
                        result.put(day, BigDecimal.valueOf(km).setScale(2, RoundingMode.HALF_UP));
                    });
        });
        return result;
    }

    private static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
