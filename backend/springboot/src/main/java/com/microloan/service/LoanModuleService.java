package com.microloan.service;

import com.microloan.dto.*;
import com.microloan.model.*;
import com.microloan.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class LoanModuleService {
    private final LoanApplicationRepository applications;
    private final LoanRepository loans;
    private final RepaymentScheduleRepository schedules;
    private final PaymentRepository payments;

    public LoanModuleService(LoanApplicationRepository applications, LoanRepository loans,
                             RepaymentScheduleRepository schedules, PaymentRepository payments) {
        this.applications = applications;
        this.loans = loans;
        this.schedules = schedules;
        this.payments = payments;
    }

    // M3 — Submit, validate and track loan requests. M4/M5 own risk and approval decisions.
    public LoanApplication submitApplication(LoanApplicationRequest request) {
        LoanApplication app = new LoanApplication();
        app.setBorrowerId(request.borrowerId());
        app.setProductId(request.productId());
        app.setRequestedAmount(request.requestedAmount());
        app.setTenureMonths(request.tenureMonths());
        app.setPurpose(request.purpose().trim());
        app.setStatus("PENDING");
        return applications.save(app);
    }

    public LoanApplication getApplication(Long applicationId) {
        return applications.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan application not found: " + applicationId));
    }

    public List<LoanApplication> getBorrowerApplications(Long borrowerId) {
        return applications.findByBorrowerIdOrderByAppliedOnDesc(borrowerId);
    }

    // M6 — Generate a reducing-balance EMI schedule only after M5 creates an ACTIVE loan.
    @Transactional
    public List<RepaymentSchedule> generateSchedule(Long loanId) {
        Loan loan = loans.findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));

        if (!"ACTIVE".equalsIgnoreCase(loan.getLoanStatus())) {
            throw new IllegalStateException("A schedule can be generated only for an approved ACTIVE loan.");
        }
        if (!schedules.findByLoanIdOrderByInstalmentNumber(loanId).isEmpty()) {
            throw new IllegalStateException("A repayment schedule already exists for this loan.");
        }
        if (loan.getTenureMonths() == null || loan.getTenureMonths() < 1
                || loan.getPrincipal() == null || loan.getPrincipal().signum() <= 0
                || loan.getAnnualInterestRate() == null || loan.getAnnualInterestRate().signum() < 0) {
            throw new IllegalStateException("The approved loan is missing valid principal, rate or tenure details.");
        }

        int months = loan.getTenureMonths();
        BigDecimal monthlyRate = loan.getAnnualInterestRate()
            .divide(new BigDecimal("1200"), 12, RoundingMode.HALF_UP);
        BigDecimal emi;

        if (monthlyRate.signum() == 0) {
            emi = loan.getPrincipal().divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        } else {
            double r = monthlyRate.doubleValue();
            double p = loan.getPrincipal().doubleValue();
            double factor = Math.pow(1.0 + r, months);
            emi = BigDecimal.valueOf(p * r * factor / (factor - 1.0))
                .setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal balance = loan.getPrincipal();
        LocalDate dueDate = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= months; i++) {
            BigDecimal interest = balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPart = emi.subtract(interest).setScale(2, RoundingMode.HALF_UP);
            if (i == months || principalPart.compareTo(balance) > 0) {
                principalPart = balance;
            }

            RepaymentSchedule row = new RepaymentSchedule();
            row.setLoanId(loanId);
            row.setInstalmentNumber(i);
            row.setDueDate(dueDate);
            row.setInterestComponent(interest);
            row.setPrincipalComponent(principalPart);
            row.setEmiAmount(principalPart.add(interest).setScale(2, RoundingMode.HALF_UP));
            balance = balance.subtract(principalPart).max(BigDecimal.ZERO);
            row.setBalanceAfterPayment(balance);
            row.setInstalmentStatus("PENDING");
            schedules.save(row);
            dueDate = dueDate.plusMonths(1);
        }
        return schedules.findByLoanIdOrderByInstalmentNumber(loanId);
    }

    public List<RepaymentSchedule> getSchedule(Long loanId) {
        if (!loans.existsById(loanId)) {
            throw new ResourceNotFoundException("Loan not found: " + loanId);
        }
        return schedules.findByLoanIdOrderByInstalmentNumber(loanId);
    }

    // M12 — Record a supplied transaction reference; reconcile separately after trusted confirmation.
    @Transactional
    public Payment recordPayment(PaymentRequest request) {
        RepaymentSchedule row = schedules.findById(request.scheduleId())
            .orElseThrow(() -> new ResourceNotFoundException("Repayment instalment not found."));
        if ("PAID".equalsIgnoreCase(row.getInstalmentStatus())) {
            throw new IllegalStateException("This instalment is already marked as paid.");
        }
        if (request.amount().compareTo(row.getEmiAmount()) != 0) {
            throw new IllegalArgumentException("For this prototype, payment amount must equal the instalment EMI.");
        }
        if (payments.existsByTransactionReference(request.transactionReference())) {
            throw new IllegalArgumentException("This transaction reference has already been recorded.");
        }

        Payment payment = new Payment();
        payment.setScheduleId(row.getScheduleId());
        payment.setAmount(request.amount());
        payment.setTransactionReference(request.transactionReference().trim());
        payment.setPaymentStatus("RECORDED");
        return payments.save(payment);
    }

    @Transactional
    public Payment reconcilePayment(ReconcilePaymentRequest request) {
        Payment payment = payments.findByTransactionReference(request.transactionReference().trim())
            .orElseThrow(() -> new ResourceNotFoundException("Transaction reference not found."));
        if (payment.getAmount().compareTo(request.verifiedAmount()) != 0) {
            throw new IllegalArgumentException("Verified amount does not match the recorded payment.");
        }
        if ("RECONCILED".equalsIgnoreCase(payment.getPaymentStatus())) {
            throw new IllegalStateException("This payment is already reconciled.");
        }

        RepaymentSchedule row = schedules.findById(payment.getScheduleId())
            .orElseThrow(() -> new ResourceNotFoundException("Repayment instalment not found."));
        Loan loan = loans.findById(row.getLoanId())
            .orElseThrow(() -> new ResourceNotFoundException("Loan not found."));

        row.setInstalmentStatus("PAID");
        schedules.save(row);

        BigDecimal newBalance = loan.getOutstandingBalance()
            .subtract(row.getPrincipalComponent()).max(BigDecimal.ZERO);
        loan.setOutstandingBalance(newBalance);
        if (newBalance.signum() == 0) {
            loan.setLoanStatus("CLOSED");
        }
        loans.save(loan);

        payment.setPaymentStatus("RECONCILED");
        return payments.save(payment);
    }

    public List<Payment> getAllPayments() {
        return payments.findAll();
    }

    public List<Payment> getPaymentsForLoan(Long loanId) {
        List<Long> scheduleIds = schedules.findByLoanIdOrderByInstalmentNumber(loanId)
            .stream().map(RepaymentSchedule::getScheduleId).toList();
        return payments.findByScheduleIdIn(scheduleIds);
    }
}
