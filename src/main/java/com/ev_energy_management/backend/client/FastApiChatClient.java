package com.ev_energy_management.backend.client;

import com.ev_energy_management.backend.dto.chat.ChatMessageResponse;
import com.ev_energy_management.backend.dto.chat.FastApiChatRequest;
import com.ev_energy_management.backend.exception.AiServiceUnavailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class FastApiChatClient {

    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private final RestClient restClient;
    private final String internalToken;

    @Autowired
    public FastApiChatClient(
            @Value("${ai.chatbot.base-url}") String baseUrl,
            @Value("${ai.internal-token:}") String internalToken,
            @Value("${ai.chatbot.connect-timeout-ms:2000}") int connectTimeoutMs,
            @Value("${ai.chatbot.read-timeout-ms:45000}") int readTimeoutMs
    ) {
        this(buildRestClient(baseUrl, connectTimeoutMs, readTimeoutMs), internalToken);
    }

    FastApiChatClient(RestClient restClient, String internalToken) {
        this.restClient = restClient;
        this.internalToken = internalToken == null ? "" : internalToken.trim();
    }

    public ChatMessageResponse chat(FastApiChatRequest request) {
        try {
            RestClient.RequestBodySpec call = restClient.post()
                    .uri("/v1/chat/messages")
                    .contentType(MediaType.APPLICATION_JSON);
            if (!internalToken.isBlank()) {
                call.header(INTERNAL_TOKEN_HEADER, internalToken);
            }
            ChatMessageResponse response = call.body(request)
                    .retrieve()
                    .body(ChatMessageResponse.class);
            if (response == null) {
                throw new AiServiceUnavailableException("챗봇 응답이 비어 있습니다.");
            }
            return response;
        } catch (AiServiceUnavailableException e) {
            throw e;
        } catch (RestClientException e) {
            throw new AiServiceUnavailableException("현재 챗봇 서비스를 사용할 수 없습니다.", e);
        }
    }

    private static RestClient buildRestClient(
            String baseUrl,
            int connectTimeoutMs,
            int readTimeoutMs
    ) {
        if (connectTimeoutMs <= 0 || readTimeoutMs <= 0) {
            throw new IllegalArgumentException("AI chatbot timeouts must be positive");
        }
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
