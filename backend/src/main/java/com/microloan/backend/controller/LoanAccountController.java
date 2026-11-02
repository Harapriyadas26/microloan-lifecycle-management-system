package com.microloan.backend.controller;

import com.microloan.backend.dto.*;
import com.microloan.backend.service.RepaymentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoanAccountController {
  private final RepaymentService service;
  public LoanAccountController(RepaymentService service){this.service=service;}

  @GetMapping("/api/borrower/loans/{id}/account")
  @PreAuthorize("hasRole('BORROWER')")
  public LoanAccountResponse borrower(@PathVariable Long id,Authentication auth){
    return service.details(id,auth.getName());
  }
  @GetMapping("/api/officer/loans/{id}/account")
  @PreAuthorize("hasAnyRole('LOAN_OFFICER','ADMIN')")
  public LoanAccountResponse officer(@PathVariable Long id){return service.details(id,null);}
  @PostMapping("/api/officer/loans/{id}/disbursement")
  @PreAuthorize("hasRole('ADMIN')")
  public LoanAccountResponse disburse(@PathVariable Long id,@Valid @RequestBody DisbursementRequest request,
      Authentication auth){return service.disburse(id,auth.getName(),request);}
  @PostMapping("/api/officer/loans/{id}/payments")
  @PreAuthorize("hasAnyRole('LOAN_OFFICER','ADMIN')")
  public LoanAccountResponse payment(@PathVariable Long id,@Valid @RequestBody PaymentRequest request,
      Authentication auth){return service.recordPayment(id,auth.getName(),request);}
  @GetMapping("/api/officer/reports/portfolio")
  @PreAuthorize("hasAnyRole('LOAN_OFFICER','ADMIN')")
  public PortfolioReportResponse portfolio(){return service.portfolio();}
}
