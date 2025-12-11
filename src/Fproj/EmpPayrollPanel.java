package Fproj;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

public class EmpPayrollPanel extends JPanel {

    private String empNo;
    private JTable table;
    private DefaultTableModel model;
    
    // UI Constants
    private final Color BRAND_COLOR = new Color(22, 102, 87);
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private final Font CELL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    public EmpPayrollPanel(String empNo) {
        this.empNo = empNo;
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 245, 245)); // Light Gray Bg
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- 1. Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("My Payslip History");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(50, 50, 50));
        
        // Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);

        // NEW: Request Payslip Button
        JButton btnRequest = new JButton("Request Payslip");
        styleButton(btnRequest, new Color(70, 130, 180)); // Steel Blue
        btnRequest.addActionListener(e -> requestPayslipAction());

        JButton btnRefresh = new JButton("Refresh");
        styleButton(btnRefresh, BRAND_COLOR);
        btnRefresh.addActionListener(e -> loadPayslips());

        btnPanel.add(btnRequest);
        btnPanel.add(btnRefresh);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. Table ---
        String[] cols = {"Period", "Date Received", "Net Pay", "Action", "Gross", "Ded", "Pres", "Abs", "Late", "Under"};
        
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only button is editable
            }
        };

        table = new JTable(model);
        styleTable(table);

        // Configure "View" Button
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
        table.getColumn("Action").setMaxWidth(100);
        table.getColumn("Action").setMinWidth(100);
        
        // Hide Data Columns
        for(int i=4; i<=9; i++) {
            table.getColumnModel().getColumn(i).setMinWidth(0);
            table.getColumnModel().getColumn(i).setMaxWidth(0);
            table.getColumnModel().getColumn(i).setWidth(0);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(scroll, BorderLayout.CENTER);

        loadPayslips();
    }

    // --- NEW ACTION: Send Request ---
    private void requestPayslipAction() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Send a request to Admin for your latest payslip?", 
            "Confirm Request", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            Database.insertPayslipRequest(empNo);
            JOptionPane.showMessageDialog(this, "Request sent to Admin successfully!");
        }
    }

    private void styleTable(JTable t) {
        t.setRowHeight(40);
        t.setFont(CELL_FONT);
        t.setShowGrid(true);
        t.setGridColor(Color.LIGHT_GRAY); // Changed to Light Gray for cleaner look
        t.setIntercellSpacing(new Dimension(1, 1));
        t.setSelectionBackground(new Color(230, 240, 255));
        t.setSelectionForeground(Color.BLACK);
        
        JTableHeader header = t.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(BRAND_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        t.getColumnModel().getColumn(0).setCellRenderer(center);
        t.getColumnModel().getColumn(1).setCellRenderer(center);
        t.getColumnModel().getColumn(2).setCellRenderer(center);
    }

    private void loadPayslips() {
        model.setRowCount(0);
        String sql = "SELECT * FROM payslips WHERE empNo = ? ORDER BY id DESC";

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, empNo);
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()) {
                String period = rs.getString("period_month") + " " + rs.getString("period_year");
                
                model.addRow(new Object[]{
                    period,
                    rs.getString("date_generated"),
                    df.format(rs.getDouble("net_pay")),
                    "View Slip",
                    rs.getDouble("gross_pay"),
                    rs.getDouble("deductions"),
                    rs.getInt("total_present"),
                    rs.getInt("total_absent"),
                    rs.getInt("total_late"),
                    rs.getInt("total_undertime")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- DIALOG ---
    private class PayslipDialog extends JDialog {
        public PayslipDialog(String period, String date, String net, double gross, double ded, int pres, int abs, int late, int under) {
            setTitle("Payslip Details - " + period);
            setSize(450, 550);
            setLocationRelativeTo(null);
            setModal(true);
            setLayout(new BorderLayout());
            getContentPane().setBackground(Color.WHITE);

            // A. Header
            JPanel pnlHeader = new JPanel(new GridLayout(2, 1));
            pnlHeader.setBackground(BRAND_COLOR);
            pnlHeader.setBorder(new EmptyBorder(15, 0, 15, 0));
            
            JLabel l1 = new JLabel("PAYSLIP", SwingConstants.CENTER);
            l1.setFont(new Font("Segoe UI", Font.BOLD, 20));
            l1.setForeground(Color.WHITE);
            
            JLabel l2 = new JLabel(period, SwingConstants.CENTER);
            l2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            l2.setForeground(new Color(220, 220, 220));
            
            pnlHeader.add(l1);
            pnlHeader.add(l2);
            add(pnlHeader, BorderLayout.NORTH);

            // B. Content
            JPanel pnlContent = new JPanel(new GridBagLayout());
            pnlContent.setBackground(Color.WHITE);
            pnlContent.setBorder(new EmptyBorder(20, 40, 20, 40));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 0, 5, 0);
            gbc.weightx = 1.0;
            int y = 0;
            
            addSectionHeader(pnlContent, gbc, y++, "ATTENDANCE SUMMARY");
            addRow(pnlContent, gbc, y++, "Days Present:", String.valueOf(pres));
            addRow(pnlContent, gbc, y++, "Days Absent:", String.valueOf(abs));
            addRow(pnlContent, gbc, y++, "Late (mins):", String.valueOf(late));
            addRow(pnlContent, gbc, y++, "Undertime (mins):", String.valueOf(under));
            
            gbc.gridy = y++; pnlContent.add(Box.createVerticalStrut(15), gbc);
            
            addSectionHeader(pnlContent, gbc, y++, "EARNINGS & DEDUCTIONS");
            addRow(pnlContent, gbc, y++, "Gross Pay:", df.format(gross));
            addRow(pnlContent, gbc, y++, "Total Deductions:", "- " + df.format(ded));
            
            gbc.gridy = y++; 
            JSeparator sep = new JSeparator();
            sep.setForeground(Color.GRAY);
            pnlContent.add(sep, gbc);
            
            gbc.gridy = y++; gbc.insets = new Insets(15, 0, 0, 0);
            JPanel netPanel = new JPanel(new BorderLayout());
            netPanel.setBackground(Color.WHITE);
            
            JLabel lblNetTitle = new JLabel("NET PAY");
            lblNetTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
            
            JLabel lblNetVal = new JLabel("PHP " + net);
            lblNetVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
            lblNetVal.setForeground(BRAND_COLOR);
            
            netPanel.add(lblNetTitle, BorderLayout.WEST);
            netPanel.add(lblNetVal, BorderLayout.EAST);
            pnlContent.add(netPanel, gbc);

            add(pnlContent, BorderLayout.CENTER);

            // C. Footer
            JPanel btnPanel = new JPanel();
            btnPanel.setBackground(Color.WHITE);
            btnPanel.setBorder(new EmptyBorder(10, 10, 20, 10));
            JButton btnPrint = new JButton("Print Copy");
            styleButton(btnPrint, BRAND_COLOR);
            btnPrint.setPreferredSize(new Dimension(100, 45));
            btnPrint.addActionListener(e -> JOptionPane.showMessageDialog(this, "Printing..."));
            btnPanel.add(btnPrint);
            add(btnPanel, BorderLayout.SOUTH);
        }

        private void addSectionHeader(JPanel p, GridBagConstraints gbc, int y, String text) {
            gbc.gridy = y;
            JLabel l = new JLabel(text);
            l.setFont(new Font("Segoe UI", Font.BOLD, 12));
            l.setForeground(Color.GRAY);
            l.setBorder(new EmptyBorder(0, 0, 5, 0));
            p.add(l, gbc);
        }

        private void addRow(JPanel p, GridBagConstraints gbc, int y, String label, String val) {
            gbc.gridy = y;
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(Color.WHITE);
            row.add(new JLabel(label), BorderLayout.WEST);
            row.add(new JLabel(val), BorderLayout.EAST);
            p.add(row, gbc);
        }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); setFont(new Font("Segoe UI", Font.PLAIN, 11)); setBackground(Color.WHITE); setBorder(BorderFactory.createLineBorder(BRAND_COLOR)); setForeground(BRAND_COLOR); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) { setText("View"); return this; }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton b = new JButton(); private boolean clicked;
        public ButtonEditor(JCheckBox c) { super(c); b.setOpaque(true); b.addActionListener(e -> { if(clicked) openDialog(); clicked=false; fireEditingStopped(); }); }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { clicked=true; b.setText("View"); return b; }
        @Override public Object getCellEditorValue() { return "View"; }
        
        private void openDialog() {
            int r = table.getSelectedRow();
            String period = (String) table.getValueAt(r, 0);
            String date = (String) table.getValueAt(r, 1);
            String net = (String) table.getValueAt(r, 2);
            double gross = (double) table.getModel().getValueAt(r, 4);
            double ded = (double) table.getModel().getValueAt(r, 5);
            int pres = (int) table.getModel().getValueAt(r, 6);
            int abs = (int) table.getModel().getValueAt(r, 7);
            int late = (int) table.getModel().getValueAt(r, 8);
            int under = (int) table.getModel().getValueAt(r, 9);
            new PayslipDialog(period, date, net, gross, ded, pres, abs, late, under).setVisible(true);
        }
    }
}