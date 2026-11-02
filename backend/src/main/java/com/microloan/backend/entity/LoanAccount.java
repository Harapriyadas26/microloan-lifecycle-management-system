package com.microloan.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="loan_accounts")
public class LoanAccount {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @OneToOne(optional=false) @JoinColumn(name="application_id",nullable=false,unique=true) public LoanApplication application;
  @Column(nullable=false,precision=12,scale=2) public BigDecimal principal;
  @Column(nullable=false,precision=5,scale=2) public BigDecimal annualRate;
  @Column(nullable=false,length=40,unique=true) public String disbursementReference;
  @Column(nullable=false) public LocalDateTime disbursedAt;
  @Column(nullable=false,length=20) public String status="ACTIVE";
  public LocalDateTime closedAt;
  @ManyToOne(optional=false) @JoinColumn(name="disbursed_by") public User disbursedBy;
}
