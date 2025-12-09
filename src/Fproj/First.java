package Fproj;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;

public class First extends JPanel {

    private Image bgImage;
    private Image logoImage;

    // 1. Declare these at the class level so we can update them later
    private JLabel title1;
    private JLabel lblTurnsTimeInto;
    private JLabel lblOpportunity;

    public First() {

        // Load images
        bgImage = new ImageIcon(getClass().getResource("bg.png")).getImage();
        logoImage = new ImageIcon(getClass().getResource("logo.png")).getImage();

        setLayout(new BorderLayout());

        // Background painting panel
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                }
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(40, 131, 101, 120));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        add(backgroundPanel, BorderLayout.CENTER);

        // --- Top Panel ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        logoPanel.setOpaque(false);
        JLabel logoLabel = new JLabel(new ImageIcon(logoImage));
        logoLabel.setPreferredSize(new Dimension(300, 120));
        logoPanel.add(logoLabel);
        topPanel.add(logoPanel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        buttonPanel.setOpaque(false);

        JButton btnContact = new JButton("Contact us");
        styleWhiteButton(btnContact);
        btnContact.addActionListener(e -> Main.cardLayout.show(Main.cardPanel, "contacts"));
        buttonPanel.add(btnContact);

        JButton btnEmployee = new JButton("Login");
        styleRedButton(btnEmployee);
        btnEmployee.addActionListener(e -> Main.cardLayout.show(Main.cardPanel, "Login"));
        buttonPanel.add(btnEmployee);

        topPanel.add(buttonPanel, BorderLayout.EAST);
        backgroundPanel.add(topPanel, BorderLayout.NORTH);

        // --- Center Panel ---
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Initialize the class-level labels
        title1 = new JLabel("Punctuality");
        styleBigText(title1); 
        title1.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(title1);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        lblTurnsTimeInto = new JLabel("Turns Time Into");
        styleBigText(lblTurnsTimeInto);
        lblTurnsTimeInto.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblTurnsTimeInto);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        lblOpportunity = new JLabel("Opportunity");
        styleBigText(lblOpportunity);
        lblOpportunity.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblOpportunity);

        backgroundPanel.add(centerPanel, BorderLayout.CENTER);

        // 2. Add Listener to detect window resizing
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeFonts();
            }
        });
    }

    // 3. Logic to calculate dynamic font size
    private void resizeFonts() {
        int h = getHeight();
        if (h == 0) return; // Window not visible yet

        // Calculate ratio. Assuming 800px height is the "standard" size for 80pt font.
        // If window is 1000px, font grows. If 600px, font shrinks.
        double ratio = (double) h / 800.0;
        
        // Prevent font from getting too small (0.5x) or too huge (1.5x)
        ratio = Math.max(0.5, Math.min(ratio, 1.5));

        int newSize = (int) (120 * ratio);
        Font dynamicFont = new Font("Copperplate Gothic Bold", Font.BOLD, newSize);

        title1.setFont(dynamicFont);
        lblTurnsTimeInto.setFont(dynamicFont);
        lblOpportunity.setFont(dynamicFont);
    }

    private void styleBigText(JLabel label) {
        // Initial setup
        label.setFont(new Font("Copperplate Gothic Bold", Font.BOLD, 80));
        label.setForeground(Color.WHITE);
        label.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void styleRedButton(JButton b) {
        b.setBackground(new Color(255, 0, 0));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Arial", Font.BOLD, 14));
    }

    private void styleWhiteButton(JButton b) {
        b.setBackground(Color.WHITE);
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        b.setFont(new Font("Arial", Font.BOLD, 14));
    }
}