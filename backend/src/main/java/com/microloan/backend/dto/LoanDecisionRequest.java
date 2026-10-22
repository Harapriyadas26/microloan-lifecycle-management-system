package com.microloan.backend.dto;

import com.microloan.backend.entity.DecisionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record LoanDecisionRequest(
  @NotNull DecisionType decision,
  @NotBlank @Size(min = 5, max = 500) String reason,
  @DecimalMin(value = "0", inclusive = false)
  @Digits(integer = 10, fraction = 2)
  BigDecimal approvedAmount,
  @DecimalMin("0")
  @DecimalMax("60")
  @Digits(integer = 2, fraction = 2)
  BigDecimal annualInterestRate
) {}
