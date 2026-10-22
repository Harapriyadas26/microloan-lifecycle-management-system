package com.microloan.backend.controller;

import com.microloan.backend.dto.LoanApplicationRequest;
import com.microloan.backend.dto.LoanApplicationResponse;
import com.microloan.backend.service.LoanApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@io.swagger.v3.oas.annotations.security.SecurityRequirement(name="bearerAuth")
@RestController
@RequestMapping("/api/borrower/loans")
@PreAuthorize("hasRole('BORROWER')")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    public LoanApplicationController(
            LoanApplicationService loanApplicationService
    ) {
        this.loanApplicationService = loanApplicationService;
    }

    @io.swagger.v3.oas.annotations.Operation(summary="submit Application")
 @PostMapping
    public ResponseEntity<LoanApplicationResponse> submitApplication(
            @Valid @RequestBody LoanApplicationRequest request,
            Authentication authentication
    ) {
        LoanApplicationResponse response =
                loanApplicationService.submitApplication(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @io.swagger.v3.oas.annotations.Operation(summary="get My Applications")
 @GetMapping
    public ResponseEntity<List<LoanApplicationResponse>>
    getMyApplications(Authentication authentication) {

        return ResponseEntity.ok(
                loanApplicationService.getMyApplications(
                        authentication.getName()
                )
        );
    }

    @io.swagger.v3.oas.annotations.Operation(summary="get My Application")
 @GetMapping("/{applicationId}")
    public ResponseEntity<LoanApplicationResponse> getMyApplication(
            @PathVariable Long applicationId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                loanApplicationService.getMyApplication(
                        authentication.getName(),
                        applicationId
                )
        );
    }

    @io.swagger.v3.oas.annotations.Operation(summary="cancel Application")
 @PatchMapping("/{applicationId}/cancel")
    public ResponseEntity<LoanApplicationResponse> cancelApplication(
            @PathVariable Long applicationId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                loanApplicationService.cancelApplication(
                        authentication.getName(),
                        applicationId
                )
        );
    }
}