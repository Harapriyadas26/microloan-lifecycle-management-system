package com.microloan.backend.repository;
import com.microloan.backend.entity.LoanAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface LoanAccountRepository extends JpaRepository<LoanAccount,Long> {
  Optional<LoanAccount> findByApplicationId(Long id);
  boolean existsByDisbursementReference(String reference);
}
