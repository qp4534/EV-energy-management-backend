package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.UserDto;
import com.ev_energy_management.backend.dto.dashboard.AccountStatusTrendDto;
import com.ev_energy_management.backend.dto.dashboard.MemberFlowDto;
import com.ev_energy_management.backend.dto.dashboard.UserRoleDistributionDto;
import com.ev_energy_management.backend.entity.UserEntity;
import com.ev_energy_management.backend.repository.UserRepository;
import com.ev_energy_management.backend.util.MaskingUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
                MaskingUtils.maskEmail(entity.getEmail()),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.getBirth(),
                entity.getIsAgree(),
                entity.getLoginFailed(),
                entity.getIsLocked(),
                entity.getPermissions(),
                MaskingUtils.maskName(entity.getName()),
                MaskingUtils.maskPhone(entity.getPhone()),
                entity.getProfileImageUrl(),
                entity.getCreatedAt(),
                entity.getEmailVerified(),
                entity.getLockedAt()
        );
    }

    public void requestPasswordReset(UUID userId) {
        // TODO: 실제 이메일 발송 로직은 나중에 구현
        System.out.println("[비밀번호 재설정 요청] userId=" + userId);
    }

    // 관리자 메인 "이용자" 카드 - 관리자/관제자 인원수
    public List<UserRoleDistributionDto> getRoleDistribution() {
        List<UserEntity> users = userRepository.findAll();
        Map<String, Long> counts = users.stream()
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .filter(u -> "관리자".equals(u.getRole()) || "관제자".equals(u.getRole()))
                .collect(Collectors.groupingBy(UserEntity::getRole, Collectors.counting()));
        return List.of(
                new UserRoleDistributionDto("관리자", counts.getOrDefault("관리자", 0L)),
                new UserRoleDistributionDto("관제자", counts.getOrDefault("관제자", 0L))
        );
    }

    // 신규 가입자 / 탈퇴자 추이 (최근 7개월, 이번 달 포함)
    public List<MemberFlowDto> getMemberFlow() {
        List<UserEntity> users = userRepository.findAll();
        List<YearMonth> months = lastNMonths(7);

        List<MemberFlowDto> result = new ArrayList<>();
        for (YearMonth ym : months) {
            long joined = users.stream()
                    .filter(u -> u.getCreatedAt() != null && monthOf(u.getCreatedAt()).equals(ym))
                    .count();
            long withdrawn = users.stream()
                    .filter(u -> u.getDeletedAt() != null && monthOf(u.getDeletedAt()).equals(ym))
                    .count();
            result.add(new MemberFlowDto(monthLabel(ym), joined, withdrawn));
        }
        return result;
    }

    // 계정 상태 추이 (최근 7개월, 그 달까지 가입한 회원 기준 정상/잠금 누적 카운트)
    public List<AccountStatusTrendDto> getAccountStatusTrend() {
        List<UserEntity> users = userRepository.findAll();
        List<YearMonth> months = lastNMonths(7);

        List<AccountStatusTrendDto> result = new ArrayList<>();
        for (YearMonth ym : months) {
            List<UserEntity> upToMonth = users.stream()
                    .filter(u -> u.getCreatedAt() != null && !monthOf(u.getCreatedAt()).isAfter(ym))
                    .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                    .toList();
            long locked = upToMonth.stream().filter(u -> Boolean.TRUE.equals(u.getIsLocked())).count();
            long active = upToMonth.size() - locked;
            result.add(new AccountStatusTrendDto(monthLabel(ym), active, locked));
        }
        return result;
    }

    private List<YearMonth> lastNMonths(int n) {
        YearMonth current = YearMonth.now();
        List<YearMonth> months = new ArrayList<>();
        for (int i = n - 1; i >= 0; i--) {
            months.add(current.minusMonths(i));
        }
        return months;
    }

    private YearMonth monthOf(java.time.OffsetDateTime dt) {
        return YearMonth.from(dt.atZoneSameInstant(ZoneOffset.systemDefault()));
    }

    private String monthLabel(YearMonth ym) {
        return ym.getMonthValue() + "월";
    }
}
