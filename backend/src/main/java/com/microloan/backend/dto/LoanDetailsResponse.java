package com.microloan.backend.dto;

import java.util.List;

public record LoanDetailsResponse(
  LoanApplicationResponse application,
  BorrowerProfileResponse profile,
  List<KycDocumentResponse> kycDocuments,
  RiskAssessmentResponse risk,
  LoanDecisionResponse decision
) {}
