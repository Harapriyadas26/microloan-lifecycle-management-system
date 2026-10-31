package com.microloan.repository;

import com.microloan.model.RepaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, Long> {
    List<RepaymentSchedule> findByLoanIdOrderByInstalmentNumber(Long loanId);
}
