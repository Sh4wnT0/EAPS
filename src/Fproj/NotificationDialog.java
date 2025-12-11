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
    private String currentUser;
    private boolean isAdmin;

    // --- CONSTRUCTOR ---
    public NotificationDialog(Frame owner, String empNo, boolean isAdmin) {
        super(owner, isAdmin ? "Pending Requests" : "Notifications", true);
        this.currentUser = empNo;
        this.isAdmin = isAdmin;
        
        setSize(450, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // --- 1. Header ---
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(BRAND_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel(isAdmin ? "Action Required" : "Inbox");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        
        // "Clear History" button only for Employees (Admins manage live requests)
        if (!isAdmin) {
            JButton btnClear = new JButton("Clear History");
            styleHeaderButton(btnClear);
            btnClear.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this, "Clear notification history?", "Confirm", JOptionPane.YES_NO_OPTION);
                if(confirm == JOptionPane.YES_OPTION) {
                    Database.clearNotifications(empNo);
                    refreshContent();
                }
            });
            headerPanel.add(btnClear, BorderLayout.EAST);
        }

        headerPanel.add(lblTitle, BorderLayout.WEST);
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

        if (isAdmin) {
            loadAdminRequests();
        } else {
            loadEmployeeNotifications();
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // ==========================================
    //           ADMIN VIEW (REQUESTS)
    // ==========================================
    private void loadAdminRequests() {
        List<String[]> requests = Database.getAdminPendingRequests();

        if (requests.isEmpty()) {
            addEmptyMessage("No pending requests.");
        } else {
            for (String[] req : requests) {
                // req: [0=ID, 1=EmpNo, 2=Name, 3=Type, 4=Details, 5=Date]
                String type = req[3];
                
                if ("Password Reset".equalsIgnoreCase(type)) {
                    contentPanel.add(createPasswordResetCard(req));
                } else {
                    contentPanel.add(createGenericRequestCard(req));
                }
                contentPanel.add(Box.createVerticalStrut(10));
            }
        }
    }

    private JPanel createPasswordResetCard(String[] data) {
        String reqId = data[0];
        String userId = data[1];
        String name = data[2];
        String date = data[5];

        JPanel card = new JPanel(new BorderLayout(10, 10));
        styleCard(card);

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        info.add(new JLabel("<html><b>Password Reset:</b> " + name + " (" + userId + ")</html>"));
        JLabel lblDate = new JLabel("Requested: " + date);
        lblDate.setFont(DATE_FONT);
        lblDate.setForeground(Color.GRAY);
        info.add(lblDate);

        JButton btnReset = new JButton("Process");
        btnReset.setBackground(new Color(255, 140, 0)); // Orange
        btnReset.setForeground(Color.WHITE);
        btnReset.setFocusPainted(false);
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnReset.addActionListener(e -> {
            // Prompt for email since we don't have it in the summary list
            String email = JOptionPane.showInputDialog(this, "Enter email to send new password:", "Send Credentials", JOptionPane.QUESTION_MESSAGE);
            if (email != null && !email.isEmpty()) {
                performPassReset(reqId, userId, name, email);
            }
        });

        card.add(info, BorderLayout.CENTER);
        card.add(btnReset, BorderLayout.EAST);
        return card;
    }

    private JPanel createGenericRequestCard(String[] data) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        styleCard(card);

        String type = data[3];
        String name = data[2];
        String date = data[5];

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        info.add(new JLabel("<html><b>" + type + " Request:</b> " + name + "</html>"));
        
        JLabel lblDetails = new JLabel("Date: " + date);
        lblDetails.setFont(DATE_FONT);
        lblDetails.setForeground(Color.GRAY);
        info.add(lblDetails);

        JLabel lblStatus = new JLabel("Pending");
        lblStatus.setForeground(new Color(220, 53, 69)); 
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));

        card.add(info, BorderLayout.CENTER);
        card.add(lblStatus, BorderLayout.EAST);
        return card;
    }

    // ==========================================
    //        EMPLOYEE VIEW (NOTIFICATIONS)
    // ==========================================
    private void loadEmployeeNotifications() {
        List<String[]> notifications = Database.getNotificationsWithDate(currentUser);

        if (notifications.isEmpty()) {
            addEmptyMessage("No new notifications.");
        } else {
            for (String[] notif : notifications) {
                contentPanel.add(createNotificationCard(notif[0], notif[1]));
                contentPanel.add(Box.createVerticalStrut(10));
            }
        }
    }

    private JPanel createNotificationCard(String message, String date) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        styleCard(card);

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

    // ==========================================
    //              HELPER LOGIC
    // ==========================================
    private void performPassReset(String reqId, String userId, String name, String email) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Generate new password for " + name + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            
        if(confirm == JOptionPane.YES_OPTION) {
            String newPass = generateRandomPassword();
            try {
                Database.performPasswordReset(reqId, userId, newPass);
                sendEmail(email, name, userId, newPass);
                JOptionPane.showMessageDialog(this, "Password reset! Email sent.");
                refreshContent();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    private void sendEmail(String to, String name, String user, String pass) throws MessagingException {
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

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject("Password Reset Successful");
        msg.setText("Hello " + name + ",\n\nYour new password is: " + pass + "\n\nPlease login and change it immediately.");
        Transport.send(msg);
    }

    // --- STYLING ---
    private void styleCard(JPanel p) {
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(12, 12, 12, 12)
        ));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
    }

    private void addEmptyMessage(String msg) {
        contentPanel.add(Box.createVerticalGlue());
        JLabel lbl = new JLabel(msg, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(Color.GRAY);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(lbl);
        contentPanel.add(Box.createVerticalGlue());
    }

    private void styleButton(JButton btn) {
        btn.setBackground(BRAND_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void styleHeaderButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(BRAND_COLOR); 
        btn.setBackground(Color.WHITE); 
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}