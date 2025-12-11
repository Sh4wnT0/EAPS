package Fproj;

import java.awt.*;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class EmpHomePanel extends JPanel {

    private String empNo;
    private JLabel lblTime, lblDate;
    private JLabel lblName, lblPosition, lblEmail;
    private JLabel lblImage;
    private JTextArea txtAnnounce;
    
    // Analytics Labels
    private JLabel valWorkdays, valPresent, valLate, valUndertime, valAbsent;

    // UI Constants
    private final Color BRAND_COLOR = new Color(22, 102, 87);
    private final Font CLOCK_FONT = new Font("Segoe UI", Font.BOLD, 42);
    private final Font DATE_FONT = new Font("Segoe UI", Font.PLAIN, 18);
    private final Font CARD_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font CARD_VAL_FONT = new Font("Segoe UI", Font.BOLD, 24);

    public EmpHomePanel(String empNo) {
        this.empNo = empNo;
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // --- 1. TOP SECTION: Profile + Digital Clock ---
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        topPanel.setOpaque(false);
        topPanel.setPreferredSize(new Dimension(0, 200)); // Increased height slightly for buttons

        // Left: Profile Card
        JPanel profileCard = new JPanel(new BorderLayout(15, 0));
        profileCard.setBackground(Color.WHITE);
        profileCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(15, 15, 15, 15)
        ));

        lblImage = new JLabel("No Photo", SwingConstants.CENTER);
        lblImage.setPreferredSize(new Dimension(110, 110));
        lblImage.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 0, 5));
        infoPanel.setBackground(Color.WHITE);
        
        JLabel lblWelcome = new JLabel("Welcome back,");
        lblWelcome.setForeground(Color.GRAY);
        lblName = new JLabel("Loading...");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblName.setForeground(BRAND_COLOR);
        
        lblPosition = new JLabel("Position: --");
        lblEmail = new JLabel("Email: --");
        
        infoPanel.add(lblWelcome);
        infoPanel.add(lblName);
        infoPanel.add(lblPosition);
        infoPanel.add(lblEmail);

        profileCard.add(lblImage, BorderLayout.WEST);
        profileCard.add(infoPanel, BorderLayout.CENTER);

        // Right: Digital Clock Card (UPDATED)
        JPanel clockCard = new JPanel(new BorderLayout());
        clockCard.setBackground(BRAND_COLOR);
        clockCard.setBorder(new EmptyBorder(15, 0, 15, 0));
        
        // Time (Center)
        lblTime = new JLabel("--:--:--", SwingConstants.CENTER);
        lblTime.setFont(CLOCK_FONT);
        lblTime.setForeground(Color.WHITE);
        clockCard.add(lblTime, BorderLayout.CENTER);
        
        // South Container (Date + Buttons)
        JPanel southContainer = new JPanel(new GridLayout(2, 1, 0, 15));
        southContainer.setOpaque(false);
        
        // Date
        lblDate = new JLabel("----, -- -- ----", SwingConstants.CENTER);
        lblDate.setFont(DATE_FONT);
        lblDate.setForeground(new Color(220, 220, 220));
        southContainer.add(lblDate);
        
        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setOpaque(false);
        
        JButton btnIn = new JButton("TIME IN");
        styleClockButton(btnIn, new Color(255, 255, 255), BRAND_COLOR); // White bg, Green text
        btnIn.addActionListener(e -> saveRecord("IN"));
        
        JButton btnOut = new JButton("TIME OUT");
        styleClockButton(btnOut, new Color(255, 100, 100), Color.WHITE); // Light Red bg, White text
        btnOut.addActionListener(e -> saveRecord("OUT"));
        
        btnPanel.add(btnIn);
        btnPanel.add(btnOut);
        
        southContainer.add(btnPanel);
        clockCard.add(southContainer, BorderLayout.SOUTH);

        topPanel.add(profileCard);
        topPanel.add(clockCard);
        add(topPanel, BorderLayout.NORTH);

        // --- 2. CENTER SECTION: Analytics Dashboard ---
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);
        
        JLabel lblDashTitle = new JLabel("Monthly Attendance Overview");
        lblDashTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDashTitle.setForeground(new Color(80, 80, 80));
        centerPanel.add(lblDashTitle, BorderLayout.NORTH);

        JPanel statsGrid = new JPanel(new GridLayout(1, 5, 15, 0));
        statsGrid.setOpaque(false);
        statsGrid.setPreferredSize(new Dimension(0, 120));

        // Create Stat Cards
        valWorkdays = new JLabel("0");
        valPresent = new JLabel("0");
        valLate = new JLabel("0");
        valUndertime = new JLabel("0");
        valAbsent = new JLabel("0");

        statsGrid.add(createStatCard("Work Days", valWorkdays, new Color(230, 240, 255))); 
        statsGrid.add(createStatCard("Present", valPresent, new Color(220, 255, 220)));   
        statsGrid.add(createStatCard("Late", valLate, new Color(255, 245, 200)));         
        statsGrid.add(createStatCard("Undertime", valUndertime, new Color(255, 235, 215))); 
        statsGrid.add(createStatCard("Absent", valAbsent, new Color(255, 220, 220)));     

        centerPanel.add(statsGrid, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // --- 3. BOTTOM SECTION: Announcements ---
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.setPreferredSize(new Dimension(0, 200));
        
        JLabel lblAnnounce = new JLabel("Company Announcements");
        lblAnnounce.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bottomPanel.add(lblAnnounce, BorderLayout.NORTH);
        
        txtAnnounce = new JTextArea();
        txtAnnounce.setEditable(false);
        txtAnnounce.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtAnnounce.setLineWrap(true);
        txtAnnounce.setWrapStyleWord(true);
        txtAnnounce.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scroll = new JScrollPane(txtAnnounce);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        bottomPanel.add(scroll, BorderLayout.CENTER);
        
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Start Logic ---
        startClock();
        loadEmployeeDetails();
        loadAnalytics();
        loadAnnouncements(); 
    }
    
    // --- UI Helper: Specific Style for Clock Buttons ---
    private void styleClockButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20)); // Bigger padding
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // --- LOGIC: Load Announcements ---
    private void loadAnnouncements() {
        StringBuilder content = new StringBuilder();
        String sql = "SELECT date, title, message FROM announcements ORDER BY id DESC LIMIT 5";

        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String date = rs.getString("date");
                String title = rs.getString("title");
                String msg = rs.getString("message");

                content.append("• [").append(date).append("] ").append(title.toUpperCase()).append("\n");
                content.append("   ").append(msg).append("\n\n");
            }

            if (content.length() == 0) {
                txtAnnounce.setText("No announcements yet.");
            } else {
                txtAnnounce.setText(content.toString());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            txtAnnounce.setText("Error loading announcements.");
        }
    }

    // --- UI Helper: Create Stat Card ---
    private JPanel createStatCard(String title, JLabel valueLabel, Color bg) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(CARD_TITLE_FONT);
        lblTitle.setForeground(new Color(100, 100, 100));
        lblTitle.setBorder(new EmptyBorder(10, 0, 5, 0));
        
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setFont(CARD_VAL_FONT);
        valueLabel.setForeground(BRAND_COLOR);
        
        JPanel strip = new JPanel();
        strip.setBackground(bg);
        strip.setPreferredSize(new Dimension(0, 5));

        p.add(lblTitle, BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        p.add(strip, BorderLayout.SOUTH);
        return p;
    }

    // --- LOGIC: Digital Clock ---
    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            lblTime.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            lblDate.setText(now.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        });
        timer.start();
    }

    // --- LOGIC: Employee Info ---
    private void loadEmployeeDetails() {
        String sql = "SELECT name, position, email, photo_path FROM employees WHERE empNo=?";
        try (Connection conn = Database.connect(); 
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, empNo);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                lblName.setText(rs.getString("name"));
                lblPosition.setText("Position: " + rs.getString("position"));
                lblEmail.setText("Email: " + rs.getString("email"));
                
                String path = rs.getString("photo_path");
                if (path != null && new File(path).exists()) {
                    ImageIcon icon = new ImageIcon(path);
                    Image img = icon.getImage().getScaledInstance(110, 110, Image.SCALE_SMOOTH);
                    lblImage.setIcon(new ImageIcon(img));
                    lblImage.setText("");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- LOGIC: Attendance Analytics ---
    private void loadAnalytics() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.withDayOfMonth(1);
        LocalDate end = today.withDayOfMonth(today.lengthOfMonth());

        int workDaysSoFar = 0;
        for (LocalDate date = start; !date.isAfter(today); date = date.plusDays(1)) {
            if (isWeekday(date)) workDaysSoFar++;
        }
        valWorkdays.setText(String.valueOf(workDaysSoFar));

        int present = 0, late = 0, under = 0, absent = 0;
        Set<String> recordedDates = new HashSet<>();

        String sql = "SELECT date, status FROM attendance_records WHERE empNo = ? AND date BETWEEN ? AND ?";
        try (Connection conn = Database.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, empNo);
            pst.setString(2, start.toString());
            pst.setString(3, end.toString());
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String dStr = rs.getString("date");
                String status = rs.getString("status");
                recordedDates.add(dStr);

                if (status != null) {
                    if (status.contains("Late")) late++;
                    if (status.contains("Undertime")) under++;
                    
                    if ("Absent".equalsIgnoreCase(status)) {
                        absent++; 
                    } else if (!"Leave".equalsIgnoreCase(status)) {
                        present++; 
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        for (LocalDate date = start; date.isBefore(today); date = date.plusDays(1)) {
            if (isWeekday(date) && !recordedDates.contains(date.toString())) {
                absent++;
            }
        }

        valPresent.setText(String.valueOf(present));
        valLate.setText(String.valueOf(late));
        valUndertime.setText(String.valueOf(under));
        valAbsent.setText(String.valueOf(absent));
    }

    private boolean isWeekday(LocalDate date) {
        java.time.DayOfWeek day = date.getDayOfWeek();
        return day != java.time.DayOfWeek.SATURDAY && day != java.time.DayOfWeek.SUNDAY;
    }
    
    // --- LOGIC: Time In / Time Out Action ---
    private void saveRecord(String type) {
        try (Connection con = Database.connect()) { // Use Database.connect() helper
            String today = java.time.LocalDate.now().toString();
            String now = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a"));

            // Check existing record
            String checkSql = "SELECT time_in, time_out, total_update_count, workday FROM attendance_records WHERE empNo = ? AND date = ?";
            String currentIn = null;
            String currentOut = null;
            int updates = 0;
            String workday = "regular";
            boolean exists = false;

            try (PreparedStatement ps = con.prepareStatement(checkSql)) {
                ps.setString(1, empNo);
                ps.setString(2, today);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    exists = true;
                    currentIn = rs.getString("time_in");
                    currentOut = rs.getString("time_out");
                    updates = rs.getInt("total_update_count");
                    workday = rs.getString("workday");
                }
            }

            boolean isOT = "OT".equals(workday) || "SH".equals(workday) || "RH".equals(workday);
            int limit = isOT ? 4 : 2;

            if (updates >= limit) {
                JOptionPane.showMessageDialog(this, "Update limit reached for today.");
                return;
            }

            if ("IN".equals(type)) {
                if (exists && currentIn != null && !currentIn.equals("00:00:00 AM")) {
                    JOptionPane.showMessageDialog(this, "You have already timed in.");
                } else if (exists) {
                    // Update existing placeholder (if any)
                    String sql = "UPDATE attendance_records SET time_in = ?, total_update_count = total_update_count + 1 WHERE empNo = ? AND date = ?";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, now);
                        ps.setString(2, empNo);
                        ps.setString(3, today);
                        ps.executeUpdate();
                    }
                    JOptionPane.showMessageDialog(this, "Timed In Successfully!");
                } else {
                    // Insert new
                    String sql = "INSERT INTO attendance_records (empNo, date, time_in, workday, total_update_count, status) VALUES (?, ?, ?, 'regular', 1, 'Working')";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, empNo);
                        ps.setString(2, today);
                        ps.setString(3, now);
                        ps.executeUpdate();
                    }
                    JOptionPane.showMessageDialog(this, "Timed In Successfully!");
                }
            } else if ("OUT".equals(type)) {
                if (!exists || currentIn == null || currentIn.equals("00:00:00 AM")) {
                    JOptionPane.showMessageDialog(this, "Please Time In first.");
                } else if (currentOut != null && !currentOut.isEmpty()) {
                    // Handle second time out for OT logic if needed, strictly simpler here:
                    if(isOT) {
                         String sql = "UPDATE attendance_records SET second_time_out = ?, total_update_count = total_update_count + 1 WHERE empNo = ? AND date = ?";
                         try (PreparedStatement ps = con.prepareStatement(sql)) {
                            ps.setString(1, now);
                            ps.setString(2, empNo);
                            ps.setString(3, today);
                            ps.executeUpdate();
                        }
                        JOptionPane.showMessageDialog(this, "OT Timed Out Successfully!");
                    } else {
                        JOptionPane.showMessageDialog(this, "You have already timed out.");
                    }
                } else {
                    String sql = "UPDATE attendance_records SET time_out = ?, total_update_count = total_update_count + 1 WHERE empNo = ? AND date = ?";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, now);
                        ps.setString(2, empNo);
                        ps.setString(3, today);
                        ps.executeUpdate();
                    }
                    JOptionPane.showMessageDialog(this, "Timed Out Successfully!");
                }
            }
            
            // Refresh Analytics
            loadAnalytics();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }
}