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

### 차량 상세 배터리 온도 계약

- `GET /api/battery-passports/car/{carId}`: 차량에 정확히 연결된 정적 여권 정보
- `GET /api/twin-frames/cars/{carId}/latest-measurement?staleAfterSeconds=10`: FastAPI 최신 Twin 측정값

실시간 온도는 두 번째 응답의 `maxCellTemperatureC`이며 `isStale`, `observedAt`을
함께 확인해야 한다. Twin 호출 실패 시 다른 차량의 여권이나
`BATTERY_PASSPORT.current_temp`를 실시간 온도로 대체하지 않는다.

## API 정의서

Base path 없음 (context-path 미설정, 로컬 `http://localhost:8080`, 배포 `https://www.mijungev.kro.kr` / 관리자 전용 `https://admin.mijungev.kro.kr`). Swagger/OpenAPI 문서는 아직 구성되어 있지 않아 이 표가 유일한 API 문서다.

**권한 표기**
- 🔓 공개 — 인증 없이 호출 가능
- 🔑 인증 필요 — 로그인한 사용자면 역할 무관하게 호출 가능 (`Authorization: Bearer <token>`)
- 🛡 관리자 전용 — `role`이 `관리자`(`ROLE_ADMIN`)인 사용자만 호출 가능

인증 실패(401)/권한 부족(403)은 둘 다 `SecurityConfig` 필터 단계에서 `{status, error, message, fieldErrors}` 형태의 고정 JSON으로 즉시 응답한다 (컨트롤러/`GlobalExceptionHandler`까지 도달하지 않음). JWT는 로그인 시 발급되는 4시간짜리 액세스 토큰 하나뿐이며 **리프레시 토큰은 없다** — 로그아웃 시 Redis에 남은 만료시간만큼 블랙리스트에 올라간다.

### 인증 / 회원

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| POST | `/api/auth/signup` | 🔓 | 회원가입 (`email, password, name, phone, birth, role, consentedTerms[]`) |
| POST | `/api/auth/login` | 🔓 | 로그인, JWT 발급 (`{token, role, userId, name}`) — IP/User-Agent를 `LOGIN_LOGS`에 자동 기록 |
| POST | `/api/auth/logout` | 🔑 | 로그아웃, 현재 토큰을 Redis 블랙리스트에 등록 |
| POST | `/api/auth/email/send-code` | 🔓 | 회원가입용 이메일 인증코드 발송 |
| POST | `/api/auth/email/verify-code` | 🔓 | 이메일 인증코드 검증 |
| POST | `/api/auth/password/reset/send-code` | 🔓 | 비밀번호 재설정용 인증코드 발송 |
| POST | `/api/auth/password/reset` | 🔓 | 비밀번호 재설정 (`email, newPassword`) |
| POST | `/api/auth/find-email` | 🔓 | 이름/전화번호/생년월일로 이메일(아이디) 찾기 |
| GET | `/api/auth/me` | 🔑 | 내 프로필 조회 |
| PATCH | `/api/auth/me` | 🔑 | 내 프로필/비밀번호 수정 |
| DELETE | `/api/auth/me` | 🔑 | 회원 탈퇴 (소프트 삭제, `currentPassword` 확인) |
| POST | `/api/auth/me/profile-image/upload-url` | 🔑 | 프로필 사진 업로드용 S3 presigned URL 발급 |
| GET / POST / PUT / DELETE | `/api/users`, `/api/users/{userId}` | 🛡 | 회원 관리 CRUD |
| POST | `/api/users/{userId}/password-reset` | 🛡 | 관리자가 특정 회원 비밀번호 강제 초기화 |

### 차량 / 충전

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| GET / GET `{carId}` / POST / PUT `{carId}` / DELETE `{carId}` | `/api/cars` | 🔑 | 차량 등록/조회/수정/삭제 (본인 차량 기준) |
| POST | `/api/cars/{carId}/image/upload-url` | 🔑 | 차량 사진 업로드용 S3 presigned URL 발급 |
| GET / POST / PUT / DELETE | `/api/chargers`, `/api/chargers/{chargerId}` | 🔑 | 개별 충전기(커넥터) CRUD — 상태/대기열/대기시간 |
| GET / POST / PUT / DELETE | `/api/charging-stations`, `/api/charging-stations/{chargeId}` | 🔑 | 충전소 CRUD — 위치, 급속/완속 대수, 이용 가능 대수 |
| GET / POST / PUT / DELETE | `/api/charging-sessions`, `/api/charging-sessions/{sessionId}` | 🔑 | 충전 세션 CRUD — 시작/종료 시각, SOC 변화 |
| GET | `/api/charging-demand/current?hour&dow&month` | 🔑 | 시간대별 충전 수요 예측 — `charging-demand` FastAPI에 프록시 (`year`는 서버가 모델 학습 최대연도로 고정 치환), 응답은 한글 키(`수요수준`, `설명`, `수요수준_비율`) 그대로 통과 |

