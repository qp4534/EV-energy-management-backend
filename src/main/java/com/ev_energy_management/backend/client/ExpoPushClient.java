package com.ev_energy_management.backend.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/** Expo의 호스팅 푸시 발송 서비스(exp.host)를 호출한다. Firebase/APNs를 직접 붙이지 않고
 * Expo push token만 있으면 이 API 하나로 Android/iOS 둘 다 발송할 수 있다. 실패해도 알림
 * 생성 자체(NOTIFICATIONS row, 인앱 배지)는 이미 끝난 뒤라 여기서는 로그만 남기고 예외를
 * 던지지 않는다(최선 노력 - 앱을 안 켜둔 사용자에게 보내는 발송 실패로 다른 기능을 막지 않기 위함). */
@Component
public class ExpoPushClient {

    private static final Logger log = LoggerFactory.getLogger(ExpoPushClient.class);

    private final RestClient restClient;

    public ExpoPushClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(5000);
        this.restClient = RestClient.builder()
                .baseUrl("https://exp.host")
                .requestFactory(requestFactory)
                .build();
    }

    public void send(List<String> expoPushTokens, String title, String body, Map<String, Object> data) {
        if (expoPushTokens.isEmpty()) return;

        List<Map<String, Object>> messages = expoPushTokens.stream()
                .map(token -> Map.<String, Object>of(
                        "to", token,
                        "title", title,
                        "body", body,
                        "data", data == null ? Map.of() : data
                ))
                .toList();

        try {
            restClient.post()
                    .uri("/--/api/v2/push/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(messages)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Expo push 발송 실패 (토큰 {}개): {}", expoPushTokens.size(), e.getMessage());
        }
    }
}
