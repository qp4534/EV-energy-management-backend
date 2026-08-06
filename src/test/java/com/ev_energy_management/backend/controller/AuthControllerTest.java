package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.config.SecurityConfig;
import com.ev_energy_management.backend.dto.auth.LoginRequest;
import com.ev_energy_management.backend.dto.auth.LoginResponse;
import com.ev_energy_management.backend.dto.auth.MeResponse;
import com.ev_energy_management.backend.dto.auth.SignupRequest;
import com.ev_energy_management.backend.security.JwtAuthenticationFilter;
import com.ev_energy_management.backend.security.JwtTokenProvider;
import com.ev_energy_management.backend.service.AuthService;
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
