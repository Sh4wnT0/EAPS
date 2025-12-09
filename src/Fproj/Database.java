package Fproj;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final String DB_URL = "jdbc:sqlite:employees.db";

    // ---------------- CONNECT ----------------
    public static Connection connect() {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // ---------------------Check if employee is registered upon Login-----------------------
    public static boolean checkEmployeeLogin(String empNo, String password) {
        String sql = "SELECT * FROM employees WHERE empNo = ? AND password = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, empNo);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // TRUE if found

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
 // ---------------- CREATE AS_RECORDS TABLE (ADMIN AND STAFF) ----------------
    public static void createASRecordsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS as_records ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "role TEXT NOT NULL CHECK (role IN ('admin', 'staff')), "
                + "address TEXT NOT NULL, "
                + "email TEXT NOT NULL, "
                + "contact TEXT NOT NULL, "
                + "position TEXT NOT NULL, "
                + "photo_path TEXT, "
                + "username TEXT NOT NULL UNIQUE, "
                + "password TEXT NOT NULL"  // Store hashed passwords here
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 // ---------------- INSERT DEFAULT ADMIN ----------------
    public static void insertDefaultAdmin() {
        String sql = "INSERT OR IGNORE INTO as_records (name, role, address, email, contact, position, username, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "Default Admin");
            pstmt.setString(2, "admin");
            pstmt.setString(3, "123 street");
            pstmt.setString(4, "admin@gmail.com"); 
            pstmt.setString(5, "09123456789"); 
            pstmt.setString(6, "CEO"); 
            pstmt.setString(7, "admin"); 
            pstmt.setString(8, "admin123"); 
            // Hash this in production: e.g., BCrypt.hashpw("admin123", BCrypt.gensalt())
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertASRecord(String name, String role, String username, String hashedPassword) {
        String sql = "INSERT INTO as_records (name, role, username, password) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, role);
            pstmt.setString(3, username);
            pstmt.setString(4, hashedPassword);  // Ensure password is hashed (e.g., BCrypt)
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // ---------------- CREATE EMPLOYEES TABLE ----------------
    public static void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS employees ("
                + "empNo TEXT PRIMARY KEY, "
                + "name TEXT NOT NULL, "
                + "role TEXT NOT NULL, "
                + "address TEXT NOT NULL, "
                + "email TEXT NOT NULL, "
                + "contact TEXT NOT NULL, "
                + "position TEXT NOT NULL, "
                + "employmentStatus TEXT NOT NULL, "
                + "dailyPay INTEGER NOT NULL, "
                + "password TEXT NOT NULL, "
                + "photo_path TEXT, "
                + "vacation_balance INTEGER DEFAULT 15, "
                + "sick_balance INTEGER DEFAULT 10, "
                + "emergency_balance INTEGER DEFAULT 7, "
                + "special_balance INTEGER DEFAULT 3"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- CREATE LEAVE REQUESTS TABLE ----------------
    public static void createLeaveTable() {
        String sql = "CREATE TABLE IF NOT EXISTS leave_requests ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "empNo TEXT NOT NULL, "
                + "name TEXT NOT NULL, "
                + "position TEXT NOT NULL, "
                + "leave_type TEXT NOT NULL, "
                + "start_date TEXT NOT NULL, "
                + "end_date TEXT NOT NULL, "
                + "reason TEXT NOT NULL, "
                + "status TEXT DEFAULT 'Pending', "
                + "submitted_date TEXT DEFAULT CURRENT_DATE"
                + ");";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void createACRTable() {
        String sql = "CREATE TABLE IF NOT EXISTS attendance_requests ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "empNo TEXT NOT NULL, "
                + "request_date TEXT NOT NULL, "
                + "time_in TEXT, "
                + "time_out TEXT, "
                + "status TEXT DEFAULT 'Pending'"
                + ");";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- CREATE ATTENDANCE RECORDS TABLE ----------------
    public static void createAttendanceTable() {
        String sql = "CREATE TABLE IF NOT EXISTS attendance_records ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "empNo TEXT NOT NULL, "
                + "date TEXT NOT NULL, "
                + "time_in TEXT, "
                + "time_out TEXT, "
                + "workday TEXT DEFAULT 'Regular', "
                + "second_time_out TEXT, "
                + "status TEXT,"
                + "total_update_count INTEGER DEFAULT 0"// Added for marking leave, present, etc.
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- CREATE NOTIFICATIONS TABLE ----------------
    public static void createNotificationsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS notifications ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "empNo TEXT NOT NULL, "
                + "message TEXT NOT NULL, "
                + "date TEXT DEFAULT CURRENT_DATE"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
 // ---------------- CREATE REQUESTS TABLE (FOR OT AND HOLIDAY) ----------------
    public static void createRequestsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS requests ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "empNo TEXT NOT NULL, "
                + "request_type TEXT NOT NULL, "  // 'OT' or 'Holiday'
                + "details TEXT NOT NULL, "       // For OT: hours; For Holiday: type (Special/Regular)
                + "start_date TEXT NOT NULL, "
                + "end_date TEXT, "                // Nullable for Holiday (no end date)
                + "reason TEXT NOT NULL, "
                + "status TEXT DEFAULT 'Pending', "
                + "submitted_date TEXT DEFAULT CURRENT_DATE"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- INSERT ATTENDANCE RECORD ----------------
    public static void insertTimeIn(String empNo, String date, String time_in) {
        String sql = "INSERT INTO attendance_records (empNo, date, time_in) "
                   + "VALUES (?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, empNo);
            pstmt.setString(2, date);
            pstmt.setString(3, time_in);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void insertTimeOut(String empNo, String date, String timeOut) {
        String sql = "UPDATE attendance_records "
                   + "SET time_out = ? "
                   + "WHERE empNo = ? AND date = ? AND time_out IS NULL";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, timeOut);
            pstmt.setString(2, empNo);
            pstmt.setString(3, date);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- FETCH LAST 5 DAYS OF RECORDS ----------------
    public static ResultSet getAttendanceRecords(String empNo) {
        String sql = "SELECT * FROM attendance_records "
                   + "WHERE empNo = ? "
                   + "AND date(date) >= date('now', '-5 days') "
                   + "ORDER BY date DESC";  // Changed from timestamp to date

        try {
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, empNo);
            return pstmt.executeQuery(); // caller must close conn later

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ---------------- INSERT EMPLOYEE FULL ----------------
    public static void insertEmployeeFull(
            String empNo, String name, String role, String address, String email,
            String contact, String position, String status,
            int salary, String password, String imagePath) {

        String sql = "INSERT INTO employees("
                + "empNo, name, role, address, email, contact, "
                + "position, employmentStatus, dailyPay, password, photo_path) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, empNo);
            pstmt.setString(2, name);
            pstmt.setString(3, role);
            pstmt.setString(4, address);
            pstmt.setString(5, email);
            pstmt.setString(6, contact);
            pstmt.setString(7, position);
            pstmt.setString(8, status);
            pstmt.setInt(9, salary);
            pstmt.setString(10, password);
            pstmt.setString(11, imagePath);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- INSERT LEAVE REQUEST ----------------
    public static void insertLeaveRequest(String empNo, String name, String position, String leaveType, String startDate, String endDate, String reason) {
        String sql = "INSERT INTO leave_requests (empNo, name, position, leave_type, start_date, end_date, reason) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empNo);
            pstmt.setString(2, name);
            pstmt.setString(3, position);
            pstmt.setString(4, leaveType);
            pstmt.setString(5, startDate);
            pstmt.setString(6, endDate);
            pstmt.setString(7, reason);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- GET LEAVE REQUESTS FOR AN EMPLOYEE ----------------
    public static ResultSet getLeaveRequests(String empNo) {
        String sql = "SELECT * FROM leave_requests WHERE empNo = ? ORDER BY id DESC";
        try {
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, empNo);
            return pstmt.executeQuery();  // Caller must close
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ---------------- GET LEAVE BALANCES FOR AN EMPLOYEE ----------------
    public static int[] getLeaveBalances(String empNo) {
        String sql = "SELECT vacation_balance, sick_balance, emergency_balance, special_balance FROM employees WHERE empNo = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empNo);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new int[]{rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4)};
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[]{0, 0, 0, 0};  // Default if not found
    }

    // ---------------- UPDATE LEAVE BALANCE ----------------
    public static void updateLeaveBalance(String empNo, String leaveType, int newBalance) {
        String column = leaveType.toLowerCase() + "_balance";
        String sql = "UPDATE employees SET " + column + " = ? WHERE empNo = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newBalance);
            pstmt.setString(2, empNo);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- GET LAST EMPLOYEE NUMBER ----------------
    public static int getLastEmployeeNumber() {
        String sql = "SELECT empNo FROM employees ORDER BY empNo DESC LIMIT 1";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                String last = rs.getString("empNo");
                return Integer.parseInt(last); // convert 
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0; // no records yet
    }

    // ---------------- GET 10 MOST RECENT LEAVE REQUESTS ----------------
    public static ResultSet getRecentLeaveRequests() throws SQLException {
        String sql = "SELECT id, empNo, leave_type, status FROM leave_requests ORDER BY submitted_date DESC LIMIT 10";
        Connection conn = connect();
        return conn.createStatement().executeQuery(sql);
    }

    // ---------------- SEARCH LEAVE REQUESTS BY EMPNO ----------------
    public static ResultSet searchLeaveRequestsByEmpNo(String empNo) throws SQLException {
        String sql = "SELECT id, empNo, leave_type, status FROM leave_requests WHERE empNo LIKE ?";
        Connection conn = connect();
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, "%" + empNo + "%");
        return pst.executeQuery();
    }

    // ---------------- GET LEAVE REQUEST DETAILS FOR VIEW ----------------
    public static ResultSet getLeaveRequestDetails(String requestId) throws SQLException {
        String sql = "SELECT * FROM leave_requests WHERE empNo = ?";
        Connection conn = connect();
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, requestId);
        return pst.executeQuery();
    }

    // ---------------- GET LEAVE REQUEST WITH EMPLOYEE DETAILS (FOR APPROVAL DIALOG) ----------------
    public static ResultSet getLeaveRequestWithEmployeeDetails(String empNo) throws SQLException {
        String sql = "SELECT lr.id, lr.empNo, e.name, lr.leave_type, lr.reason, lr.start_date, lr.end_date, lr.submitted_date, e.photo_path " +
                     "FROM leave_requests lr JOIN employees e ON lr.empNo = e.empNo WHERE lr.empNo = ?";
        Connection conn = connect();
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, empNo);
        return pst.executeQuery();
    }

    // ---------------- UPDATE LEAVE STATUS ----------------
    public static void updateLeaveStatus(int id, String status) {
        String sql = "UPDATE leave_requests SET status = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, status);
            pst.setInt(2, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

 // ---------------- PROCESS LEAVE APPROVAL (UPDATE STATUS, SEND NOTIFICATION, INSERT ATTENDANCE IF APPROVED) ----------------
    public static void processLeaveApproval(int id, String status, String empNo, String startDate, String endDate, String leaveType) {
        // If approving, check leave balance first
        if ("Approved".equals(status)) {
            // Calculate number of leave days
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);
            long days = ChronoUnit.DAYS.between(start, end) + 1;  // Inclusive of start and end

            // Get current balance for the leave type
            int[] balances = getLeaveBalances(empNo);
            int balanceIndex = getBalanceIndex(leaveType);
            if (balanceIndex == -1) {
                System.err.println("Invalid leave type: " + leaveType);
                return;
            }
            int currentBalance = balances[balanceIndex];

            // Check if balance is sufficient
            if (currentBalance < days) {
                System.err.println("Insufficient leave balance for " + leaveType + ". Current: " + currentBalance + ", Required: " + days);
                // Optionally, you can throw an exception or return a status, but for now, just log and skip
                return;
            }

            // Deduct balance
            int newBalance = currentBalance - (int) days;
            updateLeaveBalance(empNo, leaveType, newBalance);
        }

        // Update leave status
        String updateSql = "UPDATE leave_requests SET status = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(updateSql)) {
            pst.setString(1, status);
            pst.setInt(2, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return;  // Exit if update fails
        }

        // Send notification
        String message = "Your leave request has been " + status.toLowerCase() + ".";
        String notifSql = "INSERT INTO notifications (empNo, message) VALUES (?, ?)";
        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(notifSql)) {
            pst.setString(1, empNo);
            pst.setString(2, message);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // If approved, insert attendance records for each date
        if ("Approved".equals(status)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);

            String attendSql = "INSERT INTO attendance_records (empNo, date, time_in, time_out, status) VALUES (?, ?, '00:00:00 AM', '00:00:00 PM', 'Leave')";
            try (Connection conn = connect();
                 PreparedStatement pst = conn.prepareStatement(attendSql)) {
                for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                    pst.setString(1, empNo);
                    pst.setString(2, date.format(formatter));
                    pst.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Helper method to get balance index based on leave type
    private static int getBalanceIndex(String leaveType) {
        switch (leaveType.toLowerCase()) {
            case "vacation": return 0;
            case "sick": return 1;
            case "emergency": return 2;
            case "special": return 3;
            default: return -1;
        }
    }
    
    public static List<String> getRecentNotifications(String empNo) {
        List<String> list = new ArrayList<>();

        String sql = "SELECT message FROM notifications WHERE empNo = ? ORDER BY id DESC LIMIT 10";

        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, empNo);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                list.add(rs.getString("message"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
 // Get leave request with employee details by ID
    public static ResultSet getLeaveRequestWithEmployeeDetailsById(int id) throws SQLException {
        String sql = "SELECT lr.id, lr.empNo, e.name, lr.leave_type, lr.reason, lr.start_date, lr.end_date, lr.submitted_date, e.photo_path " +
                     "FROM leave_requests lr JOIN employees e ON lr.empNo = e.empNo WHERE lr.id = ?";
        Connection conn = connect();
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, id);
        return pst.executeQuery();
    }
 

    // ---------------- INSERT OT REQUEST ----------------
    public static void insertOTRequest(String empNo, String hours, String reason, String startDate, String endDate) {
        String sql = "INSERT INTO requests (empNo, request_type, details, start_date, end_date, reason) VALUES (?, 'OT', ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empNo);
            pstmt.setString(2, hours);
            pstmt.setString(3, startDate);
            pstmt.setString(4, endDate);
            pstmt.setString(5, reason);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- INSERT HOLIDAY REQUEST ----------------
    public static void insertHolidayRequest(String empNo, String holidayType, String reason, String startDate) {
        String sql = "INSERT INTO requests (empNo, request_type, details, start_date, reason) VALUES (?, 'Holiday', ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empNo);
            pstmt.setString(2, holidayType);
            pstmt.setString(3, startDate);
            pstmt.setString(4, reason);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- GET RECENT REQUESTS ----------------
    public static ResultSet getRecentRequests() throws SQLException {
        String sql = "SELECT id, empNo, request_type, status FROM requests ORDER BY submitted_date DESC LIMIT 10";
        Connection conn = connect();
        return conn.createStatement().executeQuery(sql);
    }

    // ---------------- SEARCH REQUESTS BY EMPNO ----------------
    public static ResultSet searchRequestsByEmpNo(String empNo) throws SQLException {
        String sql = "SELECT id, empNo, request_type, status FROM requests WHERE empNo LIKE ?";
        Connection conn = connect();
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, "%" + empNo + "%");
        return pst.executeQuery();
    }

    // ---------------- GET REQUEST WITH EMPLOYEE DETAILS BY ID ----------------
    public static ResultSet getRequestWithEmployeeDetailsById(int id) throws SQLException {
        String sql = "SELECT r.id, r.empNo, e.name, r.request_type, r.details, r.reason, r.start_date, r.end_date, r.submitted_date, e.photo_path " +
                     "FROM requests r JOIN employees e ON r.empNo = e.empNo WHERE r.id = ?";
        Connection conn = connect();
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, id);
        return pst.executeQuery();
    }

 // ---------------- PROCESS REQUEST APPROVAL ----------------
    public static void processRequestApproval(int id, String status, String empNo, String requestType, String details, String startDate, String endDate) {
        // Update request status
        String updateSql = "UPDATE requests SET status = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(updateSql)) {
            pst.setString(1, status);
            pst.setInt(2, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // Send notification
        String message = "Your " + requestType + " request has been " + status.toLowerCase() + ".";
        String notifSql = "INSERT INTO notifications (empNo, message) VALUES (?, ?)";
        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(notifSql)) {
            pst.setString(1, empNo);
            pst.setString(2, message);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // If approved, insert into attendance_records
        if ("Approved".equals(status)) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

            if ("OT".equals(requestType)) {

                java.time.LocalDate start = java.time.LocalDate.parse(startDate, formatter);
                java.time.LocalDate end = (endDate != null) ? java.time.LocalDate.parse(endDate, formatter) : start;

                int hours;
                try {
                    hours = Integer.parseInt(details);
                } catch (NumberFormatException e) {
                    hours = 0;
                }

                java.time.LocalTime baseTimeOut = java.time.LocalTime.of(17, 0, 0); // 05:00 PM
                java.time.LocalTime calculatedTimeOut = baseTimeOut.plusHours(hours);
                String timeOut = calculatedTimeOut.format(
                    java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a")
                );

                String def = "00:00:00 AM";
                String ot = "OT";

                try (Connection conn = connect()) {

                    for (java.time.LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

                        String dateStr = date.format(formatter);

                        // 1) Check if record exists
                        String checkSql = "SELECT 1 FROM attendance_records WHERE empNo=? AND date=?";
                        try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                            check.setString(1, empNo);
                            check.setString(2, dateStr);

                            ResultSet rs = check.executeQuery();

                            if (rs.next()) {
                                // 2) UPDATE existing record
                                String updateSql1 = """
                                    UPDATE attendance_records
                                    SET workday = ?, second_time_out = ?
                                    WHERE empNo = ? AND date = ?
                                """;
                                try (PreparedStatement pst = conn.prepareStatement(updateSql1)) {
                                    pst.setString(1, ot);
                                    pst.setString(2, timeOut);
                                    pst.setString(3, empNo);
                                    pst.setString(4, dateStr);
                                    pst.executeUpdate();
                                }

                            } else {
                                // 3) INSERT if no row exists
                                String insertSql = """
                                    INSERT INTO attendance_records
                                    (empNo, date, time_in, workday, second_time_out)
                                    VALUES (?, ?, ?, ?, ?)
                                """;
                                try (PreparedStatement pst = conn.prepareStatement(insertSql)) {
                                    pst.setString(1, empNo);
                                    pst.setString(2, dateStr);
                                    pst.setString(3, def);
                                    pst.setString(4, ot);
                                    pst.setString(5, timeOut);
                                    pst.executeUpdate();
                                }
                            }
                        }
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } else if ("Holiday".equals(requestType)) {

                String holidayStatus = "Special".equals(details) ? "SH" : "RH";
                String def = "00:00:00 AM";

                try (Connection conn = connect()) {

                    String checkSql = "SELECT 1 FROM attendance_records WHERE empNo=? AND date=?";
                    try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                        check.setString(1, empNo);
                        check.setString(2, startDate);

                        ResultSet rs = check.executeQuery();

                        if (rs.next()) {
                            // UPDATE instead of INSERT
                            String updateSql1 = """
                                UPDATE attendance_records
                                SET workday = ?
                                WHERE empNo = ? AND date = ?
                            """;
                            try (PreparedStatement pst = conn.prepareStatement(updateSql1)) {
                                pst.setString(1, holidayStatus);
                                pst.setString(2, empNo);
                                pst.setString(3, startDate);
                                pst.executeUpdate();
                            }
                        } else {
                            // INSERT only if no record exists
                            String insertSql = """
                                INSERT INTO attendance_records
                                (empNo, date, time_in, workday)
                                VALUES (?, ?, ?, ?)
                            """;
                            try (PreparedStatement pst = conn.prepareStatement(insertSql)) {
                                pst.setString(1, empNo);
                                pst.setString(2, startDate);
                                pst.setString(3, def);
                                pst.setString(4, holidayStatus);
                                pst.executeUpdate();
                            }
                        }
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

 // ---------------- CREATE UNIFIED LOGIN VIEW ----------------
    public static void createUnifiedLoginView() {
        String sql = "CREATE VIEW IF NOT EXISTS unified_login AS "
                + "SELECT empNo AS identifier, password, 'employee' AS login_type, name FROM employees "
                + "UNION ALL "
                + "SELECT username AS identifier, password, 'admin_staff' AS login_type, name FROM as_records";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

 // NEW: Update an existing leave request (for editing pending requests)
    public static boolean updateLeaveRequest(int id, String leaveType, String startDate, String endDate, String reason) {
        String sql = "UPDATE leave_requests SET leave_type = ?, start_date = ?, end_date = ?, reason = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, leaveType);
            pstmt.setString(2, startDate);
            pstmt.setString(3, endDate);
            pstmt.setString(4, reason);
            pstmt.setInt(5, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Return true if update was successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // NEW: Delete a leave request
    public static boolean deleteLeaveRequest(int id) {
        String sql = "DELETE FROM leave_requests WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Return true if deletion was successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // IMPLEMENTED: Get a single leave request by ID (for editing)
    public static ResultSet getLeaveRequestById(int requestId) {
        String sql = "SELECT * FROM leave_requests WHERE id = ?";
        try {
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, requestId);
            return pstmt.executeQuery(); // Caller must close ResultSet and PreparedStatement
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    //Update Employee by admin
    public static boolean updateEmployeeDetails(String empNo, String position, String status, int dailyPay) {
        String sql = "UPDATE employees SET position = ?, employmentStatus = ?, dailyPay = ? WHERE empNo = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, position);
            pstmt.setString(2, status);
            pstmt.setInt(3, dailyPay);
            pstmt.setString(4, empNo);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}