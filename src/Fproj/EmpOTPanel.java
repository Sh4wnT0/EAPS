package Fproj;

import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

public class EmpOTPanel extends JPanel {

    private String empNo, empName = "", empPosition = "";
    
    // Main UI Components
    private JTable table;
    private DefaultTableModel model;
    
    // UI Constants
    private final Color BRAND_COLOR = new Color(22, 102, 87);
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font CELL_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    public EmpOTPanel(String empNo) {
        this.empNo = empNo;
        fetchEmployeeDetails();

        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // --- TOP: Title ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Overtime & Holiday Requests");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(50, 50, 50));
        topPanel.add(lblTitle, BorderLayout.WEST);
        
        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: History Table ---
        // Added "Edit" and "Delete" columns
        String[] cols = {"ID", "Type", "Details", "Start", "End", "Reason", "Status", "Edit", "Delete"};
        
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { 
                // Only allow clicking the buttons (columns 7 and 8)
                return column == 7 || column == 8; 
            }
        };
        
        table = new JTable(model);
        styleTable(table);

        // Hide ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        // Add Button Renderers/Editors
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

        // --- BOTTOM: Buttons ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);

        JButton btnRefresh = new JButton("Refresh");
        styleButton(btnRefresh, new Color(70, 130, 180)); // Steel Blue
        btnRefresh.setPreferredSize(new Dimension(120, 40));
        btnRefresh.addActionListener(e -> loadRequests());
        bottomPanel.add(btnRefresh);

        JButton btnRequest = new JButton("+ New Request");
        styleButton(btnRequest, BRAND_COLOR);
        btnRequest.setPreferredSize(new Dimension(160, 40));
        // Pass null to indicate NEW request
        btnRequest.addActionListener(e -> openOTRequestDialog(null));
        bottomPanel.add(btnRequest);

        add(bottomPanel, BorderLayout.SOUTH);

