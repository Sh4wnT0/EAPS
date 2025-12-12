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



 // Fixed width for the form elements to match the reference design

 private final Dimension FIELD_SIZE = new Dimension(400, 40);



 public ForgotPasswordPanel() {

 setBackground(new Color(30, 90, 75)); 

 setLayout(new GridBagLayout());

 GridBagConstraints gbc = new GridBagConstraints();

 

 // =========================================================================

 // 1. TOP BAR: Back Button (Pinned to Top-Left) with RED BACKGROUND

 // =========================================================================

 gbc.gridx = 0;

 gbc.gridy = 0; 

 gbc.gridwidth = 1;

 gbc.weightx = 1.0; 

 gbc.weighty = 0.0; 

 gbc.anchor = GridBagConstraints.NORTHWEST;

 gbc.fill = GridBagConstraints.NONE;

 gbc.insets = new Insets(30, 30, 0, 0); 



 JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

 topBar.setOpaque(false);

 

 // --- STYLING CHANGE: Red Background Button ---

 JButton btnBack = new JButton("< BACK TO LOGIN"); 

 btnBack.setForeground(Color.WHITE);

 btnBack.setBackground(new Color(220, 53, 69)); // Red color (Matches Submit button / Reference)

 btnBack.setFont(new Font("Segoe UI", Font.BOLD, 12)); 

 

 btnBack.setContentAreaFilled(true); // Fill enabled for background color

 btnBack.setBorderPainted(false);

 btnBack.setFocusPainted(false);

 btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));

 btnBack.setOpaque(true); // Ensure color renders

 

 // Add padding inside the button to give it that rectangular shape

 btnBack.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

 

 // Hover effect for Red Button (Darken on hover)

 btnBack.addMouseListener(new java.awt.event.MouseAdapter() {

 public void mouseEntered(java.awt.event.MouseEvent evt) {

 btnBack.setBackground(new Color(200, 40, 55)); // Slightly darker red

 }

 public void mouseExited(java.awt.event.MouseEvent evt) {

 btnBack.setBackground(new Color(220, 53, 69)); // Return to normal red

 }

 });

 

 btnBack.addActionListener(e -> Main.cardLayout.show(Main.cardPanel, "main"));

 

 topBar.add(btnBack);

 add(topBar, gbc); 

 

 // =========================================================================

 // 2. VERTICAL SPACER (Top Glue)

 // =========================================================================

 gbc.gridy = 1;

 gbc.weightx = 0.0;

 gbc.weighty = 0.5; 

 gbc.fill = GridBagConstraints.VERTICAL;

 gbc.insets = new Insets(0, 0, 0, 0);

 add(Box.createVerticalGlue(), gbc); 

 

 // =========================================================================

 // 3. MAIN FORM BLOCK (Centralized)

 // =========================================================================

 

 // --- Title ---

 gbc.gridy = 2;

 gbc.weighty = 0.0; 

 gbc.anchor = GridBagConstraints.CENTER;

 gbc.fill = GridBagConstraints.NONE;

 gbc.insets = new Insets(0, 0, 50, 0); 

 

 JLabel title = new JLabel("FORGOT PASSWORD");

 title.setFont(new Font("Serif", Font.BOLD, 36)); 

 title.setForeground(Color.WHITE);

 title.setHorizontalAlignment(SwingConstants.CENTER);

 add(title, gbc);



 // --- Username Field ---

 gbc.gridy = 3;

 gbc.insets = new Insets(10, 0, 10, 0); 

 

 usernameField = createUnderlineTextField("Username/ID");

 usernameField.setPreferredSize(FIELD_SIZE); 

 add(usernameField, gbc);



 // --- Email Field ---

 gbc.gridy = 4;

 gbc.insets = new Insets(10, 0, 40, 0); 

 

 emailField = createUnderlineTextField("Email");

 emailField.setPreferredSize(FIELD_SIZE); 

 add(emailField, gbc);



 // --- Submit Button ---

 gbc.gridy = 5;

 gbc.insets = new Insets(0, 0, 0, 0);

 

 submit = new JButton("Submit");

 submit.setPreferredSize(FIELD_SIZE); 

 submit.setFocusPainted(false);

 submit.setForeground(Color.WHITE);

 submit.setBackground(new Color(220, 53, 69)); 

 submit.setFont(new Font("Segoe UI", Font.BOLD, 16));

 submit.setCursor(new Cursor(Cursor.HAND_CURSOR));

 

 submit.setBorder(BorderFactory.createEmptyBorder()); 

 submit.setOpaque(true);

 submit.setUI(new RoundedButtonUI()); 

 

 submit.addActionListener(e -> handleSubmit());

 

 add(submit, gbc);

 

 // =========================================================================

 // 4. VERTICAL SPACER (Bottom Glue)

 // =========================================================================

 gbc.gridy = 6;

 gbc.weighty = 0.5;

 gbc.fill = GridBagConstraints.VERTICAL;

 add(Box.createVerticalGlue(), gbc); 

 }



 // --- Component Helper: Underline Text Field ---

 private JTextField createUnderlineTextField(String placeholder) {

 JTextField tf = new JTextField();

 tf.setFont(new Font("Segoe UI", Font.PLAIN, 16));

 tf.setForeground(Color.WHITE);

 tf.setBackground(new Color(30, 90, 75));

 tf.setCaretColor(Color.WHITE); 

 

 // Underline border style

 tf.setBorder(BorderFactory.createCompoundBorder(

 BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)), 

 BorderFactory.createEmptyBorder(5, 0, 5, 0) 

 ));

 tf.setOpaque(false);



 tf.setText(placeholder);

 tf.setForeground(new Color(200, 200, 200)); 



 tf.addFocusListener(new java.awt.event.FocusAdapter() {

 public void focusGained(java.awt.event.FocusEvent e) {

 if (tf.getText().equals(placeholder) || tf.getText().equals("Username/ID") || tf.getText().equals("Email")) {

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



 String sourceTable = Database.checkUserTable(username, email);



 if (sourceTable == null) {

 JOptionPane.showMessageDialog(this, "Account not found.\nPlease check username and email.", "Error", JOptionPane.ERROR_MESSAGE);

 } else if (sourceTable.equals("error")) {

 JOptionPane.showMessageDialog(this, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);

 } else {

 if (sourceTable.equals("employees")) {

 Database.createPasswordResetRequest(username, username, email); 

 JOptionPane.showMessageDialog(this, "Verified: Employee Account found.\nA password reset request has been sent to the Admin.");

 } else if (sourceTable.equals("as_records")) {

 performImmediateReset(username, email);

 }

 usernameField.setText("Username/ID");

 usernameField.setForeground(new Color(200, 200, 200));

 emailField.setText("Email");

 emailField.setForeground(new Color(200, 200, 200));

 }

 }



 private void performImmediateReset(String username, String email) {

 String newPass = generateRandomPassword();

 Database.updateUserPassword(username, newPass);

 sendEmail(email, username, username, newPass);

 JOptionPane.showMessageDialog(this, "Verified: Admin/Staff Account found.\n\nYour password has been successfully reset.\nPlease check your email for the new credentials.");

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

 msg.setSubject("Admin Password Reset");

 msg.setText("Hello " + name + ",\n\nYour password has been reset successfully.\n\nUsername: " + user + "\nNew Password: " + pass + "\n\nPlease login and change it immediately.");

 Transport.send(msg);

 } catch (MessagingException e) {

 e.printStackTrace();

 JOptionPane.showMessageDialog(this, "Failed to send email. Check internet connection.");

 }

 }

}



// Rounded Button UI (For Submit Button)

class RoundedButtonUI extends javax.swing.plaf.basic.BasicButtonUI {

 private static final int RADIUS = 20;

 @Override

 public void installUI(JComponent c) {

 super.installUI(c);

 c.setOpaque(false);

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