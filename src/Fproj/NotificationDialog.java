package Fproj;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class NotificationDialog extends JDialog {

    private final Color BRAND_COLOR = new Color(22, 102, 87);
    private JPanel contentPanel;
    private String currentUser;
    private boolean isAdmin;
    private Consumer<String> navigationCallback; // Callback for switching tabs

    // Constructor with Navigation Callback
    public NotificationDialog(Frame owner, String empNo, boolean isAdmin, Consumer<String> navCallback) {
        super(owner, isAdmin ? "Pending Tasks" : "Notifications", true);
        this.currentUser = empNo;
        this.isAdmin = isAdmin;
        this.navigationCallback = navCallback;
        
        setSize(500, 650);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BRAND_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel lblTitle = new JLabel(isAdmin ? "Admin Action Center" : "My Inbox");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        
        if(!isAdmin) {
            JButton btnClear = new JButton("Clear All");
            styleButton(btnClear, Color.WHITE, BRAND_COLOR);
            btnClear.addActionListener(e -> { Database.clearNotifications(empNo); refreshContent(); });
            headerPanel.add(btnClear, BorderLayout.EAST);
        }
        add(headerPanel, BorderLayout.NORTH);

        // Content
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(245, 245, 245));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(Color.WHITE);
        JButton btnClose = new JButton("Close");
        styleButton(btnClose, Color.GRAY, Color.WHITE);
        btnClose.addActionListener(e -> dispose());
        footer.add(btnClose);
        add(footer, BorderLayout.SOUTH);

        refreshContent();
    }

    private void refreshContent() {
        contentPanel.removeAll();

        if (isAdmin) {
            loadAdminContent();
        } else {
            loadEmployeeContent();
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // --- ADMIN VIEW LOGIC ---
    private void loadAdminContent() {
        List<String[]> allRequests = Database.getAdminPendingRequests();
        
        List<String[]> security = new ArrayList<>();
        List<String[]> approvals = new ArrayList<>();

        // 1. Separate requests
        for (String[] req : allRequests) {
            if ("Password Reset".equalsIgnoreCase(req[3])) {
                security.add(req);
            } else {
                approvals.add(req);
            }
        }

        // 2. Render Security Section (Priority)
        if (!security.isEmpty()) {
            addSectionHeader("Security Alerts (High Priority)", new Color(220, 53, 69));
            for (String[] req : security) {
                contentPanel.add(createPasswordResetCard(req));
                contentPanel.add(Box.createVerticalStrut(10));
            }
        }

        // 3. Render Approvals Section
        if (!approvals.isEmpty()) {
            addSectionHeader("Pending Approvals", BRAND_COLOR);
            for (String[] req : approvals) {
                contentPanel.add(createReviewCard(req));
                contentPanel.add(Box.createVerticalStrut(10));
            }
        }

        if (security.isEmpty() && approvals.isEmpty()) {
            addEmptyMessage("All caught up! No pending requests.");
        }
    }

    // --- CARD: Password Reset ---
    private JPanel createPasswordResetCard(String[] data) {
        // data: [ID, EmpNo, Name, Type, Date, Table]
        JPanel card = createBaseCard();
        card.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(220, 53, 69))); // Red strip

        JLabel lblTitle = new JLabel("Password Reset: " + data[2]);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JLabel lblSub = new JLabel("ID: " + data[1] + " • Date: " + data[4]);
        lblSub.setForeground(Color.GRAY);

        JButton btnAction = new JButton("Process");
        styleButton(btnAction, new Color(220, 53, 69), Color.WHITE);
        btnAction.setPreferredSize(new Dimension(80, 30));
        btnAction.addActionListener(e -> {
             // Basic prompt logic (expand as needed from previous discussions)
             String email = JOptionPane.showInputDialog(this, "Enter user email to verify:");
             if(email != null && !email.isEmpty()) {
                 Database.performPasswordReset(data[0], data[1], "Temp1234"); // Simplified call
                 JOptionPane.showMessageDialog(this, "Reset complete.");
                 refreshContent();
             }
        });

        addComponentsToCard(card, lblTitle, lblSub, btnAction);
        return card;
    }

    // --- CARD: Generic Approval (Leave, OT, ACR) ---
    private JPanel createReviewCard(String[] data) {
        String type = data[3];
        Color typeColor = getTypeColor(type);
        
        JPanel card = createBaseCard();
        card.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, typeColor));

        JLabel lblTitle = new JLabel(type + " Request");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(typeColor);
        
        JLabel lblSub = new JLabel(data[2] + " (" + data[1] + ") • " + data[4]);
        lblSub.setForeground(Color.DARK_GRAY);

        JButton btnReview = new JButton("Review");
        styleButton(btnReview, typeColor, Color.WHITE);
        btnReview.setPreferredSize(new Dimension(80, 30));
        
        btnReview.addActionListener(e -> {
            dispose(); // Close dialog
            // Navigate based on type
            if (navigationCallback != null) {
                switch(type) {
                    case "Leave": navigationCallback.accept("leave"); break;
                    case "OT": 
                    case "Holiday": navigationCallback.accept("ot"); break;
                    case "ACR": navigationCallback.accept("attendance"); break;
                    case "Payslip": navigationCallback.accept("payroll"); break;
                }
            }
        });

        addComponentsToCard(card, lblTitle, lblSub, btnReview);
        return card;
    }

    // --- UTILS ---
    private void loadEmployeeContent() {
        List<String[]> notifs = Database.getNotificationsWithDate(currentUser);
        if(notifs.isEmpty()) addEmptyMessage("No notifications.");
        for(String[] n : notifs) {
            JPanel card = createBaseCard();
            JTextArea txt = new JTextArea(n[0]);
            txt.setWrapStyleWord(true); txt.setLineWrap(true); txt.setOpaque(false); txt.setEditable(false);
            card.add(txt, BorderLayout.CENTER);
            JLabel date = new JLabel(n[1], SwingConstants.RIGHT);
            date.setForeground(Color.GRAY); date.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            card.add(date, BorderLayout.SOUTH);
            contentPanel.add(card);
            contentPanel.add(Box.createVerticalStrut(10));
        }
    }

    private JPanel createBaseCard() {
        JPanel p = new JPanel(new BorderLayout(10, 5));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        return p;
    }

    private void addComponentsToCard(JPanel card, JLabel title, JLabel sub, JButton btn) {
        JPanel textP = new JPanel(new GridLayout(2, 1));
        textP.setOpaque(false);
        textP.add(title);
        textP.add(sub);
        card.add(textP, BorderLayout.CENTER);
        
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnP.setOpaque(false);
        btnP.add(btn);
        card.add(btnP, BorderLayout.EAST);
    }

    private void addSectionHeader(String title, Color c) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(c);
        lbl.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPanel.add(lbl);
    }

    private void addEmptyMessage(String msg) {
        contentPanel.add(Box.createVerticalGlue());
        JLabel l = new JLabel(msg, SwingConstants.CENTER);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setForeground(Color.GRAY);
        contentPanel.add(l);
        contentPanel.add(Box.createVerticalGlue());
    }

    private void styleButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg); b.setForeground(fg);
        b.setFocusPainted(false); b.setFont(new Font("Segoe UI", Font.BOLD, 11));
    }

    private Color getTypeColor(String type) {
        switch(type) {
            case "Leave": return new Color(255, 193, 7); // Amber
            case "ACR": return new Color(23, 162, 184); // Teal
            case "Payslip": return new Color(40, 167, 69); // Green
            default: return new Color(108, 117, 125); // Gray
        }
    }
}