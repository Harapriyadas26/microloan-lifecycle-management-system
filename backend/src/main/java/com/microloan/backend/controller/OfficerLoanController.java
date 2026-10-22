package com.microloan.backend.controller;

import com.microloan.backend.dto.*;
import com.microloan.backend.entity.LoanStatus;
import com.microloan.backend.service.LoanDecisionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@io.swagger.v3.oas.annotations.security.SecurityRequirement(name="bearerAuth")
@RestController
@RequestMapping("/api/officer/loans")
@PreAuthorize("hasAnyRole('LOAN_OFFICER','ADMIN')")
public class OfficerLoanController {

  private final LoanDecisionService loans;

  public OfficerLoanController(LoanDecisionService l) {
    loans = l;
  }

  @io.swagger.v3.oas.annotations.Operation(summary = "list")
  @GetMapping
  public List<LoanApplicationResponse> list(
    @RequestParam(required = false) LoanStatus status
  ) {
    return loans.list(status);
  }

  @io.swagger.v3.oas.annotations.Operation(summary = "detail")
  @GetMapping("/{id}")
  public LoanDetailsResponse detail(@PathVariable Long id) {
    return loans.details(id, null);
  }

  @io.swagger.v3.oas.annotations.Operation(summary = "review")
  @PatchMapping("/{id}/review")
  public LoanApplicationResponse review(@PathVariable Long id) {
    return loans.review(id);
  }

  @io.swagger.v3.oas.annotations.Operation(summary = "risk")
  @PostMapping("/{id}/risk")
  public RiskAssessmentResponse risk(@PathVariable Long id) {
    return loans.reevaluate(id);
  }

  @io.swagger.v3.oas.annotations.Operation(summary = "decide")
  @PostMapping("/{id}/decision")
  public LoanDecisionResponse decide(
    @PathVariable Long id,
    @Valid @RequestBody LoanDecisionRequest request,
    Authentication auth
  ) {
    return loans.decide(id, auth.getName(), request);
  }
}
