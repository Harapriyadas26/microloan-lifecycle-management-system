package com.microloan.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record LoanApplicationRequest(
    @NotNull @Positive Long borrowerId,
    @Positive Long productId,
    @NotNull @DecimalMin("1.00") BigDecimal requestedAmount,
    @NotNull @Min(1) @Max(60) Integer tenureMonths,
    @NotBlank @Size(max = 250) String purpose
) {}
