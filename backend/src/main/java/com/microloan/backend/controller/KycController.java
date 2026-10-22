package com.microloan.backend.controller;

import com.microloan.backend.dto.KycDocumentResponse;
import com.microloan.backend.entity.DocumentType;
import com.microloan.backend.service.KycService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/borrower/kyc")
@PreAuthorize("hasRole('BORROWER')")
public class KycController {

    private final KycService kycService;

    public KycController(KycService kycService) {
        this.kycService = kycService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<KycDocumentResponse> uploadDocument(
            @RequestParam DocumentType documentType,
            @RequestParam String documentNumber,
            @RequestParam MultipartFile file,
            Authentication authentication
    ) {
        KycDocumentResponse response = kycService.uploadDocument(
                authentication.getName(),
                documentType,
                documentNumber,
                file
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<KycDocumentResponse>> getMyDocuments(
            Authentication authentication
    ) {
        List<KycDocumentResponse> documents =
                kycService.getMyDocuments(authentication.getName());

        return ResponseEntity.ok(documents);
    }
}