package com.ev_energy_management.backend.client;

import com.ev_energy_management.backend.exception.AiServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** charging_demand FastAPI 서비스(fastapi_app.py)를 호출한다. ClusterIP라서 앱이 직접 못
 * 붙고 backend가 대신 호출해서 넘겨준다(지도 화면 "지금 시간대 충전 수요" 배지 전용).
 *
 * 모델이 팔로알토 실측 데이터(2020년 이전)로 학습되어 있어 그 범위 밖 연도는 400을 던진다.
 * /health로 학습 연도 범위를 한 번 받아 캐싱해두고, 실제 연도 대신 학습 범위의 최신 연도로
 * 대체해서 보낸다(요일·시간대 패턴만 참고하는 용도라 연도 자체는 의미가 없다). */
@Component
public class ChargingDemandClient {

    private final RestClient restClient;
    private volatile int[] cachedYearRange; // [lo, hi] - null이면 아직 조회 전

    public ChargingDemandClient(
            @Value("${charging.demand.base-url}") String baseUrl,
            @Value("${charging.demand.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${charging.demand.read-timeout-ms:5000}") int readTimeoutMs
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @SuppressWarnings("unchecked")
    private int[] getTrainedYearRange() {
        int[] cached = cachedYearRange;
        if (cached != null) return cached;

        Map<String, Object> health;
        try {
            health = restClient.get().uri("/health").retrieve().body(Map.class);
        } catch (RestClientException e) {
            throw new AiServiceUnavailableException("현재 충전 수요 예측 서비스를 사용할 수 없습니다.", e);
        }
        List<Number> range = health == null ? null : (List<Number>) health.get("학습_연도범위");
        if (range == null || range.size() < 2) {
            throw new AiServiceUnavailableException("충전 수요 예측 서비스 상태를 확인할 수 없습니다.");
        }
        int[] result = { range.get(0).intValue(), range.get(1).intValue() };
        cachedYearRange = result;
        return result;
    }

    /** 지금 시간대(hour/dow/month)의 충전 수요 수준을 예측한다. dow는 0=월 ~ 6=일. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> predict(int hour, int dow, int month) {
        int[] yearRange = getTrainedYearRange();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("hour", hour);
        body.put("dow", dow);
        body.put("month", month);
        body.put("year", yearRange[1]);

        try {
            Map<String, Object> resp = restClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            if (resp == null) {
                throw new AiServiceUnavailableException("충전 수요 예측 응답이 비어 있습니다.");
            }
            return resp;
        } catch (AiServiceUnavailableException e) {
            throw e;
        } catch (RestClientException e) {
            throw new AiServiceUnavailableException("현재 충전 수요 예측 서비스를 사용할 수 없습니다.", e);
        }
    }
}
