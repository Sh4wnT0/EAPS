package Fproj;

import javax.swing.*;
import java.awt.*;

public class EmpOTPanel extends JPanel {

    public EmpOTPanel() {
        setBackground(Color.WHITE);
        setLayout(null);

        JLabel lbl = new JLabel("OT / Holiday Request Panel");
        lbl.setFont(new Font("Arial", Font.BOLD, 22));
        lbl.setBounds(50, 40, 600, 40);
        add(lbl);
    }
}
