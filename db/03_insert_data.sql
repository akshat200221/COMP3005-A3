-- ============================================================
-- 03_insert_data.sql
-- Populates the "students" table with the initial data set.
-- student_id is not listed because SERIAL generates it (1, 2, 3).
-- Run this while connected to students_db.
-- ============================================================

INSERT INTO students (first_name, last_name, email, enrollment_date) VALUES
('John', 'Doe', 'john.doe@example.com', '2023-09-01'),
('Jane', 'Smith', 'jane.smith@example.com', '2023-09-01'),
('Jim', 'Beam', 'jim.beam@example.com', '2023-09-02');

-- Check the result
SELECT * FROM students ORDER BY student_id;
