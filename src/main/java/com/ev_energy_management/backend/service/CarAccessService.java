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

    private static final String ADMIN_ROLE = "관리자";
    private static final String CONTROLLER_ROLE = "관제자";

    private final CarRepository carRepository;

    public CarAccessService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public void requireOwner(AuthenticatedUser user, UUID carId) {
        validate(user, carId);
        if (!carRepository.existsByCarIdAndUserId(carId, user.userId())) {
            // Do not reveal whether another user's vehicle exists.
            throw new AccessDeniedException("vehicle access denied");
        }
    }

    /** Owners see their own car; controllers and admins can inspect registered fleet cars. */
    public void requireChatAccess(AuthenticatedUser user, UUID carId) {
        validate(user, carId);
        if (ADMIN_ROLE.equals(user.role()) || CONTROLLER_ROLE.equals(user.role())) {
            if (!carRepository.existsById(carId)) {
                throw new AccessDeniedException("vehicle access denied");
            }
            return;
        }
        if (!carRepository.existsByCarIdAndUserId(carId, user.userId())) {
            throw new AccessDeniedException("vehicle access denied");
        }
    }

    private void validate(AuthenticatedUser user, UUID carId) {
        if (user == null) {
            throw new AuthenticationCredentialsNotFoundException("인증이 필요합니다.");
        }
        if (carId == null) {
            throw new InvalidRequestException("carId가 필요합니다.");
        }
    }
}
