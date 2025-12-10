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
        topPanel.setPreferredSize(new Dimension(0, 180));

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

        // Right: Digital Clock Card
        JPanel clockCard = new JPanel(new BorderLayout());
        clockCard.setBackground(BRAND_COLOR);
        clockCard.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        lblTime = new JLabel("--:--:--", SwingConstants.CENTER);
        lblTime.setFont(CLOCK_FONT);
        lblTime.setForeground(Color.WHITE);
        
        lblDate = new JLabel("----, -- -- ----", SwingConstants.CENTER);
        lblDate.setFont(DATE_FONT);
        lblDate.setForeground(new Color(220, 220, 220));
        
        clockCard.add(lblTime, BorderLayout.CENTER);
        clockCard.add(lblDate, BorderLayout.SOUTH);

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

        statsGrid.add(createStatCard("Work Days", valWorkdays, new Color(230, 240, 255))); // Blue
        statsGrid.add(createStatCard("Present", valPresent, new Color(220, 255, 220)));   // Green
        statsGrid.add(createStatCard("Late", valLate, new Color(255, 245, 200)));         // Yellow
        statsGrid.add(createStatCard("Undertime", valUndertime, new Color(255, 235, 215))); // Orange
        statsGrid.add(createStatCard("Absent", valAbsent, new Color(255, 220, 220)));     // Red

        centerPanel.add(statsGrid, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // --- 3. BOTTOM SECTION: Announcements ---
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.setPreferredSize(new Dimension(0, 200));
        
        JLabel lblAnnounce = new JLabel("Company Announcements");
        lblAnnounce.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bottomPanel.add(lblAnnounce, BorderLayout.NORTH);
        
        JTextArea txtAnnounce = new JTextArea();
        txtAnnounce.setEditable(false);
        txtAnnounce.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtAnnounce.setLineWrap(true);
        txtAnnounce.setWrapStyleWord(true);
        txtAnnounce.setText("• Payday is this Friday (15th).\n• Monthly Meeting: Monday at 10:00 AM in Conference Room A.\n• Reminder: Submit all leave requests by Wednesday.");
        txtAnnounce.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scroll = new JScrollPane(txtAnnounce);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        bottomPanel.add(scroll, BorderLayout.CENTER);
        
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Start Logic ---
        startClock();
        loadEmployeeDetails();
        loadAnalytics();
    }

    // --- UI Helper: Create Stat Card ---
    private JPanel createStatCard(String title, JLabel valueLabel, Color bg) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        // Title Header
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(CARD_TITLE_FONT);
        lblTitle.setForeground(new Color(100, 100, 100));
        lblTitle.setBorder(new EmptyBorder(10, 0, 5, 0));
        
        // Value Center
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setFont(CARD_VAL_FONT);
        valueLabel.setForeground(BRAND_COLOR);
        
        // Footer Strip (Color Code)
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
        try (Connection conn = Database.connect(); // Use your Database class
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

        // 1. Calculate Workdays (M-F) so far this month
        int workDaysSoFar = 0;
        for (LocalDate date = start; !date.isAfter(today); date = date.plusDays(1)) {
            if (isWeekday(date)) workDaysSoFar++;
        }
        valWorkdays.setText(String.valueOf(workDaysSoFar));

        // 2. Fetch Records from DB
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

                // Count logic based on status string
                if (status != null) {
                    if (status.contains("Late")) late++;
                    if (status.contains("Undertime")) under++;
                    
                    if ("Absent".equalsIgnoreCase(status)) {
                        absent++; // Explicitly marked absent
                    } else if (!"Leave".equalsIgnoreCase(status)) {
                        present++; // Present if not absent/leave
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        // 3. Auto-detect unrecorded absences
        // If a workday passed and no record exists, count as Absent
        for (LocalDate date = start; date.isBefore(today); date = date.plusDays(1)) {
            if (isWeekday(date) && !recordedDates.contains(date.toString())) {
                absent++;
            }
        }

        // 4. Update UI
        valPresent.setText(String.valueOf(present));
        valLate.setText(String.valueOf(late));
        valUndertime.setText(String.valueOf(under));
        valAbsent.setText(String.valueOf(absent));
    }

    private boolean isWeekday(LocalDate date) {
        java.time.DayOfWeek day = date.getDayOfWeek();
        return day != java.time.DayOfWeek.SATURDAY && day != java.time.DayOfWeek.SUNDAY;
    }
}