package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.CarDto;
import com.ev_energy_management.backend.entity.CarEntity;
import com.ev_energy_management.backend.repository.CarRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CarService {

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public List<CarDto> findAll() {
        return carRepository.findAll().stream().map(this::toDto).toList();
    }

    public CarDto findById(UUID carId) {
        return toDto(carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car not found: " + carId)));
    }

    public CarDto create(CarDto request) {
        CarEntity entity = CarEntity.builder()
                .carNumber(request.carNumber())
                .model(request.model())
                .vin(request.vin())
                .nickname(request.nickname())
                .imageUrl(request.imageUrl())
                .isPrimary(request.isPrimary() != null ? request.isPrimary() : false)
                .userId(request.userId())
                .build();
        return toDto(carRepository.save(entity));
    }

    public CarDto update(UUID carId, CarDto request) {
        CarEntity entity = carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car not found: " + carId));
        entity.setCarNumber(request.carNumber());
        entity.setModel(request.model());
        entity.setVin(request.vin());
        entity.setNickname(request.nickname());
        entity.setImageUrl(request.imageUrl());
        entity.setIsPrimary(request.isPrimary());
        entity.setUserId(request.userId());
        return toDto(carRepository.save(entity));
    }

    public void delete(UUID carId) {
        carRepository.deleteById(carId);
    }

    private CarDto toDto(CarEntity entity) {
        return new CarDto(
                entity.getCarId(),
                entity.getCarNumber(),
                entity.getModel(),
                entity.getVin(),
                entity.getNickname(),
                entity.getImageUrl(),
                entity.getIsPrimary(),
                entity.getCreatedAt(),
                entity.getUserId()
        );
    }
}
