ALTER TABLE issues
    ADD COLUMN IF NOT EXISTS ai_classification_status VARCHAR(32);

UPDATE issues
SET ai_classification_status = CASE
    WHEN category_id IS NOT NULL THEN 'COMPLETED'
    ELSE 'NEEDS_REVIEW'
END
WHERE ai_classification_status IS NULL;

ALTER TABLE issues
    ALTER COLUMN ai_classification_status SET NOT NULL;
