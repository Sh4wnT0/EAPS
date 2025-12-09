package Fproj;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class LeaveApproval extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearchEmpNo;
    private JComboBox<String> cbStatusFilter;
    private JComboBox<String> cbLeaveTypeFilter;
    
    private final Color BRAND_COLOR = new Color(22, 102, 87);

    public LeaveApproval() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- 1. Top Panel: Title, Search ---
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(Color.WHITE);
        GridBagConstraints gbcTop = new GridBagConstraints();
        gbcTop.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel lblTitle = new JLabel("Leave Requests Approval");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        gbcTop.gridx = 0; gbcTop.gridy = 0; gbcTop.gridwidth = 4; gbcTop.anchor = GridBagConstraints.WEST;
        topPanel.add(lblTitle, gbcTop);

        // Search Label
        JLabel lblSearch = new JLabel("Search by Emp No:");
        gbcTop.gridx = 0; gbcTop.gridy = 1; gbcTop.gridwidth = 1;
        topPanel.add(lblSearch, gbcTop);

        // Search Text Field
        txtSearchEmpNo = new JTextField();
        gbcTop.gridx = 1; gbcTop.fill = GridBagConstraints.HORIZONTAL; gbcTop.weightx = 1.0;
        topPanel.add(txtSearchEmpNo, gbcTop);

        // Search Button
        JButton btnSearch = new JButton("Search");
        styleButton(btnSearch); // Reusing style method
        btnSearch.addActionListener(e -> fetchLeaveRequests()); // Calls the merged method
        gbcTop.gridx = 2; gbcTop.fill = GridBagConstraints.NONE; gbcTop.weightx = 0;
        topPanel.add(btnSearch, gbcTop);

        // --- 2. Table Setup ---
        
        // Model includes HIDDEN columns for processing (EmpNo, StartDate, EndDate)
        // Indices: 0=Select, 1=ID, 2=Date, 3=Name, 4=Type, 5=Status, 6=View, 7=EmpNo, 8=Start, 9=End
        model = new DefaultTableModel(
                new String[]{"Select", "ID", "Date", "Name", "Leave Type", "Status", "View", "EmpNo", "Start", "End"}, 0
        ) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : super.getColumnClass(column);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 6; // Only Select and View are clickable
            }
        };

        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (column != 0 && column != 6) { // Don't color checkboxes or buttons
                    String status = (String) getValueAt(row, 5);
                    if (!isRowSelected(row)) {
                        if ("Approved".equalsIgnoreCase(status)) {
                            c.setBackground(new Color(63, 255, 38, 178));
                            c.setForeground(Color.BLACK);
                        } else if ("Rejected".equalsIgnoreCase(status)) {
                            c.setBackground(new Color(255, 32, 32, 178));
                            c.setForeground(Color.WHITE);
                        } else if ("Pending".equalsIgnoreCase(status)) {
                            c.setBackground(new Color(255, 247, 5, 178));
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

        // --- 3. Column Configuration (Hiding & Sizing) ---
        int[] hiddenCols = {1, 7, 8, 9}; // ID, EmpNo, StartDate, EndDate
        for (int col : hiddenCols) {
            table.getColumnModel().getColumn(col).setMinWidth(0);
            table.getColumnModel().getColumn(col).setMaxWidth(0);
            table.getColumnModel().getColumn(col).setWidth(0);
        }

        table.setRowHeight(30);
        table.getColumn("View").setMaxWidth(80);
        table.getColumn("View").setMinWidth(80);

        // Renderers & Editors
        table.getColumn("Select").setCellRenderer(new CheckBoxRenderer());
        table.getColumn("Select").setCellEditor(new DefaultCellEditor(new JCheckBox()));
        table.getColumn("View").setCellRenderer(new ButtonRenderer());
        table.getColumn("View").setCellEditor(new ButtonEditor(new JCheckBox()));

        // --- 4. Filter Panel ---
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBackground(Color.LIGHT_GRAY);
        filterPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.GRAY));
        GridBagConstraints gbcFilter = new GridBagConstraints();
        gbcFilter.insets = new Insets(5, 5, 5, 5);

        // Spacers to align filters to the right
        gbcFilter.gridx = 0; gbcFilter.weightx = 1.0; 
        filterPanel.add(new JLabel(""), gbcFilter);

        // Leave Type
        gbcFilter.gridx = 1; gbcFilter.weightx = 0;
        filterPanel.add(new JLabel("Type:"), gbcFilter);
        cbLeaveTypeFilter = new JComboBox<>(new String[]{"All", "Vacation", "Sick", "Emergency", "Special"});
        cbLeaveTypeFilter.addActionListener(e -> fetchLeaveRequests());
        gbcFilter.gridx = 2;
        filterPanel.add(cbLeaveTypeFilter, gbcFilter);

        // Status
        gbcFilter.gridx = 3;
        filterPanel.add(new JLabel("Status:"), gbcFilter);
        cbStatusFilter = new JComboBox<>(new String[]{"All", "Pending", "Approved", "Rejected"});
        cbStatusFilter.addActionListener(e -> fetchLeaveRequests());
        gbcFilter.gridx = 4;
        filterPanel.add(cbStatusFilter, gbcFilter);

        // Combine Filters + Table
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.add(filterPanel, BorderLayout.NORTH);
        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);

        // --- 5. Bottom Panel (Bulk Actions) ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);

        JButton btnBulkApprove = new JButton("Bulk Approve Selected");
        styleButton(btnBulkApprove);
        btnBulkApprove.addActionListener(e -> bulkProcess("Approved"));

        JButton btnBulkReject = new JButton("Bulk Reject Selected");
        styleButton(btnBulkReject);
        btnBulkReject.setBackground(new Color(220, 53, 69)); // Red
        btnBulkReject.addActionListener(e -> bulkProcess("Rejected"));

        bottomPanel.add(btnBulkApprove);
        bottomPanel.add(btnBulkReject);

        // Add to Main Layout
        add(topPanel, BorderLayout.NORTH);
        add(tableContainer, BorderLayout.CENTER);
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

    private void styleButton(JButton btn) {
        btn.setBackground(BRAND_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    /**
     * Unified method for Searching, Filtering, and Loading.
     * Selects extra hidden columns to optimize bulk processing.
     */
    void fetchLeaveRequests() {
        model.setRowCount(0);

        String empNoSearch = txtSearchEmpNo.getText().trim();
        String statusFilter = (String) cbStatusFilter.getSelectedItem();
        String leaveTypeFilter = (String) cbLeaveTypeFilter.getSelectedItem();

        StringBuilder sql = new StringBuilder(
            "SELECT lr.id, lr.submitted_date, e.name, lr.leave_type, lr.status, e.empNo, lr.start_date, lr.end_date " +
            "FROM leave_requests lr " +
            "JOIN employees e ON lr.empNo = e.empNo " +
            "WHERE 1=1 "
        );

        if (!empNoSearch.isEmpty()) {
            sql.append("AND lr.empNo LIKE ? ");
        }
        if (!"All".equals(statusFilter)) {
            sql.append("AND lr.status = ? ");
        }
        if (!"All".equals(leaveTypeFilter)) {
            sql.append("AND lr.leave_type = ? ");
        }

        sql.append("ORDER BY lr.submitted_date DESC");

        // Limit results only if not searching (optional, keeps UI snappy)
        if (empNoSearch.isEmpty()) {
            sql.append(" LIMIT 50"); 
        }

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
                            rs.getInt("id"),            // 1: ID (Hidden)
                            rs.getString("submitted_date"), // 2
                            rs.getString("name"),       // 3
                            rs.getString("leave_type"), // 4
                            rs.getString("status"),     // 5
                            "View",                     // 6: Button
                            rs.getString("empNo"),      // 7: Hidden EmpNo
                            rs.getString("start_date"), // 8: Hidden Start
                            rs.getString("end_date")    // 9: Hidden End
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching data: " + e.getMessage());
        }
    }

    /**
     * Optimized Bulk Process.
     * Uses the HIDDEN columns in the table model to avoid N+1 Database queries.
     */
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
                // Get data directly from table model (Hidden Columns)
                int id = (int) model.getValueAt(row, 1);
                String leaveType = (String) model.getValueAt(row, 4);
                String empNo = (String) model.getValueAt(row, 7);     // From hidden col
                String startDate = (String) model.getValueAt(row, 8); // From hidden col
                String endDate = (String) model.getValueAt(row, 9);   // From hidden col

                Database.processLeaveApproval(id, action, empNo, startDate, endDate, leaveType);
                successCount++;
            } catch (Exception e) {
                System.err.println("Error processing row " + row + ": " + e.getMessage());
            }
        }

        JOptionPane.showMessageDialog(this, "Successfully " + action.toLowerCase() + " " + successCount + " requests.");
        fetchLeaveRequests(); // Refresh table
    }

    private void viewLeaveDetails(int id) {
        LeaveDetailsDialog dialog = new LeaveDetailsDialog(id);
        dialog.setVisible(true);
        fetchLeaveRequests();
    }

    // --- Inner Classes: Renderers, Editors, Dialog ---

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
        public ButtonRenderer() { setOpaque(true); }
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
            button.setBorderPainted(false);
            button.setOpaque(true);
            button.addActionListener((ActionEvent e) -> {
                if (clicked) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        int id = (int) table.getValueAt(row, 1); // ID is at index 1
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
            
            // Re-apply color logic for the button editor state if needed, or keep standard
            String status = (String) table.getValueAt(row, 5);
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
        public Object getCellEditorValue() { return label; }
        @Override
        public boolean stopCellEditing() { clicked = false; return super.stopCellEditing(); }
    }

    // Dialog Class
    private class LeaveDetailsDialog extends JDialog {
        public LeaveDetailsDialog(int id) {
            setTitle("Leave Request Details");
            setModal(true);
            setSize(500, 450); // Slightly larger
            setLocationRelativeTo(null);
            setLayout(null);

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

                    if (photoPath != null && !photoPath.isEmpty()) {
                        File imageFile = new File(photoPath);
                        if (imageFile.exists()) {
                            ImageIcon icon = new ImageIcon(photoPath);
                            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                            lblImage.setIcon(new ImageIcon(img));
                        } else {
                            lblImage.setText("No photo");
                            lblImage.setHorizontalAlignment(SwingConstants.CENTER);
                        }
                    } else {
                        lblImage.setText("No photo");
                        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
                    }
                    add(lblImage);

                    int y = 20;
                    addLabel("Emp No: " + empNoVal, 140, y, Color.BLACK); y += 30;
                    addLabel("Name: " + name, 140, y, Color.BLACK); y += 30;
                    addLabel("Leave Type: " + leaveType, 140, y, Color.RED); y += 30;
                    addLabel("Reason: " + reason, 140, y, Color.RED); y += 30;
                    addLabel("Start Date: " + startDate, 140, y, Color.RED); y += 30;
                    addLabel("End Date: " + endDate, 140, y, Color.RED); y += 30;
                    addLabel("Submitted: " + submittedDate, 140, y, Color.BLACK); y += 50;

                    JButton approve = new JButton("Approve");
                    approve.setBounds(140, y, 100, 30);
                    approve.setBackground(Color.GREEN);
                    approve.setForeground(Color.WHITE);
                    approve.addActionListener(e -> {
                        Database.processLeaveApproval(requestId, "Approved", empNoVal, startDate, endDate, leaveType);
                        JOptionPane.showMessageDialog(this, "Approved!");
                        dispose();
                    });
                    add(approve);

                    JButton reject = new JButton("Reject");
                    reject.setBounds(260, y, 100, 30);
                    reject.setBackground(Color.RED);
                    reject.setForeground(Color.WHITE);
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
            lbl.setFont(new Font("Arial", Font.PLAIN, 14));
            lbl.setForeground(color);
            add(lbl);
        }
    }
}