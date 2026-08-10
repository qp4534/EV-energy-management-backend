package com.ev_energy_management.backend.dto;

import java.util.UUID;

// userId는 요청 바디로 안 받고 인증된 사용자 본인으로 서버에서 고정한다(NotificationController 참고).
public record NotificationCreateRequest(
        String riskLevel,
        String title,
        String body,
        UUID carId,
        UUID reportId
) {}
