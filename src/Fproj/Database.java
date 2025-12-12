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

    public static void insertASRecord(String name, String role, String username, String hashedPassword, String address, String email, String contact, String position) {
        // Ensure your as_records table has these columns. 
        // If not, run ALTER TABLE in SQL browser first.
        String sql = "INSERT INTO as_records (name, role, username, password, address, email, contact, position) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, role);
            pstmt.setString(3, username);
            pstmt.setString(4, hashedPassword);
            pstmt.setString(5, address);
            pstmt.setString(6, email);
            pstmt.setString(7, contact);
            pstmt.setString(8, position);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 // ---------------- GET STAFF POSITION ----------------
    public static String getASPosition(String username) {
        String sql = "SELECT position FROM as_records WHERE username = ?";
        
        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                // Return the actual position (e.g., "HR Officer", "Accountant")
                return rs.getString("position");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Default fallback if no position found (Assumes full Admin)
        return "Admin"; 
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
    
 // ---------------- CREATE ACCOUNT REQUESTS TABLE ----------------
    public static void createAccountRequestsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS account_requests ("
                   + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                   + "full_name TEXT NOT NULL, "
                   + "email TEXT NOT NULL, "
                   + "resume_path TEXT NOT NULL, " // We store the file path, not the blob
                   + "status TEXT DEFAULT 'Pending', "
                   + "request_date TEXT DEFAULT CURRENT_DATE"
                   + ");";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
 // ---------------- GET ACCOUNT REQUESTS ----------------
    public static ResultSet getAccountRequests() {
        // Returns ID, Name, Email, Date, and the File Path
        String sql = "SELECT id, full_name, email, request_date, resume_path FROM account_requests WHERE status='Pending' ORDER BY id DESC";
        try {
            Connection conn = connect();
            return conn.createStatement().executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ---------------- DELETE ACCOUNT REQUEST ----------------
    public static void deleteAccountRequest(int id) {
        String sql = "DELETE FROM account_requests WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- INSERT ACCOUNT REQUEST ----------------
    public static boolean insertAccountRequest(String name, String email, String path) {
        String sql = "INSERT INTO account_requests (full_name, email, resume_path) VALUES (?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, name);
            pst.setString(2, email);
            pst.setString(3, path);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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
 // ---------------- CREATE UNIFIED REQUESTS TABLE ----------------
    public static void createRequestsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS requests ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "empNo TEXT NOT NULL, "
                + "name TEXT, "
                + "email TEXT, "               // <--- NEW COLUMN HERE
                + "request_type TEXT NOT NULL, " 
                + "details TEXT, "            
                + "start_date TEXT, "         
                + "end_date TEXT, "           
                + "reason TEXT, "             
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
 // =============================================================
    // ================== NEW PAYSLIP METHODS ======================
    // =============================================================

    // 1. Create the Payslips Table (Call this once in your main setup)
    public static void createPayslipsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS payslips ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "empNo TEXT NOT NULL, "
                + "period_month TEXT NOT NULL, "
                + "period_year TEXT NOT NULL, "
                + "total_present INTEGER, "
                + "total_absent INTEGER, "
                + "total_late INTEGER, "
                + "total_undertime INTEGER, "
                + "gross_pay REAL, "
                + "deductions REAL, "
                + "net_pay REAL, "
                + "date_generated TEXT DEFAULT CURRENT_DATE"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 2. Save Payslip and Send Notification
 // Changed from 'void' to 'boolean' so we can check if it worked
    public static boolean savePayslip(String empNo, String month, String year, 
                                   int present, int absent, int late, int undertime, 
                                   double gross, double ded, double net) {
        
        String sql = "INSERT INTO payslips (empNo, period_month, period_year, total_present, total_absent, total_late, total_undertime, gross_pay, deductions, net_pay) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        // We also want to notify the employee
        String notifSql = "INSERT INTO notifications (empNo, message) VALUES (?, ?)";
        
        try (Connection conn = connect()) {
            
            // 1. Insert Payslip
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, empNo);
                pst.setString(2, month);
                pst.setString(3, year);
                pst.setInt(4, present);
                pst.setInt(5, absent);
                pst.setInt(6, late);
                pst.setInt(7, undertime);
                pst.setDouble(8, gross);
                pst.setDouble(9, ded);
                pst.setDouble(10, net);
                
                int rowsAffected = pst.executeUpdate();
                
                // If no rows were added, return false immediately
                if (rowsAffected == 0) return false;
            }

            // 2. Insert Notification (Optional, but good for UX)
            try (PreparedStatement pst = conn.prepareStatement(notifSql)) {
                String msg = "Payslip for " + month + " " + year + " has been generated.";
                pst.setString(1, empNo);
                pst.setString(2, msg);
                pst.executeUpdate();
            }

            return true; // Success!

        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Failed
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
 // ---------------- GENERIC REQUEST INSERT ----------------
    public static boolean insertRequest(String empNo, String name, String email, String type, String details, String start, String end, String reason) {
        // Added 'email' to the query
        String sql = "INSERT INTO requests (empNo, name, email, request_type, details, start_date, end_date, reason) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, empNo);
            pst.setString(2, name);
            pst.setString(3, email);   // <--- Set the email here
            pst.setString(4, type);    
            pst.setString(5, details); 
            pst.setString(6, start);
            pst.setString(7, end);
            pst.setString(8, reason);
            
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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

    public static void createPasswordResetRequest(String userId, String username, String email) {
        // 1. Check Employees Table
        String sqlEmp = "SELECT name FROM employees WHERE empNo = ? AND email = ?";
        // 2. Check Admin/Staff Table
        String sqlAdmin = "SELECT name FROM as_records WHERE username = ? AND email = ?";

        try (Connection conn = connect()) {
            String foundName = null;
            String foundRole = null;

            // Check Employee
            try (PreparedStatement pst = conn.prepareStatement(sqlEmp)) {
                pst.setString(1, userId); 
                pst.setString(2, email);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    foundName = rs.getString("name");
                    foundRole = "Employee";
                }
            }

            // If not found, Check Admin/Staff
            if (foundName == null) {
                try (PreparedStatement pst = conn.prepareStatement(sqlAdmin)) {
                    pst.setString(1, userId);
                    pst.setString(2, email);
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        foundName = rs.getString("name");
                        foundRole = "Admin/Staff";
                    }
                }
            }

            if (foundName != null) {
                // A. INSERT INTO REQUESTS TABLE (For records)
                insertRequest(
                    userId, 
                    foundName, 
                    email, 
                    "Password Reset", 
                    "Role: " + foundRole, 
                    java.time.LocalDate.now().toString(), 
                    null, 
                    "User forgot password"
                );
                
                // B. SEND NOTIFICATION TO ADMIN (For the Dialog)
                // This targets the username "admin" (from insertDefaultAdmin)
                String notifMsg = "Password Reset Requested: " + foundName + " (" + userId + ")";
                sendNotification("admin", notifMsg);
                
                System.out.println("Password reset request logged and Admin notified.");
                
            } else {
                System.out.println("User details do not match any record.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
 // ---------------- GET USER EMAIL (Smart Lookup) ----------------
    public static String getUserEmail(String userId) {
        // 1. Try Employee Table
        String sqlEmp = "SELECT email FROM employees WHERE empNo = ?";
        try (Connection conn = connect(); PreparedStatement pst = conn.prepareStatement(sqlEmp)) {
            pst.setString(1, userId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getString("email");
        } catch (SQLException e) { e.printStackTrace(); }

        // 2. Try Admin/Staff Table
        String sqlStaff = "SELECT email FROM as_records WHERE username = ?";
        try (Connection conn = connect(); PreparedStatement pst = conn.prepareStatement(sqlStaff)) {
            pst.setString(1, userId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getString("email");
        } catch (SQLException e) { e.printStackTrace(); }

        return null; // Not found
    }
    
 // ---------------- GET PENDING PASSWORD RESET REQUESTS ----------------
    // Returns List of [RequestID, EmpNo, Name, Email, Date]
    public static java.util.List<String[]> getPendingPasswordResets() {
        java.util.List<String[]> list = new java.util.ArrayList<>();
        String sql = "SELECT id, empNo, name, email, submitted_date FROM requests WHERE request_type = 'Password Reset' AND status = 'Pending'";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("empNo"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("submitted_date")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ---------------- UPDATE PASSWORD (FOR RESET) ----------------
    public static void updateUserPassword(String identifier, String newPassword) {
        // Try updating employee table first
        String sqlEmp = "UPDATE employees SET password = ? WHERE empNo = ?";
        // Try updating admin/staff table
        String sqlAdmin = "UPDATE as_records SET password = ? WHERE username = ?";

        try (Connection conn = connect()) {
            boolean updated = false;
            
            try (PreparedStatement pst = conn.prepareStatement(sqlEmp)) {
                pst.setString(1, newPassword);
                pst.setString(2, identifier);
                if (pst.executeUpdate() > 0) updated = true;
            }

            if (!updated) {
                try (PreparedStatement pst = conn.prepareStatement(sqlAdmin)) {
                    pst.setString(1, newPassword);
                    pst.setString(2, identifier);
                    pst.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void performPasswordReset(String requestId, String userId, String newPassword) {
        updateUserPassword(userId, newPassword);
        updateRequestStatus(requestId, "Completed");
    }

    // ---------------- UPDATE REQUEST STATUS ----------------
    public static void updateRequestStatus(String requestId, String status) {
        String sql = "UPDATE requests SET status = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, status);
            pst.setString(2, requestId);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 // ---------------- GET ALL REQUESTS (FOR NOTIFICATION/DISPLAY) ----------------
    public static List<String> getAllRequests() {
        List<String> requestList = new ArrayList<>();
        // Query to select relevant columns
        String sql = "SELECT id, empNo, name, request_type, status, submitted_date FROM requests ORDER BY submitted_date DESC";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Loop through results
            while (rs.next()) {
                int id = rs.getInt("id");
                String empNo = rs.getString("empNo");
                String name = rs.getString("name");
                String type = rs.getString("request_type");
                String status = rs.getString("status");
                String date = rs.getString("submitted_date");

                // Format the output string (You can customize this format)
                String requestInfo = String.format("ID: %d | Emp: %s (%s) | Type: %s | Status: %s | Date: %s", 
                                                   id, empNo, (name != null ? name : "N/A"), type, status, date);
                
                requestList.add(requestInfo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            requestList.add("Error fetching requests: " + e.getMessage());
        }
        return requestList;
    }
    
 // ---------------- GENERIC SEND NOTIFICATION ----------------
    public static void sendNotification(String targetUser, String message) {
        String sql = "INSERT INTO notifications (empNo, message) VALUES (?, ?)";
        
        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, targetUser); // The empNo or Username receiving the alert
            pst.setString(2, message);
            pst.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 // ---------------- FETCH NOTIFICATIONS WITH DATE ----------------
    // Returns a list of String arrays: [Message, Date]
    public static java.util.List<String[]> getNotificationsWithDate(String empNo) {
        java.util.List<String[]> list = new java.util.ArrayList<>();
        String sql = "SELECT message, date FROM notifications WHERE empNo = ? ORDER BY id DESC LIMIT 20";

        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, empNo);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("message"),
                    rs.getString("date")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // Optional: Clear notifications
    public static void clearNotifications(String empNo) {
        String sql = "DELETE FROM notifications WHERE empNo = ?";
        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, empNo);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 // ---------------- CHECK USER EXISTENCE (FORGOT PASSWORD) ----------------
    // Returns "employees", "as_records", or null
    public static String checkUserTable(String identifier, String email) {
        
        // 1. Check 'employees' table first
        // Note: employees table uses 'empNo', not 'username'. 
        // We treat the input identifier as empNo here.
        String empQuery = "SELECT empNo FROM employees WHERE empNo = ? AND email = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(empQuery)) {
            
            pstmt.setString(1, identifier);
            pstmt.setString(2, email);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return "employees"; // Found in employees table
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }

        // 2. If not found, check 'as_records' table
        // Note: as_records table DOES have a 'username' column.
        String asQuery = "SELECT username FROM as_records WHERE username = ? AND email = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(asQuery)) {
            
            pstmt.setString(1, identifier);
            pstmt.setString(2, email);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return "as_records"; // Found in as_records table
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }

        return null; // User not found in either table
    }
    
 // ==========================================
    //           ANALYTICS QUERIES
    // ==========================================

    // 1. WORKFORCE COUNTS
    public static int countTotalEmployees() {
        return getCount("SELECT COUNT(*) FROM employees");
    }

    public static int countEmployeesByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM employees WHERE employmentStatus = ?";
        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, status);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // 2. ATTENDANCE TODAY
    public static int countPresentToday() {
        // Count records where time_in is NOT NULL and NOT the placeholder "00:00:00 AM"
        String sql = "SELECT COUNT(*) FROM attendance_records WHERE date = CURRENT_DATE AND time_in IS NOT NULL AND time_in != '00:00:00 AM' AND status != 'Absent'";
        return getCount(sql);
    }

    public static int countLateToday() {
        String sql = "SELECT COUNT(*) FROM attendance_records WHERE date = CURRENT_DATE AND status LIKE '%Late%'";
        return getCount(sql);
    }

    public static int countOnLeaveToday() {
        // Checks leave_requests for approved leaves spanning today
        String sql = "SELECT COUNT(*) FROM leave_requests WHERE status = 'Approved' AND date('now') BETWEEN start_date AND end_date";
        return getCount(sql);
    }

    // 3. PENDING ACTIONS (To-Do List)
    public static int countPendingLeaves() {
        return getCount("SELECT COUNT(*) FROM leave_requests WHERE status = 'Pending'");
    }

    public static int countPendingOT() {
        return getCount("SELECT COUNT(*) FROM requests WHERE (request_type = 'OT' OR request_type = 'Holiday') AND status = 'Pending'");
    }

    public static int countPendingResets() {
        return getCount("SELECT COUNT(*) FROM requests WHERE request_type = 'Password Reset' AND status = 'Pending'");
    }
    
 // ---------------- INSERT PAYSLIP REQUEST (FIXED) ----------------
    public static void insertPayslipRequest(String empNo) {
        String sql = "SELECT name, email FROM employees WHERE empNo = ?";
        
        // FIX APPLIED: Added 'start_date' to the insert list and used 'CURRENT_DATE' as the value
        String insertSql = "INSERT INTO requests (empNo, name, email, request_type, details, reason, status, submitted_date, start_date) " +
                           "VALUES (?, ?, ?, 'Payslip', 'Request for latest payslip', 'Employee requested copy', 'Pending', CURRENT_DATE, CURRENT_DATE)";
        
        try (Connection conn = connect()) {
            String name = "";
            String email = "";
            
            // 1. Get Employee Details
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, empNo);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    name = rs.getString("name");
                    email = rs.getString("email");
                }
            }
            
            // 2. Insert Request
            try (PreparedStatement pst = conn.prepareStatement(insertSql)) {
                pst.setString(1, empNo);
                pst.setString(2, name);
                pst.setString(3, email);
                pst.executeUpdate();
            }
            
            // 3. Notify Admin
            sendNotification("admin", "Payslip Requested by: " + name + " (" + empNo + ")");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

 // --- NEW ANALYTICS QUERIES ---

    // 1. Count Absent Employees Today (Total Active - Present - On Leave)
    public static int countAbsentToday() {
        int total = countTotalEmployees();
        int present = countPresentToday();
        int onLeave = countOnLeaveToday();
        // Assuming anyone not present and not on leave is absent
        // (This is a simplified calculation suitable for a dashboard snapshot)
        int absent = total - (present + onLeave);
        return Math.max(0, absent); 
    }

    // 2. Count Pending Payslip Requests
    public static int countPendingPayslipRequests() {
        return getCount("SELECT COUNT(*) FROM requests WHERE request_type = 'Payslip' AND status = 'Pending'");
    }

    // Helper for simple count queries
    private static int getCount(String sql) {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
    
 // ---------------- CREATE COMPANY INFO TABLE ----------------
    public static void createCompanyInfoTable() {
        // Single row table logic
        String sql = "CREATE TABLE IF NOT EXISTS company_info ("
                   + "id INTEGER PRIMARY KEY CHECK (id = 1), " // Enforce single row
                   + "name TEXT, "
                   + "address TEXT, "
                   + "contact TEXT, "
                   + "logo_path TEXT"
                   + ");";
        
        // Announcements table
        String sql2 = "CREATE TABLE IF NOT EXISTS announcements ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "title TEXT, "
                    + "message TEXT, "
                    + "date TEXT DEFAULT CURRENT_DATE"
                    + ");";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            stmt.execute(sql2);
            // Initialize empty row 1 if not exists
            stmt.execute("INSERT OR IGNORE INTO company_info (id, name, address, contact) VALUES (1, 'Your Company', 'Address Here', 'Contact Here')");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ---------------- GETTERS & SETTERS FOR SETTINGS ----------------
    public static String[] getCompanyInfo() {
        String sql = "SELECT name, address, contact, logo_path FROM company_info WHERE id = 1";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new String[]{rs.getString("name"), rs.getString("address"), rs.getString("contact"), rs.getString("logo_path")};
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new String[]{"", "", "", ""};
    }

    public static void updateCompanyInfo(String name, String addr, String contact, String path) {
        String sql = "UPDATE company_info SET name=?, address=?, contact=?, logo_path=? WHERE id=1";
        try (Connection conn = connect(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, name); pst.setString(2, addr); pst.setString(3, contact); pst.setString(4, path);
            pst.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void addAnnouncement(String title, String msg) {
        String sql = "INSERT INTO announcements (title, message, date) VALUES (?, ?, CURRENT_DATE)";
        try (Connection conn = connect(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, title); pst.setString(2, msg); pst.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    public static void deleteAnnouncement(int id) {
        try (Connection conn = connect(); PreparedStatement pst = conn.prepareStatement("DELETE FROM announcements WHERE id=?")) {
            pst.setInt(1, id); pst.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
 // ---------------- GET ALL PENDING REQUESTS (FOR ADMIN NOTIFICATIONS) ----------------
    // Returns list of: [ReqID, EmpNo, Name, Type, Details, Date]
 // ---------------- GET ALL PENDING REQUESTS (CONSOLIDATED) ----------------
    // Returns: [ReqID, EmpNo, Name, Type, Date, TableName]
    public static java.util.List<String[]> getAdminPendingRequests() {
        java.util.List<String[]> list = new java.util.ArrayList<>();
        
        // 1. General Requests (OT, Holiday, Payslip, Password Reset)
        String q1 = "SELECT id, empNo, name, request_type, submitted_date, 'requests' as source_table FROM requests WHERE status = 'Pending'";
        
        // 2. Leave Requests
        String q2 = "SELECT id, empNo, name, 'Leave' as request_type, submitted_date, 'leave_requests' as source_table FROM leave_requests WHERE status = 'Pending'";
        
        // 3. ACR (Attendance Correction) - Needs JOIN for Name
        String q3 = "SELECT r.id, r.empNo, e.name, 'ACR' as request_type, r.request_date as submitted_date, 'attendance_requests' as source_table " +
                    "FROM attendance_requests r JOIN employees e ON r.empNo = e.empNo WHERE r.status = 'Pending'";

        // Combine all using UNION ALL
        String sql = q1 + " UNION ALL " + q2 + " UNION ALL " + q3 + " ORDER BY submitted_date DESC";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("id")),      // 0
                    rs.getString("empNo"),                // 1
                    rs.getString("name"),                 // 2
                    rs.getString("request_type"),         // 3 (Type)
                    rs.getString("submitted_date"),       // 4
                    rs.getString("source_table")          // 5 (Used for navigation logic)
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
 // ---------------- DELETE EMPLOYEE ----------------
    public static boolean deleteEmployee(String empNo) {
        String sql = "DELETE FROM employees WHERE empNo = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, empNo);
            int rowsAffected = pstmt.executeUpdate();
            
            // Optional: Also clean up related records to prevent "orphan" data
            if (rowsAffected > 0) {
                deleteRelatedRecords(empNo); 
            }
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper to clean up related data (Optional but recommended)
    private static void deleteRelatedRecords(String empNo) {
        try (Connection conn = connect()) {
            // Delete from other tables
            conn.createStatement().execute("DELETE FROM attendance_records WHERE empNo='" + empNo + "'");
            conn.createStatement().execute("DELETE FROM leave_requests WHERE empNo='" + empNo + "'");
            conn.createStatement().execute("DELETE FROM requests WHERE empNo='" + empNo + "'");
            conn.createStatement().execute("DELETE FROM notifications WHERE empNo='" + empNo + "'");
            conn.createStatement().execute("DELETE FROM payslips WHERE empNo='" + empNo + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 // ---------------- MARK UNPAID LEAVE (DIRECT INSERT) ----------------
    public static void markUnpaidLeave(String empNo, String date) {
        // We check if a record exists first to avoid primary key collisions or duplicates
        String checkSql = "SELECT id FROM attendance_records WHERE empNo = ? AND date = ?";
        
        // If it exists, we update it; if not, we insert it.
        String insertSql = "INSERT INTO attendance_records (empNo, date, time_in, time_out, status, workday) " +
                           "VALUES (?, ?, '00:00:00 AM', '00:00:00 PM', 'Absent', 'UL')";
                           
        String updateSql = "UPDATE attendance_records SET status = 'Absent', workday = 'UL', " +
                           "time_in = '00:00:00 AM', time_out = '00:00:00 PM' WHERE empNo = ? AND date = ?";

        try (Connection conn = connect()) {
            boolean exists = false;
            try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                check.setString(1, empNo);
                check.setString(2, date);
                ResultSet rs = check.executeQuery();
                if (rs.next()) exists = true;
            }

            if (exists) {
                try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                    upd.setString(1, empNo);
                    upd.setString(2, date);
                    upd.executeUpdate();
                }
            } else {
                try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                    ins.setString(1, empNo);
                    ins.setString(2, date);
                    ins.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 

 // 1. Get a specific request by ID (for pre-filling the Edit Dialog)
 public static ResultSet getRequestById(int id) {
     String sql = "SELECT * FROM requests WHERE id = ?";
     try {
         Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         pstmt.setInt(1, id);
         return pstmt.executeQuery(); 
     } catch (SQLException e) {
         e.printStackTrace();
         return null;
     }
 }

 // 2. Update an existing request
 public static void updateRequest(int id, String type, String details, String start, String end, String reason) {
     String sql = "UPDATE requests SET request_type = ?, details = ?, start_date = ?, end_date = ?, reason = ? WHERE id = ?";
     try (Connection conn = connect();
          PreparedStatement pstmt = conn.prepareStatement(sql)) {
         pstmt.setString(1, type);
         pstmt.setString(2, details); // Hours for OT, Type for Holiday
         pstmt.setString(3, start);
         pstmt.setString(4, end);
         pstmt.setString(5, reason);
         pstmt.setInt(6, id);
         pstmt.executeUpdate();
     } catch (SQLException e) {
         e.printStackTrace();
     }
 }

 // 3. Delete a request
 public static void deleteRequest(int id) {
     String sql = "DELETE FROM requests WHERE id = ?";
     try (Connection conn = connect();
          PreparedStatement pstmt = conn.prepareStatement(sql)) {
         pstmt.setInt(1, id);
         pstmt.executeUpdate();
     } catch (SQLException e) {
         e.printStackTrace();
     }
 }
}