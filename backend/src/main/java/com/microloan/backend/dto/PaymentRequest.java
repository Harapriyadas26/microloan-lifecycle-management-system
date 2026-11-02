package com.microloan.backend.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record PaymentRequest(@NotBlank @Pattern(regexp="[A-Za-z0-9-]{5,40}") String reference,
  @NotNull @DecimalMin(value="0.00",inclusive=false) @Digits(integer=10,fraction=2) BigDecimal amount) {}
