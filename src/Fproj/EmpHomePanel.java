package Fproj;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EmpHomePanel extends JPanel {

    private String empNo;
    private JLabel lblDateTime;  // For real-time date and time
    private Timer timer;         // Timer to update date/time
    private JTextArea txtAnnouncements;  // For announcements

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

    // NEW: Method to load announcements from database (optional)
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

        // NEW: Announcements section at the bottom
        JLabel lblAnnouncementsTitle = new JLabel("Announcements", SwingConstants.CENTER);
        lblAnnouncementsTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblAnnouncementsTitle.setBounds(50, 220, 700, 30);  // Centered title at bottom start
        add(lblAnnouncementsTitle);

        txtAnnouncements = new JTextArea();
        txtAnnouncements.setEditable(false);
        txtAnnouncements.setBackground(new Color(250, 250, 250));  // Light gray background
        txtAnnouncements.setFont(new Font("Arial", Font.PLAIN, 14));
        txtAnnouncements.setText("• Company Meeting on Friday at 10 AM.\n• New Policy Update: Check your email.\n• Holiday Reminder: Office closed on Monday.\n\n(No new announcements.)");  // Placeholder text

        JScrollPane scrollPane = new JScrollPane(txtAnnouncements);
        scrollPane.setBounds(50, 260, 700, 150);  // Spans width, height for scrolling
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
}