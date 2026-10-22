package com.microloan.backend.dto;

import com.microloan.backend.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record RiskAssessmentResponse(
  Long id,
  int score,
  RiskLevel riskLevel,
  RiskRecommendation recommendation,
  BigDecimal estimatedMonthlyInstallment,
  BigDecimal debtToIncomeRatio,
  BigDecimal loanToAnnualIncomeRatio,
  List<String> reasons,
  LocalDateTime evaluatedAt,
  String ruleVersion
) {}
