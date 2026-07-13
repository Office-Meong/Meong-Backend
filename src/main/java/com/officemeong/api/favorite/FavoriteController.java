package com.officemeong.api.favorite;

import com.officemeong.common.dto.ApiResponse;
import com.officemeong.domain.favorite.dto.FavoriteResponse;
import com.officemeong.domain.favorite.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "즐겨찾기", description = "반려동물 동반 장소 즐겨찾기 API (인증 필요)")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(
            summary = "내 즐겨찾기 목록 조회",
            description = "로그인한 사용자가 즐겨찾기한 장소 목록을 최신순으로 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 없음 또는 만료")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getMyFavorites(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(favoriteService.getMyFavorites(userId)));
    }

    @Operation(
            summary = "즐겨찾기 추가",
            description = "특정 장소를 즐겨찾기에 추가합니다. 이미 즐겨찾기한 장소는 409를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "즐겨찾기 추가 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 없음 또는 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 즐겨찾기한 장소")
    })
    @PostMapping("/{placeId}")
    public ResponseEntity<ApiResponse<FavoriteResponse>> addFavorite(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "즐겨찾기할 장소 ID", required = true) @PathVariable Long placeId) {
        return ResponseEntity.ok(ApiResponse.ok(favoriteService.addFavorite(userId, placeId)));
    }

    @Operation(
            summary = "즐겨찾기 취소",
            description = "특정 장소의 즐겨찾기를 취소합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "즐겨찾기 취소 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 없음 또는 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "즐겨찾기를 찾을 수 없음")
    })
    @DeleteMapping("/{placeId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "즐겨찾기 취소할 장소 ID", required = true) @PathVariable Long placeId) {
        favoriteService.removeFavorite(userId, placeId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