### 배터리

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| GET / GET `{batteryId}` / GET `/car/{carId}` / POST / PUT `{batteryId}` / DELETE `{batteryId}` | `/api/battery-passports` | 🔑 | 배터리 여권 CRUD. `car_id`는 CAR와 1:1(unique). **정적 스냅샷**이며 실시간 온도가 아님 — 아래 "차량 상세 배터리 온도 계약" 참고 |
| GET / POST / PUT / DELETE | `/api/battery-offers`, `/api/battery-offers/{offerId}` | 🔑 | 매입처 제안가 CRUD |
| POST | `/api/battery-offers/live-offers` | 🔑 | `{grade, capacityKwh, condition}` 기준 실시간 매입처 탐색 — `rul-diagnosis` FastAPI(`/buyers/live-offers`)에 프록시, 검색 실패 시 정적 목록으로 폴백 |
| GET / POST / PUT / DELETE | `/api/battery-proposals`, `/api/battery-proposals/{proposalId}` | 🔑 | 매도 제안서 CRUD |
| POST | `/api/battery-proposals/pdf` | 🔑 | 매도 제안서 PDF 생성 — `rul-diagnosis` FastAPI(`/report/pdf/full`)에 프록시, `application/pdf` 바이너리 응답 |
| GET / POST / PUT / DELETE | `/api/battery-diagnosis-metrics`, `/api/battery-diagnosis-metrics/{metricId}` | 🔑 | 배터리 진단 세부 점수(잔존수명/방전출력/충전건강도/전압안정성) CRUD |

### 디지털 트윈 / AI 진단 / 챗봇

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| GET / GET `{frameId}` / GET `/car/{carId}` / POST / PUT `{frameId}` / DELETE `{frameId}` | `/api/twin-frames` | 🔑 | 트윈 프레임(위험도 판정 이력) 로컬 CRUD |
| POST | `/api/twin-frames/cars/{carId}/bms-samples` | 🔑 | BMS 원시 샘플(온도/전압/전류) 전송 — `ev_ai_inference_api` FastAPI(`/api/v1/twins/vehicles/{id}/samples`)에 프록시, 위험도 판정 결과를 받아 저장 |
| GET | `/api/twin-frames/cars/{carId}/latest-measurement?staleAfterSeconds=10` | 🔑 | **실시간 온도 조회** — FastAPI 최신 측정값 프록시. `maxCellTemperatureC`가 실시간 온도 값이며 `isStale`/`observedAt`을 반드시 확인해야 함 |
| GET / GET `{reportId}` / POST / PUT `{reportId}` / DELETE `{reportId}` | `/api/ai-reports` | 🔑 | AI 진단 리포트 CRUD (본인 차량 기준) |
| PATCH | `/api/ai-reports/{reportId}/read` | 🔑 | 리포트 읽음 처리 |
| POST | `/api/ai-reports/{reportId}/actions/notify-customer` | 🔑 | 고객에게 알림 발송 액션 트리거 |
| POST | `/api/ai-reports/{reportId}/actions/dispatch-emergency` | 🔑 | 긴급 출동 요청 액션 트리거 |
| GET / POST / PUT / DELETE | `/api/anomaly-logs`, `/api/anomaly-logs/{anomalyId}` | 🔑 | 이상 감지 로그 CRUD — 평소엔 `ev_ai_inference_api`가 위험(주의 이상) 판정 시 직접 적재, 프론트가 POST할 일은 거의 없음 |
| GET | `/api/dashboard/vehicle-risk-overview` | 🔑 | 차량별 위험도 현황 + 일별 위험 건수 요약 |
| POST | `/api/v1/chat/messages` | 🔑 | AI 충전 가이드 챗봇 대화 (`vehicleId, message, conversationId`) — `ev_ai_inference_api` FastAPI(`/v1/chat/messages`)에 프록시. 다른 도메인과 달리 경로가 `/api/v1/...`로 버전 프리픽스가 붙음 |

