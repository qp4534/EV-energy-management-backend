package com.ev_energy_management.backend.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ev_energy_management.backend.dto.FastApiTwinMeasurementResponse;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FastApiTwinClientTest {

    @Test
    void springCanCreateTheClientWhenTheTestConstructorAlsoExists() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource(
                    "fastapiTwinClientTest",
                    Map.of(
                            "fastapi.base-url", "http://fastapi",
                            "fastapi.connect-timeout-ms", "1000",
                            "fastapi.read-timeout-ms", "1500",
                            "fastapi.internal-token", "test-internal-token"
                    )
            ));
            context.registerBean(FastApiTwinClient.class);

            context.refresh();

            assertNotNull(context.getBean(FastApiTwinClient.class));
        }
    }

    @Test
    void requestsLatestMeasurementForTheExactVehicle() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://fastapi");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FastApiTwinClient client = new FastApiTwinClient(builder, "test-internal-token");
        UUID carId = UUID.fromString("11111111-1111-4111-8111-111111111111");

        server.expect(once(), requestTo(
                        "http://fastapi/api/v1/twins/vehicles/" + carId
                                + "/latest/measurement?stale_after_seconds=10"
                ))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Twin-Service-Token", "test-internal-token"))
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

    @Test
    void exposesCamelCaseJsonToTheFrontend() throws Exception {
        FastApiTwinMeasurementResponse response = new FastApiTwinMeasurementResponse(
                "11111111-1111-4111-8111-111111111111",
                java.time.OffsetDateTime.parse("2026-08-10T13:30:00+09:00"),
                42L,
                "twin_live",
                78.2,
                51.4,
                44.0,
                3.1,
                4.0,
                (short) 3,
                0.7,
                10,
                false
        );
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

        String json = mapper.writeValueAsString(response);

        assertTrue(json.contains("\"maxCellTemperatureC\":78.2"));
        assertTrue(json.contains("\"observedAt\""));
        assertTrue(json.contains("\"isStale\":false"));
        assertFalse(json.contains("max_cell_temperature_c"));
    }
}
