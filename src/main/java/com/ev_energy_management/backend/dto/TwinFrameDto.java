package com.ev_energy_management.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TwinFrameDto(
        UUID frameId,
        OffsetDateTime observedAt,
        Short hotspotCellIndex,
        Short hotspotConnectorIndex,
        Short mlRiskLevel,
        Short physicsRiskLevel,
        Short finalRiskLevel,
        Short imageRiskLevel,
        Float imageConfidence,
        String rawMetrics,
        String modelInput,
        UUID anomalyId,
        UUID carId,
        UUID sessionId,
        String sourceImageRef
) {}
