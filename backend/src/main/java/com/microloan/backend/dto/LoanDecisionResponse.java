package com.microloan.backend.dto;

import com.microloan.backend.entity.DecisionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanDecisionResponse(
  Long id,
  DecisionType decision,
  String reason,
  BigDecimal approvedAmount,
  BigDecimal annualInterestRate,
  Long officerId,
  String officerName,
  LocalDateTime decidedAt
) {}
