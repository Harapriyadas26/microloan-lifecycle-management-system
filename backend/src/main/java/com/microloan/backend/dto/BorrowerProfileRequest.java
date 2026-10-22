package com.microloan.backend.dto;

import com.microloan.backend.entity.EmploymentType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BorrowerProfileRequest(

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^[0-9]{10}$",
                message = "Phone number must contain exactly 10 digits"
        )
        String phone,

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @NotBlank(message = "Address is required")
        @Size(max = 250, message = "Address cannot exceed 250 characters")
        String address,

        @NotBlank(message = "City is required")
        @Size(max = 80, message = "City cannot exceed 80 characters")
        String city,

        @NotBlank(message = "State is required")
        @Size(max = 80, message = "State cannot exceed 80 characters")
        String state,

        @NotBlank(message = "Postal code is required")
        @Pattern(
                regexp = "^[0-9]{6}$",
                message = "Postal code must contain exactly 6 digits"
        )
        String postalCode,

        @NotBlank(message = "Occupation is required")
        @Size(max = 100, message = "Occupation cannot exceed 100 characters")
        String occupation,

        @NotNull(message = "Employment type is required")
        EmploymentType employmentType,

        @NotNull(message = "Monthly income is required")
        @DecimalMin(
                value = "0.0",
                inclusive = false,
                message = "Monthly income must be greater than zero"
        )
        @Digits(
                integer = 10,
                fraction = 2,
                message = "Enter a valid monthly income"
        )
        BigDecimal monthlyIncome
) {
}