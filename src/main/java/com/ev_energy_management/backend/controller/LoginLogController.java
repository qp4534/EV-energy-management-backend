package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.LoginLogDto;
import com.ev_energy_management.backend.service.LoginLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/login-logs")
public class LoginLogController {

    private final LoginLogService loginLogService;

    public LoginLogController(LoginLogService loginLogService) {
        this.loginLogService = loginLogService;
    }

    @GetMapping
    public List<LoginLogDto> getLoginLogs() {
        return loginLogService.findAll();
    }

    @GetMapping("/{logId}")
    public LoginLogDto getLoginLog(@PathVariable UUID logId) {
        return loginLogService.findById(logId);
    }

    @PostMapping
    public ResponseEntity<LoginLogDto> createLoginLog(@RequestBody LoginLogDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loginLogService.create(request));
    }

    @PutMapping("/{logId}")
    public LoginLogDto updateLoginLog(@PathVariable UUID logId, @RequestBody LoginLogDto request) {
        return loginLogService.update(logId, request);
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> deleteLoginLog(@PathVariable UUID logId) {
        loginLogService.delete(logId);
        return ResponseEntity.noContent().build();
    }
}
