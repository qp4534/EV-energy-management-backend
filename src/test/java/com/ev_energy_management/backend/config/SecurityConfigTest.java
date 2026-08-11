package com.ev_energy_management.backend.config;

import com.ev_energy_management.backend.controller.CarController;
import com.ev_energy_management.backend.security.JwtAuthenticationFilter;
import com.ev_energy_management.backend.security.JwtTokenProvider;
import com.ev_energy_management.backend.security.TokenBlacklistService;
import com.ev_energy_management.backend.service.CarService;
import com.ev_energy_management.backend.service.S3Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 도메인 API(여기선 CarController로 대표) 전부가 이제 로그인을 요구하는지 확인하는
// 회귀 테스트. SecurityConfig.securityFilterChain의 anyRequest().authenticated() 정책 검증용.
@WebMvcTest(CarController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
@TestPropertySource(properties = {
        "jwt.secret=test-secret-value-for-jwt-signing",
        "jwt.expiration-ms=3600000"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CarService carService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private S3Service s3Service;

    @Test
    void domainEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/cars"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void domainEndpointWithValidTokenReturns200() throws Exception {
        when(carService.findAll()).thenReturn(List.of());
        String token = jwtTokenProvider.generateToken(UUID.randomUUID(), "관제자");

        mockMvc.perform(get("/api/cars").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
