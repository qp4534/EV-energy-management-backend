package com.ev_energy_management.backend.repository;

import com.ev_energy_management.backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    // 탈퇴(is_deleted=true) 계정은 이메일 유니크 제약에서 제외되므로(schema.sql
    // UQ_USER_EMAIL_ACTIVE 파샬 인덱스 참고) 같은 이메일로 활성 계정 1개 + 탈퇴 계정 여러 개가
    // 동시에 존재할 수 있다 - Optional 대신 List로 받아서 서비스 레이어에서 활성 계정을 우선
    // 선택한다(AuthService.findAccountByEmail).
    List<UserEntity> findAllByEmail(String email);

    // 전화번호는 가입 시점에 따라 하이픈 유무가 달라질 수 있어(자동 하이픈 포맷팅이 나중에 추가됨)
    // DB 쿼리에서 전화번호까지 정확히 매칭하지 않는다 - AuthService.findEmail에서 숫자만
    // 남긴 뒤 비교한다.
    List<UserEntity> findByNameAndBirthAndRoleAndIsDeletedFalse(String name, LocalDate birth, String role);

    List<UserEntity> findByRoleAndIsDeletedFalse(String role);
}
