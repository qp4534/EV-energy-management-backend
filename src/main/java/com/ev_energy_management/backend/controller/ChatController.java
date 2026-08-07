package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.chat.ChatMessageRequest;
import com.ev_energy_management.backend.dto.chat.ChatMessageResponse;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import com.ev_energy_management.backend.service.ChatService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/messages")
    public ChatMessageResponse chat(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody ChatMessageRequest request
    ) {
        return chatService.chat(user, request);
    }
}
