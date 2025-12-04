package Fproj;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.awt.event.ActionEvent;

public class Third extends JPanel {

    private JTextField textField;
    private JPasswordField passwordField;

    public Third() {

        setLayout(new GridLayout(1, 2)); // Split into two panels
        setBackground(new Color(240, 245, 255)); // light blue-ish background (kept for overall panel)

        //----------------------------------------------------
        // LEFT PANEL (Green Background - Login Section)
        //----------------------------------------------------
        JPanel left = new JPanel();
        left.setBackground(new Color(22, 102, 87));
        left.setLayout(null);

        // Title (adapted from LoginScreen)
        JLabel title = new JLabel("EXISTING USER?");
        title.setFont(new Font("Serif", Font.BOLD, 38));
        title.setForeground(Color.WHITE);
        title.setBounds(50, 100, 400, 50);
        left.add(title);

        // EMP ID Label (kept from original)
        JLabel lblNewLabel = new JLabel("Employee ID");
        lblNewLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        lblNewLabel.setForeground(Color.WHITE); // Changed to white for visibility
        lblNewLabel.setBounds(50, 180, 100, 20);
        left.add(lblNewLabel);

        // EMP ID Field
        textField = new JTextField();
        textField.setBounds(50, 200, 300, 35);
        textField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.WHITE)); // Styled like LoginScreen
        textField.setForeground(Color.WHITE);
        textField.setCaretColor(Color.WHITE);
        textField.setOpaque(false);
        left.add(textField);

        // Password Label
        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        lblPassword.setForeground(Color.WHITE); // Changed to white for visibility
        lblPassword.setBounds(50, 250, 100, 20);
        left.add(lblPassword);

        // Password Field
        passwordField = new JPasswordField();
        passwordField.setBounds(50, 270, 300, 35);
        passwordField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.WHITE)); // Styled like LoginScreen
        passwordField.setForeground(Color.WHITE);
        passwordField.setCaretColor(Color.WHITE);
        passwordField.setOpaque(false);
        passwordField.setEchoChar('●');
        left.add(passwordField);

        // Login Button (adapted styling from LoginScreen)
        JButton btnLogin = new JButton("Log In");
        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                String empNo = textField.getText().trim();
                String pass = new String(passwordField.getPassword()).trim();

                if (empNo.isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill all fields.");
                    return;
                }

                try {
                    // CONNECT TO DATABASE
                    Class.forName("org.sqlite.JDBC");
                    Connection conn = DriverManager.getConnection("jdbc:sqlite:employees.db");

                    // SQL using empNo + password
                    String sql = "SELECT * FROM employees WHERE empNo = ? AND password = ?";
                    PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setString(1, empNo);
                    pst.setString(2, pass);

                    ResultSet rs = pst.executeQuery();

                    if (rs.next()) {

                        // -------------------------------
                        // PASS empNo to Employee Dashboard
                        // -------------------------------

                        Employee empDash = new Employee(empNo);  
                        Main.cardPanel.add(empDash, "EmpDashboard");  
                        Main.cardLayout.show(Main.cardPanel, "EmpDashboard");

                    } else {
                        JOptionPane.showMessageDialog(null, "Incorrect Employee ID or Password.");
                    }

                    rs.close();
                    pst.close();
                    conn.close();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
        btnLogin.setBounds(120, 330, 120, 40);
        btnLogin.setBackground(new Color(240, 40, 40)); // Red like LoginScreen
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        left.add(btnLogin);

        // Forgot Button (adapted styling)
        JButton btnForgot = new JButton("Forgot?");
        btnForgot.setBounds(260, 330, 120, 40);
        btnForgot.setBackground(new Color(240, 40, 40)); // Red like LoginScreen
        btnForgot.setForeground(Color.WHITE);
        btnForgot.setFont(new Font("Arial", Font.BOLD, 14));
        btnForgot.setFocusPainted(false);
        btnForgot.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        left.add(btnForgot);

        // Back Button (moved to left panel, adjusted position)
        JButton btnNewButton = new JButton("Back");
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Main.cardLayout.show(Main.cardPanel, "main");
            }
        });
        btnNewButton.setBackground(new Color(255, 0, 0));
        btnNewButton.setForeground(new Color(255, 255, 255));
        btnNewButton.setBounds(10, 10, 85, 21);
        left.add(btnNewButton);

        //----------------------------------------------------
        // RIGHT PANEL (Gray Background - Request Account Section)
        //----------------------------------------------------
        JPanel right = new JPanel();
        right.setBackground(new Color(235, 230, 230));
        right.setLayout(null);

        // Title (adapted from LoginScreen)
        JLabel reqTitle = new JLabel("Request an Account");
        reqTitle.setFont(new Font("Serif", Font.BOLD, 34));
        reqTitle.setBounds(80, 130, 400, 40);
        right.add(reqTitle);

        // Subtitle
        JLabel sub = new JLabel("request an account now");
        sub.setFont(new Font("Serif", Font.PLAIN, 18));
        sub.setBounds(130, 180, 300, 20);
        right.add(sub);

        // Request Button (adapted styling from LoginScreen)
        JButton btnRequest = new JButton("Request");
        btnRequest.setBounds(150, 230, 180, 40);
        btnRequest.setBackground(new Color(240, 40, 40)); // Red like LoginScreen
        btnRequest.setForeground(Color.WHITE);
        btnRequest.setFont(new Font("Arial", Font.BOLD, 14));
        btnRequest.setFocusPainted(false);
        btnRequest.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        right.add(btnRequest);

        //----------------------------------------------------
        // Add Panels
        //----------------------------------------------------
        add(left);
        add(right);
    }
}
