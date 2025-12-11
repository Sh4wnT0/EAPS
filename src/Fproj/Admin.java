package Fproj; //Update 4

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class Admin extends JPanel {

    // Layout components
    private CardLayout innerCardLayout;
    private JPanel innerCardPanel;
    private String currentAdminUsername;

    // Sidebar buttons
    private JButton btnHome, btnProfile, btnLeaveReq, btnOTReq, btnRecords, btnAttendance, btnPayroll, btnSettings;
    private JButton currentSelectedButton = null;

    // Panel References
    private AdminHomePanel homePanel;
    private adProfile profilePanel;
    private LeaveApproval leavePanel;
    private OTAdmin otPanel;
    private AdminRecords recordsPanel;
    private adminAttendance attendancePanel;
    private adminPayroll payrollPanel;
    private AdminSettings settingPanel;
    // Colors
    private final Color BRAND_COLOR = new Color(22, 102, 87);
    private final Color SIDEBAR_BG = new Color(240, 244, 248);
    private final Color HOVER_COLOR = new Color(220, 230, 240);
    private final Color ACTIVE_COLOR = new Color(200, 220, 255);

    public Admin(String adminUsername) {
        this.currentAdminUsername = adminUsername;
        setLayout(new BorderLayout());

        // --- 1. HEADER SECTION ---
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        // --- 2. SIDEBAR SECTION ---
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // --- 3. CONTENT AREA (CardLayout) ---
        innerCardLayout = new CardLayout();
        innerCardPanel = new JPanel(innerCardLayout);
        innerCardPanel.setBackground(Color.WHITE);

        // Initialize Panels
        homePanel = new AdminHomePanel();
        profilePanel = new adProfile(this.currentAdminUsername);
        leavePanel = new LeaveApproval();
        otPanel = new OTAdmin();
        recordsPanel = new AdminRecords();
        attendancePanel = new adminAttendance();
        payrollPanel = new adminPayroll();
        settingPanel = new AdminSettings();

        // Add Panels to CardLayout
        innerCardPanel.add(homePanel, "home");
        innerCardPanel.add(profilePanel, "profile");
        innerCardPanel.add(leavePanel, "leave");
        innerCardPanel.add(otPanel, "ot");
        innerCardPanel.add(recordsPanel, "records");
        innerCardPanel.add(attendancePanel, "attendance");
        innerCardPanel.add(payrollPanel, "payroll");
        innerCardPanel.add(settingPanel, "settings");

        add(innerCardPanel, BorderLayout.CENTER);

        // Select Home by default
        setupButtonActions();
        innerCardLayout.show(innerCardPanel, "home");
        selectButton(btnHome);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BRAND_COLOR);
        header.setPreferredSize(new Dimension(800, 65)); // Slightly taller
        header.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        // LEFT: Logo and Title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        leftPanel.setOpaque(false);

        ImageIcon logoIcon = null;
        try {
            ImageIcon original = new ImageIcon(getClass().getResource("logo.png"));
            Image img = original.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
            logoIcon = new ImageIcon(img);
        } catch (Exception e) { }

        JLabel lblLogo = new JLabel();
        if (logoIcon != null) lblLogo.setIcon(logoIcon);
        
        JLabel lblTitle = new JLabel("Admin Dashboard");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);

        leftPanel.add(lblLogo);
        leftPanel.add(lblTitle);

        // RIGHT: Buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        rightPanel.setOpaque(false);

        // --- BUTTON 1: NOTIFICATIONS (White Style) ---
        JButton btnNotifs = new JButton("Notifications");
        btnNotifs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNotifs.setForeground(BRAND_COLOR); // Green text
        btnNotifs.setBackground(Color.WHITE); // Solid White bg
        btnNotifs.setFocusPainted(false);
        btnNotifs.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnNotifs.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover Effect
        btnNotifs.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnNotifs.setBackground(new Color(230, 230, 230)); }
            public void mouseExited(MouseEvent e) { btnNotifs.setBackground(Color.WHITE); }
        });

        btnNotifs.addActionListener(e -> {
            java.awt.Window win = SwingUtilities.getWindowAncestor(this);
            if (win instanceof java.awt.Frame) {
                new NotificationDialog((java.awt.Frame) win, currentAdminUsername, true).setVisible(true);
            }
        });

        // --- BUTTON 2: LOGOUT (Red Style) ---
        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(new Color(220, 53, 69)); // Solid Red
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover Effect
        btnLogout.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnLogout.setBackground(new Color(200, 40, 50)); }
            public void mouseExited(MouseEvent e) { btnLogout.setBackground(new Color(220, 53, 69)); }
        });

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if(confirm == JOptionPane.YES_OPTION){
                Main.cardLayout.show(Main.cardPanel, "main");
            }
        });

        rightPanel.add(btnNotifs);
        rightPanel.add(btnLogout);

        header.add(leftPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new GridBagLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 10, 5, 10);

        // Create Buttons
        btnHome = createMenuButton("Home", "Home.png");
        btnProfile = createMenuButton("My Profile", "leave.png");
        btnLeaveReq = createMenuButton("Leave Requests", "otholiday.png");
        btnOTReq = createMenuButton("OT / Holiday", "otholiday.png");
        btnRecords = createMenuButton("Employees", "profile.png");
        btnAttendance = createMenuButton("Attendance", "attendance.png");
        btnPayroll = createMenuButton("Payroll", "payroll.png");
        btnSettings = createMenuButton("Company", "settings.png");
        

        // Add to Sidebar
        sidebar.add(btnHome, gbc); gbc.gridy++;
        sidebar.add(btnProfile, gbc); gbc.gridy++;
        
        // Separator
        gbc.insets = new Insets(15, 10, 5, 10);
        JLabel lblManage = new JLabel("MANAGEMENT");
        lblManage.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblManage.setForeground(Color.GRAY);
        sidebar.add(lblManage, gbc); gbc.gridy++;
        gbc.insets = new Insets(5, 10, 5, 10);

        sidebar.add(btnLeaveReq, gbc); gbc.gridy++;
        sidebar.add(btnOTReq, gbc); gbc.gridy++;
        sidebar.add(btnRecords, gbc); gbc.gridy++;
        sidebar.add(btnAttendance, gbc); gbc.gridy++;
        sidebar.add(btnPayroll, gbc); gbc.gridy++;
        sidebar.add(btnSettings, gbc); gbc.gridy++;

        // Filler
        GridBagConstraints gbcFiller = new GridBagConstraints();
        gbcFiller.gridy = 100;
        gbcFiller.weighty = 1.0;
        sidebar.add(new JPanel(null) {{ setOpaque(false); }}, gbcFiller);

        return sidebar;
    }

    private void setupButtonActions() {
        btnHome.addActionListener(e -> {
            innerCardLayout.show(innerCardPanel, "home");
            selectButton(btnHome);
        });
        btnProfile.addActionListener(e -> {
            innerCardLayout.show(innerCardPanel, "profile");
            selectButton(btnProfile);
        });
        btnLeaveReq.addActionListener(e -> {
            innerCardLayout.show(innerCardPanel, "leave");
            leavePanel.fetchLeaveRequests(); 
            selectButton(btnLeaveReq);
        });
        btnOTReq.addActionListener(e -> {
            innerCardLayout.show(innerCardPanel, "ot");
            otPanel.fetchRequests(); 
            selectButton(btnOTReq);
        });
        btnRecords.addActionListener(e -> {
            innerCardLayout.show(innerCardPanel, "records");
            selectButton(btnRecords);
        });
        btnAttendance.addActionListener(e -> {
            innerCardLayout.show(innerCardPanel, "attendance");
            attendancePanel.loadSummaryTable(); 
            selectButton(btnAttendance);
        });
        btnPayroll.addActionListener(e -> {
            innerCardLayout.show(innerCardPanel, "payroll");
            selectButton(btnPayroll);
        });
        btnSettings.addActionListener(e -> {
            innerCardLayout.show(innerCardPanel, "settings");
            selectButton(btnSettings);
        });
    }

    private JButton createMenuButton(String text, String iconName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(60, 60, 60));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 10));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ImageIcon icon = loadIcon(iconName, 20);
        if (icon != null) {
            btn.setIcon(icon);
            btn.setIconTextGap(20);
        }

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != currentSelectedButton) {
                    btn.setBackground(new Color(235, 245, 240)); 
                }
            }
            public void mouseExited(MouseEvent e) {
                if (btn != currentSelectedButton) {
                    btn.setBackground(Color.WHITE);
                }
            }
        });
        return btn;
    }

    private ImageIcon loadIcon(String fileName, int size) {
        try {
            java.net.URL imgURL = getClass().getResource("/" + fileName);
            if (imgURL != null) {
                ImageIcon originalIcon = new ImageIcon(imgURL);
                Image img = originalIcon.getImage();
                Image scaledImg = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private void selectButton(JButton btn) {
        if (currentSelectedButton != null) {
            currentSelectedButton.setBackground(Color.WHITE);
            currentSelectedButton.setForeground(Color.DARK_GRAY);
            currentSelectedButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }
        currentSelectedButton = btn;
        currentSelectedButton.setBackground(ACTIVE_COLOR);
        currentSelectedButton.setForeground(BRAND_COLOR);
        currentSelectedButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }
}