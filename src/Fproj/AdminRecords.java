package Fproj;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Properties;

public class AdminRecords extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    // --- Colors & Fonts ---
    private final Color BRAND_COLOR = new Color(22, 102, 87);
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    public AdminRecords() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- 1. Top Panel ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(BACKGROUND_COLOR);

        JLabel lblTitle = new JLabel("Employee Records");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(BRAND_COLOR);
        topPanel.add(lblTitle, BorderLayout.WEST);

        // Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(BACKGROUND_COLOR);

        // NEW: Review Applications Button
        JButton btnReview = new JButton("Review Applications");
        styleButton(btnReview, new Color(70, 130, 180)); // Blue
        btnReview.addActionListener(e -> new ReviewRequestsDialog().setVisible(true));

        JButton btnRegister = new JButton("+ Register New");
        styleButton(btnRegister, BRAND_COLOR);
        btnRegister.addActionListener(e -> {
            new RegistrationDialog(null, null).setVisible(true); // Null = No pre-fill
        });

        txtSearch = new JTextField(15);
        txtSearch.putClientProperty("JTextField.placeholderText", "Enter ID...");

        JButton btnSearch = new JButton("Search");
        styleButton(btnSearch, BRAND_COLOR); 
        btnSearch.addActionListener(e -> searchEmployee());

        actionPanel.add(btnReview); // Added here
        actionPanel.add(Box.createHorizontalStrut(10));
        actionPanel.add(btnRegister);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(new JLabel("Search ID:"));
        actionPanel.add(txtSearch);
        actionPanel.add(btnSearch);

        topPanel.add(actionPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- 2. Table Setup ---
        String[] cols = {"Emp No", "Name", "Address", "Email", "Contact", "Position", "Status", "Daily Pay", "Edit", "Delete"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 8 || column == 9; }
        };

        table = new JTable(model);
        styleTable(); 

        table.getColumn("Edit").setCellRenderer(new ButtonRenderer(false));
        table.getColumn("Edit").setCellEditor(new EditButtonEditor(new JCheckBox()));
        table.getColumn("Edit").setMaxWidth(70);

        table.getColumn("Delete").setCellRenderer(new ButtonRenderer(true));
        table.getColumn("Delete").setCellEditor(new DeleteButtonEditor(new JCheckBox()));
        table.getColumn("Delete").setMaxWidth(80);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(scroll, BorderLayout.CENTER);

        loadTable();
    }
   
    private void styleTable() {
        table.setRowHeight(35);
        table.setFont(MAIN_FONT);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(22, 102, 87, 50));
        table.setSelectionForeground(Color.BLACK);
        JTableHeader header = table.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(BRAND_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 8 && i != 9) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void loadTable() {
        model.setRowCount(0);
        String sql = "SELECT empNo, name, address, email, contact, position, employmentStatus, dailyPay FROM employees ORDER BY empNo";
        try (Connection conn = Database.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("empNo"), rs.getString("name"), rs.getString("address"), rs.getString("email"),
                    rs.getString("contact"), rs.getString("position"), rs.getString("employmentStatus"), rs.getInt("dailyPay"), "Edit", "Delete"
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void searchEmployee() {
        String search = txtSearch.getText().trim();
        if (search.isEmpty()) { loadTable(); return; }
        model.setRowCount(0);
        String sql = "SELECT empNo, name, address, email, contact, position, employmentStatus, dailyPay FROM employees WHERE empNo LIKE ?";
        try (Connection conn = Database.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + search + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("empNo"), rs.getString("name"), rs.getString("address"), rs.getString("email"),
                    rs.getString("contact"), rs.getString("position"), rs.getString("employmentStatus"), rs.getInt("dailyPay"), "Edit", "Delete"
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // =========================================================================
    //                        NEW: REVIEW REQUESTS DIALOG
    // =========================================================================
    private class ReviewRequestsDialog extends JDialog {
        private JTable reqTable;
        private DefaultTableModel reqModel;

        public ReviewRequestsDialog() {
            super((Frame) SwingUtilities.getWindowAncestor(AdminRecords.this), "Review Account Applications", true);
            setSize(700, 450);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());

            // Table
            String[] cols = {"ID", "Name", "Email", "Date", "Resume Path"};
            reqModel = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            reqTable = new JTable(reqModel);
            reqTable.setRowHeight(30);
            reqTable.getTableHeader().setFont(HEADER_FONT);
            reqTable.getTableHeader().setBackground(BRAND_COLOR);
            reqTable.getTableHeader().setForeground(Color.WHITE);
            // Hide Path Column
            reqTable.getColumnModel().getColumn(4).setMinWidth(0);
            reqTable.getColumnModel().getColumn(4).setMaxWidth(0);
            reqTable.getColumnModel().getColumn(4).setWidth(0);
            reqTable.getColumnModel().getColumn(0).setMaxWidth(50); // ID width

            add(new JScrollPane(reqTable), BorderLayout.CENTER);

            // Buttons
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            
            JButton btnView = new JButton("View Resume");
            styleButton(btnView, new Color(23, 162, 184)); // Teal
            btnView.addActionListener(e -> viewResume());

            JButton btnApprove = new JButton("Approve (Register)");
            styleButton(btnApprove, new Color(40, 167, 69)); // Green
            btnApprove.addActionListener(e -> approveRequest());

            JButton btnReject = new JButton("Reject");
            styleButton(btnReject, new Color(220, 53, 69)); // Red
            btnReject.addActionListener(e -> rejectRequest());

            btnPanel.add(btnView);
            btnPanel.add(btnApprove);
            btnPanel.add(btnReject);
            add(btnPanel, BorderLayout.SOUTH);

            loadRequests();
        }

        private void loadRequests() {
            reqModel.setRowCount(0);
            try (ResultSet rs = Database.getAccountRequests()) {
                if(rs != null) {
                    while (rs.next()) {
                        reqModel.addRow(new Object[]{
                            rs.getInt("id"), rs.getString("full_name"), rs.getString("email"), 
                            rs.getString("request_date"), rs.getString("resume_path")
                        });
                    }
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }

        private void viewResume() {
            int row = reqTable.getSelectedRow();
            if (row == -1) return;
            
            String pathStr = (String) reqTable.getValueAt(row, 4);
            try {
                File pdfFile = new File(pathStr);
                if (pdfFile.exists() && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                } else {
                    JOptionPane.showMessageDialog(this, "File not found or format unsupported.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage());
            }
        }

        private void approveRequest() {
            int row = reqTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select an applicant."); return; }

            int id = (int) reqTable.getValueAt(row, 0);
            String name = (String) reqTable.getValueAt(row, 1);
            String email = (String) reqTable.getValueAt(row, 2);

            // Open Registration with Pre-filled Data
            RegistrationDialog reg = new RegistrationDialog(name, email);
            reg.setVisible(true);

            // If registration was successful (you'd ideally verify, but basic flow assumes manual check)
            // Ask to clear request
            int confirm = JOptionPane.showConfirmDialog(this, "Did you complete the registration?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                Database.deleteAccountRequest(id);
                loadRequests();
            }
        }

        private void rejectRequest() {
            int row = reqTable.getSelectedRow();
            if (row == -1) return;
            
            int confirm = JOptionPane.showConfirmDialog(this, "Reject and delete this application?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                int id = (int) reqTable.getValueAt(row, 0);
                Database.deleteAccountRequest(id);
                loadRequests();
            }
        }
    }

    // --- TABLE BUTTON RENDERERS ---
    class ButtonRenderer extends JButton implements TableCellRenderer {
        private boolean isDelete;
        public ButtonRenderer(boolean isDelete) { this.isDelete = isDelete; setOpaque(true); setFont(new Font("Segoe UI", Font.PLAIN, 11)); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText(isDelete ? "Delete" : "Edit");
            setBackground(isDelete ? new Color(220, 53, 69) : new Color(70, 130, 180));
            setForeground(Color.WHITE);
            return this;
        }
    }
    class EditButtonEditor extends DefaultCellEditor {
        JButton b=new JButton(); boolean c;
        public EditButtonEditor(JCheckBox k) { super(k); b.setOpaque(true); b.addActionListener(e->{ if(c){ 
            new EditEmployeeDialog((String)table.getValueAt(table.getSelectedRow(),0)).setVisible(true); loadTable(); 
        } c=false; fireEditingStopped(); }); }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { b.setText("Edit"); b.setBackground(new Color(70, 130, 180)); b.setForeground(Color.WHITE); this.c=true; return b; }
        @Override public Object getCellEditorValue() { return "Edit"; }
    }
    class DeleteButtonEditor extends DefaultCellEditor {
        JButton b=new JButton(); boolean c;
        public DeleteButtonEditor(JCheckBox k) { super(k); b.setOpaque(true); b.addActionListener(e->{ if(c){ 
            String no=(String)table.getValueAt(table.getSelectedRow(),0); 
            if(JOptionPane.showConfirmDialog(null, "Delete employee "+no+"?", "Confirm", JOptionPane.YES_NO_OPTION)==0) {
                Database.deleteEmployee(no); loadTable();
            }
        } c=false; fireEditingStopped(); }); }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { b.setText("Delete"); b.setBackground(new Color(220, 53, 69)); b.setForeground(Color.WHITE); this.c=true; return b; }
        @Override public Object getCellEditorValue() { return "Delete"; }
    }

    // --- DIALOGS ---
    private class EditEmployeeDialog extends JDialog {
        private JComboBox<String> cbPosition, cbStatus;
        private JTextField txtDailyPay;
        private String empNo;
        public EditEmployeeDialog(String empNo) {
            this.empNo = empNo; setTitle("Edit Employee"); setModal(true); setSize(400, 350); setLocationRelativeTo(null);
            JPanel p = new JPanel(new GridBagLayout()); p.setBackground(Color.WHITE); p.setBorder(new EmptyBorder(20, 20, 20, 20));
            GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(10, 10, 10, 10); g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0;
            g.gridx=0; g.gridy=0; g.gridwidth=2; JLabel l=new JLabel("Update: "+empNo); l.setFont(HEADER_FONT); l.setForeground(BRAND_COLOR); p.add(l, g);
            g.gridy++; g.gridwidth=1; p.add(new JLabel("Position:"), g); g.gridx=1; cbPosition=new JComboBox<>(); cbPosition.setEditable(true); cbPosition.setBackground(Color.WHITE); p.add(cbPosition, g); loadPositions();
            g.gridx=0; g.gridy++; p.add(new JLabel("Status:"), g); g.gridx=1; cbStatus=new JComboBox<>(new String[]{"Regular", "Probationary", "Contractual"}); cbStatus.setBackground(Color.WHITE); p.add(cbStatus, g);
            g.gridx=0; g.gridy++; p.add(new JLabel("Daily Pay:"), g); g.gridx=1; txtDailyPay=new JTextField(); p.add(txtDailyPay, g);
            g.gridx=0; g.gridy++; g.gridwidth=2; JButton b=new JButton("Save"); styleButton(b, BRAND_COLOR); b.addActionListener(e->save()); p.add(b, g);
            add(p); loadData();
        }
        private void loadData() { try(Connection c=Database.connect(); PreparedStatement p=c.prepareStatement("SELECT position, employmentStatus, dailyPay FROM employees WHERE empNo=?")){ p.setString(1,empNo); ResultSet r=p.executeQuery(); if(r.next()){ cbPosition.setSelectedItem(r.getString(1)); cbStatus.setSelectedItem(r.getString(2)); txtDailyPay.setText(r.getString(3)); }}catch(Exception e){} }
        private void loadPositions() { try(Connection c=Database.connect(); ResultSet r=c.createStatement().executeQuery("SELECT DISTINCT position FROM employees")){ while(r.next()) cbPosition.addItem(r.getString(1)); }catch(Exception e){} }
        private void save() { try { Database.updateEmployeeDetails(empNo, (String)cbPosition.getSelectedItem(), (String)cbStatus.getSelectedItem(), Integer.parseInt(txtDailyPay.getText())); dispose(); } catch(Exception e){} }
    }

    private class RegistrationDialog extends JDialog {
        private JTextField txtName, txtAddress, txtEmail, txtContact, txtSalaryRate;
        private JComboBox<String> cbRole, cbPosition, cbStatus;
        private JLabel lblEmpNumber, lblPhotoDisplay, lblEmpNoLabel; 
        private JButton btnUploadPhoto, btnAddPos, btnRemovePos, btnSave;
        private String imagePath = "";
        private int empCounter = Database.getLastEmployeeNumber() + 1;
        private final String[] STAFF_POSITIONS = {"HR Officer", "Accountant"};

        // Updated Constructor: Allows pre-filling data from Account Requests
        public RegistrationDialog(String preName, String preEmail) {
            super((Frame) null, "Register New Account", true);
            setSize(850, 650);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
            mainPanel.setBackground(Color.WHITE);
            mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

            // Photo (GridBag)
            JPanel photoPanel = new JPanel(new GridBagLayout()); photoPanel.setBackground(Color.WHITE); photoPanel.setBorder(BorderFactory.createTitledBorder("Profile Photo")); photoPanel.setPreferredSize(new Dimension(200, 0));
            GridBagConstraints gp = new GridBagConstraints(); gp.gridx=0; gp.gridy=0; gp.insets=new Insets(10,10,15,10); gp.anchor=GridBagConstraints.CENTER;
            lblPhotoDisplay=new JLabel("No photo", SwingConstants.CENTER); lblPhotoDisplay.setPreferredSize(new Dimension(150,150)); lblPhotoDisplay.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); photoPanel.add(lblPhotoDisplay, gp);
            gp.gridy=1; gp.insets=new Insets(0,10,10,10); btnUploadPhoto=new JButton("Upload"); styleButton(btnUploadPhoto, BRAND_COLOR); btnUploadPhoto.setPreferredSize(new Dimension(100,30)); btnUploadPhoto.addActionListener(e->uploadPhoto()); photoPanel.add(btnUploadPhoto, gp);
            mainPanel.add(photoPanel, BorderLayout.WEST);

            // Form
            JPanel formPanel = new JPanel(new GridBagLayout()); formPanel.setBackground(Color.WHITE);
            GridBagConstraints g = new GridBagConstraints(); g.insets=new Insets(8,10,8,10); g.fill=GridBagConstraints.HORIZONTAL; g.weightx=1.0; int r=0;
            
            cbRole = new JComboBox<>(new String[]{"Employee", "Staff"}); cbRole.setBackground(Color.WHITE); cbRole.addActionListener(e->toggleRoleView()); addRow(formPanel, g, r++, "Role:", cbRole);
            lblEmpNoLabel = new JLabel("Employee No.:"); lblEmpNoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblEmpNumber = new JLabel(String.format("%03d", empCounter)); lblEmpNumber.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblEmpNumber.setForeground(BRAND_COLOR);
            g.gridx=0; g.gridy=r; g.weightx=0; formPanel.add(lblEmpNoLabel, g); g.gridx=1; g.weightx=1; formPanel.add(lblEmpNumber, g); r++;

            txtName = new JTextField(preName != null ? preName : ""); addRow(formPanel, g, r++, "Full Name:", txtName);
            txtAddress = new JTextField(); addRow(formPanel, g, r++, "Address:", txtAddress);
            txtEmail = new JTextField(preEmail != null ? preEmail : ""); addRow(formPanel, g, r++, "Email:", txtEmail);
            txtContact = new JTextField(); addRow(formPanel, g, r++, "Contact No.:", txtContact);

            g.gridx=0; g.gridy=r; g.weightx=0; formPanel.add(new JLabel("Position:"), g);
            g.gridx=1; g.weightx=1; JPanel pp=new JPanel(new BorderLayout(5,0)); pp.setBackground(Color.WHITE);
            cbPosition = new JComboBox<>(); cbPosition.setEditable(true); cbPosition.setBackground(Color.WHITE); pp.add(cbPosition, BorderLayout.CENTER);
            JPanel mp=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)); mp.setBackground(Color.WHITE);
            btnAddPos=new JButton("+"); btnAddPos.setPreferredSize(new Dimension(40,30)); btnAddPos.addActionListener(e->{String n=(String)cbPosition.getSelectedItem(); if(n!=null && !n.isEmpty()) cbPosition.addItem(n);});
            btnRemovePos=new JButton("-"); btnRemovePos.setPreferredSize(new Dimension(40,30)); btnRemovePos.addActionListener(e->cbPosition.removeItem(cbPosition.getSelectedItem()));
            mp.add(btnAddPos); mp.add(btnRemovePos); pp.add(mp, BorderLayout.EAST);
            formPanel.add(pp, g); r++;

            cbStatus = new JComboBox<>(new String[]{"Regular", "Probationary", "Contractual"}); cbStatus.setBackground(Color.WHITE); cbStatus.addActionListener(e->updateSalary()); addRow(formPanel, g, r++, "Emp. Status:", cbStatus);
            txtSalaryRate = new JTextField(); addRow(formPanel, g, r++, "Salary Rate:", txtSalaryRate);
            mainPanel.add(formPanel, BorderLayout.CENTER);

            JPanel bp = new JPanel(); bp.setBackground(Color.WHITE); btnSave=new JButton("Complete Registration"); styleButton(btnSave, BRAND_COLOR); btnSave.setPreferredSize(new Dimension(200,40)); btnSave.addActionListener(e->register()); bp.add(btnSave);
            mainPanel.add(bp, BorderLayout.SOUTH);

            add(mainPanel); loadPositions(); toggleRoleView(); updateSalary();
        }

        private void toggleRoleView() {
            boolean isStaff = "Staff".equals(cbRole.getSelectedItem());
            lblEmpNoLabel.setVisible(!isStaff); lblEmpNumber.setVisible(!isStaff);
            cbStatus.setEnabled(!isStaff); txtSalaryRate.setEnabled(!isStaff);
            if(isStaff) txtSalaryRate.setText("N/A"); else updateSalary();
            cbPosition.removeAllItems();
            if (isStaff) { for (String pos : STAFF_POSITIONS) cbPosition.addItem(pos); cbPosition.setEditable(false); btnAddPos.setVisible(false); btnRemovePos.setVisible(false); }
            else { loadPositions(); cbPosition.setEditable(true); btnAddPos.setVisible(true); btnRemovePos.setVisible(true); }
        }
        
        private void register() {
            String name=txtName.getText(), email=txtEmail.getText();
            if(name.isEmpty() || email.isEmpty()) { JOptionPane.showMessageDialog(this,"Missing info"); return; }
            try {
                String pass = generateRandomPassword(10); String user = "";
                if("Staff".equalsIgnoreCase((String)cbRole.getSelectedItem())) {
                    String pos = (String)cbPosition.getSelectedItem();
                    String prefix = "HR Officer".equals(pos) ? "hr_" : ("Accountant".equals(pos) ? "pay_" : "staff_");
                    user = prefix + name.replaceAll("\\s+","").toLowerCase();
                    Database.insertASRecord(name, "staff", user, pass, txtAddress.getText(), email, txtContact.getText(), pos);
                } else {
                    user = lblEmpNumber.getText();
                    int sal = 0; try{sal=Integer.parseInt(txtSalaryRate.getText());}catch(Exception e){}
                    Database.insertEmployeeFull(user, name, "employee", txtAddress.getText(), email, txtContact.getText(), (String)cbPosition.getSelectedItem(), (String)cbStatus.getSelectedItem(), sal, pass, imagePath);
                }
                sendEmail(email, name, user, pass);
                JOptionPane.showMessageDialog(this, "Success! User: "+user); dispose(); loadTable();
            } catch(Exception e) { JOptionPane.showMessageDialog(this, "Error: "+e.getMessage()); }
        }

        private void addRow(JPanel p, GridBagConstraints g, int r, String l, JComponent c) { g.gridx=0; g.gridy=r; g.weightx=0; p.add(new JLabel(l), g); g.gridx=1; g.weightx=1; c.setPreferredSize(new Dimension(200, 30)); p.add(c, g); }
        private void loadPositions() { try(Connection c=Database.connect(); ResultSet r=c.createStatement().executeQuery("SELECT DISTINCT position FROM employees")){ while(r.next()) cbPosition.addItem(r.getString(1)); }catch(Exception e){} }
        private void updateSalary() { String s=(String)cbStatus.getSelectedItem(); txtSalaryRate.setText(("Probationary".equalsIgnoreCase(s)||"Contractual".equalsIgnoreCase(s))?"600":"650"); txtSalaryRate.setEditable(!"Probationary".equalsIgnoreCase(s)&&!"Contractual".equalsIgnoreCase(s)); }
        private void uploadPhoto() { JFileChooser f=new JFileChooser(); f.setFileFilter(new FileNameExtensionFilter("Images","jpg","png")); if(f.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){ try { Path dest=Paths.get("employee_photos","emp_"+System.currentTimeMillis()+".jpg"); if(!Files.exists(dest.getParent())) Files.createDirectories(dest.getParent()); Files.copy(f.getSelectedFile().toPath(), dest, StandardCopyOption.REPLACE_EXISTING); imagePath=dest.toString(); lblPhotoDisplay.setIcon(new ImageIcon(new ImageIcon(imagePath).getImage().getScaledInstance(150,150,Image.SCALE_SMOOTH))); lblPhotoDisplay.setText(""); } catch(IOException e){} } }
        private String generateRandomPassword(int l) { String chars="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%"; SecureRandom r=new SecureRandom(); StringBuilder sb=new StringBuilder(); for(int i=0;i<l;i++) sb.append(chars.charAt(r.nextInt(chars.length()))); return sb.toString(); }
        private void sendEmail(String t, String n, String u, String p) { 
            final String from="sh4wntolentino@gmail.com"; final String pwd="dkffdbkmlifnvows";
            Properties props = new Properties(); props.put("mail.smtp.auth", "true"); props.put("mail.smtp.starttls.enable", "true"); props.put("mail.smtp.host", "smtp.gmail.com"); props.put("mail.smtp.port", "587");
            Session session = Session.getInstance(props, new Authenticator() { protected PasswordAuthentication getPasswordAuthentication() { return new PasswordAuthentication(from, pwd); } });
            try { Message msg = new MimeMessage(session); msg.setFrom(new InternetAddress(from)); msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(t)); msg.setSubject("Your Account Credentials"); msg.setText("Hello " + n + ",\n\nUsername: " + u + "\nPassword: " + p + "\n\nPlease change your password upon login."); Transport.send(msg); } catch (MessagingException e) { e.printStackTrace(); }
        }
    }
}