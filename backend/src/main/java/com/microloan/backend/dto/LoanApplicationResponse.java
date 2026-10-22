package com.microloan.backend.dto;

import com.microloan.backend.entity.LoanPurpose;
import com.microloan.backend.entity.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanApplicationResponse(
        Long id,
        Long borrowerId,
        String borrowerName,
        String borrowerEmail,
        BigDecimal loanAmount,
        LoanPurpose purpose,
        String purposeDescription,
        Integer tenureMonths,
        BigDecimal existingMonthlyDebt,
        LoanStatus status,
        LocalDateTime submittedAt,
        LocalDateTime updatedAt
) {
}