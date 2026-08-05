package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.NotificationMatrixDto;
import com.ev_energy_management.backend.service.NotificationMatrixService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notification-matrix")
public class NotificationMatrixController {

    private final NotificationMatrixService notificationMatrixService;

    public NotificationMatrixController(NotificationMatrixService notificationMatrixService) {
        this.notificationMatrixService = notificationMatrixService;
    }

    @GetMapping
    public List<NotificationMatrixDto> getNotificationMatrixEntries() {
        return notificationMatrixService.findAll();
    }

    @GetMapping("/{matrixId}")
    public NotificationMatrixDto getNotificationMatrixEntry(@PathVariable UUID matrixId) {
        return notificationMatrixService.findById(matrixId);
    }

    @PostMapping
    public ResponseEntity<NotificationMatrixDto> createNotificationMatrixEntry(@RequestBody NotificationMatrixDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationMatrixService.create(request));
    }

    @PutMapping("/{matrixId}")
    public NotificationMatrixDto updateNotificationMatrixEntry(@PathVariable UUID matrixId, @RequestBody NotificationMatrixDto request) {
        return notificationMatrixService.update(matrixId, request);
    }

    @DeleteMapping("/{matrixId}")
    public ResponseEntity<Void> deleteNotificationMatrixEntry(@PathVariable UUID matrixId) {
        notificationMatrixService.delete(matrixId);
        return ResponseEntity.noContent().build();
    }
}
