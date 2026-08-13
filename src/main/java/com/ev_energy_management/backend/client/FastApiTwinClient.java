package com.ev_energy_management.backend.client;

import com.ev_energy_management.backend.dto.BmsTwinSampleRequest;
import com.ev_energy_management.backend.dto.FastApiTwinFrameResponse;
import com.ev_energy_management.backend.dto.FastApiTwinMeasurementResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Sends one BMS observation through FastAPI's unified 3D Twin pipeline. */
@Component
public class FastApiTwinClient {

    private static final String INTERNAL_TOKEN_HEADER = "X-Twin-Service-Token";

    private final RestClient restClient;

    @Autowired
    public FastApiTwinClient(
            @Value("${fastapi.base-url}") String baseUrl,
            @Value("${fastapi.connect-timeout-ms:1000}") int connectTimeoutMs,
            @Value("${fastapi.read-timeout-ms:1500}") int readTimeoutMs,
            @Value("${fastapi.internal-token:}") String internalToken
    ) {
        this.restClient = buildRestClient(
                baseUrl, connectTimeoutMs, readTimeoutMs, internalToken);
    }

    FastApiTwinClient(RestClient restClient) {
        this.restClient = restClient;
    }

    FastApiTwinClient(RestClient.Builder builder, String internalToken) {
        this.restClient = withInternalToken(builder, internalToken).build();
    }

    public FastApiTwinFrameResponse evaluate(UUID vehicleId, BmsTwinSampleRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("schema_version", 1);
        payload.put("layout_id", "generic_ev_concept_96_v1");
        payload.put("observed_at", request.observedAt());
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

    public FastApiTwinMeasurementResponse latestMeasurement(
            UUID vehicleId,
            int staleAfterSeconds
    ) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/twins/vehicles/{vehicleId}/latest/measurement")
                        .queryParam("stale_after_seconds", staleAfterSeconds)
                        .build(vehicleId))
                .retrieve()
                .body(FastApiTwinMeasurementResponse.class);
    }

    private static RestClient buildRestClient(
            String baseUrl,
            int connectTimeoutMs,
            int readTimeoutMs,
            String internalToken
    ) {
        if (connectTimeoutMs <= 0 || readTimeoutMs <= 0) {
            throw new IllegalArgumentException("FastAPI Twin timeouts must be positive");
        }
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory);
        return withInternalToken(builder, internalToken).build();
    }

    private static RestClient.Builder withInternalToken(
            RestClient.Builder builder,
            String internalToken
    ) {
        if (internalToken != null && !internalToken.isBlank()) {
            builder.defaultHeader(INTERNAL_TOKEN_HEADER, internalToken);
        }
        return builder;
    }
}
