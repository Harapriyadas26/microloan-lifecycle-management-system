package com.microloan.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PaymentRequest(
    @NotNull @Positive Long scheduleId,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotBlank @Size(max = 100) String transactionReference
) {}
