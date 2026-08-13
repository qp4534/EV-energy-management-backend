package com.ev_energy_management.backend.dto;

import java.time.OffsetDateTime;

// "최근 배포 이력" 카드용 - GitHub Actions 워크플로 실행 1건을 화면에 맞게 축약한 것.
// version은 "저장소@커밋SHA(7자리)", desc는 커밋 메시지(display_title), status는 성공/실패/진행중.
public record DeployHistoryDto(
        String version,
        String repo,
        String desc,
        OffsetDateTime deployedAt,
        String status
) {}
