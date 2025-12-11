package Fproj;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class contacts extends JPanel {

    private Image companyImage; 
    private JPanel textPanel; 
    
    // UI Fields (Promoted to class level so we can update them)
    private JLabel lblLocStatic, lblAddrLine1, lblAddrLine2, lblContactInfo;

    public contacts() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // ===========================
        // LEFT SIDEBAR (Fixed Width)
        // ===========================
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(new Color(10, 60, 50));
        sidebar.setPreferredSize(new Dimension(100, 0));

        // Back Button
        JPanel topBtnContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topBtnContainer.setOpaque(false);
        JButton btnBack = new JButton("Back");
        btnBack.setBackground(new Color(255, 50, 50));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        btnBack.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnBack.addActionListener(e -> Main.cardLayout.show(Main.cardPanel, "main"));
        topBtnContainer.add(btnBack);
        sidebar.add(topBtnContainer, BorderLayout.NORTH);

        // Vertical Text
        JComponent verticalText = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Font font = new Font("Serif", Font.BOLD, 32);
                g2.setFont(font);
                g2.setColor(Color.WHITE);
                
                String text = "CONTACTS";
                FontMetrics fm = g2.getFontMetrics();
                
                g2.translate(getWidth() / 2, getHeight() / 2);
                g2.rotate(-Math.PI / 2);
                g2.drawString(text, -fm.stringWidth(text) / 2, 0); 
                g2.dispose();
            }
        };
        sidebar.add(verticalText, BorderLayout.CENTER);
        add(sidebar, BorderLayout.WEST);

        // ===========================
        // CENTER CONTENT
        // ===========================
        JPanel contentPanel = new JPanel(new GridLayout(1, 2)); 
        contentPanel.setBackground(Color.WHITE);

        // --- 1. Text Info Panel ---
        textPanel = new JPanel(new GridBagLayout());
        textPanel.setBackground(Color.WHITE);
        textPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; 
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0); 

        // Initialize Labels (Empty for now, filled by refreshData)
        lblLocStatic = addResponsiveLabel("Located in", Font.PLAIN, 40, gbc);
        lblAddrLine1 = addResponsiveLabel("", Font.BOLD, 48, gbc);
        lblAddrLine2 = addResponsiveLabel("", Font.PLAIN, 40, gbc);

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(Color.BLACK);
        gbc.insets = new Insets(20, 0, 20, 0);
        textPanel.add(sep, gbc);

        // Contact Info
        gbc.insets = new Insets(5, 0, 5, 0);
        addResponsiveLabel("Get in Touch:", Font.BOLD, 18, gbc);
        lblContactInfo = addResponsiveLabel("", Font.PLAIN, 16, gbc);

        // --- 2. Image Panel ---
        JPanel imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (companyImage != null) {
                    g.drawImage(companyImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(240, 240, 240));
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(Color.RED);
                    g.drawString("Image Not Found", getWidth()/2 - 40, getHeight()/2);
                }
            }
        };

        contentPanel.add(textPanel);
        contentPanel.add(imagePanel);
        add(contentPanel, BorderLayout.CENTER);

        // ===========================
        // LISTENERS
        // ===========================
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeFonts();
            }
            
            // THIS UPDATES DATA EVERY TIME PANEL IS SHOWN
            @Override
            public void componentShown(ComponentEvent e) {
                refreshContactDetails();
            }
        });
        
        // Initial load
        refreshContactDetails();
    }

    // --- LOGIC TO REFRESH DATA ---
    private void refreshContactDetails() {
        String[] info = Database.getCompanyInfo();
        String compAddress = info[1];
        String compContact = info[2];
        String logoPath = info[3];

        // Address Logic
        String line2 = "Address Not Set";
        String line3 = "";

        if (compAddress != null && !compAddress.isEmpty()) {
            String[] parts = compAddress.split(",");
            if (parts.length >= 1) line2 = parts[0].trim() + ",";
            if (parts.length >= 2) line3 = parts[1].trim();
            if (parts.length == 1) { line2 = compAddress; line3 = ""; }
        }

        // Update Labels
        lblAddrLine1.setText(line2);
        lblAddrLine2.setText(line3);
        lblContactInfo.setText("<html>" + compContact + "</html>");

        // Reload Image
        loadCompanyImage(logoPath);
        
        // Refresh UI
        revalidate();
        repaint();
    }

    private JLabel addResponsiveLabel(String text, int style, int baseSize, GridBagConstraints gbc) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Serif", style, baseSize));
        l.putClientProperty("baseSize", (float) baseSize); 
        textPanel.add(l, gbc);
        return l;
    }

    private void resizeFonts() {
        int height = getHeight();
        if (height == 0) return;
        float ratio = (float) height / 600f;
        ratio = Math.max(0.8f, Math.min(ratio, 2.5f));

        for (Component comp : textPanel.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel lbl = (JLabel) comp;
                Object property = lbl.getClientProperty("baseSize");
                if (property instanceof Float) {
                    float baseSize = (Float) property;
                    int newSize = Math.round(baseSize * ratio);
                    if (lbl.getFont().getSize() != newSize) {
                        lbl.setFont(lbl.getFont().deriveFont((float) newSize));
                    }
                }
            }
        }
    }

    private void loadCompanyImage(String logoPath) {
        try {
            ImageIcon icon = null;
            if (logoPath != null && logoPath.equals("/Fproj/contacts.png")) {
                java.net.URL imgURL = getClass().getResource(logoPath);
                if (imgURL != null) icon = new ImageIcon(imgURL);
            } 
            else if (logoPath != null && new File(logoPath).exists()) {
                icon = new ImageIcon(logoPath);
            }
            else {
                java.net.URL imgURL = getClass().getResource("/Fproj/contacts.png");
                if (imgURL != null) icon = new ImageIcon(imgURL);
            }

            if (icon != null) {
                this.companyImage = icon.getImage();
            }
        } catch (Exception e) {
            // Silently fail or log
        }
    }
}