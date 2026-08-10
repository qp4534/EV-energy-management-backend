package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.BatchJobDto;
import com.ev_energy_management.backend.entity.BatchJobEntity;
import com.ev_energy_management.backend.repository.BatchJobRepository;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BatchJobService {

    private final BatchJobRepository batchJobRepository;
    private final ActionLogWriter actionLogWriter;

    public BatchJobService(BatchJobRepository batchJobRepository, ActionLogWriter actionLogWriter) {
        this.batchJobRepository = batchJobRepository;
        this.actionLogWriter = actionLogWriter;
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

    // "실행" 버튼 - lastRunAt을 지금 시각으로, nextRunAt은 cycle 기준으로 대략 계산.
    // 진짜 스케줄러랑 연동된 게 아니라, 수동으로 "지금 실행했다"고 기록하는 정도의 기능.
    public BatchJobDto run(AuthenticatedUser actor, String jobId) {
        BatchJobEntity entity = batchJobRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Batch job not found: " + jobId));

        OffsetDateTime now = OffsetDateTime.now();
        entity.setStatus("정상");
        entity.setLastRunAt(now);
        entity.setNextRunAt(computeNextRun(now, entity.getCycle()));
        BatchJobDto saved = toDto(batchJobRepository.save(entity));

        actionLogWriter.write(
                actor == null ? null : actor.userId(),
                "BATCH_JOB_RUN",
                "BATCH_JOB",
                UUID.nameUUIDFromBytes(jobId.getBytes()),
                Map.of("jobId", jobId, "jobName", entity.getJobName() == null ? "" : entity.getJobName())
        );
        return saved;
    }

    // cycle 문자열("매일"/"매주"/"매월" 등)에 "주"/"월" 글자가 있는지로 대략 판단.
    // 정확한 스케줄 파싱이 아니라 임시 근사치.
    private OffsetDateTime computeNextRun(OffsetDateTime from, String cycle) {
        if (cycle == null) return from.plusDays(1);
        if (cycle.contains("월")) return from.plusMonths(1);
        if (cycle.contains("주")) return from.plusWeeks(1);
        return from.plusDays(1);
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