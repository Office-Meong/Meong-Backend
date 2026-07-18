package com.officemeong.api.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "이미지 업로드 Presigned URL 응답")
@Getter
@Builder
public class UploadPresignedResponse {

    @Schema(description = "S3 Presigned PUT URL (10분간 유효). 이 URL로 파일을 직접 PUT 업로드하세요.",
            example = "https://bucket.s3.ap-northeast-2.amazonaws.com/dogs/1/uuid.jpg?X-Amz-Signature=...")
    private String presignedUrl;

    @Schema(description = "업로드 완료 후 저장할 이미지 공개 URL (Dog.imageUrl에 저장)",
            example = "https://bucket.s3.ap-northeast-2.amazonaws.com/dogs/1/uuid.jpg")
    private String imageUrl;
}
