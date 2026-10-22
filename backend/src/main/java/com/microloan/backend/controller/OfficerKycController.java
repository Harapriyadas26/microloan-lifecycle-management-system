package com.microloan.backend.controller;

import com.microloan.backend.dto.*;
import com.microloan.backend.entity.KycStatus;
import com.microloan.backend.service.KycService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@io.swagger.v3.oas.annotations.security.SecurityRequirement(name="bearerAuth")
@RestController
@RequestMapping("/api/officer/kyc")
@PreAuthorize("hasAnyRole('LOAN_OFFICER','ADMIN')")
public class OfficerKycController {

  private final KycService kyc;

  public OfficerKycController(KycService k) {
    kyc = k;
  }

  @io.swagger.v3.oas.annotations.Operation(summary = "list")
  @GetMapping
  public List<KycDocumentResponse> list(
    @RequestParam(required = false) KycStatus status
  ) {
    return kyc.listForOfficer(status);
  }

  @io.swagger.v3.oas.annotations.Operation(summary = "detail")
  @GetMapping("/{id}")
  public KycDocumentResponse detail(@PathVariable Long id) {
    return kyc.detail(id, null);
  }

  @io.swagger.v3.oas.annotations.Operation(summary = "file")
  @GetMapping("/{id}/file")
  public ResponseEntity<Resource> file(
    @PathVariable Long id,
    @RequestParam(defaultValue = "false") boolean download
  ) {
    return kyc.file(id, null, download);
  }

  @io.swagger.v3.oas.annotations.Operation(summary = "verify")
  @PatchMapping("/{id}/verify")
  public KycDocumentResponse verify(@PathVariable Long id) {
    return kyc.decide(id, true, null);
  }

  @io.swagger.v3.oas.annotations.Operation(summary = "reject")
  @PatchMapping("/{id}/reject")
  public KycDocumentResponse reject(
    @PathVariable Long id,
    @Valid @RequestBody ReasonRequest request
  ) {
    return kyc.decide(id, false, request.reason());
  }
}
