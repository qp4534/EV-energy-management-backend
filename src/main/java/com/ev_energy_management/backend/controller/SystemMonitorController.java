package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.DeployHistoryDto;
import com.ev_energy_management.backend.dto.ResourceUsageDto;
import com.ev_energy_management.backend.service.GitHubDeployHistoryService;
import com.ev_energy_management.backend.service.SystemMonitorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system/monitor")
public class SystemMonitorController {

    private final SystemMonitorService systemMonitorService;
    private final GitHubDeployHistoryService gitHubDeployHistoryService;

    public SystemMonitorController(
            SystemMonitorService systemMonitorService,
            GitHubDeployHistoryService gitHubDeployHistoryService
    ) {
        this.systemMonitorService = systemMonitorService;
        this.gitHubDeployHistoryService = gitHubDeployHistoryService;
    }

    @GetMapping("/resource-usage")
    public List<ResourceUsageDto> getResourceUsage() {
        return systemMonitorService.getResourceUsage();
    }

    @GetMapping("/deploy-history")
    public List<DeployHistoryDto> getDeployHistory(@RequestParam(defaultValue = "5") int limit) {
        return gitHubDeployHistoryService.getRecentDeployHistory(limit);
    }
}
