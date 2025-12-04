package Fproj;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.swing.filechooser.FileNameExtensionFilter;

public class RegistrationPanel extends JPanel {

    private JTextField txtName, txtAddress, txtEmail, txtContact;
    private JComboBox<String> cbPosition, cbStatus;
    private JPasswordField txtPassword;
    private JLabel lblEmpNumber, lblSalaryRate;
    private JButton btnUploadPhoto;
    private JLabel lblPhotoDisplay;
    private String imagePath;  // Stores the relative path of the uploaded image

    private static int empCounter;  // Auto-increment 001, 002, 003
    private static final int RIGHT_OFFSET = 250;  // Offset to shift existing elements to the right

    public RegistrationPanel() {
        setLayout(null);
        setBackground(new Color(240, 240, 240));

        empCounter = Database.getLastEmployeeNumber() + 1;

        // TITLE (adjusted slightly for centering)
        JLabel lblTitle = new JLabel("Employee Registration");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setBounds(150 + RIGHT_OFFSET / 2, 10, 300, 30);  // Shifted right
        add(lblTitle);

        // EMPLOYEE NUMBER (shifted right)
        JLabel lblEN = new JLabel("Employee No.:");
        lblEN.setBounds(40 + RIGHT_OFFSET, 60, 120, 25);
        add(lblEN);

        lblEmpNumber = new JLabel(generateEmpNo());
        lblEmpNumber.setBounds(160 + RIGHT_OFFSET, 60, 200, 25);
        add(lblEmpNumber);

        // FULL NAME (shifted right)
        addFieldLabel("Full Name:", 100);
        txtName = createTextField(100);

        // ADDRESS (shifted right)
        addFieldLabel("Address:", 140);
        txtAddress = createTextField(140);

        // EMAIL (shifted right)
        addFieldLabel("Email:", 180);
        txtEmail = createTextField(180);

        // CONTACT NUMBER (shifted right)
        addFieldLabel("Contact No.:", 220);
        txtContact = createTextField(220);

        // POSITION (shifted right)
        addFieldLabel("Position:", 260);
        cbPosition = new JComboBox<>(new String[]{
                "Operator", "Line Manager", "Supervisor"
        });
        cbPosition.setBounds(160 + RIGHT_OFFSET, 260, 200, 25);
        add(cbPosition);

        // EMPLOYMENT STATUS (shifted right)
        addFieldLabel("Employment Status:", 300);
        cbStatus = new JComboBox<>(new String[]{
                "Regular", "Probationary", "Contractual"
        });
        cbStatus.setBounds(160 + RIGHT_OFFSET, 300, 200, 25);
        cbStatus.addActionListener(e -> updateSalaryRate());
        add(cbStatus);

        // SALARY RATE (AUTO) (shifted right)
        addFieldLabel("Salary Rate (Daily):", 340);
        lblSalaryRate = new JLabel("₱" + getSalaryRate(cbStatus.getSelectedItem().toString()));
        lblSalaryRate.setBounds(160 + RIGHT_OFFSET, 340, 200, 25);
        add(lblSalaryRate);

        // PASSWORD (shifted right)
        addFieldLabel("Set Password:", 380);
        txtPassword = new JPasswordField();
        txtPassword.setBounds(160 + RIGHT_OFFSET, 380, 200, 25);
        add(txtPassword);

        // REGISTER BUTTON (shifted right)
        JButton btnSave = new JButton("Register");
        btnSave.setBackground(new Color(0, 120, 0));
        btnSave.setForeground(Color.white);
        btnSave.setBounds(240 + RIGHT_OFFSET, 60, 120, 30);
        add(btnSave);
        btnSave.addActionListener(e -> registerEmployee());

        // --- NEW: IMAGE UPLOAD COMPONENTS (on the left side) ---
        // Label for photo section
        JLabel lblPhoto = new JLabel("Employee Photo:");
        lblPhoto.setBounds(50, 15, 120, 25);
        add(lblPhoto);

        // Button to upload photo
        btnUploadPhoto = new JButton("Upload Photo");
        btnUploadPhoto.setBounds(50, 35, 150, 30);
        btnUploadPhoto.addActionListener(new UploadPhotoListener());
        add(btnUploadPhoto);

        // Label to display selected image (or placeholder)
        lblPhotoDisplay = new JLabel("No photo selected", SwingConstants.CENTER);
        lblPhotoDisplay.setBounds(50, 55, 150, 150);  // 150x150 for image display
        lblPhotoDisplay.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        add(lblPhotoDisplay);
    }

