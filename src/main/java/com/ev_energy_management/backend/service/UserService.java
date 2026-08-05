package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.UserDto;
import com.ev_energy_management.backend.entity.UserEntity;
import com.ev_energy_management.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    public UserDto findById(UUID userId) {
        return toDto(userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId)));
    }

    public UserDto create(UserDto request) {
        UserEntity entity = UserEntity.builder()
                .email(request.email())
                .passwordHash(request.passwordHash())
                .role(request.role())
                .birth(request.birth())
                .isAgree(request.isAgree())
                .loginFailed(request.loginFailed())
                .isLocked(request.isLocked())
                .permissions(request.permissions())
                .name(request.name())
                .phone(request.phone())
                .profileImageUrl(request.profileImageUrl())
                .emailVerified(request.emailVerified())
                .build();
        return toDto(userRepository.save(entity));
    }

    public UserDto update(UUID userId, UserDto request) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        entity.setEmail(request.email());
        entity.setPasswordHash(request.passwordHash());
        entity.setRole(request.role());
        entity.setBirth(request.birth());
        entity.setIsAgree(request.isAgree());
        entity.setLoginFailed(request.loginFailed());
        entity.setIsLocked(request.isLocked());
        entity.setPermissions(request.permissions());
        entity.setName(request.name());
        entity.setPhone(request.phone());
        entity.setProfileImageUrl(request.profileImageUrl());
        entity.setEmailVerified(request.emailVerified());
        return toDto(userRepository.save(entity));
    }

    public void delete(UUID userId) {
        userRepository.deleteById(userId);
    }

    private UserDto toDto(UserEntity entity) {
        return new UserDto(
                entity.getUserId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.getBirth(),
                entity.getIsAgree(),
                entity.getLoginFailed(),
                entity.getIsLocked(),
                entity.getPermissions(),
                entity.getName(),
                entity.getPhone(),
                entity.getProfileImageUrl(),
                entity.getCreatedAt(),
                entity.getEmailVerified(),
                entity.getLockedAt()
        );
    }
}
