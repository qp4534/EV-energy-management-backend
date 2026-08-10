-- Generated from ERDCloud export (미정이 erd (1).sql), PostgreSQL-runnable version.
-- Fixes applied vs. the raw export:
--   1. Inline ENUM(...) columns -> VARCHAR. Postgres native enum types fight JPA/Hibernate
--      parameter binding (pgjdbc won't implicitly cast a bound VARCHAR param to a custom
--      enum type), and the value lists are still being negotiated with the AI team, so a
--      DB-level enum would mean an ALTER TYPE every time a value changes. Valid values are
--      documented via COMMENT ON COLUMN and enforced with Java enums at the app layer instead.
--   2. NOTIFICATIONS.risk_level had no value list in the export -> documented from its own comment
--   3. AI_REPORTS.report_type uses the production values '월간보고서' and '이상'.
--   4. BATTERY_PASSPORT.battery_level mixed unquoted numbers with a quoted string -> documented as '1','2','3','미등록'
--   5. Added UNIQUE on BATTERY_PASSPORT.car_id (1 car : 1 battery, per ERD diagram)

CREATE TABLE "NOTIFICATION_CHANNELS" (
	"channel_id"	UUID		NOT NULL,
	"channel_name"	VARCHAR(50)		NOT NULL,
	"is_active"	BOOLEAN	DEFAULT TRUE	NOT NULL,
	"updated_at"	TIMESTAMPTZ	DEFAULT CURRENT_TIMESTAMP	NULL
);

CREATE TABLE "ANOMALY_LOGS" (
	"anomaly_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"abnormal_type"	VARCHAR(30)		NOT NULL,
	"source_type"	VARCHAR(20)		NOT NULL,
	"trigger_value"	VARCHAR(50)		NULL,
	"detected_at"	TIMESTAMPTZ	DEFAULT CURRENT_TIMESTAMP	NULL,
	"risk_level"	VARCHAR(10)	DEFAULT '정상'	NOT NULL,
	"car_id"	UUID		NULL,
	"session_id"	UUID		NULL
);

COMMENT ON COLUMN "ANOMALY_LOGS"."abnormal_type" IS '화재 위험, 온도 상승, 정상 등';
COMMENT ON COLUMN "ANOMALY_LOGS"."risk_level" IS '긴급, 경고, 주의, 정상';

CREATE TABLE "BATTERY_SOH_HISTORY" (
	"history_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"soh_score"	NUMERIC(4, 1)		NOT NULL,
	"recorded_at"	TIMESTAMPTZ	DEFAULT CURRENT_TIMESTAMP	NOT NULL,
	"battery_id"	UUID		NOT NULL
);

CREATE TABLE "LOGIN_LOGS" (
	"log_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"ip_address"	VARCHAR(45)		NOT NULL,
	"user_agent"	VARCHAR(255)		NOT NULL,
	"location"	VARCHAR(100)		NULL,
	"status"	VARCHAR(10)	DEFAULT 'SUCCESS'	NOT NULL,
	"created_at"	TIMESTAMPTZ	DEFAULT CURRENT_TIMESTAMP	NULL,
	"user_id"	UUID		NOT NULL,
	"fail_reason"	VARCHAR(100)		NULL
);

COMMENT ON COLUMN "LOGIN_LOGS"."status" IS 'SUCCESS, FAILED';
COMMENT ON COLUMN "LOGIN_LOGS"."fail_reason" IS '5회 이상 실패로 계정이 잠김';

CREATE TABLE "BATCH_JOBS" (
	"job_id"	VARCHAR(50)		NOT NULL,
	"job_name"	VARCHAR(100)		NOT NULL,
	"cycle"	VARCHAR(50)		NULL,
	"status"	VARCHAR(10)	DEFAULT '정상'	NOT NULL,
	"last_run_at"	TIMESTAMPTZ		NULL,
	"next_run_at"	TIMESTAMPTZ		NULL
);

COMMENT ON COLUMN "BATCH_JOBS"."status" IS '오류, 정상';

