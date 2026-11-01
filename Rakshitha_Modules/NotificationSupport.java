public class NotificationSupport {

    // Send Loan Application Status Notification
    public void sendApplicationNotification(
            String borrowerName,
            String status) {

        System.out.println("===== APPLICATION NOTIFICATION =====");
        System.out.println("Hello " + borrowerName);
        System.out.println("Your loan application status: " + status);
        System.out.println("====================================");
    }

    // Send Repayment Reminder
    public void sendRepaymentReminder(
            String borrowerName,
            double amount,
            String dueDate) {

        System.out.println("===== REPAYMENT REMINDER =====");
        System.out.println("Hello " + borrowerName);
        System.out.println("Payment Amount: Rs. " + amount);
        System.out.println("Due Date: " + dueDate);
        System.out.println("Please pay before the due date.");
        System.out.println("==============================");
    }

    // Send Overdue Alert
    public void sendOverdueAlert(
            String borrowerName,
            double amount) {

        System.out.println("===== OVERDUE ALERT =====");
        System.out.println("Hello " + borrowerName);
        System.out.println("Your payment is overdue.");
        System.out.println("Overdue Amount: Rs. " + amount);
        System.out.println("Please pay as soon as possible.");
        System.out.println("=========================");
    }

    public static void main(String[] args) {

        NotificationSupport notification =
                new NotificationSupport();

        notification.sendApplicationNotification(
                "Rahul", "Approved");

        notification.sendRepaymentReminder(
                "Rahul", 5000, "10-11-2026");

        notification.sendOverdueAlert(
                "Rahul", 2000);
    }
}