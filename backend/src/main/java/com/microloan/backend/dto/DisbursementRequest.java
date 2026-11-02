package com.microloan.backend.dto;
import jakarta.validation.constraints.*;
public record DisbursementRequest(@NotBlank @Pattern(regexp="[A-Za-z0-9-]{5,40}") String reference) {}
