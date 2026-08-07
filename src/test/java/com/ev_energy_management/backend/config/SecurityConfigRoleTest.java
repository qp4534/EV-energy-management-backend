package com.ev_energy_management.backend.config;

import com.ev_energy_management.backend.controller.NoticeController;
import com.ev_energy_management.backend.security.JwtAuthenticationFilter;
import com.ev_energy_management.backend.security.JwtTokenProvider;
import com.ev_energy_management.backend.security.TokenBlacklistService;
import com.ev_energy_management.backend.service.NoticeService;
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

// 관리자 전용 도메인(여기선 NoticeController로 대표)이 실제로 ROLE_ADMIN만 통과시키는지 확인.
// SecurityConfig.securityFilterChain의 hasRole("ADMIN") 규칙 + JwtAuthenticationFilter의
// role→authority 매핑을 함께 검증한다.
@WebMvcTest(NoticeController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
@TestPropertySource(properties = {
        "jwt.secret=test-secret-value-for-jwt-signing",
        "jwt.expiration-ms=3600000"
})
class SecurityConfigRoleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private NoticeService noticeService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void adminOnlyEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/notices"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminOnlyEndpointWithControllerRoleReturns403() throws Exception {
        String token = jwtTokenProvider.generateToken(UUID.randomUUID(), "관제자");

        mockMvc.perform(get("/api/notices").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminOnlyEndpointWithAdminRoleReturns200() throws Exception {
        when(noticeService.findAll()).thenReturn(List.of());
        String token = jwtTokenProvider.generateToken(UUID.randomUUID(), "관리자");

        mockMvc.perform(get("/api/notices").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
