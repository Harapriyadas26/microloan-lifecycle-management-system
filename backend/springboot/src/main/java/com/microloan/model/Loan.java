package com.microloan.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "loans")
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_id")
    private Long loanId;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "borrower_id", nullable = false)
    private Long borrowerId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal principal;

    @Column(name = "annual_interest_rate", nullable = false, precision = 6, scale = 3)
    private BigDecimal annualInterestRate;

    @Column(name = "outstanding_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal outstandingBalance;

    @Column(name = "tenure_months", nullable = false)
    private Integer tenureMonths;

    @Column(name = "loan_status", nullable = false)
    private String loanStatus = "ACTIVE";

    public Long getLoanId() { return loanId; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public Long getBorrowerId() { return borrowerId; }
    public void setBorrowerId(Long borrowerId) { this.borrowerId = borrowerId; }
    public BigDecimal getPrincipal() { return principal; }
    public void setPrincipal(BigDecimal principal) { this.principal = principal; }
    public BigDecimal getAnnualInterestRate() { return annualInterestRate; }
    public void setAnnualInterestRate(BigDecimal annualInterestRate) { this.annualInterestRate = annualInterestRate; }
    public BigDecimal getOutstandingBalance() { return outstandingBalance; }
    public void setOutstandingBalance(BigDecimal outstandingBalance) { this.outstandingBalance = outstandingBalance; }
    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }
    public String getLoanStatus() { return loanStatus; }
    public void setLoanStatus(String loanStatus) { this.loanStatus = loanStatus; }
}
