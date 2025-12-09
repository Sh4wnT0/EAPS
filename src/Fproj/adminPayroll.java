package Fproj;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class adminPayroll extends JPanel {

    public adminPayroll() {
        setLayout(null);
        setBackground(new Color(240,240,240));

        JLabel lblTitle = new JLabel("Payroll Records (Placeholder)");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setBounds(20, 45, 300, 30);
        add(lblTitle);

        // Add payroll table/logic here later
        JButton btnBack = new JButton("Back to Employee Records");
        btnBack.setBounds(20, 7, 200, 36);
        add(btnBack);
        btnBack.addActionListener(e -> switchToPanel(new AdminRecords()));
    }

    private void switchToPanel(JPanel newPanel) {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame != null) {
            parentFrame.setContentPane(newPanel);
            parentFrame.revalidate();
            parentFrame.repaint();
        }
    }
}