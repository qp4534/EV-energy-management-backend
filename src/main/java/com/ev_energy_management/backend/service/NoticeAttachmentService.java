package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.NoticeAttachmentDto;
import com.ev_energy_management.backend.dto.NoticeAttachmentUploadUrlResponse;
import com.ev_energy_management.backend.entity.NoticeAttachmentEntity;
import com.ev_energy_management.backend.repository.NoticeAttachmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NoticeAttachmentService {

    private final NoticeAttachmentRepository noticeAttachmentRepository;
    private final S3Service s3Service;

    public NoticeAttachmentService(NoticeAttachmentRepository noticeAttachmentRepository, S3Service s3Service) {
        this.noticeAttachmentRepository = noticeAttachmentRepository;
        this.s3Service = s3Service;
    }

    // 업로드 1단계: 프론트가 파일 선택하면, 이 URL부터 받아서 S3에 직접 업로드
    public NoticeAttachmentUploadUrlResponse createUploadUrl(String fileName, String contentType) {
        return s3Service.createNoticeAttachmentUploadUrl(fileName, contentType);
    }

    public List<NoticeAttachmentDto> findAll() {
        return noticeAttachmentRepository.findAll().stream().map(this::toDto).toList();
    }

    public NoticeAttachmentDto findById(UUID attachmentId) {
        return toDto(noticeAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Notice attachment not found: " + attachmentId)));
    }

    // 업로드 2단계: S3 업로드 성공 후, 실제 파일 기록을 DB에 저장
    // (request.fileUrl()엔 S3 objectKey가 들어옴 - 공개 URL 아님)
    public NoticeAttachmentDto create(NoticeAttachmentDto request) {
        NoticeAttachmentEntity entity = NoticeAttachmentEntity.builder()
                .fileName(request.fileName())
                .fileUrl(request.fileUrl())
                .fileSize(request.fileSize())
                .fileType(request.fileType())
                .noticeId(request.noticeId())
                .build();
        return toDto(noticeAttachmentRepository.save(entity));
    }

    public NoticeAttachmentDto update(UUID attachmentId, NoticeAttachmentDto request) {
        NoticeAttachmentEntity entity = noticeAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Notice attachment not found: " + attachmentId));
        entity.setFileName(request.fileName());
        entity.setFileUrl(request.fileUrl());
        entity.setFileSize(request.fileSize());
        entity.setFileType(request.fileType());
        entity.setNoticeId(request.noticeId());
        return toDto(noticeAttachmentRepository.save(entity));
    }

    // DB 기록 삭제 전에 S3 파일부터 먼저 지움 (순서 반대로 하면, S3 삭제 실패 시
    // DB엔 없는데 S3엔 파일이 남는 것보다, DB엔 있는데 S3만 지워진 상태가 덜 위험함)
    // DB 기록 삭제 전에 S3 파일부터 먼저 지움 (순서 반대로 하면, S3 삭제 실패 시
    // DB엔 없는데 S3엔 파일이 남는 것보다, DB엔 있는데 S3만 지워진 상태가 덜 위험함)
    public void delete(UUID attachmentId) {
        NoticeAttachmentEntity entity = noticeAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Notice attachment not found: " + attachmentId));
        s3Service.deleteObject(entity.getFileUrl());
        noticeAttachmentRepository.deleteById(attachmentId);
    }

    // 공지 자체가 삭제될 때, 거기 딸린 첨부파일 전부(S3 + DB) 같이 정리.
    // NoticeService.delete()에서 공지 지우기 직전에 호출함.
    public void deleteByNoticeId(UUID noticeId) {
        List<NoticeAttachmentEntity> attachments = noticeAttachmentRepository.findByNoticeId(noticeId);
        attachments.forEach(a -> s3Service.deleteObject(a.getFileUrl()));
        noticeAttachmentRepository.deleteAll(attachments);
    }

    // 조회할 땐 DB에 저장된 objectKey를, 그 순간 유효한 임시 다운로드 링크로 바꿔서 내려줌
    // (objectKey 자체는 공개 URL이 아니라서 그대로 주면 다운로드 불가능)
    private NoticeAttachmentDto toDto(NoticeAttachmentEntity entity) {
        String downloadUrl = entity.getFileUrl() == null
                ? null
                : s3Service.createNoticeAttachmentDownloadUrl(entity.getFileUrl());
        return new NoticeAttachmentDto(
                entity.getAttachmentId(),
                entity.getFileName(),
                downloadUrl,
                entity.getFileSize(),
                entity.getFileType(),
                entity.getNoticeId()
        );
    }
}