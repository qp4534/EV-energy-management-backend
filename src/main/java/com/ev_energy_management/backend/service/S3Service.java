package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.auth.ProfileImageUploadUrlResponse;
import com.ev_energy_management.backend.exception.InvalidRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
public class S3Service {

    private static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(5);

    // 버킷 정책에서 profile-images/*만 공개 읽기를 허용해뒀으므로, 업로드 가능한 형식도
    // 이 화이트리스트로 제한한다 (임의 파일 확장자로 업로드되는 것을 막기 위함).
    private static final Map<String, String> ALLOWED_PROFILE_IMAGE_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final S3Presigner presigner;
    private final String bucket;
    private final String region;

    public S3Service(
            S3Presigner presigner,
            @Value("${aws.s3.bucket}") String bucket,
            @Value("${aws.region}") String region
    ) {
        this.presigner = presigner;
        this.bucket = bucket;
        this.region = region;
    }

    public ProfileImageUploadUrlResponse createProfileImageUploadUrl(UUID userId, String contentType) {
        String extension = ALLOWED_PROFILE_IMAGE_TYPES.get(contentType);
        if (extension == null) {
            throw new InvalidRequestException("지원하지 않는 이미지 형식입니다. (jpeg, png, webp만 허용)");
        }

        String objectKey = "profile-images/%s/%s.%s".formatted(userId, UUID.randomUUID(), extension);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(UPLOAD_URL_TTL)
                .putObjectRequest(putObjectRequest)
                .build();

        String uploadUrl = presigner.presignPutObject(presignRequest).url().toString();
        // profile-images/*는 버킷 정책으로 공개 읽기이므로, presigned GET 없이 이 정적 URL로 바로 접근 가능.
        String imageUrl = "https://%s.s3.%s.amazonaws.com/%s".formatted(bucket, region, objectKey);

        return new ProfileImageUploadUrlResponse(uploadUrl, imageUrl);
    }
}
