package Fproj;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.table.*;

public class adminAttendance extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    public adminAttendance() {
        setLayout(new BorderLayout(20, 20)); // Use BorderLayout for resizing
        setBackground(new Color(240, 240, 240));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ================= TOP PANEL =================
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Attendance Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        topPanel.add(lblTitle, BorderLayout.WEST);

        // --- Action Bar (Search + ACR) ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        // ACR Button (Replaces Refresh)
        JButton btnACR = new JButton("Review Requests (ACR)");
        btnACR.setBackground(new Color(255, 140, 0)); // Orange
        btnACR.setForeground(Color.WHITE);
        btnACR.setFocusPainted(false);
        btnACR.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnACR.addActionListener(e -> showACRDialog());

        // Search Fields
        txtSearch = new JTextField(15);
        JButton btnSearch = new JButton("Search");
        btnSearch.setBackground(new Color(22, 102, 87));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        
        JButton btnReset = new JButton("Reset");
        btnReset.setBackground(Color.GRAY);
        btnReset.setForeground(Color.WHITE);

        actionPanel.add(btnACR);
        actionPanel.add(new JLabel("Emp No:"));
        actionPanel.add(txtSearch);
        actionPanel.add(btnSearch);
        actionPanel.add(btnReset);

        topPanel.add(actionPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // ================= CENTER TABLE =================
        String[] cols = {"Emp No", "Name", "Action"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Only button column is editable
            }
        };

