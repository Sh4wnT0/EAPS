package Fproj;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

public class adminPayroll extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    
    // UI Constants
    private final Color BRAND_COLOR = new Color(22, 102, 87);
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private final Font CELL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    public adminPayroll() {
        Database.createPayslipsTable(); 
        
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Top Panel ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Payroll Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(50, 50, 50));
        topPanel.add(lblTitle, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        
        JButton btnRequests = new JButton("View Pending Requests");
        styleButton(btnRequests, new Color(70, 130, 180)); 
        btnRequests.addActionListener(e -> showPayrollRequestsDialog());

        txtSearch = new JTextField(15);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        JButton btnSearch = new JButton("Search");
        styleButton(btnSearch, BRAND_COLOR);
        btnSearch.addActionListener(e -> loadEmployees());

        actionPanel.add(btnRequests);
        actionPanel.add(new JLabel("Search:"));
        actionPanel.add(txtSearch);
        actionPanel.add(btnSearch);
        
        topPanel.add(actionPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- Main Table ---
        String[] columns = {"Select", "Emp No", "Name", "Status", "Base Daily Pay", "Action"};

        model = new DefaultTableModel(columns, 0) {
            // 1. Force Column 0 to be Boolean so JTable uses a Checkbox
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class; 
                return super.getColumnClass(columnIndex);
            }

            // 2. Allow editing only for Checkbox (0) and Button (5)
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 5; 
            }
        };

        table = new JTable(model);
        styleTable(table); // Applies styling but skips the checkbox column
        
        // Adjust column widths
        table.getColumn("Select").setMaxWidth(50);
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
        table.getColumn("Action").setMaxWidth(100);
        table.getColumn("Action").setMinWidth(100);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(scroll, BorderLayout.CENTER);

        // --- Bottom Panel (Bulk Send) ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        
        JButton btnBulkSend = new JButton("Generate & Send Selected Payslips");
        styleButton(btnBulkSend, new Color(40, 167, 69)); // Green
        btnBulkSend.setPreferredSize(new Dimension(250, 40));
        btnBulkSend.addActionListener(e -> processMainTableBulkSend());
        
        bottomPanel.add(btnBulkSend);
        add(bottomPanel, BorderLayout.SOUTH);

        loadEmployees();
    }

    // ==========================================
    //      TABLE STYLING (FIXED)
    // ==========================================
    private void styleTable(JTable t) {
        t.setRowHeight(40);
        t.setFont(CELL_FONT);
        t.setShowGrid(true);
        t.setGridColor(new Color(230, 230, 230));
        t.setSelectionBackground(new Color(230, 240, 255));
        t.setSelectionForeground(Color.BLACK);
        
        JTableHeader header = t.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(BRAND_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        // FIX: Loop through columns but SKIP column 0 (Checkbox)
        // If we apply text renderer to Col 0, it destroys the checkbox icon.
        for(int i = 0; i < t.getColumnCount(); i++) {
            if(i != 0 && i != 5) { // Skip Checkbox(0) and Button(5)
                t.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    // ==========================================
    //      MAIN TABLE BULK SEND LOGIC
    // ==========================================
    private void processMainTableBulkSend() {
        boolean hasSelection = false;
        for (int i = 0; i < table.getRowCount(); i++) {
            Boolean checked = (Boolean) table.getValueAt(i, 0);
            if (checked != null && checked) {
                hasSelection = true;
                break;
            }
        }

        if (!hasSelection) {
            JOptionPane.showMessageDialog(this, "Please select employees first.");
            return;
        }

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        String[] years = {"2024", "2025", "2026"};
        JComboBox<String> cbMonth = new JComboBox<>(months);
        JComboBox<String> cbYear = new JComboBox<>(years);
        
        Calendar cal = Calendar.getInstance();
        cbMonth.setSelectedIndex(cal.get(Calendar.MONTH));
        cbYear.setSelectedItem(String.valueOf(cal.get(Calendar.YEAR)));

        panel.add(new JLabel("Select Month:")); panel.add(cbMonth);
        panel.add(new JLabel("Select Year:")); panel.add(cbYear);

        int result = JOptionPane.showConfirmDialog(this, panel, "Batch Processing Settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String selectedMonth = (String) cbMonth.getSelectedItem();
        String selectedYear = (String) cbYear.getSelectedItem();

        int successCount = 0;
        int failCount = 0;

        for (int i = 0; i < table.getRowCount(); i++) {
            Boolean checked = (Boolean) table.getValueAt(i, 0);
            if (checked != null && checked) {
                String empNo = (String) table.getValueAt(i, 1);
                String name = (String) table.getValueAt(i, 2);
                
                if (generateAndSavePayslip(empNo, name, selectedMonth, selectedYear)) {
                    successCount++;
                    table.setValueAt(false, i, 0);
                } else {
                    failCount++;
                }
            }
        }

        String msg = "Processing Complete.\nSent: " + successCount + "\nFailed/Already Exists: " + failCount;
        JOptionPane.showMessageDialog(this, msg);
    }

    // --- SHARED GENERATION LOGIC ---
    private boolean generateAndSavePayslip(String empNo, String name, String month, String year) {
        double dailyPay = getDailyPay(empNo);
        String status = getEmpStatus(empNo);
        PayrollDetailsDialog calculator = new PayrollDetailsDialog(empNo, name, status, dailyPay, false);
        calculator.overrideDateContext(month, year);
        calculator.calculateAndLoadPayroll(); 

        boolean saved = Database.savePayslip(empNo, month, year, calculator.countPresent, calculator.countAbsent, calculator.countLate, calculator.countUndertime, calculator.valGross, calculator.valDed, calculator.valNet);
        if (saved) {
            Database.sendNotification(empNo, "Your payslip for " + month + " " + year + " has been generated.");
            return true;
        }
        return false;
    }

    // ==========================================
    //      PAYROLL REQUESTS DIALOG
    // ==========================================
    private void showPayrollRequestsDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Pending Payslip Requests", true);
        dialog.setSize(850, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        String[] reqCols = {"Select", "Req ID", "Emp No", "Name", "Date Requested", "Action"};
        DefaultTableModel reqModel = new DefaultTableModel(reqCols, 0) {
            @Override public Class<?> getColumnClass(int c) { return (c==0) ? Boolean.class : super.getColumnClass(c); }
            @Override public boolean isCellEditable(int r, int c) { return c==0 || c==5; }
        };
        
        JTable reqTable = new JTable(reqModel);
        styleTable(reqTable);
        reqTable.getColumn("Action").setCellRenderer(new RequestButtonRenderer());
        reqTable.getColumn("Action").setCellEditor(new RequestButtonEditor(new JCheckBox(), reqTable, reqModel));
        reqTable.getColumn("Select").setMaxWidth(60);

        dialog.add(new JScrollPane(reqTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);
        JButton btnSendSelected = new JButton("Process Selected Requests");
        styleButton(btnSendSelected, new Color(40, 167, 69));
        btnSendSelected.addActionListener(e -> processBulkRequests(reqTable, reqModel));
        bottomPanel.add(btnSendSelected);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        loadPayrollRequests(reqModel);
        dialog.setVisible(true);
    }

    private void loadPayrollRequests(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT id, empNo, name, submitted_date FROM requests WHERE request_type = 'Payslip' AND status = 'Pending'";
        try (Connection conn = Database.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) model.addRow(new Object[]{false, rs.getInt("id"), rs.getString("empNo"), rs.getString("name"), rs.getString("submitted_date"), "Send Slip"});
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void processBulkRequests(JTable table, DefaultTableModel model) {
        Calendar cal = Calendar.getInstance();
        String curMonth = new java.text.DateFormatSymbols().getMonths()[cal.get(Calendar.MONTH)];
        String curYear = String.valueOf(cal.get(Calendar.YEAR));

        List<Integer> rowsToRemove = new ArrayList<>();
        int successCount = 0;

        for (int i = 0; i < table.getRowCount(); i++) {
            Boolean isChecked = (Boolean) table.getValueAt(i, 0);
            if (isChecked != null && isChecked) {
                int reqId = (int) table.getValueAt(i, 1);
                String empNo = (String) table.getValueAt(i, 2);
                String name = (String) table.getValueAt(i, 3);

                if (generateAndSavePayslip(empNo, name, curMonth, curYear)) {
                    Database.updateRequestStatus(String.valueOf(reqId), "Completed");
                    successCount++;
                    rowsToRemove.add(i);
                }
            }
        }
        for (int i = rowsToRemove.size() - 1; i >= 0; i--) model.removeRow(rowsToRemove.get(i));
        JOptionPane.showMessageDialog(this, "Processed " + successCount + " requests.");
    }

    // --- Helpers ---
    private double getDailyPay(String empNo) {
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement("SELECT dailyPay FROM employees WHERE empNo=?")) {
            p.setString(1, empNo); ResultSet rs = p.executeQuery(); if(rs.next()) return rs.getDouble(1);
        } catch(Exception e) {} return 0;
    }
    
    private String getEmpStatus(String empNo) {
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement("SELECT employmentStatus FROM employees WHERE empNo=?")) {
            p.setString(1, empNo); ResultSet rs = p.executeQuery(); if(rs.next()) return rs.getString(1);
        } catch(Exception e) {} return "Regular";
    }

    private void loadEmployees() {
        model.setRowCount(0);
        String search = txtSearch.getText().trim();
        String sql = "SELECT empNo, name, employmentStatus, dailyPay FROM employees WHERE empNo LIKE ? OR name LIKE ?";
        try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + search + "%"); ps.setString(2, "%" + search + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    false, // Unchecked by default
                    rs.getString("empNo"),
                    rs.getString("name"),
                    rs.getString("employmentStatus"),
                    df.format(rs.getDouble("dailyPay")),
                    "View Payroll"
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg); btn.setForeground(Color.WHITE); btn.setFocusPainted(false); btn.setFont(new Font("Segoe UI", Font.BOLD, 12)); btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    class RequestButtonRenderer extends JButton implements TableCellRenderer {
        public RequestButtonRenderer() { setOpaque(true); setBackground(new Color(40, 167, 69)); setForeground(Color.WHITE); setFont(new Font("Segoe UI", Font.BOLD, 11)); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean h, int r, int c) { setText("Send Slip"); return this; }
    }
    class RequestButtonEditor extends DefaultCellEditor {
        private JButton button = new JButton(); private boolean clicked; private JTable table; private DefaultTableModel model;
        public RequestButtonEditor(JCheckBox c, JTable t, DefaultTableModel m) {
            super(c); this.table = t; this.model = m; button.setOpaque(true); button.setBackground(new Color(40, 167, 69)); button.setForeground(Color.WHITE);
            button.addActionListener(e -> {
                if (clicked) {
                    int row = table.getSelectedRow();
                    int reqId = (int) table.getValueAt(row, 1);
                    String empNo = (String) table.getValueAt(row, 2);
                    String name = (String) table.getValueAt(row, 3);
                    Calendar cal = Calendar.getInstance(); 
                    String curMonth = new java.text.DateFormatSymbols().getMonths()[cal.get(Calendar.MONTH)];
                    String curYear = String.valueOf(cal.get(Calendar.YEAR));
                    
                    if(generateAndSavePayslip(empNo, name, curMonth, curYear)) {
                        Database.updateRequestStatus(String.valueOf(reqId), "Completed");
                        model.removeRow(row);
                        JOptionPane.showMessageDialog(null, "Payslip Sent!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed or Duplicate Payslip.");
                    }
                }
                clicked = false; fireEditingStopped();
            });
        }
        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSel, int row, int column) { clicked = true; button.setText("Send Slip"); return button; }
        @Override public Object getCellEditorValue() { return "Send Slip"; }
        @Override public boolean stopCellEditing() { clicked = false; return super.stopCellEditing(); }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); setFont(new Font("Segoe UI", Font.PLAIN, 11)); setBackground(new Color(240, 240, 240)); setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int column) { setText("View Payroll"); return this; }
    }
    class ButtonEditor extends DefaultCellEditor {
        private JButton button = new JButton(); private boolean clicked;
        public ButtonEditor(JCheckBox checkBox) { super(checkBox); button.setOpaque(true); button.addActionListener(e -> { if (clicked) { int row = table.getSelectedRow(); String empNo = (String) table.getValueAt(row, 1); String name = (String) table.getValueAt(row, 2); String status = (String) table.getValueAt(row, 3); double dailyPay = 0; try { dailyPay = df.parse((String)table.getValueAt(row, 4)).doubleValue(); } catch(Exception ex) {} new PayrollDetailsDialog(empNo, name, status, dailyPay, true).setVisible(true); } clicked = false; fireEditingStopped(); }); }
        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSel, int row, int column) { clicked = true; button.setText("View Payroll"); return button; }
        @Override public Object getCellEditorValue() { return "View Payroll"; }
        @Override public boolean stopCellEditing() { clicked = false; return super.stopCellEditing(); }
    }

    // ==========================================
    //      PAYROLL DETAILS DIALOG
    // ==========================================
    private class PayrollDetailsDialog extends JDialog {
        private JTable detailsTable;
        private DefaultTableModel detailsModel;
        private JLabel lblTotalNet, lblTotalDed, lblTotalGross;
        private JComboBox<String> cbMonth, cbYear;
        private String empNo, empName, empStatus;
        private double baseDailyPay;

        public int countPresent = 0, countAbsent = 0, countLate = 0, countUndertime = 0;
        public double valGross = 0, valDed = 0, valNet = 0;

        public PayrollDetailsDialog(String empNo, String name, String status, double dailyPay, boolean showUI) {
            this.empNo = empNo; this.empName = name; this.empStatus = status; this.baseDailyPay = dailyPay;

            cbMonth = new JComboBox<>(new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"});
            cbYear = new JComboBox<>(new String[]{"2024", "2025", "2026"});
            Calendar cal = Calendar.getInstance();
            cbMonth.setSelectedIndex(cal.get(Calendar.MONTH));
            cbYear.setSelectedItem(String.valueOf(cal.get(Calendar.YEAR)));
            
            detailsModel = new DefaultTableModel(new String[]{"Date", "Workday", "Status", "Earned", "Ded", "Net"}, 0);
            
            if(showUI) {
                initUI();
                calculateAndLoadPayroll(); 
            }
        }
        
        public void overrideDateContext(String month, String year) {
            cbMonth.setSelectedItem(month);
            cbYear.setSelectedItem(year);
        }
        
        private void initUI() {
            setTitle("Payroll Records: " + empName + " (" + empNo + ")");
            setSize(950, 600);
            setLocationRelativeTo(null);
            setModal(true);
            setLayout(new BorderLayout(15, 15));
            getContentPane().setBackground(Color.WHITE);
            
            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
            headerPanel.setBackground(new Color(245, 245, 245));
            JButton btnPrint = new JButton("Print PDF"); styleButton(btnPrint, BRAND_COLOR); btnPrint.addActionListener(e -> printRecord());
            
            JButton btnSend = new JButton("Send Payslip");
            styleButton(btnSend, new Color(40, 167, 69));
            btnSend.addActionListener(e -> {
               if(generateAndSavePayslip(empNo, empName, (String)cbMonth.getSelectedItem(), (String)cbYear.getSelectedItem())) {
                   JOptionPane.showMessageDialog(this, "Payslip Sent Successfully!");
               } else {
                   JOptionPane.showMessageDialog(this, "Failed. Payslip likely already exists.");
               }
            });

            cbMonth.addActionListener(e -> calculateAndLoadPayroll());
            cbYear.addActionListener(e -> calculateAndLoadPayroll());

            headerPanel.add(new JLabel("Period:")); headerPanel.add(cbMonth); headerPanel.add(cbYear);
            headerPanel.add(btnPrint); headerPanel.add(btnSend);
            add(headerPanel, BorderLayout.NORTH);
            
            detailsTable = new JTable(detailsModel);
            styleTable(detailsTable);
            add(new JScrollPane(detailsTable), BorderLayout.CENTER);
            
            JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 15));
            lblTotalGross = createTotalLabel("Gross: 0.00", new Color(40, 167, 69));
            lblTotalDed = createTotalLabel("Deductions: 0.00", new Color(220, 53, 69));
            lblTotalNet = createTotalLabel("NET PAY: 0.00", BRAND_COLOR);
            footerPanel.add(lblTotalGross); footerPanel.add(lblTotalDed); footerPanel.add(lblTotalNet);
            add(footerPanel, BorderLayout.SOUTH);
        }

        private JLabel createTotalLabel(String text, Color c) {
            JLabel l = new JLabel(text); l.setFont(new Font("Segoe UI", Font.BOLD, 14)); l.setForeground(c); return l;
        }
        
        private void updateLabels() {
            if(lblTotalGross != null && lblTotalGross.isShowing()) { 
                lblTotalGross.setText("Gross: " + df.format(valGross));
                lblTotalDed.setText("Deductions: " + df.format(valDed));
                lblTotalNet.setText("NET PAY: " + df.format(valNet));
            }
        }

        private void printRecord() {
            try {
                MessageFormat header = new MessageFormat("Payroll: " + empName + " - " + cbMonth.getSelectedItem() + " " + cbYear.getSelectedItem());
                MessageFormat footer = new MessageFormat("Page {0,number,integer}");
                detailsTable.print(JTable.PrintMode.FIT_WIDTH, header, footer);
            } catch (PrinterException pe) { JOptionPane.showMessageDialog(this, "Printing Failed: " + pe.getMessage()); }
        }

        public void calculateAndLoadPayroll() {
            countPresent = 0; countAbsent = 0; countLate = 0; countUndertime = 0;
            valGross = 0; valDed = 0; valNet = 0;
            if(detailsModel != null) detailsModel.setRowCount(0);

            int month = cbMonth.getSelectedIndex() + 1;
            int year = Integer.parseInt((String) cbYear.getSelectedItem());
            double hourlyRate = ("Regular".equalsIgnoreCase(empStatus)) ? 81.25 : 75.00; 
            
            String sql = "SELECT * FROM attendance_records WHERE empNo = ? AND CAST(strftime('%m', date) AS INTEGER) = ? AND CAST(strftime('%Y', date) AS INTEGER) = ? ORDER BY date ASC";
            
            try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, empNo); ps.setInt(2, month); ps.setInt(3, year);
                ResultSet rs = ps.executeQuery();
                DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.US);
                
                while (rs.next()) {
                    String attStatus = rs.getString("status");
                    String workdayType = rs.getString("workday");
                    String timeInStr = rs.getString("time_in");
                    String timeOut2Str = rs.getString("second_time_out");
                    String timeOut1Str = rs.getString("time_out");
                    
                    double dayEarned = 0, deduction = 0;
                    
                    if ("Absent".equalsIgnoreCase(attStatus)) { countAbsent++; } 
                    else { 
                        countPresent++; 
                        if("OT".equalsIgnoreCase(workdayType) && timeInStr != null && timeOut2Str != null) {
                             try {
                                LocalTime tIn = LocalTime.parse(timeInStr, timeFmt);
                                LocalTime tOut2 = LocalTime.parse(timeOut2Str, timeFmt);
                                long mins = Duration.between(tIn, tOut2).toMinutes();
                                dayEarned = (mins/60.0) * hourlyRate;
                             } catch(Exception e) {}
                        } else if("RH".equalsIgnoreCase(workdayType)) dayEarned = baseDailyPay*2;
                        else if("SH".equalsIgnoreCase(workdayType)) dayEarned = baseDailyPay*1.5;
                        else dayEarned = baseDailyPay;
                    }
                    
                    if (!"Absent".equalsIgnoreCase(attStatus) && timeInStr != null && !timeInStr.startsWith("00:00")) {
                        try {
                            LocalTime tIn = LocalTime.parse(timeInStr, timeFmt);
                            LocalTime startShift = LocalTime.of(8, 0, 0); 
                            if (tIn.isAfter(startShift)) {
                                long lateMins = Duration.between(startShift, tIn).toMinutes();
                                deduction += (lateMins * 1.35); 
                                countLate++; 
                            }
                            LocalTime target = ("OT".equalsIgnoreCase(workdayType) && timeOut2Str != null) ? LocalTime.parse(timeOut2Str, timeFmt) : LocalTime.of(17, 0, 0);
                            if(timeOut1Str != null && !timeOut1Str.startsWith("00:00")) {
                                LocalTime tOut = LocalTime.parse(timeOut1Str, timeFmt);
                                if(tOut.isBefore(target)) {
                                    long under = Duration.between(tOut, target).toMinutes();
                                    deduction += (hourlyRate/60.0)*under;
                                    countUndertime++;
                                }
                            }
                        } catch(Exception e){}
                    }
                    double dayNet = Math.max(0, dayEarned - deduction);
                    valGross += dayEarned; valDed += deduction; valNet += dayNet;
                    
                    if(detailsModel != null) detailsModel.addRow(new Object[]{rs.getString("date"), workdayType, attStatus, df.format(dayEarned), df.format(deduction), df.format(dayNet)});
                }
                updateLabels();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}