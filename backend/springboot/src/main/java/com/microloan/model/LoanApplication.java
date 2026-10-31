package com.microloan.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loan_applications")
public class LoanApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "borrower_id", nullable = false)
    private Long borrowerId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "requested_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "tenure_months", nullable = false)
    private Integer tenureMonths;

    private String purpose;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "approved_by_user_id")
    private Long approvedByUserId;

    @Column(name = "applied_on", nullable = false)
    private LocalDate appliedOn = LocalDate.now();

    public Long getApplicationId() { return applicationId; }
    public Long getBorrowerId() { return borrowerId; }
    public void setBorrowerId(Long borrowerId) { this.borrowerId = borrowerId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }
    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getApprovedByUserId() { return approvedByUserId; }
    public void setApprovedByUserId(Long approvedByUserId) { this.approvedByUserId = approvedByUserId; }
    public LocalDate getAppliedOn() { return appliedOn; }
}
