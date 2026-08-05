package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.BatchJobLogDto;
import com.ev_energy_management.backend.entity.BatchJobLogEntity;
import com.ev_energy_management.backend.repository.BatchJobLogRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BatchJobLogService {

    private final BatchJobLogRepository batchJobLogRepository;

    public BatchJobLogService(BatchJobLogRepository batchJobLogRepository) {
        this.batchJobLogRepository = batchJobLogRepository;
    }

    public List<BatchJobLogDto> findAll() {
        return batchJobLogRepository.findAll().stream().map(this::toDto).toList();
    }

    public BatchJobLogDto findById(UUID batchLogId) {
        return toDto(batchJobLogRepository.findById(batchLogId)
                .orElseThrow(() -> new EntityNotFoundException("Batch job log not found: " + batchLogId)));
    }

    public BatchJobLogDto create(BatchJobLogDto request) {
        BatchJobLogEntity entity = BatchJobLogEntity.builder()
                .runType(request.runType() != null ? request.runType() : "AUTO")
                .status(request.status() != null ? request.status() : "SUCCESS")
                .message(request.message())
                .jobId(request.jobId())
                .build();
        return toDto(batchJobLogRepository.save(entity));
    }

    public BatchJobLogDto update(UUID batchLogId, BatchJobLogDto request) {
        BatchJobLogEntity entity = batchJobLogRepository.findById(batchLogId)
                .orElseThrow(() -> new EntityNotFoundException("Batch job log not found: " + batchLogId));
        entity.setRunType(request.runType());
        entity.setStatus(request.status());
        entity.setMessage(request.message());
        entity.setJobId(request.jobId());
        return toDto(batchJobLogRepository.save(entity));
    }

    public void delete(UUID batchLogId) {
        batchJobLogRepository.deleteById(batchLogId);
    }

    private BatchJobLogDto toDto(BatchJobLogEntity entity) {
        return new BatchJobLogDto(
                entity.getBatchLogId(),
                entity.getRunType(),
                entity.getStatus(),
                entity.getMessage(),
                entity.getExecutedAt(),
                entity.getJobId()
        );
    }
}
