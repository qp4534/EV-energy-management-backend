package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.VehicleRiskOverviewDto;
import com.ev_energy_management.backend.service.VehicleRiskOverviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class VehicleRiskOverviewController {

    private final VehicleRiskOverviewService vehicleRiskOverviewService;

    public VehicleRiskOverviewController(VehicleRiskOverviewService vehicleRiskOverviewService) {
        this.vehicleRiskOverviewService = vehicleRiskOverviewService;
    }

    @GetMapping("/vehicle-risk-overview")
    public VehicleRiskOverviewDto getOverview() {
        return vehicleRiskOverviewService.getOverview();
    }
}
