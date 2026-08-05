package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.UserDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private List<UserDto> mockData() {
        return List.of(
                new UserDto(UUID.randomUUID(), "user1@example.com", "hashed-password-1", "이용자",
                        LocalDate.of(1995, 3, 12), true, 0, false, "{}"),
                new UserDto(UUID.randomUUID(), "admin@example.com", "hashed-password-2", "관리자",
                        LocalDate.of(1990, 7, 1), true, 0, false, "{\"all\":true}")
        );
    }

    public List<UserDto> findAll() {
        return mockData();
    }

    public UserDto findById(UUID userId) {
        return new UserDto(userId, "user1@example.com", "hashed-password-1", "이용자",
                LocalDate.of(1995, 3, 12), true, 0, false, "{}");
    }

    public UserDto create(UserDto request) {
        return new UserDto(UUID.randomUUID(), request.email(), request.passwordHash(), request.role(),
                request.birth(), request.isAgree(), request.loginFailed(), request.isLocked(), request.permissions());
    }

    public UserDto update(UUID userId, UserDto request) {
        return new UserDto(userId, request.email(), request.passwordHash(), request.role(),
                request.birth(), request.isAgree(), request.loginFailed(), request.isLocked(), request.permissions());
    }

    public void delete(UUID userId) {
        // TODO: ERD 확정 후 실제 삭제 로직 연결
    }
}
