package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.NotificationCreateRequest;
import com.ev_energy_management.backend.dto.NotificationDto;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import com.ev_energy_management.backend.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// 다른 도메인 컨트롤러(CarController 등)와 달리, 이 테이블엔 user_id가 있어서 처음부터
// 로그인한 본인 것만 조회/수정 가능하도록 @AuthenticationPrincipal로 스코핑한다.
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationDto> getMyNotifications(@AuthenticationPrincipal AuthenticatedUser user) {
        return notificationService.findMine(user.userId());
    }

    @GetMapping("/{notificationId}")
    public NotificationDto getMyNotification(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID notificationId
    ) {
        return notificationService.findMineById(user.userId(), notificationId);
    }

    @PostMapping
    public ResponseEntity<NotificationDto> createNotification(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody NotificationCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.create(user.userId(), request));
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationDto markAsRead(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID notificationId
    ) {
        return notificationService.markAsRead(user.userId(), notificationId);
    }
}
