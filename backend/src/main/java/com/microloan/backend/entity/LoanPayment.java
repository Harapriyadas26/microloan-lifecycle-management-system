package com.microloan.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="loan_payments")
public class LoanPayment {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @ManyToOne(optional=false) @JoinColumn(name="account_id",nullable=false) public LoanAccount account;
  @Column(nullable=false,length=40,unique=true) public String reference;
  @Column(nullable=false,precision=12,scale=2) public BigDecimal amount;
  @Column(nullable=false) public LocalDateTime recordedAt;
  @ManyToOne(optional=false) @JoinColumn(name="recorded_by") public User recordedBy;
}