CREATE TABLE "BATTERY_DIAGNOSIS_METRICS" (
	"metric_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"remaining_life_score"	INT		NULL,
	"discharge_power_score"	INT		NULL,
	"charge_health_score"	INT		NULL,
	"voltage_stability_score"	INT		NULL,
	"diagnosed_at"	TIMESTAMPTZ	DEFAULT CURRENT_TIMESTAMP	NULL,
	"battery_id"	UUID		NOT NULL
);

CREATE TABLE "TWIN_FRAMES" (
	"frame_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"observed_at"	TIMESTAMPTZ		NOT NULL,
	"hotspot_cell_index"	SMALLINT		NULL,
	"hotspot_connector_index"	SMALLINT		NULL,
	"ml_risk_level"	SMALLINT		NULL,
	"physics_risk_level"	SMALLINT		NULL,
	"final_risk_level"	SMALLINT		NULL,
	"image_risk_level"	SMALLINT		NULL,
	"image_confidence"	REAL		NULL,
	"raw_metrics"	JSONB		NULL,
	"model_input"	JSONB		NULL,
	"anomaly_id"	UUID		NULL,
	"car_id"	UUID		NOT NULL,
	"session_id"	UUID		NULL,
	"source_image_ref"	VARCHAR(500)		NULL
);

COMMENT ON COLUMN "TWIN_FRAMES"."ml_risk_level" IS '머신러닝이 판단한 위험도';
COMMENT ON COLUMN "TWIN_FRAMES"."physics_risk_level" IS '단순 물리적인 정보(온도 등)으로 판단한 위험도';
COMMENT ON COLUMN "TWIN_FRAMES"."image_confidence" IS 'image_risk_level에 관한 정확도';
COMMENT ON COLUMN "TWIN_FRAMES"."raw_metrics" IS '3D 렌더링용 원본 배열 데이터, 웹이 통째로 읽어서 씀';
COMMENT ON COLUMN "TWIN_FRAMES"."model_input" IS 'BMS 화재·안전 분류 모델에 실제 전달된 입력값';
COMMENT ON COLUMN "TWIN_FRAMES"."source_image_ref" IS 'AI 분석에 쓰인 원본 열화상 사진의 경로';

CREATE TABLE "NOTIFICATIONS" (
	"notification_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"risk_level"	VARCHAR(10)		NOT NULL,
	"title"	VARCHAR(100)		NOT NULL,
	"body"	TEXT		NULL,
	"is_read"	BOOLEAN	DEFAULT FALSE	NOT NULL,
	"created_at"	TIMESTAMPTZ	DEFAULT CURRENT_TIMESTAMP	NOT NULL,
	"user_id"	UUID		NOT NULL,
	"car_id"	UUID		NULL,
	"report_id"	UUID		NULL
);

COMMENT ON COLUMN "NOTIFICATIONS"."risk_level" IS '긴급, 경고, 주의, 정상';

CREATE TABLE "CHARGING_SESSION" (
	"session_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"start_time"	TIMESTAMPTZ		NULL,
	"end_time"	TIMESTAMPTZ		NULL,
	"change_state"	VARCHAR(10)	DEFAULT '대기중'	NOT NULL,
	"car_id"	UUID		NOT NULL,
	"charger_id"	UUID		NOT NULL,
	"start_soc"	NUMERIC(4,1)		NULL,
	"end_soc"	NUMERIC(4,1)		NULL
);

COMMENT ON COLUMN "CHARGING_SESSION"."start_time" IS '주차를 했으나 충전을 안 할 경우를 대비해 NULL';
COMMENT ON COLUMN "CHARGING_SESSION"."change_state" IS '충전중, 중단됨, 충전완료, 대기중(주차중)';

CREATE TABLE "NOTIFICATION_MATRIX" (
	"matrix_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"risk_level"	VARCHAR(10)	DEFAULT '정상'	NOT NULL,
	"is_enabled"	BOOLEAN		NOT NULL,
	"channel_id"	UUID		NOT NULL
);

COMMENT ON COLUMN "NOTIFICATION_MATRIX"."risk_level" IS '긴급, 경고, 주의, 정상';

