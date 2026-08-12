package com.ev_energy_management.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.regex.Pattern;

// 로그인 로그의 "위치" 컬럼용 - ip-api.com(무료, API 키 불필요, 분당 45회 제한)으로 IP를
// 도시/국가로 변환한다. 로컬/사설 IP(로컬 개발, ALB 뒤 내부망 등)는 조회해봤자 항상 실패하니
// 애초에 호출을 건너뛴다. 조회가 실패해도(rate limit, 네트워크 문제 등) 로그인 자체를 막으면
// 안 되므로 예외를 절대 밖으로 던지지 않고 null만 반환한다(fail-open - EmailVerificationService의
// fail-closed와는 다르게, 위치는 로그인 보안 게이트가 아니라 부가 정보라서).
@Service
public class IpGeoLocationService {

    private static final Logger log = LoggerFactory.getLogger(IpGeoLocationService.class);

    private static final Pattern PRIVATE_OR_LOOPBACK = Pattern.compile(
            "^(127\\.|10\\.|172\\.(1[6-9]|2\\d|3[01])\\.|192\\.168\\.|::1|0:0:0:0:0:0:0:1|localhost)"
    );

    private final RestClient restClient;

    public IpGeoLocationService() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(1500);
        requestFactory.setReadTimeout(1500);
        this.restClient = RestClient.builder()
                .baseUrl("http://ip-api.com")
                .requestFactory(requestFactory)
                .build();
    }

    @SuppressWarnings("unchecked")
    public String resolve(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank() || PRIVATE_OR_LOOPBACK.matcher(ipAddress).find()) {
            return null;
        }
        try {
            Map<String, Object> body = restClient.get()
                    .uri("/json/{ip}?fields=status,country,city", ipAddress)
                    .retrieve()
                    .body(Map.class);
            if (body == null || !"success".equals(body.get("status"))) {
                return null;
            }
            String country = (String) body.get("country");
            String city = (String) body.get("city");
            if (country == null && city == null) {
                return null;
            }
            return (country != null ? country : "") + (city != null && !city.isBlank() ? " " + city : "");
        } catch (Exception e) {
            log.warn("IP 위치 조회 실패 (ip={}): {}", ipAddress, e.getMessage());
            return null;
        }
    }
}
