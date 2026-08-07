package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);

    // 전화번호는 가입 시점에 따라 하이픈 유무가 달라질 수 있어(자동 하이픈 포맷팅이 나중에 추가됨)
    // DB 쿼리에서 전화번호까지 정확히 매칭하지 않는다 - AuthService.findEmail에서 숫자만
    // 남긴 뒤 비교한다.
    List<UserEntity> findByNameAndBirthAndRoleAndIsDeletedFalse(String name, LocalDate birth, String role);
}
