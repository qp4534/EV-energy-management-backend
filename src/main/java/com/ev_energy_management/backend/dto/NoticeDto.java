package com.ev_energy_management.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NoticeDto(
        UUID noticeId,
        String title,
        String content,
        Boolean isPinned,
        OffsetDateTime createdAt,
        UUID userId,
        Boolean isRead
) {}