### 알림

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| GET | `/api/notifications` | 🔑 | 내 알림 목록 (본인 것만 조회) |
| GET | `/api/notifications/{notificationId}` | 🔑 | 내 알림 상세 |
| POST | `/api/notifications` | 🔑 | 알림 생성 (`riskLevel, title, body, carId, reportId`) — `userId`는 토큰에서 서버가 고정 |
| PATCH | `/api/notifications/{notificationId}/read` | 🔑 | 읽음 처리 |

### 공지사항

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| GET | `/api/notices`, `/api/notices/{noticeId}` | 🔑 | 공지 목록/상세 — `targetRole`에 따라 서버에서 노출 필터링 (모든 로그인 사용자가 조회 가능) |
| POST / PUT `/{noticeId}` / DELETE `/{noticeId}` | `/api/notices` | 🛡 | 공지 작성/수정/삭제 |
| GET / POST / PUT / DELETE | `/api/notice-attachments` | 🛡 | 공지 첨부파일 CRUD |

### 대시보드 / 통계

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| GET | `/api/dashboard/car-model-distribution` | 🔑 | 차종별 등록 대수 분포 |
| GET | `/api/dashboard/user-role-distribution` | 🔑 | 역할별 회원 분포 |
| GET | `/api/dashboard/member-flow` | 🔑 | 월별 가입/탈퇴 추이 |
| GET | `/api/dashboard/account-status-trend` | 🔑 | 월별 활성/잠김 계정 추이 |
| GET | `/api/stats-report/user/type-distribution` | 🔑 | 회원 유형 분포 |
| GET | `/api/stats-report/user/member-trend` | 🔑 | 월별 총 회원수 추이 |
| GET | `/api/stats-report/user/summary` | 🔑 | 회원 요약(총원/활성율/이달 신규 등) |
| GET | `/api/stats-report/battery/diagnosis-trend` | 🔑 | 월별 배터리 진단 건수 추이 |
| GET | `/api/stats-report/battery/soh-trend` | 🔑 | 월별 평균 SOH 추이 |
| GET | `/api/stats-report/battery/grade-distribution` | 🔑 | 배터리 등급 분포 |
| GET | `/api/stats-report/battery/metric-average` | 🔑 | 진단 세부 점수 평균 |
| GET | `/api/stats-report/battery/recent-diagnoses?limit=6` | 🔑 | 최근 진단 이력 |
| GET | `/api/stats-report/fire/summary` | 🔑 | 화재 위험 경보 요약 |
| GET | `/api/stats-report/fire/alert-trend?months=6` | 🔑 | 월별 경보 추이 |
| GET | `/api/system/monitor/resource-usage` | 🔑 | 서버 리소스 사용률 |

이 문단(대시보드/통계)은 `SecurityConfig`상 인증만 요구하지만, 실질적으로는 관리자 화면 전용 데이터다.

### 관리자 전용 운영 API

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| GET / POST / PUT / DELETE | `/api/login-logs` | 🛡 | 로그인 이력 조회 (로그인 시 자동 적재) |
| GET / POST / PUT / DELETE | `/api/action-logs` | 🛡 | 관리자 조작 이력(감사 로그) |
| GET / POST / PUT / DELETE | `/api/batch-jobs` | 🛡 | 배치 작업 정의 CRUD |
| POST | `/api/batch-jobs/{jobId}/run` | 🛡 | 배치 작업 수동 실행 |
| GET / POST / PUT / DELETE | `/api/batch-job-logs` | 🛡 | 배치 작업 실행 로그 CRUD |
| GET / POST / PUT / DELETE | `/api/notification-channels` | 🛡 | 알림 발송 채널(SMS/이메일/푸시 등) 관리 |
| GET / POST / PUT / DELETE | `/api/notification-matrix` | 🛡 | 위험도별 알림 채널 라우팅 규칙 관리 |
| GET / POST / PUT / DELETE | `/api/external-integrations` | 🛡 | 외부 연동(API 키 등) 관리 |
| POST | `/api/external-integrations/{integrationId}/reissue` | 🛡 | 연동 API 키 재발급 |

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
