package Fproj;

import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.*;

public class Third extends JPanel {

    // 1. Declare components at Class Level for dynamic resizing
    private JLabel title, lblUser, lblPass, reqTitle, sub;
    private JTextField textField;
    private JPasswordField passwordField;
    private JButton btnLogin, btnForgot, btnBack, btnRequest;

    public Third() {
        // Main Layout: Split screen 50/50
        setLayout(new GridLayout(1, 2)); 
        setBackground(new Color(240, 245, 255)); 

        //----------------------------------------------------
        // LEFT PANEL (Login Section)
        //----------------------------------------------------
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(new Color(22, 102, 87));

        // -- Top Bar (Back Button) --
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.setOpaque(false);
        
        btnBack = new JButton("Back");
        btnBack.setBackground(new Color(255, 0, 0));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> Main.cardLayout.show(Main.cardPanel, "main"));
        topBar.add(btnBack);
        left.add(topBar, BorderLayout.NORTH);

        // -- Form Area --
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0); 
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        title = new JLabel("EXISTING USER?", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        gbc.gridy = 0;
        formPanel.add(title, gbc);

        // ID Label
        lblUser = new JLabel("ID / Username");
        lblUser.setForeground(Color.WHITE);
        gbc.gridy = 1;
        gbc.insets = new Insets(20, 0, 5, 0);
        formPanel.add(lblUser, gbc);

        // ID Field
        textField = new JTextField();
        textField.setPreferredSize(new Dimension(300, 35));
        textField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.WHITE));
        textField.setForeground(Color.WHITE);
        textField.setCaretColor(Color.WHITE);
        textField.setOpaque(false);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(textField, gbc);

        // Password Label
        lblPass = new JLabel("Password");
        lblPass.setForeground(Color.WHITE);
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 0, 5, 0);
        formPanel.add(lblPass, gbc);

        // Password Field
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(300, 35));
        passwordField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.WHITE));
        passwordField.setForeground(Color.WHITE);
        passwordField.setCaretColor(Color.WHITE);
        passwordField.setOpaque(false);
        passwordField.setEchoChar('●');
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 20, 0);
        formPanel.add(passwordField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        btnLogin = new JButton("Log In");
        styleButton(btnLogin);
        btnLogin.addActionListener(e -> handleLogin());

        btnForgot = new JButton("Forgot?");
        styleButton(btnForgot);
        btnForgot.addActionListener(e -> Main.cardLayout.show(Main.cardPanel, "forgot"));

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnForgot);

        gbc.gridy = 5;
        formPanel.add(buttonPanel, gbc);
        left.add(formPanel, BorderLayout.CENTER);

        //----------------------------------------------------
        // RIGHT PANEL (Request Account Section)
        //----------------------------------------------------
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(new Color(235, 230, 230));
        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.gridx = 0;
        gbcRight.insets = new Insets(10, 0, 10, 0);

        reqTitle = new JLabel("Request an Account");
        gbcRight.gridy = 0;
        right.add(reqTitle, gbcRight);

        sub = new JLabel("request an account now");
        gbcRight.gridy = 1;
        right.add(sub, gbcRight);

        btnRequest = new JButton("Request");
        styleButton(btnRequest);
        btnRequest.setPreferredSize(new Dimension(180, 40));
        btnRequest.addActionListener(e -> {
            // Open the dialog
            RequestAccountDialog dialog = new RequestAccountDialog((Frame) SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
        });
        gbcRight.gridy = 2;
        gbcRight.insets = new Insets(30, 0, 10, 0);
        right.add(btnRequest, gbcRight);

        add(left);
        add(right);

        // 2. Add Listener to detect window resizing
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeFonts();
            }
        });
        
        // Initial font set
        resizeFonts(); 
    }

    // 3. Logic to calculate dynamic font sizes
    private void resizeFonts() {
        int h = getHeight();
        if (h == 0) return; // Prevent errors on first load

        // Calculate scale ratio. 600 is our "base" design height.
        double ratio = Math.min((double)getWidth() / 800, (double)getHeight() / 600);
        
        // Clamp the ratio
        ratio = Math.max(0.8, Math.min(ratio, 2.5));

        // Update Fonts using the Ratio
        title.setFont(new Font("Serif", Font.BOLD, (int)(38 * ratio)));
        reqTitle.setFont(new Font("Serif", Font.BOLD, (int)(34 * ratio)));
        sub.setFont(new Font("Serif", Font.PLAIN, (int)(18 * ratio)));
        
        // Labels
        Font labelFont = new Font("Arial", Font.PLAIN, (int)(14 * ratio));
        lblUser.setFont(labelFont);
        lblPass.setFont(labelFont);
        
        // Fields & Buttons
        Font fieldFont = new Font("Arial", Font.PLAIN, (int)(14 * ratio));
        Font btnFont = new Font("Arial", Font.BOLD, (int)(14 * ratio));
        
        textField.setFont(fieldFont);
        passwordField.setFont(fieldFont);
        btnLogin.setFont(btnFont);
        btnForgot.setFont(btnFont);
        btnBack.setFont(btnFont);
        btnRequest.setFont(btnFont);
    }

    private void styleButton(JButton btn) {
        btn.setBackground(new Color(240, 40, 40));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setPreferredSize(new Dimension(120, 40));
    }

    // ==========================================
    //           UPDATED LOGIN LOGIC
    // ==========================================
    private void handleLogin() {
        String identifier = textField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (identifier.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill all fields.");
            return;
        }

        // 1. CHECK ADMIN/STAFF TABLE (as_records)
        String role = checkASRecords(identifier, password);
        
        if (role != null) {
            // BOTH Admin and Staff go to the Admin Dashboard.
            // The Admin class constructor will hide buttons based on the user's role.
            Admin adminDash = new Admin(identifier);
            Main.cardPanel.add(adminDash, "AdDashboard");
            Main.cardLayout.show(Main.cardPanel, "AdDashboard");
            return; 
        }

        // 2. CHECK EMPLOYEE TABLE (employees)
        if (Database.checkEmployeeLogin(identifier, password)) {
            Employee empDash = new Employee(identifier);
            Main.cardPanel.add(empDash, "EmpDashboard");
            Main.cardLayout.show(Main.cardPanel, "EmpDashboard");
        } else {
            JOptionPane.showMessageDialog(null, "Incorrect ID/Username or Password.");
        }
    }

    private String checkASRecords(String username, String password) {
        String sql = "SELECT role FROM as_records WHERE username = ? AND password = ?";
        try (Connection conn = Database.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getString("role");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}