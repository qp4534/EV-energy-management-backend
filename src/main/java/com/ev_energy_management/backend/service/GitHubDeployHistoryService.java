package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.DeployHistoryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

// "시스템 상태 > 최근 배포 이력" 카드용 - GitHub Actions의 워크플로 실행 이력을 그대로 보여준다.
// public repo라 실행 목록 조회(list workflow runs)는 토큰 없이도 가능하다(실측 확인함,
// 원본 로그 다운로드만 인증이 필요해서 403 - 이 기능은 로그가 아니라 목록만 쓰므로 토큰 불필요).
// 저장소별로 개별 호출하고, 하나가 실패해도(rate limit, 네트워크 등) 나머지는 보여줘야 하므로
// 저장소 단위로 예외를 흡수한다(fail-open, IpGeoLocationService와 같은 방식).
@Service
public class GitHubDeployHistoryService {

    private static final Logger log = LoggerFactory.getLogger(GitHubDeployHistoryService.class);
    private static final String OWNER = "qp4534";

    // gitops는 자체 CI가 없어서(ArgoCD가 감시만 함) 제외
    private static final List<String> REPOS = List.of(
            "EV-energy-management-backend",
            "EV-energy-management-frontend-web",
            "EV-energy-management-frontend-admin",
            "EV-energy-management-fastapi"
    );

    private final RestClient restClient;
    private final int runsPerRepo;

    public GitHubDeployHistoryService(
            @Value("${github.deploy-history.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${github.deploy-history.read-timeout-ms:5000}") int readTimeoutMs,
            @Value("${github.deploy-history.runs-per-repo:5}") int runsPerRepo
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        this.restClient = RestClient.builder()
                .baseUrl("https://api.github.com")
                .requestFactory(requestFactory)
                // GitHub API는 User-Agent 헤더 없으면 요청 자체를 거부한다
                .defaultHeader(HttpHeaders.USER_AGENT, "ev-energy-management-backend")
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .build();
        this.runsPerRepo = runsPerRepo;
    }

    /** 등록된 저장소들의 최근 워크플로 실행을 모아 최신순으로 최대 limit개 반환한다. */
    @SuppressWarnings("unchecked")
    public List<DeployHistoryDto> getRecentDeployHistory(int limit) {
        List<DeployHistoryDto> merged = new ArrayList<>();

        for (String repo : REPOS) {
            try {
                Map<String, Object> body = restClient.get()
                        .uri("/repos/{owner}/{repo}/actions/runs?per_page={n}", OWNER, repo, runsPerRepo)
                        .retrieve()
                        .body(Map.class);
                List<Map<String, Object>> runs = body == null
                        ? null
                        : (List<Map<String, Object>>) body.get("workflow_runs");
                if (runs == null) continue;

                for (Map<String, Object> run : runs) {
                    merged.add(toDto(repo, run));
                }
            } catch (Exception e) {
                // 한 저장소 조회가 실패해도 나머지 저장소는 정상 표시되어야 함
                log.warn("GitHub Actions 실행 이력 조회 실패 (repo={}): {}", repo, e.getMessage());
            }
        }

        return merged.stream()
                .sorted(Comparator.comparing(DeployHistoryDto::deployedAt).reversed())
                .limit(limit)
                .toList();
    }

    private DeployHistoryDto toDto(String repo, Map<String, Object> run) {
        String sha = (String) run.get("head_sha");
        String shortSha = sha != null && sha.length() >= 7 ? sha.substring(0, 7) : sha;
        String displayTitle = (String) run.get("display_title");
        String status = mapStatus((String) run.get("status"), (String) run.get("conclusion"));
        OffsetDateTime deployedAt = OffsetDateTime.parse((String) run.get("created_at"));
        String shortRepoName = repo.replaceFirst("^EV-energy-management-", "");

        return new DeployHistoryDto(
                shortRepoName + "@" + shortSha,
                shortRepoName,
                displayTitle,
                deployedAt,
                status
        );
    }

    private String mapStatus(String status, String conclusion) {
        if (!"completed".equals(status)) {
            return "진행중";
        }
        if ("success".equals(conclusion)) return "성공";
        if ("failure".equals(conclusion)) return "실패";
        if ("cancelled".equals(conclusion)) return "취소";
        return conclusion != null ? conclusion : "알 수 없음";
    }
}
