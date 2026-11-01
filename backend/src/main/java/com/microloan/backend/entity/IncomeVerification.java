package com.microloan.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "income_verifications")
public class IncomeVerification {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
  @OneToOne(optional = false) @JoinColumn(name = "application_id", unique = true, nullable = false) public LoanApplication application;
  @Column(nullable = false, precision = 12, scale = 2) public BigDecimal declaredIncome;
  @Column(nullable = false, precision = 12, scale = 2) public BigDecimal observedIncome;
  @Column(nullable = false, length = 30) public String status;
  @Column(nullable = false, length = 40) public String scenario;
  @Column(nullable = false) public LocalDateTime checkedAt;
}
