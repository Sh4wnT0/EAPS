package Fproj;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class Employee extends JPanel {

    private CardLayout innerCardLayout;
    private JPanel innerCardPanel;

    private String empNo;

    public Employee(String empNo) {
        this.empNo = empNo;

        setLayout(new BorderLayout());

        // ================= HEADER ===================
        ImageIcon originalIcon = new ImageIcon(getClass().getResource("logo.png"));
        Image scaledImage = originalIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JPanel header = new JPanel();
        header.setBackground(new Color(22, 102, 87));
        header.setPreferredSize(new Dimension(100, 50));
        header.setLayout(null);

        JLabel logoLabel = new JLabel(scaledIcon);
        logoLabel.setBounds(10, -11, 75, 75);
        header.add(logoLabel);

        JLabel lblTitle = new JLabel("Employee Dashboard");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setBounds(95, 8, 200, 30);
        header.add(lblTitle);

        // ========== NOTIFICATION BUTTON ==========
        JButton btnNotifications = new JButton("Notifications");
        btnNotifications.setBounds(740, 10, 100, 30);
        btnNotifications.setBackground(new Color(220, 20, 60));
        btnNotifications.setForeground(Color.WHITE);
        btnNotifications.setFocusPainted(false);

        // When clicked → show the 10 recent notifications
        btnNotifications.addActionListener(e -> showNotificationsDialog());

        header.add(btnNotifications);

        // LOGOUT BUTTON
        JButton btnLogout = new JButton("Log-out");
        btnLogout.setBounds(850, 10, 100, 30);
        btnLogout.setBackground(new Color(220, 20, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> Main.cardLayout.show(Main.cardPanel, "main"));
        header.add(btnLogout);

        add(header, BorderLayout.NORTH);

        // ================= SIDEBAR ===================
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(180, 400));
        sidebar.setBackground(new Color(230, 235, 245));
        sidebar.setLayout(new GridLayout(10, 1, 0, 8));

        JButton btnHome = createMenuButton("Home");
        JButton btnAttendance = createMenuButton("Attendance");
        JButton btnLeave = createMenuButton("Leave Request");
        JButton btnPayroll = createMenuButton("Payroll");
        JButton btnOT = createMenuButton("OT Request");

        sidebar.add(btnHome);
        sidebar.add(btnAttendance);
        sidebar.add(btnLeave);
        sidebar.add(btnPayroll);
        sidebar.add(btnOT);

        add(sidebar, BorderLayout.WEST);

        // ================= INNER CARD PANELS ================
        innerCardLayout = new CardLayout();
        innerCardPanel = new JPanel(innerCardLayout);

        innerCardPanel.add(new JScrollPane(new EmpHomePanel(empNo)), "home");
        innerCardPanel.add(new JScrollPane(new EmpAttendancePanel(empNo)), "attendance");
        innerCardPanel.add(new JScrollPane(new EmpLeavePanel(empNo)), "leave");

        add(innerCardPanel, BorderLayout.CENTER);

        // ================ BUTTON LOGIC ======================
        btnHome.addActionListener(e -> innerCardLayout.show(innerCardPanel, "home"));
        btnAttendance.addActionListener(e -> innerCardLayout.show(innerCardPanel, "attendance"));
        btnLeave.addActionListener(e -> innerCardLayout.show(innerCardPanel, "leave"));
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.PLAIN, 15));
        btn.setBorder(BorderFactory.createLineBorder(new Color(180,180,180)));
        btn.setFocusPainted(false);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(200, 220, 255));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });

        return btn;
    }

    // ================================================================
    //            NOTIFICATION RETRIEVAL + DIALOG DISPLAY
    // ================================================================

    /** Retrieves the 10 most recent notifications */
    private java.util.List<String> getRecentNotifications() {
        java.util.List<String> list = new ArrayList<>();

        String sql = "SELECT message, date FROM notifications WHERE empNo=? ORDER BY id DESC LIMIT 10";

        try (Connection conn = Database.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, empNo);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String msg = rs.getString("message");
                String date = rs.getString("date");
                list.add(date + ": " + msg);  // FIXED: Add formatted notification to list
            }

            if (list.isEmpty()) {
                list.add("No new notifications.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            list.add("Error loading notifications.");
        }

        return list;
    }

    /** Shows notifications inside a scrollable dialog */
    private void showNotificationsDialog() {
        java.util.List<String> notifs = getRecentNotifications();

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);

        for (String n : notifs) {
            textArea.append(n + "\n\n");
        }

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(400, 250));

        JOptionPane.showMessageDialog(
                this,
                scroll,
                "Recent Notifications",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
