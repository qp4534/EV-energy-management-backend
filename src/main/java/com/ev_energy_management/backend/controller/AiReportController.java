package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.AiReportDto;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import com.ev_energy_management.backend.service.AiReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai-reports")
public class AiReportController {

    private final AiReportService aiReportService;

    public AiReportController(AiReportService aiReportService) {
        this.aiReportService = aiReportService;
    }

    @GetMapping
    public List<AiReportDto> getAiReports(@AuthenticationPrincipal AuthenticatedUser user) {
        return aiReportService.findAll(user);
    }

    @GetMapping("/{reportId}")
    public AiReportDto getAiReport(@PathVariable UUID reportId) {
        return aiReportService.findById(reportId);
    }

    @PostMapping
    public ResponseEntity<AiReportDto> createAiReport(@RequestBody AiReportDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aiReportService.create(request));
    }

    @PutMapping("/{reportId}")
    public AiReportDto updateAiReport(@PathVariable UUID reportId, @RequestBody AiReportDto request) {
        return aiReportService.update(reportId, request);
    }

    @PatchMapping("/{reportId}/read")
    public AiReportDto markAiReportAsRead(@PathVariable UUID reportId) {
        return aiReportService.markAsRead(reportId);
    }

    @PostMapping("/{reportId}/actions/notify-customer")
    public ResponseEntity<Void> notifyCustomer(@PathVariable UUID reportId) {
        aiReportService.notifyCustomer(reportId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{reportId}/actions/dispatch-emergency")
    public ResponseEntity<Void> dispatchEmergency(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID reportId
    ) {
        aiReportService.dispatchEmergency(user, reportId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteAiReport(@PathVariable UUID reportId) {
        aiReportService.delete(reportId);
        return ResponseEntity.noContent().build();
    }
}
