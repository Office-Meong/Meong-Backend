package com.officemeong.api.walk;

import com.officemeong.common.dto.ApiResponse;
import com.officemeong.domain.walk.dto.CourseRecommendResponse;
import com.officemeong.domain.walk.service.WalkCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "산책 코스", description = "위치 기반 산책 코스 추천 API (두루누비 코스 데이터)")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/v1/walk-courses")
@RequiredArgsConstructor
public class WalkCourseController {

    private final WalkCourseService walkCourseService;

    @Operation(
            summary = "주변 산책 코스 추천",
            description = "사용자 현재 위치 기반으로 두루누비 산책 코스를 추천합니다.\n\n" +
                    "- 반경 30km 이내 코스를 가까운 순으로 최대 10개 반환\n" +
                    "- `dogId` 지정 시 반려견 체중에 맞는 코스 거리 필터링 (소형견 ≤ 3km, 중형견 ≤ 6km, 대형견 제한 없음)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "추천 코스 목록 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수 파라미터 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "반려견을 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseRecommendResponse>>> recommend(
            @Parameter(description = "현재 위치 위도", example = "37.7960", required = true)
            @RequestParam Double lat,
            @Parameter(description = "현재 위치 경도", example = "128.9000", required = true)
            @RequestParam Double lng,
            @Parameter(description = "반려견 ID (선택). 지정 시 체중에 맞는 코스만 반환", example = "1")
            @RequestParam(required = false) Long dogId,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(walkCourseService.recommend(lat, lng, userId, dogId)));
    }
}
