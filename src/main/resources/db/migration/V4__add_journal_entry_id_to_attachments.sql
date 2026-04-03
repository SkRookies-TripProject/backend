ALTER TABLE attachments
    ADD COLUMN journal_entry_id BIGINT NULL;

ALTER TABLE attachments
    ADD CONSTRAINT fk_attachment_journal_entry
        FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id)
        ON DELETE CASCADE;

CREATE INDEX idx_attachments_journal_entry_id
    ON attachments (journal_entry_id, created_at);
