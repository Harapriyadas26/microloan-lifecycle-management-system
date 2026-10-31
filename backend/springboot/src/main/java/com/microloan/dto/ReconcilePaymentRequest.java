package com.microloan.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ReconcilePaymentRequest(
    @NotBlank String transactionReference,
    @NotNull @DecimalMin("0.01") BigDecimal verifiedAmount
) {}
