package com.ev_energy_management.backend.dto.chat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record ChatMessageResponse(
        String answer,
        String route,
        String safetyLevel,
        OffsetDateTime dataAsOf,
        List<ChatSourceDto> sources,
        List<String> missingFields,
        Boolean fallbackUsed,
        Map<String, Object> metadata
) {}
