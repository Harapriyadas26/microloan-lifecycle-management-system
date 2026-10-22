package com.microloan.backend.repository;

import com.microloan.backend.entity.RiskAssessment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskAssessmentRepository
  extends JpaRepository<RiskAssessment, Long>
{
  Optional<RiskAssessment> findByApplicationId(Long applicationId);
}
