package com.microloan.repository;

import com.microloan.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByTransactionReference(String transactionReference);
    Optional<Payment> findByTransactionReference(String transactionReference);
    List<Payment> findByScheduleIdIn(List<Long> scheduleIds);
}
