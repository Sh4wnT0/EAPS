package Fproj;

import com.toedter.calendar.JDateChooser; // Requires jcalendar-1.4.jar
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

public class EmpAttendancePanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private String empNo;

    public EmpAttendancePanel(String empNo) {
        this.empNo = empNo;

        // --- UI SETUP ---
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- TOP PANEL: Title and Buttons ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        // Title
        JLabel lblTitle = new JLabel("Daily Attendance");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        topPanel.add(lblTitle, BorderLayout.WEST);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(Color.WHITE);

        // --- NEW ACR BUTTON ---
        JButton btnACR = new JButton("ACR");
        btnACR.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnACR.setBackground(new Color(52, 152, 219)); // Blue
        btnACR.setForeground(Color.WHITE);
        btnACR.setPreferredSize(new Dimension(170, 40));
        btnACR.setFocusPainted(false);
        btnACR.addActionListener(e -> showACRDialog()); // Action Listener

        JButton btnIn = new JButton("Time-In");
        btnIn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnIn.setBackground(new Color(46, 204, 113)); // Modern Green
        btnIn.setForeground(Color.WHITE);
        btnIn.setPreferredSize(new Dimension(120, 40));
        btnIn.setFocusPainted(false);

        JButton btnOut = new JButton("Time-Out");
        btnOut.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnOut.setBackground(new Color(231, 76, 60)); // Modern Red
        btnOut.setForeground(Color.WHITE);
        btnOut.setPreferredSize(new Dimension(120, 40));
        btnOut.setFocusPainted(false);

        btnPanel.add(btnACR); // Add ACR Button
        btnPanel.add(btnIn);
        btnPanel.add(btnOut);
        topPanel.add(btnPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: TABLE ---
        model = new DefaultTableModel(new String[]{"Date", "Time-In", "Time-Out", "Workday", "OT Time-Out", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                String workday = (String) getValueAt(row, 3);
                String value = (String) getValueAt(row, column);
                boolean isPlaceholder = value == null || "00:00:00 AM".equals(value);
                if (("OT".equals(workday) || "SH".equals(workday) || "RH".equals(workday)) && isPlaceholder && (column == 1 || column == 2)) {
                    String date = (String) getValueAt(row, 0);
                    int totalUpdateCount = getTotalUpdateCount(date);
                    return totalUpdateCount < 2;
                }
                return false;
            }
        };

        table = new JTable(model);
        
        // Table Styling
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Custom Editors
        table.getColumnModel().getColumn(1).setCellEditor(new TimeInEditor());
        table.getColumnModel().getColumn(2).setCellEditor(new TimeOutEditor());

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // Button Actions
        btnIn.addActionListener(e -> saveRecord("IN"));
        btnOut.addActionListener(e -> saveRecord("OUT"));

        loadTable();
    }

    // =========================================================================
    //                      ACR DIALOG LOGIC
    // =========================================================================

    private void showACRDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Attendance Correction Request", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        // Container for dynamic rows
        JPanel rowsPanel = new JPanel();
        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(rowsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add the first row by default
        addACRRow(rowsPanel);

        // Bottom Panel (Add Button + Submit)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnAdd = new JButton("+ Add Another");
        btnAdd.addActionListener(e -> addACRRow(rowsPanel));

        JButton btnSubmit = new JButton("Submit Requests");
        btnSubmit.setBackground(new Color(46, 204, 113));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.addActionListener(e -> submitACRRequests(dialog, rowsPanel));

        bottomPanel.add(btnAdd);
        bottomPanel.add(btnSubmit);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

 // =========================================================================
    //                  UPDATED ACR LOGIC (Auto-Fill Time)
    // =========================================================================

    private void addACRRow(JPanel container) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        // 1. Time Spinners (Created first so we can reference them in the Date Listener)
        JSpinner timeInSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeInEditor = new JSpinner.DateEditor(timeInSpinner, "hh:mm:ss a");
        timeInSpinner.setEditor(timeInEditor);
        timeInSpinner.setValue(new Date()); // Default to now

        JSpinner timeOutSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeOutEditor = new JSpinner.DateEditor(timeOutSpinner, "hh:mm:ss a");
        timeOutSpinner.setEditor(timeOutEditor);
        timeOutSpinner.setValue(new Date());

        // 2. JCalendar Date Picker with Auto-Check Listener
        JLabel lblDate = new JLabel("Date:");
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setPreferredSize(new Dimension(120, 30));

        // --- NEW: LISTENER TO AUTO-CHECK DATABASE ---
        dateChooser.addPropertyChangeListener("date", evt -> {
            if (evt.getNewValue() instanceof Date) {
                Date selectedDate = (Date) evt.getNewValue();
                // Call helper to check DB and update spinners
                fetchAndFillTime(selectedDate, timeInSpinner, timeOutSpinner);
            }
        });

        // 3. Labels
        JLabel lblIn = new JLabel("Time-In:");
        JLabel lblOut = new JLabel("Time-Out:");

        // 4. Remove Button
        JButton btnRemove = new JButton("x");
        btnRemove.setForeground(Color.RED);
        btnRemove.setBorder(BorderFactory.createEmptyBorder());
        btnRemove.setContentAreaFilled(false);
        btnRemove.addActionListener(e -> {
            container.remove(row);
            container.revalidate();
            container.repaint();
        });

        // Add components to row
        row.add(lblDate);
        row.add(dateChooser);
        row.add(lblIn);
        row.add(timeInSpinner);
        row.add(lblOut);
        row.add(timeOutSpinner);
        row.add(btnRemove);

        // Add hidden property to identify this component
        row.putClientProperty("isRow", true);

        container.add(row);
        container.revalidate();
        container.repaint();
    }

    // --- NEW HELPER METHOD ---
    private void fetchAndFillTime(Date date, JSpinner inSpinner, JSpinner outSpinner) {
        if (date == null) return;

        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");
        String dateStr = dbDateFormat.format(date);

        String sql = "SELECT time_in, time_out FROM attendance_records WHERE empNo = ? AND date = ?";

        try (Connection con = DriverManager.getConnection("jdbc:sqlite:employees.db");
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, empNo);
            ps.setString(2, dateStr);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String dbTimeIn = rs.getString("time_in");
                String dbTimeOut = rs.getString("time_out");

                // Update Time-In Spinner if record exists
                if (dbTimeIn != null && !dbTimeIn.isEmpty() && !"00:00:00 AM".equals(dbTimeIn)) {
                    try {
                        Date tIn = timeFormat.parse(dbTimeIn);
                        inSpinner.setValue(tIn);
                    } catch (Exception ignored) {} // Ignore parse errors
                }

                // Update Time-Out Spinner if record exists
                if (dbTimeOut != null && !dbTimeOut.isEmpty()) {
                    try {
                        Date tOut = timeFormat.parse(dbTimeOut);
                        outSpinner.setValue(tOut);
                    } catch (Exception ignored) {}
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void submitACRRequests(JDialog dialog, JPanel rowsPanel) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");
        
        List<String[]> requests = new ArrayList<>();
        boolean hasError = false;

        // Iterate through UI components to gather data
        for (Component comp : rowsPanel.getComponents()) {
            if (comp instanceof JPanel && ((JPanel)comp).getClientProperty("isRow") != null) {
                JPanel row = (JPanel) comp;
                
                // Extract Components based on index (fragile but effective for simple layout)
                JDateChooser dc = (JDateChooser) row.getComponent(1);
                JSpinner spinIn = (JSpinner) row.getComponent(3);
                JSpinner spinOut = (JSpinner) row.getComponent(5);

                Date selectedDate = dc.getDate();
                if (selectedDate == null) {
                    JOptionPane.showMessageDialog(dialog, "Please select a date for all rows.", "Error", JOptionPane.ERROR_MESSAGE);
                    hasError = true;
                    break;
                }

                String dateStr = dateFormat.format(selectedDate);
                String timeInStr = timeFormat.format(spinIn.getValue());
                String timeOutStr = timeFormat.format(spinOut.getValue());

                requests.add(new String[]{dateStr, timeInStr, timeOutStr});
            }
        }

        if (hasError) return;
        if (requests.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "No requests to submit.");
            return;
        }

        // Database Insert
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:employees.db")) {
            con.setAutoCommit(false);
            String sql = "INSERT INTO attendance_requests (empNo, request_date, time_in, time_out, status) VALUES (?, ?, ?, ?, 'Pending')";
            
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (String[] req : requests) {
                    ps.setString(1, empNo);
                    ps.setString(2, req[0]); // Date
                    ps.setString(3, req[1]); // Time In
                    ps.setString(4, req[2]); // Time Out
                    ps.addBatch();
                }
                ps.executeBatch();
                con.commit();
                
                JOptionPane.showMessageDialog(dialog, "Requests submitted successfully!");
                dialog.dispose();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Database Error: " + e.getMessage());
        }
    }


    // =========================================================================
    //                  EXISTING HELPER METHODS (Preserved)
    // =========================================================================

    class TimeInEditor extends javax.swing.AbstractCellEditor implements TableCellEditor {
        private JTextField textField = new JTextField();
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            textField.setText(value != null ? value.toString() : "");
            textField.setFont(new Font("Segoe UI", Font.PLAIN, 12)); 
            return textField;
        }
        @Override
        public Object getCellEditorValue() {
            String newValue = textField.getText();
            String date = (String) table.getValueAt(table.getSelectedRow(), 0);
            int totalUpdateCount = getTotalUpdateCount(date);
            if (totalUpdateCount >= 2) {
                JOptionPane.showMessageDialog(null, "Time-In/Time-Out can only be updated twice total for OT/SH/RH workdays.");
                return table.getValueAt(table.getSelectedRow(), 1);
            }
            updateTimeInInDB(date, newValue);
            incrementTotalUpdateCount(date);
            return newValue;
        }
    }

    class TimeOutEditor extends javax.swing.AbstractCellEditor implements TableCellEditor {
        private JTextField textField = new JTextField();
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            textField.setText(value != null ? value.toString() : "");
            textField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            return textField;
        }
        @Override
        public Object getCellEditorValue() {
            String newValue = textField.getText();
            String date = (String) table.getValueAt(table.getSelectedRow(), 0);
            int totalUpdateCount = getTotalUpdateCount(date);
            if (totalUpdateCount >= 2) {
                JOptionPane.showMessageDialog(null, "Time-In/Time-Out can only be updated twice total for OT/SH/RH workdays.");
                return table.getValueAt(table.getSelectedRow(), 2);
            }
            updateTimeOutInDB(date, newValue);
            incrementTotalUpdateCount(date);
            return newValue;
        }
    }

    private int getTotalUpdateCount(String date) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:employees.db");
             PreparedStatement ps = con.prepareStatement("SELECT total_update_count FROM attendance_records WHERE empNo = ? AND date = ?")) {
            ps.setString(1, empNo);
            ps.setString(2, date);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("total_update_count");
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    private void incrementTotalUpdateCount(String date) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:employees.db");
             PreparedStatement ps = con.prepareStatement("UPDATE attendance_records SET total_update_count = total_update_count + 1 WHERE empNo = ? AND date = ?")) {
            ps.setString(1, empNo);
            ps.setString(2, date);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateTimeInInDB(String date, String newTimeIn) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:employees.db");
             PreparedStatement ps = con.prepareStatement("UPDATE attendance_records SET time_in = ? WHERE empNo = ? AND date = ?")) {
            ps.setString(1, newTimeIn);
            ps.setString(2, empNo);
            ps.setString(3, date);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateTimeOutInDB(String date, String newTimeOut) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:employees.db");
             PreparedStatement ps = con.prepareStatement("UPDATE attendance_records SET time_out = ? WHERE empNo = ? AND date = ?")) {
            ps.setString(1, newTimeOut);
            ps.setString(2, empNo);
            ps.setString(3, date);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void saveRecord(String type) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:employees.db")) {
            String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String now = new SimpleDateFormat("hh:mm:ss a").format(new Date());

            String currentWorkday = null;
            try (PreparedStatement workdayPs = con.prepareStatement("SELECT workday FROM attendance_records WHERE empNo = ? AND date = ?")) {
                workdayPs.setString(1, empNo);
                workdayPs.setString(2, today);
                ResultSet rs = workdayPs.executeQuery();
                if (rs.next()) currentWorkday = rs.getString("workday");
            }

            boolean allowUpdates = "OT".equals(currentWorkday) || "SH".equals(currentWorkday) || "RH".equals(currentWorkday);

            if (type.equals("IN")) {
                int totalUpdateCount = 0;
                try (PreparedStatement countPs = con.prepareStatement("SELECT total_update_count FROM attendance_records WHERE empNo = ? AND date = ?")) {
                    countPs.setString(1, empNo);
                    countPs.setString(2, today);
                    ResultSet rs = countPs.executeQuery();
                    if (rs.next()) totalUpdateCount = rs.getInt("total_update_count");
                }
                if (totalUpdateCount >= 2) {
                    JOptionPane.showMessageDialog(null, "Time-In/Time-Out can only be updated twice total for OT/SH/RH workdays.");
                    return;
                }

                try (PreparedStatement checkPs = con.prepareStatement("SELECT time_in FROM attendance_records WHERE empNo=? AND date=?")) {
                    checkPs.setString(1, empNo);
                    checkPs.setString(2, today);
                    ResultSet checkRs = checkPs.executeQuery();
                    if (checkRs.next()) {
                        String timeIn = checkRs.getString("time_in");
                        if (timeIn == null || ("OT".equals(currentWorkday) && "00:00:00 AM".equals(timeIn))) {
                            try (PreparedStatement ps = con.prepareStatement("UPDATE attendance_records SET time_in = ?, total_update_count = total_update_count + 1 WHERE empNo = ? AND date = ?")) {
                                ps.setString(1, now);
                                ps.setString(2, empNo);
                                ps.setString(3, today);
                                ps.executeUpdate();
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "Time-in already recorded for today!");
                            return;
                        }
                    } else {
                        try (PreparedStatement ps = con.prepareStatement("INSERT INTO attendance_records(empNo, date, time_in, workday, total_update_count) VALUES (?, ?, ?, ?, 1)")) {
                            ps.setString(1, empNo);
                            ps.setString(2, today);
                            ps.setString(3, now);
                            ps.setString(4, "regular");
                            ps.executeUpdate();
                        }
                    }
                }
            } else { // OUT
                int totalUpdateCount = 0;
                try (PreparedStatement countPs = con.prepareStatement("SELECT total_update_count FROM attendance_records WHERE empNo = ? AND date = ?")) {
                    countPs.setString(1, empNo);
                    countPs.setString(2, today);
                    ResultSet rs = countPs.executeQuery();
                    if (rs.next()) totalUpdateCount = rs.getInt("total_update_count");
                }
                if (totalUpdateCount >= 2) {
                    JOptionPane.showMessageDialog(null, "Time-In/Time-Out can only be updated twice total for OT/SH/RH workdays.");
                    return;
                }

                try (PreparedStatement checkPs = con.prepareStatement("SELECT time_out, second_time_out FROM attendance_records WHERE empNo=? AND date=?")) {
                    checkPs.setString(1, empNo);
                    checkPs.setString(2, today);
                    ResultSet checkRs = checkPs.executeQuery();
                    if (checkRs.next()) {
                        String timeOut = checkRs.getString("time_out");
                        String secondTimeOut = checkRs.getString("second_time_out");
                        if (timeOut == null) {
                            try (PreparedStatement ps = con.prepareStatement("UPDATE attendance_records SET time_out = ?, total_update_count = total_update_count + 1 WHERE empNo = ? AND date = ?")) {
                                ps.setString(1, now);
                                ps.setString(2, empNo);
                                ps.setString(3, today);
                                ps.executeUpdate();
                            }
                        } else if (allowUpdates && secondTimeOut == null) {
                            try (PreparedStatement ps = con.prepareStatement("UPDATE attendance_records SET second_time_out = ?, total_update_count = total_update_count + 1 WHERE empNo = ? AND date = ?")) {
                                ps.setString(1, now);
                                ps.setString(2, empNo);
                                ps.setString(3, today);
                                ps.executeUpdate();
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "Time-out already recorded for today!");
                            return;
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "No time-in found for today!");
                        return;
                    }
                }
            }

            String newStatus = calculateStatus(today);
            try (PreparedStatement statusPs = con.prepareStatement("UPDATE attendance_records SET status = ? WHERE empNo = ? AND date = ?")) {
                statusPs.setString(1, newStatus);
                statusPs.setString(2, empNo);
                statusPs.setString(3, today);
                statusPs.executeUpdate();
            }

            loadTable();
            JOptionPane.showMessageDialog(null, "Recorded successfully!");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving record: " + ex.getMessage());
        }
    }

    private String calculateStatus(String dateStr) {
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
            java.time.DayOfWeek day = date.getDayOfWeek();
            boolean isWorkday = !(day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY);
            if (!isWorkday) return "";
        } catch (Exception e) { return "Error"; }

        try (Connection con = DriverManager.getConnection("jdbc:sqlite:employees.db");
             PreparedStatement ps = con.prepareStatement("SELECT time_in, time_out, second_time_out, workday, status FROM attendance_records WHERE empNo = ? AND date = ?")) {
            ps.setString(1, empNo);
            ps.setString(2, dateStr);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String timeIn = rs.getString("time_in");
                String timeOut = rs.getString("time_out");
                String secondTimeOut = rs.getString("second_time_out");
                String workday = rs.getString("workday");

                if ("OT".equals(workday)) {
                    if (timeIn != null && timeOut != null && !"00:00:00 AM".equals(timeIn) && secondTimeOut != null) {
                        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a");
                        java.time.LocalTime timeInParsed = java.time.LocalTime.parse(timeIn, formatter);
                        java.time.LocalTime timeOutParsed = java.time.LocalTime.parse(timeOut, formatter);
                        java.time.LocalTime expectedEnd = java.time.LocalTime.parse(secondTimeOut, formatter);
                        java.time.LocalTime start = java.time.LocalTime.of(8, 0);

                        boolean onTimeIn = timeInParsed.isBefore(start) || timeInParsed.equals(start);
                        boolean onTimeOut = timeOutParsed.isAfter(expectedEnd) || timeOutParsed.equals(expectedEnd);

                        if (onTimeIn && onTimeOut) return "On Time";
                        if (!onTimeIn && onTimeOut) return "Late";
                        if (onTimeIn && !onTimeOut) return "Undertime";
                        return "Late and Undertime";
                    } else { return "OT"; }
                }

                if ("SH".equals(workday) || "RH".equals(workday)) {
                    if (timeIn != null && timeOut != null) {
                        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a");
                        java.time.LocalTime timeInParsed = java.time.LocalTime.parse(timeIn, formatter);
                        java.time.LocalTime timeOutParsed = java.time.LocalTime.parse(timeOut, formatter);
                        java.time.LocalTime start = java.time.LocalTime.of(8, 0);
                        java.time.LocalTime end = java.time.LocalTime.of(17, 0);

                        boolean onTimeIn = timeInParsed.isBefore(start) || timeInParsed.equals(start);
                        boolean onTimeOut = timeOutParsed.isAfter(end) || timeOutParsed.equals(end);

                        if (onTimeIn && onTimeOut) return "On Time";
                        if (!onTimeIn && onTimeOut) return "Late";
                        if (onTimeIn && !onTimeOut) return "Undertime";
                        return "Late and Undertime";
                    } else { return workday; }
                }

                if (timeIn != null && timeOut != null) {
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a");
                    java.time.LocalTime timeInParsed = java.time.LocalTime.parse(timeIn, formatter);
                    java.time.LocalTime timeOutParsed = java.time.LocalTime.parse(timeOut, formatter);
                    java.time.LocalTime start = java.time.LocalTime.of(8, 0);
                    java.time.LocalTime end = java.time.LocalTime.of(17, 0);

                    boolean onTimeIn = timeInParsed.isBefore(start) || timeInParsed.equals(start);
                    boolean onTimeOut = timeOutParsed.isAfter(end) || timeOutParsed.equals(end);

                    if (onTimeIn && onTimeOut) return "On Time";
                    if (!onTimeIn && onTimeOut) return "Late";
                    if (onTimeIn && !onTimeOut) return "Undertime";
                    return "Late and Undertime";
                }
                return "Absent";
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "Absent";
    }

    private void applyRowColors(JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) table.getValueAt(row, 5);

                if (!isSelected) {
                    if ("On Time".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(200, 255, 200)); 
                    } else if ("Late".equalsIgnoreCase(status) || "Undertime".equalsIgnoreCase(status) || "Late and Undertime".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(200, 220, 255)); 
                    } else if ("Leave".equalsIgnoreCase(status)) {
                        c.setBackground(Color.YELLOW); 
                    } else if ("Absent".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(255, 200, 200)); 
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });
    }

    private void loadTable() {
        model.setRowCount(0);
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate startOfMonth = now.withDayOfMonth(1);
        String startDate = startOfMonth.toString();
        String endDate = now.toString();

        try (Connection con = DriverManager.getConnection("jdbc:sqlite:employees.db")) {
            PreparedStatement ps = con.prepareStatement(
                "SELECT date, time_in, time_out, workday, second_time_out, status FROM attendance_records WHERE empNo=? AND date BETWEEN ? AND ? ORDER BY date ASC"
            );
            ps.setString(1, empNo);
            ps.setString(2, startDate);
            ps.setString(3, endDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("date"),
                        rs.getString("time_in"),
                        rs.getString("time_out"),
                        rs.getString("workday"),
                        rs.getString("second_time_out"),
                        rs.getString("status")
                    });
                }
            }
            
            Set<String> existingDates = new HashSet<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                existingDates.add((String) model.getValueAt(i, 0));
            }

            for (java.time.LocalDate date = startOfMonth; !date.isAfter(now); date = date.plusDays(1)) {
                if (isWeekday(date)) {
                    String dateStr = date.toString();
                    if (!existingDates.contains(dateStr)) {
                        try (PreparedStatement insertPs = con.prepareStatement(
                                "INSERT INTO attendance_records(empNo, date, workday, status) VALUES (?, ?, ?, ?)"
                        )) {
                            insertPs.setString(1, empNo);
                            insertPs.setString(2, dateStr);
                            insertPs.setString(3, "regular");
                            insertPs.setString(4, "Absent");
                            insertPs.executeUpdate();
                        }
                        model.addRow(new Object[]{dateStr, null, null, "regular", null, "Absent"});
                    }
                }
            }

            for (int i = 0; i < model.getRowCount(); i++) {
                String date = (String) model.getValueAt(i, 0);
                String status = (String) model.getValueAt(i, 5);
                if (status == null || status.trim().isEmpty()) {
                    String newStatus = calculateStatus(date);
                    try (PreparedStatement updatePs = con.prepareStatement(
                            "UPDATE attendance_records SET status = ? WHERE empNo = ? AND date = ?"
                    )) {
                        updatePs.setString(1, newStatus);
                        updatePs.setString(2, empNo);
                        updatePs.setString(3, date);
                        updatePs.executeUpdate();
                    }
                    model.setValueAt(newStatus, i, 5);
                }
            }
            applyRowColors(table);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private boolean isWeekday(java.time.LocalDate date) {
        java.time.DayOfWeek day = date.getDayOfWeek();
        return day != java.time.DayOfWeek.SATURDAY && day != java.time.DayOfWeek.SUNDAY;
    }
}