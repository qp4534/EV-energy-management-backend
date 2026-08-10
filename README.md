# EV-energy-management-backend

전기차 배터리/충전 관리 플랫폼의 메인 API 서버. Spring Boot 기반이며, 관리자 웹(frontend-web)과
모바일 앱(frontend-admin) 양쪽이 이 서버를 통해 인증·차량·배터리·충전·알림·매도제안 등의 기능을 사용한다.
AI 추론(배터리 진단, 화재 위험, 충전 수요 예측, 챗봇, PDF 생성)은 이 서버가 직접 계산하지 않고
[EV-energy-management-fastapi](../EV-energy-management-fastapi) 쪽으로 요청을 위임한다.

## 기술 스택

| 구분 | 내용 |
|---|---|
| 프레임워크 | Spring Boot (Java 17, Gradle) |
| DB | PostgreSQL (AWS RDS), Spring Data JPA |
| 캐시/세션 | Redis (ElastiCache) — 로그아웃 토큰 블랙리스트 |
| 인증 | JWT (jjwt) — 로그인/회원가입 공개, 나머지는 Bearer 토큰 필요, 일부는 `ROLE_ADMIN` 필요 |
| 메일 | Spring Mail (Daum SMTP) — 회원가입 이메일 인증코드 발송 |
| 모니터링 | Spring Boot Actuator + Micrometer(Prometheus) — `/actuator/health`, `/actuator/prometheus` |

## 주요 기능 (컨트롤러 기준)

- **인증/회원**: `AuthController`, `UserController` — 회원가입, 로그인, 이메일 인증, 아이디/비번 찾기
- **차량/충전**: `CarController`, `ChargerController`, `ChargingStationController`, `ChargingSessionController`
- **배터리**: `BatteryPassportController`(배터리 여권), `BatteryOfferController`(매입처 제안가),
  `BatteryProposalController`/`BatteryProposalPdfController`(매도 제안서 및 PDF 다운로드),
  `BatteryDiagnosisMetricController`
- **AI 연동**: `AiReportController`(AI 진단 리포트), `ChatController`(챗봇), `TwinFrameController`(디지털 트윈),
  `VehicleRiskOverviewController`, `AnomalyLogController` — 전부 fastapi 서비스 프록시
- **관리자**: `NoticeController`/`NoticeAttachmentController`(공지사항), `LoginLogController`/`ActionLogController`
  (로그 관리), `BatchJobController`/`BatchJobLogController`, `NotificationChannelController`/
  `NotificationMatrixController`, `ExternalIntegrationController`

## 로컬 실행

```bash
./gradlew bootRun
```

필수 환경변수(Kubernetes Secret으로 주입, 하드코딩 금지):

```env
DB_PASSWORD=...
JWT_SECRET=...
MAIL_USERNAME=...          # tkyaho@mijungev.kro.kr
MAIL_APP_PASSWORD=...      # Daum 개인 계정(카카오)에서 발급한 앱 비밀번호
FASTAPI_BASE_URL=...       # 디지털 트윈용 fastapi 주소
RUL_DIAGNOSIS_BASE_URL=... # 배터리 진단용 rul-diagnosis 주소
```

로컬 기본값(`application.properties`)은 `localhost` 기준이라 별도 설정 없이도 기동은 되지만,
DB/Redis/외부 AI 서비스가 실제로 붙어 있지 않으면 해당 기능만 개별적으로 실패한다.

## 배포

GitHub Actions(`.github/workflows/deploy.yml`)가 이미지를 빌드해 Docker Hub에 push하고,
[EV-energy-management-gitops](../EV-energy-management-gitops)의 `apps/backend-eks/deployment.yaml`
이미지 태그를 갱신하면 ArgoCD가 자동으로 EKS에 반영한다.

- 외부(사용자) 경로: `https://www.mijungev.kro.kr/api/*`
- 내부(관리자 전용, VPN 필요) 경로: `https://admin.mijungev.kro.kr/api/*`
- 헬스체크: `/actuator/health/readiness`, `/actuator/health/liveness` (K8s probe), `/actuator/prometheus`(지표)

## 브랜치 규칙

작업은 `dev_nh`(개인 브랜치)에서 커밋 → 빌드 확인 후 `main`에 병합 → CI가 자동 배포.
