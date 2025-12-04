package Fproj;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class AdminRecords extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    public AdminRecords() {
        setLayout(null);
        setBackground(new Color(240,240,240));

        JLabel lblTitle = new JLabel("Employee Records");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setBounds(20, 45, 174, 30);
        add(lblTitle);

        // Search field
        JLabel lblSearch = new JLabel("Search by Emp No:");
        lblSearch.setBounds(427, 50, 150, 25);
        add(lblSearch);

        txtSearch = new JTextField();
        txtSearch.setBounds(544, 50, 150, 25);
        add(txtSearch);

        JButton btnSearch = new JButton("Search");
        btnSearch.setBounds(700, 50, 100, 25);
        add(btnSearch);

        // --- UPDATED COLUMNS ---
        String[] cols = {
                "Employee No", "Name", "Address", "Email",
                "Contact", "Position", "Status", "Daily Pay"
        };

        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(20, 90, 780, 330);
        add(scroll);

        // Load all records from DB
        loadTable();

        btnSearch.addActionListener(e -> searchEmployee());
    }

    // Method to switch panels in the parent window
    private void switchToPanel(JPanel newPanel) {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame != null) {
            parentFrame.setContentPane(newPanel);
            parentFrame.revalidate();
            parentFrame.repaint();
        }
    }

    // -------------------- LOAD ALL EMPLOYEES --------------------
    private void loadTable() {
        model.setRowCount(0);

        String sql = "SELECT empNo, name, address, email, contact, position, employmentStatus, dailyPay FROM employees";

        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("empNo"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("email"),
                        rs.getString("contact"),
                        rs.getString("position"),
                        rs.getString("employmentStatus"),
                        rs.getInt("dailyPay")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ----------------------- SEARCH EMPLOYEE -----------------------
    private void searchEmployee() {
        String search = txtSearch.getText().trim();
        if (search.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Employee Number!");
            return;
        }

        model.setRowCount(0);

        String sql = "SELECT empNo, name, address, email, contact, position, employmentStatus, dailyPay FROM employees WHERE empNo = ?";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, search);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("empNo"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("email"),
                        rs.getString("contact"),
                        rs.getString("position"),
                        rs.getString("employmentStatus"),
                        rs.getInt("dailyPay")
                });
            } else {
                JOptionPane.showMessageDialog(this, "Employee not found.");
                loadTable();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}