package Fproj;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

public class LeaveApproval extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearchEmpNo;
    private JComboBox<String> cbLeaveTypeFilter;
    private JComboBox<String> cbStatusFilter;
    
    // --- UI CONSTANTS (Matched to OTAdmin) ---
    private final Color PRIMARY_COLOR = new Color(22, 102, 87);
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private final Font CELL_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    public LeaveApproval() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // ================= TOP PANEL =================
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Title
        JLabel lblTitle = new JLabel("Leave Requests Approval");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(50, 50, 50));
        topPanel.add(lblTitle, BorderLayout.WEST);

        // --- Action Bar (Filters + Search) ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        // Filters
        cbLeaveTypeFilter = new JComboBox<>(new String[]{"All", "Vacation", "Sick", "Emergency", "Special"});
        styleComboBox(cbLeaveTypeFilter);
        cbLeaveTypeFilter.addActionListener(e -> fetchLeaveRequests());

        cbStatusFilter = new JComboBox<>(new String[]{"All", "Pending", "Approved", "Rejected"});
        styleComboBox(cbStatusFilter);
        cbStatusFilter.addActionListener(e -> fetchLeaveRequests());

        // Search Field
        txtSearchEmpNo = new JTextField(15);
        putPlaceholder(txtSearchEmpNo, "Search Emp No...");
        txtSearchEmpNo.setPreferredSize(new Dimension(150, 35));

        // Search Button
        JButton btnSearch = new JButton("Search");
        styleButton(btnSearch, PRIMARY_COLOR);
        btnSearch.addActionListener(e -> fetchLeaveRequests());

        // Add to Action Panel
        actionPanel.add(new JLabel("Type:"));
        actionPanel.add(cbLeaveTypeFilter);
        actionPanel.add(Box.createHorizontalStrut(10));
        actionPanel.add(new JLabel("Status:"));
        actionPanel.add(cbStatusFilter);
        actionPanel.add(Box.createHorizontalStrut(15));
        actionPanel.add(txtSearchEmpNo);
        actionPanel.add(btnSearch);

        topPanel.add(actionPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // ================= CENTER TABLE =================
        // Indices: 
        // 0=Select, 1=ID, 2=Date, 3=Name, 4=Leave Type, 5=Status, 6=View
        // Hidden: 7=EmpNo, 8=Start, 9=End, 10=Reason
        model = new DefaultTableModel(
                new String[]{
                    "Select", "ID", "Date", "Name", "Leave Type", "Status", "View", 
                    "EmpNo", "Start", "End", "Reason"
                }, 0
        ) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : super.getColumnClass(column);
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 6; // Only Select and View
            }
        };

        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (column != 0 && column != 6) { 
                    String status = (String) getValueAt(row, 5); // Status is at index 5
                    if (!isRowSelected(row)) {
                        if ("Approved".equalsIgnoreCase(status)) {
                            c.setBackground(new Color(200, 255, 200)); // Light Green
                            c.setForeground(Color.BLACK);
                        } else if ("Rejected".equalsIgnoreCase(status)) {
                            c.setBackground(new Color(255, 200, 200)); // Light Red
                            c.setForeground(Color.BLACK);
                        } else if ("Pending".equalsIgnoreCase(status)) {
                            c.setBackground(new Color(255, 255, 200)); // Light Yellow
                            c.setForeground(Color.BLACK);
                        } else {
                            c.setBackground(Color.WHITE);
                            c.setForeground(Color.BLACK);
                        }
                    }
                }
                return c;
            }
        };

        // --- TABLE STYLING ---
        table.setRowHeight(45);
        table.setFont(CELL_FONT);
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(230, 240, 255));
        table.setSelectionForeground(Color.BLACK);

        // Header Styling
        JTableHeader header = table.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));

        // --- COLUMN SIZING & HIDING ---
        int[] hiddenCols = {1, 7, 8, 9, 10}; // ID, EmpNo, Start, End, Reason
        TableColumnModel cm = table.getColumnModel();
        
        // Hide Columns
        for (int col : hiddenCols) {
            cm.getColumn(col).setMinWidth(0);
            cm.getColumn(col).setMaxWidth(0);
            cm.getColumn(col).setWidth(0);
        }

        // Specific Widths
        cm.getColumn(0).setMaxWidth(60); // Select
        cm.getColumn(0).setMinWidth(60);
        
        cm.getColumn(3).setPreferredWidth(200); // Name
        cm.getColumn(4).setPreferredWidth(120); // Type
        
        cm.getColumn(6).setMaxWidth(90); // View
        cm.getColumn(6).setMinWidth(90);

        // Renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setBorder(new EmptyBorder(0, 5, 0, 0));
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        table.getColumn("Select").setCellRenderer(new CheckBoxRenderer());
        table.getColumn("Select").setCellEditor(new DefaultCellEditor(new JCheckBox()));
        table.getColumn("View").setCellRenderer(new ButtonRenderer());
        table.getColumn("View").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // ================= BOTTOM PANEL =================
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(new Color(245, 245, 245));

        JButton btnBulkApprove = new JButton("Bulk Approve Selected");
        styleButton(btnBulkApprove, new Color(22, 102, 87));
        btnBulkApprove.addActionListener(e -> bulkProcess("Approved"));

        JButton btnBulkReject = new JButton("Bulk Reject Selected");
        styleButton(btnBulkReject, new Color(220, 53, 69));
        btnBulkReject.addActionListener(e -> bulkProcess("Rejected"));

        bottomPanel.add(btnBulkApprove);
        bottomPanel.add(btnBulkReject);

        add(bottomPanel, BorderLayout.SOUTH);

        // Initial Load
        fetchLeaveRequests();

        // Refresh on show
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                fetchLeaveRequests();
            }
        });
    }

    // ---------------- HELPER METHODS (UI) ----------------
    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleComboBox(JComboBox<String> cb) {
        cb.setBackground(Color.WHITE);
        cb.setFont(CELL_FONT);
        cb.setPreferredSize(new Dimension(100, 35));
    }

    private void putPlaceholder(JTextField field, String text) {
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        // You could add a focus listener here for true placeholder text if desired
    }

    // ---------------- LOGIC METHODS ----------------
    void fetchLeaveRequests() {
        model.setRowCount(0);

        String empNoSearch = txtSearchEmpNo.getText().trim();
        String statusFilter = (String) cbStatusFilter.getSelectedItem();
        String leaveTypeFilter = (String) cbLeaveTypeFilter.getSelectedItem();

        StringBuilder sql = new StringBuilder(
            "SELECT lr.id, lr.submitted_date, e.name, lr.leave_type, lr.status, e.empNo, lr.start_date, lr.end_date, lr.reason " +
            "FROM leave_requests lr " +
            "JOIN employees e ON lr.empNo = e.empNo " +
            "WHERE 1=1 "
        );

        if (!empNoSearch.isEmpty()) sql.append("AND lr.empNo LIKE ? ");
        if (!"All".equals(statusFilter)) sql.append("AND lr.status = ? ");
        if (!"All".equals(leaveTypeFilter)) sql.append("AND lr.leave_type = ? ");

        sql.append("ORDER BY lr.submitted_date DESC");
        if (empNoSearch.isEmpty()) sql.append(" LIMIT 50");

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (!empNoSearch.isEmpty()) ps.setString(index++, "%" + empNoSearch + "%");
            if (!"All".equals(statusFilter)) ps.setString(index++, statusFilter);
            if (!"All".equals(leaveTypeFilter)) ps.setString(index++, leaveTypeFilter);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            false,                      // 0: Select
                            rs.getInt("id"),            // 1: ID
                            rs.getString("submitted_date"), // 2
                            rs.getString("name"),       // 3
                            rs.getString("leave_type"), // 4
                            rs.getString("status"),     // 5
                            "View",                     // 6: Button
                            rs.getString("empNo"),      // 7: Hidden
                            rs.getString("start_date"), // 8: Hidden
                            rs.getString("end_date"),   // 9: Hidden
                            rs.getString("reason")      // 10: Hidden
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching data: " + e.getMessage());
        }
    }

    private void bulkProcess(String action) {
        if (table.getRowCount() == 0) return;

        List<Integer> rowsToProcess = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if ((Boolean) model.getValueAt(i, 0)) {
                rowsToProcess.add(i);
            }
        }

        if (rowsToProcess.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one request.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to " + action.toLowerCase() + " " + rowsToProcess.size() + " items?", 
            "Confirm Bulk Action", JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) return;

        int successCount = 0;
        for (int row : rowsToProcess) {
            try {
                int id = (int) model.getValueAt(row, 1);
                String leaveType = (String) model.getValueAt(row, 4);
                String empNo = (String) model.getValueAt(row, 7);
                String startDate = (String) model.getValueAt(row, 8);
                String endDate = (String) model.getValueAt(row, 9);

                Database.processLeaveApproval(id, action, empNo, startDate, endDate, leaveType);
                successCount++;
            } catch (Exception e) {
                System.err.println("Error processing row " + row + ": " + e.getMessage());
            }
        }

        JOptionPane.showMessageDialog(this, "Successfully " + action.toLowerCase() + " " + successCount + " requests.");
        fetchLeaveRequests(); 
    }

    private void viewLeaveDetails(int id) {
        LeaveDetailsDialog dialog = new LeaveDetailsDialog(id);
        dialog.setVisible(true);
        fetchLeaveRequests();
    }

    // --- Renderers & Editors (Same as OTAdmin) ---

    class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {
        public CheckBoxRenderer() { setHorizontalAlignment(SwingConstants.CENTER); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setSelected(value != null && (Boolean) value);
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { 
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 11));
            setBackground(new Color(230, 230, 230)); 
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            setMargin(new Insets(2, 5, 2, 5));
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private final JButton button = new JButton();
        private String label;
        private boolean clicked;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button.setFocusPainted(false);
            button.setOpaque(true);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            button.addActionListener((ActionEvent e) -> {
                if (clicked) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        int id = (int) table.getValueAt(row, 1);
                        viewLeaveDetails(id);
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

    // --- Leave Details Dialog (Copied Format from OTAdmin) ---
    private class LeaveDetailsDialog extends JDialog {
        public LeaveDetailsDialog(int id) {
            setTitle("Leave Request Details");
            setModal(true);
            setSize(500, 450);
            setLocationRelativeTo(null);
            setLayout(null); // Absolute positioning to match OTAdmin
            getContentPane().setBackground(Color.WHITE);

            try (ResultSet rs = Database.getLeaveRequestWithEmployeeDetailsById(id)) {
                if (rs != null && rs.next()) {
                    int requestId = rs.getInt("id");
                    String empNoVal = rs.getString("empNo");
                    String name = rs.getString("name");
                    String leaveType = rs.getString("leave_type");
                    String reason = rs.getString("reason");
                    String startDate = rs.getString("start_date");
                    String endDate = rs.getString("end_date");
                    String submittedDate = rs.getString("submitted_date");
                    String photoPath = rs.getString("photo_path");

                    JLabel lblImage = new JLabel();
                    lblImage.setBounds(20, 20, 100, 100);
                    lblImage.setBorder(BorderFactory.createLineBorder(Color.GRAY));

                    if (photoPath != null && !photoPath.isEmpty() && new File(photoPath).exists()) {
                            ImageIcon icon = new ImageIcon(photoPath);
                            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                            lblImage.setIcon(new ImageIcon(img));
                    } else {
                        lblImage.setText("No photo");
                        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
                    }
                    add(lblImage);

                    int y = 20;
                    addLabel("Emp No: " + empNoVal, 140, y, Color.BLACK); y += 30;
                    addLabel("Name: " + name, 140, y, Color.BLACK); y += 30;
                    addLabel("Leave Type: " + leaveType, 140, y, new Color(22, 102, 87)); y += 30;
                    addLabel("Reason: " + reason, 140, y, Color.DARK_GRAY); y += 30;
                    addLabel("Start Date: " + startDate, 140, y, Color.BLACK); y += 30;
                    addLabel("End Date: " + endDate, 140, y, Color.BLACK); y += 30;
                    addLabel("Submitted: " + submittedDate, 140, y, Color.GRAY); y += 40;

                    JButton approve = new JButton("Approve");
                    styleButton(approve, new Color(22, 102, 87));
                    approve.setBounds(140, y, 100, 35);
                    approve.addActionListener(e -> {
                        Database.processLeaveApproval(requestId, "Approved", empNoVal, startDate, endDate, leaveType);
                        JOptionPane.showMessageDialog(this, "Approved!");
                        dispose();
                    });
                    add(approve);

                    JButton reject = new JButton("Reject");
                    styleButton(reject, new Color(220, 53, 69));
                    reject.setBounds(260, y, 100, 35);
                    reject.addActionListener(e -> {
                        Database.processLeaveApproval(requestId, "Rejected", empNoVal, startDate, endDate, leaveType);
                        JOptionPane.showMessageDialog(this, "Rejected!");
                        dispose();
                    });
                    add(reject);

                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading details.");
                dispose();
            }
        }

        private void addLabel(String text, int x, int y, Color color) {
            JLabel lbl = new JLabel(text);
            lbl.setBounds(x, y, 320, 25);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lbl.setForeground(color);
            add(lbl);
        }
    }
}