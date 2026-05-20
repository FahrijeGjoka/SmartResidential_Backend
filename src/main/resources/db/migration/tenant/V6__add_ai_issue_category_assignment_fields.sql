ALTER TABLE issue_categories
    ADD COLUMN IF NOT EXISTS required_specialization VARCHAR(255);

ALTER TABLE issues
    ADD COLUMN IF NOT EXISTS ai_category_confidence DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS ai_category_reason TEXT;

UPDATE issue_categories
SET required_specialization = CASE name
    WHEN 'Electrical' THEN 'Electrical'
    WHEN 'Plumbing' THEN 'Plumbing'
    WHEN 'Elevator' THEN 'Elevator'
    WHEN 'HVAC' THEN 'HVAC'
    WHEN 'Security' THEN 'Security'
    WHEN 'Emergency' THEN 'General maintenance'
    WHEN 'Cleaning' THEN 'Cleaning'
    WHEN 'Structural' THEN 'Structural'
    ELSE required_specialization
END
WHERE required_specialization IS NULL;
