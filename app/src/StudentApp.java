import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * COMP3005 - Assignment 3
 * Database Interaction with PostgreSQL and Application Programming
 *
 * This console application connects to a PostgreSQL database using JDBC
 * and performs CRUD operations on the "students" table:
 *   - getAllStudents()      -> Read   (SELECT)
 *   - addStudent(...)       -> Create (INSERT)
 *   - updateStudentEmail()  -> Update (UPDATE)
 *   - deleteStudent()       -> Delete (DELETE)
 *
 * A simple text menu lets the user call each function.
 */
public class StudentApp {

    // ------------------------------------------------------------------
    // JDBC & database credentials
    // Change USER / PASSWORD to match your own PostgreSQL installation.
    // ------------------------------------------------------------------
    private static final String URL = "jdbc:postgresql://localhost:5432/students_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password_here";

    // SQLState code PostgreSQL returns when a UNIQUE constraint is violated
    private static final String UNIQUE_VIOLATION = "23505";

    /**
     * Opens and returns a new connection to the database.
     * Each function calls this and closes the connection when it is done
     * (using try-with-resources), so connections are never left open.
     */
    private static Connection getConnection() throws SQLException {
        try {
            // Load the PostgreSQL JDBC driver (the .jar file in the lib folder)
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC driver not found. "
                    + "Make sure the postgresql .jar is on the classpath.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // ==================================================================
    // 1. READ - getAllStudents()
    // ==================================================================

    /**
     * Retrieves all records from the students table and prints them
     * as a table in the console.
     */
    public static void getAllStudents() {
        String sql = "SELECT student_id, first_name, last_name, email, enrollment_date "
                   + "FROM students ORDER BY student_id";

        // try-with-resources automatically closes conn, stmt and rs
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Print the table header
            System.out.printf("%-4s %-12s %-12s %-28s %-12s%n",
                    "ID", "First Name", "Last Name", "Email", "Enrolled");
            System.out.println("-".repeat(72));

            int count = 0;
            // Move through the result set one row at a time
            while (rs.next()) {
                int id = rs.getInt("student_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                Date enrolled = rs.getDate("enrollment_date"); // may be NULL

                System.out.printf("%-4d %-12s %-12s %-28s %-12s%n",
                        id, firstName, lastName, email,
                        (enrolled == null ? "N/A" : enrolled.toString()));
                count++;
            }

            if (count == 0) {
                System.out.println("(no students found)");
            }
            System.out.println(count + " record(s) found.");

        } catch (SQLException e) {
            System.out.println("Error retrieving students: " + e.getMessage());
        }
    }

    // ==================================================================
    // 2. CREATE - addStudent(first_name, last_name, email, enrollment_date)
    // ==================================================================

    /**
     * Inserts a new student record into the students table.
     * student_id is generated automatically by the database (SERIAL).
     *
     * @param firstName      student's first name (required)
     * @param lastName       student's last name (required)
     * @param email          student's email (required, must be unique)
     * @param enrollmentDate date in YYYY-MM-DD format (blank = no date)
     */
    public static void addStudent(String firstName, String lastName,
                                  String email, String enrollmentDate) {
        // Validate required fields before touching the database
        if (isBlank(firstName) || isBlank(lastName) || isBlank(email)) {
            System.out.println("Error: first name, last name and email are required.");
            return;
        }

        // Convert the date text to a java.sql.Date (or NULL if left blank)
        Date date = null;
        if (!isBlank(enrollmentDate)) {
            try {
                date = Date.valueOf(enrollmentDate.trim());
            } catch (IllegalArgumentException e) {
                System.out.println("Error: date must be in YYYY-MM-DD format.");
                return;
            }
        }

        // PreparedStatement with ? placeholders prevents SQL injection.
        // RETURNING gives back the ID the database generated.
        String sql = "INSERT INTO students (first_name, last_name, email, enrollment_date) "
                   + "VALUES (?, ?, ?, ?) RETURNING student_id";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, firstName.trim());
            pstmt.setString(2, lastName.trim());
            pstmt.setString(3, email.trim());
            pstmt.setDate(4, date); // setDate accepts null for a NULL value

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Student added successfully with ID "
                            + rs.getInt("student_id") + ".");
                }
            }

        } catch (SQLException e) {
            if (UNIQUE_VIOLATION.equals(e.getSQLState())) {
                System.out.println("Error: the email '" + email.trim() + "' is already in use.");
            } else {
                System.out.println("Error adding student: " + e.getMessage());
            }
        }
    }

    // ==================================================================
    // 3. UPDATE - updateStudentEmail(student_id, new_email)
    // ==================================================================

    /**
     * Updates the email address of the student with the given ID.
     *
     * @param studentId ID of the student to update
     * @param newEmail  the new email address (must be unique)
     */
    public static void updateStudentEmail(int studentId, String newEmail) {
        if (isBlank(newEmail)) {
            System.out.println("Error: new email cannot be empty.");
            return;
        }

        String sql = "UPDATE students SET email = ? WHERE student_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newEmail.trim());
            pstmt.setInt(2, studentId);

            // executeUpdate returns how many rows were changed
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Email for student " + studentId + " updated successfully.");
            } else {
                System.out.println("No student found with ID " + studentId + ".");
            }

        } catch (SQLException e) {
            if (UNIQUE_VIOLATION.equals(e.getSQLState())) {
                System.out.println("Error: the email '" + newEmail.trim() + "' is already in use.");
            } else {
                System.out.println("Error updating email: " + e.getMessage());
            }
        }
    }

    // ==================================================================
    // 4. DELETE - deleteStudent(student_id)
    // ==================================================================

    /**
     * Deletes the student with the given ID from the students table.
     *
     * @param studentId ID of the student to delete
     */
    public static void deleteStudent(int studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);

            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Student " + studentId + " deleted successfully.");
            } else {
                System.out.println("No student found with ID " + studentId + ".");
            }

        } catch (SQLException e) {
            System.out.println("Error deleting student: " + e.getMessage());
        }
    }

    // ==================================================================
    // Helper methods
    // ==================================================================

    /** Returns true if the text is null or only whitespace. */
    private static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * Keeps asking until the user types a valid whole number.
     * Used for reading student IDs and menu choices.
     */
    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    // ==================================================================
    // Main menu - lets the user run each function
    // ==================================================================

    public static void main(String[] args) {
        // Check the connection once at start-up so problems show immediately
        try (Connection conn = getConnection()) {
            System.out.println("Connected to PostgreSQL successfully! (database: "
                    + conn.getCatalog() + ")");
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database: " + e.getMessage());
            System.out.println("Check that PostgreSQL is running and the URL/USER/PASSWORD are correct.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("===== Student Management =====");
            System.out.println("1. Get all students");
            System.out.println("2. Add a student");
            System.out.println("3. Update a student's email");
            System.out.println("4. Delete a student");
            System.out.println("0. Exit");
            int choice = readInt(scanner, "Choose an option: ");
            System.out.println();

            switch (choice) {
                case 1:
                    getAllStudents();
                    break;
                case 2:
                    System.out.print("First name: ");
                    String firstName = scanner.nextLine();
                    System.out.print("Last name: ");
                    String lastName = scanner.nextLine();
                    System.out.print("Email: ");
                    String email = scanner.nextLine();
                    System.out.print("Enrollment date (YYYY-MM-DD, blank for none): ");
                    String date = scanner.nextLine();
                    addStudent(firstName, lastName, email, date);
                    break;
                case 3:
                    int updateId = readInt(scanner, "Student ID to update: ");
                    System.out.print("New email: ");
                    String newEmail = scanner.nextLine();
                    updateStudentEmail(updateId, newEmail);
                    break;
                case 4:
                    int deleteId = readInt(scanner, "Student ID to delete: ");
                    deleteStudent(deleteId);
                    break;
                case 0:
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please choose 0-4.");
            }
        }

        scanner.close();
    }
}
