package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.BatchJobLogDto;
import com.ev_energy_management.backend.service.BatchJobLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/batch-job-logs")
public class BatchJobLogController {

    private final BatchJobLogService batchJobLogService;

    public BatchJobLogController(BatchJobLogService batchJobLogService) {
        this.batchJobLogService = batchJobLogService;
    }

    @GetMapping
    public List<BatchJobLogDto> getBatchJobLogs() {
        return batchJobLogService.findAll();
    }

    @GetMapping("/{batchLogId}")
    public BatchJobLogDto getBatchJobLog(@PathVariable UUID batchLogId) {
        return batchJobLogService.findById(batchLogId);
    }

    @PostMapping
    public ResponseEntity<BatchJobLogDto> createBatchJobLog(@RequestBody BatchJobLogDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(batchJobLogService.create(request));
    }

    @PutMapping("/{batchLogId}")
    public BatchJobLogDto updateBatchJobLog(@PathVariable UUID batchLogId, @RequestBody BatchJobLogDto request) {
        return batchJobLogService.update(batchLogId, request);
    }

    @DeleteMapping("/{batchLogId}")
    public ResponseEntity<Void> deleteBatchJobLog(@PathVariable UUID batchLogId) {
        batchJobLogService.delete(batchLogId);
        return ResponseEntity.noContent().build();
    }
}
