package Fproj;



import javax.swing.*;

import javax.swing.border.EmptyBorder;

import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;

import java.awt.geom.Ellipse2D;

import java.awt.image.BufferedImage;

import java.io.File;

import java.nio.file.*;

import java.sql.*;



public class empProfile extends JPanel {



 // ========= COMPONENTS =========

 private JLabel lblPhoto;

 private JLabel lblNameVal, lblRoleVal, lblPosVal, lblAddressVal, lblEmailVal, lblContactVal;

 private JLabel lblEmpNoVal, lblStatusVal, lblDailyPayVal;

 private JLabel lblVacBal, lblSickBal, lblEmerBal, lblSpecialBal;

 private JTextArea txtDetails;



 private JButton btnUpload, btnUpdateInfo, btnUpdateCreds;



 // ========= DATA =========

 private String empNo;

 private String photoPath;



 // ========= STYLE =========

 private final Color BRAND_COLOR = new Color(22, 102, 87);

 private final Color BG_COLOR = new Color(240, 245, 255);

 private final Color TEXT_DARK = new Color(40, 40, 40);

 private final Color TEXT_LABEL = new Color(140, 140, 140);

 private final Font FONT_VAL = new Font("Segoe UI", Font.BOLD, 14);

 private final Font FONT_CAP = new Font("Segoe UI", Font.BOLD, 10);



