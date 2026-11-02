package com.microloan.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name="loan_installments",uniqueConstraints=@UniqueConstraint(columnNames={"account_id","installment_number"}))
public class Installment {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @ManyToOne(optional=false) @JoinColumn(name="account_id",nullable=false) public LoanAccount account;
  @Column(name="installment_number",nullable=false) public int number;
  @Column(nullable=false) public LocalDate dueDate;
  @Column(nullable=false,precision=12,scale=2) public BigDecimal principalDue;
  @Column(nullable=false,precision=12,scale=2) public BigDecimal interestDue;
  @Column(nullable=false,precision=12,scale=2) public BigDecimal amountPaid=BigDecimal.ZERO;
  @Column(nullable=false,precision=12,scale=2) public BigDecimal penaltyDue=BigDecimal.ZERO;
}
