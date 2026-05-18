ALTER TABLE technician_profiles
    ADD COLUMN IF NOT EXISTS max_active_issues INTEGER NOT NULL DEFAULT 5;

UPDATE technician_profiles
SET max_active_issues = 5
WHERE max_active_issues IS NULL;