CREATE TABLE "ACTION_LOGS" (
	"action_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"action_type"	VARCHAR(50)		NOT NULL,
	"target_type"	VARCHAR(50)		NULL,
	"target_id"	UUID		NOT NULL,
	"detail"	JSONB		NULL,
	"created_at"	TIMESTAMPTZ	DEFAULT CURRENT_TIMESTAMP	NOT NULL,
	"user_id"	UUID		NOT NULL
);

COMMENT ON COLUMN "ACTION_LOGS"."action_type" IS 'LOGIN,  LOGOUT, NOTICE_CREATE 등등';
COMMENT ON COLUMN "ACTION_LOGS"."target_type" IS '관련 대상 테이블 명';
COMMENT ON COLUMN "ACTION_LOGS"."target_id" IS '관련 대상의 PK(id)값, 대상 테이블이 다양해 FK로 연결하지 않음';

CREATE TABLE "USER" (
	"user_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"email"	VARCHAR(50)		NOT NULL,
	"password_hash"	VARCHAR(100)		NOT NULL,
	"role"	VARCHAR(10)	DEFAULT '이용자'	NOT NULL,
	"birth"	DATE		NOT NULL,
	"is_agree"	BOOLEAN	DEFAULT FALSE	NOT NULL,
	"login_faild"	INT	DEFAULT 0	NOT NULL,
	"is_locked"	BOOLEAN	DEFAULT FALSE	NOT NULL,
	"permissions"	JSONB		NOT NULL,
	"name"	VARCHAR		NOT NULL,
	"phone"	VARCHAR(50)		NOT NULL,
	"profile_image_url"	VARCHAR(500)		NULL,
	"created_at"	TIMESTAMPTZ	DEFAULT CURRENT_TIMESTAMP	NOT NULL,
	"email_verified"	BOOLEAN	DEFAULT FALSE	NOT NULL,
	"locked_at"	TIMESTAMPTZ		NULL
);

COMMENT ON COLUMN "USER"."password_hash" IS '해시처리해서 암호화';
COMMENT ON COLUMN "USER"."role" IS '관리자, 관제자, 이용자';
COMMENT ON COLUMN "USER"."is_locked" IS 'TRUE, FALSE';

CREATE TABLE "EXTERNAL_INTEGRATIONS" (
	"integration_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"name"	VARCHAR(100)		NOT NULL,
	"description"	TEXT		NULL,
	"api_key"	VARCHAR(255)		NOT NULL,
	"is_status"	BOOLEAN	DEFAULT TRUE	NOT NULL,
	"last_connected_at"	TIMESTAMPTZ		NULL,
	"created_at"	TIMESTAMPTZ	DEFAULT CURRENT_TIMESTAMP	NULL
);

COMMENT ON COLUMN "EXTERNAL_INTEGRATIONS"."name" IS 'IoT 디바이스 게이트웨이';
COMMENT ON COLUMN "EXTERNAL_INTEGRATIONS"."description" IS '배터리 센서 실시간 수집';
COMMENT ON COLUMN "EXTERNAL_INTEGRATIONS"."api_key" IS '암호화 혹은 해시 처리해서 저장';
COMMENT ON COLUMN "EXTERNAL_INTEGRATIONS"."is_status" IS 'TRUE, FALSE';

CREATE TABLE "BATTERY_PROPOSALS" (
	"proposal_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"total_price"	NUMERIC(12, 2)		NOT NULL,
	"price_per_kwh"	NUMERIC(10, 2)		NOT NULL,
	"capacity_range"	VARCHAR(50)		NULL,
	"suitability_reason"	TEXT		NULL,
	"notice_text"	TEXT		NULL,
	"created_at"	TIMESTAMPTZ	DEFAULT CURRENT_TIMESTAMP	NULL,
	"battery_id"	UUID		NOT NULL
);

