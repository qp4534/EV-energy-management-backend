package com.ev_energy_management.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Public 3D Twin frame returned by FastAPI after inference and live publication. */
public record FastApiTwinFrameResponse(
        @JsonProperty("schema_version") Integer schemaVersion,
        @JsonProperty("layout_id") String layoutId,
        @JsonProperty("vehicle_id") String vehicleId,
        @JsonProperty("anomaly_id") String anomalyId,
        @JsonProperty("session_id") UUID sessionId,
        @JsonProperty("observed_at") OffsetDateTime observedAt,
        Long sequence,
        @JsonProperty("temperature_decic") List<Integer> temperatureDecic,
        @JsonProperty("voltage_mv") List<Integer> voltageMv,
        @JsonProperty("state_level") List<Integer> stateLevel,
        @JsonProperty("connector_temperature_decic") List<Integer> connectorTemperatureDecic,
        @JsonProperty("connector_state_level") List<Integer> connectorStateLevel,
        @JsonProperty("hotspot_cell_index") Integer hotspotCellIndex,
        @JsonProperty("hotspot_connector_index") Integer hotspotConnectorIndex,
        @JsonProperty("ml_risk_level") Short mlRiskLevel,
        @JsonProperty("physics_risk_level") Short physicsRiskLevel,
        @JsonProperty("final_risk_level") Short finalRiskLevel,
        @JsonProperty("cell_heat_score") List<Double> cellHeatScore,
        @JsonProperty("image_risk_level") Short imageRiskLevel,
        @JsonProperty("image_confidence") Double imageConfidence,
        @JsonProperty("image_probabilities") List<Double> imageProbabilities,
        @JsonProperty("image_model_status") String imageModelStatus,
        @JsonProperty("module_heat_score") List<Double> moduleHeatScore,
        @JsonProperty("module_state_level") List<Integer> moduleStateLevel,
        @JsonProperty("hotspot_module_index") Integer hotspotModuleIndex,
        @JsonProperty("thermal_frame_ref") String thermalFrameRef,
        @JsonProperty("thermal_frame_sha256") String thermalFrameSha256,
        @JsonProperty("fusion_source") String fusionSource
) {}
