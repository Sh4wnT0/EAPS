package Fproj;

import java.sql.*;
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

    // ---------------- CREATE EMPLOYEES TABLE ----------------
    public static void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS employees ("
                + "empNo TEXT PRIMARY KEY, "
                + "name TEXT NOT NULL, "
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

    // ---------------- CREATE ATTENDANCE RECORDS TABLE ----------------
    public static void createAttendanceTable() {
        String sql = "CREATE TABLE IF NOT EXISTS attendance_records ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "empNo TEXT NOT NULL, "
                + "date TEXT NOT NULL, "
                + "time_in TEXT NOT NULL, "
                + "time_out TEXT, "
                + "status TEXT"  // Added for marking leave, present, etc.
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
            String empNo, String name, String address, String email,
            String contact, String position, String status,
            int salary, String password, String imagePath) {

        String sql = "INSERT INTO employees("
                + "empNo, name, address, email, contact, "
                + "position, employmentStatus, dailyPay, password, photo_path) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, empNo);
            pstmt.setString(2, name);
            pstmt.setString(3, address);
            pstmt.setString(4, email);
            pstmt.setString(5, contact);
            pstmt.setString(6, position);
            pstmt.setString(7, status);
            pstmt.setInt(8, salary);
            pstmt.setString(9, password);
            pstmt.setString(10, imagePath);

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
    public static ResultSet getLeaveRequestDetails(String empNo) throws SQLException {
        String sql = "SELECT * FROM leave_requests WHERE empNo = ?";
        Connection conn = connect();
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, empNo);
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

}