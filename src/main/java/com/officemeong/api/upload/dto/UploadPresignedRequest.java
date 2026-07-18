package com.officemeong.api.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "이미지 업로드 Presigned URL 요청")
@Getter
@NoArgsConstructor
public class UploadPresignedRequest {

    @Schema(description = "업로드할 파일명 (확장자 포함)", example = "dog.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String filename;

    @Schema(description = "MIME 타입 (image/jpeg, image/png, image/webp, image/gif)", example = "image/jpeg", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String contentType;
}