CREATE TABLE "BATTERY_PASSPORT" (
	"battery_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"manufacturer"	VARCHAR(50)		NULL,
	"battery_type"	VARCHAR(30)		NULL,
	"rated_capacity"	VARCHAR(20)		NULL,
	"soh_score"	NUMERIC(4, 1)		NULL,
	"charge_cycles"	INT		NULL,
	"current_temp"	NUMERIC(4, 1)		NULL,
	"last_inspected_at"	DATE		NULL,
	"car_id"	UUID		NOT NULL,
	"battery_level"	VARCHAR(10)	DEFAULT '미등록'	NULL,
	"remaining_cycles"	INT		NULL,
	"total_cycles"	INT		NULL,
	"reuse_status"	VARCHAR(50)		NULL,
	"grade_detail"	VARCHAR(50)		NULL,
	"reliability_score"	NUMERIC(4, 1)		NULL,
	"reuse_probabilities"	JSONB		NULL,
	"voltage"	NUMERIC(6,2)		NULL,
	"current"	NUMERIC(6,2)		NULL,
	"rul"	NUMERIC(4,1)		NULL,
	"manufactured_at"	DATE		NULL,
	"installed_at"	DATE		NULL
);

COMMENT ON COLUMN "BATTERY_PASSPORT"."battery_level" IS '1, 2, 3, 미등록';
COMMENT ON COLUMN "BATTERY_PASSPORT"."reuse_status" IS '양호, 노후, 수명 말기 등 배터리 진단 등급';
COMMENT ON COLUMN "BATTERY_PASSPORT"."grade_detail" IS '재사용(EV 재제조)급 ,  2차사용(ESS)급, 재활용(소재회수)급, 수거·매입(중개)급';
COMMENT ON COLUMN "BATTERY_PASSPORT"."reuse_probabilities" IS '{ "unusable": 0.05, "reusable": 0.90, "remanufacturable": 0.05 }';

CREATE TABLE "CHARGING_STATION" (
	"charge_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"region"	VARCHAR(20)		NOT NULL,
	"address"	VARCHAR(250)		NOT NULL,
	"latitude"	NUMERIC(10, 7)		NOT NULL,
	"longitude"	NUMERIC(10, 7)		NOT NULL,
	"name"	VARCHAR(100)		NOT NULL,
	"slow_charger_count"	INT	DEFAULT 0	NOT NULL,
	"fast_charger_count"	INT	DEFAULT 0	NOT NULL,
	"available_count"	INT	DEFAULT 0	NOT NULL,
	"min_queue_length"	INT		NULL,
	"min_waiting_time"	INT		NULL
);

COMMENT ON COLUMN "CHARGING_STATION"."region" IS '17개 광역지자체는 CHECK로';
COMMENT ON COLUMN "CHARGING_STATION"."min_queue_length" IS '충전소 테이블 로 계산 후 주기 갱신';
COMMENT ON COLUMN "CHARGING_STATION"."min_waiting_time" IS '충전소 테이블 로 계산 후 주기 갱신';

CREATE TABLE "CHARGER" (
	"charger_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"charger_type"	VARCHAR(10)		NOT NULL,
	"rated_power_kw"	NUMERIC(6,2)		NULL,
	"status"	VARCHAR(10)	DEFAULT '사용가능'	NOT NULL,
	"queue_length"	INT	DEFAULT 0	NULL,
	"waiting_time_min"	INT		NULL,
	"updated_at"	TIMESTAMPTZ		NULL,
	"charge_id"	UUID		NOT NULL
);

COMMENT ON COLUMN "CHARGER"."charger_type" IS '충전 예측 계산에 활용(급속, 완속)';
COMMENT ON COLUMN "CHARGER"."rated_power_kw" IS '충전기 능력';
COMMENT ON COLUMN "CHARGER"."status" IS '기본값은 충전 안 할 때(충전중, 대기중, 사용가능, 고장)';
COMMENT ON COLUMN "CHARGER"."queue_length" IS '주기 배치 갱신';
COMMENT ON COLUMN "CHARGER"."waiting_time_min" IS 'AI 모델 출력값, 주기 배치 갱신';
COMMENT ON COLUMN "CHARGER"."updated_at" IS '위 실시간 필드 마지막 갱신 시각';

