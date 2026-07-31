package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.BatchJobLogDto;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BatchJobLogService {

    private List<BatchJobLogDto> mockData() {
        return List.of(
                new BatchJobLogDto(UUID.randomUUID(), "AUTO", "SUCCESS", null, OffsetDateTime.now(), "BATCH_DIAGNOSIS"),
                new BatchJobLogDto(UUID.randomUUID(), "MANUAL", "FAILED", "타임아웃 발생", OffsetDateTime.now(), "BATCH_REPORT")
        );
    }

    public List<BatchJobLogDto> findAll() {
        return mockData();
    }

    public BatchJobLogDto findById(UUID batchLogId) {
        return new BatchJobLogDto(batchLogId, "AUTO", "SUCCESS", null, OffsetDateTime.now(), "BATCH_DIAGNOSIS");
    }

    public BatchJobLogDto create(BatchJobLogDto request) {
        return new BatchJobLogDto(UUID.randomUUID(), request.runType(), request.status(), request.message(),
                OffsetDateTime.now(), request.jobId());
    }

    public BatchJobLogDto update(UUID batchLogId, BatchJobLogDto request) {
        return new BatchJobLogDto(batchLogId, request.runType(), request.status(), request.message(),
                request.executedAt(), request.jobId());
    }

    public void delete(UUID batchLogId) {
        // TODO: ERD 확정 후 실제 삭제 로직 연결
    }
}
