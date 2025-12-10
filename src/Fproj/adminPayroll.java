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
import java.util.Calendar;
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

        // Search Bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        
        txtSearch = new JTextField(15);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        JButton btnSearch = new JButton("Search Employee");
        styleButton(btnSearch, BRAND_COLOR);
        btnSearch.addActionListener(e -> loadEmployees());

        searchPanel.add(new JLabel("Search ID/Name:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- Main Table (Employee List) ---
        String[] columns = {"Emp No", "Name", "Status", "Base Daily Pay", "Action"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; 
            }
        };

        table = new JTable(model);
        styleTable(table);
        
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
        table.getColumn("Action").setMaxWidth(100);
        table.getColumn("Action").setMinWidth(100);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(scroll, BorderLayout.CENTER);

        loadEmployees();
    }

    private void loadEmployees() {
        model.setRowCount(0);
        String search = txtSearch.getText().trim();
        String sql = "SELECT empNo, name, employmentStatus, dailyPay FROM employees WHERE empNo LIKE ? OR name LIKE ?";

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, "%" + search + "%");
            ps.setString(2, "%" + search + "%");
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("empNo"),
                    rs.getString("name"),
                    rs.getString("employmentStatus"),
                    df.format(rs.getDouble("dailyPay")),
                    "View Payroll"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
        for(int i=0; i<t.getColumnCount(); i++) {
             t.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); setFont(new Font("Segoe UI", Font.PLAIN, 11)); setBackground(new Color(240, 240, 240)); setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int column) { setText("View Payroll"); return this; }
    }
    class ButtonEditor extends DefaultCellEditor {
        private JButton button = new JButton(); private boolean clicked;
        public ButtonEditor(JCheckBox checkBox) { super(checkBox); button.setOpaque(true); button.addActionListener(e -> { if (clicked) { int row = table.getSelectedRow(); String empNo = (String) table.getValueAt(row, 0); String name = (String) table.getValueAt(row, 1); String status = (String) table.getValueAt(row, 2); double dailyPay = 0; try { dailyPay = df.parse((String)table.getValueAt(row, 3)).doubleValue(); } catch(Exception ex) {} new PayrollDetailsDialog(empNo, name, status, dailyPay).setVisible(true); } clicked = false; fireEditingStopped(); }); }
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

        // --- Summary Variables for Payslip ---
        private int countPresent = 0;
        private int countAbsent = 0;
        private int countLate = 0;
        private int countUndertime = 0;
        private double valGross = 0;
        private double valDed = 0;
        private double valNet = 0;

        public PayrollDetailsDialog(String empNo, String name, String status, double dailyPay) {
            this.empNo = empNo;
            this.empName = name;
            this.empStatus = status;
            this.baseDailyPay = dailyPay;

            setTitle("Payroll Records: " + name + " (" + empNo + ")");
            setSize(950, 600);
            setLocationRelativeTo(null);
            setModal(true);
            setLayout(new BorderLayout(15, 15));
            getContentPane().setBackground(Color.WHITE);

            // --- Header ---
            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
            headerPanel.setBackground(new Color(245, 245, 245));
            headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

            cbMonth = new JComboBox<>(new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"});
            cbYear = new JComboBox<>(new String[]{"2024", "2025", "2026"});
            
            Calendar cal = Calendar.getInstance();
            cbMonth.setSelectedIndex(cal.get(Calendar.MONTH));
            cbYear.setSelectedItem(String.valueOf(cal.get(Calendar.YEAR)));

            cbMonth.addActionListener(e -> calculateAndLoadPayroll());
            cbYear.addActionListener(e -> calculateAndLoadPayroll());

            JButton btnPrint = new JButton("Print PDF");
            styleButton(btnPrint, BRAND_COLOR);
            btnPrint.addActionListener(e -> printRecord());

            // NEW BUTTON: Send Payslip
            JButton btnSendPayslip = new JButton("Send Payslip");
            styleButton(btnSendPayslip, new Color(40, 167, 69)); // Brighter Green
            btnSendPayslip.addActionListener(e -> sendPayslipAction());

            headerPanel.add(new JLabel("Period:"));
            headerPanel.add(cbMonth);
            headerPanel.add(cbYear);
            headerPanel.add(Box.createHorizontalStrut(15));
            headerPanel.add(btnPrint);
            headerPanel.add(btnSendPayslip); 

            add(headerPanel, BorderLayout.NORTH);

            // --- Center Table ---
            String[] cols = {"Date", "Workday", "Status", "Earned Pay", "Deductions", "Net Pay"};
            detailsModel = new DefaultTableModel(cols, 0);
            detailsTable = new JTable(detailsModel);
            styleTable(detailsTable);
            detailsTable.setRowHeight(30);
            
            JScrollPane scroll = new JScrollPane(detailsTable);
            scroll.getViewport().setBackground(Color.WHITE);
            add(scroll, BorderLayout.CENTER);

            // --- Footer ---
            JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 15));
            footerPanel.setBackground(new Color(245, 245, 245));
            footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

            lblTotalGross = createTotalLabel("Gross: 0.00", Color.DARK_GRAY);
            lblTotalDed = createTotalLabel("Deductions: 0.00", Color.RED);
            lblTotalNet = createTotalLabel("NET PAY: 0.00", new Color(0, 100, 0));
            lblTotalNet.setFont(new Font("Segoe UI", Font.BOLD, 16));

            footerPanel.add(lblTotalGross);
            footerPanel.add(lblTotalDed);
            footerPanel.add(lblTotalNet);
            add(footerPanel, BorderLayout.SOUTH);
            
            calculateAndLoadPayroll();
        }

        private JLabel createTotalLabel(String text, Color c) {
            JLabel l = new JLabel(text); l.setFont(new Font("Segoe UI", Font.BOLD, 14)); l.setForeground(c); return l;
        }

        private void printRecord() {
            try {
                MessageFormat header = new MessageFormat("Payroll: " + empName + " - " + cbMonth.getSelectedItem() + " " + cbYear.getSelectedItem());
                MessageFormat footer = new MessageFormat(lblTotalGross.getText() + " | " + lblTotalDed.getText() + " | " + lblTotalNet.getText());
                if (detailsTable.print(JTable.PrintMode.FIT_WIDTH, header, footer)) {
                    JOptionPane.showMessageDialog(this, "Printing Complete");
                }
            } catch (PrinterException pe) {
                JOptionPane.showMessageDialog(this, "Printing Failed: " + pe.getMessage());
            }
        }

        // NEW: Send Payslip Action
        private void sendPayslipAction() {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Save and Send Payslip for " + cbMonth.getSelectedItem() + " " + cbYear.getSelectedItem() + "?\n\n" +
                "Gross: " + df.format(valGross) + "\n" +
                "Net: " + df.format(valNet), 
                "Confirm Send", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // Call Database to store and return boolean success
                boolean success = Database.savePayslip(
                    empNo, 
                    (String) cbMonth.getSelectedItem(), 
                    (String) cbYear.getSelectedItem(), 
                    countPresent, 
                    countAbsent, 
                    countLate, 
                    countUndertime, 
                    valGross, 
                    valDed, 
                    valNet
                );
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "Payslip Stored Successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to store payslip. Check database connection or logs.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void calculateAndLoadPayroll() {
            detailsModel.setRowCount(0);
            
            // RESET COUNTERS
            countPresent = 0;
            countAbsent = 0;
            countLate = 0;
            countUndertime = 0;
            valGross = 0;
            valDed = 0;
            valNet = 0;

            int month = cbMonth.getSelectedIndex() + 1;
            int year = Integer.parseInt((String) cbYear.getSelectedItem());

            double hourlyRate = ("Regular".equalsIgnoreCase(empStatus)) ? 81.25 : 75.00; 

            String sql = "SELECT * FROM attendance_records WHERE empNo = ? " +
                         "AND CAST(strftime('%m', date) AS INTEGER) = ? " +
                         "AND CAST(strftime('%Y', date) AS INTEGER) = ? " +
                         "ORDER BY date ASC";
            
            try (Connection conn = Database.connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setString(1, empNo);
                ps.setInt(2, month);
                ps.setInt(3, year);
                
                ResultSet rs = ps.executeQuery();
                DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.US);

                while (rs.next()) {
                    String date = rs.getString("date");
                    String workdayType = rs.getString("workday"); 
                    String attStatus = rs.getString("status");    
                    String timeInStr = rs.getString("time_in");
                    String timeOut1Str = rs.getString("time_out");         
                    String timeOut2Str = rs.getString("second_time_out");  

                    double dayEarned = 0;
                    double deduction = 0;
                    double dayNet = 0;
                    
                    // --- Count Absent/Present ---
                    if ("Absent".equalsIgnoreCase(attStatus)) {
                        countAbsent++;
                        dayEarned = 0;
                    } else {
                        countPresent++;
                        
                        // EARN PAY LOGIC
                        if ("OT".equalsIgnoreCase(workdayType)) {
                            if (timeInStr != null && timeOut2Str != null && !timeInStr.startsWith("00:00") && !timeOut2Str.startsWith("00:00")) {
                                try {
                                    LocalTime tIn = LocalTime.parse(timeInStr, timeFmt);
                                    LocalTime tOut2 = LocalTime.parse(timeOut2Str, timeFmt);
                                    long minutes = Duration.between(tIn, tOut2).toMinutes();
                                    dayEarned = (minutes / 60.0) * hourlyRate;
                                } catch (Exception e) { dayEarned = 0; }
                            }
                        } else if ("RH".equalsIgnoreCase(workdayType)) {
                            dayEarned = baseDailyPay * 2;
                        } else if ("SH".equalsIgnoreCase(workdayType)) {
                            dayEarned = baseDailyPay * 1.5;
                        } else {
                            dayEarned = baseDailyPay; // Regular
                        }
                    }

                    // --- DEDUCTIONS & Count Late/Undertime ---
                    if (!"Absent".equalsIgnoreCase(attStatus) && timeInStr != null && !timeInStr.startsWith("00:00")) {
                        try {
                            LocalTime tIn = LocalTime.parse(timeInStr, timeFmt);
                            LocalTime startShift = LocalTime.of(8, 0, 0); 
                            
                            // A. LATE
                            if (tIn.isAfter(startShift)) {
                                long lateMins = Duration.between(startShift, tIn).toMinutes();
                                deduction += (lateMins * 1.35);
                                countLate++; // Increment Late Count
                            }
                            
                            // B. UNDERTIME
                            LocalTime targetExitTime;
                            if ("OT".equalsIgnoreCase(workdayType) && timeOut2Str != null && !timeOut2Str.startsWith("00:00")) {
                                targetExitTime = LocalTime.parse(timeOut2Str, timeFmt);
                            } else {
                                targetExitTime = LocalTime.of(17, 0, 0);
                            }

                            if (timeOut1Str != null && !timeOut1Str.startsWith("00:00")) {
                                LocalTime actualOut = LocalTime.parse(timeOut1Str, timeFmt);
                                if (actualOut.isBefore(targetExitTime)) {
                                    long underMins = Duration.between(actualOut, targetExitTime).toMinutes();
                                    deduction += (hourlyRate / 60.0) * underMins;
                                    countUndertime++; // Increment Undertime Count
                                }
                            }
                        } catch (Exception e) {}
                    }

                    dayNet = dayEarned - deduction;
                    if(dayNet < 0) dayNet = 0;

                    // Accumulate Totals
                    valGross += dayEarned;
                    valDed += deduction;
                    valNet += dayNet;

                    detailsModel.addRow(new Object[]{
                        date, workdayType, (attStatus == null ? "" : attStatus),
                        df.format(dayEarned), df.format(deduction), df.format(dayNet)
                    });
                }

                lblTotalGross.setText("Gross: " + df.format(valGross));
                lblTotalDed.setText("Deductions: " + df.format(valDed));
                lblTotalNet.setText("NET PAY: " + df.format(valNet));

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}