        table = new JTable(model);
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Custom Renderer for View Button
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);

        // ================= LISTENERS =================
        btnSearch.addActionListener(e -> searchSummary());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            loadSummaryTable();
        });

        loadSummaryTable();
    }

    // ---------------- LOAD SUMMARY ----------------
    void loadSummaryTable() {
        model.setRowCount(0);
        String sql = "SELECT DISTINCT e.empNo, e.name FROM employees e LEFT JOIN attendance_records a ON a.empNo = e.empNo ORDER BY e.empNo";

        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("empNo"),
                        rs.getString("name"),
                        "View Details"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- SEARCH ----------------
    private void searchSummary() {
        String search = txtSearch.getText().trim();
        if (search.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Employee Number!");
            return;
        }
        model.setRowCount(0);
        String sql = "SELECT e.empNo, e.name FROM employees e WHERE e.empNo = ?";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, search);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("empNo"),
                        rs.getString("name"),
                        "View Details"
                });
            } else {
                JOptionPane.showMessageDialog(this, "No records found.");
                loadSummaryTable();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    //                        ACR PROCESSING DIALOG
    // =========================================================================
    private void showACRDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Pending Correction Requests", true);
        dialog.setSize(900, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // --- Table for Requests ---
        String[] reqCols = {"Req ID", "Emp No", "Date", "New Time In", "New Time Out", "Status"};
        DefaultTableModel reqModel = new DefaultTableModel(reqCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable reqTable = new JTable(reqModel);
        reqTable.setRowHeight(30);

        // Load Pending Requests
        loadRequests(reqModel);

        dialog.add(new JScrollPane(reqTable), BorderLayout.CENTER);

        // --- Button Panel ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnApprove = new JButton("Approve Selected");
        btnApprove.setBackground(new Color(46, 204, 113));
        btnApprove.setForeground(Color.WHITE);
        
        JButton btnReject = new JButton("Reject Selected");
        btnReject.setBackground(new Color(231, 76, 60));
        btnReject.setForeground(Color.WHITE);

        btnApprove.addActionListener(e -> processRequest(reqTable, reqModel, "Approved"));
        btnReject.addActionListener(e -> processRequest(reqTable, reqModel, "Rejected"));

        btnPanel.add(btnApprove);
        btnPanel.add(btnReject);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void loadRequests(DefaultTableModel model) {
        model.setRowCount(0);
        // Only show Pending requests
        String sql = "SELECT * FROM attendance_requests WHERE status = 'Pending'";
        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("empNo"),
                    rs.getString("request_date"),
                    rs.getString("time_in"),
                    rs.getString("time_out"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void processRequest(JTable table, DefaultTableModel model, String newStatus) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a request.");
            return;
        }

        int reqId = (int) table.getValueAt(selectedRow, 0);
        String empNo = (String) table.getValueAt(selectedRow, 1);
        String date = (String) table.getValueAt(selectedRow, 2);
        String newIn = (String) table.getValueAt(selectedRow, 3);
        String newOut = (String) table.getValueAt(selectedRow, 4);

        try (Connection conn = Database.connect()) {
            conn.setAutoCommit(false); // Start Transaction

            // 1. Update Request Status
            String updateReq = "UPDATE attendance_requests SET status = ? WHERE id = ?";
            try (PreparedStatement pst = conn.prepareStatement(updateReq)) {
                pst.setString(1, newStatus);
                pst.setInt(2, reqId);
                pst.executeUpdate();
            }

            // 2. If Approved, Update Main Attendance Table
            if ("Approved".equals(newStatus)) {
                // Check if record exists for that date
                String checkSql = "SELECT count(*) FROM attendance_records WHERE empNo = ? AND date = ?";
                boolean exists = false;
                try (PreparedStatement pstCheck = conn.prepareStatement(checkSql)) {
                    pstCheck.setString(1, empNo);
                    pstCheck.setString(2, date);
                    ResultSet rs = pstCheck.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) exists = true;
                }

                if (exists) {
                    String updateMain = "UPDATE attendance_records SET time_in = ?, time_out = ?, status = 'Present (Correction)' WHERE empNo = ? AND date = ?";
                    try (PreparedStatement pstUp = conn.prepareStatement(updateMain)) {
                        pstUp.setString(1, newIn);
                        pstUp.setString(2, newOut);
                        pstUp.setString(3, empNo);
                        pstUp.setString(4, date);
                        pstUp.executeUpdate();
                    }
                } else {
                    // Insert if missing (Unlikely but safe to handle)
                    String insertMain = "INSERT INTO attendance_records (empNo, date, time_in, time_out) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement pstIns = conn.prepareStatement(insertMain)) {
                        pstIns.setString(1, empNo);
                        pstIns.setString(2, date);
                        pstIns.setString(3, newIn);
                        pstIns.setString(4, newOut);
                        pstIns.executeUpdate();
                    }
                }
            }

            // 3. Send Notification
            String msg = "Your ACR for " + date + " has been " + newStatus + ".";
            String notifSql = "INSERT INTO notifications (empNo, message, date) VALUES (?, ?, ?)";
            try (PreparedStatement pstNotif = conn.prepareStatement(notifSql)) {
                pstNotif.setString(1, empNo);
                pstNotif.setString(2, msg);
                pstNotif.setString(3, new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
                pstNotif.executeUpdate();
            }

            conn.commit(); // Commit Transaction
            JOptionPane.showMessageDialog(null, "Request " + newStatus + " Successfully!");
            loadRequests(model); // Refresh table

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error processing request: " + e.getMessage());
        }
    }

    // =========================================================================
    //                        VIEW DETAILS LOGIC
    // =========================================================================

    private void viewAttendanceDetails(String empNo) {
        AttendanceDetailsDialog dialog = new AttendanceDetailsDialog(empNo);
        dialog.setVisible(true);
    }

    // ---------------- BUTTON RENDERER & EDITOR ----------------
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
                String empNo = (String) table.getValueAt(table.getSelectedRow(), 0);
                viewAttendanceDetails(empNo);
            });
        }
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            return button;
        }
        public Object getCellEditorValue() { return label; }
    }

    // ---------------- DETAILS DIALOG CLASS ----------------
    private class AttendanceDetailsDialog extends JDialog {
        private JTable detailsTable;
        private DefaultTableModel detailsModel;
        private final String empNo;

        public AttendanceDetailsDialog(String empNo) {
            this.empNo = empNo;
            setTitle("Attendance Records - " + empNo);
            setModal(true);
            setSize(900, 500);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout(10, 10));

            String[] cols = {
                "Date", "Time In", "Time Out", "Second Time Out", "Workday", "Status"
            };

            detailsModel = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };

            detailsTable = new JTable(detailsModel);
            detailsTable.setRowHeight(25);
            add(new JScrollPane(detailsTable), BorderLayout.CENTER);

            JButton btnClose = new JButton("Close");
            btnClose.addActionListener(e -> dispose());
            JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            pnl.add(btnClose);
            add(pnl, BorderLayout.SOUTH);

            loadDetailsTable();
            applyRowColors(detailsTable);
        }

        private void loadDetailsTable() {
            detailsModel.setRowCount(0);
            String sql = "SELECT date, time_in, time_out, second_time_out, workday, status FROM attendance_records WHERE empNo = ? ORDER BY date DESC";
            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, empNo);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    detailsModel.addRow(new Object[]{
                            rs.getString("date"),
                            rs.getString("time_in"),
                            rs.getString("time_out"),
                            rs.getString("second_time_out"),
                            rs.getString("workday"),
                            rs.getString("status")
                    });
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void applyRowColors(JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = "";
                try {
                    Object o = table.getValueAt(row, 5); // Status Column
                    if (o != null) status = o.toString();
                } catch (Exception ignored) {}

                if (!isSelected) {
                    if (status.contains("On Time")) c.setBackground(new Color(200, 255, 200));
                    else if (status.contains("Late") || status.contains("Undertime")) c.setBackground(new Color(255, 230, 180));
                    else if (status.contains("Absent")) c.setBackground(new Color(255, 200, 200));
                    else c.setBackground(Color.WHITE);
                }
                return c;
            }
        });
    }
}