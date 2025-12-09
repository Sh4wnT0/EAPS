package lab2;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class Main extends JFrame {
	// Method calls 220 and 221.
	public static boolean ValiDate(String dateStr, String format) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        try {
        	LocalDate.parse(dateStr, formatter); 
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
	}
	
	public static boolean Petsada(String startStr, String returnStr, String format) {
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
	    try {
	    	 LocalDate startDate = LocalDate.parse(startStr, formatter);
	         LocalDate returnDate = LocalDate.parse(returnStr, formatter);
	         
	         return !returnDate.isBefore(startDate);
	    } catch (DateTimeParseException e) {
	            return false;
	    }
	}
	
	int i = 0;
	String name = "None";
	String id = "None";
	String ePass = "None";
	String dest = "None";
	String start = "None";
	String end = "None";
	String orderNum = "None";
	
	// class and hashmap calls 285, 286, 234, 235, 368 - 370
	public class displayDetails {
	    String employeeName;
	    String employeeID;
	    String employeePass;
	    String destination;
	    String startDate;
	    String returnDate;
	    String orderNumber;
	   
	    
	    public displayDetails(String name, String id, String ePass, String dest, String start, String end, String orderNum) {
	        this.employeeName = name;
	        this.employeeID = id;
	        this.employeePass = ePass;
	        this.destination = dest;
	        this.startDate = start;
	        this.returnDate = end;
	        this.orderNumber = orderNum;
	    }

	    public String toString() {
	        return 
	        	   "System Record: \n" +
	               "\nName: " + employeeName + "\n" +
	               "ID: " + employeeID + "\n" +
	               "Employee Pass:" + employeePass + "\n" +
	               "Travel Order: " + orderNumber + "\n" +
	               "Destinationstem: " + destination + "\n" +
	               "Start: " + startDate + "\n" +
	               "Return: " + returnDate + "\n";
	    }
	}
	Map<String, displayDetails> ordersByID = new HashMap<>();
	Map<String, displayDetails> ordersByNumber = new HashMap<>();
	
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textField;
	private JLabel lblNewLabel_2;
	private JTextField textField_1;
	private JTextField textField_2;
	private JLabel lblNewLabel_3;
	private JLabel lblNewLabel_4;
	private JTextField txtMmddyyyy;
	private JLabel lblNewLabel_5;
	private JTextField txtMmddyyyy_1;
	private JButton btnNewButton;
	private JButton btnAddTravelOrder;
	private JButton btnDisplayDetails;
	private JTextArea textArea;
	private JScrollPane scrollPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Try frame = new Try();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Main() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 550);
		setResizable(false);
		setTitle("Order Management System");
		contentPane = new JPanel();
		contentPane.setBackground(new Color(255, 182, 193));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("ORDER MANAGEMENT SYSTEM");
		lblNewLabel.setBackground(new Color(255, 128, 192));
		lblNewLabel.setForeground(new Color(0, 0, 0));
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(10, 10, 416, 22);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Employee Name:");
		lblNewLabel_1.setBackground(new Color(255, 128, 255));
		lblNewLabel_1.setBounds(10, 42, 114, 22);
		contentPane.add(lblNewLabel_1);
		
		textField = new JTextField();
		textField.setBounds(134, 43, 292, 22);
		contentPane.add(textField);
		textField.setColumns(10);
		
		lblNewLabel_2 = new JLabel("Employee ID:");
		lblNewLabel_2.setBounds(10, 70, 231, 22);
		contentPane.add(lblNewLabel_2);
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		textField_1.setBounds(134, 71, 292, 22);
		contentPane.add(textField_1);
		
		textField_2 = new JTextField();
		textField_2.setColumns(10);
		textField_2.setBounds(134, 100, 292, 22);
		contentPane.add(textField_2);
		
		lblNewLabel_3 = new JLabel("Travel Destination:");
		lblNewLabel_3.setBounds(10, 99, 231, 22);
		contentPane.add(lblNewLabel_3);
		
		lblNewLabel_4 = new JLabel("Travel Date:");
		lblNewLabel_4.setBounds(10, 128, 231, 22);
		contentPane.add(lblNewLabel_4);
		
		txtMmddyyyy = new JTextField();
		txtMmddyyyy.setText("MM-DD-YYYY");
		txtMmddyyyy.setColumns(10);
		txtMmddyyyy.setBounds(134, 129, 292, 22);
		contentPane.add(txtMmddyyyy);
		
		lblNewLabel_5 = new JLabel("Return Date:");
		lblNewLabel_5.setBounds(10, 157, 231, 22);
		contentPane.add(lblNewLabel_5);
		
		txtMmddyyyy_1 = new JTextField();
		txtMmddyyyy_1.setText("MM-DD-YYYY");
		txtMmddyyyy_1.setColumns(10);
		txtMmddyyyy_1.setBounds(134, 158, 292, 22);
		contentPane.add(txtMmddyyyy_1);
		
		btnNewButton = new JButton("Add Employee");
		btnNewButton.setBounds(10, 189, 114, 20);
		contentPane.add(btnNewButton);
		
		btnAddTravelOrder = new JButton("Add Travel Order");
		btnAddTravelOrder.setBounds(134, 189, 148, 20);
		contentPane.add(btnAddTravelOrder);
		
		btnDisplayDetails = new JButton("Display Details");
		btnDisplayDetails.setBounds(292, 189, 134, 20);
		contentPane.add(btnDisplayDetails);
		
		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(10, 218, 416, 285);
		contentPane.add(scrollPane);
		
		
		btnAddTravelOrder.addActionListener(e -> {
			String getdestination = textField_2.getText();
			int randomNum = (int)(Math.random() * 10000);
			String orderNum = "" + randomNum;
			String getstart = txtMmddyyyy.getText();
			String getend = txtMmddyyyy_1.getText();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
			
			if (getdestination.isEmpty()) {
	            textArea.setText("\n Invalid Input. Travel Destination cannot be empty.");
	            return;
			}
			
	        if (getstart.isEmpty() || getstart.equals("MM-DD-YYYY")) {
	            textArea.setText("\n Invalid Input. Travel Date cannot be empty.");
	            return;
	        }
	        
	        if (getstart.chars().anyMatch(Character::isLetter)) {
	            textArea.setText("\n Invalid Input. Travel Date cannot contain letters: \"" + getstart + "\"");
	            return;
	        }
	        
	        if (getend.isEmpty() || getend.equals("MM-DD-YYYY")) {
	            textArea.setText("\n Invalid Input. Return Date cannot be empty.");
	            return;
	        }
            
	        if (getend.chars().anyMatch(Character::isLetter)) {
	            textArea.setText("\n Invalid Input. Return Date cannot contain letters: \"" + getend + "\"");
	            return;
	        }
	        
	        if (getstart.length() != 10) {
	            textArea.setText("\n Invalid Input. Date must contain 10 characters.");
	            return;
	        }
	        
	        if (getend.length() != 10) {
	            textArea.setText("\n Invalid Input. Date must contain 10 characters.");
	            return;
	        }
	        
            LocalDate startDate = LocalDate.parse(getstart, formatter);
			LocalDate todayDate = LocalDate.now();
			
        	if (startDate.isBefore(todayDate)) {
	            textArea.setText("\n Invalid Input. Travel Date cannot be earlier than today.");
	            return;
        	}
        	
        	LocalDate endDate = LocalDate.parse(getend, formatter);
        	if (endDate.isBefore(startDate)) {
	            textArea.setText("\n Invalid Input. Return Date cannot be earlier than Travel Date.");
	            return;
        	}
	        if (textArea.getText().contains("Invalid Input")) {
	        	textArea.setText("");
				
				
	        }
	        textArea.append("\nTravel Order Details\n");
        	textArea.append("\nTravel Destination: " + getdestination);
			textArea.append("\nTravel Order: " + randomNum);
			textArea.append("\nTravel Start: " + getstart);
			textArea.append("\nTravel End: " + getend);
	        
			txtMmddyyyy.setText("MM-DD-YYYY");
			txtMmddyyyy_1.setText("MM-DD-YYYY");
			
			displayDetails order = new displayDetails(name, id, ePass, getdestination, getstart, getend, orderNum);
			ordersByNumber.put(orderNum, order);
		});
		// add employee function 
		btnNewButton.addActionListener(e -> {
			
			String getname = textField.getText().trim();
			String getstrip = textField.getText().replaceAll("\\s+", "");
			String getid = textField_1.getText().trim();

			if (getname.isEmpty()) {
			    textArea.setText("\nInvalid Input. Employee Name cannot be empty.");
			    return;
			}

			if (!getstrip.chars().allMatch(Character::isLetter)) {
			    textArea.setText("\nInvalid Input. Employee Name must not contain numbers.");
			    textField.setText("Please put your real name here.");
			    return;
			}

			if (getid.length() != 5 || !getid.chars().allMatch(Character::isDigit)) {
			    textArea.setText("\nInvalid Input. Employee ID must be 5 digit numbers (00001 - 99999).");
			    return;
			}

			if (ordersByID.containsKey(getid)) {
			    textArea.setText("");
			    textArea.append("The ID: " + getid + " is already taken.");
			    return;
			}

			if (textArea.getText().contains("Invalid Input")) {
			    textArea.setText("");
			}

			i++;
			String pass = "EP0000" + i;
			textArea.append("Employee Details: \n");
			textArea.append("\nEmployee Pass: " + pass);
			textArea.append("\nEmployee Name: " + getname);
			textArea.append("\nEmployee ID: " + getid);
			textField.setText("");
			textField_1.setText("");

			displayDetails order = new displayDetails(getname, getid, pass, dest, start, end, orderNum);
			ordersByID.put(getid, order);
			
			
		});
		
		btnDisplayDetails.addActionListener(e -> {
			String getname = textField.getText().trim();
			String getstrip = textField.getText().replaceAll("\\s+", "");
			String getid = textField_1.getText().trim();
			String getOnum = textField_2.getText().trim();
			String getdestination = textField_2.getText().trim();
			int randomNum = (int)(Math.random() * 10000);
			String orderNum = String.valueOf(randomNum);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
			String getstart = txtMmddyyyy.getText().trim();
			String getend = txtMmddyyyy_1.getText().trim();
			LocalDate today = LocalDate.now();

		
			if (getid.isEmpty() || getOnum.isEmpty()) {
				if (getid.isEmpty() && getdestination.isEmpty()) {
					textArea.setText("\nPlease input your details first.");
					return;
				}
			    if (!getOnum.isEmpty()) {
			        displayDetails track = ordersByNumber.get(getOnum);
			        if (track != null) {
			            textArea.append("\nSearched by Travel Order: \n");
			            textArea.append(track.toString());
			            textField_2.setText("");
			            return;
			        } else {
			            textArea.setText("There is no travel information with the order number: " + getOnum);
			            textField_2.setText("");
			            return;
			        }
			    } else {
			        displayDetails search = ordersByID.get(getid);
			        if (search != null) {
			            textArea.append("\nSearched by Employee Number: \n");
			            textArea.append(search.toString());
			            textField_1.setText("");
			            return;
			        } else {
			            textArea.setText("\nNo Employee has the ID: " + getid);
			            textField_1.setText("");
			            return;
			        }
			    }
			}

			// Validate name
			if (!getstrip.chars().allMatch(Character::isLetter)) {
			    textArea.setText("\nInvalid Input. Employee Name must not contain numbers");
			    textField.setText("Please put your real name here.");
			    return;
			}

			// Validate ID format
			if (getid.length() != 5 || !getid.chars().allMatch(Character::isDigit)) {
			    textArea.setText("\nInvalid Input. Employee ID must be 5 digit numbers (00001 - 99999).");
			    return;
			}

			// Check if ID already exists
			if (ordersByID.containsKey(getid)) {
			    textArea.setText("ID: " + getid + " has already been taken.");
			    return;
			}

			// Validate date format
			if (getstart.equals("MM-DD-YYYY") || getend.equals("MM-DD-YYYY")) {
			    textArea.setText("\nPlease enter a valid date in the given format.");
			    txtMmddyyyy.setText("MM-DD-YYYY");
			    txtMmddyyyy_1.setText("MM-DD-YYYY");
			    return;
			}

			// Parse dates
			LocalDate startDate;
			LocalDate returnDate;
			try {
			    startDate = LocalDate.parse(getstart, formatter);
			    returnDate = LocalDate.parse(getend, formatter);
			} catch (Exception a ) {
			    textArea.setText("\nInvalid Input. Dates must be in MM-DD-YYYY format.");
			    return;
			}

			// Validate start date is not before today
			if (startDate.isBefore(today)) {
			    textArea.setText("\nInvalid Input. Travel Date cannot be earlier than today.");
			    return;
			}

			// Validate return date is not before start date
			if (!Petsada(getstart, getend, "MM-dd-yyyy")) {
			    textArea.setText("\nReturn date must be later or the same as the start date.");
			    txtMmddyyyy.setText("MM-DD-YYYY");
			    return;
			}

			// All validations passed — proceed to save and display
			i++;
			String pass = "EP0000" + i;
			textArea.append("\nEmployee and Travel Order details: \n");
			textArea.append("\nEmployee Name: " + getname);
			textArea.append("\nEmployee ID: " + getid);
			textArea.append("\nEmployee Pass: " + pass);
			textArea.append("\nTravel Destination: " + getdestination);
			textArea.append("\nTravel Order: " + orderNum);
			textArea.append("\nTravel Start: " + getstart);
			textArea.append("\nTravel End: " + getend);

			// Reset fields
			txtMmddyyyy.setText("MM-DD-YYYY");
			txtMmddyyyy_1.setText("MM-DD-YYYY");
			textField_2.setText("");
			textField.setText("");
			textField_1.setText("");

			// Store order
			displayDetails order = new displayDetails(getname, getid, pass, getdestination, getstart, getend, orderNum);
			ordersByID.put(getid, order);
			ordersByNumber.put(orderNum, order);
			
		});
	}
}