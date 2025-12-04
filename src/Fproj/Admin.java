package Fproj;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Admin extends JPanel {

    private CardLayout innerCardLayout;
    private JPanel innerCardPanel;

    public Admin() {

        setLayout(new BorderLayout());   // Better layout structure
        
        // Load and scale the logo image with error handling
        ImageIcon originalIcon = new ImageIcon(getClass().getResource("logo.png")); // Load from Fproj package
        Image scaledImage = originalIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH); // Scale to 50x50 without cropping
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        // ====================== HEADER ============================
        JPanel header = new JPanel();
        header.setBackground(new Color(22, 102, 87));
        header.setPreferredSize(new Dimension(100, 50));
        header.setLayout(null);

        // Create a label for the logo
        JLabel logoLabel = new JLabel(scaledIcon);
        logoLabel.setBounds(10, -11, 75, 75); // Position in top-left
        header.add(logoLabel);

        // ... (rest of the header code remains unchanged)
        
        JLabel lblTitle = new JLabel("Admin Dashboard");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setBounds(95, 8, 200, 30);
        header.add(lblTitle);

        // NEW: Notification button in top right (left of logout)
        JButton btnNotifications = new JButton("Notifications");
        btnNotifications.setBounds(740, 10, 100, 30);  // Positioned left of logout
        btnNotifications.setBackground(new Color(220, 20, 60));  // Match logout style
        btnNotifications.setForeground(Color.WHITE);
        btnNotifications.setFocusPainted(false);
        btnNotifications.addActionListener(e -> JOptionPane.showMessageDialog(this, "No new notifications."));  // Placeholder action
        header.add(btnNotifications);

        JButton btnLogout = new JButton("Log-out");
        btnLogout.setBounds(850, 10, 100, 30);
        btnLogout.setBackground(new Color(220, 20, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> Main.cardLayout.show(Main.cardPanel, "main"));
        header.add(btnLogout);

        add(header, BorderLayout.NORTH);
        
        
        // ====================== SIDEBAR ============================
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(180, 400));
        sidebar.setBackground(new Color(230, 235, 245));
        sidebar.setLayout(new GridLayout(11, 1, 0, 8));  // Increased to 11 rows for the new Home button

        JButton btnHome = createMenuButton("Home");  // NEW: Home button
        JButton btnRegistration = createMenuButton("Registration");
        JButton btnLeaveReq = createMenuButton("Leave Requests");
        JButton btnOTReq = createMenuButton("Holiday/OT Requests");
        JButton btnRecords = createMenuButton("Employee Records");
        JButton btnAttendance = createMenuButton("Attendance Records");  // New button
        JButton btnPayroll = createMenuButton("Payroll Records");       // New button

        sidebar.add(btnHome);  // Add Home first
        sidebar.add(btnRegistration);
        sidebar.add(btnLeaveReq);
        sidebar.add(btnOTReq);
        sidebar.add(btnRecords);
        sidebar.add(btnAttendance);  // Add to sidebar
        sidebar.add(btnPayroll);     // Add to sidebar

        add(sidebar, BorderLayout.WEST);

        // =================== INNER CARD PANEL (CONTENT AREA) ===============
        innerCardLayout = new CardLayout();
        innerCardPanel = new JPanel(innerCardLayout);
        innerCardPanel.setBackground(Color.WHITE);

        innerCardPanel.add(new HomePanel(), "home");  // NEW: Add Home panel
        innerCardPanel.add(new RegistrationPanel(), "registration");
        innerCardPanel.add(new LeaveApproval(), "leave");
        innerCardPanel.add(new OTAdmin(), "ot");
        innerCardPanel.add(new AdminRecords(), "records");
        innerCardPanel.add(new adminAttendance(), "attendance");  // New panel
        innerCardPanel.add(new adminPayroll(), "payroll");        // New panel

        add(innerCardPanel, BorderLayout.CENTER);

        // ====================== BUTTON ACTIONS =======================
        btnHome.addActionListener(e -> innerCardLayout.show(innerCardPanel, "home"));  // NEW: Home action
        btnRegistration.addActionListener(e -> innerCardLayout.show(innerCardPanel, "registration"));
        btnLeaveReq.addActionListener(e -> innerCardLayout.show(innerCardPanel, "leave"));
        btnOTReq.addActionListener(e -> innerCardLayout.show(innerCardPanel, "ot"));
        btnRecords.addActionListener(e -> innerCardLayout.show(innerCardPanel, "records"));
        btnAttendance.addActionListener(e -> innerCardLayout.show(innerCardPanel, "attendance"));  // New action
        btnPayroll.addActionListener(e -> innerCardLayout.show(innerCardPanel, "payroll"));        // New action
    }

    // ====================== MENU BUTTON FACTORY ======================
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.PLAIN, 15));
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(new Color(180,180,180)));

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(200, 220, 255));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });

        return btn;
    }

    // NEW: Simple Home Panel inner class
    private class HomePanel extends JPanel {
        public HomePanel() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            
            JLabel welcomeLabel = new JLabel("Welcome to the Admin Dashboard!", SwingConstants.CENTER);
            welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
            welcomeLabel.setForeground(new Color(22, 102, 87));
            add(welcomeLabel, BorderLayout.CENTER);
            
            // You can add more components here, like quick stats or links
        }
    }
}