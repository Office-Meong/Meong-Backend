package com.officemeong.infrastructure.s3;

import com.officemeong.api.upload.dto.UploadPresignedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3UploadService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final Duration PRESIGN_DURATION = Duration.ofMinutes(10);

    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    public UploadPresignedResponse generatePresignedUrl(Long userId, String filename, String contentType) {
        if (!isConfigured(s3Properties.getBucket()) || !isConfigured(s3Properties.getAccessKey())
                || !isConfigured(s3Properties.getSecretKey())) {
            throw new IllegalStateException("이미지 업로드 기능이 아직 설정되지 않았습니다. (S3_BUCKET_NAME/AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY)");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. (jpeg/png/webp/gif)");
        }

        String ext = extractExtension(filename);
        String key = String.format("dogs/%d/%s.%s", userId, UUID.randomUUID(), ext);

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putRequest)
                .signatureDuration(PRESIGN_DURATION)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);

        String imageUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                s3Properties.getBucket(), s3Properties.getRegion(), key);

        return UploadPresignedResponse.builder()
                .presignedUrl(presigned.url().toString())
                .imageUrl(imageUrl)
                .build();
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    // 환경변수 미설정 시 application.yml의 ${...} 플레이스홀더가 해석되지 않은 채 문자 그대로 들어오므로 함께 체크
    private boolean isConfigured(String value) {
        return value != null && !value.isBlank() && !value.startsWith("${");
    }
}
