package Fproj;

import java.awt.*;
import java.io.File;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class AdminSettings extends JPanel {

    // Profile Components
    private JTextField txtCompName, txtContact;
    private JTextArea txtAddress;
    private JLabel lblLogoPreview;
    private String currentLogoPath = "";

    // Announcement Components
    private JTable tableAnnounce;
    private DefaultTableModel modelAnnounce;
    private JTextField txtAnnounceTitle;
    private JTextArea txtAnnounceMsg;

    // Colors
    private final Color BRAND_COLOR = new Color(22, 102, 87);

    public AdminSettings() {
        // Ensure tables exist
        Database.createCompanyInfoTable();

        setLayout(new GridLayout(1, 2, 20, 0)); // Split 50/50
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // LEFT: Company Profile
        add(createProfilePanel());

        // RIGHT: Announcements
        add(createAnnouncementPanel());
        
        loadProfileData();
        loadAnnouncements();
    }

    // ==========================================
    //           LEFT PANEL: PROFILE
    // ==========================================
    private JPanel createProfilePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 15));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.LIGHT_GRAY, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // Title
        JLabel lblTitle = new JLabel("Company Profile Details");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(BRAND_COLOR);
        p.add(lblTitle, BorderLayout.NORTH);

        // Form Container
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // 1. Logo Section
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridheight = 4;
        lblLogoPreview = new JLabel("No Logo", SwingConstants.CENTER);
        lblLogoPreview.setPreferredSize(new Dimension(120, 120));
        lblLogoPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        form.add(lblLogoPreview, gbc);

        // 2. Upload Button
        gbc.gridy = 4; gbc.gridheight = 1;
        JButton btnUpload = new JButton("Upload Logo");
        styleButton(btnUpload, new Color(70, 130, 180));
        btnUpload.addActionListener(e -> chooseImage());
        form.add(btnUpload, gbc);

        // 3. Fields (Right of Logo)
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        form.add(new JLabel("Company Name:"), gbc);
        
        gbc.gridy++;
        txtCompName = new JTextField();
        form.add(txtCompName, gbc);

        gbc.gridy++;
        form.add(new JLabel("Contact Number/Email:"), gbc);

        gbc.gridy++;
        txtContact = new JTextField();
        form.add(txtContact, gbc);

        gbc.gridy++;
        form.add(new JLabel("Office Address:"), gbc);

        gbc.gridy++;
        txtAddress = new JTextArea(3, 20);
        txtAddress.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        txtAddress.setLineWrap(true);
        form.add(txtAddress, gbc);

        p.add(form, BorderLayout.CENTER);

        // Save Button
        JButton btnSave = new JButton("Save Company Details");
        styleButton(btnSave, BRAND_COLOR);
        btnSave.setPreferredSize(new Dimension(0, 40));
        btnSave.addActionListener(e -> {
            Database.updateCompanyInfo(txtCompName.getText(), txtAddress.getText(), txtContact.getText(), currentLogoPath);
            JOptionPane.showMessageDialog(this, "Company details updated!");
        });
        p.add(btnSave, BorderLayout.SOUTH);

        return p;
    }

    // ==========================================
    //        RIGHT PANEL: ANNOUNCEMENTS
    // ==========================================
    private JPanel createAnnouncementPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 15));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.LIGHT_GRAY, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // Title
        JLabel lblTitle = new JLabel("Manage Announcements");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(BRAND_COLOR);
        p.add(lblTitle, BorderLayout.NORTH);

        // List (Top Half)
        modelAnnounce = new DefaultTableModel(new String[]{"ID", "Date", "Title", "Message"}, 0);
        tableAnnounce = new JTable(modelAnnounce);
        tableAnnounce.setRowHeight(25);
        tableAnnounce.getColumnModel().getColumn(0).setMinWidth(0);
        tableAnnounce.getColumnModel().getColumn(0).setMaxWidth(0); // Hide ID
        tableAnnounce.getColumnModel().getColumn(0).setWidth(0);
        tableAnnounce.getColumnModel().getColumn(1).setMaxWidth(80); // Date width

        JScrollPane scroll = new JScrollPane(tableAnnounce);
        scroll.setPreferredSize(new Dimension(0, 200));
        p.add(scroll, BorderLayout.CENTER);

        // Editor (Bottom Half)
        JPanel editor = new JPanel(new GridBagLayout());
        editor.setBackground(Color.WHITE);
        editor.setBorder(BorderFactory.createTitledBorder("New Announcement"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0;
        editor.add(new JLabel("Title:"), gbc);
        
        gbc.gridx = 1;
        txtAnnounceTitle = new JTextField();
        editor.add(txtAnnounceTitle, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        editor.add(new JLabel("Message:"), gbc);

        gbc.gridx = 1;
        txtAnnounceMsg = new JTextArea(3, 20);
        txtAnnounceMsg.setLineWrap(true);
        txtAnnounceMsg.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        editor.add(new JScrollPane(txtAnnounceMsg), gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        
        JButton btnPost = new JButton("Post");
        styleButton(btnPost, BRAND_COLOR);
        btnPost.addActionListener(e -> {
            if(txtAnnounceTitle.getText().isEmpty()) return;
            Database.addAnnouncement(txtAnnounceTitle.getText(), txtAnnounceMsg.getText());
            loadAnnouncements();
            txtAnnounceTitle.setText(""); txtAnnounceMsg.setText("");
        });

        JButton btnDelete = new JButton("Delete Selected");
        styleButton(btnDelete, new Color(220, 53, 69));
        btnDelete.addActionListener(e -> {
            int row = tableAnnounce.getSelectedRow();
            if(row != -1) {
                int id = Integer.parseInt(modelAnnounce.getValueAt(row, 0).toString());
                Database.deleteAnnouncement(id);
                loadAnnouncements();
            }
        });

        btnPanel.add(btnDelete);
        btnPanel.add(btnPost);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        editor.add(btnPanel, gbc);

        p.add(editor, BorderLayout.SOUTH);
        return p;
    }

    // --- DATA LOADING HELPERS ---
    private void loadProfileData() {
        String[] data = Database.getCompanyInfo();
        txtCompName.setText(data[0]);
        txtAddress.setText(data[1]);
        txtContact.setText(data[2]);
        currentLogoPath = data[3];
        
        if (currentLogoPath != null && !currentLogoPath.isEmpty()) {
            ImageIcon icon = new ImageIcon(currentLogoPath);
            Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            lblLogoPreview.setIcon(new ImageIcon(img));
            lblLogoPreview.setText("");
        }
    }

    private void loadAnnouncements() {
        modelAnnounce.setRowCount(0);
        try (Connection conn = Database.connect(); 
             Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery("SELECT * FROM announcements ORDER BY id DESC")) {
            while (rs.next()) {
                modelAnnounce.addRow(new Object[]{rs.getInt("id"), rs.getString("date"), rs.getString("title"), rs.getString("message")});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void chooseImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "jpeg"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            currentLogoPath = file.getAbsolutePath();
            ImageIcon icon = new ImageIcon(currentLogoPath);
            Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            lblLogoPreview.setIcon(new ImageIcon(img));
            lblLogoPreview.setText("");
        }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }
}