package com.microloan.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_decisions")
public class LoanDecision {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  public Long getId() {
    return id;
  }

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "application_id", nullable = false, unique = true)
  private LoanApplication application;

  public LoanApplication getApplication() {
    return application;
  }

  public void setApplication(LoanApplication value) {
    this.application = value;
  }

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DecisionType decision;

  public DecisionType getDecision() {
    return decision;
  }

  public void setDecision(DecisionType value) {
    this.decision = value;
  }

  @Column(nullable = false, length = 500)
  private String reason;

  public String getReason() {
    return reason;
  }

  public void setReason(String value) {
    this.reason = value;
  }

  @Column(precision = 12, scale = 2)
  private BigDecimal approvedAmount;

  public BigDecimal getApprovedAmount() {
    return approvedAmount;
  }

  public void setApprovedAmount(BigDecimal value) {
    this.approvedAmount = value;
  }

  @Column(precision = 5, scale = 2)
  private BigDecimal annualInterestRate;

  public BigDecimal getAnnualInterestRate() {
    return annualInterestRate;
  }

  public void setAnnualInterestRate(BigDecimal value) {
    this.annualInterestRate = value;
  }

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "officer_id", nullable = false)
  private User officer;

  public User getOfficer() {
    return officer;
  }

  public void setOfficer(User value) {
    this.officer = value;
  }

  @Column(nullable = false)
  private LocalDateTime decidedAt;

  public LocalDateTime getDecidedAt() {
    return decidedAt;
  }

  public void setDecidedAt(LocalDateTime value) {
    this.decidedAt = value;
  }
}
