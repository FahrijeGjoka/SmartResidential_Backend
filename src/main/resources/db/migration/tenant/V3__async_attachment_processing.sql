ALTER TABLE attachments
    ALTER COLUMN file_url DROP NOT NULL;

ALTER TABLE attachments
    ADD COLUMN IF NOT EXISTS uploaded_by BIGINT,
    ADD COLUMN IF NOT EXISTS processing_status VARCHAR(32) NOT NULL DEFAULT 'COMPLETED',
    ADD COLUMN IF NOT EXISTS processing_error TEXT,
    ADD COLUMN IF NOT EXISTS processed_at TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN IF NOT EXISTS file_type VARCHAR(255),
    ADD COLUMN IF NOT EXISTS file_size BIGINT,
    ADD COLUMN IF NOT EXISTS original_filename VARCHAR(255),
    ADD COLUMN IF NOT EXISTS stored_filename VARCHAR(255),
    ADD COLUMN IF NOT EXISTS thumbnail_path VARCHAR(255);

UPDATE attachments
SET original_filename = COALESCE(original_filename, file_name),
    processing_status = COALESCE(processing_status, 'COMPLETED'),
    processed_at = COALESCE(processed_at, uploaded_at)
WHERE original_filename IS NULL
   OR processed_at IS NULL;

ALTER TABLE attachments
    ADD CONSTRAINT FK_ATTACHMENTS_ON_UPLOADED_BY FOREIGN KEY (uploaded_by) REFERENCES users (id);
