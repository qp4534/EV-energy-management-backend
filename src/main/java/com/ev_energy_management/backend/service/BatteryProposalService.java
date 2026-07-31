package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.BatteryProposalDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BatteryProposalService {

    private List<BatteryProposalDto> mockData() {
        return List.of(
                new BatteryProposalDto(UUID.randomUUID(), new BigDecimal("8500000.00"), new BigDecimal("110000.00"),
                        "60~80kWh", "재사용 가능성이 높은 배터리입니다.", "제안가는 시세에 따라 변동될 수 있습니다.",
                        OffsetDateTime.now(), UUID.randomUUID()),
                new BatteryProposalDto(UUID.randomUUID(), new BigDecimal("4200000.00"), new BigDecimal("70000.00"),
                        "40~60kWh", "ESS 재사용에 적합합니다.", "제안가는 시세에 따라 변동될 수 있습니다.",
                        OffsetDateTime.now(), UUID.randomUUID())
        );
    }

    public List<BatteryProposalDto> findAll() {
        return mockData();
    }

    public BatteryProposalDto findById(UUID proposalId) {
        return new BatteryProposalDto(proposalId, new BigDecimal("8500000.00"), new BigDecimal("110000.00"),
                "60~80kWh", "재사용 가능성이 높은 배터리입니다.", "제안가는 시세에 따라 변동될 수 있습니다.",
                OffsetDateTime.now(), UUID.randomUUID());
    }

    public BatteryProposalDto create(BatteryProposalDto request) {
        return new BatteryProposalDto(UUID.randomUUID(), request.totalPrice(), request.pricePerKwh(),
                request.capacityRange(), request.suitabilityReason(), request.noticeText(),
                OffsetDateTime.now(), request.batteryId());
    }

    public BatteryProposalDto update(UUID proposalId, BatteryProposalDto request) {
        return new BatteryProposalDto(proposalId, request.totalPrice(), request.pricePerKwh(),
                request.capacityRange(), request.suitabilityReason(), request.noticeText(),
                request.createdAt(), request.batteryId());
    }

    public void delete(UUID proposalId) {
        // TODO: ERD 확정 후 실제 삭제 로직 연결
    }
}
