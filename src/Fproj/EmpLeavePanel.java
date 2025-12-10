package Fproj;

import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import com.toedter.calendar.JDateChooser;

public class EmpLeavePanel extends JPanel {

    private String empNo, empName, empPosition;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lblVacation, lblSick, lblEmergency, lblSpecial;

    // --- UI CONSTANTS ---
    private final Color BRAND_COLOR = new Color(22, 102, 87);
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font CELL_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    public EmpLeavePanel(String empNo) {
        this.empNo = empNo;
        fetchEmployeeDetails();

        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // --- TOP PANEL: Title and Balances ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        // Title
        JLabel lblTitle = new JLabel("My Leave Requests", SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(50, 50, 50));
        topPanel.add(lblTitle, BorderLayout.NORTH);

        // Balances Panel
        JPanel balancesPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        balancesPanel.setBackground(Color.WHITE);
        balancesPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(10, 15, 10, 15)
        ));

        // Create balance cards
        lblVacation = createBalanceLabel("Vacation", "0");
        lblSick = createBalanceLabel("Sick", "0");
        lblEmergency = createBalanceLabel("Emergency", "0");
        lblSpecial = createBalanceLabel("Special", "0");

        balancesPanel.add(createBalanceCard(lblVacation, "Vacation"));
        balancesPanel.add(createBalanceCard(lblSick, "Sick"));
        balancesPanel.add(createBalanceCard(lblEmergency, "Emergency"));
        balancesPanel.add(createBalanceCard(lblSpecial, "Special"));
        
        topPanel.add(balancesPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: Table ---
        String[] cols = {"ID", "Type", "Start", "End", "Reason", "Status", "Edit", "Delete"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6 || column == 7;
            }
        };
        
        table = new JTable(model);
        styleTable(table);

        // Hide ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        
        // Renderers & Editors
        table.getColumn("Edit").setCellRenderer(new ActionButtonRenderer("Edit"));
        table.getColumn("Edit").setCellEditor(new EditButtonEditor(new JCheckBox()));
        table.getColumn("Edit").setMaxWidth(80);

        table.getColumn("Delete").setCellRenderer(new ActionButtonRenderer("Delete"));
        table.getColumn("Delete").setCellEditor(new DeleteButtonEditor(new JCheckBox()));
        table.getColumn("Delete").setMaxWidth(80);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(scroll, BorderLayout.CENTER);

        // --- BOTTOM: Request Button ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);

        JButton btnRequestLeave = new JButton("+ Request New Leave");
        styleButton(btnRequestLeave, BRAND_COLOR);
        btnRequestLeave.setPreferredSize(new Dimension(200, 45));
        btnRequestLeave.addActionListener(e -> openLeaveRequestDialog(null)); 

        bottomPanel.add(btnRequestLeave);
        add(bottomPanel, BorderLayout.SOUTH);

        // Load data
        loadBalances();
        loadLeaveRequests();
    }
    
    // --- UI Helpers ---
    private JLabel createBalanceLabel(String title, String val) {
        JLabel lbl = new JLabel(val, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(BRAND_COLOR);
        return lbl;
    }
    
    private JPanel createBalanceCard(JLabel valueLabel, String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(Color.GRAY);
        
        p.add(valueLabel, BorderLayout.CENTER);
        p.add(lblTitle, BorderLayout.SOUTH);
        return p;
    }

    private void styleTable(JTable t) {
        t.setRowHeight(40);
        t.setFont(CELL_FONT);
        t.setShowGrid(true);
        t.setGridColor(Color.GRAY);
        t.setSelectionBackground(new Color(230, 240, 255));
        t.setSelectionForeground(Color.BLACK);

        JTableHeader header = t.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(BRAND_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
        
        // Custom Renderer for Row Colors
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                
                if (!isSelected) {
                    String status = (String) table.getValueAt(row, 5);
                    if ("Approved".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(220, 255, 220)); 
                    } else if ("Rejected".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(255, 220, 220));
                    } else if ("Pending".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(255, 250, 200));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });
    }
    
    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        lblVacation.setText(String.valueOf(balances[0]));
        lblSick.setText(String.valueOf(balances[1]));
        lblEmergency.setText(String.valueOf(balances[2]));
        lblSpecial.setText(String.valueOf(balances[3]));
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

    // --- DIALOG ---
    private void openLeaveRequestDialog(Integer requestId) {
        JDialog dialog = new JDialog((java.awt.Frame) null, requestId == null ? "Request Leave" : "Edit Leave Request", true);
        dialog.setSize(450, 520);
        dialog.setLocationRelativeTo(this);
        
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Header Info
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblHead = new JLabel("Leave Application Form");
        lblHead.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHead.setForeground(BRAND_COLOR);
        p.add(lblHead, gbc);
        
        gbc.gridy++;
        p.add(new JSeparator(), gbc);

        // Fields
        gbc.gridy++; gbc.gridwidth = 1;
        p.add(new JLabel("Leave Type:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<String> cbLeaveType = new JComboBox<>(new String[]{"Vacation", "Sick", "Emergency", "Special"});
        cbLeaveType.setBackground(Color.WHITE);
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
        JTextArea txtReason = new JTextArea(3, 20);
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        txtReason.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        p.add(txtReason, gbc);

        // Pre-fill
        if (requestId != null) {
            try (ResultSet rs = Database.getLeaveRequestById(requestId)) {
                if (rs.next()) {
                    cbLeaveType.setSelectedItem(rs.getString("leave_type"));
                    dateChooserStart.setDate(java.sql.Date.valueOf(rs.getString("start_date")));
                    dateChooserEnd.setDate(java.sql.Date.valueOf(rs.getString("end_date")));
                    txtReason.setText(rs.getString("reason"));
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }

        // Submit Button
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 5, 5);
        
        JButton btnSubmit = new JButton(requestId == null ? "Submit Request" : "Update Request");
        styleButton(btnSubmit, BRAND_COLOR);
        btnSubmit.setPreferredSize(new Dimension(200, 40));
        
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

            if (requestId == null && startDateLocal.isBefore(today)) { // Allow edits to past dates if admin allows, but typically no
                 // Strict for new requests
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
            loadBalances(); 
        }
    }

    private long calculateLeaveDays(String startDate, String endDate) {
        try {
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
            return java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        } catch (Exception e) { return 0; }
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

    // --- BUTTON RENDERERS ---

    class ActionButtonRenderer extends JButton implements TableCellRenderer {
        public ActionButtonRenderer(String text) {
            setOpaque(true);
            setText(text);
            setFont(new Font("Segoe UI", Font.PLAIN, 11));
            // Color based on action
            if("Delete".equals(text)) {
                setBackground(new Color(255, 235, 235));
                setForeground(Color.RED);
                setBorder(BorderFactory.createLineBorder(Color.RED));
            } else {
                setBackground(new Color(235, 245, 255));
                setForeground(Color.BLUE);
                setBorder(BorderFactory.createLineBorder(Color.BLUE));
            }
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class EditButtonEditor extends DefaultCellEditor {
        private JButton button = new JButton();
        private boolean clicked;

        public EditButtonEditor(JCheckBox checkBox) {
            super(checkBox);
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
            button.setText("Edit");
            clicked = true;
            return button;
        }
        @Override
        public Object getCellEditorValue() { return "Edit"; }
    }

    class DeleteButtonEditor extends DefaultCellEditor {
        private JButton button = new JButton();
        private boolean clicked;

        public DeleteButtonEditor(JCheckBox checkBox) {
            super(checkBox);
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
            button.setText("Delete");
            clicked = true;
            return button;
        }
        @Override
        public Object getCellEditorValue() { return "Delete"; }
    }
}