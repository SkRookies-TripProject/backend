ALTER TABLE journal_entries
    DROP FOREIGN KEY fk_journal_entry_user,
    DROP COLUMN user_id,
    DROP COLUMN day_index;

DROP TABLE journal_images;
