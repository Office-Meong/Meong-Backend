package com.officemeong.domain.favorite.dto;

import com.officemeong.domain.favorite.entity.Favorite;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "즐겨찾기 응답")
@Getter
@Builder
public class FavoriteResponse {

    @Schema(description = "장소 ID", example = "42")
    private Long placeId;

    @Schema(description = "장소명", example = "강릉 반려견 카페 예시")
    private String placeName;

    @Schema(description = "장소 유형", example = "FOOD")
    private PlaceType placeType;

    @Schema(description = "지역", example = "GANGNEUNG")
    private Region region;

    @Schema(description = "주소", example = "강원도 강릉시 ...")
    private String address;

    @Schema(description = "즐겨찾기 등록 시각")
    private LocalDateTime favoritedAt;

    public static FavoriteResponse from(Favorite favorite) {
        return FavoriteResponse.builder()
                .placeId(favorite.getPlace().getId())
                .placeName(favorite.getPlace().getName())
                .placeType(favorite.getPlace().getPlaceType())
                .region(favorite.getPlace().getRegion())
                .address(favorite.getPlace().getAddress())
                .favoritedAt(favorite.getCreatedAt())
                .build();
    }
}
