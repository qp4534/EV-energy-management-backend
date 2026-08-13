package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.AiReportDto;
import com.ev_energy_management.backend.entity.AiReportEntity;
import com.ev_energy_management.backend.repository.AiReportRepository;
import com.ev_energy_management.backend.repository.CarRepository;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiReportServiceTest {

    @Mock
    private AiReportRepository aiReportRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ActionLogWriter actionLogWriter;

    private AiReportService aiReportService;

    @BeforeEach
    void setUp() {
        aiReportService = new AiReportService(aiReportRepository, carRepository, notificationService, actionLogWriter);
    }

    @Test
    void returnsFastApiReportJsonAsStructuredDataInNewestFirstOrder() {
        AiReportEntity entity = reportEntity(false);
        entity.setReportType("이상");
        entity.setReportData("""
                {
                  "schemaVersion":"1.0",
                  "riskLevel":"WARNING",
                  "sections":[{"type":"summary","title":"요약","content":"내용"}],
                  "sources":[],"missingFields":[],"actions":[]
                }
                """);
        when(aiReportRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(entity));

        AiReportDto result = aiReportService.findAll(new AuthenticatedUser(UUID.randomUUID(), "관리자")).get(0);

        assertEquals("이상", result.reportType());
        assertEquals("WARNING", result.reportData().get("riskLevel"));
        assertEquals(1, ((List<?>) result.reportData().get("sections")).size());
    }

    @Test
    void legacySummaryIsWrappedForTheCurrentReportRenderer() {
        AiReportEntity entity = reportEntity(false);
        entity.setReportData("{\"summary\":\"기존 보고서 내용\"}");
        when(aiReportRepository.findById(entity.getReportId())).thenReturn(Optional.of(entity));

        AiReportDto result = aiReportService.findById(entity.getReportId());

        List<?> sections = (List<?>) result.reportData().get("sections");
        Map<?, ?> summary = (Map<?, ?>) sections.get(0);
        assertEquals("summary", summary.get("type"));
        assertEquals("기존 보고서 내용", summary.get("content"));
    }

    @Test
    void markAsReadDoesNotOverwriteReportContent() {
        AiReportEntity entity = reportEntity(false);
        String originalData = entity.getReportData();
        when(aiReportRepository.findById(entity.getReportId())).thenReturn(Optional.of(entity));
        when(aiReportRepository.save(entity)).thenReturn(entity);

        AiReportDto result = aiReportService.markAsRead(entity.getReportId());

        assertEquals(true, result.isRead());
        assertEquals(originalData, entity.getReportData());
        verify(aiReportRepository).save(entity);
    }

    @Test
    void alreadyReadReportDoesNotPerformAnUnnecessaryWrite() {
        AiReportEntity entity = reportEntity(true);
        when(aiReportRepository.findById(entity.getReportId())).thenReturn(Optional.of(entity));

        AiReportDto result = aiReportService.markAsRead(entity.getReportId());

        assertSame(true, result.isRead());
        verify(aiReportRepository, never()).save(entity);
    }

    @Test
    void createSerializesStructuredDataAndUsesCanonicalMonthlyType() {
        UUID carId = UUID.randomUUID();
        Map<String, Object> reportData = Map.of(
                "riskLevel", "NORMAL",
                "sections", List.of()
        );
        AiReportDto request = new AiReportDto(
                null, "월간 보고서", reportData, "월간보고서",
                null, carId, null, null
        );
        when(aiReportRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> {
                    AiReportEntity saved = invocation.getArgument(0);
                    saved.setReportId(UUID.randomUUID());
                    saved.setCreatedAt(OffsetDateTime.now());
                    return saved;
                });

        AiReportDto result = aiReportService.create(request);

        ArgumentCaptor<AiReportEntity> captor = ArgumentCaptor.forClass(AiReportEntity.class);
        verify(aiReportRepository).save(captor.capture());
        assertEquals("월간보고서", captor.getValue().getReportType());
        assertEquals("NORMAL", result.reportData().get("riskLevel"));
    }

    private AiReportEntity reportEntity(boolean isRead) {
        return AiReportEntity.builder()
                .reportId(UUID.randomUUID())
                .title("AI 보고서")
                .reportData("{\"riskLevel\":\"NORMAL\",\"sections\":[]}")
                .reportType("월간보고서")
                .createdAt(OffsetDateTime.parse("2026-08-06T12:00:00+09:00"))
                .carId(UUID.randomUUID())
                .isRead(isRead)
                .build();
    }
}
