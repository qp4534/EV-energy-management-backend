package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.CarDto;
import com.ev_energy_management.backend.dto.dashboard.CarModelDistributionDto;
import com.ev_energy_management.backend.entity.CarEntity;
import com.ev_energy_management.backend.repository.CarRepository;
import com.ev_energy_management.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public CarService(CarRepository carRepository, UserRepository userRepository,
                       AuditLogService auditLogService) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
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
        CarEntity saved = carRepository.save(entity);
        logCarChange(saved, "CAR_CREATE", "신규 등록");
        return toDto(saved);
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
        CarEntity saved = carRepository.save(entity);
        logCarChange(saved, "CAR_UPDATE", "차량 정보 수정");
        return toDto(saved);
    }

    public void delete(UUID carId) {
        CarEntity entity = carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car not found: " + carId));
        // 삭제 전에 로그 남김 - 지운 뒤엔 car_number/소유자를 다시 조회할 수 없음
        logCarChange(entity, "CAR_DELETE", "차량 삭제");
        carRepository.deleteById(carId);
    }

    // 관리자 로그 관리 화면(LogManage.jsx "차량 등록/변경" 탭)이 detail.carNumber/owner/
    // changeType/result를 그대로 읽어서 표에 채운다 - 여기서 그 키 이름에 맞춰서 남긴다.
    // 실행 주체(로그인한 사람)를 따로 추적하는 인증 컨텍스트가 CarController에 없어서,
    // 우선 차량 소유자 본인이 등록/변경한다고 가정하고 owner를 actor로 함께 쓴다.
    private void logCarChange(CarEntity car, String actionType, String changeTypeKo) {
        String ownerName = userRepository.findById(car.getUserId())
                .map(com.ev_energy_management.backend.entity.UserEntity::getName)
                .orElse(null);
        auditLogService.log(car.getUserId(), actionType, "CAR", car.getCarId(), Map.of(
                "carNumber", car.getCarNumber(),
                "owner", ownerName != null ? ownerName : "",
                "changeType", changeTypeKo,
                "result", "success"
        ));
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

    // 관리자 메인 "이용자" 카드 - 차주가 보유한 차량 모델별 분포
    public List<CarModelDistributionDto> getModelDistribution() {
        List<CarEntity> cars = carRepository.findAll();
        Map<String, Long> counts = cars.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        c -> c.getModel() == null ? "미등록" : c.getModel(),
                        java.util.stream.Collectors.counting()));
        return counts.entrySet().stream()
                .map(e -> new CarModelDistributionDto(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.count(), a.count()))
                .toList();
    }
}
