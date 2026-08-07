package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.AiReportDto;
import com.ev_energy_management.backend.service.AiReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class AiReportControllerTest {

    @Mock
    private AiReportService aiReportService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new AiReportController(aiReportService)).build();
    }

    @Test
    void markAsReadUsesDedicatedEndpointWithoutAReplacementBody() throws Exception {
        UUID reportId = UUID.randomUUID();
        AiReportDto response = new AiReportDto(
                reportId,
                "이상 보고서",
                Map.of(
                        "riskLevel", "WARNING",
                        "sections", List.of(Map.of(
                                "type", "summary",
                                "title", "요약",
                                "content", "내용"
                        ))
                ),
                "이상",
                OffsetDateTime.parse("2026-08-06T12:00:00+09:00"),
                UUID.randomUUID(),
                UUID.randomUUID(),
                true
        );
        when(aiReportService.markAsRead(reportId)).thenReturn(response);

        mockMvc.perform(patch("/api/ai-reports/{reportId}/read", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value(reportId.toString()))
                .andExpect(jsonPath("$.reportType").value("이상"))
                .andExpect(jsonPath("$.reportData.riskLevel").value("WARNING"))
                .andExpect(jsonPath("$.isRead").value(true));

        verify(aiReportService).markAsRead(reportId);
    }
}