CREATE TABLE "BATTERY_OFFERS" (
	"offer_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"buyer_name"	VARCHAR(100)		NOT NULL,
	"business_type"	VARCHAR(50)		NULL,
	"offered_price"	NUMERIC(12, 2)		NOT NULL,
	"price_per_kwh"	NUMERIC(10, 2)		NULL,
	"rank_order"	INT		NULL,
	"description"	TEXT		NULL,
	"battery_id"	UUID		NOT NULL
);

CREATE TABLE "TERM_AGREEMENT" (
	"agreement_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"term_key"	VARCHAR(30)		NOT NULL,
	"agreed_at"	TIMESTAMPTZ	DEFAULT CURRENT_TIMESTAMP	NOT NULL,
	"user_id"	UUID		NOT NULL
);

COMMENT ON COLUMN "TERM_AGREEMENT"."term_key" IS 'service, age, privacy, location, vehicleData, thermalCamera';

CREATE TABLE "AI_REPORTS" (
	"report_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"title"	VARCHAR(100)		NOT NULL,
	"report_data"	JSONB		NOT NULL,
	"report_type"	VARCHAR(10)	DEFAULT '월간보고서'	NOT NULL,
	"created_at"	TIMESTAMPTZ	DEFAULT CURRENT_TIMESTAMP	NULL,
	"car_id"	UUID		NULL,
	"anomaly_id"	UUID		NULL,
	"is_read"	BOOLEAN	DEFAULT FALSE	NOT NULL
);

COMMENT ON COLUMN "AI_REPORTS"."report_data" IS '프롬프트를 통해 나오는 데이터 값의 형식을 고정시켜야함.(렌더링을 위함)';
COMMENT ON COLUMN "AI_REPORTS"."report_type" IS '월간보고서, 이상';
COMMENT ON COLUMN "AI_REPORTS"."is_read" IS 'TRUE, FALSE';

CREATE TABLE "BATCH_JOB_LOGS" (
	"batch_log_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"run_type"	VARCHAR(10)	DEFAULT 'AUTO'	NOT NULL,
	"status"	VARCHAR(20)	DEFAULT 'SUCCESS'	NOT NULL,
	"message"	TEXT		NULL,
	"executed_at"	TIMESTAMPTZ	DEFAULT CURRENT_TIMESTAMP	NULL,
	"job_id"	VARCHAR(50)		NOT NULL
);

COMMENT ON COLUMN "BATCH_JOB_LOGS"."run_type" IS 'AUTO, MANUAL';
COMMENT ON COLUMN "BATCH_JOB_LOGS"."status" IS 'SUCCESS, FAILED';
COMMENT ON COLUMN "BATCH_JOB_LOGS"."message" IS '실패 사유 등';

CREATE TABLE "NOTICE_ATTACHMENT" (
	"attachment_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"file_name"	VARCHAR(255)		NOT NULL,
	"file_url"	VARCHAR(500)		NOT NULL,
	"file_size"	BIGINT		NULL,
	"file_type"	VARCHAR(50)		NULL,
	"notice_id"	UUID		NOT NULL
);

CREATE TABLE "NOTICE" (
	"notice_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"title"	VARCHAR(100)		NOT NULL,
	"content"	TEXT		NOT NULL,
	"is_pinned"	BOOLEAN	DEFAULT FALSE	NOT NULL,
	"created_at"	TIMESTAMPTZ	DEFAULT CURRENT_TIMESTAMP	NOT NULL,
	"user_id"	UUID		NOT NULL,
	"is_read"	BOOLEAN	DEFAULT FALSE	NOT NULL,
	"is_important"	BOOLEAN	DEFAULT FALSE	NOT NULL,
	"target_role"	VARCHAR(20)		NULL,
	"view_count"	INT	DEFAULT 0	NOT NULL,
	"updated_at"	TIMESTAMPTZ		NULL
);

COMMENT ON COLUMN "NOTICE"."is_pinned" IS 'TRUE, FALSE';
COMMENT ON COLUMN "NOTICE"."is_read" IS 'TRUE, FALSE';
COMMENT ON COLUMN "NOTICE"."is_important" IS 'TRUE, FALSE';
COMMENT ON COLUMN "NOTICE"."target_role" IS 'ADMIN, CONTROLLER';

