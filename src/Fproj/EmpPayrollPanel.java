package Fproj;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class EmpPayrollPanel extends JPanel {
	private String empNo, empName, empPosition;
    public EmpPayrollPanel(String empNo2) {
        setBackground(Color.WHITE);
        setLayout(null);

        JLabel lbl = new JLabel("Payroll Panel");
        lbl.setFont(new Font("Arial", Font.BOLD, 22));
        lbl.setBounds(50, 40, 500, 40);
        add(lbl);
    }
}
