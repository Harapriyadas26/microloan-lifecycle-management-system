package com.microloan.backend.repository;

import com.microloan.backend.entity.IncomeVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncomeVerificationRepository extends JpaRepository<IncomeVerification, Long> {
  Optional<IncomeVerification> findByApplicationId(Long applicationId);
}