CREATE TABLE "CAR" (
	"car_id"	UUID	DEFAULT gen_random_uuid()	NOT NULL,
	"car_number"	VARCHAR(20)		NOT NULL,
	"model"	VARCHAR(30)		NOT NULL,
	"vin"	VARCHAR(17)		NOT NULL,
	"nickname"	VARCHAR(30)		NULL,
	"image_url"	VARCHAR(500)		NULL,
	"is_primary"	BOOLEAN	DEFAULT FALSE	NOT NULL,
	"created_at"	TIMESTAMPTZ	DEFAULT CURRENT_TIMESTAMP	NULL,
	"user_id"	UUID		NOT NULL
);

ALTER TABLE "NOTIFICATION_CHANNELS" ADD CONSTRAINT "PK_NOTIFICATION_CHANNELS" PRIMARY KEY ("channel_id");
ALTER TABLE "ANOMALY_LOGS" ADD CONSTRAINT "PK_ANOMALY_LOGS" PRIMARY KEY ("anomaly_id");
ALTER TABLE "BATTERY_SOH_HISTORY" ADD CONSTRAINT "PK_BATTERY_SOH_HISTORY" PRIMARY KEY ("history_id");
ALTER TABLE "LOGIN_LOGS" ADD CONSTRAINT "PK_LOGIN_LOGS" PRIMARY KEY ("log_id");
ALTER TABLE "BATCH_JOBS" ADD CONSTRAINT "PK_BATCH_JOBS" PRIMARY KEY ("job_id");
ALTER TABLE "BATTERY_DIAGNOSIS_METRICS" ADD CONSTRAINT "PK_BATTERY_DIAGNOSIS_METRICS" PRIMARY KEY ("metric_id");
ALTER TABLE "TWIN_FRAMES" ADD CONSTRAINT "PK_TWIN_FRAMES" PRIMARY KEY ("frame_id");
ALTER TABLE "NOTIFICATIONS" ADD CONSTRAINT "PK_NOTIFICATIONS" PRIMARY KEY ("notification_id");
ALTER TABLE "CHARGING_SESSION" ADD CONSTRAINT "PK_CHARGING_SESSION" PRIMARY KEY ("session_id");
ALTER TABLE "NOTIFICATION_MATRIX" ADD CONSTRAINT "PK_NOTIFICATION_MATRIX" PRIMARY KEY ("matrix_id");
ALTER TABLE "ACTION_LOGS" ADD CONSTRAINT "PK_ACTION_LOGS" PRIMARY KEY ("action_id");
ALTER TABLE "USER" ADD CONSTRAINT "PK_USER" PRIMARY KEY ("user_id");
ALTER TABLE "EXTERNAL_INTEGRATIONS" ADD CONSTRAINT "PK_EXTERNAL_INTEGRATIONS" PRIMARY KEY ("integration_id");
ALTER TABLE "BATTERY_PROPOSALS" ADD CONSTRAINT "PK_BATTERY_PROPOSALS" PRIMARY KEY ("proposal_id");
ALTER TABLE "BATTERY_PASSPORT" ADD CONSTRAINT "PK_BATTERY_PASSPORT" PRIMARY KEY ("battery_id");
ALTER TABLE "CHARGING_STATION" ADD CONSTRAINT "PK_CHARGING_STATION" PRIMARY KEY ("charge_id");
ALTER TABLE "CHARGER" ADD CONSTRAINT "PK_CHARGER" PRIMARY KEY ("charger_id");
ALTER TABLE "BATTERY_OFFERS" ADD CONSTRAINT "PK_BATTERY_OFFERS" PRIMARY KEY ("offer_id");
ALTER TABLE "TERM_AGREEMENT" ADD CONSTRAINT "PK_TERM_AGREEMENT" PRIMARY KEY ("agreement_id");
ALTER TABLE "AI_REPORTS" ADD CONSTRAINT "PK_AI_REPORTS" PRIMARY KEY ("report_id");
ALTER TABLE "BATCH_JOB_LOGS" ADD CONSTRAINT "PK_BATCH_JOB_LOGS" PRIMARY KEY ("batch_log_id");
ALTER TABLE "NOTICE_ATTACHMENT" ADD CONSTRAINT "PK_NOTICE_ATTACHMENT" PRIMARY KEY ("attachment_id");
ALTER TABLE "NOTICE" ADD CONSTRAINT "PK_NOTICE" PRIMARY KEY ("notice_id");
ALTER TABLE "CAR" ADD CONSTRAINT "PK_CAR" PRIMARY KEY ("car_id");

