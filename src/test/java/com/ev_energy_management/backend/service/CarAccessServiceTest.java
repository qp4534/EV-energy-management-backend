package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.repository.CarRepository;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarAccessServiceTest {

    @Mock
    private CarRepository carRepository;

    @Test
    void ownerIsAllowedAndNonOwnerIsDeniedWithoutRevealingVehicle() {
        CarAccessService service = new CarAccessService(carRepository);
        UUID userId = UUID.randomUUID();
        UUID ownedCar = UUID.randomUUID();
        UUID anotherCar = UUID.randomUUID();
        AuthenticatedUser user = new AuthenticatedUser(userId, "사용자");
        when(carRepository.existsByCarIdAndUserId(ownedCar, userId)).thenReturn(true);
        when(carRepository.existsByCarIdAndUserId(anotherCar, userId)).thenReturn(false);

        assertDoesNotThrow(() -> service.requireOwner(user, ownedCar));
        assertThrows(AccessDeniedException.class, () -> service.requireOwner(user, anotherCar));
    }
}
