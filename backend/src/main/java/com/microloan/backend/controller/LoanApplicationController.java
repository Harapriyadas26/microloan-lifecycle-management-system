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

    @GetMapping
    public ResponseEntity<List<LoanApplicationResponse>>
    getMyApplications(Authentication authentication) {

        return ResponseEntity.ok(
                loanApplicationService.getMyApplications(
                        authentication.getName()
                )
        );
    }

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