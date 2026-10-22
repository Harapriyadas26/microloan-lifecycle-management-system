package com.microloan.backend.service;

import com.microloan.backend.dto.LoanApplicationRequest;
import com.microloan.backend.dto.LoanApplicationResponse;
import com.microloan.backend.entity.*;
import com.microloan.backend.repository.BorrowerProfileRepository;
import com.microloan.backend.repository.KycDocumentRepository;
import com.microloan.backend.repository.LoanApplicationRepository;
import com.microloan.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;
    private final BorrowerProfileRepository borrowerProfileRepository;
    private final KycDocumentRepository kycDocumentRepository;

    public LoanApplicationService(
            LoanApplicationRepository loanApplicationRepository,
            UserRepository userRepository,
            BorrowerProfileRepository borrowerProfileRepository,
            KycDocumentRepository kycDocumentRepository
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.userRepository = userRepository;
        this.borrowerProfileRepository = borrowerProfileRepository;
        this.kycDocumentRepository = kycDocumentRepository;
    }

    @Transactional
    public LoanApplicationResponse submitApplication(
            String email,
            LoanApplicationRequest request
    ) {
        User borrower = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Borrower not found"
                ));

        if (borrowerProfileRepository
                .findByUserEmailIgnoreCase(email)
                .isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Complete your borrower profile before applying"
            );
        }

        boolean hasUsableKycDocument = kycDocumentRepository
                .findByUserEmailIgnoreCaseOrderByUploadedAtDesc(email)
                .stream()
                .anyMatch(document ->
                        document.getStatus() != KycStatus.REJECTED
                );

        if (!hasUsableKycDocument) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Submit at least one KYC document before applying"
            );
        }

        boolean hasActiveApplication = loanApplicationRepository
                .existsByBorrowerEmailIgnoreCaseAndStatusIn(
                        email,
                        List.of(
                                LoanStatus.SUBMITTED,
                                LoanStatus.UNDER_REVIEW
                        )
                );

        if (hasActiveApplication) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You already have an active loan application"
            );
        }

        if (request.purpose() == LoanPurpose.OTHER
                && (request.purposeDescription() == null
                || request.purposeDescription().isBlank())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Purpose description is required for OTHER purpose"
            );
        }

        LoanApplication application = new LoanApplication();
        application.setBorrower(borrower);
        application.setLoanAmount(request.loanAmount());
        application.setPurpose(request.purpose());
        application.setPurposeDescription(
                normalizeDescription(request.purposeDescription())
        );
        application.setTenureMonths(request.tenureMonths());
        application.setExistingMonthlyDebt(
                request.existingMonthlyDebt()
        );
        application.setStatus(LoanStatus.SUBMITTED);

        LoanApplication savedApplication =
                loanApplicationRepository.save(application);

        return toResponse(savedApplication);
    }

    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getMyApplications(String email) {
        return loanApplicationRepository
                .findByBorrowerEmailIgnoreCaseOrderBySubmittedAtDesc(email)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LoanApplicationResponse getMyApplication(
            String email,
            Long applicationId
    ) {
        LoanApplication application = loanApplicationRepository
                .findByIdAndBorrowerEmailIgnoreCase(
                        applicationId,
                        email
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Loan application not found"
                ));

        return toResponse(application);
    }

    @Transactional
    public LoanApplicationResponse cancelApplication(
            String email,
            Long applicationId
    ) {
        LoanApplication application = loanApplicationRepository
                .findByIdAndBorrowerEmailIgnoreCase(
                        applicationId,
                        email
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Loan application not found"
                ));

        if (application.getStatus() != LoanStatus.SUBMITTED
                && application.getStatus()
                != LoanStatus.UNDER_REVIEW) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This loan application cannot be cancelled"
            );
        }

        application.setStatus(LoanStatus.CANCELLED);

        return toResponse(
                loanApplicationRepository.save(application)
        );
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }

    private LoanApplicationResponse toResponse(
            LoanApplication application
    ) {
        User borrower = application.getBorrower();

        return new LoanApplicationResponse(
                application.getId(),
                borrower.getId(),
                borrower.getFullName(),
                borrower.getEmail(),
                application.getLoanAmount(),
                application.getPurpose(),
                application.getPurposeDescription(),
                application.getTenureMonths(),
                application.getExistingMonthlyDebt(),
                application.getStatus(),
                application.getSubmittedAt(),
                application.getUpdatedAt()
        );
    }
}