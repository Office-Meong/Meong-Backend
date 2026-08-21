package com.officemeong.api.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "이미지 업로드 Presigned URL 응답")
@Getter
@Builder
public class UploadPresignedResponse {

    @Schema(description = "S3 Presigned PUT URL (10분간 유효). HTTP PUT 메서드로 업로드하세요.",
            example = "https://bucket.s3.ap-northeast-2.amazonaws.com/dogs/1/uuid.jpg?X-Amz-Signature=...")
    private String presignedUrl;

    @Schema(description = "업로드 완료 후 저장할 이미지 공개 URL (Dog.imageUrl에 저장)",
            example = "https://bucket.s3.ap-northeast-2.amazonaws.com/dogs/1/uuid.jpg")
    private String imageUrl;

    @Schema(description = "presignedUrl 호출 시 사용할 HTTP 메서드 (반드시 PUT)", example = "PUT")
    private String method;

    @Schema(description = "presignedUrl 호출 시 반드시 포함해야 할 Content-Type 헤더 값. 서명에 포함되어 있어 값이 다르면 403 발생.",
            example = "image/jpeg")
    private String contentType;
}
