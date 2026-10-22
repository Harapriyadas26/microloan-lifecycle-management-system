package com.microloan.backend.dto;

import com.microloan.backend.entity.DocumentType;
import com.microloan.backend.entity.KycStatus;

import java.time.LocalDateTime;

public record KycDocumentResponse(
        Long id,
        Long borrowerId,
        String borrowerName,
        String borrowerEmail,
        DocumentType documentType,
        String maskedDocumentNumber,
        String originalFileName,
        String contentType,
        long fileSize,
        KycStatus status,
        String rejectionReason,
        LocalDateTime uploadedAt,
        LocalDateTime verifiedAt
) {
}