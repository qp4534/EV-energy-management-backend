package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.DeviceTokenRegisterRequest;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import com.ev_energy_management.backend.service.DeviceTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device-tokens")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    public DeviceTokenController(DeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    @PostMapping
    public ResponseEntity<Void> register(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody DeviceTokenRegisterRequest request
    ) {
        deviceTokenService.register(user.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{expoPushToken}")
    public ResponseEntity<Void> unregister(@PathVariable String expoPushToken) {
        deviceTokenService.unregister(expoPushToken);
        return ResponseEntity.noContent().build();
    }
}
