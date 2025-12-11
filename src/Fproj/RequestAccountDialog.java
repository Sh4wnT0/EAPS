package Fproj;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

class RequestAccountDialog extends JDialog {

    private JTextField txtName, txtEmail;
    private JLabel lblFile;
    private JButton btnUpload, btnSubmit;
    private File selectedFile;

    private final Color BRAND_COLOR = new Color(22, 102, 87);

    public RequestAccountDialog(Frame owner) {
        super(owner, "Request New Account", true);
        setSize(450, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; 
        
        // Header
        gbc.gridy = 0;
        JLabel title = new JLabel("Join Our Team");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(BRAND_COLOR);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(title, gbc);
        
        gbc.gridy++;
        JLabel sub = new JLabel("Submit your details for review");
        sub.setForeground(Color.GRAY);
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(sub, gbc);

        // Name
        gbc.gridy++; gbc.insets = new Insets(20, 0, 5, 0);
        mainPanel.add(new JLabel("Full Name:"), gbc);
        
        gbc.gridy++; gbc.insets = new Insets(0, 0, 10, 0);
        txtName = new JTextField();
        txtName.setPreferredSize(new Dimension(0, 30));
        mainPanel.add(txtName, gbc);

        // Email
        gbc.gridy++; gbc.insets = new Insets(5, 0, 5, 0);
        mainPanel.add(new JLabel("Email Address:"), gbc);
        
        gbc.gridy++; gbc.insets = new Insets(0, 0, 10, 0);
        txtEmail = new JTextField();
        txtEmail.setPreferredSize(new Dimension(0, 30));
        mainPanel.add(txtEmail, gbc);

        // Resume Upload
        gbc.gridy++; gbc.insets = new Insets(5, 0, 5, 0);
        mainPanel.add(new JLabel("Resume (PDF Only):"), gbc);
        
        gbc.gridy++;
        JPanel filePanel = new JPanel(new BorderLayout(10, 0));
        filePanel.setOpaque(false);
        
        lblFile = new JLabel("No file selected");
        lblFile.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblFile.setForeground(Color.GRAY);
        
        btnUpload = new JButton("Choose File");
        styleButton(btnUpload, new Color(70, 130, 180));
        btnUpload.addActionListener(e -> chooseFile());
        
        filePanel.add(lblFile, BorderLayout.CENTER);
        filePanel.add(btnUpload, BorderLayout.EAST);
        mainPanel.add(filePanel, gbc);

        // Submit Button
        gbc.gridy++; gbc.insets = new Insets(30, 0, 10, 0);
        btnSubmit = new JButton("Submit Request");
        styleButton(btnSubmit, BRAND_COLOR);
        btnSubmit.setPreferredSize(new Dimension(0, 40));
        btnSubmit.addActionListener(e -> submitRequest());
        mainPanel.add(btnSubmit, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Documents", "pdf"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            lblFile.setText(selectedFile.getName());
            lblFile.setForeground(new Color(22, 102, 87));
        }
    }

    private void submitRequest() {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();

        if (name.isEmpty() || email.isEmpty() || selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please fill all fields and upload a resume.", "Missing Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Save file to local folder logic
        try {
            Path uploadDir = Paths.get("resume_uploads");
            if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);

            // Create unique filename: Name_Timestamp.pdf
            String newFileName = name.replaceAll("\\s+", "_") + "_" + System.currentTimeMillis() + ".pdf";
            Path destPath = uploadDir.resolve(newFileName);
            
            Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
            
            // Save to DB
            boolean success = Database.insertAccountRequest(name, email, destPath.toString());
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Request submitted successfully!\nWe will contact you via email.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Database error. Please try again.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage());
        }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }
}