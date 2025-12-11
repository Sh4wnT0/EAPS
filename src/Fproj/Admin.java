package Fproj;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer; // Import needed for the callback
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Admin extends JPanel {

    private CardLayout innerCardLayout;
    private JPanel innerCardPanel;
    private String currentAdminUsername;
    private JButton currentSelectedButton = null;

    // --- Buttons ---
    private JButton btnHome, btnProfile, btnRecords, btnAttendance, btnLeave, btnOT, btnPayroll, btnSettings;

    // --- Colors ---
    private final Color HEADER_COLOR = new Color(22, 102, 87);
    private final Color SIDEBAR_BG = new Color(230, 235, 245);
    private final Color BTN_HOVER = new Color(200, 220, 255);
    private final Color LOGOUT_BTN = new Color(220, 20, 60);

    public Admin(String username) {
        this.currentAdminUsername = username;
        setLayout(new BorderLayout());

        // =================================================================================
        // 1. HEADER SECTION
        // =================================================================================
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_COLOR);
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Left: Logo + Title
        JPanel branding = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        branding.setOpaque(false);
        
        JLabel logo = new JLabel(); 
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/Fproj/logo.png"));
            Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            logo.setIcon(new ImageIcon(img));
        } catch (Exception e) { /* Ignore */ }

        JLabel title = new JLabel("Management Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        
        branding.add(logo);
        branding.add(title);

        // Right: Notifications + Logout
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnNotifs = new JButton("Notifications");
        styleHeaderButton(btnNotifs, LOGOUT_BTN); 
        
        // --- STEP 3 APPLIED: Navigation Logic Callback ---
        // This defines how the NotificationDialog switches tabs
        Consumer<String> navLogic = (cardName) -> {
            // 1. Switch the main view
            innerCardLayout.show(innerCardPanel, cardName);
            
            // 2. Highlight the correct sidebar button
            if (cardName.equals("leave")) selectButton(btnLeave);
            else if (cardName.equals("ot")) selectButton(btnOT);
            else if (cardName.equals("attendance")) selectButton(btnAttendance);
            else if (cardName.equals("payroll")) selectButton(btnPayroll);
            else if (cardName.equals("records")) selectButton(btnRecords);
        };

        btnNotifs.addActionListener(e -> {
            Window win = SwingUtilities.getWindowAncestor(this);
            if (win instanceof Frame) {
                // Pass navLogic to the dialog
                new NotificationDialog((Frame) win, currentAdminUsername, true, navLogic).setVisible(true);
            }
        });

        JButton btnLogout = new JButton("Log-out");
        styleHeaderButton(btnLogout, LOGOUT_BTN);
        btnLogout.addActionListener(e -> Main.cardLayout.show(Main.cardPanel, "main"));

        actions.add(btnNotifs);
        actions.add(btnLogout);

        header.add(branding, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // =================================================================================
        // 2. SIDEBAR SECTION
        // =================================================================================
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        JPanel menuContainer = new JPanel(new GridLayout(0, 1, 0, 5));
        menuContainer.setOpaque(false);
        menuContainer.setBorder(new EmptyBorder(15, 10, 15, 10));

        // Initialize Buttons
        btnHome = createMenuButton("Dashboard", "Home.png");
        btnProfile = createMenuButton("My Profile", "leave.png"); 
        btnRecords = createMenuButton("Employee Records", "payroll.png");
        btnAttendance = createMenuButton("Attendance Logs", "attendance.png");
        btnLeave = createMenuButton("Leave Requests", "otholiday.png");
        btnOT = createMenuButton("OT / Holiday", "otholiday.png");
        btnPayroll = createMenuButton("Payroll Processing", "payroll.png");
        btnSettings = createMenuButton("Company Settings", "settings.png");

        menuContainer.add(btnHome);
        menuContainer.add(btnProfile);
        menuContainer.add(btnRecords);
        menuContainer.add(btnAttendance);
        menuContainer.add(btnLeave);
        menuContainer.add(btnOT);
        menuContainer.add(btnPayroll);
        menuContainer.add(btnSettings);

        sidebar.add(menuContainer, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);

        // =================================================================================
        // 3. MAIN CONTENT AREA
        // =================================================================================
        innerCardLayout = new CardLayout();
        innerCardPanel = new JPanel(innerCardLayout);
        innerCardPanel.setBackground(Color.WHITE);

        // Load Sub-Panels (Important: The string keys here match the 'cardName' passed in navLogic)
        innerCardPanel.add(new AdminHomePanel(), "home");
        innerCardPanel.add(new adProfile(currentAdminUsername), "profile");
        innerCardPanel.add(new AdminRecords(), "records");
        innerCardPanel.add(new adminAttendance(), "attendance");
        innerCardPanel.add(new LeaveApproval(), "leave");
        innerCardPanel.add(new OTAdmin(), "ot");
        innerCardPanel.add(new adminPayroll(), "payroll");
        innerCardPanel.add(new AdminSettings(), "settings");

        add(innerCardPanel, BorderLayout.CENTER);

        // =================================================================================
        // 4. ROLE-BASED ACCESS CONTROL
        // =================================================================================
        applyRolePermissions();

        // =================================================================================
        // 5. BUTTON LISTENERS
        // =================================================================================
        setupNavigation(btnHome, "home");
        setupNavigation(btnProfile, "profile");
        setupNavigation(btnRecords, "records");
        setupNavigation(btnAttendance, "attendance");
        setupNavigation(btnLeave, "leave");
        setupNavigation(btnOT, "ot");
        setupNavigation(btnPayroll, "payroll");
        setupNavigation(btnSettings, "settings");

        selectButton(btnHome);
        innerCardLayout.show(innerCardPanel, "home");
    }

    // -----------------------------------------------------------
    //                  ROLE MANAGEMENT LOGIC
    // -----------------------------------------------------------
    private void applyRolePermissions() {
        String position = Database.getASPosition(currentAdminUsername);
        String role = (position != null) ? position.toLowerCase() : "admin";

        // 1. HR OFFICER
        if (role.contains("hr") || role.contains("human resource")) {
            btnPayroll.setVisible(false);
            btnOT.setVisible(false); 
            btnSettings.setVisible(false);
        } 
        // 2. ACCOUNTANT / PAYROLL
        else if (role.contains("accountant") || role.contains("payroll")) {
            btnRecords.setVisible(false);
            btnLeave.setVisible(false);
            btnAttendance.setVisible(false);
            btnSettings.setVisible(false);
        }
    }

    // -----------------------------------------------------------
    //                  UI HELPERS
    // -----------------------------------------------------------
    
    private void setupNavigation(JButton btn, String cardName) {
        btn.addActionListener(e -> {
            selectButton(btn);
            innerCardLayout.show(innerCardPanel, cardName);
        });
    }

    private JButton createMenuButton(String text, String iconName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.DARK_GRAY);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            new EmptyBorder(10, 20, 10, 10)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 45));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != currentSelectedButton) btn.setBackground(SIDEBAR_BG);
            }
            public void mouseExited(MouseEvent e) {
                if (btn != currentSelectedButton) btn.setBackground(Color.WHITE);
            }
        });

        try {
            java.net.URL imgURL = getClass().getResource("/Fproj/" + iconName);
            if (imgURL == null) imgURL = getClass().getResource("/" + iconName);
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(img));
                btn.setIconTextGap(15);
            }
        } catch (Exception e) {}

        return btn;
    }

    private void styleHeaderButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(5, 15, 5, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void selectButton(JButton btn) {
        if (currentSelectedButton != null) {
            currentSelectedButton.setBackground(Color.WHITE);
            currentSelectedButton.setForeground(Color.DARK_GRAY);
            currentSelectedButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }
        currentSelectedButton = btn;
        btn.setBackground(BTN_HOVER);
        btn.setForeground(new Color(22, 102, 87)); 
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }
}