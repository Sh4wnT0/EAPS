package Fproj;

import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import com.toedter.calendar.JDateChooser;

public class EmpLeavePanel extends JPanel {

    private String empNo, empName, empPosition;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lblVacation, lblSick, lblEmergency, lblSpecial;

    public EmpLeavePanel(String empNo) {
        this.empNo = empNo;
        fetchEmployeeDetails();

        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20)); // Add padding around the whole panel

        // --- TOP PANEL: Title and Balances ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);

        // Title
        JLabel lblTitle = new JLabel("My Leave Requests", SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        topPanel.add(lblTitle, BorderLayout.NORTH);

        // Balances Panel (Styled with a border)
        JPanel balancesPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        balancesPanel.setBackground(Color.WHITE);
        balancesPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Leave Balances (Remaining)", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.PLAIN, 12)));

        lblVacation = createBalanceLabel("Vacation: --");
        lblSick = createBalanceLabel("Sick: --");
        lblEmergency = createBalanceLabel("Emergency: --");
        lblSpecial = createBalanceLabel("Special: --");

        balancesPanel.add(lblVacation);
        balancesPanel.add(lblSick);
        balancesPanel.add(lblEmergency);
        balancesPanel.add(lblSpecial);
        
        // Wrapper to keep height reasonable
        JPanel balanceWrapper = new JPanel(new BorderLayout());
        balanceWrapper.setBackground(Color.WHITE);
        balanceWrapper.add(balancesPanel, BorderLayout.CENTER);
        balanceWrapper.add(Box.createVerticalStrut(10), BorderLayout.SOUTH); // Spacing below balances
        
        topPanel.add(balanceWrapper, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: Table ---
        model = new DefaultTableModel(new String[]{"ID", "Type", "Start", "End", "Reason", "Status", "Edit", "Delete"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6 || column == 7; // Only buttons are editable
            }
        };
        
        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                String status = (String) getValueAt(row, 5);
                if (!isRowSelected(row)) {
                    if ("Approved".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(200, 255, 200)); // Softer Green
                        c.setForeground(Color.BLACK);
                    } else if ("Rejected".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(255, 200, 200)); // Softer Red
                        c.setForeground(Color.BLACK);
                    } else if ("Pending".equalsIgnoreCase(status)) {
                        c.setBackground(Color.YELLOW); // Softer Yellow
                        c.setForeground(Color.BLACK);
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                }
                return c;
            }
        };

        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Hide ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        // Button renderers and editors
        table.getColumn("Edit").setCellRenderer(new ButtonRenderer());
        table.getColumn("Edit").setCellEditor(new EditButtonEditor(new JCheckBox()));
        table.getColumn("Delete").setCellRenderer(new ButtonRenderer());
        table.getColumn("Delete").setCellEditor(new DeleteButtonEditor(new JCheckBox()));

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // --- BOTTOM: Request Button ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);

        JButton btnRequestLeave = new JButton("Request New Leave");
        btnRequestLeave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRequestLeave.setBackground(new Color(34, 139, 34));
        btnRequestLeave.setForeground(Color.WHITE);
        btnRequestLeave.setPreferredSize(new Dimension(180, 40));
        btnRequestLeave.setFocusPainted(false);
        btnRequestLeave.addActionListener(e -> openLeaveRequestDialog(null)); 

        bottomPanel.add(btnRequestLeave);
        add(bottomPanel, BorderLayout.SOUTH);

        // Load data
        loadBalances();
        loadLeaveRequests();
    }
    
    private JLabel createBalanceLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(50, 50, 50));
        return lbl;
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
        lblVacation.setText("Vacation: " + balances[0]);
        lblSick.setText("Sick: " + balances[1]);
        lblEmergency.setText("Emergency: " + balances[2]);
        lblSpecial.setText("Special: " + balances[3]);
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
                        rs.getString("status"),
                        "Edit", 
                        "Delete" 
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- REFACTORED DIALOG USING GRIDBAGLAYOUT ---
    private void openLeaveRequestDialog(Integer requestId) {
        JDialog dialog = new JDialog((java.awt.Frame) null, requestId == null ? "Request Leave" : "Edit Leave Request", true);
        dialog.setSize(450, 500); // Adjusted size
        dialog.setLocationRelativeTo(this);
        
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // --- Read Only Info ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblHeader = new JLabel("Employee Details");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        p.add(lblHeader, gbc);
        
        gbc.gridy++; gbc.gridwidth = 1;
        p.add(new JLabel("Name: " + empName), gbc);
        
        gbc.gridx = 1;
        p.add(new JLabel("Position: " + empPosition), gbc);
        
        // Separator
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        p.add(new JSeparator(), gbc);

        // --- Form Fields ---
        gbc.gridy++; gbc.gridwidth = 1;
        p.add(new JLabel("Leave Type:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<String> cbLeaveType = new JComboBox<>(new String[]{"Vacation", "Sick", "Emergency", "Special"});
        p.add(cbLeaveType, gbc);

        gbc.gridx = 0; gbc.gridy++;
        p.add(new JLabel("Start Date:"), gbc);
        
        gbc.gridx = 1;
        JDateChooser dateChooserStart = new JDateChooser();
        dateChooserStart.setDateFormatString("yyyy-MM-dd");
        p.add(dateChooserStart, gbc);

        gbc.gridx = 0; gbc.gridy++;
        p.add(new JLabel("End Date:"), gbc);
        
        gbc.gridx = 1;
        JDateChooser dateChooserEnd = new JDateChooser();
        dateChooserEnd.setDateFormatString("yyyy-MM-dd");
        p.add(dateChooserEnd, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        p.add(new JLabel("Reason:"), gbc);
        
        gbc.gridx = 1;
        JTextArea txtReason = new JTextArea(4, 20);
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        JScrollPane reasonScroll = new JScrollPane(txtReason);
        p.add(reasonScroll, gbc);

        // Pre-fill if editing
        if (requestId != null) {
            try (ResultSet rs = Database.getLeaveRequestById(requestId)) {
                if (rs.next()) {
                    cbLeaveType.setSelectedItem(rs.getString("leave_type"));
                    dateChooserStart.setDate(java.sql.Date.valueOf(rs.getString("start_date")));
                    dateChooserEnd.setDate(java.sql.Date.valueOf(rs.getString("end_date")));
                    txtReason.setText(rs.getString("reason"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // --- Submit Button ---
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 5, 5); // Extra top padding
        
        JButton btnSubmit = new JButton(requestId == null ? "Submit Request" : "Update Request");
        btnSubmit.setPreferredSize(new Dimension(200, 35));
        btnSubmit.setBackground(new Color(34, 139, 34));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFocusPainted(false);
        
        btnSubmit.addActionListener(e -> {
            String leaveType = (String) cbLeaveType.getSelectedItem();
            Date startDateObj = dateChooserStart.getDate();
            Date endDateObj = dateChooserEnd.getDate();
            String reason = txtReason.getText().trim();

            if (startDateObj == null || endDateObj == null || reason.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields!");
                return;
            }

            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate startDateLocal = startDateObj.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

            if (startDateLocal.isBefore(today)) {
                JOptionPane.showMessageDialog(dialog, "Start date cannot be earlier than today!");
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String startDate = sdf.format(startDateObj);
            String endDate = sdf.format(endDateObj);

            if (endDateObj.before(startDateObj)) {
                JOptionPane.showMessageDialog(dialog, "End date must be after start date!");
                return;
            }

            if (requestId == null) {
                int[] balances = Database.getLeaveBalances(empNo);
                int balanceIndex = getBalanceIndex(leaveType);
                if (balanceIndex != -1) {
                    long days = calculateLeaveDays(startDate, endDate);
                    int currentBalance = balances[balanceIndex];
                    if (currentBalance < days) {
                        JOptionPane.showMessageDialog(dialog, "Insufficient leave balance! Current: " + currentBalance + ", Required: " + days);
                        return;
                    }
                }
                Database.insertLeaveRequest(empNo, empName, empPosition, leaveType, startDate, endDate, reason);
                JOptionPane.showMessageDialog(dialog, "Leave request submitted!");
            } else {
                Database.updateLeaveRequest(requestId, leaveType, startDate, endDate, reason);
                JOptionPane.showMessageDialog(dialog, "Leave request updated!");
            }

            dialog.dispose();
            loadLeaveRequests();
            loadBalances();
        });
        
        p.add(btnSubmit, gbc);
        dialog.add(p);
        dialog.setVisible(true);
    }

    private void deleteLeaveRequest(int requestId) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this leave request?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Database.deleteLeaveRequest(requestId);
            JOptionPane.showMessageDialog(this, "Leave request deleted!");
            loadLeaveRequests();
            loadBalances(); // Update balances in case a pending request held funds (optional based on logic)
        }
    }

    private long calculateLeaveDays(String startDate, String endDate) {
        try {
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
            return java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        } catch (Exception e) {
            return 0;
        }
    }

    private int getBalanceIndex(String leaveType) {
        switch (leaveType.toLowerCase()) {
            case "vacation": return 0;
            case "sick": return 1;
            case "emergency": return 2;
            case "special": return 3;
            default: return -1;
        }
    }

    // --- RENDERERS ---

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

    class EditButtonEditor extends DefaultCellEditor {
        private JButton button = new JButton();
        private String label;
        private boolean clicked;

        public EditButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(true);
            button.addActionListener(e -> {
                if (clicked) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        int id = (int) table.getValueAt(row, 0);
                        String status = (String) table.getValueAt(row, 5);
                        if ("Pending".equalsIgnoreCase(status)) {
                            openLeaveRequestDialog(id);
                        } else {
                            JOptionPane.showMessageDialog(EmpLeavePanel.this, "Only pending requests can be edited.");
                        }
                    }
                }
                clicked = false;
                fireEditingStopped();
            });
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            clicked = true;
            return button;
        }
        @Override
        public Object getCellEditorValue() { return label; }
        @Override
        public boolean stopCellEditing() { clicked = false; return super.stopCellEditing(); }
    }

    class DeleteButtonEditor extends DefaultCellEditor {
        private JButton button = new JButton();
        private String label;
        private boolean clicked;

        public DeleteButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(true);
            button.addActionListener(e -> {
                if (clicked) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        int id = (int) table.getValueAt(row, 0);
                        deleteLeaveRequest(id);
                    }
                }
                clicked = false;
                fireEditingStopped();
            });
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            clicked = true;
            return button;
        }
        @Override
        public Object getCellEditorValue() { return label; }
        @Override
        public boolean stopCellEditing() { clicked = false; return super.stopCellEditing(); }
    }
}