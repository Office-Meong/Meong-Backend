package com.officemeong.api.place;

import com.officemeong.common.dto.ApiResponse;
import com.officemeong.common.dto.PageResponse;
import com.officemeong.domain.place.dto.PlaceDetailResponse;
import com.officemeong.domain.place.dto.PlaceSummaryResponse;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.place.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    /**
     * 장소 목록 조회
     * GET /api/v1/places?region=GANGNEUNG&type=STAY&sort=SCORE&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PlaceSummaryResponse>>> getPlaces(
            @RequestParam(required = false) Region region,
            @RequestParam(required = false) PlaceType type,
            @RequestParam(defaultValue = "SCORE") PlaceService.SortType sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<PlaceSummaryResponse> result = placeService.getPlaces(region, type, sort, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * 장소 상세 조회
     * GET /api/v1/places/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlaceDetailResponse>> getPlace(@PathVariable Long id) {
        PlaceDetailResponse result = placeService.getPlace(id);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
