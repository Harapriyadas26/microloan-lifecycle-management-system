package com.microloan.repository;

import com.microloan.model.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByBorrowerIdOrderByAppliedOnDesc(Long borrowerId);
}
