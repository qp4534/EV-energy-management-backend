package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.client.FastApiChatClient;
import com.ev_energy_management.backend.dto.chat.ChatMessageRequest;
import com.ev_energy_management.backend.dto.chat.ChatMessageResponse;
import com.ev_energy_management.backend.dto.chat.FastApiChatRequest;
import com.ev_energy_management.backend.exception.InvalidRequestException;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private static final int MAX_MESSAGE_LENGTH = 4_000;
    private static final int MAX_CONVERSATION_ID_LENGTH = 128;

    private final FastApiChatClient fastApiChatClient;
    private final CarAccessService carAccessService;

    public ChatService(FastApiChatClient fastApiChatClient, CarAccessService carAccessService) {
        this.fastApiChatClient = fastApiChatClient;
        this.carAccessService = carAccessService;
    }

    public ChatMessageResponse chat(AuthenticatedUser user, ChatMessageRequest request) {
        if (user == null) {
            throw new AuthenticationCredentialsNotFoundException("인증이 필요합니다.");
        }
        if (request == null || request.message() == null || request.message().isBlank()) {
            throw new InvalidRequestException("message가 필요합니다.");
        }
        String message = request.message().trim();
        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new InvalidRequestException("message는 4000자를 초과할 수 없습니다.");
        }
        String conversationId = normalizeConversationId(request.conversationId());
        if (request.vehicleId() != null) {
            carAccessService.requireOwner(user, request.vehicleId());
        }

        return fastApiChatClient.chat(new FastApiChatRequest(
                user.userId().toString(),
                request.vehicleId() == null ? null : request.vehicleId().toString(),
                message,
                conversationId
        ));
    }

    private String normalizeConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return null;
        }
        String normalized = conversationId.trim();
        if (normalized.length() > MAX_CONVERSATION_ID_LENGTH) {
            throw new InvalidRequestException("conversationId는 128자를 초과할 수 없습니다.");
        }
        return normalized;
    }
}
