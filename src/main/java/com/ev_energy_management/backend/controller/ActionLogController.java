package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.ActionLogDto;
import com.ev_energy_management.backend.service.ActionLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/action-logs")
public class ActionLogController {

    private final ActionLogService actionLogService;

    public ActionLogController(ActionLogService actionLogService) {
        this.actionLogService = actionLogService;
    }

    @GetMapping
    public List<ActionLogDto> getActionLogs() {
        return actionLogService.findAll();
    }

    @GetMapping("/{actionId}")
    public ActionLogDto getActionLog(@PathVariable UUID actionId) {
        return actionLogService.findById(actionId);
    }

    @PostMapping
    public ResponseEntity<ActionLogDto> createActionLog(@RequestBody ActionLogDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(actionLogService.create(request));
    }

    @PutMapping("/{actionId}")
    public ActionLogDto updateActionLog(@PathVariable UUID actionId, @RequestBody ActionLogDto request) {
        return actionLogService.update(actionId, request);
    }

    @DeleteMapping("/{actionId}")
    public ResponseEntity<Void> deleteActionLog(@PathVariable UUID actionId) {
        actionLogService.delete(actionId);
        return ResponseEntity.noContent().build();
    }
}
