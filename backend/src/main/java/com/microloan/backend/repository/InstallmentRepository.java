package com.microloan.backend.repository;
import com.microloan.backend.entity.Installment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface InstallmentRepository extends JpaRepository<Installment,Long> {
  List<Installment> findByAccountIdOrderByNumberAsc(Long accountId);
}
