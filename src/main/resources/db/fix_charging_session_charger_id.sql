DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'CHARGING_SESSION' AND column_name = 'charge_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'CHARGING_SESSION' AND column_name = 'charger_id'
    ) THEN
        ALTER TABLE "CHARGING_SESSION" RENAME COLUMN "charge_id" TO "charger_id";
    END IF;
END $$;
