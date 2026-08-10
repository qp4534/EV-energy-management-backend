package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.auth.DeleteAccountRequest;
import com.ev_energy_management.backend.dto.auth.FindEmailRequest;
import com.ev_energy_management.backend.dto.auth.FindEmailResponse;
import com.ev_energy_management.backend.dto.auth.LoginRequest;
import com.ev_energy_management.backend.dto.auth.LoginResponse;
import com.ev_energy_management.backend.dto.auth.MeResponse;
import com.ev_energy_management.backend.dto.auth.PasswordResetRequest;
import com.ev_energy_management.backend.dto.auth.ProfileUpdateRequest;
import com.ev_energy_management.backend.dto.auth.SignupRequest;
import com.ev_energy_management.backend.entity.LoginLogEntity;
import com.ev_energy_management.backend.entity.TermAgreementEntity;
import com.ev_energy_management.backend.entity.UserEntity;
import com.ev_energy_management.backend.exception.AccountDeletedException;
import com.ev_energy_management.backend.exception.AccountLockedException;
import com.ev_energy_management.backend.exception.EmailAlreadyExistsException;
import com.ev_energy_management.backend.exception.EmailNotVerifiedException;
import com.ev_energy_management.backend.exception.InvalidCredentialsException;
import com.ev_energy_management.backend.exception.InvalidPasswordException;
import com.ev_energy_management.backend.repository.LoginLogRepository;
import com.ev_energy_management.backend.repository.TermAgreementRepository;
import com.ev_energy_management.backend.repository.UserRepository;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import com.ev_energy_management.backend.security.JwtTokenProvider;
import com.ev_energy_management.backend.security.TokenBlacklistService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final String ADMIN_ROLE = "관리자";
    // 8자리 이상 + 대문자/소문자/숫자/특수문자 모두 포함
    private static final Pattern PASSWORD_POLICY =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");
    private static final String PASSWORD_POLICY_MESSAGE =
            "비밀번호는 8자리 이상이며 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다.";

    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
    private final TermAgreementRepository termAgreementRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuditLogService auditLogService;
    private final EmailVerificationService emailVerificationService;

    public AuthService(
            UserRepository userRepository,
            LoginLogRepository loginLogRepository,
            TermAgreementRepository termAgreementRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            TokenBlacklistService tokenBlacklistService,
            AuditLogService auditLogService,
            EmailVerificationService emailVerificationService
    ) {
        this.userRepository = userRepository;
        this.loginLogRepository = loginLogRepository;
        this.termAgreementRepository = termAgreementRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
        this.auditLogService = auditLogService;
        this.emailVerificationService = emailVerificationService;
    }

    // 아이디(이메일) 찾기: 이름+생년월일+역할로 후보를 좁힌 뒤, 전화번호는 숫자만 남겨서
    // 비교한다(가입 시점에 따라 "010-1234-5678"/"01012345678"처럼 하이픈 유무가 달라질 수 있어서
    // DB에 저장된 원본 형식에 의존하지 않기 위함). 탈퇴한 계정은 애초에 후보에서 제외한다.
    public FindEmailResponse findEmail(FindEmailRequest request) {
        String requestedDigits = digitsOnly(request.phone());
        UserEntity user = userRepository
                .findByNameAndBirthAndRoleAndIsDeletedFalse(request.name(), request.birth(), request.role())
                .stream()
                .filter(candidate -> digitsOnly(candidate.getPhone()).equals(requestedDigits))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("일치하는 계정을 찾을 수 없습니다."));
        return new FindEmailResponse(user.getName(), user.getEmail());
    }

    private static String digitsOnly(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    // 비밀번호 재설정 1단계: 계정이 실제로 존재하고 탈퇴하지 않았을 때만 인증코드를 보낸다.
    // 코드 발급/저장/발송 자체는 회원가입 때와 같은 EmailVerificationService를 그대로 재사용한다.
    public void requestPasswordReset(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("가입된 계정을 찾을 수 없습니다."));
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new AccountDeletedException("탈퇴한 계정입니다.");
        }
        emailVerificationService.sendCode(email);
    }

    // 비밀번호 재설정 2단계: /api/auth/email/verify-code로 인증을 마쳐야만 성공한다
    // (isVerified 플래그가 곧 "본인 확인 완료" 증표). 성공하면 로그인 실패 잠금도 같이 풀어준다
    // - 비번을 잊어서 여러 번 틀려 잠긴 계정이 이메일 인증으로 복구하는 흐름을 자연스럽게 포함.
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        if (!emailVerificationService.isVerified(request.email())) {
            throw new EmailNotVerifiedException("이메일 인증을 먼저 완료해주세요.");
        }
        if (!PASSWORD_POLICY.matcher(request.newPassword()).matches()) {
            throw new InvalidPasswordException(PASSWORD_POLICY_MESSAGE);
        }
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new EntityNotFoundException("가입된 계정을 찾을 수 없습니다."));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setLoginFailed(0);
        user.setIsLocked(false);
        user.setLockedAt(null);
        userRepository.save(user);

        emailVerificationService.clearVerified(request.email());
        auditLogService.log(user.getUserId(), "PASSWORD_RESET", "USER", user.getUserId(), null);
    }

    // 이메일 인증코드 발송 전에도 같은 규칙으로 미리 막아서, 사용자가 인증까지 다 끝낸
    // 뒤에야 "이미 가입된 이메일"이라는 걸 알게 되는 일이 없게 한다.
    public void ensureEmailAvailable(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException("이미 가입된 이메일입니다.");
        }
    }

    @Transactional
    public MeResponse signup(SignupRequest request) {
        ensureEmailAvailable(request.email());
        if (!PASSWORD_POLICY.matcher(request.password()).matches()) {
            throw new InvalidPasswordException(PASSWORD_POLICY_MESSAGE);
        }
        if (!emailVerificationService.isVerified(request.email())) {
            throw new EmailNotVerifiedException("이메일 인증을 먼저 완료해주세요.");
        }

        UserEntity entity = UserEntity.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .birth(request.birth())
                .isAgree(true)
                .loginFailed(0)
                .isLocked(false)
                .permissions(defaultPermissionsJson(request.role()))
                .name(request.name())
                .phone(request.phone())
                .emailVerified(true)
                .pushEnabled(true)
                .build();
        UserEntity saved = userRepository.save(entity);

        if (request.consentedTerms() != null) {
            for (String termKey : request.consentedTerms()) {
                termAgreementRepository.save(TermAgreementEntity.builder()
                        .termKey(termKey)
                        .userId(saved.getUserId())
                        .build());
            }
        }

        auditLogService.log(saved.getUserId(), "SIGNUP", "USER", saved.getUserId(), Map.of("role", saved.getRole()));
        emailVerificationService.clearVerified(saved.getEmail());

        return toMeResponse(saved);
    }

    // 실패 시 던지는 InvalidCredentialsException/AccountLockedException은 둘 다 RuntimeException이라
    // 기본 설정이면 Spring이 트랜잭션을 통째로 롤백한다 - 그러면 예외를 던지기 직전에 기록한
    // loginFailed 증가/계정 잠금/로그인 로그가 전부 취소되어 버린다(로그인 실패 추적 기능이
    // 무의미해짐). noRollbackFor로 이 두 예외는 롤백 대상에서 제외해서, "실패 기록은 남기고
    // 클라이언트에는 실패로 응답한다"가 실제로 같이 성립하게 한다.
    @Transactional(noRollbackFor = {InvalidCredentialsException.class, AccountLockedException.class})
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            writeLoginLog(user.getUserId(), ipAddress, userAgent, "FAILED", "ACCOUNT_DELETED");
            throw new AccountDeletedException("탈퇴한 계정입니다.");
        }

        if (Boolean.TRUE.equals(user.getIsLocked())) {
            writeLoginLog(user.getUserId(), ipAddress, userAgent, "FAILED", "ACCOUNT_LOCKED");
            throw new AccountLockedException("5회 이상 로그인에 실패해 계정이 잠겼습니다. 관리자에게 문의해주세요.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            int failedCount = user.getLoginFailed() == null ? 1 : user.getLoginFailed() + 1;
            user.setLoginFailed(failedCount);
            boolean justLocked = failedCount >= MAX_LOGIN_ATTEMPTS;
            String failReason = "BAD_CREDENTIALS";
            if (justLocked) {
                user.setIsLocked(true);
                user.setLockedAt(OffsetDateTime.now());
                failReason = "ACCOUNT_LOCKED";
            }
            userRepository.save(user);
            writeLoginLog(user.getUserId(), ipAddress, userAgent, "FAILED", failReason);
            // 잠기게 만든 바로 그 시도에도 일반 오류만 던지면, 사용자는 다음 시도에서야
            // (isLocked 체크에 걸려서) 잠긴 걸 알게 된다. 어차피 다음 시도에서 드러날
            // 정보라 여기서 바로 알려줘도 계정 존재 여부가 추가로 새지는 않는다.
            if (justLocked) {
                throw new AccountLockedException("5회 이상 로그인에 실패해 계정이 잠겼습니다. 관리자에게 문의해주세요.");
            }
            // 잠기기 바로 1번 전(마지막 기회)에만 경고를 얹는다. 이 시점부터는 다음 실패로
            // 계정이 잠긴다는 게 이미 확정적이라(어느 이메일이든 동일 로직) 추가로 새는
            // 정보가 크지 않다. 그 전 시도들은 여전히 완전히 동일한 문구만 준다.
            if (failedCount == MAX_LOGIN_ATTEMPTS - 1) {
                throw new InvalidCredentialsException(
                        "이메일 또는 비밀번호가 올바르지 않습니다. 1회 더 실패하면 계정이 잠깁니다.");
            }
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (user.getLoginFailed() != null && user.getLoginFailed() > 0) {
            user.setLoginFailed(0);
            userRepository.save(user);
        }
        writeLoginLog(user.getUserId(), ipAddress, userAgent, "SUCCESS", null);
        auditLogService.log(user.getUserId(), "LOGIN", "USER", user.getUserId(), null);

        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getRole());
        return new LoginResponse(token, user.getRole(), user.getUserId(), user.getName());
    }

    public void logout(AuthenticatedUser authenticatedUser, String token) {
        jwtTokenProvider.getExpiration(token)
                .ifPresent(expiry -> tokenBlacklistService.blacklist(token, Duration.between(Instant.now(), expiry)));
        auditLogService.log(authenticatedUser.userId(), "LOGOUT", "USER", authenticatedUser.userId(), null);
    }

    // 소프트 삭제: row는 남기고 is_deleted만 세운다. 지금 쓰던 토큰은 로그아웃과 동일하게
    // 블랙리스트에 올려 바로 무효화하고, 다른 기기의 세션은 findUser()의 is_deleted 체크로
    // /me 계열 호출 시 자연스럽게 막힌다(자연 만료 전까지는 완전히 죽진 않지만, 그 사이엔
    // 조회/수정 자체가 막힘).
    @Transactional
    public void deleteAccount(AuthenticatedUser authenticatedUser, DeleteAccountRequest request, String token) {
        UserEntity user = findUser(authenticatedUser.userId());
        if (request.currentPassword() == null
                || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("비밀번호가 일치하지 않습니다.");
        }
        user.setIsDeleted(true);
        user.setDeletedAt(OffsetDateTime.now());
        userRepository.save(user);

        jwtTokenProvider.getExpiration(token)
                .ifPresent(expiry -> tokenBlacklistService.blacklist(token, Duration.between(Instant.now(), expiry)));
        auditLogService.log(authenticatedUser.userId(), "ACCOUNT_DELETE", "USER", authenticatedUser.userId(), null);
    }

    public MeResponse getMe(AuthenticatedUser authenticatedUser) {
        return toMeResponse(findUser(authenticatedUser.userId()));
    }

    @Transactional
    public MeResponse updateProfile(AuthenticatedUser authenticatedUser, ProfileUpdateRequest request) {
        UserEntity user = findUser(authenticatedUser.userId());
        List<String> changedFields = new ArrayList<>();

        if (request.name() != null && !request.name().equals(user.getName())) {
            user.setName(request.name());
            changedFields.add("name");
        }
        if (request.phone() != null && !request.phone().equals(user.getPhone())) {
            user.setPhone(request.phone());
            changedFields.add("phone");
        }
        if (request.profileImageUrl() != null && !request.profileImageUrl().equals(user.getProfileImageUrl())) {
            user.setProfileImageUrl(request.profileImageUrl());
            changedFields.add("profileImageUrl");
        }
        if (request.pushEnabled() != null && !request.pushEnabled().equals(user.getPushEnabled())) {
            user.setPushEnabled(request.pushEnabled());
            changedFields.add("pushEnabled");
        }
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            if (!PASSWORD_POLICY.matcher(request.newPassword()).matches()) {
                throw new InvalidPasswordException(PASSWORD_POLICY_MESSAGE);
            }
            if (request.currentPassword() == null
                    || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                throw new InvalidCredentialsException("현재 비밀번호가 일치하지 않습니다.");
            }
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
            changedFields.add("password");
        }

        UserEntity saved = userRepository.save(user);

        if (!changedFields.isEmpty()) {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("fields", changedFields);
            auditLogService.log(authenticatedUser.userId(), "PROFILE_UPDATE", "USER", authenticatedUser.userId(), detail);
        }

        return toMeResponse(saved);
    }

    private UserEntity findUser(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new EntityNotFoundException("User not found: " + userId);
        }
        return user;
    }

    private void writeLoginLog(UUID userId, String ipAddress, String userAgent, String status, String failReason) {
        LoginLogEntity log = LoginLogEntity.builder()
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .status(status)
                .userId(userId)
                .failReason(failReason)
                .build();
        loginLogRepository.save(log);
    }

    private String defaultPermissionsJson(String role) {
        boolean isAdmin = ADMIN_ROLE.equals(role);
        return "{"
                + "\"dashboard_view\":true,"
                + "\"fire_alert\":true,"
                + "\"battery_diag_view\":true,"
                + "\"battery_grade_manage\":true,"
                + "\"report_download\":true,"
                + "\"user_manage\":" + isAdmin + ","
                + "\"system_setting\":" + isAdmin
                + "}";
    }

    private MeResponse toMeResponse(UserEntity entity) {
        return new MeResponse(
                entity.getUserId(),
                entity.getEmail(),
                entity.getRole(),
                entity.getName(),
                entity.getPhone(),
                entity.getProfileImageUrl(),
                entity.getPushEnabled(),
                entity.getBirth(),
                entity.getPermissions(),
                entity.getEmailVerified(),
                entity.getCreatedAt()
        );
    }
}
