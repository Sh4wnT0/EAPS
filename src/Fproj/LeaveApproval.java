package Fproj;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.util.Vector;

public class LeaveApproval extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearchEmpNo;

    public LeaveApproval() {
        setLayout(null);
        setBackground(Color.WHITE);

        // Title
        JLabel lblTitle = new JLabel("Leave Requests Approval");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setBounds(20, 20, 250, 30);
        add(lblTitle);

        // Search Field
        JLabel lblSearch = new JLabel("Search by Emp No:");
        lblSearch.setBounds(20, 60, 150, 25);
        add(lblSearch);
        txtSearchEmpNo = new JTextField();
        txtSearchEmpNo.setBounds(150, 60, 150, 25);
        add(txtSearchEmpNo);

        // Search Button
        JButton btnSearch = new JButton("Search");
        btnSearch.setBounds(310, 60, 80, 25);
        btnSearch.addActionListener(e -> searchLeaveRequests());
        add(btnSearch);

        // NEW: Refresh Button
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBounds(400, 60, 80, 25);
        btnRefresh.addActionListener(e -> loadRecentLeaveRequests());
        add(btnRefresh);

        // UPDATED: Table with ID column
        model = new DefaultTableModel(new String[]{"ID", "Emp No", "Leave Type", "Status", "View"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;  // Only View button is editable
            }
        };
        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                String status = (String) getValueAt(row, 3);
                if (!isRowSelected(row)) {
                    if ("Approved".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(63, 255, 38, 178)); // rgba(63, 255, 38, 0.7)
                        if (c instanceof JButton) {
                            ((JButton) c).setForeground(Color.BLACK);
                        } else {
                            c.setForeground(Color.BLACK);
                        }
                    } else if ("Rejected".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(255, 32, 32, 178)); // rgba(255, 32, 32, 0.7)
                        if (c instanceof JButton) {
                            ((JButton) c).setForeground(Color.WHITE);
                        } else {
                            c.setForeground(Color.WHITE);
                        }
                    } else if ("Pending".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(255, 247, 5, 178)); // rgba(255, 247, 5, 0.7)
                        if (c instanceof JButton) {
                            ((JButton) c).setForeground(Color.BLACK);
                        } else {
                            c.setForeground(Color.BLACK);
                        }
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                }
                return c;
            }
        };
        table.getColumn("View").setCellRenderer(new ButtonRenderer());
        table.getColumn("View").setCellEditor(new ButtonEditor(new JCheckBox()));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(20, 100, 750, 300);
        add(scroll);

        // Load initial data (10 most recent)
        loadRecentLeaveRequests();
    }

    private void loadRecentLeaveRequests() {
        model.setRowCount(0);
        try (ResultSet rs = Database.getRecentLeaveRequests()) {
            while (rs != null && rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),  // NEW: Include ID
                        rs.getString("empNo"),
                        rs.getString("leave_type"),
                        rs.getString("status"),
                        "View"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchLeaveRequests() {
        String empNo = txtSearchEmpNo.getText().trim();
        model.setRowCount(0);
        try (ResultSet rs = Database.searchLeaveRequestsByEmpNo(empNo)) {
            while (rs != null && rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),  // NEW: Include ID
                        rs.getString("empNo"),
                        rs.getString("leave_type"),
                        rs.getString("status"),
                        "View"
                });
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
                    // UPDATED: Pass ID (column 0) instead of empNo
                    int id = (Integer) table.getValueAt(table.getSelectedRow(), 0);
                    viewLeaveDetails(id);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            // Set background and foreground for editor button based on status
            String status = (String) table.getValueAt(row, 3);
            if ("Approved".equalsIgnoreCase(status)) {
                button.setBackground(new Color(63, 255, 38, 178));
                button.setForeground(Color.BLACK);
            } else if ("Rejected".equalsIgnoreCase(status)) {
                button.setBackground(new Color(255, 32, 32, 178));
                button.setForeground(Color.WHITE);
            } else if ("Pending".equalsIgnoreCase(status)) {
                button.setBackground(new Color(255, 247, 5, 178));
                button.setForeground(Color.BLACK);
            } else {
                button.setBackground(Color.WHITE);
                button.setForeground(Color.BLACK);
            }
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

    // UPDATED: viewLeaveDetails now takes int id
    private void viewLeaveDetails(int id) {
        LeaveDetailsDialog dialog = new LeaveDetailsDialog(id);
        dialog.setVisible(true);
        // Refresh table after dialog closes
        loadRecentLeaveRequests();
    }

    // Custom Dialog for Leave Details
    private class LeaveDetailsDialog extends JDialog {
        // UPDATED: Constructor takes int id
        public LeaveDetailsDialog(int id) {
            setTitle("Leave Request Details");
            setModal(true);
            setSize(500, 400);
            setLocationRelativeTo(null);
            setLayout(null);

            try (ResultSet rs = Database.getLeaveRequestWithEmployeeDetailsById(id)) {  // UPDATED: Query by ID
                if (rs != null && rs.next()) {
                    // Extract values from rs to avoid accessing closed ResultSet later
                    int requestId = rs.getInt("id");
                    String empNoVal = rs.getString("empNo");
                    String name = rs.getString("name");
                    String leaveType = rs.getString("leave_type");
                    String reason = rs.getString("reason");
                    String startDate = rs.getString("start_date");
                    String endDate = rs.getString("end_date");
                    String submittedDate = rs.getString("submitted_date");
                    String photoPath = rs.getString("photo_path");

                    // Employee Picture (left side)
                    JLabel lblImage = new JLabel();
                    lblImage.setBounds(20, 20, 100, 100);
                    lblImage.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                    if (photoPath != null && !photoPath.isEmpty()) {
                        File imageFile = new File(photoPath);
                        if (imageFile.exists()) {
                            ImageIcon icon = new ImageIcon(photoPath);
                            Image scaledImage = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                            lblImage.setIcon(new ImageIcon(scaledImage));
                        } else {
                            lblImage.setText("No photo");
                            lblImage.setHorizontalAlignment(SwingConstants.CENTER);
                        }
                    } else {
                        lblImage.setText("No photo");
                        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
                    }
                    add(lblImage);

                    // Details (right side)
                    int y = 20;
                    addDetailLabel("Emp No: " + empNoVal, 140, y, Color.BLACK); y += 30;
                    addDetailLabel("Name: " + name, 140, y, Color.BLACK); y += 30;
                    addDetailLabel("Leave Type: " + leaveType, 140, y, Color.RED); y += 30;  // Red
                    addDetailLabel("Reason: " + reason, 140, y, Color.RED); y += 30;  // Red
                    addDetailLabel("Start Date: " + startDate, 140, y, Color.RED); y += 30;  // Red
                    addDetailLabel("End Date: " + endDate, 140, y, Color.RED); y += 30;  // Red
                    addDetailLabel("Submission Date: " + submittedDate, 140, y, Color.BLACK); y += 40;

                    // Approve Button (green)
                    JButton btnApprove = new JButton("Approve");
                    btnApprove.setBounds(140, y, 100, 30);
                    btnApprove.setBackground(Color.GREEN);
                    btnApprove.setForeground(Color.WHITE);
                    btnApprove.addActionListener(e -> {
                        // UPDATED: Check balance before approving
                        int[] balances = Database.getLeaveBalances(empNoVal);
                        int balanceIndex = getBalanceIndex(leaveType);
                        if (balanceIndex != -1) {
                            long days = calculateLeaveDays(startDate, endDate);
                            int currentBalance = balances[balanceIndex];
                            if (currentBalance < days) {
                                JOptionPane.showMessageDialog(this, "Insufficient leave balance! Current: " + currentBalance + " days, Required: " + days + " days.");
                                return;  // Don't proceed
                            }
                        }
                        Database.processLeaveApproval(requestId, "Approved", empNoVal, startDate, endDate, leaveType);
                        JOptionPane.showMessageDialog(this, "Request Approved! Notification sent to employee.");
                        dispose();
                    });
                    add(btnApprove);

                    // Reject Button (red)
                    JButton btnReject = new JButton("Reject");
                    btnReject.setBounds(260, y, 100, 30);
                    btnReject.setBackground(Color.RED);
                    btnReject.setForeground(Color.WHITE);
                    btnReject.addActionListener(e -> {
                        Database.processLeaveApproval(requestId, "Rejected", empNoVal, startDate, endDate, leaveType);
                        JOptionPane.showMessageDialog(this, "Request Rejected! Notification sent to employee.");
                        dispose();
                    });
                    add(btnReject);
                } else {
                    JOptionPane.showMessageDialog(this, "No details found for ID: " + id);
                    dispose();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading details.");
                dispose();
            }
        }

        private void addDetailLabel(String text, int x, int y, Color color) {
            JLabel lbl = new JLabel(text);
            lbl.setBounds(x, y, 300, 25);
            lbl.setFont(new Font("Arial", Font.PLAIN, 14));
            lbl.setForeground(color);  // Set the text color
            add(lbl);
        }

        // Helper to calculate leave days
        private long calculateLeaveDays(String startDate, String endDate) {
            try {
                java.time.LocalDate start = java.time.LocalDate.parse(startDate);
                java.time.LocalDate end = java.time.LocalDate.parse(endDate);
                return java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
            } catch (Exception e) {
                return 0;
            }
        }

        // Helper to get balance index
        private int getBalanceIndex(String leaveType) {
            switch (leaveType.toLowerCase()) {
                case "vacation": return 0;
                case "sick": return 1;
                case "emergency": return 2;
                case "special": return 3;
                default: return -1;
            }
        }
    }
}