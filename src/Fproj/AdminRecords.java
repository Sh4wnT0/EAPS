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

        // Register Button
        JButton btnRegister = new JButton("+ Register New");
        styleButton(btnRegister, BRAND_COLOR);
        btnRegister.addActionListener(e -> {
            RegistrationDialog dialog = new RegistrationDialog();
            dialog.setVisible(true);
        });

        // Search Section
        JLabel lblSearch = new JLabel("Search Emp No:");
        lblSearch.setFont(MAIN_FONT);
        
        txtSearch = new JTextField(15);
        txtSearch.putClientProperty("JTextField.placeholderText", "Enter ID...");

        JButton btnSearch = new JButton("Search");
        styleButton(btnSearch, BRAND_COLOR); // Dark Gray for secondary action
        btnSearch.addActionListener(e -> searchEmployee());

        actionPanel.add(btnRegister);
        actionPanel.add(Box.createHorizontalStrut(20)); // Spacer
        actionPanel.add(lblSearch);
        actionPanel.add(txtSearch);
        actionPanel.add(btnSearch);

        topPanel.add(actionPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- 2. Table Setup ---
        String[] cols = {
                "Employee No", "Name", "Address", "Email",
                "Contact", "Position", "Status", "Daily Pay", "Edit"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8; // Only "Edit" button is editable
            }
        };

        table = new JTable(model);
        styleTable(); // Apply visual styles

        // Set up Edit column
        table.getColumn("Edit").setCellRenderer(new ButtonRenderer());
        table.getColumn("Edit").setCellEditor(new EditButtonEditor(new JCheckBox()));
        table.getColumn("Edit").setMaxWidth(80);
        table.getColumn("Edit").setMinWidth(80);

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
        
        // Center align text in cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 8) { // Skip button column
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
            loadTable(); // Reset if empty
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
                        "Edit"
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

    // --- Table Button Renderers & Editors ---

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 11));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Edit");
            setForeground(Color.WHITE);
            setBackground(new Color(70, 130, 180)); // Orange for Edit
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
            button.setBackground(new Color(229, 149, 0));
            button.setForeground(Color.WHITE);
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() { return "Edit"; }
        @Override
        public boolean stopCellEditing() { clicked = false; return super.stopCellEditing(); }
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
        private JLabel lblEmpNumber, lblPhotoDisplay, lblEmpNoLabel; // Added reference to label
        private JButton btnUploadPhoto, btnAddPos, btnRemovePos, btnSave;
        private String imagePath = "";
        private int empCounter = Database.getLastEmployeeNumber() + 1;

        // Staff specific choices
        private final String[] STAFF_POSITIONS = {"HR Officer", "Accountant"};

        public RegistrationDialog() {
            super((Frame) null, "Register New Account", true);
            setSize(850, 650);
            setLocationRelativeTo(null);

            // Main Layout
            JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
            mainPanel.setBackground(Color.WHITE);
            mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

            // --- Left: Photo ---
            JPanel photoPanel = new JPanel();
            photoPanel.setLayout(new BoxLayout(photoPanel, BoxLayout.Y_AXIS));
            photoPanel.setBackground(Color.WHITE);
            photoPanel.setBorder(BorderFactory.createTitledBorder("Profile Photo"));

            lblPhotoDisplay = new JLabel("No photo", SwingConstants.CENTER);
            lblPhotoDisplay.setPreferredSize(new Dimension(150, 150));
            lblPhotoDisplay.setMaximumSize(new Dimension(150, 150));
            lblPhotoDisplay.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            
            btnUploadPhoto = new JButton("Upload");
            styleButton(btnUploadPhoto, BRAND_COLOR);
            btnUploadPhoto.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnUploadPhoto.addActionListener(e -> uploadPhoto());

            photoPanel.add(Box.createVerticalStrut(20));
            photoPanel.add(lblPhotoDisplay);
            photoPanel.add(Box.createVerticalStrut(15));
            photoPanel.add(btnUploadPhoto);
            mainPanel.add(photoPanel, BorderLayout.WEST);

            // --- Center: Form ---
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(Color.WHITE);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 10, 8, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0;

            int row = 0;
            
            // 1. Role Selection (First to trigger changes)
            cbRole = new JComboBox<>(new String[]{"Employee", "Staff"});
            cbRole.setBackground(Color.WHITE);
            cbRole.addActionListener(e -> toggleRoleView());
            addRow(formPanel, gbc, row++, "Role:", cbRole);

            // 2. Emp No (Visible only for Employee)
            lblEmpNoLabel = new JLabel("Employee No.:"); // Keep ref to toggle visibility
            lblEmpNoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            
            lblEmpNumber = new JLabel(generateEmpNo());
            lblEmpNumber.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblEmpNumber.setForeground(BRAND_COLOR);
            
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
            formPanel.add(lblEmpNoLabel, gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            formPanel.add(lblEmpNumber, gbc);
            row++;

            // 3. Common Fields
            txtName = new JTextField(); addRow(formPanel, gbc, row++, "Full Name:", txtName);
            txtAddress = new JTextField(); addRow(formPanel, gbc, row++, "Address:", txtAddress);
            txtEmail = new JTextField(); addRow(formPanel, gbc, row++, "Email:", txtEmail);
            txtContact = new JTextField(); addRow(formPanel, gbc, row++, "Contact No.:", txtContact);

            // 4. Position (Dynamic based on Role)
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
            formPanel.add(new JLabel("Position:"), gbc);

            gbc.gridx = 1; gbc.weightx = 1;
            JPanel posPanel = new JPanel(new BorderLayout(5, 0));
            posPanel.setBackground(Color.WHITE);

            cbPosition = new JComboBox<>();
            cbPosition.setEditable(true); // Default for Employee
            cbPosition.setBackground(Color.WHITE);
            posPanel.add(cbPosition, BorderLayout.CENTER);

            // +/- Buttons (Only for Employee)
            JPanel miniBtnPan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            miniBtnPan.setBackground(Color.WHITE);
            btnAddPos = new JButton("+"); btnAddPos.setPreferredSize(new Dimension(40, 25));
            btnRemovePos = new JButton("-"); btnRemovePos.setPreferredSize(new Dimension(40, 25));
            
            btnAddPos.addActionListener(e -> addPosition());
            btnRemovePos.addActionListener(e -> removePosition());
            
            miniBtnPan.add(btnAddPos);
            miniBtnPan.add(btnRemovePos);
            posPanel.add(miniBtnPan, BorderLayout.EAST);
            
            formPanel.add(posPanel, gbc);
            row++;

            // 5. Status & Pay (Only for Employee)
            cbStatus = new JComboBox<>(new String[]{"Regular", "Probationary", "Contractual"});
            cbStatus.setBackground(Color.WHITE);
            cbStatus.addActionListener(e -> updateSalaryRate());
            addRow(formPanel, gbc, row++, "Emp. Status:", cbStatus);

            txtSalaryRate = new JTextField();
            addRow(formPanel, gbc, row++, "Salary Rate:", txtSalaryRate);

            mainPanel.add(formPanel, BorderLayout.CENTER);

            // --- Bottom: Save ---
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.WHITE);
            btnSave = new JButton("Complete Registration");
            styleButton(btnSave, BRAND_COLOR);
            btnSave.setPreferredSize(new Dimension(200, 40));
            btnSave.addActionListener(e -> register());
            buttonPanel.add(btnSave);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            add(mainPanel);
            
            // Initial View Setup
            loadPositions(); // Load DB positions first
            toggleRoleView(); // Then set view state
        }

        // --- View Logic ---
        private void toggleRoleView() {
            boolean isStaff = "Staff".equals(cbRole.getSelectedItem());

            // 1. Hide/Show Emp No
            lblEmpNoLabel.setVisible(!isStaff);
            lblEmpNumber.setVisible(!isStaff);

            // 2. Hide/Show Salary & Status
            cbStatus.setEnabled(!isStaff);
            txtSalaryRate.setEnabled(!isStaff);
            if(isStaff) {
                txtSalaryRate.setText("N/A");
            } else {
                updateSalaryRate();
            }

            // 3. Change Position Dropdown
            cbPosition.removeAllItems();
            if (isStaff) {
                // Fixed choices for Staff
                for (String pos : STAFF_POSITIONS) cbPosition.addItem(pos);
                cbPosition.setEditable(false);
                btnAddPos.setVisible(false);
                btnRemovePos.setVisible(false);
            } else {
                // Dynamic from DB for Employee
                loadPositions();
                cbPosition.setEditable(true);
                btnAddPos.setVisible(true);
                btnRemovePos.setVisible(true);
            }
        }

        // --- Registration Logic ---
        private void register() {
            String name = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            String role = cbRole.getSelectedItem().toString();
            String position = cbPosition.getSelectedItem().toString();

            if (name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill required fields.");
                return;
            }

            try {
                String password = generateRandomPassword(10);
                String username = "";

                // A. STAFF REGISTRATION
                if ("Staff".equalsIgnoreCase(role)) {
                    // Generate Username Rule
                    // Remove spaces from name for username safety
                    String cleanName = name.replaceAll("\\s+", "").toLowerCase();
                    
                    if ("HR Officer".equals(position)) {
                        username = "hr_" + cleanName;
                    } else if ("Accountant".equals(position)) {
                        username = "pay_" + cleanName;
                    } else {
                        username = "staff_" + cleanName;
                    }

                    // Save to as_records (name, role, username, password)
                    // Note: You need to update Database.insertASRecord to accept address/contact too if you want them saved
                    // For now, using the method signature we have:
                    Database.insertASRecord(
                            name, 
                            role.toLowerCase(), 
                            username, 
                            password,
                            txtAddress.getText(),
                            email,
                            txtContact.getText(),
                            position // "HR Officer" or "Accountant"
                        );
                    
                    // IF you updated insertASRecord to take address/email/contact/position, use that instead.
                    // Assuming existing method: insertASRecord(name, role, username, password)
                    // You might want to update Database.java to store the extra details for staff too.
                    
                } 
                // B. EMPLOYEE REGISTRATION
                else {
                    username = lblEmpNumber.getText(); // EmpNo is username
                    int salary = 0;
                    try { salary = Integer.parseInt(txtSalaryRate.getText().trim()); } catch(Exception e){}

                    Database.insertEmployeeFull(
                            username, name, role.toLowerCase(), txtAddress.getText(), email,
                            txtContact.getText(), position, 
                            cbStatus.getSelectedItem().toString(), salary, password, imagePath
                    );
                }

                sendEmail(email, name, username, password);
                JOptionPane.showMessageDialog(this, "Registration Successful!\nUsername: " + username + "\nCredentials sent to email.");
                dispose();
                loadTable();
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }

        // --- Helper Methods ---
        private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
            JLabel l = new JLabel(label); l.setFont(new Font("Segoe UI", Font.BOLD, 12));
            panel.add(l, gbc);
            gbc.gridx = 1; gbc.weightx = 1; comp.setPreferredSize(new Dimension(200, 30));
            panel.add(comp, gbc);
        }

        private String generateEmpNo() { return String.format("%03d", empCounter); }

        private void loadPositions() {
            try (Connection conn = Database.connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DISTINCT position FROM employees")) {
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                while (rs.next()) model.addElement(rs.getString("position"));
                cbPosition.setModel(model);
            } catch (SQLException e) { e.printStackTrace(); }
        }

        private void addPosition() {
            String newPos = cbPosition.getEditor().getItem().toString().trim();
            if (!newPos.isEmpty()) {
                DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cbPosition.getModel();
                if (model.getIndexOf(newPos) == -1) { model.addElement(newPos); cbPosition.setSelectedItem(newPos); }
            }
        }

        private void removePosition() {
            DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cbPosition.getModel();
            if (cbPosition.getSelectedItem() != null) model.removeElement(cbPosition.getSelectedItem());
        }

        private void updateSalaryRate() {
            String status = (String) cbStatus.getSelectedItem();
            int salary = ("Probationary".equalsIgnoreCase(status) || "Contractual".equalsIgnoreCase(status)) ? 600 : 650;
            txtSalaryRate.setText(String.valueOf(salary));
            txtSalaryRate.setEditable(!"Probationary".equalsIgnoreCase(status) && !"Contractual".equalsIgnoreCase(status));
        }

        private void uploadPhoto() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png"));
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File selectedFile = fileChooser.getSelectedFile();
                    Path photoDir = Paths.get("employee_photos");
                    if (!Files.exists(photoDir)) Files.createDirectories(photoDir);
                    String newFileName = "emp_" + System.currentTimeMillis() + ".jpg"; // timestamp for uniqueness
                    Path dest = photoDir.resolve(newFileName);
                    Files.copy(selectedFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                    imagePath = dest.toString();
                    ImageIcon icon = new ImageIcon(imagePath);
                    Image scaled = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    lblPhotoDisplay.setIcon(new ImageIcon(scaled));
                    lblPhotoDisplay.setText("");
                } catch (IOException ex) { JOptionPane.showMessageDialog(this, "Error uploading photo."); }
            }
        }

        private String generateRandomPassword(int length) {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
            SecureRandom rnd = new SecureRandom();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
            return sb.toString();
        }

        private void sendEmail(String to, String name, String user, String pass) throws MessagingException {
            final String from = "sh4wntolentino@gmail.com";
            final String pwd = "dkffdbkmlifnvows";
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() { return new PasswordAuthentication(from, pwd); }
            });
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject("Your Account Credentials");
            msg.setText("Hello " + name + ",\n\nUsername: " + user + "\nPassword: " + pass + "\n\nPlease change your password upon login.");
            Transport.send(msg);
        }
    }
}