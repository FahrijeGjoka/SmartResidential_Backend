ALTER TABLE attachments
    ALTER COLUMN file_url DROP NOT NULL;

ALTER TABLE attachments
    ADD COLUMN IF NOT EXISTS file_size BIGINT,
    ADD COLUMN IF NOT EXISTS file_type VARCHAR(255),
    ADD COLUMN IF NOT EXISTS original_filename VARCHAR(255),
    ADD COLUMN IF NOT EXISTS stored_filename VARCHAR(255),
    ADD COLUMN IF NOT EXISTS thumbnail_path VARCHAR(500),
    ADD COLUMN IF NOT EXISTS processing_status VARCHAR(50) DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS processing_error TEXT,
    ADD COLUMN IF NOT EXISTS processed_at TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN IF NOT EXISTS uploaded_by BIGINT;

ALTER TABLE attachments
    ALTER COLUMN thumbnail_path TYPE VARCHAR(500),
    ALTER COLUMN processing_status TYPE VARCHAR(50),
    ALTER COLUMN processing_status SET DEFAULT 'PENDING';

UPDATE attachments
SET original_filename = COALESCE(original_filename, file_name),
    processing_status = COALESCE(processing_status, 'COMPLETED'),
    processed_at = CASE
        WHEN COALESCE(processing_status, 'COMPLETED') = 'COMPLETED'
            THEN COALESCE(processed_at, uploaded_at, CURRENT_TIMESTAMP)
        ELSE processed_at
    END;

ALTER TABLE attachments
    ALTER COLUMN processing_status SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_attachments_on_uploaded_by'
          AND conrelid = 'attachments'::regclass
    ) THEN
        ALTER TABLE attachments
            ADD CONSTRAINT fk_attachments_on_uploaded_by
            FOREIGN KEY (uploaded_by) REFERENCES users (id);
    END IF;
END $$;
