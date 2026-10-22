package com.microloan.backend.dto;

import com.microloan.backend.entity.EmploymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BorrowerProfileResponse(
        Long profileId,
        Long userId,
        String fullName,
        String email,
        String phone,
        LocalDate dateOfBirth,
        String address,
        String city,
        String state,
        String postalCode,
        String occupation,
        EmploymentType employmentType,
        BigDecimal monthlyIncome,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}