package Fproj;

import javax.swing.*;
import java.awt.*;

public class EmpPayrollPanel extends JPanel {

    public EmpPayrollPanel() {
        setBackground(Color.WHITE);
        setLayout(null);

        JLabel lbl = new JLabel("Payroll Panel");
        lbl.setFont(new Font("Arial", Font.BOLD, 22));
        lbl.setBounds(50, 40, 500, 40);
        add(lbl);
    }
}
