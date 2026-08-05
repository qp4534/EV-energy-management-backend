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

ALTER TABLE "CHARGER" ADD CONSTRAINT "PK_CHARGER" PRIMARY KEY ("charger_id");
ALTER TABLE "CHARGER" ADD CONSTRAINT "FK_CHARGING_STATION_TO_CHARGER_1" FOREIGN KEY ("charge_id") REFERENCES "CHARGING_STATION" ("charge_id");
