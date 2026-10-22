package com.microloan.backend.dto;

import jakarta.validation.constraints.*;

public record ReasonRequest(
  @NotBlank @Size(min = 5, max = 300) String reason
) {}
