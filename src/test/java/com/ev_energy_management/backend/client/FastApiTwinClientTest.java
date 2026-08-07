package com.ev_energy_management.backend.client;

import com.ev_energy_management.backend.dto.BmsTwinSampleRequest;
import com.ev_energy_management.backend.dto.FastApiTwinFrameResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FastApiTwinClientTest {

    @Test
    void sendsIsoTimestampAndReadsTwinFrameResponse() {
        UUID vehicleId = UUID.fromString("5291cb69-cb5d-4f0b-8019-0dcc3020e47e");
        UUID sessionId = UUID.fromString("2dcb45b7-0fa9-4b95-96fd-d7d9746b8681");
        OffsetDateTime observedAt = OffsetDateTime.parse("2026-08-07T04:07:52.525Z");
        RestClient.Builder builder = RestClient.builder().baseUrl("http://fastapi");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FastApiTwinClient client = new FastApiTwinClient(builder.build());

        server.expect(once(), requestTo("http://fastapi/api/v1/twins/vehicles/" + vehicleId + "/samples"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.observed_at").value("2026-08-07T04:07:52.525Z"))
                .andRespond(withSuccess("""
                        {
                          "schema_version": 1,
                          "layout_id": "generic_ev_concept_96_v1",
                          "vehicle_id": "5291cb69-cb5d-4f0b-8019-0dcc3020e47e",
                          "session_id": "2dcb45b7-0fa9-4b95-96fd-d7d9746b8681",
                          "observed_at": "2026-08-07T04:07:52.525Z",
                          "sequence": 1,
                          "temperature_decic": [300],
                          "voltage_mv": [3600],
                          "state_level": [0],
                          "connector_temperature_decic": [300, 300, 300],
                          "connector_state_level": [0, 0, 0],
                          "hotspot_cell_index": 0,
                          "hotspot_connector_index": 0,
                          "physics_risk_level": 0,
                          "final_risk_level": 0,
                          "image_model_status": "unavailable",
                          "fusion_source": "sensor-only"
                        }
                        """, MediaType.APPLICATION_JSON));

        FastApiTwinFrameResponse response = client.evaluate(
                vehicleId,
                new BmsTwinSampleRequest(
                        sessionId,
                        observedAt,
                        1L,
                        Collections.nCopies(96, 300),
                        Collections.nCopies(96, 3600),
                        Collections.nCopies(3, 300),
                        25.0,
                        20.0
                )
        );

        assertEquals("2026-08-07T04:07:52.525Z", response.observedAt());
        assertEquals(Short.valueOf((short) 0), response.finalRiskLevel());
        server.verify();
    }
}
