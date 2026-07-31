package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.BatchJobDto;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class BatchJobService {

    private List<BatchJobDto> mockData() {
        return List.of(
                new BatchJobDto("BATCH_DIAGNOSIS", "배터리 진단 배치", "매일 03:00", "정상",
                        OffsetDateTime.now().minusHours(6), OffsetDateTime.now().plusHours(18)),
                new BatchJobDto("BATCH_REPORT", "월간 리포트 생성 배치", "매월 1일", "정상",
                        OffsetDateTime.now().minusDays(5), OffsetDateTime.now().plusDays(25))
        );
    }

    public List<BatchJobDto> findAll() {
        return mockData();
    }

    public BatchJobDto findById(String jobId) {
        return new BatchJobDto(jobId, "배터리 진단 배치", "매일 03:00", "정상",
                OffsetDateTime.now().minusHours(6), OffsetDateTime.now().plusHours(18));
    }

    public BatchJobDto create(BatchJobDto request) {
        return new BatchJobDto(request.jobId(), request.jobName(), request.cycle(), request.status(),
                request.lastRunAt(), request.nextRunAt());
    }

    public BatchJobDto update(String jobId, BatchJobDto request) {
        return new BatchJobDto(jobId, request.jobName(), request.cycle(), request.status(),
                request.lastRunAt(), request.nextRunAt());
    }

    public void delete(String jobId) {
        // TODO: ERD 확정 후 실제 삭제 로직 연결
    }
}