        // Initial Load
        loadRequests();
    }

    // --- UI Helpers ---
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
        
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER); 
                
                String status = (String) table.getValueAt(row, 6); 
                if (!isSelected) {
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

    private void loadRequests() {
        model.setRowCount(0);
        String sql = "SELECT id, request_type, details, start_date, end_date, reason, status FROM requests WHERE empNo=? ORDER BY id DESC";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empNo);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("request_type"),
                        rs.getString("details"),
                        rs.getString("start_date"),
                        rs.getString("end_date"),
                        rs.getString("reason"),
                        rs.getString("status"),
                        "Edit",   // Added
                        "Delete"  // Added
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- WORKDAY LOGIC ---
    private long calculateBusinessDays(Date startDate, Date endDate) {
        long businessDays = 0;
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);

        while (!startCal.getTime().after(endCal.getTime())) {
            int dayOfWeek = startCal.get(Calendar.DAY_OF_WEEK);
            // Count if NOT Saturday and NOT Sunday
            if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                businessDays++;
            }
            startCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return businessDays;
    }

    // --- DIALOG FOR REQUEST FORM (New & Edit) ---
    private void openOTRequestDialog(Integer requestId) {
        String title = (requestId == null) ? "New Request" : "Edit Request";
        JDialog dialog = new JDialog((Frame) null, title, true);
        dialog.setSize(450, 580);
        dialog.setLocationRelativeTo(this);
        
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // --- 1. Header Info ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblHeader = new JLabel("Request Form");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHeader.setForeground(BRAND_COLOR);
        p.add(lblHeader, gbc);
        
        gbc.gridy++;
        p.add(new JSeparator(), gbc);

        gbc.gridy++; gbc.gridwidth = 1;
        p.add(new JLabel("Name: " + empName), gbc);
        gbc.gridx = 1;
        p.add(new JLabel("Position: " + empPosition), gbc);
        
        // --- 2. Form Fields ---
        
        // Request Type
        gbc.gridx = 0; gbc.gridy++;
        p.add(new JLabel("Request Type:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<String> cbRequestType = new JComboBox<>(new String[]{"Select", "OT", "Holiday"});
        cbRequestType.setBackground(Color.WHITE);
        p.add(cbRequestType, gbc);

        // Hours (OT Only)
        gbc.gridx = 0; gbc.gridy++;
        JLabel lblHours = new JLabel("Hours:");
        p.add(lblHours, gbc);
        
        gbc.gridx = 1;
        JTextField txtHours = new JTextField();
        p.add(txtHours, gbc);

        // Holiday Type (Holiday Only)
        gbc.gridx = 0; gbc.gridy++; 
        JLabel lblHolidayType = new JLabel("Holiday Type:");
        p.add(lblHolidayType, gbc);

        gbc.gridx = 1;
        JComboBox<String> cbHolidayType = new JComboBox<>(new String[]{"Special", "Regular"});
        cbHolidayType.setBackground(Color.WHITE);
        p.add(cbHolidayType, gbc);

        // Start Date
        gbc.gridx = 0; gbc.gridy++;
        p.add(new JLabel("Start Date:"), gbc);

        gbc.gridx = 1;
        JDateChooser dcStartDate = new JDateChooser();
        dcStartDate.setDateFormatString("yyyy-MM-dd");
        p.add(dcStartDate, gbc);
        
        gbc.gridx = 0; gbc.gridy++;
        JLabel lblEndDate = new JLabel("End Date:");
        p.add(lblEndDate, gbc);

        gbc.gridx = 1;
        JDateChooser dcEndDate = new JDateChooser();
        dcEndDate.setDateFormatString("yyyy-MM-dd");
        p.add(dcEndDate, gbc);

        // Reason
        gbc.gridx = 0; gbc.gridy++;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        p.add(new JLabel("Reason:"), gbc);
        
        gbc.gridx = 1;
        JTextArea txtReason = new JTextArea(3, 20);
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        txtReason.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        p.add(txtReason, gbc);

        // --- Logic to Toggle Visibility ---
        Runnable toggleAction = () -> {
            String type = (String) cbRequestType.getSelectedItem();
            boolean isOT = "OT".equals(type);
            boolean isHoliday = "Holiday".equals(type);
            boolean isAny = isOT || isHoliday;

            lblHours.setVisible(isOT);
            txtHours.setVisible(isOT);
            
            lblHolidayType.setVisible(isHoliday);
            cbHolidayType.setVisible(isHoliday);
            
            lblEndDate.setVisible(isOT);
            dcEndDate.setVisible(isOT);
            
            dcStartDate.setEnabled(isAny);
            txtReason.setEnabled(isAny);
            
            dialog.revalidate();
            dialog.repaint();
        };

        toggleAction.run();
        cbRequestType.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) toggleAction.run();
        });

        // --- PRE-FILL DATA IF EDITING ---
        if (requestId != null) {
            try (ResultSet rs = Database.getRequestById(requestId)) {
                if (rs.next()) {
                    String type = rs.getString("request_type");
                    cbRequestType.setSelectedItem(type);
                    txtReason.setText(rs.getString("reason"));
                    
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        dcStartDate.setDate(sdf.parse(rs.getString("start_date")));
                        
                        // Handle End Date (might be null for Holidays)
                        String endStr = rs.getString("end_date");
                        if (endStr != null && !endStr.isEmpty()) {
                            dcEndDate.setDate(sdf.parse(endStr));
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }

                    if ("OT".equals(type)) {
                        txtHours.setText(rs.getString("details")); // details column holds hours
                    } else if ("Holiday".equals(type)) {
                        cbHolidayType.setSelectedItem(rs.getString("details")); // details column holds holiday type
                    }
                    toggleAction.run();
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        }

        // --- 3. Submit Button ---
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 5, 5);
        
        JButton btnSubmit = new JButton(requestId == null ? "Submit Request" : "Update Request");
        styleButton(btnSubmit, new Color(34, 139, 34));
        btnSubmit.setPreferredSize(new Dimension(200, 40));
        
        btnSubmit.addActionListener(e -> {
            String type = (String) cbRequestType.getSelectedItem();
            String reason = txtReason.getText().trim();
            Date startDateObj = dcStartDate.getDate();
            Date endDateObj = dcEndDate.getDate();

            if (startDateObj == null) {
                JOptionPane.showMessageDialog(dialog, "Start Date is required!");
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String startDate = sdf.format(startDateObj);
            String endDate = (endDateObj != null) ? sdf.format(endDateObj) : startDate;

            // --- WORKDAY VALIDATION ---
            // Ensure end date isn't before start date
            if (endDateObj != null && endDateObj.before(startDateObj)) {
                JOptionPane.showMessageDialog(dialog, "End date cannot be before start date!");
                return;
            }

            // Check if selected range includes valid workdays (Mon-Fri)
            long businessDays = calculateBusinessDays(startDateObj, (endDateObj != null ? endDateObj : startDateObj));
            if (businessDays == 0) {
                JOptionPane.showMessageDialog(dialog, "You selected only weekends. Please select valid workdays (Mon-Fri).");
                return;
            }

            // Prepare Data for Insert/Update
            String details = "";
            if ("OT".equals(type)) {
                if (endDateObj == null) {
                    JOptionPane.showMessageDialog(dialog, "End date is required for OT.");
                    return;
                }
                String hours = txtHours.getText().trim();
                if (hours.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter hours.");
                    return;
                }
                details = hours;
            } else if ("Holiday".equals(type)) {
                details = (String) cbHolidayType.getSelectedItem();
                // For Holidays, usually one day, so force end = start
                endDate = startDate; 
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a request type.");
                return;
            }

            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Reason is required.");
                return;
            }

            // --- DATABASE ACTION ---
            if (requestId == null) {
                // INSERT
                if ("OT".equals(type)) {
                    Database.insertOTRequest(empNo, details, reason, startDate, endDate);
                } else {
                    Database.insertHolidayRequest(empNo, details, reason, startDate);
                }
                JOptionPane.showMessageDialog(dialog, "Request submitted successfully!");
            } else {
                // UPDATE
                Database.updateRequest(requestId, type, details, startDate, endDate, reason);
                JOptionPane.showMessageDialog(dialog, "Request updated successfully!");
            }

            dialog.dispose();
            loadRequests();
        });
        
        p.add(btnSubmit, gbc);
        dialog.add(p);
        dialog.setVisible(true);
    }
    
    // --- DELETE LOGIC ---
    private void deleteRequest(int requestId) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this request?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            Database.deleteRequest(requestId);
            JOptionPane.showMessageDialog(this, "Request deleted!");
            loadRequests();
        }
    }

    // --- BUTTON RENDERERS (Same as LeavePanel) ---
    class ActionButtonRenderer extends JButton implements TableCellRenderer {
        public ActionButtonRenderer(String text) {
            setOpaque(true);
            setText(text);
            setFont(new Font("Segoe UI", Font.PLAIN, 11));
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
                        String status = (String) table.getValueAt(row, 6); // Status is col 6
                        if ("Pending".equalsIgnoreCase(status)) {
                            openOTRequestDialog(id);
                        } else {
                            JOptionPane.showMessageDialog(EmpOTPanel.this, "Only pending requests can be edited.");
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
                        String status = (String) table.getValueAt(row, 6);
                        if ("Pending".equalsIgnoreCase(status)) {
                            deleteRequest(id);
                        } else {
                            JOptionPane.showMessageDialog(EmpOTPanel.this, "Cannot delete processed requests.");
                        }
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