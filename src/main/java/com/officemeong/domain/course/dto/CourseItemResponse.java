package com.officemeong.domain.course.dto;

import com.officemeong.domain.course.entity.CourseItem;
import com.officemeong.domain.place.entity.PlaceImage;
import com.officemeong.domain.place.enums.LodgingType;
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

    @Schema(description = "썸네일 이미지 URL (없으면 null)", example = "http://example.com/thumb.jpg")
    private String thumbnailUrl;

    @Schema(description = "숙소 유형 (placeType이 STAY인 아이템만 해당, 그 외 null). " +
            "PENSION=펜션, GUESTHOUSE=민박/게스트하우스/한옥, CAMPING=캠핑장, GLAMPING=글램핑장, HOTEL=호텔/모텔/리조트, CARAVAN=카라반. " +
            "STAY이지만 원본 데이터에서 유형을 판별하지 못한 경우도 null",
            example = "PENSION")
    private LodgingType lodgingType;

    public static CourseItemResponse from(CourseItem item) {
        var place = item.getPlace();
        String thumbnail = place.getImages().stream()
                .filter(PlaceImage::isThumbnail)
                .map(PlaceImage::getImageUrl)
                .findFirst()
                .orElseGet(() -> place.getImages().isEmpty() ? null
                        : place.getImages().get(0).getImageUrl());

        return CourseItemResponse.builder()
                .id(item.getId())
                .dayNumber(item.getDayNumber())
                .visitOrder(item.getVisitOrder())
                .slotLabel(item.getSlotLabel())
                .placeId(place.getId())
                .placeName(place.getName())
                .placeType(place.getPlaceType())
                .address(place.getAddress())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .startTime(item.getStartTime())
                .endTime(item.getEndTime())
                .distanceFromPrevKm(item.getDistanceFromPrevKm())
                .thumbnailUrl(thumbnail)
                .lodgingType(place.getOperation() != null ? place.getOperation().getLodgingType() : null)
                .build();
    }
}
