package Fproj;

import java.awt.*;
import java.security.SecureRandom;
import java.util.List;
import java.util.Properties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class NotificationDialog extends JDialog {

    private final Color BRAND_COLOR = new Color(22, 102, 87);
    private final Font MSG_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font DATE_FONT = new Font("Segoe UI", Font.ITALIC, 11);
    
    private JPanel contentPanel;
    private String currentAdmin;

    public NotificationDialog(Frame owner, String empNo) {
        super(owner, "Notifications", true);
        this.currentAdmin = empNo;
        setSize(450, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // --- 1. Header ---
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(BRAND_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Inbox");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        
        JButton btnClear = new JButton("Clear History");
        styleHeaderButton(btnClear);
        btnClear.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Clear notification history?", "Confirm", JOptionPane.YES_NO_OPTION);
            if(confirm == JOptionPane.YES_OPTION) {
                Database.clearNotifications(empNo);
                refreshContent();
            }
        });

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnClear, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. Scrollable Content ---
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(245, 245, 245));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. Footer ---
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(Color.WHITE);
        JButton btnClose = new JButton("Close");
        styleButton(btnClose);
        btnClose.addActionListener(e -> dispose());
        footerPanel.add(btnClose);
        add(footerPanel, BorderLayout.SOUTH);

        refreshContent();
    }

    private void refreshContent() {
        contentPanel.removeAll();

        // A. LOAD ACTIONABLE REQUESTS (The Button!)
        List<String[]> resets = Database.getPendingPasswordResets();
        
        if (!resets.isEmpty()) {
            JLabel lblSection = new JLabel("Pending Actions");
            lblSection.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblSection.setForeground(Color.GRAY);
            lblSection.setBorder(new EmptyBorder(5, 5, 5, 5));
            contentPanel.add(lblSection);

            for (String[] req : resets) {
                contentPanel.add(createResetActionCard(req));
                contentPanel.add(Box.createVerticalStrut(10));
            }
            contentPanel.add(new JSeparator());
            contentPanel.add(Box.createVerticalStrut(10));
        }

        // B. LOAD STANDARD NOTIFICATIONS
        List<String[]> notifications = Database.getNotificationsWithDate(currentAdmin);
        
        if (notifications.isEmpty() && resets.isEmpty()) {
            contentPanel.add(Box.createVerticalGlue());
            JLabel lblEmpty = new JLabel("No new notifications.", SwingConstants.CENTER);
            lblEmpty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblEmpty.setForeground(Color.GRAY);
            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(lblEmpty);
            contentPanel.add(Box.createVerticalGlue());
        } else {
            for (String[] notif : notifications) {
                contentPanel.add(createNotificationCard(notif[0], notif[1]));
                contentPanel.add(Box.createVerticalStrut(10));
            }
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // --- CARD WITH RESET BUTTON ---
    private JPanel createResetActionCard(String[] data) {
        String reqId = data[0];
        String userId = data[1];
        String userName = data[2];
        String userEmail = data[3];

        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 165, 0), 1), // Orange border
            new EmptyBorder(12, 12, 12, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        info.add(new JLabel("<html><b>Password Reset:</b> " + userName + " (" + userId + ")</html>"));
        JLabel lblEmail = new JLabel(userEmail);
        lblEmail.setFont(DATE_FONT);
        lblEmail.setForeground(Color.GRAY);
        info.add(lblEmail);

        JButton btnReset = new JButton("Reset");
        btnReset.setBackground(new Color(255, 140, 0)); // Orange
        btnReset.setForeground(Color.WHITE);
        btnReset.setFocusPainted(false);
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnReset.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Generate new password for " + userName + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if(confirm == JOptionPane.YES_OPTION) {
                String newPass = generateRandomPassword();
                Database.performPasswordReset(reqId, userId, newPass);
                sendEmail(userEmail, userName, userId, newPass);
                JOptionPane.showMessageDialog(this, "Password reset! Email sent.");
                refreshContent();
            }
        });

        card.add(info, BorderLayout.CENTER);
        card.add(btnReset, BorderLayout.EAST);
        return card;
    }

    // --- STANDARD NOTIFICATION CARD ---
    private JPanel createNotificationCard(String message, String date) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(12, 12, 12, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JTextArea txtMsg = new JTextArea(message);
        txtMsg.setFont(MSG_FONT);
        txtMsg.setLineWrap(true);
        txtMsg.setWrapStyleWord(true);
        txtMsg.setEditable(false);
        txtMsg.setOpaque(false);

        JLabel lblDate = new JLabel(date);
        lblDate.setFont(DATE_FONT);
        lblDate.setForeground(Color.GRAY);
        lblDate.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(new JLabel("●"), BorderLayout.WEST);
        card.add(txtMsg, BorderLayout.CENTER);
        card.add(lblDate, BorderLayout.SOUTH);
        return card;
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    private void sendEmail(String to, String name, String user, String pass) {
        final String from = "sh4wntolentino@gmail.com"; 
        final String pwd = "dkffdbkmlifnvows"; 

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, pwd);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject("Password Reset Successful");
            msg.setText("Hello " + name + ",\n\nYour new password is: " + pass + "\n\nPlease login and change it immediately.");
            Transport.send(msg);
        } catch (MessagingException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to send email: " + e.getMessage());
        }
    }

    private void styleButton(JButton btn) {
        btn.setBackground(BRAND_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }
    
    private void styleHeaderButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(BRAND_COLOR); 
        btn.setBackground(Color.WHITE); 
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setFocusPainted(false);
    }
}