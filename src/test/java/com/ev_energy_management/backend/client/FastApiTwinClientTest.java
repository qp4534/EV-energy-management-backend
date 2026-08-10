package com.ev_energy_management.backend.client;

import com.ev_energy_management.backend.dto.FastApiTwinMeasurementResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FastApiTwinClientTest {

    @Test
    void requestsLatestMeasurementForTheExactVehicle() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://fastapi");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FastApiTwinClient client = new FastApiTwinClient(builder.build());
        UUID carId = UUID.fromString("11111111-1111-4111-8111-111111111111");

        server.expect(once(), requestTo(
                        "http://fastapi/api/v1/twins/vehicles/" + carId
                                + "/latest/measurement?stale_after_seconds=10"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "vehicle_id":"11111111-1111-4111-8111-111111111111",
                          "observed_at":"2026-08-10T13:30:00+09:00",
                          "sequence":42,
                          "source":"twin_live",
                          "max_cell_temperature_c":78.2,
                          "mean_cell_temperature_c":51.4,
                          "max_connector_temperature_c":44.0,
                          "min_cell_voltage_v":3.1,
                          "max_cell_voltage_v":4.0,
                          "final_risk_level":3,
                          "age_seconds":0.7,
                          "stale_after_seconds":10,
                          "is_stale":false
                        }
                        """, MediaType.APPLICATION_JSON));

        FastApiTwinMeasurementResponse response = client.latestMeasurement(carId, 10);

        assertEquals(78.2, response.maxCellTemperatureC());
        assertEquals("twin_live", response.source());
        assertFalse(response.isStale());
        server.verify();
    }
}
