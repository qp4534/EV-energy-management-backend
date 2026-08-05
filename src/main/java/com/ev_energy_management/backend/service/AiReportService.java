package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.AiReportDto;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AiReportService {

    private List<AiReportDto> mockData() {
        return List.of(
                new AiReportDto(UUID.randomUUID(), "2026년 6월 월간 리포트", "{\"summary\":\"전체 차량 상태 양호\"}",
                        "월간보고서", OffsetDateTime.now(), UUID.randomUUID(), null, false),
                new AiReportDto(UUID.randomUUID(), "이상 감지 리포트", "{\"summary\":\"온도 상승 감지\"}",
                        "이상", OffsetDateTime.now(), UUID.randomUUID(), UUID.randomUUID(), true)
        );
    }

    public List<AiReportDto> findAll() {
        return mockData();
    }

    public AiReportDto findById(UUID reportId) {
        return new AiReportDto(reportId, "2026년 6월 월간 리포트", "{\"summary\":\"전체 차량 상태 양호\"}",
                "월간보고서", OffsetDateTime.now(), UUID.randomUUID(), null, false);
    }

    public AiReportDto create(AiReportDto request) {
        return new AiReportDto(UUID.randomUUID(), request.title(), request.reportData(), request.reportType(),
                OffsetDateTime.now(), request.carId(), request.anomalyId(), false);
    }

    public AiReportDto update(UUID reportId, AiReportDto request) {
        return new AiReportDto(reportId, request.title(), request.reportData(), request.reportType(),
                request.createdAt(), request.carId(), request.anomalyId(), request.isRead());
    }

    public void delete(UUID reportId) {
        // TODO: ERD 확정 후 실제 삭제 로직 연결
    }
}
