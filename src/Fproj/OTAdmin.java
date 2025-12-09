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

public class OTAdmin extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearchEmpNo;
    private JComboBox<String> cbTypeFilter;
    private JComboBox<String> cbStatusFilter;

    private final Color BRAND_COLOR = new Color(22, 102, 87);

    public OTAdmin() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- 1. Top Panel: Title & Search ---
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(Color.WHITE);
        GridBagConstraints gbcTop = new GridBagConstraints();
        gbcTop.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel lblTitle = new JLabel("OT/Holiday Requests Approval");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        gbcTop.gridx = 0; gbcTop.gridy = 0; gbcTop.gridwidth = 4; gbcTop.anchor = GridBagConstraints.WEST;
        topPanel.add(lblTitle, gbcTop);

        // Search Label
        JLabel lblSearch = new JLabel("Search by Emp No:");
        gbcTop.gridx = 0; gbcTop.gridy = 1; gbcTop.gridwidth = 1;
        topPanel.add(lblSearch, gbcTop);

        // Search Field
        txtSearchEmpNo = new JTextField();
        gbcTop.gridx = 1; gbcTop.fill = GridBagConstraints.HORIZONTAL; gbcTop.weightx = 1.0;
        topPanel.add(txtSearchEmpNo, gbcTop);

        // Search Button
        JButton btnSearch = new JButton("Search");
        styleButton(btnSearch);
        btnSearch.addActionListener(e -> fetchRequests());
        gbcTop.gridx = 2; gbcTop.fill = GridBagConstraints.NONE; gbcTop.weightx = 0;
        topPanel.add(btnSearch, gbcTop);

        // --- 2. Table Setup (With Hidden Columns for Optimization) ---
        // Indices: 
        // 0=Select, 1=ID, 2=Date, 3=Name, 4=EmpNo, 5=Type, 6=Status, 7=View
        // Hidden: 8=Details, 9=StartDate, 10=EndDate
        model = new DefaultTableModel(
                new String[]{
                    "Select", "ID", "Date", "Name", "Emp No", "Type", "Status", "View", 
                    "Details", "StartDate", "EndDate"
                }, 0
        ) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : super.getColumnClass(column);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 7; // Only Select and View
            }
        };

        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (column != 0 && column != 7) { // Skip Checkbox and Button
                    String status = (String) getValueAt(row, 6); // Status is col 6
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

        // --- 3. Hide Optimization Columns & ID ---
        int[] hiddenCols = {1, 8, 9, 10}; // ID, Details, Start, End
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

        // Spacer
        gbcFilter.gridx = 0; gbcFilter.weightx = 1.0; 
        filterPanel.add(new JLabel(""), gbcFilter);

        // Type Filter
        gbcFilter.gridx = 1; gbcFilter.weightx = 0;
        filterPanel.add(new JLabel("Request Type:"), gbcFilter);
        cbTypeFilter = new JComboBox<>(new String[]{"All", "OT", "Holiday"});
        cbTypeFilter.addActionListener(e -> fetchRequests());
        gbcFilter.gridx = 2;
        filterPanel.add(cbTypeFilter, gbcFilter);

        // Status Filter
        gbcFilter.gridx = 3;
        filterPanel.add(new JLabel("Status:"), gbcFilter);
        cbStatusFilter = new JComboBox<>(new String[]{"All", "Pending", "Approved", "Rejected"});
        cbStatusFilter.addActionListener(e -> fetchRequests());
        gbcFilter.gridx = 4;
        filterPanel.add(cbStatusFilter, gbcFilter);

        // Table Container
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

        // Assemble Layout
        add(topPanel, BorderLayout.NORTH);
        add(tableContainer, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Initial Load
        fetchRequests();

        // Reload on Show
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                fetchRequests();
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
     * Unified Fetch Method: Handles Search, Type Filter, and Status Filter.
     * Loads hidden columns for bulk efficiency.
     */
    void fetchRequests() {
        model.setRowCount(0);

        String empNoSearch = txtSearchEmpNo.getText().trim();
        String typeFilter = (String) cbTypeFilter.getSelectedItem();
        String statusFilter = (String) cbStatusFilter.getSelectedItem();

        StringBuilder sql = new StringBuilder(
            "SELECT r.id, r.submitted_date, e.name, r.empNo, r.request_type, r.status, r.details, r.start_date, r.end_date " +
            "FROM requests r " +
            "JOIN employees e ON r.empNo = e.empNo " +
            "WHERE 1=1 "
        );

        if (!empNoSearch.isEmpty()) sql.append("AND r.empNo LIKE ? ");
        if (!"All".equals(typeFilter)) sql.append("AND r.request_type = ? ");
        if (!"All".equals(statusFilter)) sql.append("AND r.status = ? ");

        sql.append("ORDER BY r.submitted_date DESC");

        if (empNoSearch.isEmpty()) sql.append(" LIMIT 50"); // Limit for performance if no search

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (!empNoSearch.isEmpty()) ps.setString(index++, "%" + empNoSearch + "%");
            if (!"All".equals(typeFilter)) ps.setString(index++, typeFilter);
            if (!"All".equals(statusFilter)) ps.setString(index++, statusFilter);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        false,                          // 0: Select
                        rs.getInt("id"),                // 1: ID (Hidden)
                        rs.getString("submitted_date"), // 2
                        rs.getString("name"),           // 3
                        rs.getString("empNo"),          // 4
                        rs.getString("request_type"),   // 5
                        rs.getString("status"),         // 6
                        "View",                         // 7: Button
                        rs.getString("details"),        // 8: Hidden
                        rs.getString("start_date"),     // 9: Hidden
                        rs.getString("end_date")        // 10: Hidden
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching data: " + e.getMessage());
        }
    }

    /**
     * Optimized Bulk Process: Reads directly from hidden table columns.
     * No extra DB queries per row.
     */
    private void bulkProcess(String action) {
        if (table.getRowCount() == 0) return;

        List<Integer> rowsToProcess = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if ((Boolean) model.getValueAt(i, 0)) { // Checked
                rowsToProcess.add(i);
            }
        }

        if (rowsToProcess.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one request.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to " + action.toLowerCase() + " " + rowsToProcess.size() + " items?", 
            "Confirm", JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) return;

        int successCount = 0;
        for (int row : rowsToProcess) {
            try {
                // Fetch data from hidden model columns
                int id = (int) model.getValueAt(row, 1);
                String empNo = (String) model.getValueAt(row, 4);
                String type = (String) model.getValueAt(row, 5);
                String details = (String) model.getValueAt(row, 8); // Hidden
                String start = (String) model.getValueAt(row, 9);   // Hidden
                String end = (String) model.getValueAt(row, 10);    // Hidden

                Database.processRequestApproval(id, action, empNo, type, details, start, end);
                successCount++;
            } catch (Exception e) {
                System.err.println("Error processing row " + row);
            }
        }

        JOptionPane.showMessageDialog(this, "Successfully " + action.toLowerCase() + " " + successCount + " requests.");
        fetchRequests();
    }

    private void viewRequestDetails(int id) {
        RequestDetailsDialog dialog = new RequestDetailsDialog(id);
        dialog.setVisible(true);
        fetchRequests();
    }

    // --- Renderers & Editors ---

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
                        int id = (int) table.getValueAt(row, 1); // ID is index 1
                        viewRequestDetails(id);
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
            
            String status = (String) table.getValueAt(row, 6); // Status is index 6
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

    // --- Details Dialog ---
    private class RequestDetailsDialog extends JDialog {
        public RequestDetailsDialog(int id) {
            setTitle("Request Details");
            setModal(true);
            setSize(500, 450);
            setLocationRelativeTo(null);
            setLayout(null);

            try (ResultSet rs = Database.getRequestWithEmployeeDetailsById(id)) {
                if (rs != null && rs.next()) {
                    int requestId = rs.getInt("id");
                    String empNoVal = rs.getString("empNo");
                    String name = rs.getString("name");
                    String requestType = rs.getString("request_type");
                    String details = rs.getString("details");
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
                    addLabel("Type: " + requestType, 140, y, Color.RED); y += 30;
                    addLabel("Details: " + details, 140, y, Color.RED); y += 30;
                    addLabel("Reason: " + reason, 140, y, Color.RED); y += 30;
                    addLabel("Start: " + startDate, 140, y, Color.RED); y += 30;
                    if(endDate != null) { addLabel("End: " + endDate, 140, y, Color.RED); y += 30; }
                    addLabel("Submitted: " + submittedDate, 140, y, Color.BLACK); y += 40;

                    JButton approve = new JButton("Approve");
                    approve.setBounds(140, y, 100, 30);
                    approve.setBackground(Color.GREEN);
                    approve.setForeground(Color.WHITE);
                    approve.addActionListener(e -> {
                        Database.processRequestApproval(requestId, "Approved", empNoVal, requestType, details, startDate, endDate);
                        JOptionPane.showMessageDialog(this, "Approved!");
                        dispose();
                    });
                    add(approve);

                    JButton reject = new JButton("Reject");
                    reject.setBounds(260, y, 100, 30);
                    reject.setBackground(Color.RED);
                    reject.setForeground(Color.WHITE);
                    reject.addActionListener(e -> {
                        Database.processRequestApproval(requestId, "Rejected", empNoVal, requestType, details, startDate, endDate);
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