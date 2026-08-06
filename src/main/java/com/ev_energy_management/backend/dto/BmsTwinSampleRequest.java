package com.ev_energy_management.backend.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * One BMS observation accepted by the Spring backend before it is evaluated by
 * the FastAPI digital-twin service.
 */
public record BmsTwinSampleRequest(
        UUID sessionId,
        OffsetDateTime observedAt,
        Long sequence,
        List<Integer> temperatureDecic,
        List<Integer> voltageMv,
        List<Integer> connectorTemperatureDecic,
        Double ambientTemperatureC,
        Double packCurrentA
) {}
