package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.BatchJobDto;
import com.ev_energy_management.backend.entity.BatchJobEntity;
import com.ev_energy_management.backend.repository.BatchJobRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BatchJobService {

    private final BatchJobRepository batchJobRepository;

    public BatchJobService(BatchJobRepository batchJobRepository) {
        this.batchJobRepository = batchJobRepository;
    }

    public List<BatchJobDto> findAll() {
        return batchJobRepository.findAll().stream().map(this::toDto).toList();
    }

    public BatchJobDto findById(String jobId) {
        return toDto(batchJobRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Batch job not found: " + jobId)));
    }

    public BatchJobDto create(BatchJobDto request) {
        BatchJobEntity entity = BatchJobEntity.builder()
                .jobId(request.jobId())
                .jobName(request.jobName())
                .cycle(request.cycle())
                .status(request.status() != null ? request.status() : "정상")
                .lastRunAt(request.lastRunAt())
                .nextRunAt(request.nextRunAt())
                .build();
        return toDto(batchJobRepository.save(entity));
    }

    public BatchJobDto update(String jobId, BatchJobDto request) {
        BatchJobEntity entity = batchJobRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Batch job not found: " + jobId));
        entity.setJobName(request.jobName());
        entity.setCycle(request.cycle());
        entity.setStatus(request.status());
        entity.setLastRunAt(request.lastRunAt());
        entity.setNextRunAt(request.nextRunAt());
        return toDto(batchJobRepository.save(entity));
    }

    public void delete(String jobId) {
        batchJobRepository.deleteById(jobId);
    }

    private BatchJobDto toDto(BatchJobEntity entity) {
        return new BatchJobDto(
                entity.getJobId(),
                entity.getJobName(),
                entity.getCycle(),
                entity.getStatus(),
                entity.getLastRunAt(),
                entity.getNextRunAt()
        );
    }
}