    // --- Helper Methods ---

    private void addFieldLabel(String text, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setBounds(40 + RIGHT_OFFSET, y, 150, 25);  // Shifted right
        add(lbl);
    }

    private JTextField createTextField(int y) {
        JTextField txt = new JTextField();
        txt.setBounds(160 + RIGHT_OFFSET, y, 200, 25);  // Shifted right
        add(txt);
        return txt;
    }

    private String generateEmpNo() {
        return String.format("%03d", empCounter);
    }

    private int getSalaryRate(String status) {
        switch (status) {
            case "Regular": return 650;
            case "Probationary": return 600;
            case "Contractual": return 600;
            default: return 0;
        }
    }

    private void updateSalaryRate() {
        lblSalaryRate.setText("₱" + getSalaryRate(cbStatus.getSelectedItem().toString()));
    }

    // NEW: ActionListener for photo upload
    private class UploadPhotoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Employee Photo");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif");
            fileChooser.setFileFilter(filter);

            int result = fileChooser.showOpenDialog(RegistrationPanel.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile != null) {
                    try {
                        // Create folder if it doesn't exist (in project root, not in package)
                        Path photoDir = Paths.get("employee_photos");
                        if (!Files.exists(photoDir)) {
                            Files.createDirectories(photoDir);
                        }

                        // Generate unique filename (e.g., emp_001_123456789.jpg)
                        String empNo = lblEmpNumber.getText();
                        String timestamp = String.valueOf(System.currentTimeMillis());
                        String extension = getFileExtension(selectedFile.getName());
                        String newFileName = "emp_" + empNo + "_" + timestamp + "." + extension;
                        Path destination = photoDir.resolve(newFileName);

                        // Copy file to the folder
                        Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                        // Store relative path
                        imagePath = "employee_photos/" + newFileName;

                        // Display image in label (scaled)
                        ImageIcon icon = new ImageIcon(destination.toString());
                        Image scaledImage = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                        lblPhotoDisplay.setIcon(new ImageIcon(scaledImage));
                        lblPhotoDisplay.setText("");  // Clear placeholder text

                        JOptionPane.showMessageDialog(RegistrationPanel.this, "Photo uploaded successfully!");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(RegistrationPanel.this, "Error uploading photo: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        }

        // Helper to get file extension
        private String getFileExtension(String fileName) {
            int lastDot = fileName.lastIndexOf('.');
            return (lastDot > 0) ? fileName.substring(lastDot + 1).toLowerCase() : "jpg";  // Default to jpg
        }
    }

    private void registerEmployee() {
        String empNo = lblEmpNumber.getText();
        String name = txtName.getText();
        String address = txtAddress.getText();
        String email = txtEmail.getText();
        String contact = txtContact.getText();
        String position = cbPosition.getSelectedItem().toString();
        String status = cbStatus.getSelectedItem().toString();
        int salary = getSalaryRate(status);
        String pass = new String(txtPassword.getPassword());

        if (name.isEmpty() || pass.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
            return;
        }

        // SAVE TO DATABASE (updated to include imagePath; modify your Database class accordingly)
        Database.insertEmployeeFull(
                empNo, name, address, email, contact,
                position, status, salary, pass, imagePath  // Added imagePath
        );

        JOptionPane.showMessageDialog(this, "Employee Registered Successfully!");

        // RESET (including image)
        empCounter++;
        lblEmpNumber.setText(generateEmpNo());
        txtName.setText("");
        txtAddress.setText("");
        txtEmail.setText("");
        txtContact.setText("");
        txtPassword.setText("");
        cbStatus.setSelectedIndex(0);
        updateSalaryRate();
        // Reset image
        imagePath = null;
        lblPhotoDisplay.setIcon(null);
        lblPhotoDisplay.setText("No photo selected");
    }
}
