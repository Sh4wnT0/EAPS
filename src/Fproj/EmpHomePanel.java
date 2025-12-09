package Fproj;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class EmpHomePanel extends JPanel {

    private String empNo;
    private JLabel lblDateTime;  // For real-time date and time
    private Timer timer;         // Timer to update date/time
    private JTextArea txtAnnouncements;  // For announcements

    // Analytics labels
    private JLabel lblTotalWorkdays, lblTotalLates, lblUndertime, lblAbsent, lblLeaves;

    private void loadEmployeeDetails() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:employees.db");

            String sql = "SELECT name, position, email, contact, photo_path FROM employees WHERE empNo=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, empNo);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                String position = rs.getString("position");
                String email = rs.getString("email");
                String contact = rs.getString("contact");
                String photoPath = rs.getString("photo_path");

                // Display image on top left (100x100)
                JLabel lblImage = new JLabel();
                lblImage.setBounds(50, 50, 100, 100);
                lblImage.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                if (photoPath != null && !photoPath.isEmpty()) {
                    File imageFile = new File(photoPath);
                    if (imageFile.exists()) {
                        ImageIcon icon = new ImageIcon(photoPath);
                        Image scaledImage = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                        lblImage.setIcon(new ImageIcon(scaledImage));
                    } else {
                        lblImage.setText("No photo available");
                        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
                    }
                } else {
                    lblImage.setText("No photo available");
                    lblImage.setHorizontalAlignment(SwingConstants.CENTER);
                }
                add(lblImage);

                // Labels next to the image (top left area)
                JLabel lblName = new JLabel("Name: " + name);
                lblName.setBounds(170, 50, 300, 25);
                lblName.setFont(new Font("Arial", Font.PLAIN, 16));
                add(lblName);

                JLabel lblEmpNo = new JLabel("Employee No: " + empNo);
                lblEmpNo.setBounds(170, 80, 300, 25);
                lblEmpNo.setFont(new Font("Arial", Font.PLAIN, 16));
                add(lblEmpNo);

                JLabel lblPosition = new JLabel("Position: " + position);
                lblPosition.setBounds(170, 110, 300, 25);
                lblPosition.setFont(new Font("Arial", Font.PLAIN, 16));
                add(lblPosition);
            }

            rs.close();
            pst.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to load attendance analytics for the current month
    private void loadAttendanceAnalytics() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        // Calculate total workdays (weekdays only, entire month)
        int totalWorkdays = 0;
        for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
            if (isWeekday(date)) {
                totalWorkdays++;
            }
        }

        int totalLates = 0;
        int undertime = 0;
        int absent = 0;
        int leaves = 0;

        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:employees.db");
            
            // Fetch attendance records for the current month
            String sql = "SELECT date, status FROM attendance_records WHERE empNo = ? AND date BETWEEN ? AND ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, empNo);
            pst.setString(2, startOfMonth.toString());
            pst.setString(3, endOfMonth.toString());

            ResultSet rs = pst.executeQuery();
            
            // Track recorded workdays
            java.util.Set<String> recordedDays = new java.util.HashSet<>();
            
            while (rs.next()) {
                String dateStr = rs.getString("date");
                LocalDate date = LocalDate.parse(dateStr);
                String status = rs.getString("status");

                if (isWeekday(date)) {
                    recordedDays.add(dateStr);

                    // Count based on status
                    if ("Late and Undertime".equalsIgnoreCase(status)) {
                        totalLates++;
                        undertime++;
                    } else if ("UL".equalsIgnoreCase(status)) {
                        absent++;
                    } else if ("Leave".equalsIgnoreCase(status)) {
                        leaves++;
                    }
                    // Other statuses (e.g., "Present") are not counted in these metrics
                }
            }

            rs.close();
            pst.close();
            conn.close();

            // Calculate absent: workdays from start of month to today (current day) with no record (plus any "UL" already counted)
            for (LocalDate date = startOfMonth; !date.isAfter(now); date = date.plusDays(1)) {
                if (isWeekday(date)) {
                    String dateStr = date.toString();
                    if (!recordedDays.contains(dateStr)) {
                        absent++;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Update labels
        lblTotalWorkdays.setText("Total Workdays: " + totalWorkdays);
        lblTotalLates.setText("Total Lates: " + totalLates);
        lblUndertime.setText("Undertime: " + undertime);
        lblAbsent.setText("Absent: " + absent);
        lblLeaves.setText("Leaves: " + leaves);
    }

    // Method to load announcements from database (optional)
    private void loadAnnouncements() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:employees.db");
            String sql = "SELECT title, message, date FROM announcements ORDER BY date DESC LIMIT 5";  // Example: Get latest 5
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            StringBuilder announcements = new StringBuilder();
            while (rs.next()) {
                announcements.append(rs.getString("date")).append(" - ").append(rs.getString("title")).append("\n")
                             .append(rs.getString("message")).append("\n\n");
            }
            txtAnnouncements.setText(announcements.toString());

            rs.close();
            pst.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            txtAnnouncements.setText("Error loading announcements.");
        }
    }

    public EmpHomePanel(String empNo) {
        this.empNo = empNo;

        setBackground(Color.WHITE);
        setLayout(null);

        JLabel lbl = new JLabel("Welcome Employee No: " + empNo);
        lbl.setFont(new Font("Arial", Font.BOLD, 22));
        lbl.setBounds(300, 10, 500, 40);  // Adjusted to center-ish, below the top elements
        add(lbl);
        
        loadEmployeeDetails();

        // Date and time label in top right
        lblDateTime = new JLabel();
        lblDateTime.setBounds(600, 50, 200, 25);  // Top right position
        lblDateTime.setFont(new Font("Arial", Font.PLAIN, 14));
        add(lblDateTime);

        // Timer to update date/time every second
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateDateTime();
            }
        });
        timer.start();
        updateDateTime();  // Initial update

        // Analytics Dashboard Section
        JLabel lblAnalyticsTitle = new JLabel("Attendance Analytics (Current Month)", SwingConstants.CENTER);
        lblAnalyticsTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblAnalyticsTitle.setBounds(50, 170, 700, 30);
        add(lblAnalyticsTitle);

        // Analytics labels (card-like display)
        lblTotalWorkdays = new JLabel("Total Workdays: --", SwingConstants.CENTER);
        lblTotalWorkdays.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotalWorkdays.setBounds(50, 210, 150, 50);
        lblTotalWorkdays.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        lblTotalWorkdays.setOpaque(true);
        lblTotalWorkdays.setBackground(new Color(240, 248, 255));
        add(lblTotalWorkdays);

        lblTotalLates = new JLabel("Total Lates: --", SwingConstants.CENTER);
        lblTotalLates.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotalLates.setBounds(220, 210, 150, 50);
        lblTotalLates.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        lblTotalLates.setOpaque(true);
        lblTotalLates.setBackground(new Color(255, 240, 245));
        add(lblTotalLates);

        lblUndertime = new JLabel("Undertime: --", SwingConstants.CENTER);
        lblUndertime.setFont(new Font("Arial", Font.BOLD, 16));
        lblUndertime.setBounds(390, 210, 150, 50);
        lblUndertime.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        lblUndertime.setOpaque(true);
        lblUndertime.setBackground(new Color(255, 250, 205));
        add(lblUndertime);

        lblAbsent = new JLabel("Absent: --", SwingConstants.CENTER);
        lblAbsent.setFont(new Font("Arial", Font.BOLD, 16));
        lblAbsent.setBounds(560, 210, 150, 50);
        lblAbsent.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        lblAbsent.setOpaque(true);
        lblAbsent.setBackground(new Color(255, 228, 225));
        add(lblAbsent);

        lblLeaves = new JLabel("Leaves: --", SwingConstants.CENTER);
        lblLeaves.setFont(new Font("Arial", Font.BOLD, 16));
        lblLeaves.setBounds(50, 270, 150, 50);  // Below the first row
        lblLeaves.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        lblLeaves.setOpaque(true);
        lblLeaves.setBackground(new Color(240, 255, 240));
        add(lblLeaves);

        // Load analytics data
        loadAttendanceAnalytics();

        // Announcements section at the bottom (adjusted position)
        JLabel lblAnnouncementsTitle = new JLabel("Announcements", SwingConstants.CENTER);
        lblAnnouncementsTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblAnnouncementsTitle.setBounds(50, 340, 700, 30);  // Moved down
        add(lblAnnouncementsTitle);

        txtAnnouncements = new JTextArea();
        txtAnnouncements.setEditable(false);
        txtAnnouncements.setBackground(new Color(250, 250, 250));  // Light gray background
        txtAnnouncements.setFont(new Font("Arial", Font.PLAIN, 14));
        txtAnnouncements.setText("• Company Meeting on Friday at 10 AM.\n• New Policy Update: Check your email.\n• Holiday Reminder: Office closed on Monday.\n\n(No new announcements.)");  // Placeholder text

        JScrollPane scrollPane = new JScrollPane(txtAnnouncements);
        scrollPane.setBounds(50, 380, 700, 150);  // Moved down
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane);

        // Optional: Load announcements from DB (uncomment if you have the table)
        // loadAnnouncements();
    }

    // Method to update the date and time label
    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        lblDateTime.setText(now.format(formatter));
    }

    // Helper to check if a date is a weekday (Monday to Friday)
    private boolean isWeekday(LocalDate date) {
        java.time.DayOfWeek day = date.getDayOfWeek();
        return day != java.time.DayOfWeek.SATURDAY && day != java.time.DayOfWeek.SUNDAY;
    }
}