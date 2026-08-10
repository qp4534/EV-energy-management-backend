package com.ev_energy_management.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/** Current live Twin measurements used by vehicle/passport displays. */
public record FastApiTwinMeasurementResponse(
        @JsonProperty("vehicle_id") String vehicleId,
        @JsonProperty("observed_at") OffsetDateTime observedAt,
        Long sequence,
        String source,
        @JsonProperty("max_cell_temperature_c") Double maxCellTemperatureC,
        @JsonProperty("mean_cell_temperature_c") Double meanCellTemperatureC,
        @JsonProperty("max_connector_temperature_c") Double maxConnectorTemperatureC,
        @JsonProperty("min_cell_voltage_v") Double minCellVoltageV,
        @JsonProperty("max_cell_voltage_v") Double maxCellVoltageV,
        @JsonProperty("final_risk_level") Short finalRiskLevel,
        @JsonProperty("age_seconds") Double ageSeconds,
        @JsonProperty("stale_after_seconds") Integer staleAfterSeconds,
        @JsonProperty("is_stale") Boolean isStale
) {}
