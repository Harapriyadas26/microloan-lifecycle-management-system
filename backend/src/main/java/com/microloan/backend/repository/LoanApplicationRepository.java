package com.microloan.backend.repository;

import com.microloan.backend.entity.LoanApplication;
import com.microloan.backend.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LoanApplicationRepository
        extends JpaRepository<LoanApplication, Long> {

    List<LoanApplication>
    findByBorrowerEmailIgnoreCaseOrderBySubmittedAtDesc(String email);

    Optional<LoanApplication>
    findByIdAndBorrowerEmailIgnoreCase(Long id, String email);

    List<LoanApplication>
    findByStatusOrderBySubmittedAtAsc(LoanStatus status);

    boolean existsByBorrowerEmailIgnoreCaseAndStatusIn(
            String email,
            Collection<LoanStatus> statuses
    );
}