package Fproj;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;

public class adminAttendance extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    public adminAttendance() {
        setLayout(null);
        setBackground(new Color(240,240,240));

        JLabel lblTitle = new JLabel("Attendance Records");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setBounds(20, 10, 200, 30);
        add(lblTitle);

        // Search field
        JLabel lblSearch = new JLabel("Search by Emp No:");
        lblSearch.setBounds(427, 50, 150, 25);
        add(lblSearch);

        txtSearch = new JTextField();
        txtSearch.setBounds(544, 50, 150, 25);
        add(txtSearch);

        JButton btnSearch = new JButton("Search");
        btnSearch.setBounds(700, 50, 100, 25);
        add(btnSearch);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBounds(20, 50, 100, 25);
        add(btnRefresh);

        // Table columns for summary view
        String[] cols = {"Emp No", "Name", "View"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;  // Only View button is editable
            }
        };
        table = new JTable(model);
        table.getColumn("View").setCellRenderer(new ButtonRenderer());
        table.getColumn("View").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(20, 90, 780, 330);
        add(scroll);

        loadSummaryTable();

        btnSearch.addActionListener(e -> searchSummary());
        btnRefresh.addActionListener(e -> loadSummaryTable());
    }

    private void loadSummaryTable() {
        model.setRowCount(0);

        String sql = "SELECT DISTINCT a.empNo, e.name FROM attendance_records a JOIN employees e ON a.empNo = e.empNo ORDER BY a.empNo";

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

        String sql = "SELECT DISTINCT a.empNo, e.name FROM attendance_records a JOIN employees e ON a.empNo = e.empNo WHERE a.empNo = ?";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, search);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("empNo"),
                        rs.getString("name"),
                        "View"
                });
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No records found for Emp No: " + search);
                loadSummaryTable();  // Reload all if none found
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Custom renderer for View button
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // Custom editor for View button
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    String empNo = (String) table.getValueAt(table.getSelectedRow(), 0);
                    viewAttendanceDetails(empNo);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // Action handled in button listener
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

    private void viewAttendanceDetails(String empNo) {
        AttendanceDetailsDialog dialog = new AttendanceDetailsDialog(empNo);
        dialog.setVisible(true);
    }

    // Custom Dialog for Attendance Details
    private class AttendanceDetailsDialog extends JDialog {
        private JTable detailsTable;
        private DefaultTableModel detailsModel;
        private JComboBox<String> cbFilter;
        private JButton btnFilter;

        public AttendanceDetailsDialog(String empNo) {
            setTitle("Attendance Records for Emp No: " + empNo);
            setModal(true);
            setSize(800, 450);  // Increased height for filter
            setLocationRelativeTo(null);
            setLayout(null);

            // Filter components
            JLabel lblFilter = new JLabel("Filter by Month Half:");
            lblFilter.setBounds(20, 20, 150, 25);
            add(lblFilter);

            cbFilter = new JComboBox<>(new String[]{"All", "First Half (1-15)", "Second Half (16-End)"});
            cbFilter.setBounds(170, 20, 200, 25);
            add(cbFilter);

            btnFilter = new JButton("Apply Filter");
            btnFilter.setBounds(380, 20, 100, 25);
            btnFilter.addActionListener(e -> loadDetailsTable(getTitle().substring(getTitle().lastIndexOf(": ") + 2)));
            add(btnFilter);

            // Table columns
            String[] cols = {"ID", "Date", "Time In", "Time Out", "Status"};
            detailsModel = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    // Make only Time In (2) and Time Out (3) editable; Status (4) is not editable
                    return column == 2 || column == 3;
                }
            };
            detailsTable = new JTable(detailsModel);

            JScrollPane scroll = new JScrollPane(detailsTable);
            scroll.setBounds(20, 60, 740, 250);  // Adjusted y position
            add(scroll);

            JButton btnSave = new JButton("Save Changes");
            btnSave.setBounds(20, 330, 120, 30);  // Adjusted y position
            btnSave.setBackground(new Color(34, 139, 34));  // Green for save
            btnSave.setForeground(Color.WHITE);
            btnSave.addActionListener(e -> saveDetailsChanges());
            add(btnSave);

            JButton btnClose = new JButton("Close");
            btnClose.setBounds(160, 330, 100, 30);  // Adjusted y position
            btnClose.addActionListener(e -> dispose());
            add(btnClose);

            loadDetailsTable(empNo);
        }

        private void loadDetailsTable(String empNo) {
            detailsModel.setRowCount(0);

            String filter = (String) cbFilter.getSelectedItem();
            String sql = "SELECT id, date, time_in, time_out, status FROM attendance_records WHERE empNo = ? ";
            if ("First Half (1-15)".equals(filter)) {
                sql += "AND CAST(SUBSTR(date, 9, 2) AS INTEGER) BETWEEN 1 AND 15 ";
            } else if ("Second Half (16-End)".equals(filter)) {
                sql += "AND CAST(SUBSTR(date, 9, 2) AS INTEGER) >= 16 ";
            }
            sql += "ORDER BY date ASC";  // Sort by date descending

            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, empNo);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    detailsModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("date"),
                            rs.getString("time_in"),
                            rs.getString("time_out"),
                            rs.getString("status")
                    });
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Update statuses for existing records if null
            for (int i = 0; i < detailsModel.getRowCount(); i++) {
                String date = (String) detailsModel.getValueAt(i, 1);
                String timeIn = (String) detailsModel.getValueAt(i, 2);
                String timeOut = (String) detailsModel.getValueAt(i, 3);
                String status = (String) detailsModel.getValueAt(i, 4);
                if (status == null || status.trim().isEmpty()) {
                    String newStatus = calculateStatus(timeIn, timeOut, date);
                    detailsModel.setValueAt(newStatus, i, 4);
                    // Update in database
                    try (Connection conn = Database.connect();
                         PreparedStatement updatePs = conn.prepareStatement(
                             "UPDATE attendance_records SET status = ? WHERE empNo = ? AND date = ?"
                         )) {
                        updatePs.setString(1, newStatus);
                        updatePs.setString(2, empNo);
                        updatePs.setString(3, date);
                        updatePs.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Apply row colors after loading
            applyRowColors(detailsTable);
        }

        private void saveDetailsChanges() {
            boolean hasChanges = false;
            String updateSql = "UPDATE attendance_records SET time_in = ?, time_out = ? WHERE id = ?";

            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

                for (int i = 0; i < detailsModel.getRowCount(); i++) {
                    int id = (Integer) detailsModel.getValueAt(i, 0);
                    String timeIn = (String) detailsModel.getValueAt(i, 2);
                    String timeOut = (String) detailsModel.getValueAt(i, 3);

                    // Basic validation: Ensure times are not empty and in expected format
                    if ((timeIn != null && !timeIn.trim().isEmpty()) || (timeOut != null && !timeOut.trim().isEmpty())) {
                        // Validate format (optional, but recommended)
                        try {
                            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a");
                            if (timeIn != null && !timeIn.trim().isEmpty()) {
                                java.time.LocalTime.parse(timeIn, formatter);
                            }
                            if (timeOut != null && !timeOut.trim().isEmpty()) {
                                java.time.LocalTime.parse(timeOut, formatter);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Invalid time format in row " + (i + 1) + ". Use 'hh:mm:ss AM/PM'. Skipping save.");
                            continue;
                        }

                        // Set parameters and execute update
                        pstmt.setString(1, timeIn);
                        pstmt.setString(2, timeOut);
                        pstmt.setInt(3, id);
                        pstmt.executeUpdate();
                        hasChanges = true;

                        // Recalculate status after updating times
                        String date = (String) detailsModel.getValueAt(i, 1);
                        String newStatus = calculateStatus(timeIn, timeOut, date);
                        detailsModel.setValueAt(newStatus, i, 4);
                        // Update status in database
                        PreparedStatement statusPs = conn.prepareStatement(
                            "UPDATE attendance_records SET status = ? WHERE id = ?"
                        );
                        statusPs.setString(1, newStatus);
                        statusPs.setInt(2, id);
                        statusPs.executeUpdate();
                        statusPs.close();
                    }
                }

                if (hasChanges) {
                    JOptionPane.showMessageDialog(this, "Changes saved successfully!");
                    // Reload the details table to reflect updates
                    String empNo = getTitle().substring(getTitle().lastIndexOf(": ") + 2);
                    loadDetailsTable(empNo);
                } else {
                    JOptionPane.showMessageDialog(this, "No changes to save.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving changes: " + e.getMessage());
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
    }

    private void applyRowColors(JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
                );

                String status = (String) table.getValueAt(row, 4);  // status column (index 4 in details table)

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
}