ALTER TABLE "ANOMALY_LOGS" ADD CONSTRAINT "FK_CAR_TO_ANOMALY_LOGS_1" FOREIGN KEY ("car_id") REFERENCES "CAR" ("car_id");
ALTER TABLE "ANOMALY_LOGS" ADD CONSTRAINT "FK_CHARGING_SESSION_TO_ANOMALY_LOGS_1" FOREIGN KEY ("session_id") REFERENCES "CHARGING_SESSION" ("session_id");
ALTER TABLE "BATTERY_SOH_HISTORY" ADD CONSTRAINT "FK_BATTERY_PASSPORT_TO_BATTERY_SOH_HISTORY_1" FOREIGN KEY ("battery_id") REFERENCES "BATTERY_PASSPORT" ("battery_id");
ALTER TABLE "LOGIN_LOGS" ADD CONSTRAINT "FK_USER_TO_LOGIN_LOGS_1" FOREIGN KEY ("user_id") REFERENCES "USER" ("user_id");
ALTER TABLE "BATTERY_DIAGNOSIS_METRICS" ADD CONSTRAINT "FK_BATTERY_PASSPORT_TO_BATTERY_DIAGNOSIS_METRICS_1" FOREIGN KEY ("battery_id") REFERENCES "BATTERY_PASSPORT" ("battery_id");
ALTER TABLE "TWIN_FRAMES" ADD CONSTRAINT "FK_ANOMALY_LOGS_TO_TWIN_FRAMES_1" FOREIGN KEY ("anomaly_id") REFERENCES "ANOMALY_LOGS" ("anomaly_id");
ALTER TABLE "TWIN_FRAMES" ADD CONSTRAINT "FK_CAR_TO_TWIN_FRAMES_1" FOREIGN KEY ("car_id") REFERENCES "CAR" ("car_id");
ALTER TABLE "NOTIFICATIONS" ADD CONSTRAINT "FK_USER_TO_NOTIFICATIONS_1" FOREIGN KEY ("user_id") REFERENCES "USER" ("user_id");
ALTER TABLE "NOTIFICATIONS" ADD CONSTRAINT "FK_CAR_TO_NOTIFICATIONS_1" FOREIGN KEY ("car_id") REFERENCES "CAR" ("car_id");
ALTER TABLE "NOTIFICATIONS" ADD CONSTRAINT "FK_AI_REPORTS_TO_NOTIFICATIONS_1" FOREIGN KEY ("report_id") REFERENCES "AI_REPORTS" ("report_id");
ALTER TABLE "CHARGING_SESSION" ADD CONSTRAINT "FK_CAR_TO_CHARGING_SESSION_1" FOREIGN KEY ("car_id") REFERENCES "CAR" ("car_id");
ALTER TABLE "CHARGING_SESSION" ADD CONSTRAINT "FK_CHARGER_TO_CHARGING_SESSION_1" FOREIGN KEY ("charger_id") REFERENCES "CHARGER" ("charger_id");
ALTER TABLE "NOTIFICATION_MATRIX" ADD CONSTRAINT "FK_NOTIFICATION_CHANNELS_TO_NOTIFICATION_MATRIX_1" FOREIGN KEY ("channel_id") REFERENCES "NOTIFICATION_CHANNELS" ("channel_id");
ALTER TABLE "ACTION_LOGS" ADD CONSTRAINT "FK_USER_TO_ACTION_LOGS_1" FOREIGN KEY ("user_id") REFERENCES "USER" ("user_id");
ALTER TABLE "BATTERY_PROPOSALS" ADD CONSTRAINT "FK_BATTERY_PASSPORT_TO_BATTERY_PROPOSALS_1" FOREIGN KEY ("battery_id") REFERENCES "BATTERY_PASSPORT" ("battery_id");
ALTER TABLE "BATTERY_PASSPORT" ADD CONSTRAINT "FK_CAR_TO_BATTERY_PASSPORT_1" FOREIGN KEY ("car_id") REFERENCES "CAR" ("car_id");
ALTER TABLE "CHARGER" ADD CONSTRAINT "FK_CHARGING_STATION_TO_CHARGER_1" FOREIGN KEY ("charge_id") REFERENCES "CHARGING_STATION" ("charge_id");
ALTER TABLE "BATTERY_OFFERS" ADD CONSTRAINT "FK_BATTERY_PASSPORT_TO_BATTERY_OFFERS_1" FOREIGN KEY ("battery_id") REFERENCES "BATTERY_PASSPORT" ("battery_id");
ALTER TABLE "TERM_AGREEMENT" ADD CONSTRAINT "FK_USER_TO_TERM_AGREEMENT_1" FOREIGN KEY ("user_id") REFERENCES "USER" ("user_id");
ALTER TABLE "AI_REPORTS" ADD CONSTRAINT "FK_CAR_TO_AI_REPORTS_1" FOREIGN KEY ("car_id") REFERENCES "CAR" ("car_id");
ALTER TABLE "AI_REPORTS" ADD CONSTRAINT "FK_ANOMALY_LOGS_TO_AI_REPORTS_1" FOREIGN KEY ("anomaly_id") REFERENCES "ANOMALY_LOGS" ("anomaly_id");
ALTER TABLE "BATCH_JOB_LOGS" ADD CONSTRAINT "FK_BATCH_JOBS_TO_BATCH_JOB_LOGS_1" FOREIGN KEY ("job_id") REFERENCES "BATCH_JOBS" ("job_id");
ALTER TABLE "NOTICE_ATTACHMENT" ADD CONSTRAINT "FK_NOTICE_TO_NOTICE_ATTACHMENT_1" FOREIGN KEY ("notice_id") REFERENCES "NOTICE" ("notice_id");
ALTER TABLE "NOTICE" ADD CONSTRAINT "FK_USER_TO_NOTICE_1" FOREIGN KEY ("user_id") REFERENCES "USER" ("user_id");
ALTER TABLE "CAR" ADD CONSTRAINT "FK_USER_TO_CAR_1" FOREIGN KEY ("user_id") REFERENCES "USER" ("user_id");

