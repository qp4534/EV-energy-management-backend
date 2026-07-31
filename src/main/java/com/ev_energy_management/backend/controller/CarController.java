package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.CarDto;
import com.ev_energy_management.backend.service.CarService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping
    public List<CarDto> getCars() {
        return carService.findAll();
    }

    @GetMapping("/{carId}")
    public CarDto getCar(@PathVariable UUID carId) {
        return carService.findById(carId);
    }

    @PostMapping
    public ResponseEntity<CarDto> createCar(@RequestBody CarDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carService.create(request));
    }

    @PutMapping("/{carId}")
    public CarDto updateCar(@PathVariable UUID carId, @RequestBody CarDto request) {
        return carService.update(carId, request);
    }

    @DeleteMapping("/{carId}")
    public ResponseEntity<Void> deleteCar(@PathVariable UUID carId) {
        carService.delete(carId);
        return ResponseEntity.noContent().build();
    }
}
