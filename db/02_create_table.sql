-- ============================================================
-- 02_create_table.sql
-- Creates the "students" table using the schema given in the
-- assignment. Run this while connected to students_db.
-- ============================================================

-- Remove the table if it already exists so the script can be re-run
-- to reset the database to a clean state.
DROP TABLE IF EXISTS students;

CREATE TABLE students (
    -- SERIAL = INTEGER that auto-increments (PostgreSQL creates a
    -- sequence behind the scenes). PRIMARY KEY = NOT NULL + UNIQUE.
    student_id      SERIAL  PRIMARY KEY,

    -- Names are required, so they cannot be NULL.
    first_name      TEXT    NOT NULL,
    last_name       TEXT    NOT NULL,

    -- Email is required and no two students may share the same email.
    email           TEXT    NOT NULL UNIQUE,

    -- Enrollment date is optional (NULL is allowed by default).
    enrollment_date DATE
);
