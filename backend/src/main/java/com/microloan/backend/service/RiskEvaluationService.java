package com.microloan.backend.service;

import com.microloan.backend.dto.RiskAssessmentResponse;
import com.microloan.backend.entity.*;
import com.microloan.backend.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RiskEvaluationService {

  private final RiskAssessmentRepository assessments;
  private final BorrowerProfileRepository profiles;
  private final KycDocumentRepository documents;

  public RiskEvaluationService(
    RiskAssessmentRepository a,
    BorrowerProfileRepository p,
    KycDocumentRepository d
  ) {
    assessments = a;
    profiles = p;
    documents = d;
  }

  @Transactional
  public RiskAssessmentResponse evaluate(LoanApplication loan) {
    BorrowerProfile profile = profiles
      .findByUserEmailIgnoreCase(loan.getBorrower().getEmail())
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Complete borrower profile first"
        )
      );
    var docs = documents.findByUserEmailIgnoreCaseOrderByUploadedAtDesc(
      loan.getBorrower().getEmail()
    );
    var result = calculate(
      profile.getMonthlyIncome(),
      profile.getEmploymentType(),
      loan.getLoanAmount(),
      loan.getTenureMonths(),
      loan.getExistingMonthlyDebt(),
      docs.stream().map(KycDocument::getStatus).toList()
    );
    RiskAssessment a = assessments
      .findByApplicationId(loan.getId())
      .orElseGet(RiskAssessment::new);
    a.setApplication(loan);
    a.setScore(result.score());
    a.setRiskLevel(result.riskLevel());
    a.setRecommendation(result.recommendation());
    a.setEstimatedMonthlyInstallment(result.estimatedMonthlyInstallment());
    a.setDebtToIncomeRatio(result.debtToIncomeRatio());
    a.setLoanToAnnualIncomeRatio(result.loanToAnnualIncomeRatio());
    a.setReasons(String.join("\n", result.reasons()));
    a.setEvaluatedAt(LocalDateTime.now());
    a.setRuleVersion("RULES_V1");
    return toResponse(assessments.saveAndFlush(a));
  }

  @Transactional(readOnly = true)
  public RiskAssessmentResponse get(Long id) {
    return assessments
      .findByApplicationId(id)
      .map(this::toResponse)
      .orElse(null);
  }

  public RiskAssessmentResponse toResponse(RiskAssessment a) {
    return new RiskAssessmentResponse(
      a.getId(),
      a.getScore(),
      a.getRiskLevel(),
      a.getRecommendation(),
      a.getEstimatedMonthlyInstallment(),
      a.getDebtToIncomeRatio(),
      a.getLoanToAnnualIncomeRatio(),
      List.of(a.getReasons().split("\n")),
      a.getEvaluatedAt(),
      a.getRuleVersion()
    );
  }

  // Ratios are calculated to 12 places for scoring; display/persistence uses 6 places.
  // The installment is a principal-only estimate, never a repayment schedule or interest quote.
  public static RiskLevel classify(int score) {
    return score >= 75
      ? RiskLevel.LOW
      : score >= 50
        ? RiskLevel.MEDIUM
        : RiskLevel.HIGH;
  }

  public RiskAssessmentResponse calculate(
    BigDecimal income,
    EmploymentType employment,
    BigDecimal amount,
    Integer tenure,
    BigDecimal debt,
    List<KycStatus> kyc
  ) {
    if (
      income == null ||
      income.signum() <= 0 ||
      employment == null ||
      amount == null ||
      amount.signum() <= 0 ||
      tenure == null ||
      tenure <= 0 ||
      debt == null ||
      debt.signum() < 0
    ) throw new ResponseStatusException(
      HttpStatus.BAD_REQUEST,
      "Positive income, amount and tenure and non-negative debt are required"
    );
    BigDecimal installment = amount.divide(
      BigDecimal.valueOf(tenure),
      2,
      RoundingMode.HALF_UP
    );
    BigDecimal dti = debt
      .add(installment)
      .multiply(new BigDecimal("100"))
      .divide(income, 12, RoundingMode.HALF_UP);
    BigDecimal ratio = amount.divide(
      income.multiply(new BigDecimal("12")),
      12,
      RoundingMode.HALF_UP
    );
    int dtiPenalty =
      dti.compareTo(new BigDecimal("30")) <= 0
        ? 0
        : dti.compareTo(new BigDecimal("40")) <= 0
          ? 15
          : dti.compareTo(new BigDecimal("50")) <= 0
            ? 30
            : 50;
    int ratioPenalty =
      ratio.compareTo(new BigDecimal("0.5")) <= 0
        ? 0
        : ratio.compareTo(BigDecimal.ONE) <= 0
          ? 10
          : ratio.compareTo(new BigDecimal("2")) <= 0
            ? 20
            : 35;
    int employmentPenalty = switch (employment) {
      case SALARIED -> 0;
      case SELF_EMPLOYED -> 5;
      case DAILY_WAGE -> 15;
      case OTHER -> 10;
      case STUDENT -> 25;
      case UNEMPLOYED -> 30;
    };
    boolean verified = kyc.contains(KycStatus.VERIFIED),
      pending = kyc.contains(KycStatus.PENDING),
      rejected = kyc.contains(KycStatus.REJECTED);
    int score = Math.clamp(
      100 - dtiPenalty - ratioPenalty - employmentPenalty - (verified ? 0 : 10),
      0,
      100
    );
    RiskLevel level = classify(score);
    RiskRecommendation recommendation =
      level == RiskLevel.HIGH
        ? RiskRecommendation.REJECT
        : level == RiskLevel.LOW && verified
          ? RiskRecommendation.APPROVE
          : RiskRecommendation.MANUAL_REVIEW;
    List<String> reasons = new ArrayList<>();
    reasons.add(
      "Principal-only monthly estimate: " +
        installment +
        "; interest is excluded."
    );
    reasons.add(
      "Debt-to-income: " +
        dti.setScale(2, RoundingMode.HALF_UP) +
        "% (-" +
        dtiPenalty +
        " points)."
    );
    reasons.add(
      "Loan / annual income: " +
        ratio.setScale(3, RoundingMode.HALF_UP) +
        " (-" +
        ratioPenalty +
        " points)."
    );
    reasons.add(
      "Employment: " + employment + " (-" + employmentPenalty + " points)."
    );
    reasons.add(
      verified
        ? "At least one KYC document is verified."
        : pending
          ? "KYC is pending; manual review and verification are required (-10 points)."
          : "No usable KYC; approval is blocked (-10 points)."
    );
    if (rejected) reasons.add(
      "A rejected KYC document exists; review its rejection reason."
    );
    reasons.add(
      "Recommendation only. A loan officer makes the final decision."
    );
    return new RiskAssessmentResponse(
      null,
      score,
      level,
      recommendation,
      installment,
      dti.setScale(6, RoundingMode.HALF_UP),
      ratio.setScale(6, RoundingMode.HALF_UP),
      reasons,
      LocalDateTime.now(),
      "RULES_V1"
    );
  }
}
