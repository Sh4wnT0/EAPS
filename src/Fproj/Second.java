package Fproj;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.MatteBorder;

public class Second extends JPanel {

    private JPasswordField passwordField;
    private JTextField usernameField;

    public Second() {

        setLayout(null);
        setBackground(new Color(22, 102, 87)); // Green background like Third.java

        // === TITLE ===
        JLabel title = new JLabel("ADMIN LOGIN", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 30));
        title.setForeground(Color.WHITE); // White text for visibility on green
        title.setBounds(0, 40, 1000, 40);     // centered automatically because width = frame width
        add(title);

        // === CARD PANEL ===
        JPanel card = new JPanel();
        card.setLayout(null);
        card.setBackground(Color.WHITE);
        card.setBounds(350, 120, 300, 250);   // centered horizontally for 1000px width
        card.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));
        add(card);

        // Username Label
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Arial", Font.PLAIN, 15));
        lblUser.setBounds(30, 25, 240, 20);
        card.add(lblUser);

        usernameField = new JTextField();
        usernameField.setBounds(30, 45, 240, 30);
        usernameField.setBorder(new MatteBorder(0, 0, 1, 0, (Color) new Color(0, 0, 0))); // White bottom border like Third.java
        usernameField.setForeground(Color.BLACK); // Black text for visibility on white card
        usernameField.setCaretColor(Color.BLACK);
        card.add(usernameField);

        // Password Label
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Arial", Font.PLAIN, 15));
        lblPass.setBounds(30, 90, 240, 20);
        card.add(lblPass);

        passwordField = new JPasswordField();
        passwordField.setBounds(30, 110, 240, 30);
        passwordField.setBorder(new MatteBorder(0, 0, 1, 0, (Color) new Color(0, 0, 0))); // White bottom border like Third.java
        passwordField.setForeground(Color.BLACK); // Black text for visibility on white card
        passwordField.setCaretColor(Color.BLACK);
        passwordField.setOpaque(false);
        passwordField.setEchoChar('●');
        card.add(passwordField);

        // LOGIN BUTTON
        JButton btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 15));
        btnLogin.setBackground(new Color(240, 40, 40)); // Red like Third.java
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnLogin.setBounds(30, 160, 110, 35);
        btnLogin.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (username.equals("admin") && password.equals("admin123")) {
                Main.cardLayout.show(Main.cardPanel, "AdDashboard");
            } else {
                JOptionPane.showMessageDialog(null, "Incorrect admin credentials.");
            }
        });
        card.add(btnLogin);

        // FORGOT BUTTON
        JButton btnForgot = new JButton("Forgot?");
        btnForgot.setFont(new Font("Arial", Font.BOLD, 15));
        btnForgot.setBackground(new Color(240, 40, 40)); // Red like Third.java
        btnForgot.setForeground(Color.WHITE);
        btnForgot.setFocusPainted(false);
        btnForgot.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnForgot.setBounds(160, 160, 110, 35);
        card.add(btnForgot);

        // BACK BUTTON
        JButton btnBack = new JButton("Back");
        btnBack.setBackground(new Color(255, 0, 0));
        btnBack.setForeground(Color.WHITE);
        btnBack.setBounds(20, 20, 90, 28);
        btnBack.addActionListener(e -> Main.cardLayout.show(Main.cardPanel, "main"));
        add(btnBack);
    }
}