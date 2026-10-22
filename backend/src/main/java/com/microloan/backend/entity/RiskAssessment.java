package com.microloan.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_assessments")
public class RiskAssessment {

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

  @Column(nullable = false)
  private int score;

  public int getScore() {
    return score;
  }

  public void setScore(int value) {
    this.score = value;
  }

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RiskLevel riskLevel;

  public RiskLevel getRiskLevel() {
    return riskLevel;
  }

  public void setRiskLevel(RiskLevel value) {
    this.riskLevel = value;
  }

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RiskRecommendation recommendation;

  public RiskRecommendation getRecommendation() {
    return recommendation;
  }

  public void setRecommendation(RiskRecommendation value) {
    this.recommendation = value;
  }

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal estimatedMonthlyInstallment;

  public BigDecimal getEstimatedMonthlyInstallment() {
    return estimatedMonthlyInstallment;
  }

  public void setEstimatedMonthlyInstallment(BigDecimal value) {
    this.estimatedMonthlyInstallment = value;
  }

  @Column(nullable = false, precision = 30, scale = 6)
  private BigDecimal debtToIncomeRatio;

  public BigDecimal getDebtToIncomeRatio() {
    return debtToIncomeRatio;
  }

  public void setDebtToIncomeRatio(BigDecimal value) {
    this.debtToIncomeRatio = value;
  }

  @Column(nullable = false, precision = 30, scale = 6)
  private BigDecimal loanToAnnualIncomeRatio;

  public BigDecimal getLoanToAnnualIncomeRatio() {
    return loanToAnnualIncomeRatio;
  }

  public void setLoanToAnnualIncomeRatio(BigDecimal value) {
    this.loanToAnnualIncomeRatio = value;
  }

  @Column(nullable = false, length = 4000)
  private String reasons;

  public String getReasons() {
    return reasons;
  }

  public void setReasons(String value) {
    this.reasons = value;
  }

  @Column(nullable = false)
  private LocalDateTime evaluatedAt;

  public LocalDateTime getEvaluatedAt() {
    return evaluatedAt;
  }

  public void setEvaluatedAt(LocalDateTime value) {
    this.evaluatedAt = value;
  }

  @Column(nullable = false, length = 30)
  private String ruleVersion;

  public String getRuleVersion() {
    return ruleVersion;
  }

  public void setRuleVersion(String value) {
    this.ruleVersion = value;
  }
}
