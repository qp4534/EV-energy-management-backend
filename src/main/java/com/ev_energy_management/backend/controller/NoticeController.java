package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.NoticeDto;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import com.ev_energy_management.backend.service.NoticeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public List<NoticeDto> getNotices(@AuthenticationPrincipal AuthenticatedUser actor) {
        return noticeService.findAll(actor);
    }

    @GetMapping("/{noticeId}")
    public NoticeDto getNotice(
            @AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable UUID noticeId
    ) {
        return noticeService.findById(actor, noticeId);
    }

    @PostMapping
    public ResponseEntity<NoticeDto> createNotice(
            @AuthenticationPrincipal AuthenticatedUser actor,
            @RequestBody NoticeDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(noticeService.create(actor, request));
    }

    @PutMapping("/{noticeId}")
    public NoticeDto updateNotice(
            @AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable UUID noticeId,
            @RequestBody NoticeDto request
    ) {
        return noticeService.update(actor, noticeId, request);
    }

    @DeleteMapping("/{noticeId}")
    public ResponseEntity<Void> deleteNotice(
            @AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable UUID noticeId
    ) {
        noticeService.delete(actor, noticeId);
        return ResponseEntity.noContent().build();
    }
}