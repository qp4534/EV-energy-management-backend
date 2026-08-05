package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.NoticeAttachmentDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NoticeAttachmentService {

    private List<NoticeAttachmentDto> mockData() {
        return List.of(
                new NoticeAttachmentDto(UUID.randomUUID(), "notice.pdf", "https://example.com/files/notice.pdf",
                        102400L, "application/pdf", UUID.randomUUID()),
                new NoticeAttachmentDto(UUID.randomUUID(), "guide.png", "https://example.com/files/guide.png",
                        51200L, "image/png", UUID.randomUUID())
        );
    }

    public List<NoticeAttachmentDto> findAll() {
        return mockData();
    }

    public NoticeAttachmentDto findById(UUID attachmentId) {
        return new NoticeAttachmentDto(attachmentId, "notice.pdf", "https://example.com/files/notice.pdf",
                102400L, "application/pdf", UUID.randomUUID());
    }

    public NoticeAttachmentDto create(NoticeAttachmentDto request) {
        return new NoticeAttachmentDto(UUID.randomUUID(), request.fileName(), request.fileUrl(),
                request.fileSize(), request.fileType(), request.noticeId());
    }

    public NoticeAttachmentDto update(UUID attachmentId, NoticeAttachmentDto request) {
        return new NoticeAttachmentDto(attachmentId, request.fileName(), request.fileUrl(),
                request.fileSize(), request.fileType(), request.noticeId());
    }

    public void delete(UUID attachmentId) {
        // TODO: ERD 확정 후 실제 삭제 로직 연결
    }
}
