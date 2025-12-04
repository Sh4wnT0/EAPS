package Fproj;

public class EmployeeData {

    private String empNum;
    private String name;
    private String position;
    private double dailyPay;

    public EmployeeData(String empNum, String name, String position, double dailyPay) {
        this.empNum = empNum;
        this.name = name;
        this.position = position;
        this.dailyPay = dailyPay;
    }

    public String getEmpNum() { return empNum; }
    public String getName() { return name; }
    public String getPosition() { return position; }
    public double getDailyPay() { return dailyPay; }
}
