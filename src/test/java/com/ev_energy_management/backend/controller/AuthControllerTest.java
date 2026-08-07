package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.config.SecurityConfig;
import com.ev_energy_management.backend.dto.auth.DeleteAccountRequest;
import com.ev_energy_management.backend.dto.auth.FindEmailRequest;
import com.ev_energy_management.backend.dto.auth.FindEmailResponse;
import com.ev_energy_management.backend.dto.auth.LoginRequest;
import com.ev_energy_management.backend.dto.auth.LoginResponse;
import com.ev_energy_management.backend.dto.auth.MeResponse;
import com.ev_energy_management.backend.dto.auth.PasswordResetRequest;
import com.ev_energy_management.backend.dto.auth.SendEmailCodeRequest;
import com.ev_energy_management.backend.dto.auth.SignupRequest;
import com.ev_energy_management.backend.dto.auth.VerifyEmailCodeRequest;
import com.ev_energy_management.backend.exception.EmailAlreadyExistsException;
import com.ev_energy_management.backend.exception.EmailNotVerifiedException;
import com.ev_energy_management.backend.exception.InvalidVerificationCodeException;
import jakarta.persistence.EntityNotFoundException;
import com.ev_energy_management.backend.security.JwtAuthenticationFilter;
import com.ev_energy_management.backend.security.JwtTokenProvider;
import com.ev_energy_management.backend.security.TokenBlacklistService;
import com.ev_energy_management.backend.service.AuthService;
import com.ev_energy_management.backend.service.EmailVerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
@TestPropertySource(properties = {
        "jwt.secret=test-secret-value-for-jwt-signing",
        "jwt.expiration-ms=3600000"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AuthService authService;

    // isBlacklisted()가 기본 false를 반환하도록 목으로 대체 (실제 Redis 연결 없이 슬라이스 테스트).
    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void signupReturns201() throws Exception {
        SignupRequest request = new SignupRequest(
                "new@user.com", "password123", "홍길동", "010-0000-0000",
                LocalDate.of(1990, 1, 1), "관제자", List.of("age", "service")
        );
        UUID userId = UUID.randomUUID();
        when(authService.signup(any())).thenReturn(new MeResponse(
                userId, "new@user.com", "관제자", "홍길동", "010-0000-0000",
                null, LocalDate.of(1990, 1, 1), "{}", true, null
        ));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void loginReturns200() throws Exception {
        LoginRequest request = new LoginRequest("user@user.com", "password123");
        when(authService.login(any(), any(), any())).thenReturn(
                new LoginResponse("jwt-token", "관제자", UUID.randomUUID(), "홍길동"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void meWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutWithValidTokenReturns204() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(userId, "관제자");

        mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void sendEmailCodeReturns204() throws Exception {
        mockMvc.perform(post("/api/auth/email/send-code")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SendEmailCodeRequest("new@user.com"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void sendEmailCodeWithAlreadyRegisteredEmailReturns409() throws Exception {
        doThrow(new EmailAlreadyExistsException("이미 가입된 이메일입니다."))
                .when(authService).ensureEmailAvailable("dup@user.com");

        mockMvc.perform(post("/api/auth/email/send-code")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SendEmailCodeRequest("dup@user.com"))))
                .andExpect(status().isConflict());
    }

    @Test
    void verifyEmailCodeReturns204() throws Exception {
        mockMvc.perform(post("/api/auth/email/verify-code")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new VerifyEmailCodeRequest("new@user.com", "123456"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void verifyEmailCodeWithWrongCodeReturns400() throws Exception {
        doThrow(new InvalidVerificationCodeException("인증번호가 올바르지 않거나 만료되었습니다."))
                .when(emailVerificationService).verifyCode(any(), any());

        mockMvc.perform(post("/api/auth/email/verify-code")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new VerifyEmailCodeRequest("new@user.com", "000000"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteMeWithoutTokenReturns401() throws Exception {
        mockMvc.perform(delete("/api/auth/me")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new DeleteAccountRequest("password"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteMeWithValidTokenReturns204() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(userId, "관제자");

        mockMvc.perform(delete("/api/auth/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new DeleteAccountRequest("password"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void findEmailReturns200() throws Exception {
        FindEmailRequest request = new FindEmailRequest("홍길동", "010-0000-0000", LocalDate.of(1990, 1, 1), "관제자");
        when(authService.findEmail(any())).thenReturn(new FindEmailResponse("홍길동", "found@user.com"));

        mockMvc.perform(post("/api/auth/find-email")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void findEmailWithNoMatchReturns404() throws Exception {
        doThrow(new EntityNotFoundException("일치하는 계정을 찾을 수 없습니다.")).when(authService).findEmail(any());

        mockMvc.perform(post("/api/auth/find-email")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new FindEmailRequest("홍길동", "010-0000-0000", LocalDate.of(1990, 1, 1), "관제자"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void sendPasswordResetCodeReturns204() throws Exception {
        mockMvc.perform(post("/api/auth/password/reset/send-code")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SendEmailCodeRequest("reset@user.com"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void resetPasswordReturns204() throws Exception {
        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new PasswordResetRequest("reset@user.com", "New-password1!"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void resetPasswordWithoutVerificationReturns400() throws Exception {
        doThrow(new EmailNotVerifiedException("이메일 인증을 먼저 완료해주세요.")).when(authService).resetPassword(any());

        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new PasswordResetRequest("reset@user.com", "New-password1!"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void meWithValidTokenReturns200() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(userId, "관제자");
        when(authService.getMe(any())).thenReturn(new MeResponse(
                userId, "user@user.com", "관제자", "홍길동", "010-0000-0000",
                null, LocalDate.of(1990, 1, 1), "{}", true, null
        ));

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
