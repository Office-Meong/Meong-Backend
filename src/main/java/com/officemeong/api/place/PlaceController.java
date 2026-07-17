package com.officemeong.api.place;

import com.officemeong.common.dto.ApiResponse;
import com.officemeong.common.dto.PageResponse;
import com.officemeong.domain.place.dto.PlaceDetailResponse;
import com.officemeong.domain.place.dto.PlaceSummaryResponse;
import com.officemeong.domain.place.enums.CongestionLevel;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.place.service.PlaceService;
import com.officemeong.domain.walk.dto.NearbyWalkCourseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "장소", description = "반려동물 동반 가능 장소 조회 API (인증 불필요)")
@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @Operation(
            summary = "장소 목록 조회",
            description = "반려동물 동반 가능 장소를 필터/정렬 조건으로 조회합니다. 인증 없이 사용 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 파라미터 (region, type, sort 값 오류)")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PlaceSummaryResponse>>> getPlaces(
            @Parameter(description = "지역 필터 (예: GANGNEUNG, SOKCHO, YANGYANG 등). 미입력 시 전체 지역")
            @RequestParam(required = false) Region region,

            @Parameter(description = "장소 유형 필터 (예: STAY, RESTAURANT, CAFE, ACTIVITY 등). 미입력 시 전체 유형")
            @RequestParam(required = false) PlaceType type,

            @Parameter(description = "정렬 기준 (SCORE: 평점순, DISTANCE: 거리순). 기본값: SCORE")
            @RequestParam(defaultValue = "SCORE") PlaceService.SortType sort,

            @Parameter(description = "혼잡도 필터 (RELAXED: 여유, NORMAL: 보통, CROWDED: 혼잡, VERY_CROWDED: 매우혼잡). 미입력 시 전체")
            @RequestParam(required = false) CongestionLevel congestion,

            @Parameter(description = "페이지 번호 (0부터 시작). 기본값: 0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기. 기본값: 20")
            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal Long userId
    ) {
        PageResponse<PlaceSummaryResponse> result = placeService.getPlaces(region, type, sort, congestion, page, size, userId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(
            summary = "장소 상세 조회",
            description = "장소 ID로 상세 정보를 조회합니다. 반려동물 동반 조건, 운영 시간, 접근성 정보 포함. 인증 없이 사용 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlaceDetailResponse>> getPlace(
            @Parameter(description = "장소 ID", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        PlaceDetailResponse result = placeService.getPlace(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(
            summary = "장소 주변 산책 코스 조회",
            description = "장소 ID로 동일 지역 내 10km 이내 산책 코스를 거리 오름차순으로 최대 10개 조회합니다. 인증 없이 사용 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음")
    })
    @GetMapping("/{id}/walk-courses")
    public ResponseEntity<ApiResponse<List<NearbyWalkCourseResponse>>> getNearbyWalkCourses(
            @Parameter(description = "장소 ID", required = true)
            @PathVariable Long id) {
        List<NearbyWalkCourseResponse> result = placeService.getNearbyWalkCourses(id);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
