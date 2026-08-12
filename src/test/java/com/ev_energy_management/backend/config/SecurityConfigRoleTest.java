package com.ev_energy_management.backend.config;

import com.ev_energy_management.backend.controller.NoticeAttachmentController;
import com.ev_energy_management.backend.security.JwtAuthenticationFilter;
import com.ev_energy_management.backend.security.JwtTokenProvider;
import com.ev_energy_management.backend.security.TokenBlacklistService;
import com.ev_energy_management.backend.service.NoticeAttachmentService;
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

// 관리자 전용 도메인(여기선 NoticeAttachmentController로 대표)이 실제로 ROLE_ADMIN만 통과시키는지
// 확인. SecurityConfig.securityFilterChain의 hasRole("ADMIN") 규칙 + JwtAuthenticationFilter의
// role→authority 매핑을 함께 검증한다.
// (예전엔 NoticeController로 대표했지만, 공지사항 "조회"는 이용자 앱도 봐야 해서 더 이상
// admin 전용이 아니게 됐다 - NoticeController.getNotices 참고. notice-attachments는 여전히
// admin 전용이라 대신 이걸로 검증한다.)
@WebMvcTest(NoticeAttachmentController.class)
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
    private NoticeAttachmentService noticeAttachmentService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void adminOnlyEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/notice-attachments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminOnlyEndpointWithControllerRoleReturns403() throws Exception {
        String token = jwtTokenProvider.generateToken(UUID.randomUUID(), "관제자");

        mockMvc.perform(get("/api/notice-attachments").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminOnlyEndpointWithAdminRoleReturns200() throws Exception {
        when(noticeAttachmentService.findAll()).thenReturn(List.of());
        String token = jwtTokenProvider.generateToken(UUID.randomUUID(), "관리자");

        mockMvc.perform(get("/api/notice-attachments").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
