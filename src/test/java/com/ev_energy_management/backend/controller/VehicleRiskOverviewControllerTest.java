package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.VehicleRiskOverviewDto;
import com.ev_energy_management.backend.service.VehicleRiskOverviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class VehicleRiskOverviewControllerTest {

    @Mock
    private VehicleRiskOverviewService vehicleRiskOverviewService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new VehicleRiskOverviewController(vehicleRiskOverviewService)).build();
    }

    @Test
    void returnsDashboardRiskOverview() throws Exception {
        when(vehicleRiskOverviewService.getOverview()).thenReturn(new VehicleRiskOverviewDto(
                new VehicleRiskOverviewDto.VehicleRiskSummaryDto(140, 12, 22, 34, 72),
                List.of(),
                List.of(new VehicleRiskOverviewDto.DailyRiskCountDto("2026-08-07", 4))
        ));

        mockMvc.perform(get("/api/dashboard/vehicle-risk-overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.total").value(140))
                .andExpect(jsonPath("$.summary.emergency").value(12))
                .andExpect(jsonPath("$.dailyRiskCounts[0].count").value(4));

        verify(vehicleRiskOverviewService).getOverview();
    }
}
