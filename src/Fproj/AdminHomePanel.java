package Fproj;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AdminHomePanel extends JPanel {

    private final Color BRAND_COLOR = new Color(22, 102, 87);
    private final Font VAL_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private final Font LBL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    
    // Clock Labels
    private JLabel lblTime, lblDate;

    // Data Labels
    private JLabel lblTotalEmp, lblRegular, lblContractual, lblProbationary;
    private JLabel lblPresent, lblLate, lblOnLeave, lblAbsent;
    private JLabel lblPendingLeave, lblPendingOT, lblPendingReset, lblPendingPayslip;

    public AdminHomePanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(25, 30, 30, 30));

        // --- 1. TOP HEADER (Title + Digital Clock) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel lblTitle = new JLabel("Dashboard Overview");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(50, 50, 50));
        
        // Digital Clock Panel
        JPanel clockPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        clockPanel.setBackground(Color.WHITE);
        
        lblTime = new JLabel("00:00:00");
        lblTime.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTime.setForeground(BRAND_COLOR);
        
        lblDate = new JLabel("----, --- -- ----");
        lblDate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDate.setForeground(Color.GRAY);
        
        JPanel timeContainer = new JPanel(new BorderLayout());
        timeContainer.setOpaque(false);
        timeContainer.add(lblTime, BorderLayout.NORTH);
        timeContainer.add(lblDate, BorderLayout.SOUTH);
        
        clockPanel.add(timeContainer);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(clockPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. MAIN CONTENT (Grid of Sections) ---
        // Using GridBagLayout for vertical stacking of variable height panels
        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 0, 25)); 
        centerPanel.setBackground(Color.WHITE);

        // ROW 1: Workforce Stats (4 Cards)
        centerPanel.add(createSection("Workforce Overview", createWorkforcePanel()));

        // ROW 2: Attendance Today (4 Cards)
        centerPanel.add(createSection("Attendance Today", createAttendancePanel()));

        // ROW 3: Pending Actions (4 Cards)
        centerPanel.add(createSection("Action Required", createActionPanel()));

        add(centerPanel, BorderLayout.CENTER);

        // --- 3. REFRESH BUTTON ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);
        JButton btnRefresh = new JButton("Refresh Data");
        styleButton(btnRefresh);
        btnRefresh.addActionListener(e -> refreshData());
        bottomPanel.add(btnRefresh);
        add(bottomPanel, BorderLayout.SOUTH);

        // Start Clock & Load Data
        startClock();
        refreshData(); 
    }

    // --- SECTIONS ---
    private JPanel createSection(String title, JPanel cards) {
        JPanel section = new JPanel(new BorderLayout(0, 10));
        section.setBackground(Color.WHITE);
        
        JLabel lblHeader = new JLabel(title);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeader.setForeground(BRAND_COLOR);
        
        section.add(lblHeader, BorderLayout.NORTH);
        section.add(cards, BorderLayout.CENTER);
        return section;
    }

    private JPanel createWorkforcePanel() {
        JPanel p = new JPanel(new GridLayout(1, 4, 15, 0)); // 4 Cols
        p.setBackground(Color.WHITE);

        lblTotalEmp = new JLabel("0");
        lblRegular = new JLabel("0");
        lblContractual = new JLabel("0");
        lblProbationary = new JLabel("0");

        p.add(createCard("Total Employees", lblTotalEmp, new Color(230, 240, 255))); // Blue
        p.add(createCard("Regular", lblRegular, new Color(240, 255, 240)));          // Green Tint
        p.add(createCard("Contractual", lblContractual, new Color(255, 250, 230)));  // Yellow Tint
        p.add(createCard("Probationary", lblProbationary, new Color(245, 240, 255)));// Purple Tint
        return p;
    }

    private JPanel createAttendancePanel() {
        JPanel p = new JPanel(new GridLayout(1, 4, 15, 0));
        p.setBackground(Color.WHITE);

        lblPresent = new JLabel("0");
        lblLate = new JLabel("0");
        lblOnLeave = new JLabel("0");
        lblAbsent = new JLabel("0");

        p.add(createCard("Present", lblPresent, new Color(220, 255, 220)));  // Green
        p.add(createCard("Late", lblLate, new Color(255, 235, 215)));        // Orange
        p.add(createCard("On Leave", lblOnLeave, new Color(245, 245, 245))); // Gray
        p.add(createCard("Absent", lblAbsent, new Color(255, 220, 220)));    // Red
        return p;
    }

    private JPanel createActionPanel() {
        JPanel p = new JPanel(new GridLayout(1, 4, 15, 0));
        p.setBackground(Color.WHITE);

        lblPendingLeave = new JLabel("0");
        lblPendingOT = new JLabel("0");
        lblPendingReset = new JLabel("0");
        lblPendingPayslip = new JLabel("0");

        // Action items use Red tint to indicate urgency
        p.add(createCard("Leave Requests", lblPendingLeave, new Color(255, 235, 235)));
        p.add(createCard("OT / Holiday", lblPendingOT, new Color(255, 235, 235)));
        p.add(createCard("Password Resets", lblPendingReset, new Color(255, 235, 235)));
        p.add(createCard("Payslip Requests", lblPendingPayslip, new Color(255, 235, 235)));
        return p;
    }

    // --- CARD COMPONENT ---
    private JPanel createCard(String title, JLabel valueLabel, Color bg) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bg);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(LBL_FONT);
        lblTitle.setForeground(new Color(80, 80, 80));
        lblTitle.setBorder(new EmptyBorder(15, 0, 5, 0));

        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setFont(VAL_FONT);
        valueLabel.setForeground(BRAND_COLOR);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // --- LOGIC ---
    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            lblTime.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            lblDate.setText(now.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        });
        timer.start();
    }

    public void refreshData() {
        // 1. Workforce
        lblTotalEmp.setText(String.valueOf(Database.countTotalEmployees()));
        lblRegular.setText(String.valueOf(Database.countEmployeesByStatus("Regular")));
        lblContractual.setText(String.valueOf(Database.countEmployeesByStatus("Contractual")));
        lblProbationary.setText(String.valueOf(Database.countEmployeesByStatus("Probationary")));

        // 2. Attendance
        lblPresent.setText(String.valueOf(Database.countPresentToday()));
        lblLate.setText(String.valueOf(Database.countLateToday()));
        lblOnLeave.setText(String.valueOf(Database.countOnLeaveToday()));
        lblAbsent.setText(String.valueOf(Database.countAbsentToday())); // New Logic

        // 3. Actions
        lblPendingLeave.setText(String.valueOf(Database.countPendingLeaves()));
        lblPendingOT.setText(String.valueOf(Database.countPendingOT()));
        lblPendingReset.setText(String.valueOf(Database.countPendingResets()));
        lblPendingPayslip.setText(String.valueOf(Database.countPendingPayslipRequests())); // New Logic
    }

    private void styleButton(JButton btn) {
        btn.setBackground(BRAND_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}