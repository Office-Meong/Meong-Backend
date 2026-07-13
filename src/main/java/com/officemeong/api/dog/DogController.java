package com.officemeong.api.dog;

import com.officemeong.common.dto.ApiResponse;
import com.officemeong.domain.dog.dto.DogRequest;
import com.officemeong.domain.dog.dto.DogResponse;
import com.officemeong.domain.dog.service.DogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "반려견", description = "반려견 프로필 등록 및 관리 API (인증 필요)")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/v1/dogs")
@RequiredArgsConstructor
public class DogController {

    private final DogService dogService;

    @Operation(
            summary = "내 반려견 목록 조회",
            description = "로그인한 사용자가 등록한 반려견 전체 목록을 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 없음 또는 만료")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<DogResponse>>> getMyDogs(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(dogService.getMyDogs(userId)));
    }

    @Operation(
            summary = "반려견 등록",
            description = "반려견 프로필을 등록합니다. 이름은 필수이며 최대 50자입니다. 품종, 몸무게, 생년월일, 중성화 여부는 선택 입력입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이름 누락 또는 유효성 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 없음 또는 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<DogResponse>> addDog(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody DogRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(dogService.addDog(userId, request)));
    }

    @Operation(
            summary = "반려견 정보 수정",
            description = "반려견의 이름, 품종, 몸무게, 생년월일, 중성화 여부를 수정합니다. 본인의 반려견만 수정 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이름 누락 또는 유효성 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 없음 또는 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "반려견을 찾을 수 없거나 권한 없음")
    })
    @PutMapping("/{dogId}")
    public ResponseEntity<ApiResponse<DogResponse>> updateDog(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "수정할 반려견 ID", required = true) @PathVariable Long dogId,
            @Valid @RequestBody DogRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(dogService.updateDog(userId, dogId, request)));
    }

    @Operation(
            summary = "반려견 삭제",
            description = "반려견 프로필을 삭제합니다. 본인의 반려견만 삭제 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 없음 또는 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "반려견을 찾을 수 없거나 권한 없음")
    })
    @DeleteMapping("/{dogId}")
    public ResponseEntity<ApiResponse<Void>> deleteDog(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "삭제할 반려견 ID", required = true) @PathVariable Long dogId) {
        dogService.deleteDog(userId, dogId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
