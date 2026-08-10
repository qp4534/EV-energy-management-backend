package com.ev_energy_management.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.OffsetDateTime;

/** Current live Twin measurements used by vehicle/passport displays. */
public record FastApiTwinMeasurementResponse(
        @JsonAlias("vehicle_id") String vehicleId,
        @JsonAlias("observed_at") OffsetDateTime observedAt,
        Long sequence,
        String source,
        @JsonAlias("max_cell_temperature_c") Double maxCellTemperatureC,
        @JsonAlias("mean_cell_temperature_c") Double meanCellTemperatureC,
        @JsonAlias("max_connector_temperature_c") Double maxConnectorTemperatureC,
        @JsonAlias("min_cell_voltage_v") Double minCellVoltageV,
        @JsonAlias("max_cell_voltage_v") Double maxCellVoltageV,
        @JsonAlias("final_risk_level") Short finalRiskLevel,
        @JsonAlias("age_seconds") Double ageSeconds,
        @JsonAlias("stale_after_seconds") Integer staleAfterSeconds,
        @JsonAlias("is_stale") Boolean isStale
) {}
