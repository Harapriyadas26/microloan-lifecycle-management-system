package com.microloan.backend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications")
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "borrower_id", nullable = false)
    private User borrower;

    @Column(name = "loan_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal loanAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LoanPurpose purpose;

    @Column(name = "purpose_description", length = 300)
    private String purposeDescription;

    @Column(name = "tenure_months", nullable = false)
    private Integer tenureMonths;

    @Column(
            name = "existing_monthly_debt",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal existingMonthlyDebt = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LoanStatus status = LoanStatus.SUBMITTED;

    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public LoanApplication() {
    }

    @PrePersist
    public void beforeInsert() {
        LocalDateTime now = LocalDateTime.now();
        submittedAt = now;
        updatedAt = now;

        if (status == null) {
            status = LoanStatus.SUBMITTED;
        }

        if (existingMonthlyDebt == null) {
            existingMonthlyDebt = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getBorrower() {
        return borrower;
    }

    public void setBorrower(User borrower) {
        this.borrower = borrower;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public LoanPurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(LoanPurpose purpose) {
        this.purpose = purpose;
    }

    public String getPurposeDescription() {
        return purposeDescription;
    }

    public void setPurposeDescription(String purposeDescription) {
        this.purposeDescription = purposeDescription;
    }

    public Integer getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(Integer tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    public BigDecimal getExistingMonthlyDebt() {
        return existingMonthlyDebt;
    }

    public void setExistingMonthlyDebt(BigDecimal existingMonthlyDebt) {
        this.existingMonthlyDebt = existingMonthlyDebt;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}