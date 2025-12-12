package Fproj;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Properties;

public class AdminRecords extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    // --- Colors & Fonts ---
    private final Color BRAND_COLOR = new Color(22, 102, 87);
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    public AdminRecords() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- 1. Top Panel: Title & Actions ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(BACKGROUND_COLOR);

        // Title
        JLabel lblTitle = new JLabel("Employee Records");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(BRAND_COLOR);
        topPanel.add(lblTitle, BorderLayout.WEST);

        // Action Panel (Search + Register)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(BACKGROUND_COLOR);

        // NEW: Review Applications Button
        JButton btnReview = new JButton("Review Applications");
        styleButton(btnReview, new Color(70, 130, 180)); // Blue
        btnReview.addActionListener(e -> new ReviewRequestsDialog().setVisible(true));

        // Register Button
        JButton btnRegister = new JButton("+ Register New");
        styleButton(btnRegister, BRAND_COLOR);
        btnRegister.addActionListener(e -> {
            new RegistrationDialog(null, null).setVisible(true);
        });

        // Search Section
        JLabel lblSearch = new JLabel("Search Emp No:");
        lblSearch.setFont(MAIN_FONT);
        
        txtSearch = new JTextField(15);
        txtSearch.putClientProperty("JTextField.placeholderText", "Enter ID...");

        JButton btnSearch = new JButton("Search");
        styleButton(btnSearch, BRAND_COLOR); // Dark Gray for secondary action
        btnSearch.addActionListener(e -> searchEmployee());

        actionPanel.add(btnReview);
        actionPanel.add(Box.createHorizontalStrut(10));
        actionPanel.add(btnRegister);
        actionPanel.add(Box.createHorizontalStrut(20)); // Spacer
        actionPanel.add(lblSearch);
        actionPanel.add(txtSearch);
        actionPanel.add(btnSearch);

        topPanel.add(actionPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- 2. Table Setup ---
        // Columns include both Edit (8) and Delete (9)
        String[] cols = {
                "Emp No", "Name", "Address", "Email",
                "Contact", "Position", "Status", "Daily Pay", "Edit", "Delete"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Allow clicking Edit (8) and Delete (9)
                return column == 8 || column == 9; 
            }
        };

        table = new JTable(model);
        styleTable(); 

        // Set up Edit column (False = Blue/Edit Style)
        table.getColumn("Edit").setCellRenderer(new ButtonRenderer(false));
        table.getColumn("Edit").setCellEditor(new EditButtonEditor(new JCheckBox()));
        table.getColumn("Edit").setMaxWidth(70);
        table.getColumn("Edit").setMinWidth(70);

        // Set up Delete column (True = Red/Delete Style)
        table.getColumn("Delete").setCellRenderer(new ButtonRenderer(true));
        table.getColumn("Delete").setCellEditor(new DeleteButtonEditor(new JCheckBox()));
        table.getColumn("Delete").setMaxWidth(80);
        table.getColumn("Delete").setMinWidth(80);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(scroll, BorderLayout.CENTER);

        // Load Data
        loadTable();
    }
   
    private void styleTable() {
        table.setRowHeight(35);
        table.setFont(MAIN_FONT);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(22, 102, 87, 50));
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(BRAND_COLOR);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setPreferredSize(new Dimension(0, 40));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        // Loop through all columns but SKIP the button columns (8 and 9)
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 8 && i != 9) { 
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void loadTable() {
        model.setRowCount(0);
        String sql = "SELECT empNo, name, address, email, contact, position, employmentStatus, dailyPay FROM employees ORDER BY empNo";

        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("empNo"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("email"),
                        rs.getString("contact"),
                        rs.getString("position"),
                        rs.getString("employmentStatus"),
                        rs.getInt("dailyPay"),
                        "Edit",   
                        "Delete"  
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading employee data: " + e.getMessage());
        }
    }

    private void searchEmployee() {
        String search = txtSearch.getText().trim();
        if (search.isEmpty()) {
            loadTable(); 
            return;
        }

        model.setRowCount(0);
        String sql = "SELECT empNo, name, address, email, contact, position, employmentStatus, dailyPay FROM employees WHERE empNo LIKE ?";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + search + "%");
            ResultSet rs = pstmt.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                model.addRow(new Object[]{
                        rs.getString("empNo"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("email"),
                        rs.getString("contact"),
                        rs.getString("position"),
                        rs.getString("employmentStatus"),
                        rs.getInt("dailyPay"),
                        "Edit",
                        "Delete"
                });
            }
            
            if (!found) {
                JOptionPane.showMessageDialog(this, "No employee found with that ID.");
                loadTable();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    //                        NEW: REVIEW REQUESTS DIALOG
    // =========================================================================
    private class ReviewRequestsDialog extends JDialog {
        private JTable reqTable;
        private DefaultTableModel reqModel;

        public ReviewRequestsDialog() {
            super((Frame) SwingUtilities.getWindowAncestor(AdminRecords.this), "Review Account Applications", true);
            setSize(700, 450);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());

            // Table
            String[] cols = {"ID", "Name", "Email", "Date", "Resume Path"};
            reqModel = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            reqTable = new JTable(reqModel);
            reqTable.setRowHeight(30);
            reqTable.getTableHeader().setFont(HEADER_FONT);
            reqTable.getTableHeader().setBackground(BRAND_COLOR);
            reqTable.getTableHeader().setForeground(Color.WHITE);
            // Hide Path Column
            reqTable.getColumnModel().getColumn(4).setMinWidth(0);
            reqTable.getColumnModel().getColumn(4).setMaxWidth(0);
            reqTable.getColumnModel().getColumn(4).setWidth(0);
            reqTable.getColumnModel().getColumn(0).setMaxWidth(50); // ID width

            add(new JScrollPane(reqTable), BorderLayout.CENTER);

            // Buttons
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            
            JButton btnView = new JButton("View Resume");
            styleButton(btnView, new Color(23, 162, 184)); // Teal
            btnView.addActionListener(e -> viewResume());

            JButton btnApprove = new JButton("Approve (Register)");
            styleButton(btnApprove, new Color(40, 167, 69)); // Green
            btnApprove.addActionListener(e -> approveRequest());

            JButton btnReject = new JButton("Reject");
            styleButton(btnReject, new Color(220, 53, 69)); // Red
            btnReject.addActionListener(e -> rejectRequest());

            btnPanel.add(btnView);
            btnPanel.add(btnApprove);
            btnPanel.add(btnReject);
            add(btnPanel, BorderLayout.SOUTH);

            loadRequests();
        }

        private void loadRequests() {
            reqModel.setRowCount(0);
            try (ResultSet rs = Database.getAccountRequests()) {
                if(rs != null) {
                    while (rs.next()) {
                        reqModel.addRow(new Object[]{
                            rs.getInt("id"), rs.getString("full_name"), rs.getString("email"), 
                            rs.getString("request_date"), rs.getString("resume_path")
                        });
                    }
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }

        private void viewResume() {
            int row = reqTable.getSelectedRow();
            if (row == -1) return;
            
            String pathStr = (String) reqTable.getValueAt(row, 4);
            try {
                File pdfFile = new File(pathStr);
                if (pdfFile.exists() && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                } else {
                    JOptionPane.showMessageDialog(this, "File not found or format unsupported.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage());
            }
        }

        private void approveRequest() {
            int row = reqTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select an applicant."); return; }

            int id = (int) reqTable.getValueAt(row, 0);
            String name = (String) reqTable.getValueAt(row, 1);
            String email = (String) reqTable.getValueAt(row, 2);

            // Open Registration with Pre-filled Data
            RegistrationDialog reg = new RegistrationDialog(name, email);
            reg.setVisible(true);

            int confirm = JOptionPane.showConfirmDialog(this, "Did you complete the registration?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                Database.deleteAccountRequest(id);
                loadRequests();
            }
        }

        private void rejectRequest() {
            int row = reqTable.getSelectedRow();
            if (row == -1) return;
            
            int confirm = JOptionPane.showConfirmDialog(this, "Reject and delete this application?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                int id = (int) reqTable.getValueAt(row, 0);
                Database.deleteAccountRequest(id);
                loadRequests();
            }
        }
    }

    // --- Table Button Renderers & Editors ---

    class ButtonRenderer extends JButton implements TableCellRenderer {
        private boolean isDelete;
        
        public ButtonRenderer(boolean isDelete) {
            this.isDelete = isDelete;
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 11));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isDelete) {
                setText("Delete");
                setBackground(new Color(220, 53, 69)); // Red
            } else {
                setText("Edit");
                setBackground(new Color(70, 130, 180)); // Blue
            }
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            return this;
        }
    }

    class EditButtonEditor extends DefaultCellEditor {
        private JButton button = new JButton();
        private boolean clicked;

        public EditButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button.setFocusPainted(false);
            button.setOpaque(true);
            button.addActionListener(e -> {
                if (clicked) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        String empNo = (String) table.getValueAt(row, 0);
                        EditEmployeeDialog dialog = new EditEmployeeDialog(empNo);
                        dialog.setVisible(true);
                        loadTable();
                    }
                }
                clicked = false;
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            button.setText("Edit");
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() { return "Edit"; }
        @Override
        public boolean stopCellEditing() { clicked = false; return super.stopCellEditing(); }
    }

    class DeleteButtonEditor extends DefaultCellEditor {
        private JButton button = new JButton();
        private boolean clicked;

        public DeleteButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button.setFocusPainted(false);
            button.setOpaque(true);
            button.addActionListener(e -> {
                // --- FIX: STOP EDITING FIRST ---
                // This tells the table "We are done clicking" BEFORE we delete anything.
                fireEditingStopped(); 

                if (clicked) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        String empNo = (String) table.getValueAt(row, 0);
                        String name = (String) table.getValueAt(row, 1);
                        
                        int confirm = JOptionPane.showConfirmDialog(
                            SwingUtilities.getWindowAncestor(AdminRecords.this),
                            "Delete employee " + name + " (" + empNo + ")?\nThis cannot be undone.",
                            "Confirm Deletion",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                        );
                        
                        if (confirm == JOptionPane.YES_OPTION) {
                            if (Database.deleteEmployee(empNo)) {
                                JOptionPane.showMessageDialog(null, "Employee deleted successfully.");
                                loadTable(); // Now it's safe to reload
                            } else {
                                JOptionPane.showMessageDialog(null, "Failed to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
                clicked = false;
                // fireEditingStopped(); // <--- REMOVE THIS FROM THE BOTTOM
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            button.setText("Delete");
            button.setBackground(new Color(220, 53, 69)); 
            button.setForeground(Color.WHITE);
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() { return "Delete"; }
        
        // Remove the override for stopCellEditing, or keep it standard:
        @Override
        public boolean stopCellEditing() { 
            clicked = false; 
            return super.stopCellEditing(); 
        }
    }

    // --- DIALOGS (Styled) ---

    private class EditEmployeeDialog extends JDialog {
        private JComboBox<String> cbPosition, cbStatus;
        private JTextField txtDailyPay;
        private JButton btnSave;
        private String empNo;

        public EditEmployeeDialog(String empNo) {
            this.empNo = empNo;
            setTitle("Edit Employee Details");
            setModal(true);
            setSize(400, 350);
            setLocationRelativeTo(null);

            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(Color.WHITE);
            p.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;

            // Header
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            JLabel lblHeader = new JLabel("Update Info for: " + empNo);
            lblHeader.setFont(HEADER_FONT);
            lblHeader.setForeground(BRAND_COLOR);
            p.add(lblHeader, gbc);

            // Position
            gbc.gridy++; gbc.gridwidth = 1;
            p.add(new JLabel("Position:"), gbc);
            
            gbc.gridx = 1;
            cbPosition = new JComboBox<>();
            cbPosition.setEditable(true);
            cbPosition.setBackground(Color.WHITE);
            loadPositions();
            p.add(cbPosition, gbc);

            // Status
            gbc.gridx = 0; gbc.gridy++;
            p.add(new JLabel("Status:"), gbc);
            
            gbc.gridx = 1;
            cbStatus = new JComboBox<>(new String[]{"Regular", "Probationary", "Contractual"});
            cbStatus.setBackground(Color.WHITE);
            p.add(cbStatus, gbc);

            // Daily Pay
            gbc.gridx = 0; gbc.gridy++;
            p.add(new JLabel("Daily Pay:"), gbc);
            
            gbc.gridx = 1;
            txtDailyPay = new JTextField();
            p.add(txtDailyPay, gbc);

            // Save Button
            gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
            btnSave = new JButton("Save Changes");
            styleButton(btnSave, BRAND_COLOR);
            btnSave.addActionListener(e -> saveChanges());
            p.add(btnSave, gbc);

            add(p);
            loadCurrentData();
        }

        private void loadCurrentData() {
            String sql = "SELECT position, employmentStatus, dailyPay FROM employees WHERE empNo = ?";
            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, empNo);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    cbPosition.setSelectedItem(rs.getString("position"));
                    cbStatus.setSelectedItem(rs.getString("employmentStatus"));
                    txtDailyPay.setText(String.valueOf(rs.getInt("dailyPay")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void loadPositions() {
            try (Connection conn = Database.connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DISTINCT position FROM employees")) {
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                while (rs.next()) {
                    model.addElement(rs.getString("position"));
                }
                cbPosition.setModel(model);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void saveChanges() {
            String position = cbPosition.getSelectedItem().toString().trim();
            String status = cbStatus.getSelectedItem().toString();
            String dailyPayStr = txtDailyPay.getText().trim();

            if (position.isEmpty() || dailyPayStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            try {
                int dailyPay = Integer.parseInt(dailyPayStr);
                if (Database.updateEmployeeDetails(empNo, position, status, dailyPay)) {
                    JOptionPane.showMessageDialog(this, "Employee details updated successfully!");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Error updating employee details.");
                }
            } catch (NumberFormatException e) {
                 JOptionPane.showMessageDialog(this, "Daily Pay must be a valid number.");
            }
        }
    }

    private class RegistrationDialog extends JDialog {

        private JTextField txtName, txtAddress, txtEmail, txtContact, txtSalaryRate;
        private JComboBox<String> cbRole, cbPosition, cbStatus;
        private JLabel lblEmpNumber, lblPhotoDisplay, lblEmpNoLabel; 
        private JButton btnUploadPhoto, btnAddPos, btnRemovePos, btnSave;
        private String imagePath = "";
        private int empCounter = Database.getLastEmployeeNumber() + 1;
        private final String[] STAFF_POSITIONS = {"HR Officer", "Accountant"};

        // Constructor with optional pre-fill data (for Review Requests)
        public RegistrationDialog(String preName, String preEmail) {
            super((Frame) null, "Register New Account", true);
            setSize(850, 650);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
            mainPanel.setBackground(Color.WHITE);
            mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

            // --- FIXED PHOTO PANEL (GridBagLayout) ---
            JPanel photoPanel = new JPanel(new GridBagLayout()); 
            photoPanel.setBackground(Color.WHITE);
            photoPanel.setBorder(BorderFactory.createTitledBorder("Profile Photo"));
            photoPanel.setPreferredSize(new Dimension(200, 0));

            GridBagConstraints gbcPhoto = new GridBagConstraints();
            gbcPhoto.gridx = 0; 
            gbcPhoto.gridy = 0;
            gbcPhoto.insets = new Insets(10, 10, 15, 10);
            gbcPhoto.anchor = GridBagConstraints.CENTER;

            lblPhotoDisplay = new JLabel("No photo", SwingConstants.CENTER);
            lblPhotoDisplay.setPreferredSize(new Dimension(150, 150));
            lblPhotoDisplay.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            photoPanel.add(lblPhotoDisplay, gbcPhoto);

            gbcPhoto.gridy = 1;
            gbcPhoto.insets = new Insets(0, 10, 10, 10);
            btnUploadPhoto = new JButton("Upload");
            styleButton(btnUploadPhoto, BRAND_COLOR);
            btnUploadPhoto.setPreferredSize(new Dimension(100, 30));
            btnUploadPhoto.addActionListener(e -> uploadPhoto());
            photoPanel.add(btnUploadPhoto, gbcPhoto);
            mainPanel.add(photoPanel, BorderLayout.WEST);

            // --- FORM PANEL ---
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(Color.WHITE);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 10, 8, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;

            int row = 0;
            cbRole = new JComboBox<>(new String[]{"Employee", "Staff"});
            cbRole.setBackground(Color.WHITE);
            cbRole.addActionListener(e -> toggleRoleView());
            addRow(formPanel, gbc, row++, "Role:", cbRole);

            lblEmpNoLabel = new JLabel("Employee No.:");
            lblEmpNoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblEmpNumber = new JLabel(String.format("%03d", empCounter));
            lblEmpNumber.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblEmpNumber.setForeground(BRAND_COLOR);
            
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
            formPanel.add(lblEmpNoLabel, gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            formPanel.add(lblEmpNumber, gbc);
            row++;

            // Pre-fill fields if provided (for account requests)
            txtName = new JTextField(preName != null ? preName : "");
            addRow(formPanel, gbc, row++, "Full Name:", txtName);
            
            txtAddress = new JTextField(); 
            addRow(formPanel, gbc, row++, "Address:", txtAddress);
            
            txtEmail = new JTextField(preEmail != null ? preEmail : "");
            addRow(formPanel, gbc, row++, "Email:", txtEmail);
            
            txtContact = new JTextField(); 
            addRow(formPanel, gbc, row++, "Contact No.:", txtContact);

            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
            formPanel.add(new JLabel("Position:"), gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            JPanel posPanel = new JPanel(new BorderLayout(5, 0));
            posPanel.setBackground(Color.WHITE);
            cbPosition = new JComboBox<>(); cbPosition.setEditable(true); cbPosition.setBackground(Color.WHITE);
            posPanel.add(cbPosition, BorderLayout.CENTER);

            JPanel miniBtnPan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            miniBtnPan.setBackground(Color.WHITE);
            btnAddPos = new JButton("+"); btnAddPos.setPreferredSize(new Dimension(40, 25));
            btnRemovePos = new JButton("-"); btnRemovePos.setPreferredSize(new Dimension(40, 25));
            btnAddPos.addActionListener(e -> addPosition()); btnRemovePos.addActionListener(e -> removePosition());
            miniBtnPan.add(btnAddPos); miniBtnPan.add(btnRemovePos); posPanel.add(miniBtnPan, BorderLayout.EAST);
            formPanel.add(posPanel, gbc); row++;

            cbStatus = new JComboBox<>(new String[]{"Regular", "Probationary", "Contractual"});
            cbStatus.setBackground(Color.WHITE); cbStatus.addActionListener(e -> updateSalaryRate());
            addRow(formPanel, gbc, row++, "Emp. Status:", cbStatus);

            txtSalaryRate = new JTextField();
            addRow(formPanel, gbc, row++, "Salary Rate:", txtSalaryRate);

            mainPanel.add(formPanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(); buttonPanel.setBackground(Color.WHITE);
            btnSave = new JButton("Complete Registration"); styleButton(btnSave, BRAND_COLOR); btnSave.setPreferredSize(new Dimension(200, 40));
            btnSave.addActionListener(e -> register()); buttonPanel.add(btnSave);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            add(mainPanel);
            loadPositions(); toggleRoleView();
        }

        private void toggleRoleView() {
            boolean isStaff = "Staff".equals(cbRole.getSelectedItem());
            lblEmpNoLabel.setVisible(!isStaff); lblEmpNumber.setVisible(!isStaff);
            cbStatus.setEnabled(!isStaff); txtSalaryRate.setEnabled(!isStaff);
            if(isStaff) txtSalaryRate.setText("N/A"); else updateSalaryRate();
            cbPosition.removeAllItems();
            if (isStaff) {
                for (String pos : STAFF_POSITIONS) cbPosition.addItem(pos);
                cbPosition.setEditable(false); btnAddPos.setVisible(false); btnRemovePos.setVisible(false);
            } else {
                loadPositions(); cbPosition.setEditable(true); btnAddPos.setVisible(true); btnRemovePos.setVisible(true);
            }
        }

        // --- UPDATED REGISTRATION LOGIC WITH VALIDATION ---
        private void register() {
            String name = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            String contact = txtContact.getText().trim();

            if (name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill required fields.");
                return;
            }

            // 1. VALIDATION: Full Name (Must have at least 2 words)
            if (!name.contains(" ") || name.split("\\s+").length < 2) {
                JOptionPane.showMessageDialog(this, "Please enter full name (First & Last Name).", "Invalid Name", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. VALIDATION: Email (Must end with @gmail.com)
            if (!email.toLowerCase().endsWith("@gmail.com")) {
                JOptionPane.showMessageDialog(this, "Only @gmail.com addresses are allowed.", "Invalid Email", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 3. VALIDATION: Contact (Must be exactly 11 digits)
            if (!contact.matches("\\d{11}")) {
                JOptionPane.showMessageDialog(this, "Contact number must be exactly 11 digits.", "Invalid Contact", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                String password = generateRandomPassword(10);
                String username = "";

                if ("Staff".equalsIgnoreCase(cbRole.getSelectedItem().toString())) {
                    String clean = name.replaceAll("\\s+", "").toLowerCase();
                    String pos = cbPosition.getSelectedItem().toString();
                    username = ("HR Officer".equals(pos) ? "hr_" : ("Accountant".equals(pos) ? "pay_" : "staff_")) + clean;
                    Database.insertASRecord(name, cbRole.getSelectedItem().toString().toLowerCase(), username, password, txtAddress.getText(), email, contact, pos);
                } else {
                    username = lblEmpNumber.getText();
                    int salary = 0; try { salary = Integer.parseInt(txtSalaryRate.getText().trim()); } catch(Exception e){}
                    Database.insertEmployeeFull(username, name, "employee", txtAddress.getText(), email, contact, cbPosition.getSelectedItem().toString(), cbStatus.getSelectedItem().toString(), salary, password, imagePath);
                }

                sendEmail(email, name, username, password);
                JOptionPane.showMessageDialog(this, "Registration Successful!\nUsername: " + username + "\nCredentials sent to email.");
                dispose(); loadTable();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }

        // --- Helper Methods ---
        private void addRow(JPanel p, GridBagConstraints g, int r, String l, JComponent c) { g.gridx=0; g.gridy=r; g.weightx=0; p.add(new JLabel(l), g); g.gridx=1; g.weightx=1; c.setPreferredSize(new Dimension(200, 30)); p.add(c, g); }
        private String generateEmpNo() { return String.format("%03d", empCounter); }
        private void loadPositions() { try (Connection c=Database.connect(); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT DISTINCT position FROM employees")){ DefaultComboBoxModel<String> m=new DefaultComboBoxModel<>(); while(r.next()) m.addElement(r.getString("position")); cbPosition.setModel(m); } catch(Exception e){} }
        private void addPosition() { String n=cbPosition.getEditor().getItem().toString().trim(); if(!n.isEmpty()){ ((DefaultComboBoxModel<String>)cbPosition.getModel()).addElement(n); cbPosition.setSelectedItem(n); } }
        private void removePosition() { if(cbPosition.getSelectedItem()!=null) ((DefaultComboBoxModel<String>)cbPosition.getModel()).removeElement(cbPosition.getSelectedItem()); }
        private void updateSalaryRate() { String s=(String)cbStatus.getSelectedItem(); txtSalaryRate.setText(("Probationary".equalsIgnoreCase(s)||"Contractual".equalsIgnoreCase(s))?"600":"650"); txtSalaryRate.setEditable(!"Probationary".equalsIgnoreCase(s)&&!"Contractual".equalsIgnoreCase(s)); }
        private void uploadPhoto() { JFileChooser f=new JFileChooser(); f.setFileFilter(new FileNameExtensionFilter("Images","jpg","png")); if(f.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){ try { Path dest=Paths.get("employee_photos","emp_"+System.currentTimeMillis()+".jpg"); if(!Files.exists(dest.getParent())) Files.createDirectories(dest.getParent()); Files.copy(f.getSelectedFile().toPath(), dest, StandardCopyOption.REPLACE_EXISTING); imagePath=dest.toString(); lblPhotoDisplay.setIcon(new ImageIcon(new ImageIcon(imagePath).getImage().getScaledInstance(150,150,Image.SCALE_SMOOTH))); lblPhotoDisplay.setText(""); } catch(IOException e){} } }
        private String generateRandomPassword(int l) { String chars="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%"; SecureRandom r=new SecureRandom(); StringBuilder sb=new StringBuilder(); for(int i=0;i<l;i++) sb.append(chars.charAt(r.nextInt(chars.length()))); return sb.toString(); }
        private void sendEmail(String t, String n, String u, String p) { 
            final String from="sh4wntolentino@gmail.com"; final String pwd="dkffdbkmlifnvows";
            Properties props = new Properties(); props.put("mail.smtp.auth", "true"); props.put("mail.smtp.starttls.enable", "true"); props.put("mail.smtp.host", "smtp.gmail.com"); props.put("mail.smtp.port", "587");
            Session session = Session.getInstance(props, new Authenticator() { protected PasswordAuthentication getPasswordAuthentication() { return new PasswordAuthentication(from, pwd); } });
            try { Message msg = new MimeMessage(session); msg.setFrom(new InternetAddress(from)); msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(t)); msg.setSubject("Your Account Credentials"); msg.setText("Hello " + n + ",\n\nUsername: " + u + "\nPassword: " + p + "\n\nPlease change your password upon login."); Transport.send(msg); } catch (MessagingException e) { e.printStackTrace(); }
        }
    }
}