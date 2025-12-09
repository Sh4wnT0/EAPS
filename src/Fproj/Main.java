package Fproj;

import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class Main {
	public static java.util.ArrayList<EmployeeData> employeeList = new java.util.ArrayList<>();
    public static JFrame frame;
    public static CardLayout cardLayout;
    public static JPanel cardPanel;

    public static void main(String[] args) {
    	Database.createTable(); 
    	Database.createAttendanceTable();
    	Database.createLeaveTable();
    	Database.createNotificationsTable();
    	Database.createRequestsTable();
    	Database.createASRecordsTable();
    	Database.createACRTable();
    	Database.insertDefaultAdmin();
    	
        frame = new JFrame("Attendance & Payroll System");
        frame.setSize(1000, 500);
        frame.setMinimumSize(new Dimension(1000, 500));
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);  // Start maximized
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Add screens
        cardPanel.add(new First(), "main");
        cardPanel.add(new Third(), "Login");
        cardPanel.add(new Employee(null), "EmpDashboard");
        cardPanel.add(new contacts(), "contacts");

        
        
        frame.add(cardPanel);
        frame.setVisible(true);

        // Display the main page first
        cardLayout.show(cardPanel, "main");
    }
}
