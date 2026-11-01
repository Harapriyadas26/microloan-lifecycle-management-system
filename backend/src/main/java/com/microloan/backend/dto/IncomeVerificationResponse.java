package com.microloan.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record IncomeVerificationResponse(BigDecimal declaredIncome, BigDecimal observedIncome,
    String status, String scenario, LocalDateTime checkedAt, String source,
    List<BigDecimal> monthlyCredits) {}