ALTER TABLE "BATTERY_PASSPORT" ADD CONSTRAINT "UQ_BATTERY_PASSPORT_CAR" UNIQUE ("car_id");

-- ddl-auto=none이라 이 파일은 RDS에 수동으로 적용됨. 회원가입 이메일 중복 방지용 제약이니
-- 실제 DB에도 이 ALTER문을 한 번 수동 실행해야 함(AuthService.signup의 findByEmail 체크가 1차 방어선).
ALTER TABLE "USER" ADD CONSTRAINT "UQ_USER_EMAIL" UNIQUE ("email");

-- 회원 탈퇴(소프트 삭제)용 컬럼. 실제 row는 지우지 않고 플래그만 세운다(로그인/신규가입
-- 이메일 중복체크에서는 여전히 "사용 중"으로 취급 - AuthService 참고). 이것도 ddl-auto=none이라
-- 실제 RDS에는 수동으로 한 번 실행해야 함.
ALTER TABLE "USER" ADD COLUMN "is_deleted" BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE "USER" ADD COLUMN "deleted_at" TIMESTAMPTZ NULL;

-- 마이페이지 알림(푸시) 설정 ON/OFF 저장용. 디바이스 토큰 등록/실제 발송은 별도 기능이고,
-- 이건 그 전 단계로 사용자가 켜고 끈 상태만 저장한다. ddl-auto=none이라 실제 RDS에는
-- 수동으로 한 번 실행해야 함.
ALTER TABLE "USER" ADD COLUMN "push_enabled" BOOLEAN DEFAULT TRUE NOT NULL;
