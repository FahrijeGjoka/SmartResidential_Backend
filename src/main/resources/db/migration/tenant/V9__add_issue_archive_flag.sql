ALTER TABLE issues
    ADD COLUMN IF NOT EXISTS is_archived BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE issues
SET is_archived = FALSE
WHERE is_archived IS NULL;

CREATE INDEX IF NOT EXISTS idx_issues_is_archived ON issues (is_archived);
