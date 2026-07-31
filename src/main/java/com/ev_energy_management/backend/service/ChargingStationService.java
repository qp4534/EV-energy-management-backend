package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.ChargingStationDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ChargingStationService {

    private List<ChargingStationDto> mockData() {
        return List.of(
                new ChargingStationDto(UUID.randomUUID(), "서울특별시", "서울시 강남구 테헤란로 123",
                        new BigDecimal("37.5012345"), new BigDecimal("127.0398765")),
                new ChargingStationDto(UUID.randomUUID(), "경기도", "경기도 성남시 분당구 판교역로 456",
                        new BigDecimal("37.3947654"), new BigDecimal("127.1112345"))
        );
    }

    public List<ChargingStationDto> findAll() {
        return mockData();
    }

    public ChargingStationDto findById(UUID chargeId) {
        return new ChargingStationDto(chargeId, "서울특별시", "서울시 강남구 테헤란로 123",
                new BigDecimal("37.5012345"), new BigDecimal("127.0398765"));
    }

    public ChargingStationDto create(ChargingStationDto request) {
        return new ChargingStationDto(UUID.randomUUID(), request.region(), request.address(),
                request.latitude(), request.longitude());
    }

    public ChargingStationDto update(UUID chargeId, ChargingStationDto request) {
        return new ChargingStationDto(chargeId, request.region(), request.address(),
                request.latitude(), request.longitude());
    }

    public void delete(UUID chargeId) {
        // TODO: ERD 확정 후 실제 삭제 로직 연결
    }
}
