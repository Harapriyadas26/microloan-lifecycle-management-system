package com.microloan.backend.controller;

import com.microloan.backend.dto.*;
import com.microloan.backend.service.LoanDecisionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@io.swagger.v3.oas.annotations.security.SecurityRequirement(name="bearerAuth")
@RestController
@RequestMapping("/api/borrower/loans")
@PreAuthorize("hasRole('BORROWER')")
public class BorrowerLoanDetailsController {

  private final LoanDecisionService loans;

  public BorrowerLoanDetailsController(LoanDecisionService l) {
    loans = l;
  }

  @io.swagger.v3.oas.annotations.Operation(summary = "details")
  @GetMapping("/{id}/details")
  public LoanDetailsResponse details(@PathVariable Long id, Authentication a) {
    return loans.details(id, a.getName());
  }

  @io.swagger.v3.oas.annotations.Operation(summary = "risk")
  @GetMapping("/{id}/risk")
  public RiskAssessmentResponse risk(@PathVariable Long id, Authentication a) {
    return loans.details(id, a.getName()).risk();
  }

  @io.swagger.v3.oas.annotations.Operation(summary = "decision")
  @GetMapping("/{id}/decision")
  public LoanDecisionResponse decision(
    @PathVariable Long id,
    Authentication a
  ) {
    return loans.details(id, a.getName()).decision();
  }
}
