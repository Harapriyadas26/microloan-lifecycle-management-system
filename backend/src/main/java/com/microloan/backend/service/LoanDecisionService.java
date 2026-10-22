package com.microloan.backend.service;

import com.microloan.backend.dto.*;
import com.microloan.backend.entity.*;
import com.microloan.backend.repository.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LoanDecisionService {

  private final LoanApplicationRepository loans;
  private final LoanDecisionRepository decisions;
  private final UserRepository users;
  private final BorrowerProfileService profiles;
  private final KycService kyc;
  private final RiskEvaluationService risks;

  public LoanDecisionService(
    LoanApplicationRepository l,
    LoanDecisionRepository d,
    UserRepository u,
    BorrowerProfileService p,
    KycService k,
    RiskEvaluationService r
  ) {
    loans = l;
    decisions = d;
    users = u;
    profiles = p;
    kyc = k;
    risks = r;
  }

  @Transactional(readOnly = true)
  public List<LoanApplicationResponse> list(LoanStatus status) {
    return (
      status == null
        ? loans.findAllByOrderBySubmittedAtDesc()
        : loans.findByStatusOrderBySubmittedAtAsc(status)
    )
      .stream()
      .map(LoanApplicationService::toResponse)
      .toList();
  }

  @Transactional(readOnly = true)
  public LoanDetailsResponse details(Long id, String borrowerEmail) {
    LoanApplication a = (
      borrowerEmail == null
        ? loans.findById(id)
        : loans.findByIdAndBorrowerEmailIgnoreCase(id, borrowerEmail)
    ).orElseThrow(() ->
      error(HttpStatus.NOT_FOUND, "Loan application not found")
    );
    String email = a.getBorrower().getEmail();
    return new LoanDetailsResponse(
      LoanApplicationService.toResponse(a),
      profiles.getProfile(email),
      kyc.getMyDocuments(email),
      risks.get(id),
      decisions.findByApplicationId(id).map(this::toResponse).orElse(null)
    );
  }

  @Transactional
  public LoanApplicationResponse review(Long id) {
    LoanApplication a = locked(id);
    if (a.getStatus() != LoanStatus.SUBMITTED) throw error(
      HttpStatus.CONFLICT,
      "Only submitted applications may enter review"
    );
    a.setStatus(LoanStatus.UNDER_REVIEW);
    return LoanApplicationService.toResponse(loans.saveAndFlush(a));
  }

  @Transactional
  public RiskAssessmentResponse reevaluate(Long id) {
    LoanApplication a = locked(id);
    requireActive(a);
    return risks.evaluate(a);
  }

  @Transactional
  public LoanDecisionResponse decide(
    Long id,
    String officerEmail,
    LoanDecisionRequest request
  ) {
    LoanApplication a = locked(id);
    if (
      a.getStatus() != LoanStatus.UNDER_REVIEW ||
      decisions.findByApplicationId(id).isPresent()
    ) throw error(
      HttpStatus.CONFLICT,
      "Only applications under review without a final decision may be decided"
    );
    if (
      request.reason() == null || request.reason().trim().length() < 5
    ) throw error(
      HttpStatus.BAD_REQUEST,
      "Provide a meaningful decision reason"
    );
    profiles.getProfile(a.getBorrower().getEmail());
    if (risks.get(id) == null) throw error(
      HttpStatus.CONFLICT,
      "A risk assessment is required"
    );
    // Refresh mutable profile/KYC inputs at the final decision, under the borrower's lock.
    risks.evaluate(a);
    if (request.decision() == DecisionType.APPROVED) {
      if (
        kyc
          .getMyDocuments(a.getBorrower().getEmail())
          .stream()
          .noneMatch(d -> d.status() == KycStatus.VERIFIED)
      ) throw error(
        HttpStatus.CONFLICT,
        "Approval requires a verified KYC document"
      );
      if (
        request.approvedAmount() == null ||
        request.approvedAmount().signum() <= 0 ||
        request.approvedAmount().compareTo(a.getLoanAmount()) > 0
      ) throw error(
        HttpStatus.BAD_REQUEST,
        "Approved amount must be positive and cannot exceed requested amount"
      );
      if (request.annualInterestRate() == null) throw error(
        HttpStatus.BAD_REQUEST,
        "Annual interest rate is required for approval"
      );
    }
    User officer = users
      .findByEmailIgnoreCase(officerEmail)
      .orElseThrow(() -> error(HttpStatus.UNAUTHORIZED, "Officer not found"));
    if (
      officer.getRole() != Role.LOAN_OFFICER && officer.getRole() != Role.ADMIN
    ) throw error(HttpStatus.FORBIDDEN, "Officer access required");
    LoanDecision d = new LoanDecision();
    d.setApplication(a);
    d.setDecision(request.decision());
    d.setReason(request.reason().trim());
    d.setApprovedAmount(
      request.decision() == DecisionType.APPROVED
        ? request.approvedAmount()
        : null
    );
    d.setAnnualInterestRate(
      request.decision() == DecisionType.APPROVED
        ? request.annualInterestRate()
        : null
    );
    d.setOfficer(officer);
    d.setDecidedAt(LocalDateTime.now());
    a.setStatus(
      request.decision() == DecisionType.APPROVED
        ? LoanStatus.APPROVED
        : LoanStatus.REJECTED
    );
    loans.saveAndFlush(a);
    return toResponse(decisions.saveAndFlush(d));
  }

  @jakarta.persistence.PersistenceContext
  private jakarta.persistence.EntityManager entityManager;

  private LoanApplication locked(Long id) {
    // Always acquire user then application locks, shared with profile, KYC and borrower writes.
    LoanApplication found = loans
      .findById(id)
      .orElseThrow(() ->
        error(HttpStatus.NOT_FOUND, "Loan application not found")
      );
    users.findLockedByEmail(found.getBorrower().getEmail()).orElseThrow();
    LoanApplication locked = loans.findLockedById(id).orElseThrow();
    entityManager.refresh(locked);
    return locked;
  }

  private void requireActive(LoanApplication a) {
    if (
      a.getStatus() != LoanStatus.SUBMITTED &&
      a.getStatus() != LoanStatus.UNDER_REVIEW
    ) throw error(
      HttpStatus.CONFLICT,
      "Final or cancelled applications cannot be reassessed"
    );
  }

  private LoanDecisionResponse toResponse(LoanDecision d) {
    return new LoanDecisionResponse(
      d.getId(),
      d.getDecision(),
      d.getReason(),
      d.getApprovedAmount(),
      d.getAnnualInterestRate(),
      d.getOfficer().getId(),
      d.getOfficer().getFullName(),
      d.getDecidedAt()
    );
  }

  private ResponseStatusException error(HttpStatus status, String message) {
    return new ResponseStatusException(status, message);
  }
}
