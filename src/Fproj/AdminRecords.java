package Fproj;

import javax.swing.JComponent;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class AdminRecords extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    // --- Colors & Fonts (added for styling) ---
    private final Color BRAND_COLOR = new Color(22, 102, 87);
    private final Font FONT_VAL = new Font("Segoe UI", Font.BOLD, 14);

    public AdminRecords() {
        setLayout(null);
        setBackground(new Color(240, 240, 240));

        JLabel lblTitle = new JLabel("Employee Records");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setBounds(20, 10, 200, 30);
        add(lblTitle);

        JLabel lblSearch = new JLabel("Search by Emp No:");
        lblSearch.setBounds(427, 50, 150, 25);
        add(lblSearch);

        txtSearch = new JTextField();
        txtSearch.setBounds(544, 50, 150, 25);
        add(txtSearch);

        JButton btnSearch = new JButton("Search");
        btnSearch.setBounds(700, 50, 100, 25);
        add(btnSearch);

        String[] cols = {
                "Employee No", "Name", "Address", "Email",
                "Contact", "Position", "Status", "Daily Pay", "Edit"
        };

        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);

        // Set up Edit column
        table.getColumn("Edit").setCellRenderer(new ButtonRenderer());
        table.getColumn("Edit").setCellEditor(new EditButtonEditor(new javax.swing.JCheckBox()));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(20, 90, 780, 330);
        add(scroll);

        loadTable();

        JButton btnRegister = new JButton("Register");
        styleButton(btnRegister);
        btnRegister.setBounds(20, 50, 100, 25);
        btnRegister.addActionListener(e -> {
            RegistrationDialog dialog = new RegistrationDialog();
            dialog.setVisible(true);
        });
        add(btnRegister);

        btnSearch.addActionListener(e -> searchEmployee());
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
                        "Edit"
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
            JOptionPane.showMessageDialog(this, "Enter Employee Number!");
            return;
        }

        model.setRowCount(0);

        String sql = "SELECT empNo, name, address, email, contact, position, employmentStatus, dailyPay FROM employees WHERE empNo = ?";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, search);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("empNo"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("email"),
                        rs.getString("contact"),
                        rs.getString("position"),
                        rs.getString("employmentStatus"),
                        rs.getInt("dailyPay"),
                        "Edit"
                });
            } else {
                JOptionPane.showMessageDialog(this, "Employee not found.");
                loadTable();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- STYLING HELPER (added for consistency) ---
    private void styleButton(JButton btn) {
        btn.setBackground(BRAND_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    // Button Renderer
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // Edit Button Editor
    class EditButtonEditor extends javax.swing.DefaultCellEditor {
        private JButton button = new JButton();
        private String label;
        private boolean clicked;

        public EditButtonEditor(javax.swing.JCheckBox checkBox) {
            super(checkBox);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(true);
            button.addActionListener(e -> {
                if (clicked) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        String empNo = (String) table.getValueAt(row, 0);
                        EditEmployeeDialog dialog = new EditEmployeeDialog(empNo);
                        dialog.setVisible(true);
                        loadTable(); // Refresh after edit
                    }
                }
                clicked = false;
                fireEditingStopped();
            });
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }

    // Edit Employee Dialog (UPDATED UI)
    private class EditEmployeeDialog extends JDialog {
        private JComboBox<String> cbPosition, cbStatus;
        private JTextField txtDailyPay;
        private JButton btnSave;
        private String empNo;

        public EditEmployeeDialog(String empNo) {
            this.empNo = empNo;
            setTitle("Edit Employee Details");
            setModal(true);
            setSize(420, 350); // Adjusted size for new layout
            setLocationRelativeTo(null);

            // UPDATED: Use GridLayout panel like adProfile
            JPanel p = new JPanel(new java.awt.GridLayout(4, 2, 10, 15)); // 3 fields + button
            p.setBackground(Color.WHITE);
            p.setBorder(new EmptyBorder(30, 30, 30, 30));

            // Position
            p.add(new JLabel("Position:"));
            cbPosition = new JComboBox<>();
            cbPosition.setEditable(true);
            loadPositions(); // Load positions first
            p.add(cbPosition);

            // Status
            p.add(new JLabel("Status:"));
            cbStatus = new JComboBox<>(new String[]{"Regular", "Probationary", "Contractual"});
            p.add(cbStatus);

            // Daily Pay
            p.add(new JLabel("Daily Pay:"));
            txtDailyPay = new JTextField();
            p.add(txtDailyPay);

            // Save Button
            p.add(new JLabel("")); // Empty cell
            btnSave = new JButton("Save Changes");
            styleButton(btnSave); // Apply styling
            btnSave.addActionListener(e -> saveChanges());
            p.add(btnSave);

            add(p);

            // Load current data AFTER components exist
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

            if (!isNumeric(dailyPayStr)) {
                JOptionPane.showMessageDialog(this, "Daily Pay must be a valid number.");
                return;
            }

            int dailyPay = Integer.parseInt(dailyPayStr);

            if (Database.updateEmployeeDetails(empNo, position, status, dailyPay)) {
                JOptionPane.showMessageDialog(this, "Employee details updated successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error updating employee details.");
            }
        }

        private boolean isNumeric(String str) {
            try {
                Integer.parseInt(str);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }


    private class RegistrationDialog extends JDialog {

        private JTextField txtName, txtAddress, txtEmail, txtContact, txtSalaryRate;
        private JComboBox<String> cbRole, cbPosition, cbStatus;
        private JLabel lblEmpNumber, lblPhotoDisplay;
        private JButton btnUploadPhoto, btnAddPos, btnRemovePos, btnSave;
        private String imagePath = "";
        private int empCounter = Database.getLastEmployeeNumber() + 1;

        public RegistrationDialog() {
            super((Frame) null, "Registration", true); // <-- fixes constructor error
            setSize(750, 600);
            setLocationRelativeTo(null);

            // ================= MAIN PANEL =================
            JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
            mainPanel.setBackground(Color.WHITE);
            mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

            // ================= LEFT PHOTO PANEL =================
            JPanel photoPanel = new JPanel();
            photoPanel.setLayout(new BoxLayout(photoPanel, BoxLayout.Y_AXIS));
            photoPanel.setBackground(Color.WHITE);
            photoPanel.setBorder(BorderFactory.createTitledBorder("Profile Photo"));

            JPanel photoBox = new JPanel(new BorderLayout());
            photoBox.setPreferredSize(new Dimension(150, 150));
            photoBox.setMaximumSize(new Dimension(150, 150));
            photoBox.setBackground(Color.WHITE);
            photoBox.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

            lblPhotoDisplay = new JLabel("No photo", SwingConstants.CENTER);
            lblPhotoDisplay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            photoBox.add(lblPhotoDisplay, BorderLayout.CENTER);

            btnUploadPhoto = new JButton("Upload Photo");
            styleButton(btnUploadPhoto);
            btnUploadPhoto.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnUploadPhoto.addActionListener(e -> uploadPhoto());

            photoPanel.add(Box.createVerticalStrut(10));
            photoPanel.add(photoBox);
            photoPanel.add(Box.createVerticalStrut(10));
            photoPanel.add(btnUploadPhoto);
            photoPanel.add(Box.createVerticalGlue());

            mainPanel.add(photoPanel, BorderLayout.WEST);

            // ================= CENTER FORM PANEL =================
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(Color.WHITE);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0;

            int row = 0;

            // Employee No
            addRow(formPanel, gbc, row++, "Employee No.:", lblEmpNumber = new JLabel(generateEmpNo()));

            // Role
            cbRole = new JComboBox<>(new String[]{"Employee", "Staff"});
            cbRole.addActionListener(e -> togglePositionRole());
            addRow(formPanel, gbc, row++, "Role:", cbRole);

            // Full Name
            txtName = new JTextField();
            addRow(formPanel, gbc, row++, "Full Name:", txtName);

            // Address
            txtAddress = new JTextField();
            addRow(formPanel, gbc, row++, "Address:", txtAddress);

            // Email
            txtEmail = new JTextField();
            addRow(formPanel, gbc, row++, "Email:", txtEmail);

            // Contact
            txtContact = new JTextField();
            addRow(formPanel, gbc, row++, "Contact No.:", txtContact);

            // Position
            gbc.gridx = 0; gbc.gridy = row;
            formPanel.add(new JLabel("Position:"), gbc);

            gbc.gridx = 1;
            JPanel posPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            posPanel.setBackground(Color.WHITE);

            cbPosition = new JComboBox<>();
            cbPosition.setEditable(true);
            loadPositions();
            posPanel.add(cbPosition);

            btnAddPos = new JButton("+");
            btnAddPos.addActionListener(e -> addPosition());
            posPanel.add(btnAddPos);

            btnRemovePos = new JButton("-");
            btnRemovePos.addActionListener(e -> removePosition());
            posPanel.add(btnRemovePos);

            formPanel.add(posPanel, gbc);
            row++;

            // Status
            cbStatus = new JComboBox<>(new String[]{"Regular", "Probationary", "Contractual"});
            cbStatus.addActionListener(e -> updateSalaryRate());
            addRow(formPanel, gbc, row++, "Emp. Status:", cbStatus);

            // Salary
            txtSalaryRate = new JTextField();
            addRow(formPanel, gbc, row++, "Salary Rate:", txtSalaryRate);

            mainPanel.add(formPanel, BorderLayout.CENTER);

            // ================= SOUTH BUTTON PANEL =================
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.WHITE);

            btnSave = new JButton("Register Employee");
            styleButton(btnSave);
            btnSave.setPreferredSize(new Dimension(200, 35));
            btnSave.addActionListener(e -> register());

            buttonPanel.add(btnSave);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            add(mainPanel);

            togglePositionRole();
            updateSalaryRate();
        }

        // =========================================================
        // UI HELPER
        // =========================================================
        private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0;
            panel.add(new JLabel(label), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            panel.add(comp, gbc);
        }

        // =========================================================
        // ===== YOUR ORIGINAL LOGIC (UNCHANGED)
        // =========================================================

        private String generateEmpNo() {
            return String.format("%03d", empCounter);
        }

        private void togglePositionRole() {
            boolean isStaff = "Staff".equals(cbRole.getSelectedItem());
            cbPosition.setEnabled(!isStaff);
            btnAddPos.setEnabled(!isStaff);
            btnRemovePos.setEnabled(!isStaff);
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

        private void addPosition() {
            String newPos = cbPosition.getEditor().getItem().toString().trim();
            if (newPos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a position to add.");
                return;
            }

            DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cbPosition.getModel();
            for (int i = 0; i < model.getSize(); i++) {
                if (model.getElementAt(i).equalsIgnoreCase(newPos)) {
                    JOptionPane.showMessageDialog(this, "Position already exists.");
                    return;
                }
            }

            model.addElement(newPos);
            cbPosition.setSelectedItem(newPos);
        }

        private void removePosition() {
            DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cbPosition.getModel();
            Object selected = cbPosition.getSelectedItem();

            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Select a position to remove.");
                return;
            }

            int index = model.getIndexOf(selected);
            if (index != -1) {
                model.removeElementAt(index);
            }
        }

        private int getSalaryRate(String status) {
            switch (status) {
                case "Regular": return 650;
                case "Probationary":
                case "Contractual": return 600;
                default: return 0;
            }
        }

        private void updateSalaryRate() {
            String status = (String) cbStatus.getSelectedItem();
            int salary;

            if ("Probationary".equalsIgnoreCase(status) || "Contractual".equalsIgnoreCase(status)) {
                salary = 600;
                txtSalaryRate.setText(String.valueOf(salary));
                txtSalaryRate.setEditable(false);
            } else {
                salary = getSalaryRate(status);
                txtSalaryRate.setText(String.valueOf(salary));
                txtSalaryRate.setEditable(true);
            }
        }

        private boolean isNumeric(String str) {
            try {
                Integer.parseInt(str);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        private void uploadPhoto() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Employee Photo");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            FileNameExtensionFilter filter =
                    new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif");
            fileChooser.setFileFilter(filter);

            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile != null) {
                    try {
                        Path photoDir = Paths.get("employee_photos");
                        if (!Files.exists(photoDir)) {
                            Files.createDirectories(photoDir);
                        }

                        String empNo = lblEmpNumber.getText();
                        String timestamp = String.valueOf(System.currentTimeMillis());
                        String extension = getFileExtension(selectedFile.getName());
                        String newFileName = "emp_" + empNo + "_" + timestamp + "." + extension;

                        Path destination = photoDir.resolve(newFileName);
                        Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                        imagePath = destination.toString();
                        ImageIcon icon = new ImageIcon(imagePath);
                        Image scaled = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);

                        lblPhotoDisplay.setIcon(new ImageIcon(scaled));
                        lblPhotoDisplay.setText("");

                        JOptionPane.showMessageDialog(this, "Photo uploaded successfully!");

                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Error uploading photo: " + ex.getMessage());
                    }
                }
            }
        }

        private String getFileExtension(String fileName) {
            int lastDot = fileName.lastIndexOf('.');
            return (lastDot > 0) ? fileName.substring(lastDot + 1).toLowerCase() : "jpg";
        }

        private boolean isUniqueName(String name) {
            String sqlEmp = "SELECT 1 FROM employees WHERE LOWER(name) = LOWER(?)";
            String sqlStaff = "SELECT 1 FROM as_records WHERE LOWER(name) = LOWER(?)";

            try (Connection conn = Database.connect();
                 PreparedStatement pstmtEmp = conn.prepareStatement(sqlEmp);
                 PreparedStatement pstmtStaff = conn.prepareStatement(sqlStaff)) {

                pstmtEmp.setString(1, name);
                try (ResultSet rsEmp = pstmtEmp.executeQuery()) {
                    if (rsEmp.next()) return false;
                }

                pstmtStaff.setString(1, name);
                try (ResultSet rsStaff = pstmtStaff.executeQuery()) {
                    if (rsStaff.next()) return false;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        }

        private void register() {
            String empNo = lblEmpNumber.getText();
            String name = txtName.getText().trim();
            String address = txtAddress.getText().trim();
            String email = txtEmail.getText().trim();
            String contact = txtContact.getText().trim();
            String role = cbRole.getSelectedItem().toString();
            String position = cbPosition.getSelectedItem().toString();
            String status = cbStatus.getSelectedItem().toString();
            String salaryStr = txtSalaryRate.getText().trim();

            if (name.isEmpty() || email.isEmpty() || salaryStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
                return;
            }

            if (!isNumeric(salaryStr)) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric salary.");
                return;
            }

            int salary = Integer.parseInt(salaryStr);

            if (!isUniqueName(name)) {
                JOptionPane.showMessageDialog(this, "This full name already exists.");
                return;
            }

            String password = generateRandomPassword(10);

            try {
                if ("Staff".equalsIgnoreCase(role)) {
                    Database.insertASRecord(name, role.toLowerCase(), empNo, password);
                } else {
                    Database.insertEmployeeFull(
                            empNo, name, role.toLowerCase(), address, email,
                            contact, position, status, salary, password, imagePath
                    );
                }

                sendEmail(email, name, empNo, password);

                JOptionPane.showMessageDialog(this, "Registration successful! Credentials sent to email.");
                dispose();
                loadTable();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error during registration: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private String generateRandomPassword(int length) {
            final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
            SecureRandom random = new SecureRandom();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            return sb.toString();
        }

        private void sendEmail(String toEmail, String name, String username, String password)
                throws MessagingException {

            final String fromEmail = "sh4wntolentino@gmail.com";
            final String appPassword = "dkffdbkmlifnvows";

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, appPassword);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Your Account Credentials");
            message.setText("Hello " + name + ",\n\n" +
                    "Your account has been created.\n" +
                    "Username: " + username + "\n" +
                    "Password: " + password + "\n\n" +
                    "Please change your password after your first login.\n\n" +
                    "Regards,\nAdmin Team");

            Transport.send(message);
        }
    }

}
