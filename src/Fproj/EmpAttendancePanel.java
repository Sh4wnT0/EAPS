package Fproj;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EmpAttendancePanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private String empNo;  // pass from login

    public EmpAttendancePanel(String empNo) {
        this.empNo = empNo;

        setLayout(null);
        setBackground(Color.WHITE);

        JButton btnIn = new JButton("Time-In");
        btnIn.setFont(new Font("Serif", Font.BOLD, 20));
        btnIn.setBackground(new Color(63, 255, 38));
        btnIn.setBounds(40, 30, 180, 50);
        add(btnIn);

        JButton btnOut = new JButton("Time-Out");
        btnOut.setBackground(new Color(255, 32, 32));
        btnOut.setFont(new Font("Serif", Font.BOLD, 20));
        btnOut.setBounds(460, 30, 180, 50);
        add(btnOut);

        // TABLE FOR RECORDS
        model = new DefaultTableModel(new String[]{"Date", "Time-In", "Time-Out", "Status"}, 0);
        table = new JTable(model);

        JScrollPane scroll =  new JScrollPane(table);
        scroll.setBounds(40, 90, 600, 300);
        add(scroll);

        // Button Actions
        btnIn.addActionListener(e -> saveRecord("IN"));
        btnOut.addActionListener(e -> saveRecord("OUT"));

        loadTable();
    }

    // SAVE TIME IN/OUT (Enforces one time-in and one time-out per day)
    private void saveRecord(String type) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:employees.db")) {

            String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String now = new SimpleDateFormat("hh:mm:ss a").format(new Date());

            if (type.equals("IN")) {
                // Check if a record already exists for today
                try (PreparedStatement checkPs = con.prepareStatement(
                        "SELECT id FROM attendance_records WHERE empNo=? AND date=?"
                )) {
                    checkPs.setString(1, empNo);
                    checkPs.setString(2, today);
                    try (ResultSet checkRs = checkPs.executeQuery()) {
                        if (checkRs.next()) {
                            // Record exists - prevent duplicate time-in
                            JOptionPane.showMessageDialog(null, "You have already timed in today!");
                            return;
                        }
                    }
                }

                // Insert new record
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO attendance_records(empNo, date, time_in) VALUES (?, ?, ?)"
                )) {
                    ps.setString(1, empNo);
                    ps.setString(2, today);
                    ps.setString(3, now);
                    ps.executeUpdate();
                }

                // Update status to "Absent" since time_out is null
                try (PreparedStatement statusPs = con.prepareStatement(
                        "UPDATE attendance_records SET status = 'Absent' WHERE empNo = ? AND date = ?"
                )) {
                    statusPs.setString(1, empNo);
                    statusPs.setString(2, today);
                    statusPs.executeUpdate();
                }

            } else {  // "OUT"
                // Check if a record exists for today with time_in set AND time_out is NOT set
                try (PreparedStatement checkPs = con.prepareStatement(
                        "SELECT id FROM attendance_records WHERE empNo=? AND date=? AND time_in IS NOT NULL AND time_out IS NULL"
                )) {
                    checkPs.setString(1, empNo);
                    checkPs.setString(2, today);
                    try (ResultSet checkRs = checkPs.executeQuery()) {
                        if (!checkRs.next()) {
                            // No valid record to update (either no time-in or time-out already set)
                            JOptionPane.showMessageDialog(null, "No time-in found for today, or you have already timed out!");
                            return;
                        }
                    }
                }

                // Update time_out (only if not already set)
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE attendance_records SET time_out=? WHERE empNo=? AND date=? AND time_out IS NULL"
                )) {
                    ps.setString(1, now);
                    ps.setString(2, empNo);
                    ps.setString(3, today);
                    int rowsUpdated = ps.executeUpdate();
                    if (rowsUpdated == 0) {
                        JOptionPane.showMessageDialog(null, "Time-out already recorded for today!");
                        return;
                    }
                }

                // Calculate and update status
                try (PreparedStatement getPs = con.prepareStatement(
                        "SELECT time_in, time_out FROM attendance_records WHERE empNo = ? AND date = ?"
                )) {
                    getPs.setString(1, empNo);
                    getPs.setString(2, today);
                    try (ResultSet getRs = getPs.executeQuery()) {
                        if (getRs.next()) {
                            String timeIn = getRs.getString("time_in");
                            String timeOut = getRs.getString("time_out");
                            String status = calculateStatus(timeIn, timeOut, today);
                            try (PreparedStatement statusPs = con.prepareStatement(
                                    "UPDATE attendance_records SET status = ? WHERE empNo = ? AND date = ?"
                            )) {
                                statusPs.setString(1, status);
                                statusPs.setString(2, empNo);
                                statusPs.setString(3, today);
                                statusPs.executeUpdate();
                            }
                        }
                    }
                }
            }

            loadTable();  // Refresh table
            JOptionPane.showMessageDialog(null, "Recorded successfully!");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving record: " + ex.getMessage());
        }
    }

    private String calculateStatus(String timeInStr, String timeOutStr, String dateStr) {
        // Check if workday (Monday to Friday)
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
            java.time.DayOfWeek day = date.getDayOfWeek();
            boolean isWorkday = !(day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY);
            if (!isWorkday) {
                return "";  // Not a workday, no status
            }
        } catch (Exception e) {
            return "Error";
        }

        if (timeInStr == null || timeInStr.trim().isEmpty() || timeOutStr == null || timeOutStr.trim().isEmpty()) {
            return "Absent";
        }

        try {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a");
            java.time.LocalTime timeIn = java.time.LocalTime.parse(timeInStr, formatter);
            java.time.LocalTime timeOut = java.time.LocalTime.parse(timeOutStr, formatter);
            java.time.LocalTime start = java.time.LocalTime.of(8, 0);
            java.time.LocalTime end = java.time.LocalTime.of(17, 0);

            boolean onTimeIn = timeIn.isBefore(start) || timeIn.equals(start);  // <= 8:00
            boolean onTimeOut = timeOut.isAfter(end) || timeOut.equals(end);   // >= 17:00

            if (onTimeIn && onTimeOut) return "On Time";
            if (!onTimeIn && onTimeOut) return "Late";
            if (onTimeIn && !onTimeOut) return "Undertime";
            return "Late and Undertime";
        } catch (Exception e) {
            return "Error";
        }
    }

    private void applyRowColors(JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
                );

                String status = (String) table.getValueAt(row, 3);  // status column

                // Default background if not selected
                if (!isSelected) {
                    if ("On Time".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(200, 255, 200));  // Green
                    } else if ("Late".equalsIgnoreCase(status) || "Undertime".equalsIgnoreCase(status) || "Late and Undertime".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(200, 220, 255));  // Blue
                    } else if ("Leave".equalsIgnoreCase(status)) {
                        c.setBackground(Color.YELLOW);  // Yellow
                    } else if ("Absent".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(255, 200, 200));  // Red
                    } else {
                        c.setBackground(Color.WHITE);  // Default
                    }
                }

                return c;
            }
        });
    }

    // LOAD TABLE (ONLY LAST 5 DAYS)
    private void loadTable() {
        model.setRowCount(0);

        try (Connection con = DriverManager.getConnection("jdbc:sqlite:employees.db");
             PreparedStatement ps = con.prepareStatement(
                "SELECT date, time_in, time_out, status FROM attendance_records WHERE empNo=? ORDER BY id DESC LIMIT 5"
             )) {
            ps.setString(1, empNo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("date"),
                        rs.getString("time_in"),
                        rs.getString("time_out"),
                        rs.getString("status")
                    });
                }
            }

            // Update statuses for existing records if null, using the same connection
            for (int i = 0; i < model.getRowCount(); i++) {
                String date = (String) model.getValueAt(i, 0);
                String timeIn = (String) model.getValueAt(i, 1);
                String timeOut = (String) model.getValueAt(i, 2);
                String status = (String) model.getValueAt(i, 3);
                if (status == null || status.trim().isEmpty()) {
                    String newStatus = calculateStatus(timeIn, timeOut, date);
                    try (PreparedStatement updatePs = con.prepareStatement(
                            "UPDATE attendance_records SET status = ? WHERE empNo = ? AND date = ?"
                    )) {
                        updatePs.setString(1, newStatus);
                        updatePs.setString(2, empNo);
                        updatePs.setString(3, date);
                        updatePs.executeUpdate();
                    }
                    model.setValueAt(newStatus, i, 3);
                }
            }

            // APPLY ROW COLORS HERE
            applyRowColors(table);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
