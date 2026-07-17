package com.officemeong.api.course;

import com.officemeong.common.dto.ApiResponse;
import com.officemeong.domain.course.dto.*;
import com.officemeong.domain.course.service.CourseService;
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

@Tag(name = "워케이션 코스", description = "반려견 동반 워케이션 코스 생성 및 관리 API")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "코스 생성",
            description = "지역, 여행 기간, 업무 시간, 업무 집중도를 입력하면 n일차 워케이션 일정을 자동 생성합니다.\n\n" +
                    "**업무 집중도별 일정 구성:**\n" +
                    "- **LOW**: 오전 반업무 + 오후 관광 중심\n" +
                    "- **MEDIUM**: 오전/오후 업무 균형 + 점심 산책\n" +
                    "- **HIGH**: 하루 집중 업무 + 저녁 짧은 산책")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "코스 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "반려견을 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @Valid @RequestBody CourseCreateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.createCourse(userId, request)));
    }

    @Operation(summary = "내 코스 목록 조회", description = "생성한 워케이션 코스 목록을 최신순으로 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "목록 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseSummaryResponse>>> getMyCourses(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.getMyCourses(userId)));
    }

    @Operation(summary = "코스 상세 조회", description = "특정 코스의 일별 상세 일정을 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "코스 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "코스를 찾을 수 없음")
    })
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourse(
            @Parameter(description = "코스 ID", example = "1") @PathVariable Long courseId,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.getCourse(userId, courseId)));
    }

    @Operation(summary = "코스 아이템 수정",
            description = "특정 코스 아이템의 방문 시간 또는 장소를 변경합니다. null 필드는 변경하지 않습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정된 코스 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "코스 또는 장소를 찾을 수 없음")
    })
    @PutMapping("/{courseId}/items/{itemId}")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourseItem(
            @Parameter(description = "코스 ID", example = "1") @PathVariable Long courseId,
            @Parameter(description = "아이템 ID", example = "3") @PathVariable Long itemId,
            @RequestBody CourseItemUpdateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                courseService.updateCourseItem(userId, courseId, itemId, request)));
    }

    @Operation(summary = "코스 이름 수정", description = "코스의 이름을 변경합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이름 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이름 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "코스를 찾을 수 없음")
    })
    @PatchMapping("/{courseId}/name")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourseName(
            @Parameter(description = "코스 ID", example = "1") @PathVariable Long courseId,
            @Valid @RequestBody CourseNameUpdateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                courseService.updateCourseName(userId, courseId, request.getName())));
    }

    @Operation(summary = "코스 삭제", description = "내가 생성한 코스를 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "코스를 찾을 수 없음")
    })
    @DeleteMapping("/{courseId}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            @Parameter(description = "코스 ID", example = "1") @PathVariable Long courseId,
            @AuthenticationPrincipal Long userId
    ) {
        courseService.deleteCourse(userId, courseId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "대체 장소 추천",
            description = "코스 아이템의 장소가 마음에 들지 않을 때, 같은 유형의 대체 장소를 최대 5개 추천합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "대체 장소 목록 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "코스 또는 아이템을 찾을 수 없음")
    })
    @GetMapping("/{courseId}/items/{itemId}/alternatives")
    public ResponseEntity<ApiResponse<List<AlternativePlaceResponse>>> getAlternatives(
            @Parameter(description = "코스 ID", example = "1") @PathVariable Long courseId,
            @Parameter(description = "아이템 ID", example = "3") @PathVariable Long itemId,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                courseService.getAlternatives(userId, courseId, itemId)));
    }
}
