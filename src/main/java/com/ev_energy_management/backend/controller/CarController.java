package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.CarDto;
import com.ev_energy_management.backend.dto.ImageUploadUrlRequest;
import com.ev_energy_management.backend.dto.ImageUploadUrlResponse;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import com.ev_energy_management.backend.service.CarService;
import com.ev_energy_management.backend.service.S3Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarService carService;
    private final S3Service s3Service;

    public CarController(CarService carService, S3Service s3Service) {
        this.carService = carService;
        this.s3Service = s3Service;
    }

    @GetMapping
    public List<CarDto> getCars(@AuthenticationPrincipal AuthenticatedUser user) {
        return carService.findAll(user);
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

    @PostMapping("/{carId}/image/upload-url")
    public ImageUploadUrlResponse createImageUploadUrl(
            @PathVariable UUID carId,
            @RequestBody ImageUploadUrlRequest request
    ) {
        return s3Service.createCarImageUploadUrl(carId, request.contentType());
    }

    @DeleteMapping("/{carId}")
    public ResponseEntity<Void> deleteCar(@PathVariable UUID carId) {
        carService.delete(carId);
        return ResponseEntity.noContent().build();
    }
}
