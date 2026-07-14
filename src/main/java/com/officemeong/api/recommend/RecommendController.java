package com.officemeong.api.recommend;

import com.officemeong.ai.dto.PlaceRecommendResponse;
import com.officemeong.ai.service.RecommendService;
import com.officemeong.common.dto.ApiResponse;
import com.officemeong.domain.place.enums.Region;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "AI 추천", description = "Claude AI 기반 반려동물 동반 장소 추천 API (인증 필요)")
@RestController
@RequestMapping("/api/v1/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @Operation(
            summary = "AI 장소 추천",
            description = "반려견 정보·즐겨찾기·리뷰 이력을 바탕으로 Claude AI가 방문 장소 3곳을 한국어 추천 이유와 함께 반환합니다. " +
                          "API 키 미설정 또는 AI 오류 시 점수 기반 상위 3곳으로 폴백됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "추천 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수 파라미터(region) 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "반려견을 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlaceRecommendResponse>>> recommend(
            @Parameter(description = "여행 지역 (예: GANGNEUNG, CHUNCHEON, WONJU)", required = true)
            @RequestParam Region region,

            @Parameter(description = "반려견 ID (선택 — 지정 시 반려견 맞춤 추천)")
            @RequestParam(required = false) Long dogId,

            @AuthenticationPrincipal Long userId
    ) {
        List<PlaceRecommendResponse> result = recommendService.recommend(region, dogId, userId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
