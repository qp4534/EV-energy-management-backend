package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.UserDto;
import com.ev_energy_management.backend.dto.dashboard.AccountStatusTrendDto;
import com.ev_energy_management.backend.dto.dashboard.MemberFlowDto;
import com.ev_energy_management.backend.dto.dashboard.UserRoleDistributionDto;
import com.ev_energy_management.backend.dto.statsreport.MemberTrendDto;
import com.ev_energy_management.backend.dto.statsreport.UserSummaryStatsDto;
import com.ev_energy_management.backend.dto.statsreport.UserTypeDistributionDto;
import com.ev_energy_management.backend.entity.UserEntity;
import com.ev_energy_management.backend.repository.UserRepository;
import com.ev_energy_management.backend.security.AuthenticatedUser;
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
    private final EmailVerificationService emailVerificationService;
    private final ActionLogWriter actionLogWriter;

    public UserService(UserRepository userRepository, EmailVerificationService emailVerificationService,
                       ActionLogWriter actionLogWriter) {
        this.userRepository = userRepository;
        this.emailVerificationService = emailVerificationService;
        this.actionLogWriter = actionLogWriter;
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

    public UserDto update(AuthenticatedUser actor, UUID userId, UserDto request) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        String beforeRole = entity.getRole();
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
        UserDto saved = toDto(userRepository.save(entity));

        actionLogWriter.write(
                actor == null ? null : actor.userId(),
                "USER_ROLE_UPDATE",
                "USER",
                userId,
                Map.of("beforeRole", beforeRole == null ? "" : beforeRole,
                        "afterRole", request.role() == null ? "" : request.role())
        );
        return saved;
    }

    public void delete(AuthenticatedUser actor, UUID userId) {
        userRepository.deleteById(userId);
        actionLogWriter.write(
                actor == null ? null : actor.userId(),
                "USER_DELETE",
                "USER",
                userId,
                Map.of()
        );
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

    // 관리자가 특정 회원 대신 재설정 메일을 보내는 기능.
    // AuthService.requestPasswordReset(email)과 똑같이, 이미 있는 EmailVerificationService를
    // 그대로 재사용한다 (메일 발송 로직 자체는 새로 안 만듦).
    public void requestPasswordReset(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        emailVerificationService.sendCode(user.getEmail());
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

    // 유형별 분포 (관리자/관제자/이용자 3종 인원수)
    public List<UserTypeDistributionDto> getUserTypeDistribution() {
        List<UserEntity> users = userRepository.findAll();
        Map<String, Long> counts = users.stream()
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .collect(Collectors.groupingBy(UserEntity::getRole, Collectors.counting()));
        return List.of(
                new UserTypeDistributionDto("관리자", counts.getOrDefault("관리자", 0L)),
                new UserTypeDistributionDto("관제자", counts.getOrDefault("관제자", 0L)),
                new UserTypeDistributionDto("이용자", counts.getOrDefault("이용자", 0L))
        );
    }

    // 월별 가입자 추이 (그 달까지 누적된 전체 회원 수, 최근 7개월)
    public List<MemberTrendDto> getMemberTrend() {
        List<UserEntity> users = userRepository.findAll();
        List<YearMonth> months = lastNMonths(7);

        List<MemberTrendDto> result = new ArrayList<>();
        for (YearMonth ym : months) {
            long total = users.stream()
                    .filter(u -> u.getCreatedAt() != null && !monthOf(u.getCreatedAt()).isAfter(ym))
                    .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                    .count();
            result.add(new MemberTrendDto(monthLabel(ym), total));
        }
        return result;
    }

    // 이용자 탭 상단 요약 카드 3종
    public UserSummaryStatsDto getUserSummaryStats() {
        List<UserEntity> users = userRepository.findAll();
        List<UserEntity> alive = users.stream()
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .toList();

        YearMonth thisMonth = YearMonth.now();
        YearMonth lastMonth = thisMonth.minusMonths(1);

        long totalUsers = alive.size();
        long totalUsersLastMonth = alive.stream()
                .filter(u -> u.getCreatedAt() != null && !monthOf(u.getCreatedAt()).isAfter(lastMonth))
                .count();
        long totalUsersDelta = totalUsers - totalUsersLastMonth;

        long lockedNow = alive.stream().filter(u -> Boolean.TRUE.equals(u.getIsLocked())).count();
        double activeRate = totalUsers == 0 ? 0 : Math.round((double) (totalUsers - lockedNow) / totalUsers * 1000) / 10.0;

        long totalLastMonthAll = totalUsersLastMonth;
        long lockedLastMonth = alive.stream()
                .filter(u -> u.getCreatedAt() != null && !monthOf(u.getCreatedAt()).isAfter(lastMonth))
                .filter(u -> Boolean.TRUE.equals(u.getIsLocked()))
                .count();
        double activeRateLastMonth = totalLastMonthAll == 0 ? 0
                : Math.round((double) (totalLastMonthAll - lockedLastMonth) / totalLastMonthAll * 1000) / 10.0;
        double activeRateDelta = Math.round((activeRate - activeRateLastMonth) * 10) / 10.0;

        List<UserEntity> newThisMonth = alive.stream()
                .filter(u -> u.getCreatedAt() != null && monthOf(u.getCreatedAt()).equals(thisMonth))
                .toList();
        long newUsersGeneral = newThisMonth.stream().filter(u -> "이용자".equals(u.getRole())).count();
        long newUsersController = newThisMonth.stream().filter(u -> "관제자".equals(u.getRole())).count();

        return new UserSummaryStatsDto(
                totalUsers, totalUsersDelta, activeRate, activeRateDelta,
                (long) newThisMonth.size(), newUsersGeneral, newUsersController
        );
    }
}