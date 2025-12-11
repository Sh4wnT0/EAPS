package Fproj;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
        styleMainTable(table); 

        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        btnSearch.addActionListener(e -> searchSummary());
        btnReset.addActionListener(e -> { txtSearch.setText(""); loadSummaryTable(); });

        loadSummaryTable();
    }

    // =========================================================================
    //                        ACR PROCESSING DIALOG
    // =========================================================================
    private void showACRDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Pending Correction Requests", true);
        dialog.setSize(950, 550);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // Updated Columns: Checkbox, Hidden ID, EmpNo, Name, Date, View Button
        String[] reqCols = {"Select", "Req ID", "Emp No", "Name", "Date", "Details"};
        DefaultTableModel reqModel = new DefaultTableModel(reqCols, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if(columnIndex == 0) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }
            @Override
            public boolean isCellEditable(int row, int col) { 
                return col == 0 || col == 5; 
            }
        };

        JTable reqTable = new JTable(reqModel);
        styleMainTable(reqTable);
        
        reqTable.getColumnModel().getColumn(1).setMinWidth(0);
        reqTable.getColumnModel().getColumn(1).setMaxWidth(0);
        reqTable.getColumnModel().getColumn(1).setWidth(0);
        reqTable.getColumnModel().getColumn(0).setMaxWidth(60);

        reqTable.getColumn("Details").setCellRenderer(new ViewRequestRenderer());
        reqTable.getColumn("Details").setCellEditor(new ViewRequestEditor(new JCheckBox(), reqTable, reqModel));

        loadRequests(reqModel);
        dialog.add(new JScrollPane(reqTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnApprove = new JButton("Approve Selected");
        styleButton(btnApprove, new Color(46, 204, 113));
        
        JButton btnReject = new JButton("Reject Selected");
        styleButton(btnReject, new Color(231, 76, 60));

        btnApprove.addActionListener(e -> bulkProcessACR(reqTable, reqModel, "Approved"));
        btnReject.addActionListener(e -> bulkProcessACR(reqTable, reqModel, "Rejected"));

        btnPanel.add(btnApprove);
        btnPanel.add(btnReject);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void loadRequests(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT r.id, r.empNo, e.name, r.request_date " +
                     "FROM attendance_requests r " +
                     "JOIN employees e ON r.empNo = e.empNo " +
                     "WHERE r.status = 'Pending'";
                     
        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    false, 
                    rs.getInt("id"),
                    rs.getString("empNo"),
                    rs.getString("name"),
                    rs.getString("request_date"),
                    "View" 
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void bulkProcessACR(JTable table, DefaultTableModel model, String status) {
        List<Integer> rowsToProcess = new ArrayList<>();
        for(int i=0; i<table.getRowCount(); i++) {
            Boolean checked = (Boolean) table.getValueAt(i, 0);
            if(checked != null && checked) rowsToProcess.add(i);
        }

        if(rowsToProcess.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No requests selected.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(null, "Process " + rowsToProcess.size() + " requests as " + status + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if(confirm != JOptionPane.YES_OPTION) return;

        int successCount = 0;
        for(int i = rowsToProcess.size()-1; i >= 0; i--) {
            int row = rowsToProcess.get(i);
            int reqId = (int) table.getValueAt(row, 1);
            String empNo = (String) table.getValueAt(row, 2);
            String date = (String) table.getValueAt(row, 4);
            
            String[] times = getRequestTimes(reqId); 
            if(times != null) {
                processSingleRequest(reqId, empNo, date, times[0], times[1], status, true);
                model.removeRow(row);
                successCount++;
            }
        }
        
        if(successCount > 0) {
            JOptionPane.showMessageDialog(null, "Processed " + successCount + " requests.");
            loadSummaryTable(); 
        }
    }

    private String[] getRequestTimes(int reqId) {
        try (Connection conn = Database.connect(); PreparedStatement pst = conn.prepareStatement("SELECT time_in, time_out FROM attendance_requests WHERE id=?")) {
            pst.setInt(1, reqId);
            ResultSet rs = pst.executeQuery();
            if(rs.next()) return new String[]{rs.getString(1), rs.getString(2)};
        } catch(Exception e) {}
        return null;
    }

    // =========================================================================
    //                        VIEW DETAILS DIALOG
    // =========================================================================
    private void showRequestDetails(int reqId, String empNo, String name, String date) {
        JDialog d = new JDialog();
        d.setTitle("Request Details");
        d.setModal(true);
        d.setSize(600, 420);
        d.setLocationRelativeTo(null);
        d.setLayout(new BorderLayout());
        d.getContentPane().setBackground(Color.WHITE);

        String[] newTimes = getRequestTimes(reqId);
        String[] currentTimes = getCurrentRecord(empNo, date);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); 
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3; gbc.anchor = GridBagConstraints.CENTER;
        JLabel lblHeader = new JLabel("Correction Request: " + name);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeader.setForeground(PRIMARY_COLOR);
        lblHeader.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lblHeader, gbc);

        gbc.gridy = 1;
        JLabel lblDate = new JLabel("Attendance Record Date: " + date);
        lblDate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDate.setForeground(Color.GRAY);
        lblDate.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lblDate, gbc);

        gbc.gridy = 2; gbc.gridwidth = 1;
        gbc.gridx = 0; p.add(new JLabel(""), gbc);

        gbc.gridx = 1;
        JLabel lblCur = new JLabel("Initial Record");
        lblCur.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCur.setForeground(new Color(220, 53, 69));
        lblCur.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lblCur, gbc);

        gbc.gridx = 2;
        JLabel lblNew = new JLabel("Requested Change");
        lblNew.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNew.setForeground(new Color(40, 167, 69));
        lblNew.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lblNew, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        JLabel lblIn = new JLabel("Time IN:");
        lblIn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        p.add(lblIn, gbc);

        gbc.gridx = 1;
        JTextField txtCurIn = createReadOnlyField(currentTimes[0] == null ? "-- : --" : currentTimes[0]);
        p.add(txtCurIn, gbc);

        gbc.gridx = 2;
        JTextField txtNewIn = createReadOnlyField(newTimes[0]);
        txtNewIn.setBorder(BorderFactory.createLineBorder(new Color(40, 167, 69), 1));
        p.add(txtNewIn, gbc);

        gbc.gridy = 4; gbc.gridx = 0;
        JLabel lblOut = new JLabel("Time OUT:");
        lblOut.setFont(new Font("Segoe UI", Font.BOLD, 14));
        p.add(lblOut, gbc);

        gbc.gridx = 1;
        JTextField txtCurOut = createReadOnlyField(currentTimes[1] == null ? "-- : --" : currentTimes[1]);
        p.add(txtCurOut, gbc);

        gbc.gridx = 2;
        JTextField txtNewOut = createReadOnlyField(newTimes[1]);
        txtNewOut.setBorder(BorderFactory.createLineBorder(new Color(40, 167, 69), 1));
        p.add(txtNewOut, gbc);

        d.add(p, BorderLayout.CENTER);

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        btnP.setBackground(Color.WHITE);
        
        JButton btnApprove = new JButton("Approve");
        styleButton(btnApprove, new Color(40, 167, 69));
        btnApprove.setPreferredSize(new Dimension(110, 35));
        btnApprove.addActionListener(e -> {
            processSingleRequest(reqId, empNo, date, newTimes[0], newTimes[1], "Approved", false);
            d.dispose();
        });

        JButton btnReject = new JButton("Reject");
        styleButton(btnReject, new Color(220, 53, 69));
        btnReject.setPreferredSize(new Dimension(110, 35));
        btnReject.addActionListener(e -> {
            processSingleRequest(reqId, empNo, date, newTimes[0], newTimes[1], "Rejected", false);
        });

        btnP.add(btnApprove);
        btnP.add(btnReject);
        d.add(btnP, BorderLayout.SOUTH);

        d.setVisible(true);
    }

    private JTextField createReadOnlyField(String text) {
        JTextField tf = new JTextField(text);
        tf.setEditable(false);
        tf.setBackground(new Color(250, 250, 250));
        tf.setHorizontalAlignment(SwingConstants.CENTER);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setPreferredSize(new Dimension(140, 35));
        tf.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        return tf;
    }

    private String[] getCurrentRecord(String empNo, String date) {
        try (Connection conn = Database.connect(); 
             PreparedStatement pst = conn.prepareStatement("SELECT time_in, time_out FROM attendance_records WHERE empNo=? AND date=?")) {
            pst.setString(1, empNo); pst.setString(2, date);
            ResultSet rs = pst.executeQuery();
            if(rs.next()) return new String[]{rs.getString(1), rs.getString(2)};
        } catch(Exception e) {}
        return new String[]{null, null};
    }

    // =========================================================================
    //                        SHARED LOGIC (DB UPDATES)
    // =========================================================================
    
    private void processSingleRequest(int reqId, String empNo, String date, String newIn, String newOut, String newStatus, boolean silent) {
        try (Connection conn = Database.connect()) {
            conn.setAutoCommit(false); 

            try (PreparedStatement pst = conn.prepareStatement("UPDATE attendance_requests SET status = ? WHERE id = ?")) {
                pst.setString(1, newStatus); pst.setInt(2, reqId); pst.executeUpdate();
            }

            if ("Approved".equals(newStatus)) {
                String checkSql = "SELECT workday, second_time_out FROM attendance_records WHERE empNo = ? AND date = ?";
                boolean recordExists = false;
                String currentWorkday = "Regular"; 
                String secondTimeOut = null;

                try (PreparedStatement pstCheck = conn.prepareStatement(checkSql)) {
                    pstCheck.setString(1, empNo); pstCheck.setString(2, date);
                    ResultSet rs = pstCheck.executeQuery();
                    if (rs.next()) {
                        recordExists = true;
                        currentWorkday = rs.getString("workday");
                        secondTimeOut = rs.getString("second_time_out");
                    }
                }

                String calculatedStatus = calculateStatusLogic(newIn, newOut, secondTimeOut, currentWorkday);

                if (recordExists) {
                    String updateMain = "UPDATE attendance_records SET time_in = ?, time_out = ?, status = ? WHERE empNo = ? AND date = ?";
                    try (PreparedStatement pstUp = conn.prepareStatement(updateMain)) {
                        pstUp.setString(1, newIn); pstUp.setString(2, newOut); pstUp.setString(3, calculatedStatus);
                        pstUp.setString(4, empNo); pstUp.setString(5, date);
                        pstUp.executeUpdate();
                    }
                } else {
                    String insertMain = "INSERT INTO attendance_records (empNo, date, time_in, time_out, status, workday) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement pstIns = conn.prepareStatement(insertMain)) {
                        pstIns.setString(1, empNo); pstIns.setString(2, date); pstIns.setString(3, newIn);
                        pstIns.setString(4, newOut); pstIns.setString(5, calculatedStatus); pstIns.setString(6, currentWorkday);
                        pstIns.executeUpdate();
                    }
                }
            }

            try (PreparedStatement pstNotif = conn.prepareStatement("INSERT INTO notifications (empNo, message, date) VALUES (?, ?, CURRENT_DATE)")) {
                String msg = "Your ACR for " + date + " has been " + newStatus + ".";
                pstNotif.setString(1, empNo); pstNotif.setString(2, msg); pstNotif.executeUpdate();
            }

            conn.commit();
            if(!silent) JOptionPane.showMessageDialog(null, "Request " + newStatus + " Successfully!");

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private String calculateStatusLogic(String timeIn, String timeOut, String secondTimeOut, String workday) {
        if (timeIn == null || timeOut == null || "00:00:00 AM".equals(timeIn)) return "Absent";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
        try {
            LocalTime in = LocalTime.parse(timeIn, formatter);
            LocalTime out = LocalTime.parse(timeOut, formatter);
            LocalTime start = LocalTime.of(8, 0);
            LocalTime end = LocalTime.of(17, 0); 

            if ("OT".equals(workday) && secondTimeOut != null) {
                LocalTime expectedEnd = LocalTime.parse(secondTimeOut, formatter);
                boolean onTimeIn = in.isBefore(start) || in.equals(start);
                boolean onTimeOut = out.isAfter(expectedEnd) || out.equals(expectedEnd);
                if (onTimeIn && onTimeOut) return "On Time";
                return "Late/Undertime";
            } 
            
            boolean onTimeIn = in.isBefore(start) || in.equals(start);
            boolean onTimeOut = out.isAfter(end) || out.equals(end);

            if (onTimeIn && onTimeOut) return "On Time";
            if (!onTimeIn && onTimeOut) return "Late";
            if (onTimeIn && !onTimeOut) return "Undertime";
            return "Late and Undertime";

        } catch (Exception e) { return "Error"; }
    }

    // ---------------- HELPER METHODS ----------------
    private void styleMainTable(JTable t) {
        t.setRowHeight(35);
        t.setFont(CELL_FONT);
        t.setShowGrid(true); 
        t.setGridColor(Color.LIGHT_GRAY);
        t.setSelectionBackground(new Color(230, 240, 255));
        t.setSelectionForeground(Color.BLACK);
        t.getTableHeader().setFont(HEADER_FONT);
        t.getTableHeader().setBackground(PRIMARY_COLOR);
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setPreferredSize(new Dimension(0, 40));
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
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

    // --- RENDERERS & EDITORS ---
    class ViewRequestRenderer extends JButton implements TableCellRenderer {
        public ViewRequestRenderer() { setOpaque(true); setBackground(new Color(230, 230, 230)); setFont(new Font("Segoe UI", Font.PLAIN, 11)); }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) { setText("View"); return this; }
    }

    class ViewRequestEditor extends DefaultCellEditor {
        private JButton b; private boolean clicked; private JTable table;
        public ViewRequestEditor(JCheckBox c, JTable t, DefaultTableModel m) { 
            super(c); table=t; b=new JButton(); b.setOpaque(true); 
            b.addActionListener(e -> { 
                if(clicked) {
                    int r = table.getSelectedRow();
                    int reqId = (int) table.getValueAt(r, 1);
                    String emp = (String) table.getValueAt(r, 2);
                    String name = (String) table.getValueAt(r, 3);
                    String date = (String) table.getValueAt(r, 4);
                    fireEditingStopped(); 
                    showRequestDetails(reqId, emp, name, date);
                    SwingUtilities.getWindowAncestor(table).dispose(); 
                }
                clicked=false;
            });
        }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { clicked=true; b.setText("View"); return b; }
        public Object getCellEditorValue() { return "View"; }
    }
    
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); setFont(new Font("Segoe UI", Font.PLAIN, 11)); setBackground(new Color(230, 230, 230)); }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) { setText((v==null)?"":v.toString()); return this; }
    }
    class ButtonEditor extends DefaultCellEditor {
        JButton b; String l;
        public ButtonEditor(JCheckBox c) { super(c); b=new JButton(); b.setOpaque(true); b.addActionListener(e -> { fireEditingStopped(); String emp=(String)table.getValueAt(table.getSelectedRow(),0); viewAttendanceDetails(emp); }); }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { l=(v==null)?"":v.toString(); b.setText(l); return b; }
        public Object getCellEditorValue() { return l; }
    }
    
    // --- MAIN TABLE METHODS ---
    private void viewAttendanceDetails(String empNo) {
        new AttendanceDetailsDialog(empNo).setVisible(true);
    }
    
    void loadSummaryTable() {
        model.setRowCount(0);
        String sql = "SELECT empNo, name FROM employees ORDER BY empNo";
        try (Connection conn = Database.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{ rs.getString("empNo"), rs.getString("name"), "View" });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    private void searchSummary() {
        String search = txtSearch.getText().trim();
        if (search.isEmpty()) { loadSummaryTable(); return; }
        model.setRowCount(0);
        String sql = "SELECT empNo, name FROM employees WHERE empNo LIKE ? OR name LIKE ?";
        try (Connection conn = Database.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + search + "%"); pstmt.setString(2, "%" + search + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{ rs.getString("empNo"), rs.getString("name"), "View" });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- DETAILS DIALOG CLASS ---
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
            detailsModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
            detailsTable = new JTable(detailsModel);
            
            detailsTable.setRowHeight(30); detailsTable.setShowGrid(true); detailsTable.setGridColor(Color.LIGHT_GRAY);
            detailsTable.getTableHeader().setFont(HEADER_FONT); detailsTable.getTableHeader().setBackground(PRIMARY_COLOR); detailsTable.getTableHeader().setForeground(Color.WHITE);
            
            add(new JScrollPane(detailsTable), BorderLayout.CENTER);

            JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnPrint = new JButton("Print Records"); styleButton(btnPrint, PRIMARY_COLOR); btnPrint.addActionListener(e -> printRecords());
            JButton btnClose = new JButton("Close"); styleButton(btnClose, Color.GRAY); btnClose.addActionListener(e -> dispose());
            pnlBottom.add(btnPrint); pnlBottom.add(btnClose);
            add(pnlBottom, BorderLayout.SOUTH);

            loadDetailsTable();
            applyRowColors(detailsTable);
        }

        private void printRecords() {
            try {
                MessageFormat header = new MessageFormat("Attendance Record: " + empNo);
                MessageFormat footer = new MessageFormat("Page {0,number,integer}");
                boolean complete = detailsTable.print(JTable.PrintMode.FIT_WIDTH, header, footer);
                if (complete) JOptionPane.showMessageDialog(this, "Printing Complete", "Result", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException pe) { JOptionPane.showMessageDialog(this, "Printing Failed: " + pe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        }

        private void loadDetailsTable() {
            detailsModel.setRowCount(0);
            String sql = "SELECT date, time_in, time_out, second_time_out, workday, status FROM attendance_records WHERE empNo = ? ORDER BY date DESC";
            try (Connection conn = Database.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, empNo);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    detailsModel.addRow(new Object[]{ rs.getString("date"), rs.getString("time_in"), rs.getString("time_out"), rs.getString("second_time_out"), rs.getString("workday"), rs.getString("status") });
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