package Fproj;

import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

public class EmpOTPanel extends JPanel {

    private String empNo, empName = "", empPosition = "";
    private double empSalary = 0.0;
    
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
        model = new DefaultTableModel(new String[]{"ID", "Type", "Details", "Start", "End", "Reason", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        table = new JTable(model);
        styleTable(table);

        // Hide ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

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
        btnRequest.addActionListener(e -> openOTRequestDialog());
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
        
        // Custom Renderer for Row Colors and Alignment
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER); // Center align text
                
                String status = (String) table.getValueAt(row, 6); // Status Column
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
        String sql = "SELECT name, position, dailyPay FROM employees WHERE empNo = ?";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empNo);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                empName = rs.getString("name");
                empPosition = rs.getString("position");
                empSalary = rs.getDouble("dailyPay");
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
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- DIALOG FOR REQUEST FORM ---
    private void openOTRequestDialog() {
        JDialog dialog = new JDialog((Frame) null, "Submit Request", true);
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

        // --- 3. Logic to Toggle Visibility ---
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

        // --- 4. Submit Button ---
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 5, 5);
        
        JButton btnSubmit = new JButton("Submit Request");
        styleButton(btnSubmit, new Color(34, 139, 34));
        btnSubmit.setPreferredSize(new Dimension(200, 40));
        
        btnSubmit.addActionListener(e -> {
            String type = (String) cbRequestType.getSelectedItem();
            String reason = txtReason.getText().trim();
            Date startDateObj = dcStartDate.getDate();
            String startDate = startDateObj == null ? "" :
                new SimpleDateFormat("yyyy-MM-dd").format(startDateObj);

            if ("OT".equals(type)) {
                String hours = txtHours.getText().trim();
                Date endDateObj = dcEndDate.getDate();
                String endDate = endDateObj == null ? "" :
                    new SimpleDateFormat("yyyy-MM-dd").format(endDateObj);
                
                if (hours.isEmpty() || reason.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill all fields!");
                    return;
                }
                if (!isValidDateRange(startDate, endDate)) return;
                
                Database.insertOTRequest(empNo, hours, reason, startDate, endDate);
                
            } else if ("Holiday".equals(type)) {
                String holidayType = (String) cbHolidayType.getSelectedItem();
                if (reason.isEmpty() || startDate.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill all fields!");
                    return;
                }
                Database.insertHolidayRequest(empNo, holidayType, reason, startDate);
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a request type.");
                return;
            }

            JOptionPane.showMessageDialog(dialog, "Request submitted successfully!");
            dialog.dispose();
            loadRequests();
        });
        
        p.add(btnSubmit, gbc);
        dialog.add(p);
        dialog.setVisible(true);
    }

    private boolean isValidDateRange(String start, String end) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            Date startD = sdf.parse(start);
            Date endD = sdf.parse(end);
            if (endD.before(startD)) {
                JOptionPane.showMessageDialog(this, "End date must be after start date!");
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date format! Use yyyy-MM-dd");
            return false;
        }
        return true;
    }

    public void disposePanel() {
        if (table != null) table.setModel(null);
        if (model != null) model.setRowCount(0);
    }
}