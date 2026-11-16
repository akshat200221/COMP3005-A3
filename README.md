# COMP3005 – Assignment 3: PostgreSQL CRUD Application (Java + JDBC)

**Student:** Akshat Desai (student ID: 101190893)

A Java console application that connects to a PostgreSQL database with JDBC and performs CRUD (Create, Read, Update, Delete) operations on a `students` table.

## Video Demonstration

▶️ **[Watch the demo video](PASTE_YOUR_VIDEO_LINK_HERE)**

## Repository Structure

```
├── db/
│   ├── 01_create_database.sql   # Creates the students_db database
│   ├── 02_create_table.sql      # Creates the students table (schema + constraints)
│   └── 03_insert_data.sql       # Inserts the initial 3 students
├── app/
│   └── src/
│       └── StudentApp.java      # Java application (all 4 CRUD functions + menu)
├── lib/
│   └── postgresql-42.x.x.jar    # PostgreSQL JDBC driver
└── README.md
```

## Requirements

- **PostgreSQL** (with **pgAdmin**) – https://www.postgresql.org/download/
- **Java JDK 11 or newer** – https://adoptium.net/
- **PostgreSQL JDBC driver** (`.jar`) – https://jdbc.postgresql.org/download/ (already included in `lib/`)

## Database Schema

| Field             | Type                 | Constraints              |
|-------------------|----------------------|--------------------------|
| `student_id`      | `SERIAL` (integer)   | Primary Key, auto-increment |
| `first_name`      | `TEXT`               | Not Null                 |
| `last_name`       | `TEXT`               | Not Null                 |
| `email`           | `TEXT`               | Not Null, Unique         |
| `enrollment_date` | `DATE`               | (optional)               |

## Step 1 – Set Up the Database (pgAdmin)

1. Open **pgAdmin** and connect to your PostgreSQL server.
2. Right-click the **postgres** database → **Query Tool**, open `db/01_create_database.sql` and run it (F5). This creates `students_db`.
3. Refresh **Databases**, right-click **students_db** → **Query Tool**.
4. Open and run `db/02_create_table.sql` to create the `students` table.
5. Open and run `db/03_insert_data.sql` to insert the initial data. The output should show John Doe, Jane Smith and Jim Beam with IDs 1–3.

> To reset the database to its original state at any time, re-run `02_create_table.sql` and then `03_insert_data.sql`.

## Step 2 – Configure the Connection

Open `app/src/StudentApp.java` and edit these lines near the top to match your PostgreSQL login:

```java
private static final String URL = "jdbc:postgresql://localhost:5432/students_db";
private static final String USER = "postgres";
private static final String PASSWORD = "postgres";   // <-- your password
```

## Step 3 – Compile and Run

### Option A: Command line (run from the repository's root folder)

**Windows (Command Prompt / PowerShell):**
```
javac -cp "lib/*" -d out app/src/StudentApp.java
java -cp "out;lib/*" StudentApp
```

**macOS / Linux:**
```
javac -cp "lib/*" -d out app/src/StudentApp.java
java -cp "out:lib/*" StudentApp
```

### Option B: IntelliJ IDEA

1. **File → Open** and select the repository folder.
2. Right-click `app/src` → **Mark Directory as → Sources Root**.
3. **File → Project Structure → Modules → Dependencies → + → JARs or Directories**, select the `.jar` in `lib/`, click **OK**.
4. Make sure a JDK is selected under **Project Structure → Project → SDK**.
5. Open `StudentApp.java` and click the green ▶ Run button next to `main`.

## Using the Application

When the program starts, it tests the connection and shows a menu:

```
===== Student Management =====
1. Get all students
2. Add a student
3. Update a student's email
4. Delete a student
0. Exit
```

## Application Functions

| Function | SQL | Description |
|---|---|---|
| `getAllStudents()` | `SELECT` | Retrieves every row from `students` and prints them as a table, ordered by ID. |
| `addStudent(first_name, last_name, email, enrollment_date)` | `INSERT` | Inserts a new student. `student_id` is generated automatically; the new ID is printed. Date format is `YYYY-MM-DD` (leave blank for no date). |
| `updateStudentEmail(student_id, new_email)` | `UPDATE` | Changes the email of the student with the given ID. |
| `deleteStudent(student_id)` | `DELETE` | Removes the student with the given ID. |

### Implementation notes

- **PreparedStatement** with `?` placeholders is used for all queries that take user input, which prevents SQL injection.
- **try-with-resources** closes every `Connection`, `Statement` and `ResultSet` automatically.
- **Error handling:**
  - Duplicate email (UNIQUE violation, SQLState `23505`) → friendly message instead of a crash.
  - Updating/deleting an ID that does not exist → "No student found with ID …".
  - Empty required fields, invalid dates, and non-numeric IDs are rejected before reaching the database.
  - Connection problems (wrong password, server not running, missing driver) are reported at start-up.
