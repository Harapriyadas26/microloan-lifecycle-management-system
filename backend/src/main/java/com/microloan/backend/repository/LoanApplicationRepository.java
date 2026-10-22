package com.microloan.backend.repository;

import com.microloan.backend.entity.LoanApplication;
import com.microloan.backend.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LoanApplicationRepository
        extends JpaRepository<LoanApplication, Long> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select a from LoanApplication a where a.id = :id")
    Optional<LoanApplication> findLockedById(@org.springframework.data.repository.query.Param("id") Long id);

    List<LoanApplication> findAllByOrderBySubmittedAtDesc();

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
