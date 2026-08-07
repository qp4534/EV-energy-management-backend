package com.ev_energy_management.backend.client;

import com.ev_energy_management.backend.dto.BmsTwinSampleRequest;
import com.ev_energy_management.backend.dto.FastApiTwinFrameResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Sends one BMS observation through FastAPI's unified 3D Twin pipeline. */
@Component
public class FastApiTwinClient {

    private final RestClient restClient;

    public FastApiTwinClient(@Value("${fastapi.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public FastApiTwinFrameResponse evaluate(UUID vehicleId, BmsTwinSampleRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("schema_version", 1);
        payload.put("layout_id", "generic_ev_concept_96_v1");
        // RestClient's default Jackson converter is not guaranteed to have the
        // Java time module. Send the FastAPI contract's ISO-8601 value directly.
        payload.put("observed_at", request.observedAt().toString());
        payload.put("sequence", request.sequence());
        if (request.sessionId() != null) {
            payload.put("session_id", request.sessionId());
        }
        payload.put("temperature_decic", request.temperatureDecic());
        payload.put("voltage_mv", request.voltageMv());
        payload.put("connector_temperature_decic", request.connectorTemperatureDecic());
        if (request.ambientTemperatureC() != null) {
            payload.put("ambient_temperature_c", request.ambientTemperatureC());
        }
        if (request.packCurrentA() != null) {
            payload.put("pack_current_a", request.packCurrentA());
        }

        return restClient.post()
                .uri("/api/v1/twins/vehicles/{vehicleId}/samples", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(FastApiTwinFrameResponse.class);
    }
}
