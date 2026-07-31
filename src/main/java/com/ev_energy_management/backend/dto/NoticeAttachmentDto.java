package com.ev_energy_management.backend.dto;

import java.util.UUID;

public record NoticeAttachmentDto(
        UUID attachmentId,
        String fileName,
        String fileUrl,
        Long fileSize,
        String fileType,
        UUID noticeId
) {}
