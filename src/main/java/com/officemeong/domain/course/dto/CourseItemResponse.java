package com.officemeong.domain.course.dto;

import com.officemeong.domain.course.entity.CourseItem;
import com.officemeong.domain.place.enums.PlaceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Schema(description = "코스 아이템 응답")
@Getter
@Builder
public class CourseItemResponse {

    @Schema(description = "아이템 ID", example = "1")
    private Long id;

    @Schema(description = "n일차", example = "1")
    private Integer dayNumber;

    @Schema(description = "하루 내 방문 순서", example = "2")
    private Integer visitOrder;

    @Schema(description = "슬롯 레이블 (아침/오전 업무/점심 등)", example = "오전 업무")
    private String slotLabel;

    @Schema(description = "장소 ID", example = "42")
    private Long placeId;

    @Schema(description = "장소명", example = "강릉 펫 코워킹")
    private String placeName;

    @Schema(description = "장소 유형", example = "WORK_PLACE")
    private PlaceType placeType;

    @Schema(description = "장소 주소", example = "강원도 강릉시 ...")
    private String address;

    @Schema(description = "위도", example = "37.7960")
    private BigDecimal latitude;

    @Schema(description = "경도", example = "128.9000")
    private BigDecimal longitude;

    @Schema(description = "방문 시작 시간", example = "09:00")
    private LocalTime startTime;

    @Schema(description = "방문 종료 시간", example = "12:00")
    private LocalTime endTime;

    @Schema(description = "이전 장소로부터의 거리 (km)", example = "0.85")
    private BigDecimal distanceFromPrevKm;

    public static CourseItemResponse from(CourseItem item) {
        return CourseItemResponse.builder()
                .id(item.getId())
                .dayNumber(item.getDayNumber())
                .visitOrder(item.getVisitOrder())
                .slotLabel(item.getSlotLabel())
                .placeId(item.getPlace().getId())
                .placeName(item.getPlace().getName())
                .placeType(item.getPlace().getPlaceType())
                .address(item.getPlace().getAddress())
                .latitude(item.getPlace().getLatitude())
                .longitude(item.getPlace().getLongitude())
                .startTime(item.getStartTime())
                .endTime(item.getEndTime())
                .distanceFromPrevKm(item.getDistanceFromPrevKm())
                .build();
    }
}
