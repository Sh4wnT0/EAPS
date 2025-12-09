package Fproj;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class contacts extends JPanel {

    public contacts() {

        setLayout(null);
        setPreferredSize(new Dimension(1000, 600));

        // ===========================
        // LEFT DARK GREEN BAR
        // ===========================
        JPanel leftBar = new JPanel();
        leftBar.setBackground(new Color(10, 60, 50));
        leftBar.setBounds(0, 0, 175, 600);
        leftBar.setLayout(null);
        add(leftBar);

        JLabel lblContacts = new JLabel("CONTACTS");
        lblContacts.setFont(new Font("Serif", Font.BOLD, 26));
        lblContacts.setForeground(Color.WHITE);

        JLabel rotated = rotateLabel(lblContacts);
        rotated.setBounds(5, 100, 70, 400);
        leftBar.add(rotated);
        
                // ===========================
                // BACK BUTTON (Put First)
                // ===========================
                JButton btnBack = new JButton("Back");
                btnBack.setBounds(64, 10, 90, 28);
                leftBar.add(btnBack);
                btnBack.setBackground(new Color(255, 0, 0));
                btnBack.setForeground(Color.WHITE);
                btnBack.addActionListener(e -> Main.cardLayout.show(Main.cardPanel, "main"));

        // ===========================
        // TEXT AREA (LEFT SIDE)
        // ===========================
        JPanel textPanel = new JPanel();
        textPanel.setLayout(null);
        textPanel.setBackground(Color.WHITE);
        textPanel.setBounds(173, 0, 327, 600);
        add(textPanel);

        JLabel lblLocation = new JLabel("Lipa City, ");
        lblLocation.setFont(new Font("Serif", Font.PLAIN, 50));
        lblLocation.setBounds(0, 160, 350, 71);
        textPanel.add(lblLocation);

        JLabel lblGetInTouch = new JLabel("<html><h3>Get in Touch:</h3>"
                + "01258568<br>"
                + "Fujiko@gmail.com</html>");
        lblGetInTouch.setFont(new Font("Arial", Font.PLAIN, 16));
        lblGetInTouch.setBounds(10, 300, 458, 120);
        textPanel.add(lblGetInTouch);
        
        JLabel lblLocation_1 = new JLabel("Located in");
        lblLocation_1.setFont(new Font("Serif", Font.PLAIN, 50));
        lblLocation_1.setBounds(0, 96, 350, 100);
        textPanel.add(lblLocation_1);
        
        JLabel lblBatangas = new JLabel("Batangas");
        lblBatangas.setFont(new Font("Serif", Font.PLAIN, 50));
        lblBatangas.setBounds(0, 219, 350, 71);
        textPanel.add(lblBatangas);
        
        JLabel lblBatangas_1 = new JLabel("_____________________________");
        lblBatangas_1.setFont(new Font("Serif", Font.PLAIN, 50));
        lblBatangas_1.setBounds(0, 258, 386, 71);
        textPanel.add(lblBatangas_1);
        
                // ===========================
                // RIGHT IMAGE (FIXED)
                // ===========================
                JLabel imageLabel = new JLabel();
                imageLabel.setBounds(322, -80, 751, 600);
                add(imageLabel);
                imageLabel.setIcon(new ImageIcon(contacts.class.getResource("/Fproj/contacts.png")));

        // SAFELY LOAD IMAGE
        ImageIcon icon = null;

        try {
            icon = new ImageIcon(getClass().getResource("/resources/contacts"));
        } catch (Exception e) {
            System.out.println("ERROR: Image not found in resources folder.");
        }

        if (icon != null && icon.getImage() != null) {
            Image scaled = icon.getImage().getScaledInstance(480, 600, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaled));
        } else {
            imageLabel.setText("Image Not Found");
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setForeground(Color.RED);
        }
    }

    // Rotate label 90 degrees
    private JLabel rotateLabel(JLabel label) {
        return new JLabel(label.getText()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.rotate(-Math.PI / 2, getWidth() / 2, getHeight() / 2);
                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                return new Dimension(size.height, size.width);
            }
        };
    }
}
