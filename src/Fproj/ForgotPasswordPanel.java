package Fproj;

import javax.swing.*;
import java.awt.*;
import java.security.SecureRandom;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class ForgotPasswordPanel extends JPanel {

    private JTextField usernameField;
    private JTextField emailField;
    private JButton submit;

    public ForgotPasswordPanel() {
        setBackground(new Color(30, 90, 75)); 
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // --- 1. Top Bar (Back Button) ---
        gbc.gridx = 0;
        gbc.gridy = 0; 
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 0, 0); 

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.setOpaque(false);
        
        JButton btnBack = new JButton("Back to Login");
        btnBack.setForeground(Color.WHITE);
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Ensure "main" matches your CardLayout key for the login screen
        btnBack.addActionListener(e -> Main.cardLayout.show(Main.cardPanel, "main"));
        
        topBar.add(btnBack);
        add(topBar, gbc); 

        // --- 2. Title ---
        gbc.gridy = 1; 
        gbc.insets = new Insets(20, 30, 40, 30); 
        
        JLabel title = new JLabel("FORGOT PASSWORD");
        title.setFont(new Font("Serif", Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, gbc);

        // --- 3. Fields ---
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridwidth = 2;

        gbc.gridy = 2; 
        usernameField = createUnderlineTextField("Username/ID");
        add(usernameField, gbc);

        gbc.gridy = 3; 
        emailField = createUnderlineTextField("Email");
        add(emailField, gbc);

        // --- 4. Submit Button ---
        gbc.gridy = 4; 
        gbc.insets = new Insets(30, 0, 30, 0);
        
        submit = new JButton("Submit");
        submit.setFocusPainted(false);
        submit.setForeground(Color.WHITE);
        submit.setBackground(new Color(220, 53, 69)); 
        submit.setFont(new Font("Segoe UI", Font.BOLD, 16));
        submit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submit.setPreferredSize(new Dimension(120, 35));
        submit.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        submit.setBorderPainted(false);
        submit.setOpaque(true);
        submit.setUI(new RoundedButtonUI());
        
        submit.addActionListener(e -> handleSubmit());
        
        add(submit, gbc);
    }

    private JTextField createUnderlineTextField(String placeholder) {
        JTextField tf = new JTextField(20);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tf.setForeground(Color.WHITE);
        tf.setBackground(new Color(30, 90, 75));
        tf.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        tf.setOpaque(false);

        tf.setText(placeholder);
        tf.setForeground(new Color(200, 200, 200));

        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(Color.WHITE);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setText(placeholder);
                    tf.setForeground(new Color(200, 200, 200));
                }
            }
        });
        return tf;
    }

    // --- MAIN LOGIC ---
    private void handleSubmit() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();

        if (username.isEmpty() || username.contains("Username")) {
            JOptionPane.showMessageDialog(this, "Please enter Username/ID");
            return;
        }
        if (email.isEmpty() || email.equalsIgnoreCase("Email")) {
            JOptionPane.showMessageDialog(this, "Please enter your Email");
            return;
        }

        // Check which table the user belongs to
        String sourceTable = Database.checkUserTable(username, email);

        if (sourceTable == null) {
            JOptionPane.showMessageDialog(this, "Account not found.\nPlease check username and email.", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (sourceTable.equals("error")) {
            JOptionPane.showMessageDialog(this, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            // BRANCHING LOGIC HERE
            
            if (sourceTable.equals("employees")) {
                // CASE A: EMPLOYEE -> Send Request to Admin
                Database.createPasswordResetRequest(username, username, email); 
                JOptionPane.showMessageDialog(this, "Verified: Employee Account found.\nA password reset request has been sent to the Admin.");
            
            } else if (sourceTable.equals("as_records")) {
                // CASE B: ADMIN/STAFF -> Immediate Reset (No Approval Needed)
                performImmediateReset(username, email);
            }
            
            // Clear fields
            usernameField.setText("Username/ID");
            usernameField.setForeground(new Color(200, 200, 200));
            emailField.setText("Email");
            emailField.setForeground(new Color(200, 200, 200));
        }
    }

    // --- IMMEDIATE RESET LOGIC (For Admin/Staff) ---
    private void performImmediateReset(String username, String email) {
        String newPass = generateRandomPassword();
        
        // 1. Update Database Directly
        Database.updateUserPassword(username, newPass);
        
        // 2. Send Email
        sendEmail(email, username, username, newPass);
        
        JOptionPane.showMessageDialog(this, 
            "Verified: Admin/Staff Account found.\n\n" +
            "Your password has been successfully reset.\n" +
            "Please check your email for the new credentials.");
    }

    // --- HELPER: Random Password ---
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    // --- HELPER: Send Email ---
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
            msg.setSubject("Admin Password Reset");
            msg.setText("Hello " + name + ",\n\nYour password has been reset successfully.\n\nUsername: " + user + "\nNew Password: " + pass + "\n\nPlease login and change it immediately.");
            Transport.send(msg);
        } catch (MessagingException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to send email. Check internet connection.");
        }
    }
}

// Rounded Button UI
class RoundedButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
    private static final int RADIUS = 20;
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        c.setOpaque(false);
        c.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    }
    @Override
    public void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton)c;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (b.getModel().isPressed()) {
            g2.setColor(b.getBackground().darker());
        } else if (b.getModel().isRollover()) {
            g2.setColor(b.getBackground().brighter());
        } else {
            g2.setColor(b.getBackground());
        }
        g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), RADIUS, RADIUS);
        FontMetrics fm = g.getFontMetrics();
        Rectangle r = b.getBounds();
        String text = b.getText();
        int x = (r.width - fm.stringWidth(text)) / 2;
        int y = (r.height + fm.getAscent()) / 2 - 2;
        g2.setColor(b.getForeground());
        g2.setFont(b.getFont());
        g2.drawString(text, x, y);
        g2.dispose();
    }
}