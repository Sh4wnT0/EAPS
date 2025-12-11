package Fproj;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class Employee extends JPanel {

    private CardLayout innerCardLayout;
    private JPanel innerCardPanel;
    private String empNo;
    private JButton currentSelectedButton = null;

    // Colors
    private final Color HEADER_COLOR = new Color(22, 102, 87);
    private final Color SIDEBAR_BG = new Color(230, 235, 245);
    private final Color BTN_HOVER = new Color(200, 220, 255);
    private final Color LOGOUT_BTN = new Color(220, 20, 60);

    public Employee(String empNo) {
        this.empNo = empNo;
        setLayout(new BorderLayout());

        // =================================================================================
        // 1. HEADER
        // =================================================================================
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_COLOR);
        header.setPreferredSize(new Dimension(800, 60)); 
        header.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15)); 

        // --- Left Side (Logo + Title) ---
        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        headerLeft.setOpaque(false); 

        JLabel logoLabel = new JLabel();
        ImageIcon icon = loadIcon("logo.png", 50, 50); 
        if (icon != null) logoLabel.setIcon(icon);
        
        JLabel lblTitle = new JLabel("Employee Dashboard");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));

        headerLeft.add(logoLabel);
        headerLeft.add(lblTitle);

        // --- Right Side (Buttons) ---
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        headerRight.setOpaque(false);

        JButton btnNotifications = createHeaderButton("Notifications");
        // FIXED: Uses the unified NotificationDialog (isAdmin = false)
        btnNotifications.addActionListener(e -> {
            new NotificationDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), 
                empNo, 
                false // Not Admin
            ).setVisible(true);
        });

        JButton btnLogout = createHeaderButton("Log-out");
        btnLogout.addActionListener(e -> Main.cardLayout.show(Main.cardPanel, "main"));

        headerRight.add(btnNotifications);
        headerRight.add(btnLogout);

        header.add(headerLeft, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // =================================================================================
        // 2. SIDEBAR
        // =================================================================================
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(200, 0)); 
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        JPanel menuContainer = new JPanel(new GridLayout(0, 1, 0, 5)); 
        menuContainer.setOpaque(false);
        menuContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnHome = createMenuButton("Home", "Home.png");
        JButton btnProfile = createMenuButton("Profile", "leave.png");
        JButton btnAttendance = createMenuButton("Attendance", "attendance.png");
        JButton btnLeave = createMenuButton("Leave Request", "otholiday.png");
        JButton btnOT = createMenuButton("Holiday/OT Request", "otholiday.png");
        JButton btnPayroll = createMenuButton("Payroll", "payroll.png");

        menuContainer.add(btnHome);
        menuContainer.add(btnProfile);
        menuContainer.add(btnAttendance);
        menuContainer.add(btnLeave);
        menuContainer.add(btnOT);
        menuContainer.add(btnPayroll);

        sidebar.add(menuContainer, BorderLayout.NORTH); 
        add(sidebar, BorderLayout.WEST);

        // =================================================================================
        // 3. INNER CARD PANELS
        // =================================================================================
        innerCardLayout = new CardLayout();
        innerCardPanel = new JPanel(innerCardLayout);

        // Panels inside ScrollPanes for better responsiveness
        innerCardPanel.add(new JScrollPane(new EmpHomePanel(empNo)), "home");
        innerCardPanel.add(new JScrollPane(new empProfile(empNo)), "profile");
        innerCardPanel.add(new JScrollPane(new EmpAttendancePanel(empNo)), "attendance");
        innerCardPanel.add(new JScrollPane(new EmpLeavePanel(empNo)), "leave");
        innerCardPanel.add(new JScrollPane(new EmpOTPanel(empNo)), "ot");
        innerCardPanel.add(new JScrollPane(new EmpPayrollPanel(empNo)), "payroll");

        add(innerCardPanel, BorderLayout.CENTER);

        // =================================================================================
        // 4. BUTTON LOGIC
        // =================================================================================
        btnHome.addActionListener(e -> switchTab(btnHome, "home"));
        btnProfile.addActionListener(e -> switchTab(btnProfile, "profile"));
        btnAttendance.addActionListener(e -> switchTab(btnAttendance, "attendance"));
        btnLeave.addActionListener(e -> switchTab(btnLeave, "leave"));
        btnOT.addActionListener(e -> switchTab(btnOT, "ot"));
        btnPayroll.addActionListener(e -> switchTab(btnPayroll, "payroll"));

        selectButton(btnHome);
    }

    // --- HELPER: Switching Tabs ---
    private void switchTab(JButton btn, String cardName) {
        innerCardLayout.show(innerCardPanel, cardName);
        selectButton(btn);
    }

    // --- HELPER: Create Header Button ---
    private JButton createHeaderButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(LOGOUT_BTN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(110, 30));
        return btn;
    }

    // --- HELPER: Create Sidebar Menu Button ---
    private JButton createMenuButton(String text, String iconName) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.PLAIN, 14));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 15, 10, 10) 
        ));
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setPreferredSize(new Dimension(180, 45)); 

        ImageIcon icon = loadIcon(iconName, 22, 22);
        if (icon != null) {
            btn.setIcon(icon);
            btn.setIconTextGap(15);
        }

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != currentSelectedButton) btn.setBackground(BTN_HOVER);
            }
            public void mouseExited(MouseEvent e) {
                if (btn != currentSelectedButton) btn.setBackground(Color.WHITE);
            }
        });

        return btn;
    }

    private ImageIcon loadIcon(String fileName, int w, int h) {
        try {
            java.net.URL imgURL = getClass().getResource("/" + fileName);
            if (imgURL == null) {
                imgURL = getClass().getResource(fileName);
            }
            if (imgURL != null) {
                Image img = new ImageIcon(imgURL).getImage();
                return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void selectButton(JButton btn) {
        if (currentSelectedButton != null) {
            currentSelectedButton.setBackground(Color.WHITE);
            currentSelectedButton.setForeground(Color.BLACK);
            currentSelectedButton.setFont(new Font("Arial", Font.PLAIN, 14));
        }
        btn.setBackground(BTN_HOVER);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Arial", Font.BOLD, 14)); 
        currentSelectedButton = btn;
    }
}