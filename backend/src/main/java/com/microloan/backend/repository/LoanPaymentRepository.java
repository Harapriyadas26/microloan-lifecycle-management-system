package com.microloan.backend.repository;
import com.microloan.backend.entity.LoanPayment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface LoanPaymentRepository extends JpaRepository<LoanPayment,Long> {
  boolean existsByReference(String reference);
  List<LoanPayment> findByAccountIdOrderByRecordedAtAsc(Long accountId);
}
