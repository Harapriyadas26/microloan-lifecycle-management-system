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

@io.swagger.v3.oas.annotations.security.SecurityRequirement(name="bearerAuth")
@RestController
@RequestMapping("/api/borrower/kyc")
@PreAuthorize("hasRole('BORROWER')")
public class KycController {

    private final KycService kycService;

    public KycController(KycService kycService) {
        this.kycService = kycService;
    }

    @io.swagger.v3.oas.annotations.Operation(summary="upload Document")
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

    @io.swagger.v3.oas.annotations.Operation(summary="get My Documents")
 @GetMapping
    public ResponseEntity<List<KycDocumentResponse>> getMyDocuments(
            Authentication authentication
    ) {
        List<KycDocumentResponse> documents =
                kycService.getMyDocuments(authentication.getName());

        return ResponseEntity.ok(documents);
    }

    @io.swagger.v3.oas.annotations.Operation(summary="file")
 @GetMapping("/{id}/file")
    public ResponseEntity<org.springframework.core.io.Resource> file(@PathVariable Long id,
        @RequestParam(defaultValue="false") boolean download,Authentication auth) {
        return kycService.file(id,auth.getName(),download);
    }
}