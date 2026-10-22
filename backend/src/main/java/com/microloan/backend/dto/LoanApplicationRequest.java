package com.microloan.backend.dto;

import com.microloan.backend.entity.LoanPurpose;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record LoanApplicationRequest(

        @NotNull(message = "Loan amount is required")
        @DecimalMin(
                value = "1000.00",
                message = "Minimum loan amount is 1000"
        )
        @DecimalMax(
                value = "500000.00",
                message = "Maximum loan amount is 500000"
        )
        @Digits(integer = 10, fraction = 2)
        BigDecimal loanAmount,

        @NotNull(message = "Loan purpose is required")
        LoanPurpose purpose,

        @Size(
                max = 300,
                message = "Purpose description cannot exceed 300 characters"
        )
        String purposeDescription,

        @NotNull(message = "Tenure is required")
        @Min(value = 3, message = "Minimum tenure is 3 months")
        @Max(value = 60, message = "Maximum tenure is 60 months")
        Integer tenureMonths,

        @NotNull(message = "Existing monthly debt is required")
        @DecimalMin(
                value = "0.00",
                message = "Existing monthly debt cannot be negative"
        )
        @Digits(integer = 10, fraction = 2)
        BigDecimal existingMonthlyDebt
) {
}
