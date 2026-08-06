package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.exception.InvalidRequestException;
import com.ev_energy_management.backend.repository.CarRepository;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CarAccessService {

    private final CarRepository carRepository;

    public CarAccessService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public void requireOwner(AuthenticatedUser user, UUID carId) {
        if (user == null) {
            throw new AuthenticationCredentialsNotFoundException("인증이 필요합니다.");
        }
        if (carId == null) {
            throw new InvalidRequestException("carId가 필요합니다.");
        }
        if (!carRepository.existsByCarIdAndUserId(carId, user.userId())) {
            // Do not reveal whether another user's vehicle exists.
            throw new AccessDeniedException("vehicle access denied");
        }
    }
}
