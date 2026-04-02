CREATE TABLE journal_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    day_index INT NOT NULL,
    record_date DATE NOT NULL,
    memo VARCHAR(1000),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_journal_entry_trip
        FOREIGN KEY (trip_id) REFERENCES trips(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_journal_entry_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_journal_entries_trip_created_at
    ON journal_entries (trip_id, created_at);

CREATE INDEX idx_journal_entries_trip_record_date
    ON journal_entries (trip_id, record_date);

CREATE TABLE journal_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    journal_entry_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    file_name VARCHAR(255),
    file_size BIGINT,
    mime_type VARCHAR(100),
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_journal_image_entry
        FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_journal_images_entry_sort_order
    ON journal_images (journal_entry_id, sort_order);
