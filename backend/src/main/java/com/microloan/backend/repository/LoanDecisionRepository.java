package com.microloan.backend.repository;

import com.microloan.backend.entity.LoanDecision;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanDecisionRepository
  extends JpaRepository<LoanDecision, Long>
{
  Optional<LoanDecision> findByApplicationId(Long applicationId);
}
