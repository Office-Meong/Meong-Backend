package com.officemeong.api.upload;

import com.officemeong.api.upload.dto.UploadPresignedRequest;
import com.officemeong.api.upload.dto.UploadPresignedResponse;
import com.officemeong.common.dto.ApiResponse;
import com.officemeong.infrastructure.s3.S3UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "파일 업로드", description = "S3 이미지 업로드 Presigned URL 발급 API")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {

    private final S3UploadService s3UploadService;

    @Operation(
            summary = "이미지 업로드 Presigned URL 발급",
            description = """
                    S3에 직접 업로드할 수 있는 Presigned PUT URL을 발급합니다. (10분 유효)

                    **업로드 흐름:**
                    1. 이 API로 `presignedUrl`과 `imageUrl`을 받습니다.
                    2. `presignedUrl`로 HTTP PUT 요청을 보내 파일을 업로드합니다.
                       - `Content-Type` 헤더를 요청한 contentType과 동일하게 설정해야 합니다.
                    3. 업로드 성공 후 `imageUrl`을 반려견 등록/수정 API의 `imageUrl` 필드에 저장합니다.

                    **지원 형식:** image/jpeg, image/png, image/webp, image/gif
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Presigned URL 발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "지원하지 않는 파일 형식"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping("/presigned-url")
    public ResponseEntity<ApiResponse<UploadPresignedResponse>> generatePresignedUrl(
            @Valid @RequestBody UploadPresignedRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        UploadPresignedResponse response = s3UploadService.generatePresignedUrl(
                userId, request.getFilename(), request.getContentType());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
