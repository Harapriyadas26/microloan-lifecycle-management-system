public class ReportGeneration {

    // Generate Loan Report
    public void generateLoanReport(
            String borrowerName,
            double loanAmount,
            double amountPaid,
            double balance) {

        System.out.println("===== LOAN REPORT =====");
        System.out.println("Borrower Name: " + borrowerName);
        System.out.println("Loan Amount: Rs. " + loanAmount);
        System.out.println("Amount Paid: Rs. " + amountPaid);
        System.out.println("Remaining Balance: Rs. " + balance);
        System.out.println("=======================");
    }

    // Generate Overdue Report
    public void generateOverdueReport(
            String borrowerName,
            int overdueDays,
            double overdueAmount) {

        System.out.println("===== OVERDUE REPORT =====");
        System.out.println("Borrower Name: " + borrowerName);
        System.out.println("Overdue Days: " + overdueDays);
        System.out.println("Overdue Amount: Rs. " + overdueAmount);
        System.out.println("==========================");
    }

    public static void main(String[] args) {

        ReportGeneration report = new ReportGeneration();

        report.generateLoanReport(
                "Rahul", 50000, 20000, 30000);

        report.generateOverdueReport(
                "Rahul", 15, 5000);
    }
}