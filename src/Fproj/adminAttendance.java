package Fproj;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException; // Import for printing
import java.sql.*;
import java.text.MessageFormat;         // Import for print headers
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class adminAttendance extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    // Theme Colors
    private final Color PRIMARY_COLOR = new Color(22, 102, 87);
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private final Font CELL_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    public adminAttendance() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // ================= TOP PANEL =================
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Attendance Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(50, 50, 50));
        topPanel.add(lblTitle, BorderLayout.WEST);

        // --- Action Bar ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton btnACR = new JButton("Review Requests (ACR)");
        styleButton(btnACR, new Color(70, 130, 180)); 
        btnACR.addActionListener(e -> showACRDialog());

        txtSearch = new JTextField(15);
        putPlaceholder(txtSearch, "Search Emp No..."); 
        txtSearch.setPreferredSize(new Dimension(150, 35));

        JButton btnSearch = new JButton("Search");
        styleButton(btnSearch, PRIMARY_COLOR);
        
        JButton btnReset = new JButton("Reset");
        styleButton(btnReset, Color.GRAY);

        actionPanel.add(btnACR);
        actionPanel.add(Box.createHorizontalStrut(15));
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
                return column == 2; 
            }
        };

        table = new JTable(model);
        
        table.setRowHeight(45);
        table.setFont(CELL_FONT);
        table.setShowGrid(true); 
        table.setGridColor(Color.LIGHT_GRAY);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(230, 240, 255));
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));

        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(120);
        cm.getColumn(0).setMaxWidth(150);
        cm.getColumn(1).setPreferredWidth(400);
        cm.getColumn(2).setPreferredWidth(90);
        cm.getColumn(2).setMaxWidth(90);
        cm.getColumn(2).setMinWidth(90);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setBorder(new EmptyBorder(0, 10, 0, 0));
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        btnSearch.addActionListener(e -> searchSummary());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            loadSummaryTable();
        });

        loadSummaryTable();
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void putPlaceholder(JTextField field, String text) {
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

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
                        "View"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
                        "View"
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
        // ... (ACR Dialog logic remains unchanged)
        // Kept brief to focus on the requested change below
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Pending Correction Requests", true);
        dialog.setSize(900, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        String[] reqCols = {"Req ID", "Emp No", "Date", "New Time In", "New Time Out", "Status"};
        DefaultTableModel reqModel = new DefaultTableModel(reqCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable reqTable = new JTable(reqModel);
        reqTable.setRowHeight(30);
        reqTable.setShowGrid(true);
        reqTable.setGridColor(Color.LIGHT_GRAY);
        reqTable.getTableHeader().setFont(HEADER_FONT);
        reqTable.getTableHeader().setBackground(PRIMARY_COLOR);
        reqTable.getTableHeader().setForeground(Color.WHITE);

        loadRequests(reqModel);
        dialog.add(new JScrollPane(reqTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnApprove = new JButton("Approve Selected");
        styleButton(btnApprove, new Color(46, 204, 113));
        JButton btnReject = new JButton("Reject Selected");
        styleButton(btnReject, new Color(231, 76, 60));

        btnApprove.addActionListener(e -> processRequest(reqTable, reqModel, "Approved"));
        btnReject.addActionListener(e -> processRequest(reqTable, reqModel, "Rejected"));

        btnPanel.add(btnApprove);
        btnPanel.add(btnReject);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void loadRequests(DefaultTableModel model) {
        // ... (Existing load logic)
        model.setRowCount(0);
        String sql = "SELECT * FROM attendance_requests WHERE status = 'Pending'";
        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("empNo"), rs.getString("request_date"),
                    rs.getString("time_in"), rs.getString("time_out"), rs.getString("status")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void processRequest(JTable table, DefaultTableModel model, String newStatus) {
        // ... (Existing process logic)
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(null, "Please select a request."); return; }
        int reqId = (int) table.getValueAt(selectedRow, 0);
        try (Connection conn = Database.connect()) {
            String updateReq = "UPDATE attendance_requests SET status = ? WHERE id = ?";
            try (PreparedStatement pst = conn.prepareStatement(updateReq)) {
                pst.setString(1, newStatus); pst.setInt(2, reqId); pst.executeUpdate();
            }
            JOptionPane.showMessageDialog(null, "Request " + newStatus + " Successfully!");
            loadRequests(model);
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    private String calculateStatusLogic(String timeIn, String timeOut, String secondTimeOut, String workday) {
        // ... (Existing calculation logic)
        return "Present"; // Simplified for brevity in this answer
    }

    private void viewAttendanceDetails(String empNo) {
        AttendanceDetailsDialog dialog = new AttendanceDetailsDialog(empNo);
        dialog.setVisible(true);
    }

    // ---------------- TABLE RENDERERS ----------------
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { 
            setOpaque(true); setFont(new Font("Segoe UI", Font.PLAIN, 11));
            setBackground(new Color(230, 230, 230)); border(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }
        private void border(javax.swing.border.Border border) { setBorder(border); }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean h, int r, int c) { setText((v == null) ? "" : v.toString()); return this; }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button; private String label;
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox); button = new JButton(); button.setOpaque(true); button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            button.addActionListener(e -> { fireEditingStopped(); String empNo = (String) table.getValueAt(table.getSelectedRow(), 0); viewAttendanceDetails(empNo); });
        }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { label = (v == null) ? "" : v.toString(); button.setText(label); return button; }
        public Object getCellEditorValue() { return label; }
    }

    // =========================================================================
    //               DETAILS DIALOG WITH PRINT FUNCTION
    // =========================================================================
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

            String[] cols = {"Date", "Time In", "Time Out", "Second Time Out", "Workday", "Status"};
            detailsModel = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };

            detailsTable = new JTable(detailsModel);
            
            // Details Table Formatting
            detailsTable.setRowHeight(30);
            detailsTable.setShowGrid(true);
            detailsTable.setGridColor(Color.LIGHT_GRAY);
            detailsTable.getTableHeader().setFont(HEADER_FONT);
            detailsTable.getTableHeader().setBackground(PRIMARY_COLOR);
            detailsTable.getTableHeader().setForeground(Color.WHITE);
            
            add(new JScrollPane(detailsTable), BorderLayout.CENTER);

            // --- BOTTOM PANEL WITH PRINT AND CLOSE ---
            JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            
            // Print Button
            JButton btnPrint = new JButton("Print Records");
            styleButton(btnPrint, PRIMARY_COLOR);
            btnPrint.addActionListener(e -> printRecords());

            // Close Button
    
            pnlBottom.add(btnPrint);
            add(pnlBottom, BorderLayout.SOUTH);

            loadDetailsTable();
            applyRowColors(detailsTable);
        }

        // --- PRINTING LOGIC ---
        private void printRecords() {
            try {
                // Header shows Employee ID
                MessageFormat header = new MessageFormat("Attendance Record: " + empNo);
                // Footer shows Page Number
                MessageFormat footer = new MessageFormat("Page {0,number,integer}");
                
                // JTable's built-in print method
                boolean complete = detailsTable.print(JTable.PrintMode.FIT_WIDTH, header, footer);
                
                if (complete) {
                    JOptionPane.showMessageDialog(this, "Printing Complete", "Result", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (PrinterException pe) {
                JOptionPane.showMessageDialog(this, "Printing Failed: " + pe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
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
                            rs.getString("date"), rs.getString("time_in"), rs.getString("time_out"),
                            rs.getString("second_time_out"), rs.getString("workday"), rs.getString("status")
                    });
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // --- COLOR SCHEME ---
    private void applyRowColors(JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = "";
                try { Object o = table.getValueAt(row, 5); if (o != null) status = o.toString(); } catch (Exception ignored) {}

                if (!isSelected) {
                    if ("On Time".equalsIgnoreCase(status)) c.setBackground(new Color(200, 255, 200)); 
                    else if (status.contains("Late") || status.contains("Undertime")) c.setBackground(new Color(200, 220, 255)); 
                    else if ("Leave".equalsIgnoreCase(status)) c.setBackground(Color.YELLOW); 
                    else if ("Absent".equalsIgnoreCase(status)) c.setBackground(new Color(255, 200, 200)); 
                    else c.setBackground(Color.WHITE);
                }
                return c;
            }
        });
    }
}