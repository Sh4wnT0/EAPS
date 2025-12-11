package Fproj;

import com.toedter.calendar.JDateChooser; // Requires jcalendar-1.4.jar
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

public class EmpAttendancePanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private String empNo;

    // --- UI Constants ---
    private final Color BRAND_COLOR = new Color(22, 102, 87);
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font CELL_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    public EmpAttendancePanel(String empNo) {
        this.empNo = empNo;

        // --- UI SETUP ---
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 245, 245)); // Light Gray Background
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // --- TOP PANEL: Title and Buttons ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Title
        JLabel lblTitle = new JLabel("Daily Attendance Record");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(50, 50, 50));
        topPanel.add(lblTitle, BorderLayout.WEST);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnPanel.setOpaque(false);

        JButton btnACR = new JButton("Request Correction (ACR)");
        styleButton(btnACR, new Color(70, 130, 180)); // Steel Blue
        btnACR.setPreferredSize(new Dimension(200, 40));
        btnACR.addActionListener(e -> showACRDialog());

        JButton btnIn = new JButton("TIME IN");
        styleButton(btnIn, BRAND_COLOR); // Brand Green
        btnIn.setPreferredSize(new Dimension(120, 40));
        btnIn.addActionListener(e -> saveRecord("IN"));

        JButton btnOut = new JButton("TIME OUT");
        styleButton(btnOut, new Color(220, 53, 69)); // Red
        btnOut.setPreferredSize(new Dimension(120, 40));
        btnOut.addActionListener(e -> saveRecord("OUT"));

        btnPanel.add(btnACR);
        btnPanel.add(btnIn);
        btnPanel.add(btnOut);
        topPanel.add(btnPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: TABLE ---
        String[] cols = {"Date", "Time-In", "Time-Out", "Workday", "OT Time-Out", "Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Logic preserved from original
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
        styleTable(table); // Apply modern styling

        // Custom Editors (Preserved)
        table.getColumnModel().getColumn(1).setCellEditor(new TimeInEditor());
        table.getColumnModel().getColumn(2).setCellEditor(new TimeOutEditor());

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(scroll, BorderLayout.CENTER);

        loadTable();
    }

    // --- UI HELPER: Table Styling ---
    private void styleTable(JTable t) {
        t.setRowHeight(40);
        t.setFont(CELL_FONT);
        
        // VISIBLE GRID LINES
        t.setShowGrid(true);
        t.setGridColor(Color.GRAY);
        t.setIntercellSpacing(new Dimension(1, 1));
        
        t.setSelectionBackground(new Color(230, 240, 255));
        t.setSelectionForeground(Color.BLACK);

        // Header
        JTableHeader header = t.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(BRAND_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));

        // Custom Renderer for Row Colors and Center Alignment
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);

                if (!isSelected) {
                    String status = (String) table.getValueAt(row, 5); // Status Col
                    if ("On Time".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(220, 255, 220)); // Light Green
                    } else if (status != null && (status.contains("Late") || status.contains("Undertime"))) {
                        c.setBackground(new Color(255, 240, 200)); // Light Orange
                    } else if ("Leave".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(255, 255, 200)); // Yellow
                    } else if ("Working".equalsIgnoreCase(status) || "Working (OT)".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(200, 240, 255)); // Light Blue
                    } else if ("Absent".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(255, 220, 220)); // Light Red
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });
    }

    // --- UI HELPER: Button Styling ---
    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // =========================================================================
    //                      ACR DIALOG LOGIC
    // =========================================================================

    private void showACRDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Attendance Correction Request", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(650, 450);
        dialog.setLocationRelativeTo(this);

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        header.setBackground(Color.WHITE);
        JLabel lblHead = new JLabel("Submit correction requests for incorrect time logs.");
        lblHead.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        header.add(lblHead);
        dialog.add(header, BorderLayout.NORTH);

        // Container for dynamic rows
        JPanel rowsPanel = new JPanel();
        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));
        rowsPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(rowsPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Add the first row by default
        addACRRow(rowsPanel);

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        bottomPanel.setBackground(new Color(245, 245, 245));
        
        JButton btnAdd = new JButton("+ Add Row");
        styleButton(btnAdd, Color.GRAY);
        btnAdd.addActionListener(e -> addACRRow(rowsPanel));

        JButton btnSubmit = new JButton("Submit Requests");
        styleButton(btnSubmit, BRAND_COLOR);
        btnSubmit.setPreferredSize(new Dimension(150, 35));
        btnSubmit.addActionListener(e -> submitACRRequests(dialog, rowsPanel));

        bottomPanel.add(btnAdd);
        bottomPanel.add(btnSubmit);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addACRRow(JPanel container) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        row.setMaximumSize(new Dimension(2000, 60)); // Limit height

        // 1. Time Spinners
        JSpinner timeInSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeInEditor = new JSpinner.DateEditor(timeInSpinner, "hh:mm:ss a");
        timeInSpinner.setEditor(timeInEditor);
        timeInSpinner.setValue(new Date()); 
        styleSpinner(timeInSpinner);

        JSpinner timeOutSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeOutEditor = new JSpinner.DateEditor(timeOutSpinner, "hh:mm:ss a");
        timeOutSpinner.setEditor(timeOutEditor);
        timeOutSpinner.setValue(new Date());
        styleSpinner(timeOutSpinner);

        // 2. JCalendar
        JLabel lblDate = new JLabel("Date:");
        lblDate.setFont(CELL_FONT);
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setPreferredSize(new Dimension(130, 30));

        // Auto-Check Listener
        dateChooser.addPropertyChangeListener("date", evt -> {
            if (evt.getNewValue() instanceof Date) {
                fetchAndFillTime((Date) evt.getNewValue(), timeInSpinner, timeOutSpinner);
            }
        });

        // 3. Labels
        JLabel lblIn = new JLabel("In:");
        lblIn.setFont(CELL_FONT);
        JLabel lblOut = new JLabel("Out:");
        lblOut.setFont(CELL_FONT);

        // 4. Remove Button
        JButton btnRemove = new JButton("X");
        btnRemove.setFont(new Font("Arial", Font.BOLD, 12));
        btnRemove.setForeground(new Color(220, 53, 69));
        btnRemove.setBackground(Color.WHITE);
        btnRemove.setBorder(BorderFactory.createLineBorder(new Color(220, 53, 69)));
        btnRemove.setPreferredSize(new Dimension(30, 30));
        btnRemove.setFocusPainted(false);
        btnRemove.addActionListener(e -> {
            container.remove(row);
            container.revalidate();
            container.repaint();
        });

        // Add components
        row.add(lblDate);
        row.add(dateChooser);
        row.add(Box.createHorizontalStrut(10));
        row.add(lblIn);
        row.add(timeInSpinner);
        row.add(Box.createHorizontalStrut(5));
        row.add(lblOut);
        row.add(timeOutSpinner);
        row.add(Box.createHorizontalStrut(10));
        row.add(btnRemove);

        // Add hidden property
        row.putClientProperty("isRow", true);

        container.add(row);
        container.revalidate();
        container.repaint();
    }
    
    private void styleSpinner(JSpinner spinner) {
        spinner.setPreferredSize(new Dimension(110, 30));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor)editor).getTextField().setFont(CELL_FONT);
        }
    }

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

                if (dbTimeIn != null && !dbTimeIn.isEmpty() && !"00:00:00 AM".equals(dbTimeIn)) {
                    try { inSpinner.setValue(timeFormat.parse(dbTimeIn)); } catch (Exception ignored) {}
                }
                if (dbTimeOut != null && !dbTimeOut.isEmpty()) {
                    try { outSpinner.setValue(timeFormat.parse(dbTimeOut)); } catch (Exception ignored) {}
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

        for (Component comp : rowsPanel.getComponents()) {
            if (comp instanceof JPanel && ((JPanel)comp).getClientProperty("isRow") != null) {
                JPanel row = (JPanel) comp;
                JDateChooser dc = (JDateChooser) row.getComponent(1); // Index based on add order
                JSpinner spinIn = (JSpinner) row.getComponent(4);
                JSpinner spinOut = (JSpinner) row.getComponent(7);

                Date selectedDate = dc.getDate();
                if (selectedDate == null) {
                    JOptionPane.showMessageDialog(dialog, "Please select a date for all rows.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    hasError = true;
                    break;
                }
                requests.add(new String[]{
                    dateFormat.format(selectedDate),
                    timeFormat.format(spinIn.getValue()),
                    timeFormat.format(spinOut.getValue())
                });
            }
        }

        if (hasError) return;
        if (requests.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "No requests to submit.");
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:sqlite:employees.db")) {
            con.setAutoCommit(false);
            String sql = "INSERT INTO attendance_requests (empNo, request_date, time_in, time_out, status) VALUES (?, ?, ?, ?, 'Pending')";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (String[] req : requests) {
                    ps.setString(1, empNo);
                    ps.setString(2, req[0]);
                    ps.setString(3, req[1]);
                    ps.setString(4, req[2]);
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
    //                  EXISTING LOGIC & EDITORS (Preserved)
    // =========================================================================

    class TimeInEditor extends javax.swing.AbstractCellEditor implements TableCellEditor {
        private JTextField textField = new JTextField();
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            textField.setText(value != null ? value.toString() : "");
            textField.setFont(CELL_FONT);
            return textField;
        }
        @Override
        public Object getCellEditorValue() {
            String newValue = textField.getText();
            String date = (String) table.getValueAt(table.getSelectedRow(), 0);
            int totalUpdateCount = getTotalUpdateCount(date);
            if (totalUpdateCount >= 2) {
                JOptionPane.showMessageDialog(null, "Update limit reached for this date.");
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
            textField.setFont(CELL_FONT);
            return textField;
        }
        @Override
        public Object getCellEditorValue() {
            String newValue = textField.getText();
            String date = (String) table.getValueAt(table.getSelectedRow(), 0);
            int totalUpdateCount = getTotalUpdateCount(date);
            if (totalUpdateCount >= 2) {
                JOptionPane.showMessageDialog(null, "Update limit reached for this date.");
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

            // 1. Fetch current row details
            String currentWorkday = "regular"; // Default
            int totalUpdateCount = 0;
            String existingTimeIn = null;
            String existingTimeOut = null;
            String existingSecondTimeOut = null;
            boolean rowExists = false;

            String fetchSql = "SELECT workday, total_update_count, time_in, time_out, second_time_out FROM attendance_records WHERE empNo = ? AND date = ?";
            try (PreparedStatement ps = con.prepareStatement(fetchSql)) {
                ps.setString(1, empNo);
                ps.setString(2, today);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    rowExists = true;
                    currentWorkday = rs.getString("workday");
                    totalUpdateCount = rs.getInt("total_update_count");
                    existingTimeIn = rs.getString("time_in");
                    existingTimeOut = rs.getString("time_out");
                    existingSecondTimeOut = rs.getString("second_time_out");
                }
            }

            // 2. Define Update Limits based on Workday
            boolean isOTDay = "OT".equals(currentWorkday) || "SH".equals(currentWorkday) || "RH".equals(currentWorkday);
            int maxUpdates = isOTDay ? 4 : 2; // Allow 4 updates for OT (In, Out, OT-Out, Correction), 2 for Regular

            if (totalUpdateCount >= maxUpdates) {
                JOptionPane.showMessageDialog(null, "Update limit reached for today.");
                return;
            }

            // 3. Process Logic
            if (type.equals("IN")) {
                if (rowExists) {
                    // If row exists, ensure we aren't overwriting an existing Time In
                    if (existingTimeIn == null || existingTimeIn.isEmpty() || "00:00:00 AM".equals(existingTimeIn)) {
                        updateDB("UPDATE attendance_records SET time_in = ?, total_update_count = total_update_count + 1 WHERE empNo = ? AND date = ?", now, today);
                    } else {
                        JOptionPane.showMessageDialog(null, "Time-in already recorded!");
                        return;
                    }
                } else {
                    // Insert new record
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO attendance_records(empNo, date, time_in, workday, total_update_count, status) VALUES (?, ?, ?, ?, 1, 'Working')")) {
                        ps.setString(1, empNo);
                        ps.setString(2, today);
                        ps.setString(3, now);
                        ps.setString(4, "regular"); // Default to regular unless admin set otherwise
                        ps.executeUpdate();
                    }
                }
            } else { // OUT
                if (!rowExists) {
                    JOptionPane.showMessageDialog(null, "You must Time In first!");
                    return;
                }
                
                // Critical Check: Cannot Time Out if Time In is empty
                if (existingTimeIn == null || "00:00:00 AM".equals(existingTimeIn)) {
                     JOptionPane.showMessageDialog(null, "You must Time In before you can Time Out!");
                     return;
                }

                if (existingTimeOut == null || existingTimeOut.isEmpty()) {
                    // First Time Out (Regular Shift End)
                    updateDB("UPDATE attendance_records SET time_out = ?, total_update_count = total_update_count + 1 WHERE empNo = ? AND date = ?", now, today);
                } else if (isOTDay && (existingSecondTimeOut == null || existingSecondTimeOut.isEmpty())) {
                    // Second Time Out (OT Shift End)
                    updateDB("UPDATE attendance_records SET second_time_out = ?, total_update_count = total_update_count + 1 WHERE empNo = ? AND date = ?", now, today);
                } else {
                    JOptionPane.showMessageDialog(null, "Time-out already recorded!");
                    return;
                }
            }

            // 4. Update Status
            String newStatus = calculateStatus(today);
            updateDB("UPDATE attendance_records SET status = ? WHERE empNo = ? AND date = ?", newStatus, today);

            loadTable();
            JOptionPane.showMessageDialog(null, "Record Saved.");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    // Helper to reduce code duplication
    private void updateDB(String sql, String val1, String date) throws SQLException {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:employees.db");
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, val1);
            ps.setString(2, empNo);
            ps.setString(3, date);
            ps.executeUpdate();
        }
    }

    private String calculateStatus(String dateStr) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:employees.db");
             PreparedStatement ps = con.prepareStatement("SELECT time_in, time_out, second_time_out, workday FROM attendance_records WHERE empNo = ? AND date = ?")) {
            
            ps.setString(1, empNo);
            ps.setString(2, dateStr);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String timeIn = rs.getString("time_in");
                String timeOut = rs.getString("time_out");
                String secondTimeOut = rs.getString("second_time_out");
                String workday = rs.getString("workday");

                // 1. Check if currently working (In but not Out)
                boolean hasIn = timeIn != null && !timeIn.isEmpty() && !"00:00:00 AM".equals(timeIn);
                boolean hasOut = timeOut != null && !timeOut.isEmpty();
                
                if (hasIn && !hasOut) {
                    return "Working"; // New Status
                }

                // 2. OT Logic
                if ("OT".equals(workday)) {
                    if (hasIn && hasOut && secondTimeOut != null && !secondTimeOut.isEmpty()) {
                        return "On Time"; 
                    } else if (hasIn && hasOut) {
                        return "Working (OT)"; // Finished regular shift, currently in OT
                    }
                    return "OT"; // Pending
                }
                
                // 3. Regular Logic (Only runs if both In and Out are present)
                if (hasIn && hasOut) {
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a");
                    java.time.LocalTime timeInParsed = java.time.LocalTime.parse(timeIn, formatter);
                    java.time.LocalTime timeOutParsed = java.time.LocalTime.parse(timeOut, formatter);
                    java.time.LocalTime start = java.time.LocalTime.of(8, 0); // 8:00 AM
                    java.time.LocalTime end = java.time.LocalTime.of(17, 0);  // 5:00 PM

                    // Grace period logic can be added here (e.g., plusMinutes(5))
                    boolean onTimeIn = !timeInParsed.isAfter(start); // Before or at 8:00
                    boolean onTimeOut = !timeOutParsed.isBefore(end); // After or at 17:00

                    if (onTimeIn && onTimeOut) return "On Time";
                    if (!onTimeIn && onTimeOut) return "Late";
                    if (onTimeIn && !onTimeOut) return "Undertime";
                    return "Late and Undertime";
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        
        // Default to Absent only if we are sure no logic above matched
        return "Absent";
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
            
            // Insert missing dates logic preserved...
            Set<String> existingDates = new HashSet<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                existingDates.add((String) model.getValueAt(i, 0));
            }

            for (java.time.LocalDate date = startOfMonth; !date.isAfter(now); date = date.plusDays(1)) {
                if (isWeekday(date)) {
                    String dateStr = date.toString();
                    if (!existingDates.contains(dateStr)) {
                        // Insert absent record
                        try(PreparedStatement insert = con.prepareStatement("INSERT INTO attendance_records(empNo, date, workday, status) VALUES (?, ?, ?, ?)")) {
                            insert.setString(1, empNo);
                            insert.setString(2, dateStr);
                            insert.setString(3, "regular");
                            insert.setString(4, "Absent");
                            insert.executeUpdate();
                        }
                        model.addRow(new Object[]{dateStr, null, null, "regular", null, "Absent"});
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private boolean isWeekday(java.time.LocalDate date) {
        java.time.DayOfWeek day = date.getDayOfWeek();
        return day != java.time.DayOfWeek.SATURDAY && day != java.time.DayOfWeek.SUNDAY;
    }
}