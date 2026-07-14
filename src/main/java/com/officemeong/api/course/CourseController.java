package com.officemeong.api.course;

import com.officemeong.common.dto.ApiResponse;
import com.officemeong.domain.walk.dto.CourseRecommendResponse;
import com.officemeong.domain.walk.service.CourseService;
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

@Tag(name = "코스 추천", description = "반려견 동반 산책 코스 추천 API")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(
            summary = "코스 추천 목록 조회",
            description = "사용자 위치와 반려견 정보를 기반으로 적합한 산책 코스를 추천합니다.\n\n" +
                    "- 반경 30km 이내 코스를 가까운 순으로 최대 10개 반환\n" +
                    "- `dogId` 지정 시 반려견 체중에 맞는 코스 거리로 필터링 (소형견 ≤ 3km, 중형견 ≤ 6km, 대형견 제한 없음)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "추천 코스 목록 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "반려견을 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseRecommendResponse>>> recommend(
            @Parameter(description = "현재 위치 위도", example = "37.7960", required = true)
            @RequestParam Double lat,
            @Parameter(description = "현재 위치 경도", example = "128.9000", required = true)
            @RequestParam Double lng,
            @Parameter(description = "반려견 ID (선택). 지정 시 반려견 체중에 맞는 코스만 반환", example = "1")
            @RequestParam(required = false) Long dogId,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.recommend(lat, lng, userId, dogId)));
    }
}
