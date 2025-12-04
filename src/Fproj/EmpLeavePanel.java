package Fproj;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EmpLeavePanel extends JPanel {

    private String empNo, empName, empPosition;
    private JTextField txtStartDate, txtEndDate;
    private JTextArea txtReason;
    private JComboBox<String> cbLeaveType;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lblVacation, lblSick, lblEmergency, lblSpecial;

    public EmpLeavePanel(String empNo) {
        this.empNo = empNo;
        // Fetch empName and empPosition from DB (assuming you have a method)
        fetchEmployeeDetails();

        setLayout(null);
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));  // NEW: Set preferred size for scrolling (adjust width/height as needed)

        // Title
        JLabel lblTitle = new JLabel("Leave Request Form");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setBounds(20, 20, 200, 30);
        add(lblTitle);

        // Leave Type
        JLabel lblType = new JLabel("Leave Type:");
        lblType.setBounds(20, 60, 100, 25);
        add(lblType);
        cbLeaveType = new JComboBox<>(new String[]{"Vacation", "Sick", "Emergency", "Special"});
        cbLeaveType.setBounds(120, 60, 150, 25);
        add(cbLeaveType);

        // Employee Details (read-only)
        JLabel lblID = new JLabel("Employee ID: " + empNo);
        lblID.setBounds(20, 100, 200, 25);
        add(lblID);
        JLabel lblName = new JLabel("Name: " + empName);
        lblName.setBounds(20, 130, 200, 25);
        add(lblName);
        JLabel lblPosition = new JLabel("Position: " + empPosition);
        lblPosition.setBounds(20, 160, 200, 25);
        add(lblPosition);

        // Dates
        JLabel lblStart = new JLabel("Start Date (YYYY-MM-DD):");
        lblStart.setBounds(20, 200, 200, 25);
        add(lblStart);
        txtStartDate = new JTextField();
        txtStartDate.setBounds(220, 200, 150, 25);
        add(txtStartDate);

        JLabel lblEnd = new JLabel("End Date (YYYY-MM-DD):");
        lblEnd.setBounds(20, 230, 200, 25);
        add(lblEnd);
        txtEndDate = new JTextField();
        txtEndDate.setBounds(220, 230, 150, 25);
        add(txtEndDate);

        // Reason
        JLabel lblReason = new JLabel("Reason:");
        lblReason.setBounds(20, 260, 100, 25);
        add(lblReason);
        txtReason = new JTextArea();
        txtReason.setBounds(120, 260, 250, 60);
        txtReason.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        add(txtReason);

        // Submit Button
        JButton btnSubmit = new JButton("Submit Request");
        btnSubmit.setBounds(20, 340, 150, 30);
        btnSubmit.setBackground(new Color(34, 139, 34));
        btnSubmit.setForeground(Color.WHITE);
        add(btnSubmit);

        // Leave Balances
        JLabel lblBalances = new JLabel("Leave Balances:");
        lblBalances.setFont(new Font("Arial", Font.BOLD, 14));
        lblBalances.setBounds(400, 60, 150, 25);
        add(lblBalances);
        lblVacation = new JLabel("Vacation: -- days");
        lblVacation.setBounds(400, 90, 150, 25);
        add(lblVacation);
        lblSick = new JLabel("Sick: -- days");
        lblSick.setBounds(400, 110, 150, 25);
        add(lblSick);
        lblEmergency = new JLabel("Emergency: -- days");
        lblEmergency.setBounds(400, 130, 150, 25);
        add(lblEmergency);
        lblSpecial = new JLabel("Special: -- days");
        lblSpecial.setBounds(400, 150, 150, 25);
        add(lblSpecial);

        // UPDATED: Refresh Data Button (renamed and now refreshes both balances and table)
        JButton btnRefreshData = new JButton("Refresh");
        btnRefreshData.setBounds(550, 60, 100, 25);
        btnRefreshData.addActionListener(e -> {
            loadBalances();
            loadLeaveRequests();
        });
        add(btnRefreshData);

        // Status Table with color coding
        model = new DefaultTableModel(new String[]{"ID", "Type", "Start", "End", "Reason", "Status"}, 0);
        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                String status = (String) getValueAt(row, 5); // Status is in column 5
                if (!isRowSelected(row)) {
                    if ("Approved".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(63, 255, 38, 178)); // rgba(63, 255, 38, 0.7)
                        c.setForeground(Color.BLACK);
                    } else if ("Rejected".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(255, 32, 32, 178)); // rgba(255, 32, 32, 0.7)
                        c.setForeground(Color.WHITE);
                    } else if ("Pending".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(255, 247, 5, 178)); // rgba(255, 247, 5, 0.7)
                        c.setForeground(Color.BLACK);
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                }
                return c;
            }
        };
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(20, 390, 750, 200);
        add(scroll);

        // REMOVED: Extra JScrollBar (it was unattached and not functional)

        // Load data
        loadBalances();
        loadLeaveRequests();

        // Submit action
        btnSubmit.addActionListener(e -> submitLeaveRequest());
    }

    private void fetchEmployeeDetails() {
        String sql = "SELECT name, position FROM employees WHERE empNo = ?";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empNo);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                empName = rs.getString("name");
                empPosition = rs.getString("position");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadBalances() {
        int[] balances = Database.getLeaveBalances(empNo);
        lblVacation.setText("Vacation: " + balances[0] + " days");
        lblSick.setText("Sick: " + balances[1] + " days");
        lblEmergency.setText("Emergency: " + balances[2] + " days");
        lblSpecial.setText("Special: " + balances[3] + " days");
    }

    private void loadLeaveRequests() {
        model.setRowCount(0);
        try (ResultSet rs = Database.getLeaveRequests(empNo)) {
            while (rs != null && rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("leave_type"),
                        rs.getString("start_date"),
                        rs.getString("end_date"),
                        rs.getString("reason"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void submitLeaveRequest() {
        String leaveType = (String) cbLeaveType.getSelectedItem();
        String startDate = txtStartDate.getText().trim();
        String endDate = txtEndDate.getText().trim();
        String reason = txtReason.getText().trim();

        if (startDate.isEmpty() || endDate.isEmpty() || reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }

        // Basic validation: End after start
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            if (end.before(start)) {
                JOptionPane.showMessageDialog(this, "End date must be after start date!");
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format! Use YYYY-MM-DD.");
            return;
        }

        // NEW: Check leave balance before submitting
        int[] balances = Database.getLeaveBalances(empNo);
        int balanceIndex = getBalanceIndex(leaveType);
        if (balanceIndex != -1) {
            long days = calculateLeaveDays(startDate, endDate);
            int currentBalance = balances[balanceIndex];
            if (currentBalance < days) {
                JOptionPane.showMessageDialog(this, "Insufficient leave balance! Current: " + currentBalance + " days, Required: " + days + " days.");
                return;  // Don't submit
            }
        }

        Database.insertLeaveRequest(empNo, empName, empPosition, leaveType, startDate, endDate, reason);
        JOptionPane.showMessageDialog(this, "Leave request submitted!");
        loadLeaveRequests();  // Refresh table
        loadBalances();  // Refresh balances (though they don't change on submission)
        // Clear form
        txtStartDate.setText("");
        txtEndDate.setText("");
        txtReason.setText("");
    }

    // NEW: Helper to calculate leave days
    private long calculateLeaveDays(String startDate, String endDate) {
        try {
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
            return java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // NEW: Helper to get balance index
    private int getBalanceIndex(String leaveType) {
        switch (leaveType.toLowerCase()) {
            case "vacation": return 0;
            case "sick": return 1;
            case "emergency": return 2;
            case "special": return 3;
            default: return -1;
        }
    }
}