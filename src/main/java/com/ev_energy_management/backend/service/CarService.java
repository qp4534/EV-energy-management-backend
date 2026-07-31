package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.CarDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CarService {

    private List<CarDto> mockData() {
        return List.of(
                new CarDto(UUID.randomUUID(), "12가3456", "Ioniq 5", "KMHXX00XXXX000001", UUID.randomUUID()),
                new CarDto(UUID.randomUUID(), "34나5678", "EV6", "KMHXX00XXXX000002", UUID.randomUUID())
        );
    }

    public List<CarDto> findAll() {
        return mockData();
    }

    public CarDto findById(UUID carId) {
        return new CarDto(carId, "12가3456", "Ioniq 5", "KMHXX00XXXX000001", UUID.randomUUID());
    }

    public CarDto create(CarDto request) {
        return new CarDto(UUID.randomUUID(), request.carNumber(), request.model(), request.vin(), request.userId());
    }

    public CarDto update(UUID carId, CarDto request) {
        return new CarDto(carId, request.carNumber(), request.model(), request.vin(), request.userId());
    }

    public void delete(UUID carId) {
        // TODO: ERD 확정 후 실제 삭제 로직 연결
    }
}
