package com.ev_energy_management.backend.client;

import com.ev_energy_management.backend.dto.chat.ChatMessageResponse;
import com.ev_energy_management.backend.dto.chat.FastApiChatRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FastApiChatClientTest {

    @Test
    void sendsInternalTokenAndAuthenticatedIdentityContract() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://chatbot");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FastApiChatClient client = new FastApiChatClient(builder.build(), "internal-secret");

        server.expect(once(), requestTo("http://chatbot/v1/chat/messages"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Internal-Token", "internal-secret"))
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.vehicleId").value("car-1"))
                .andExpect(jsonPath("$.message").value("질문"))
                .andRespond(withSuccess("""
                        {
                          "answer":"답변","route":"RAG","safetyLevel":"NORMAL",
                          "dataAsOf":"2026-08-06T12:00:00+09:00",
                          "sources":[],"missingFields":[],
                          "fallbackUsed":false,"metadata":{}
                        }
                        """, MediaType.APPLICATION_JSON));

        ChatMessageResponse response = client.chat(
                new FastApiChatRequest("user-1", "car-1", "질문", null)
        );

        assertEquals("답변", response.answer());
        assertEquals("RAG", response.route());
        assertEquals(
                Instant.parse("2026-08-06T03:00:00Z"),
                response.dataAsOf().toInstant()
        );
        server.verify();
    }
}
