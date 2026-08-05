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
	"anomaly_id"	UUID		NOT NULL,
	"car_id"	UUID		NOT NULL,
	"source_image_ref"	VARCHAR(500)		NULL
);

COMMENT ON COLUMN "TWIN_FRAMES"."ml_risk_level" IS '머신러닝이 판단한 위험도';
COMMENT ON COLUMN "TWIN_FRAMES"."physics_risk_level" IS '단순 물리적인 정보(온도 등)으로 판단한 위험도';
COMMENT ON COLUMN "TWIN_FRAMES"."image_confidence" IS 'image_risk_level에 관한 정확도';
COMMENT ON COLUMN "TWIN_FRAMES"."raw_metrics" IS '3D 렌더링용 원본 배열 데이터, 웹이 통째로 읽어서 씀';
COMMENT ON COLUMN "TWIN_FRAMES"."source_image_ref" IS 'AI 분석에 쓰인 원본 열화상 사진의 경로';

ALTER TABLE "TWIN_FRAMES" ADD CONSTRAINT "PK_TWIN_FRAMES" PRIMARY KEY ("frame_id");

ALTER TABLE "TWIN_FRAMES" ADD CONSTRAINT "FK_ANOMALY_LOGS_TO_TWIN_FRAMES_1" FOREIGN KEY ("anomaly_id") REFERENCES "ANOMALY_LOGS" ("anomaly_id");
ALTER TABLE "TWIN_FRAMES" ADD CONSTRAINT "FK_CAR_TO_TWIN_FRAMES_1" FOREIGN KEY ("car_id") REFERENCES "CAR" ("car_id");