 public empProfile(String empNo) {

 this.empNo = empNo;



 setBackground(BG_COLOR);

 setLayout(new GridBagLayout());



 // ========= MAIN CARD =========

 JPanel card = new JPanel(new GridBagLayout());

 card.setBackground(Color.WHITE);

 card.setBorder(BorderFactory.createCompoundBorder(

 BorderFactory.createLineBorder(new Color(220, 220, 220), 1),

 new EmptyBorder(40, 50, 40, 50)

 ));



 GridBagConstraints gbc = new GridBagConstraints();



 // ========= PHOTO =========

 lblPhoto = new CircularImageLabel();

 lblPhoto.setPreferredSize(new Dimension(150, 150));



 gbc.gridx = 0;

 gbc.gridy = 0;

 gbc.gridheight = 15;

 gbc.anchor = GridBagConstraints.NORTH;

 gbc.insets = new Insets(0, 0, 0, 30);

 card.add(lblPhoto, gbc);



 btnUpload = new JButton("Change Photo");

 styleButton(btnUpload);

 btnUpload.addActionListener(e -> uploadPhoto());



 gbc.gridy = 7;

 gbc.gridheight = 1;

 gbc.fill = GridBagConstraints.HORIZONTAL;

 gbc.insets = new Insets(15, 0, 0, 30);

 card.add(btnUpload, gbc);



 // ========= DETAILS =========

 gbc.gridx = 1;

 gbc.gridheight = 1;

 gbc.fill = GridBagConstraints.HORIZONTAL;

 gbc.weightx = 1.0;



 int row = 0;



 addDetailRow(card, "FULL NAME", lblNameVal = new JLabel("..."), gbc, row++);

 addDetailRow(card, "EMPLOYEE NO", lblEmpNoVal = new JLabel("..."), gbc, row++);

 addDetailRow(card, "ROLE", lblRoleVal = new JLabel("..."), gbc, row++);

 addDetailRow(card, "POSITION", lblPosVal = new JLabel("..."), gbc, row++);

 addDetailRow(card, "EMPLOYMENT STATUS", lblStatusVal = new JLabel("..."), gbc, row++);

 addDetailRow(card, "DAILY PAY", lblDailyPayVal = new JLabel("..."), gbc, row++);



 JSeparator sep = new JSeparator();

 sep.setForeground(new Color(230, 230, 230));

 gbc.gridy = row * 2;

 gbc.insets = new Insets(15, 0, 15, 0);

 card.add(sep, gbc);

 row++;



 addDetailRow(card, "EMAIL ADDRESS", lblEmailVal = new JLabel("..."), gbc, row++);

 addDetailRow(card, "CONTACT NUMBER", lblContactVal = new JLabel("..."), gbc, row++);

 addDetailRow(card, "HOME ADDRESS", lblAddressVal = new JLabel("..."), gbc, row++);



 



 // ========= BUTTONS =========

 JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));

 buttonPanel.setBackground(Color.WHITE);



 btnUpdateInfo = new JButton("Edit Info");

 styleButton(btnUpdateInfo);

 btnUpdateInfo.setBackground(new Color(70, 130, 180));

 btnUpdateInfo.addActionListener(e -> openUpdateInfoDialog());



 btnUpdateCreds = new JButton("Security Settings");

 styleButton(btnUpdateCreds);

 btnUpdateCreds.setBackground(new Color(220, 53, 69));

 btnUpdateCreds.addActionListener(e -> openUpdateCredentialsDialog());



 buttonPanel.add(btnUpdateInfo);

 buttonPanel.add(btnUpdateCreds);



 gbc.gridx = 0;

 gbc.gridy = 50;

 gbc.gridwidth = 2;

 gbc.insets = new Insets(30, 0, 0, 0);

 card.add(buttonPanel, gbc);



 txtDetails = new JTextArea("Please keep your employee profile up to date.");

 txtDetails.setEditable(false);

 txtDetails.setBackground(new Color(250, 250, 250));

 txtDetails.setForeground(TEXT_LABEL);

 txtDetails.setFont(new Font("Segoe UI", Font.ITALIC, 12));

 txtDetails.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));



 gbc.gridy = 51;

 gbc.insets = new Insets(15, 0, 0, 0);

 card.add(txtDetails, gbc);



 add(card);



 loadEmployeeDetails();

 }



 // ========= HELPERS =========

 private void addDetailRow(JPanel panel, String caption, JLabel valueLabel, GridBagConstraints gbc, int logicalRow) {

 int y = logicalRow * 2;



 JLabel cap = new JLabel(caption);

 cap.setFont(FONT_CAP);

 cap.setForeground(TEXT_LABEL);



 gbc.gridy = y;

 gbc.insets = new Insets(10, 0, 2, 0);

 panel.add(cap, gbc);



 valueLabel.setFont(FONT_VAL);

 valueLabel.setForeground(TEXT_DARK);



 gbc.gridy = y + 1;

 gbc.insets = new Insets(0, 0, 0, 0);

 panel.add(valueLabel, gbc);

 }



 private void styleButton(JButton btn) {

 btn.setBackground(BRAND_COLOR);

 btn.setForeground(Color.WHITE);

 btn.setFocusPainted(false);

 btn.setFont(new Font("Segoe UI", Font.BOLD, 12));

 btn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

 btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

 }



 // ========= CIRCULAR IMAGE =========

 private class CircularImageLabel extends JLabel {

 @Override

 protected void paintComponent(Graphics g) {

 Icon icon = getIcon();

 if (icon != null) {

 BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

 Graphics2D g2 = bi.createGraphics();

 g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

 g2.setClip(new Ellipse2D.Float(0, 0, getWidth(), getHeight()));

 g2.drawImage(((ImageIcon) icon).getImage(), 0, 0, getWidth(), getHeight(), null);



 g2.setClip(null);

 g2.setColor(new Color(230, 230, 230));

 g2.setStroke(new BasicStroke(3));

 g2.drawOval(1, 1, getWidth()-3, getHeight()-3);

 g2.dispose();

 g.drawImage(bi, 0, 0, null);

 } else {

 super.paintComponent(g);

 }

 }

 }



 // ========= LOAD EMPLOYEE =========

 private void loadEmployeeDetails() {

 String sql = "SELECT * FROM employees WHERE empNo = ?";



 try (Connection conn = Database.connect();

 PreparedStatement pst = conn.prepareStatement(sql)) {



 pst.setString(1, empNo);

 ResultSet rs = pst.executeQuery();



 if (rs.next()) {

 lblNameVal.setText(rs.getString("name"));

 lblEmpNoVal.setText(rs.getString("empNo"));

 lblRoleVal.setText(rs.getString("role"));

 lblPosVal.setText(rs.getString("position"));

 lblStatusVal.setText(rs.getString("employmentStatus"));

 lblDailyPayVal.setText("₱" + rs.getInt("dailyPay"));



 lblAddressVal.setText(rs.getString("address"));

 lblEmailVal.setText(rs.getString("email"));

 lblContactVal.setText(rs.getString("contact"));



 photoPath = rs.getString("photo_path");



 if (photoPath != null && !photoPath.isEmpty() && new File(photoPath).exists()) {

 ImageIcon icon = new ImageIcon(photoPath);

 Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);

 lblPhoto.setIcon(new ImageIcon(img));

 } else {

 lblPhoto.setText("No Photo");

 }

 }



 } catch (SQLException e) {

 e.printStackTrace();

 JOptionPane.showMessageDialog(this, "Failed loading employee profile.");

 }

 }



 // ========= PHOTO UPLOAD =========

 private void uploadPhoto() {

 JFileChooser chooser = new JFileChooser();

 chooser.setDialogTitle("Select Profile Photo");

 chooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "jpeg", "png"));



 if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

 File selectedFile = chooser.getSelectedFile();

 try {

 Path photoDir = Paths.get("employee_photos");

 if (!Files.exists(photoDir)) Files.createDirectories(photoDir);



 String ext = selectedFile.getName().substring(selectedFile.getName().lastIndexOf('.') + 1);

 String newFileName = empNo + "_" + System.currentTimeMillis() + "." + ext;

 Path destPath = photoDir.resolve(newFileName);



 Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);



 try (Connection conn = Database.connect();

 PreparedStatement pst = conn.prepareStatement("UPDATE employees SET photo_path = ? WHERE empNo = ?")) {

 pst.setString(1, destPath.toString());

 pst.setString(2, empNo);

 pst.executeUpdate();

 }



 ImageIcon icon = new ImageIcon(destPath.toString());

 Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);

 lblPhoto.setIcon(new ImageIcon(img));

 lblPhoto.setText("");



 JOptionPane.showMessageDialog(this, "Photo updated!");



 } catch (Exception ex) {

 ex.printStackTrace();

 JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());

 }

 }

 }



 // ========= UPDATE INFO =========

 private void openUpdateInfoDialog() {

 JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Information", true);

 dialog.setSize(420, 480);

 dialog.setLocationRelativeTo(this);



 JPanel p = new JPanel(new GridLayout(6, 2, 10, 15));

 p.setBackground(Color.WHITE);

 p.setBorder(new EmptyBorder(30, 30, 30, 30));



 JTextField txtName = new JTextField(lblNameVal.getText());

 JTextField txtAddress = new JTextField(lblAddressVal.getText());

 JTextField txtEmail = new JTextField(lblEmailVal.getText());

 JTextField txtContact = new JTextField(lblContactVal.getText());



 p.add(new JLabel("Full Name:")); p.add(txtName);

 p.add(new JLabel("Email:")); p.add(txtEmail);

 p.add(new JLabel("Contact No:")); p.add(txtContact);

 p.add(new JLabel("Address:")); p.add(txtAddress);



 JButton btnSave = new JButton("Save Changes");

 styleButton(btnSave);



 btnSave.addActionListener(e -> {

 if (updateInfo(txtName.getText(), txtAddress.getText(), txtEmail.getText(), txtContact.getText())) {

 dialog.dispose();

 loadEmployeeDetails();

 }

 });



 p.add(new JLabel(""));

 p.add(btnSave);

 dialog.add(p);

 dialog.setVisible(true);

 }



 private boolean updateInfo(String name, String address, String email, String contact) {

 String sql = "UPDATE employees SET name=?, address=?, email=?, contact=? WHERE empNo=?";

 try (Connection conn = Database.connect();

 PreparedStatement pst = conn.prepareStatement(sql)) {



 pst.setString(1, name);

 pst.setString(2, address);

 pst.setString(3, email);

 pst.setString(4, contact);

 pst.setString(5, empNo);



 return pst.executeUpdate() > 0;

 } catch (SQLException e) {

 e.printStackTrace();

 JOptionPane.showMessageDialog(this, "Update failed.");

 }

 return false;

 }



 // ========= UPDATE PASSWORD =========

 private void openUpdateCredentialsDialog() {

 JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Security Settings", true);

 dialog.setSize(400, 350); // Increased size to accommodate the new field

 dialog.setLocationRelativeTo(this);



 JPanel p = new JPanel(new GridLayout(7, 1, 10, 10)); // Changed from 5 to 7 rows

 p.setBorder(new EmptyBorder(30, 30, 30, 30));

 p.setBackground(Color.WHITE);



 JTextField txtEmpNo = new JTextField(empNo);

 txtEmpNo.setEditable(false);



 JPasswordField txtOld = new JPasswordField();

 JPasswordField txtNew = new JPasswordField();

 JPasswordField txtReconfirm = new JPasswordField(); // New Reconfirm Password field



 p.add(new JLabel("Employee No:"));

 p.add(txtEmpNo);

 p.add(new JLabel("Current Password:"));

 p.add(txtOld);

 p.add(new JLabel("New Password:"));

 p.add(txtNew);

 p.add(new JLabel("Reconfirm Password:")); // New Label

 p.add(txtReconfirm); // New Field



 JButton btnSave = new JButton("Update Password");

 styleButton(btnSave);

 btnSave.setBackground(new Color(220, 53, 69));



 btnSave.addActionListener(e -> {

 // Pass both new password and reconfirm password to the update method

 if (updatePassword(new String(txtOld.getPassword()), new String(txtNew.getPassword()), new String(txtReconfirm.getPassword()))) {

 dialog.dispose();

 JOptionPane.showMessageDialog(this, "Password updated.");

 }

 });



 p.add(btnSave);

 dialog.add(p);

 dialog.setVisible(true);

 }



 private boolean updatePassword(String oldPass, String newPass, String reconfirmPass) { // Added reconfirmPass

 if (oldPass.isEmpty() || newPass.isEmpty() || reconfirmPass.isEmpty()) {

 JOptionPane.showMessageDialog(this, "All password fields must be filled.");

 return false;

 }



 // Check if the new password and the reconfirm password match

 if (!newPass.equals(reconfirmPass)) {

 JOptionPane.showMessageDialog(this, "New password and Reconfirm password do not match.");

 return false;

 }



 try (Connection conn = Database.connect()) {

 PreparedStatement check = conn.prepareStatement("SELECT password FROM employees WHERE empNo = ?");

 check.setString(1, empNo);

 ResultSet rs = check.executeQuery();



 if (!rs.next() || !rs.getString("password").equals(oldPass)) {

 JOptionPane.showMessageDialog(this, "Current password is incorrect.");

 return false;

 }



 PreparedStatement update = conn.prepareStatement("UPDATE employees SET password = ? WHERE empNo = ?");

 update.setString(1, newPass);

 update.setString(2, empNo);



 return update.executeUpdate() > 0;



 } catch (SQLException e) {

 e.printStackTrace();

 JOptionPane.showMessageDialog(this, "Password update failed.");

 }

 return false;

 }

}