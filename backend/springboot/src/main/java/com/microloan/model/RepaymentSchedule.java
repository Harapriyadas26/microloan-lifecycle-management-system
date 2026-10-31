package com.microloan.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "repayment_schedule")
public class RepaymentSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "loan_id", nullable = false)
    private Long loanId;

    @Column(name = "instalment_number", nullable = false)
    private Integer instalmentNumber;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "emi_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "principal_component", nullable = false, precision = 12, scale = 2)
    private BigDecimal principalComponent;

    @Column(name = "interest_component", nullable = false, precision = 12, scale = 2)
    private BigDecimal interestComponent;

    @Column(name = "balance_after_payment", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceAfterPayment;

    @Column(name = "instalment_status", nullable = false)
    private String instalmentStatus = "PENDING";

    public Long getScheduleId() { return scheduleId; }
    public Long getLoanId() { return loanId; }
    public void setLoanId(Long loanId) { this.loanId = loanId; }
    public Integer getInstalmentNumber() { return instalmentNumber; }
    public void setInstalmentNumber(Integer instalmentNumber) { this.instalmentNumber = instalmentNumber; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public BigDecimal getEmiAmount() { return emiAmount; }
    public void setEmiAmount(BigDecimal emiAmount) { this.emiAmount = emiAmount; }
    public BigDecimal getPrincipalComponent() { return principalComponent; }
    public void setPrincipalComponent(BigDecimal principalComponent) { this.principalComponent = principalComponent; }
    public BigDecimal getInterestComponent() { return interestComponent; }
    public void setInterestComponent(BigDecimal interestComponent) { this.interestComponent = interestComponent; }
    public BigDecimal getBalanceAfterPayment() { return balanceAfterPayment; }
    public void setBalanceAfterPayment(BigDecimal balanceAfterPayment) { this.balanceAfterPayment = balanceAfterPayment; }
    public String getInstalmentStatus() { return instalmentStatus; }
    public void setInstalmentStatus(String instalmentStatus) { this.instalmentStatus = instalmentStatus; }
}
