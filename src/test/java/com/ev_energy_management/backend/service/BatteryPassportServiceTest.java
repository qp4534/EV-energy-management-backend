package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.entity.BatteryPassportEntity;
import com.ev_energy_management.backend.repository.BatteryPassportRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BatteryPassportServiceTest {

    @Test
    void findsPassportByExactCarIdWithoutListFallback() {
        BatteryPassportRepository repository = mock(BatteryPassportRepository.class);
        BatteryPassportService service = new BatteryPassportService(repository);
        UUID carId = UUID.fromString("11111111-1111-4111-8111-111111111111");
        UUID batteryId = UUID.fromString("22222222-2222-4222-8222-222222222222");
        BatteryPassportEntity entity = BatteryPassportEntity.builder()
                .batteryId(batteryId)
                .carId(carId)
                .currentTemp(new BigDecimal("18.1"))
                .build();
        when(repository.findByCarId(carId)).thenReturn(Optional.of(entity));

        var result = service.findByCarId(carId);

        assertEquals(batteryId, result.batteryId());
        assertEquals(carId, result.carId());
        verify(repository).findByCarId(carId);
    }
}
