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
}
