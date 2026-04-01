-- =========================
-- USERS
-- =========================
CREATE TABLE users (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   email VARCHAR(255) NOT NULL UNIQUE,
   password VARCHAR(255) NOT NULL,
   name VARCHAR(100) NOT NULL,
   role VARCHAR(20) NOT NULL DEFAULT 'USER',
   created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
   CHECK (role IN ('USER', 'ADMIN'))
);


-- =========================
-- TRIPS
-- =========================
CREATE TABLE trips (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   user_id BIGINT NOT NULL,
   title VARCHAR(255) NOT NULL,
   country VARCHAR(100) NOT NULL,
   start_date DATE NOT NULL,
   end_date DATE NOT NULL,
   created_at DATETIME DEFAULT CURRENT_TIMESTAMP,


   CONSTRAINT fk_trip_user
       FOREIGN KEY (user_id) REFERENCES users(id)
       ON DELETE CASCADE
);


-- =========================
-- EXPENSES
-- =========================
CREATE TABLE expenses (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   trip_id BIGINT NOT NULL,
   expense_date DATE NOT NULL,
   category VARCHAR(50) NOT NULL,
   amount DECIMAL(12,2) NOT NULL,
   memo VARCHAR(500),
   created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
   updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,


   CONSTRAINT fk_expense_trip
       FOREIGN KEY (trip_id) REFERENCES trips(id)
       ON DELETE CASCADE
);


-- =========================
-- ATTACHMENTS
-- =========================
CREATE TABLE attachments (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   trip_id BIGINT NOT NULL,


   file_name VARCHAR(255),
   file_path VARCHAR(500),
   file_type VARCHAR(50),


   created_at DATETIME DEFAULT CURRENT_TIMESTAMP,


   CONSTRAINT fk_attachment_trip
       FOREIGN KEY (trip_id) REFERENCES trips(id)
       ON DELETE CASCADE
);


-- =========================
-- EXPENSE BUDGET
-- =========================
CREATE TABLE expense_budget (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   trip_id BIGINT NOT NULL,
   category VARCHAR(50) NOT NULL,
   budget_amount DECIMAL(12,2) NOT NULL,
   created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
   updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,


   CONSTRAINT fk_budget_trip
       FOREIGN KEY (trip_id) REFERENCES trips(id)
       ON DELETE CASCADE
);
