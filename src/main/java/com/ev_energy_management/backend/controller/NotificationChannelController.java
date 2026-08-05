package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.NotificationChannelDto;
import com.ev_energy_management.backend.service.NotificationChannelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification-channels")
public class NotificationChannelController {

    private final NotificationChannelService notificationChannelService;

    public NotificationChannelController(NotificationChannelService notificationChannelService) {
        this.notificationChannelService = notificationChannelService;
    }

    @GetMapping
    public List<NotificationChannelDto> getNotificationChannels() {
        return notificationChannelService.findAll();
    }

    @GetMapping("/{channelId}")
    public NotificationChannelDto getNotificationChannel(@PathVariable String channelId) {
        return notificationChannelService.findById(channelId);
    }

    @PostMapping
    public ResponseEntity<NotificationChannelDto> createNotificationChannel(@RequestBody NotificationChannelDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationChannelService.create(request));
    }

    @PutMapping("/{channelId}")
    public NotificationChannelDto updateNotificationChannel(@PathVariable String channelId, @RequestBody NotificationChannelDto request) {
        return notificationChannelService.update(channelId, request);
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteNotificationChannel(@PathVariable String channelId) {
        notificationChannelService.delete(channelId);
        return ResponseEntity.noContent().build();
    }
}
