package Fproj;

import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class EmpOTPanel extends JPanel {

    private String empNo, empName = "", empPosition = "";
    private double empSalary = 0.0;
    
    // Main UI Components
    private JTable table;
    private DefaultTableModel model;
    
    public EmpOTPanel(String empNo) {
        this.empNo = empNo;
        fetchEmployeeDetails();

        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- TOP: Title ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        
        JLabel lblTitle = new JLabel("Overtime & Holiday Requests");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        topPanel.add(lblTitle, BorderLayout.WEST);
        
        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: History Table ---
        // Table Model
        model = new DefaultTableModel(new String[]{"ID", "Type", "Details", "Start", "End", "Reason", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        // Table Styling
        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                String status = (String) getValueAt(row, 6);
                if (!isRowSelected(row)) {
                    if ("Approved".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(200, 255, 200)); // Soft Green
                        c.setForeground(Color.BLACK);
                    } else if ("Rejected".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(255, 200, 200)); // Soft Red
                        c.setForeground(Color.BLACK);
                    } else if ("Pending".equalsIgnoreCase(status)) {
                        c.setBackground(Color.YELLOW); // Soft Yellow
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

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // --- BOTTOM: Buttons ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setPreferredSize(new Dimension(100, 35));
        btnRefresh.addActionListener(e -> loadRequests());
        bottomPanel.add(btnRefresh);

        JButton btnRequest = new JButton("New Request");
        btnRequest.setBackground(new Color(34, 139, 34));
        btnRequest.setForeground(Color.WHITE);
        btnRequest.setPreferredSize(new Dimension(150, 35));
        btnRequest.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRequest.setFocusPainted(false);
        btnRequest.addActionListener(e -> openOTRequestDialog());
        bottomPanel.add(btnRequest);

        add(bottomPanel, BorderLayout.SOUTH);

        // Initial Load
        loadRequests();
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
        dialog.setSize(450, 550);
        dialog.setLocationRelativeTo(this);
        
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // --- 1. Header Info ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblHeader = new JLabel("Employee Details");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        p.add(lblHeader, gbc);
        
        gbc.gridy++; gbc.gridwidth = 1;
        p.add(new JLabel("Name: " + empName), gbc);
        
        gbc.gridx = 1;
        p.add(new JLabel("Position: " + empPosition), gbc);
        
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        p.add(new JSeparator(), gbc);

        // --- 2. Form Fields ---
        // Declare components final/effectively final for listeners
        
        // Request Type
        gbc.gridy++; gbc.gridwidth = 1;
        p.add(new JLabel("Request Type:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<String> cbRequestType = new JComboBox<>(new String[]{"Select", "OT", "Holiday"});
        p.add(cbRequestType, gbc);

        // Hours (OT Only)
        gbc.gridx = 0; gbc.gridy++;
        JLabel lblHours = new JLabel("Hours:");
        p.add(lblHours, gbc);
        
        gbc.gridx = 1;
        JTextField txtHours = new JTextField();
        p.add(txtHours, gbc);

        // Holiday Type (Holiday Only)
        gbc.gridx = 0; gbc.gridy++; // Same row as hours visually, but handled by visibility
        JLabel lblHolidayType = new JLabel("Holiday Type:");
        p.add(lblHolidayType, gbc);

        gbc.gridx = 1;
        JComboBox<String> cbHolidayType = new JComboBox<>(new String[]{"Special", "Regular"});
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
        txtReason.setBorder(BorderFactory.createLineBorder(Color.GRAY));
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

        // Initialize state
        toggleAction.run();
        
        // Add Listener
        cbRequestType.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                toggleAction.run();
            }
        });

        // --- 4. Submit Button ---
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 5, 5);
        
        JButton btnSubmit = new JButton("Submit Request");
        btnSubmit.setPreferredSize(new Dimension(200, 35));
        btnSubmit.setBackground(new Color(34, 139, 34));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFocusPainted(false);
        
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
            loadRequests(); // Refresh main table
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