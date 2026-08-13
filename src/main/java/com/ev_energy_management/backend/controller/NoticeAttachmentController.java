package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.NoticeAttachmentDto;
import com.ev_energy_management.backend.dto.NoticeAttachmentUploadUrlResponse;
import com.ev_energy_management.backend.service.NoticeAttachmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notice-attachments")
public class NoticeAttachmentController {

    private final NoticeAttachmentService noticeAttachmentService;

    public NoticeAttachmentController(NoticeAttachmentService noticeAttachmentService) {
        this.noticeAttachmentService = noticeAttachmentService;
    }

    // 파일 선택 시 제일 먼저 호출 - S3에 직접 업로드할 임시 링크 발급
    @GetMapping("/upload-url")
    public NoticeAttachmentUploadUrlResponse getUploadUrl(
            @RequestParam String fileName,
            @RequestParam String contentType
    ) {
        return noticeAttachmentService.createUploadUrl(fileName, contentType);
    }

    @GetMapping
    public List<NoticeAttachmentDto> getNoticeAttachments() {
        return noticeAttachmentService.findAll();
    }

    @GetMapping("/{attachmentId}")
    public NoticeAttachmentDto getNoticeAttachment(@PathVariable UUID attachmentId) {
        return noticeAttachmentService.findById(attachmentId);
    }

    @PostMapping
    public ResponseEntity<NoticeAttachmentDto> createNoticeAttachment(@RequestBody NoticeAttachmentDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(noticeAttachmentService.create(request));
    }

    @PutMapping("/{attachmentId}")
    public NoticeAttachmentDto updateNoticeAttachment(@PathVariable UUID attachmentId, @RequestBody NoticeAttachmentDto request) {
        return noticeAttachmentService.update(attachmentId, request);
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteNoticeAttachment(@PathVariable UUID attachmentId) {
        noticeAttachmentService.delete(attachmentId);
        return ResponseEntity.noContent().build();
    }
}