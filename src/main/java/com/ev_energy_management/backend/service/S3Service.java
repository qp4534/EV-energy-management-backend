package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.ImageUploadUrlResponse;
import com.ev_energy_management.backend.dto.NoticeAttachmentUploadUrlResponse;
import com.ev_energy_management.backend.exception.InvalidRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
public class S3Service {

    private static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(5);
    private static final Duration DOWNLOAD_URL_TTL = Duration.ofMinutes(10);

    // 버킷 정책에서 profile-images/*, car-images/*만 공개 읽기를 허용해뒀으므로, 업로드
    // 가능한 형식도 이 화이트리스트로 제한한다 (임의 파일 확장자로 업로드되는 것을 막기 위함).
    private static final Map<String, String> ALLOWED_IMAGE_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    // 공지사항 첨부파일은 이미지 말고 문서류도 필요해서, 이미지용 화이트리스트랑 별도로 관리.
    // 여긴 버킷 정책으로 공개 읽기를 열어둔 경로가 아니라서, 매번 임시 다운로드 링크를 발급하는 방식으로 씀.
    private static final Map<String, String> ALLOWED_NOTICE_ATTACHMENT_TYPES = Map.ofEntries(
            Map.entry("application/pdf", "pdf"),
            Map.entry("application/msword", "doc"),
            Map.entry("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"),
            Map.entry("application/vnd.ms-excel", "xls"),
            Map.entry("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
            Map.entry("image/jpeg", "jpg"),
            Map.entry("image/png", "png")
    );

    private final S3Presigner presigner;
    private final S3Client s3Client;
    private final String bucket;
    private final String region;

    public S3Service(
            S3Presigner presigner,
            S3Client s3Client,
            @Value("${aws.s3.bucket}") String bucket,
            @Value("${aws.region}") String region
    ) {
        this.presigner = presigner;
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.region = region;
    }

    public ImageUploadUrlResponse createProfileImageUploadUrl(UUID userId, String contentType) {
        return createUploadUrl("profile-images/" + userId, contentType);
    }

    public ImageUploadUrlResponse createCarImageUploadUrl(UUID carId, String contentType) {
        return createUploadUrl("car-images/" + carId, contentType);
    }

    private ImageUploadUrlResponse createUploadUrl(String keyPrefix, String contentType) {
        String extension = ALLOWED_IMAGE_TYPES.get(contentType);
        if (extension == null) {
            throw new InvalidRequestException("지원하지 않는 이미지 형식입니다. (jpeg, png, webp만 허용)");
        }

        String objectKey = "%s/%s.%s".formatted(keyPrefix, UUID.randomUUID(), extension);

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
        // profile-images/*, car-images/*는 버킷 정책으로 공개 읽기이므로, presigned GET 없이
        // 이 정적 URL로 바로 접근 가능.
        String imageUrl = "https://%s.s3.%s.amazonaws.com/%s".formatted(bucket, region, objectKey);

        return new ImageUploadUrlResponse(uploadUrl, imageUrl);
    }

    // 공지 첨부파일 업로드용 임시 링크 발급. notice-attachments/ 경로는 버킷 정책상
    // 공개 읽기가 아니라서, 응답엔 최종 공개 URL이 아니라 objectKey만 돌려줌.
    public NoticeAttachmentUploadUrlResponse createNoticeAttachmentUploadUrl(String fileName, String contentType) {
        String extension = ALLOWED_NOTICE_ATTACHMENT_TYPES.get(contentType);
        if (extension == null) {
            throw new InvalidRequestException("지원하지 않는 첨부파일 형식입니다. (pdf, doc(x), xls(x), jpg, png만 허용)");
        }

        String objectKey = "notice-attachments/%s.%s".formatted(UUID.randomUUID(), extension);

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
        return new NoticeAttachmentUploadUrlResponse(uploadUrl, objectKey);
    }

    // objectKey로 임시 다운로드 링크 발급 (공지 상세 조회할 때마다 새로 발급해서 내려줌)
    public String createNoticeAttachmentDownloadUrl(String objectKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(DOWNLOAD_URL_TTL)
                .getObjectRequest(getObjectRequest)
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }

    // S3에서 실제 파일 삭제. 백엔드가 갖고 있는 자격증명에 s3:DeleteObject 권한이
    // 없으면 여기서 AccessDenied 예외가 남 -> 그땐 AWS 쪽 권한(IAM 정책) 추가 요청 필요.
    public void deleteObject(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return;
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build());
    }
}