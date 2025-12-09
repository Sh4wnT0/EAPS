package Fproj;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class adProfile extends JPanel {

    // --- Components ---
    private JLabel lblPhoto;
    private JLabel lblNameVal, lblRoleVal, lblPosVal, lblAddressVal, lblEmailVal, lblContactVal;
    private JTextArea txtDetails;
    private JButton btnUpload, btnUpdateInfo, btnUpdateCreds;
    
    // --- Data ---
    private String Username;
    private String photoPath;

    // --- Colors & Fonts ---
    private final Color BRAND_COLOR = new Color(22, 102, 87);
    private final Color BG_COLOR = new Color(240, 245, 255);
    private final Color TEXT_DARK = new Color(40, 40, 40);
    private final Color TEXT_LABEL = new Color(140, 140, 140);
    private final Font FONT_VAL = new Font("Segoe UI", Font.BOLD, 14);
    private final Font FONT_CAP = new Font("Segoe UI", Font.BOLD, 10);

    public adProfile(String username) {
        this.Username = username;

        // 1. Main Container Setup
        setBackground(BG_COLOR);
        setLayout(new GridBagLayout()); // Centers the card on the screen

        // 2. The "Card" Panel
        JPanel card = new JPanel();
        card.setLayout(new GridBagLayout());
        card.setBackground(Color.WHITE);
        
        // Card Shadow/Border effect
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(40, 50, 40, 50) // Generous internal padding
        ));

        GridBagConstraints gbc = new GridBagConstraints();

        // =================================================================
        // LEFT COLUMN: PHOTO & UPLOAD
        // =================================================================
        
        // Circular Photo
        lblPhoto = new CircularImageLabel(); 
        lblPhoto.setPreferredSize(new Dimension(150, 150));
        lblPhoto.setText(""); 
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 8; // Spans down alongside the text details
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(0, 0, 0, 30); // Right margin to separate from text
        card.add(lblPhoto, gbc);

        // Change Photo Button
        btnUpload = new JButton("Change Photo");
        styleButton(btnUpload);
        btnUpload.addActionListener(e -> uploadPhoto());
        
        gbc.gridy = 8;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 0, 0, 30); // Margin top
        card.add(btnUpload, gbc);

        // =================================================================
        // RIGHT COLUMN: DETAILS
        // =================================================================
        
        // Reset height and set column
        gbc.gridheight = 1; 
        gbc.gridx = 1; 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Helper to add rows cleanly
        int row = 0;

        // --- GROUP 1: IDENTITY ---
        addDetailRow(card, "FULL NAME", lblNameVal = new JLabel("..."), gbc, row++);
        addDetailRow(card, "ROLE", lblRoleVal = new JLabel("..."), gbc, row++);
        addDetailRow(card, "POSITION", lblPosVal = new JLabel("..."), gbc, row++);

        // --- SEPARATOR ---
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(230, 230, 230));
        gbc.gridy = row * 2; // Logic adjustment for helper
        gbc.insets = new Insets(15, 0, 15, 0);
        card.add(sep, gbc);
        row++;

        // --- GROUP 2: CONTACT ---
        addDetailRow(card, "EMAIL ADDRESS", lblEmailVal = new JLabel("..."), gbc, row++);
        addDetailRow(card, "CONTACT NUMBER", lblContactVal = new JLabel("..."), gbc, row++);
        addDetailRow(card, "HOME ADDRESS", lblAddressVal = new JLabel("..."), gbc, row++);

        // =================================================================
        // BOTTOM: ACTION BUTTONS
        // =================================================================
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnUpdateInfo = new JButton("Edit Info");
        styleButton(btnUpdateInfo);
        btnUpdateInfo.setBackground(new Color(70, 130, 180)); // Steel Blue
        btnUpdateInfo.addActionListener(e -> openUpdateInfoDialog());

        btnUpdateCreds = new JButton("Security Settings");
        styleButton(btnUpdateCreds);
        btnUpdateCreds.setBackground(new Color(220, 53, 69)); // Danger Red
        btnUpdateCreds.addActionListener(e -> openUpdateCredentialsDialog());

        buttonPanel.add(btnUpdateInfo);
        buttonPanel.add(btnUpdateCreds);

        gbc.gridx = 0;
        gbc.gridy = 20; // Ensure this is at the bottom
        gbc.gridwidth = 2; // Span across Photo and Details
        gbc.insets = new Insets(30, 0, 0, 0);
        card.add(buttonPanel, gbc);

        // Footer Note
        txtDetails = new JTextArea("Welcome. Please keep your profile up to date.");
        txtDetails.setEditable(false);
        txtDetails.setBackground(new Color(250, 250, 250));
        txtDetails.setForeground(TEXT_LABEL);
        txtDetails.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        txtDetails.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        gbc.gridy = 21;
        gbc.insets = new Insets(15, 0, 0, 0);
        card.add(txtDetails, gbc);

        add(card);
        loadAdminDetails();
    }

    /**
     * Helper to add a Caption + Value pair with consistent spacing
     */
    private void addDetailRow(JPanel panel, String caption, JLabel valueLabel, GridBagConstraints gbc, int logicalRow) {
        int y = logicalRow * 2; // Each logical row takes 2 grid rows (Caption + Value)

        // Caption
        JLabel cap = new JLabel(caption);
        cap.setFont(FONT_CAP);
        cap.setForeground(TEXT_LABEL);
        
        gbc.gridy = y;
        gbc.insets = new Insets(10, 0, 2, 0); // Top padding, tight bottom
        panel.add(cap, gbc);

        // Value
        valueLabel.setFont(FONT_VAL);
        valueLabel.setForeground(TEXT_DARK);
        
        gbc.gridy = y + 1;
        gbc.insets = new Insets(0, 0, 0, 0); // No padding
        panel.add(valueLabel, gbc);
    }

    // --- STYLING HELPER ---
    private void styleButton(JButton btn) {
        btn.setBackground(BRAND_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // --- CUSTOM COMPONENT: CIRCULAR IMAGE ---
    private class CircularImageLabel extends JLabel {
        @Override
        protected void paintComponent(Graphics g) {
            Icon icon = getIcon();
            if (icon != null) {
                BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = bi.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setClip(new Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                g2.drawImage(((ImageIcon)icon).getImage(), 0, 0, getWidth(), getHeight(), null);
                
                // Border Ring
                g2.setClip(null);
                g2.setColor(new Color(230, 230, 230));
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(1, 1, getWidth()-3, getHeight()-3);
                
                g2.dispose();
                g.drawImage(bi, 0, 0, null);
            } else {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(245, 245, 245));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                String text = "No Photo";
                g2.drawString(text, (getWidth() - fm.stringWidth(text))/2, (getHeight() + fm.getAscent())/2 - 2);
            }
        }
    }

    // =================================================================
    // LOGIC & DATABASE METHODS
    // =================================================================

    private void loadAdminDetails() {
        String sql = "SELECT name, role, position, address, email, contact, photo_path FROM as_records WHERE username = ?";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, Username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                lblNameVal.setText(rs.getString("name"));
                lblRoleVal.setText(rs.getString("role"));
                lblPosVal.setText(rs.getString("position"));
                lblAddressVal.setText(rs.getString("address"));
                lblEmailVal.setText(rs.getString("email"));
                lblContactVal.setText(rs.getString("contact"));

                photoPath = rs.getString("photo_path");
                if (photoPath != null && !photoPath.isEmpty()) {
                    File imgFile = new File(photoPath);
                    if (imgFile.exists()) {
                        ImageIcon icon = new ImageIcon(photoPath);
                        // Scale specifically to the label size
                        Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                        lblPhoto.setIcon(new ImageIcon(img));
                        lblPhoto.setText("");
                    }
                } else {
                    lblPhoto.setIcon(null);
                    lblPhoto.setText("No Photo");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading profile.");
        }
    }

    private void uploadPhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Profile Photo");
        chooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "jpeg", "png"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            try {
                Path photoDir = Paths.get("employee_photos");
                if (!Files.exists(photoDir)) Files.createDirectories(photoDir);

                String ext = "";
                String name = selectedFile.getName();
                if (name.lastIndexOf('.') > 0) ext = name.substring(name.lastIndexOf('.') + 1);
                
                String newFileName = Username + "_" + System.currentTimeMillis() + "." + ext;
                Path destPath = photoDir.resolve(newFileName);
                Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

                try (Connection conn = Database.connect();
                     PreparedStatement pstmt = conn.prepareStatement("UPDATE as_records SET photo_path = ? WHERE username = ?")) {
                    pstmt.setString(1, destPath.toString());
                    pstmt.setString(2, Username);
                    pstmt.executeUpdate();
                }
                
                // Refresh Image immediately
                ImageIcon icon = new ImageIcon(destPath.toString());
                Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                lblPhoto.setIcon(new ImageIcon(img));
                lblPhoto.setText("");

                JOptionPane.showMessageDialog(this, "Photo updated!");

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void openUpdateInfoDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Information", true);
        dialog.setSize(420, 480);
        dialog.setLocationRelativeTo(this);
        
        JPanel p = new JPanel(new GridLayout(6, 2, 10, 15)); // Good spacing in dialog
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(30, 30, 30, 30));

        JTextField txtName = new JTextField(lblNameVal.getText());
        JTextField txtAddress = new JTextField(lblAddressVal.getText());
        JTextField txtEmail = new JTextField(lblEmailVal.getText());
        JTextField txtContact = new JTextField(lblContactVal.getText());
        JTextField txtPos = new JTextField(lblPosVal.getText());

        p.add(new JLabel("Full Name:")); p.add(txtName);
        p.add(new JLabel("Position:")); p.add(txtPos);
        p.add(new JLabel("Email:")); p.add(txtEmail);
        p.add(new JLabel("Contact No:")); p.add(txtContact);
        p.add(new JLabel("Address:")); p.add(txtAddress);

        JButton btnSave = new JButton("Save Changes");
        styleButton(btnSave);
        btnSave.addActionListener(e -> {
            if(updateInfo(txtName.getText(), txtAddress.getText(), txtEmail.getText(), txtContact.getText(), txtPos.getText())) {
                dialog.dispose();
                loadAdminDetails();
            }
        });

        p.add(new JLabel("")); 
        p.add(btnSave);

        dialog.add(p);
        dialog.setVisible(true);
    }

    private void openUpdateCredentialsDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Security", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel p = new JPanel(new GridLayout(5, 1, 10, 10)); // Stacked layout
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(30, 30, 30, 30));

        JTextField txtNewUser = new JTextField(Username);
        JPasswordField txtOldPass = new JPasswordField();
        JPasswordField txtNewPass = new JPasswordField();

        p.add(new JLabel("Username (ID):")); p.add(txtNewUser);
        p.add(new JLabel("Current Password:")); p.add(txtOldPass);
        p.add(new JLabel("New Password:")); p.add(txtNewPass);

        JButton btnSave = new JButton("Update Credentials");
        styleButton(btnSave);
        btnSave.addActionListener(e -> {
            if(updateCredentials(txtNewUser.getText().trim(), new String(txtOldPass.getPassword()), new String(txtNewPass.getPassword()))) {
                dialog.dispose();
                this.Username = txtNewUser.getText().trim();
                JOptionPane.showMessageDialog(this, "Security settings updated.");
                loadAdminDetails();
            }
        });

        p.add(btnSave);
        dialog.add(p);
        dialog.setVisible(true);
    }

    // --- DB UPDATE METHODS ---

    private boolean updateInfo(String name, String address, String email, String contact, String position) {
        String sql = "UPDATE as_records SET name=?, address=?, email=?, contact=?, position=? WHERE username=?";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, address);
            pstmt.setString(3, email);
            pstmt.setString(4, contact);
            pstmt.setString(5, position);
            pstmt.setString(6, Username);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
        return false;
    }

    private boolean updateCredentials(String newUser, String oldPass, String newPass) {
        if(oldPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Current password is required.");
            return false;
        }
        
        try (Connection conn = Database.connect()) {
            // Verify old password
            PreparedStatement checkStmt = conn.prepareStatement("SELECT password FROM as_records WHERE username = ?");
            checkStmt.setString(1, Username);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next() || !rs.getString("password").equals(oldPass)) {
                JOptionPane.showMessageDialog(this, "Incorrect current password.");
                return false;
            }

            // Update
            String sql = (!newPass.isEmpty()) 
                ? "UPDATE as_records SET username=?, password=? WHERE username=?" 
                : "UPDATE as_records SET username=? WHERE username=?";
            
            PreparedStatement upStmt = conn.prepareStatement(sql);
            upStmt.setString(1, newUser);
            if (!newPass.isEmpty()) {
                upStmt.setString(2, newPass);
                upStmt.setString(3, Username);
            } else {
                upStmt.setString(2, Username);
            }
            return upStmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
        return false;
    }
}