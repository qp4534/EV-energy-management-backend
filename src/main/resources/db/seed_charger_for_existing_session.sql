-- Backfills the missing CHARGER row for the one existing dummy CHARGING_SESSION,
-- which was seeded pointing its charger_id directly at a CHARGING_STATION.chargeId
-- (skipping the CHARGER hop). This creates a real CHARGER row referencing that
-- station, then repoints the session at the new charger_id.

WITH new_charger AS (
    INSERT INTO "CHARGER" ("charger_type", "status", "charge_id")
    VALUES ('완속', '충전중', '889bade3-a2ee-4809-95ab-182d3aba003f')
    RETURNING "charger_id"
)
UPDATE "CHARGING_SESSION"
SET "charger_id" = (SELECT "charger_id" FROM new_charger)
WHERE "session_id" = '201ec840-5b32-4b6b-ac27-fe6d2a0dc8f8';
