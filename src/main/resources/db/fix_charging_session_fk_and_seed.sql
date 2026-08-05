-- CHARGING_SESSION.charger_id had a leftover FK pointing at CHARGING_STATION instead of
-- CHARGER (old pre-schema structure). Drop it, backfill a real CHARGER row + repoint the
-- one existing session to it, THEN add the correct FK (must happen after the data is
-- consistent, or the ADD CONSTRAINT would fail against the stale value).

ALTER TABLE "CHARGING_SESSION" DROP CONSTRAINT IF EXISTS "FK_CHARGING_SESSION_STATION";

WITH new_charger AS (
    INSERT INTO "CHARGER" ("charger_type", "status", "charge_id")
    VALUES ('완속', '충전중', '889bade3-a2ee-4809-95ab-182d3aba003f')
    RETURNING "charger_id"
)
UPDATE "CHARGING_SESSION"
SET "charger_id" = (SELECT "charger_id" FROM new_charger)
WHERE "session_id" = '201ec840-5b32-4b6b-ac27-fe6d2a0dc8f8';

ALTER TABLE "CHARGING_SESSION" ADD CONSTRAINT "FK_CHARGER_TO_CHARGING_SESSION_1"
    FOREIGN KEY ("charger_id") REFERENCES "CHARGER" ("charger_id");
