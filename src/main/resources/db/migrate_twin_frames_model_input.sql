-- Applied to the AWS RDS database on 2026-08-05.
-- Keep this script for reproducibility; run with a migration-capable account.

BEGIN;

ALTER TABLE "TWIN_FRAMES"
    ADD COLUMN IF NOT EXISTS "session_id" UUID NULL;

ALTER TABLE "TWIN_FRAMES"
    ADD COLUMN IF NOT EXISTS "model_input" JSONB NULL;

ALTER TABLE "TWIN_FRAMES"
    ALTER COLUMN "anomaly_id" DROP NOT NULL;

COMMENT ON COLUMN "TWIN_FRAMES"."model_input"
    IS 'BMS 화재·안전 분류 모델에 실제 전달된 입력값';

CREATE INDEX IF NOT EXISTS "IX_TWIN_FRAMES_SESSION_OBSERVED"
    ON "TWIN_FRAMES" ("session_id", "observed_at");

COMMIT;
