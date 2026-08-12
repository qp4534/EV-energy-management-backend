package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.auth.LoginRequest;
import com.ev_energy_management.backend.dto.auth.LoginResponse;
import com.ev_energy_management.backend.dto.auth.MeResponse;
import com.ev_energy_management.backend.dto.auth.SignupRequest;
import com.ev_energy_management.backend.dto.auth.DeleteAccountRequest;
import com.ev_energy_management.backend.dto.auth.FindEmailRequest;
import com.ev_energy_management.backend.dto.auth.FindEmailResponse;
import com.ev_energy_management.backend.dto.auth.PasswordResetRequest;
import com.ev_energy_management.backend.entity.UserEntity;
import com.ev_energy_management.backend.exception.AccountDeletedException;
import com.ev_energy_management.backend.exception.AccountLockedException;
import com.ev_energy_management.backend.exception.EmailAlreadyExistsException;
import com.ev_energy_management.backend.exception.EmailNotVerifiedException;
import com.ev_energy_management.backend.exception.InvalidCredentialsException;
import com.ev_energy_management.backend.exception.InvalidPasswordException;
import jakarta.persistence.EntityNotFoundException;
import com.ev_energy_management.backend.repository.LoginLogRepository;
import com.ev_energy_management.backend.repository.TermAgreementRepository;
import com.ev_energy_management.backend.repository.UserRepository;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import com.ev_energy_management.backend.security.JwtTokenProvider;
import com.ev_energy_management.backend.security.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private LoginLogRepository loginLogRepository;
    @Mock
    private TermAgreementRepository termAgreementRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private EmailVerificationService emailVerificationService;
    @Mock
    private IpGeoLocationService ipGeoLocationService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository, loginLogRepository, termAgreementRepository,
                passwordEncoder, jwtTokenProvider, tokenBlacklistService, auditLogService,
                emailVerificationService, ipGeoLocationService
        );
    }

    @Test
    void signupHashesPasswordAndWritesTermAgreementsAndAuditLog() {
        UUID userId = UUID.randomUUID();
        SignupRequest request = new SignupRequest(
                "new@user.com", "Raw-password1!", "홍길동", "010-0000-0000",
                LocalDate.of(1990, 1, 1), "관제자", List.of("age", "service")
        );
        when(userRepository.findByEmail("new@user.com")).thenReturn(Optional.empty());
        when(emailVerificationService.isVerified("new@user.com")).thenReturn(true);
        when(passwordEncoder.encode("Raw-password1!")).thenReturn("hashed-password");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setUserId(userId);
            return entity;
        });

        MeResponse response = authService.signup(request);

        assertEquals(userId, response.userId());
        verify(passwordEncoder).encode("Raw-password1!");
        verify(termAgreementRepository, times(2)).save(any());
        verify(auditLogService).log(eq(userId), eq("SIGNUP"), eq("USER"), eq(userId), anyMap());
        verify(emailVerificationService).clearVerified("new@user.com");
    }

    @Test
    void signupRejectsUnverifiedEmail() {
        SignupRequest request = new SignupRequest(
                "unverified@user.com", "Raw-password1!", "홍길동", "010-0000-0000",
                LocalDate.of(1990, 1, 1), "관제자", List.of()
        );
        when(userRepository.findByEmail("unverified@user.com")).thenReturn(Optional.empty());
        when(emailVerificationService.isVerified("unverified@user.com")).thenReturn(false);

        assertThrows(EmailNotVerifiedException.class, () -> authService.signup(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void ensureEmailAvailableRejectsAlreadyRegisteredEmail() {
        when(userRepository.findByEmail("dup@user.com"))
                .thenReturn(Optional.of(UserEntity.builder().userId(UUID.randomUUID()).build()));

        assertThrows(EmailAlreadyExistsException.class, () -> authService.ensureEmailAvailable("dup@user.com"));
    }

    @Test
    void ensureEmailAvailableAllowsUnregisteredEmail() {
        when(userRepository.findByEmail("fresh@user.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authService.ensureEmailAvailable("fresh@user.com"));
    }

    @Test
    void signupRejectsDuplicateEmail() {
        SignupRequest request = new SignupRequest(
                "dup@user.com", "raw-password", "홍길동", "010-0000-0000",
                LocalDate.of(1990, 1, 1), "관제자", List.of()
        );
        when(userRepository.findByEmail("dup@user.com"))
                .thenReturn(Optional.of(UserEntity.builder().userId(UUID.randomUUID()).build()));

        assertThrows(EmailAlreadyExistsException.class, () -> authService.signup(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void signupRejectsPasswordThatFailsPolicy() {
        SignupRequest request = new SignupRequest(
                "weak@user.com", "weakpassword", "홍길동", "010-0000-0000",
                LocalDate.of(1990, 1, 1), "관제자", List.of()
        );
        when(userRepository.findByEmail("weak@user.com")).thenReturn(Optional.empty());

        assertThrows(InvalidPasswordException.class, () -> authService.signup(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginSuccessResetsFailedCountAndIssuesToken() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .userId(userId).email("ok@user.com").passwordHash("hashed")
                .role("관제자").loginFailed(2).isLocked(false).name("홍길동")
                .build();
        when(userRepository.findByEmail("ok@user.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct", "hashed")).thenReturn(true);
        when(jwtTokenProvider.generateToken(userId, "관제자")).thenReturn("jwt-token");

        LoginResponse response = authService.login(new LoginRequest("ok@user.com", "correct"), "1.1.1.1", "test-agent");

        assertEquals("jwt-token", response.token());
        assertEquals(0, user.getLoginFailed());
        verify(loginLogRepository).save(argThat(log -> "SUCCESS".equals(log.getStatus())));
        verify(auditLogService).log(eq(userId), eq("LOGIN"), eq("USER"), eq(userId), eq(null));
    }

    @Test
    void loginFailureIncrementsFailedCount() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .userId(userId).email("bad@user.com").passwordHash("hashed")
                .role("관제자").loginFailed(1).isLocked(false)
                .build();
        when(userRepository.findByEmail("bad@user.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("bad@user.com", "wrong"), "1.1.1.1", "test-agent"));

        assertEquals(2, user.getLoginFailed());
        assertFalse(Boolean.TRUE.equals(user.getIsLocked()));
        verify(loginLogRepository).save(argThat(log -> "FAILED".equals(log.getStatus())));
    }

    @Test
    void fourthConsecutiveFailureWarnsOneAttemptLeft() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .userId(userId).email("almost-locked@user.com").passwordHash("hashed")
                .role("관제자").loginFailed(3).isLocked(false)
                .build();
        when(userRepository.findByEmail("almost-locked@user.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        InvalidCredentialsException thrown = assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("almost-locked@user.com", "wrong"), "1.1.1.1", "test-agent"));

        assertEquals(4, user.getLoginFailed());
        assertFalse(Boolean.TRUE.equals(user.getIsLocked()));
        assertTrue(thrown.getMessage().contains("1회 더 실패하면"));
    }

    @Test
    void fifthConsecutiveFailureLocksAccount() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .userId(userId).email("locking@user.com").passwordHash("hashed")
                .role("관제자").loginFailed(4).isLocked(false)
                .build();
        when(userRepository.findByEmail("locking@user.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(AccountLockedException.class,
                () -> authService.login(new LoginRequest("locking@user.com", "wrong"), "1.1.1.1", "test-agent"));

        assertEquals(5, user.getLoginFailed());
        assertTrue(user.getIsLocked());
        assertNotNull(user.getLockedAt());
    }

    @Test
    void logoutBlacklistsTokenUntilExpiryAndWritesAuditLog() {
        UUID userId = UUID.randomUUID();
        AuthenticatedUser user = new AuthenticatedUser(userId, "관제자");
        String token = "jwt-token";
        when(jwtTokenProvider.getExpiration(token)).thenReturn(Optional.of(Instant.now().plusSeconds(60)));

        authService.logout(user, token);

        verify(tokenBlacklistService).blacklist(eq(token), any(Duration.class));
        verify(auditLogService).log(eq(userId), eq("LOGOUT"), eq("USER"), eq(userId), eq(null));
    }

    @Test
    void lockedAccountRejectedEvenWithCorrectPassword() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .userId(userId).email("locked@user.com").passwordHash("hashed")
                .role("관제자").loginFailed(5).isLocked(true)
                .build();
        when(userRepository.findByEmail("locked@user.com")).thenReturn(Optional.of(user));

        assertThrows(AccountLockedException.class,
                () -> authService.login(new LoginRequest("locked@user.com", "correct"), "1.1.1.1", "test-agent"));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(loginLogRepository).save(argThat(log -> "ACCOUNT_LOCKED".equals(log.getFailReason())));
    }

    @Test
    void deletedAccountRejectedOnLogin() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .userId(userId).email("deleted@user.com").passwordHash("hashed")
                .role("관제자").isDeleted(true)
                .build();
        when(userRepository.findByEmail("deleted@user.com")).thenReturn(Optional.of(user));

        assertThrows(AccountDeletedException.class,
                () -> authService.login(new LoginRequest("deleted@user.com", "correct"), "1.1.1.1", "test-agent"));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(loginLogRepository).save(argThat(log -> "ACCOUNT_DELETED".equals(log.getFailReason())));
    }

    @Test
    void deleteAccountSoftDeletesAndBlacklistsTokenAndWritesAuditLog() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .userId(userId).email("withdraw@user.com").passwordHash("hashed")
                .role("관제자")
                .build();
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, "관제자");
        String token = "jwt-token";
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct", "hashed")).thenReturn(true);
        when(jwtTokenProvider.getExpiration(token)).thenReturn(Optional.of(Instant.now().plusSeconds(60)));

        authService.deleteAccount(authenticatedUser, new DeleteAccountRequest("correct"), token);

        assertTrue(user.getIsDeleted());
        assertNotNull(user.getDeletedAt());
        verify(userRepository).save(user);
        verify(tokenBlacklistService).blacklist(eq(token), any(Duration.class));
        verify(auditLogService).log(eq(userId), eq("ACCOUNT_DELETE"), eq("USER"), eq(userId), eq(null));
    }

    @Test
    void deleteAccountRejectsWrongPassword() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .userId(userId).email("withdraw2@user.com").passwordHash("hashed")
                .role("관제자")
                .build();
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, "관제자");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.deleteAccount(authenticatedUser, new DeleteAccountRequest("wrong"), "jwt-token"));

        assertFalse(Boolean.TRUE.equals(user.getIsDeleted()));
        verify(userRepository, never()).save(any());
        verify(tokenBlacklistService, never()).blacklist(anyString(), any());
    }

    @Test
    void findEmailReturnsNameAndEmailOnExactMatch() {
        UserEntity user = UserEntity.builder()
                .email("found@user.com").name("홍길동").phone("010-1111-2222")
                .birth(LocalDate.of(1990, 1, 1)).role("관제자")
                .build();
        when(userRepository.findByNameAndBirthAndRoleAndIsDeletedFalse(
                "홍길동", LocalDate.of(1990, 1, 1), "관제자"))
                .thenReturn(List.of(user));

        FindEmailResponse response = authService.findEmail(
                new FindEmailRequest("홍길동", "010-1111-2222", LocalDate.of(1990, 1, 1), "관제자"));

        assertEquals("found@user.com", response.email());
        assertEquals("홍길동", response.name());
    }

    @Test
    void findEmailMatchesPhoneRegardlessOfHyphenFormatting() {
        UserEntity user = UserEntity.builder()
                .email("found2@user.com").name("홍길동").phone("01011112222")
                .birth(LocalDate.of(1990, 1, 1)).role("관제자")
                .build();
        when(userRepository.findByNameAndBirthAndRoleAndIsDeletedFalse(
                "홍길동", LocalDate.of(1990, 1, 1), "관제자"))
                .thenReturn(List.of(user));

        FindEmailResponse response = authService.findEmail(
                new FindEmailRequest("홍길동", "010-1111-2222", LocalDate.of(1990, 1, 1), "관제자"));

        assertEquals("found2@user.com", response.email());
    }

    @Test
    void findEmailThrowsWhenNoMatch() {
        when(userRepository.findByNameAndBirthAndRoleAndIsDeletedFalse(any(), any(), any()))
                .thenReturn(List.of());

        assertThrows(EntityNotFoundException.class, () -> authService.findEmail(
                new FindEmailRequest("홍길동", "010-1111-2222", LocalDate.of(1990, 1, 1), "관제자")));
    }

    @Test
    void findEmailThrowsWhenNameAndBirthAndRoleMatchButPhoneDoesNot() {
        UserEntity user = UserEntity.builder()
                .email("found3@user.com").name("홍길동").phone("010-9999-9999")
                .birth(LocalDate.of(1990, 1, 1)).role("관제자")
                .build();
        when(userRepository.findByNameAndBirthAndRoleAndIsDeletedFalse(
                "홍길동", LocalDate.of(1990, 1, 1), "관제자"))
                .thenReturn(List.of(user));

        assertThrows(EntityNotFoundException.class, () -> authService.findEmail(
                new FindEmailRequest("홍길동", "010-1111-2222", LocalDate.of(1990, 1, 1), "관제자")));
    }

    @Test
    void requestPasswordResetSendsCodeForExistingAccount() {
        UserEntity user = UserEntity.builder().email("reset@user.com").isDeleted(false).build();
        when(userRepository.findByEmail("reset@user.com")).thenReturn(Optional.of(user));

        authService.requestPasswordReset("reset@user.com");

        verify(emailVerificationService).sendCode("reset@user.com");
    }

    @Test
    void requestPasswordResetRejectsUnknownEmail() {
        when(userRepository.findByEmail("nobody@user.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> authService.requestPasswordReset("nobody@user.com"));
        verify(emailVerificationService, never()).sendCode(anyString());
    }

    @Test
    void requestPasswordResetRejectsDeletedAccount() {
        UserEntity user = UserEntity.builder().email("gone@user.com").isDeleted(true).build();
        when(userRepository.findByEmail("gone@user.com")).thenReturn(Optional.of(user));

        assertThrows(AccountDeletedException.class, () -> authService.requestPasswordReset("gone@user.com"));
        verify(emailVerificationService, never()).sendCode(anyString());
    }

    @Test
    void resetPasswordUpdatesHashAndUnlocksAccount() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .userId(userId).email("reset2@user.com").passwordHash("old-hash")
                .loginFailed(3).isLocked(true)
                .build();
        when(emailVerificationService.isVerified("reset2@user.com")).thenReturn(true);
        when(userRepository.findByEmail("reset2@user.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("New-password1!")).thenReturn("new-hash");

        authService.resetPassword(new PasswordResetRequest("reset2@user.com", "New-password1!"));

        assertEquals("new-hash", user.getPasswordHash());
        assertEquals(0, user.getLoginFailed());
        assertFalse(user.getIsLocked());
        assertNull(user.getLockedAt());
        verify(emailVerificationService).clearVerified("reset2@user.com");
        verify(auditLogService).log(eq(userId), eq("PASSWORD_RESET"), eq("USER"), eq(userId), eq(null));
    }

    @Test
    void resetPasswordRejectsWhenEmailNotVerified() {
        when(emailVerificationService.isVerified("noverify@user.com")).thenReturn(false);

        assertThrows(EmailNotVerifiedException.class, () -> authService.resetPassword(
                new PasswordResetRequest("noverify@user.com", "New-password1!")));
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void resetPasswordRejectsWeakPassword() {
        when(emailVerificationService.isVerified("weak2@user.com")).thenReturn(true);

        assertThrows(InvalidPasswordException.class, () -> authService.resetPassword(
                new PasswordResetRequest("weak2@user.com", "weakpassword")));
        verify(userRepository, never()).findByEmail(anyString());
    }
}
