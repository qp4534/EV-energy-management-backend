package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.ResourceUsageDto;
import com.ev_energy_management.backend.service.SystemMonitorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system/monitor")
public class SystemMonitorController {

    private final SystemMonitorService systemMonitorService;

    public SystemMonitorController(SystemMonitorService systemMonitorService) {
        this.systemMonitorService = systemMonitorService;
    }

    @GetMapping("/resource-usage")
    public List<ResourceUsageDto> getResourceUsage() {
        return systemMonitorService.getResourceUsage();
    }
}