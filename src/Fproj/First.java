package Fproj;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class First extends JPanel {

    private Image bgImage;
    private Image logoImage;

    public First() {


        // Load images
        bgImage = new ImageIcon(getClass().getResource("bg.png")).getImage();
        logoImage = new ImageIcon(getClass().getResource("logo.png")).getImage();

        setLayout(null); // manual placement for full control
        setPreferredSize(new Dimension(1000, 500));

        // ---------- LOGO (Top-left) ----------
        JLabel logoLabel = new JLabel(new ImageIcon(logoImage));
        logoLabel.setBounds(20, 20, 300, 110);  // resize here if needed
        add(logoLabel);

        // ---------- "Contact us" BUTTON ----------
        JButton btnContact = new JButton("Contact us");
        btnContact.setBounds(520, 25, 130, 35);
        styleWhiteButton(btnContact);
        add(btnContact);

        // ---------- MAIN TITLE TEXT (centered) ----------
        JLabel title1 = new JLabel("Punctuality");
        title1.setVerticalAlignment(SwingConstants.BOTTOM);
        title1.setToolTipText("");

        styleBigText(title1);

        // Positioning (center on JFrame)
        title1.setBounds(124, 130, 714, 78);

        add(title1);

        // ---------- ADMIN button ----------
        JButton btnAdmin = new JButton("Admin Login");
        btnAdmin.setBounds(660, 22, 150, 40);
        styleBlueButton(btnAdmin);
        btnAdmin.addActionListener(e -> Main.cardLayout.show(Main.cardPanel, "adminLogin"));
        add(btnAdmin);

        // ---------- EMPLOYEE button ----------
        JButton btnEmployee = new JButton("Employee Login");
        btnEmployee.setForeground(new Color(255, 255, 255));
        btnEmployee.setBackground(new Color(255, 0, 0));
        btnEmployee.setBounds(820, 23, 150, 40);
        styleRedButton(btnEmployee);
        btnEmployee.addActionListener(e -> Main.cardLayout.show(Main.cardPanel, "employeeLogin"));
        add(btnEmployee);
        
        JLabel lblTurnsTimeInto = new JLabel("Turns Time Into");
        lblTurnsTimeInto.setVerticalAlignment(SwingConstants.BOTTOM);
        lblTurnsTimeInto.setToolTipText("");
        lblTurnsTimeInto.setHorizontalAlignment(SwingConstants.CENTER);
        lblTurnsTimeInto.setForeground(Color.WHITE);
        lblTurnsTimeInto.setFont(new Font("Copperplate Gothic Bold", Font.BOLD, 80));
        lblTurnsTimeInto.setBounds(124, 215, 745, 78);
        add(lblTurnsTimeInto);
        
        JLabel lblOppurtunity = new JLabel("Oppurtunity");
        lblOppurtunity.setVerticalAlignment(SwingConstants.BOTTOM);
        lblOppurtunity.setToolTipText("");
        lblOppurtunity.setHorizontalAlignment(SwingConstants.CENTER);
        lblOppurtunity.setForeground(Color.WHITE);
        lblOppurtunity.setFont(new Font("Copperplate Gothic Bold", Font.BOLD, 80));
        lblOppurtunity.setBounds(124, 303, 714, 78);
        add(lblOppurtunity);
    }

    // ---------- PAINT BACKGROUND ----------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // draw background image
        g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);

        // create a translucent overlay
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(new Color(40, 131, 101, 120));  // green shade with opacity
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }


    // ---------- STYLE HELPERS ----------
    private void styleBigText(JLabel label) {
        label.setFont(new Font("Copperplate Gothic Bold", Font.BOLD, 80));
        label.setForeground(Color.WHITE);
        label.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void styleBlueButton(JButton b) {
        b.setBackground(new Color(0, 122, 255));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Arial", Font.BOLD, 14));
    }

    private void styleRedButton(JButton b) {
    }

    private void styleWhiteButton(JButton b) {
        b.setBackground(Color.WHITE);
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        b.setFont(new Font("Arial", Font.BOLD, 14));
    }
}
