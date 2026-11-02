package com.microloan.backend.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
public record LoanAccountResponse(Long applicationId, String status, BigDecimal principal,
  BigDecimal annualRate, String disbursementReference, LocalDateTime disbursedAt,
  LocalDateTime closedAt, BigDecimal outstanding, List<InstallmentView> installments,
  List<PaymentView> payments) {
  public record InstallmentView(int number, LocalDate dueDate, BigDecimal principalDue,
    BigDecimal interestDue, BigDecimal penaltyDue, BigDecimal amountPaid,
    BigDecimal remaining, String status) {}
  public record PaymentView(String reference, BigDecimal amount, LocalDateTime recordedAt, String recordedBy) {}
}
