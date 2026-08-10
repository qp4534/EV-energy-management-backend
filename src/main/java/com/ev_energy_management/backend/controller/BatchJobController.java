package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.BatchJobDto;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import com.ev_energy_management.backend.service.BatchJobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/batch-jobs")
public class BatchJobController {

    private final BatchJobService batchJobService;

    public BatchJobController(BatchJobService batchJobService) {
        this.batchJobService = batchJobService;
    }

    @GetMapping
    public List<BatchJobDto> getBatchJobs() {
        return batchJobService.findAll();
    }

    @GetMapping("/{jobId}")
    public BatchJobDto getBatchJob(@PathVariable String jobId) {
        return batchJobService.findById(jobId);
    }

    @PostMapping
    public ResponseEntity<BatchJobDto> createBatchJob(@RequestBody BatchJobDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(batchJobService.create(request));
    }

    @PutMapping("/{jobId}")
    public BatchJobDto updateBatchJob(@PathVariable String jobId, @RequestBody BatchJobDto request) {
        return batchJobService.update(jobId, request);
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> deleteBatchJob(@PathVariable String jobId) {
        batchJobService.delete(jobId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{jobId}/run")
    public BatchJobDto runBatchJob(
            @AuthenticationPrincipal AuthenticatedUser actor,
            @PathVariable String jobId
    ) {
        return batchJobService.run(actor, jobId);
    }
}