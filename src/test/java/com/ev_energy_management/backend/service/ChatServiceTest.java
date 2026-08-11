package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.client.FastApiChatClient;
import com.ev_energy_management.backend.dto.chat.ChatMessageRequest;
import com.ev_energy_management.backend.dto.chat.ChatMessageResponse;
import com.ev_energy_management.backend.dto.chat.FastApiChatRequest;
import com.ev_energy_management.backend.exception.InvalidRequestException;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private FastApiChatClient fastApiChatClient;
    @Mock
    private CarAccessService carAccessService;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(fastApiChatClient, carAccessService);
    }

    @Test
    void authenticatedOwnerIsForwardedWithoutTrustingClientUserId() {
        UUID userId = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        AuthenticatedUser user = new AuthenticatedUser(userId, "사용자");
        ChatMessageResponse expected = new ChatMessageResponse(
                "답변", "RAG", "NORMAL", null, List.of(), List.of(), false,
                Map.of()
        );
        when(fastApiChatClient.chat(org.mockito.ArgumentMatchers.any())).thenReturn(expected);

        ChatMessageResponse result = chatService.chat(
                user,
                new ChatMessageRequest(carId, "  충전 방법 알려줘  ", " conversation-1 ")
        );

        assertSame(expected, result);
        verify(carAccessService).requireChatAccess(user, carId);
        ArgumentCaptor<FastApiChatRequest> captor = ArgumentCaptor.forClass(FastApiChatRequest.class);
        verify(fastApiChatClient).chat(captor.capture());
        assertEquals(userId.toString(), captor.getValue().userId());
        assertEquals("사용자", captor.getValue().actorRole());
        assertEquals(carId.toString(), captor.getValue().vehicleId());
        assertEquals("충전 방법 알려줘", captor.getValue().message());
        assertEquals("conversation-1", captor.getValue().conversationId());
    }

    @Test
    void anotherUsersVehicleIsNeverSentToFastApi() {
        UUID carId = UUID.randomUUID();
        AuthenticatedUser user = new AuthenticatedUser(UUID.randomUUID(), "사용자");
        doThrow(new AccessDeniedException("denied"))
                .when(carAccessService).requireChatAccess(user, carId);

        assertThrows(
                AccessDeniedException.class,
                () -> chatService.chat(user, new ChatMessageRequest(carId, "상태", null))
        );
        verify(fastApiChatClient, never()).chat(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void blankMessageIsRejectedBeforeFastApiCall() {
        AuthenticatedUser user = new AuthenticatedUser(UUID.randomUUID(), "사용자");

        assertThrows(
                InvalidRequestException.class,
                () -> chatService.chat(user, new ChatMessageRequest(null, "   ", null))
        );
        verify(fastApiChatClient, never()).chat(org.mockito.ArgumentMatchers.any());
    }
}
