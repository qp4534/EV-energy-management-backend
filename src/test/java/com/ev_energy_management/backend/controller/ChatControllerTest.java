package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.config.SecurityConfig;
import com.ev_energy_management.backend.dto.chat.ChatMessageResponse;
import com.ev_energy_management.backend.exception.AiServiceUnavailableException;
import com.ev_energy_management.backend.exception.InvalidRequestException;
import com.ev_energy_management.backend.security.JwtAuthenticationFilter;
import com.ev_energy_management.backend.security.JwtTokenProvider;
import com.ev_energy_management.backend.security.TokenBlacklistService;
import com.ev_energy_management.backend.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
@TestPropertySource(properties = {
        "jwt.secret=test-secret-value-for-jwt-signing",
        "jwt.expiration-ms=3600000"
})
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private ChatService chatService;
    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void chatRequiresJwt() throws Exception {
        mockMvc.perform(post("/api/v1/chat/messages")
                        .contentType("application/json")
                        .content("{\"message\":\"안녕\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedChatReturnsFastApiContract() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(userId, "사용자");
        when(chatService.chat(any(), any())).thenReturn(new ChatMessageResponse(
                "안녕하세요", "GENERAL", "NORMAL", null,
                List.of(), List.of(), true, Map.of()
        ));

        mockMvc.perform(post("/api/v1/chat/messages")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"message\":\"안녕\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("안녕하세요"))
                .andExpect(jsonPath("$.route").value("GENERAL"))
                .andExpect(jsonPath("$.fallbackUsed").value(true));
    }

    @Test
    void invalidChatRequestReturnsPublicBadRequestContract() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(userId, "사용자");
        when(chatService.chat(any(), any()))
                .thenThrow(new InvalidRequestException("message가 필요합니다."));

        mockMvc.perform(post("/api/v1/chat/messages")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"message\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    @Test
    void unavailableFastApiReturnsServiceUnavailableWithoutInternalDetails() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(userId, "사용자");
        when(chatService.chat(any(), any()))
                .thenThrow(new AiServiceUnavailableException("현재 챗봇 서비스를 사용할 수 없습니다."));

        mockMvc.perform(post("/api/v1/chat/messages")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"message\":\"안녕\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("AI_SERVICE_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("현재 챗봇 서비스를 사용할 수 없습니다."));
    }
}
