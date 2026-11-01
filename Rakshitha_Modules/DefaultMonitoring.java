import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DefaultMonitoring {

    public String checkDefault(
            LocalDate dueDate,
            boolean isPaid,
            int missedPayments) {

        if (isPaid) {
            return "Payment Completed";
        }

        LocalDate today = LocalDate.now();

        if (today.isAfter(dueDate)) {

            long overdueDays =
                ChronoUnit.DAYS.between(dueDate, today);

            if (missedPayments >= 3) {
                return "High Default Risk - "
                        + overdueDays + " days overdue";
            }

            return "Overdue - "
                    + overdueDays + " days";
        }

        return "Payment Due";
    }

    public static void main(String[] args) {

        DefaultMonitoring monitoring =
                new DefaultMonitoring();

        String result = monitoring.checkDefault(
                LocalDate.of(2026, 10, 1),
                false,
                3
        );

        System.out.println(result);
    }
}