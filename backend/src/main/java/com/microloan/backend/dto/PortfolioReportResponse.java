package com.microloan.backend.dto;
import java.math.BigDecimal;
public record PortfolioReportResponse(int activeLoans,int closedLoans,int overdueInstallments,
  int dueWithinSevenDays,BigDecimal totalDisbursed,BigDecimal outstanding) {}
