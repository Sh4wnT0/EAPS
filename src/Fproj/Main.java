package Fproj;

import javax.swing.*;
import java.awt.*;


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
    	
        frame = new JFrame("Attendance & Payroll System");
        frame.setSize(1000, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Add screens
        cardPanel.add(new First(), "main");
        cardPanel.add(new Second(), "adminLogin");
        cardPanel.add(new Third(), "employeeLogin");
        cardPanel.add(new Admin(), "AdDashboard");
        cardPanel.add(new Employee(null), "EmpDashboard");
       
        
        
        frame.add(cardPanel);
        frame.setVisible(true);

        // Display the main page first
        cardLayout.show(cardPanel, "main");
    }
}
