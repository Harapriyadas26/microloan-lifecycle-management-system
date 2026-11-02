package com.microloan.backend.service;

import com.microloan.backend.dto.*;
import com.microloan.backend.entity.*;
import com.microloan.backend.repository.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RepaymentService {
  private final LoanApplicationRepository applications;
  private final LoanDecisionRepository decisions;
  private final LoanAccountRepository accounts;
  private final InstallmentRepository installments;
  private final LoanPaymentRepository payments;
  private final UserRepository users;
  @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
  public RepaymentService(LoanApplicationRepository applications,LoanDecisionRepository decisions,
      LoanAccountRepository accounts,InstallmentRepository installments,LoanPaymentRepository payments,UserRepository users) {
    this.applications=applications;this.decisions=decisions;this.accounts=accounts;
    this.installments=installments;this.payments=payments;this.users=users;
  }
  private ResponseStatusException conflict(String message) { return new ResponseStatusException(HttpStatus.CONFLICT,message); }
  private LoanApplication locked(Long id) {
    LoanApplication found=applications.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Application not found"));
    users.findLockedByEmail(found.getBorrower().getEmail()).orElseThrow();
    LoanApplication application=applications.findLockedById(id).orElseThrow();
    entityManager.refresh(application);
    return application;
  }
  @Transactional
  public LoanAccountResponse disburse(Long id,String actor,DisbursementRequest request) {
    LoanApplication application=locked(id);
    if(application.getStatus()!=LoanStatus.APPROVED) throw conflict("Only approved loans may be disbursed");
    if(accounts.findByApplicationId(id).isPresent()) throw conflict("Loan already disbursed");
    String reference=request.reference().trim().toUpperCase(Locale.ROOT);
    if(accounts.existsByDisbursementReference(reference)) throw conflict("Disbursement reference already used");
    LoanDecision decision=decisions.findByApplicationId(id).orElseThrow(()->conflict("Final approval required"));
    if(decision.getDecision()!=DecisionType.APPROVED) throw conflict("Final approval required");
    LoanAccount account=new LoanAccount();account.application=application;
    account.principal=decision.getApprovedAmount();account.annualRate=decision.getAnnualInterestRate();
    account.disbursementReference=reference;account.disbursedAt=LocalDateTime.now();
    account.disbursedBy=users.findByEmailIgnoreCase(actor).orElseThrow();
    try { accounts.saveAndFlush(account); }
    catch(DataIntegrityViolationException ex) { throw conflict("Disbursement or reference already recorded"); }
    generateSchedule(account,application.getTenureMonths());
    return response(account);
  }
  private void generateSchedule(LoanAccount account,int months) {
    BigDecimal balance=account.principal;
    BigDecimal monthlyRate=account.annualRate.divide(new BigDecimal("1200"),12,RoundingMode.HALF_UP);
    BigDecimal emi;
    if(monthlyRate.signum()==0) emi=balance.divide(BigDecimal.valueOf(months),2,RoundingMode.HALF_UP);
    else {
      BigDecimal factor=BigDecimal.ONE.add(monthlyRate).pow(months,MathContext.DECIMAL128);
      emi=balance.multiply(monthlyRate).multiply(factor)
        .divide(factor.subtract(BigDecimal.ONE),2,RoundingMode.HALF_UP);
    }
    for(int number=1;number<=months;number++) {
      BigDecimal interest=balance.multiply(monthlyRate).setScale(2,RoundingMode.HALF_UP);
      BigDecimal principal=number==months?balance:emi.subtract(interest).min(balance);
      if(principal.signum()<=0) throw conflict("Rate and tenure do not amortize the loan");
      Installment row=new Installment();row.account=account;row.number=number;
      row.dueDate=account.disbursedAt.toLocalDate().plusMonths(number);
      row.principalDue=principal;row.interestDue=interest;
      installments.save(row);balance=balance.subtract(principal);
    }
  }
  @Transactional
  public LoanAccountResponse recordPayment(Long id,String actor,PaymentRequest request) {
    LoanApplication application=locked(id);
    LoanAccount account=accounts.findByApplicationId(application.getId()).orElseThrow(()->conflict("Disburse before recording payments"));
    if(!"ACTIVE".equals(account.status)) throw conflict("Loan already closed");
    String reference=request.reference().trim().toUpperCase(Locale.ROOT);
    if(payments.existsByReference(reference)) throw conflict("Payment reference already used");
    List<Installment> rows=installments.findByAccountIdOrderByNumberAsc(account.id);
    accruePenalties(rows,LocalDate.now());
    BigDecimal outstanding=outstanding(rows);
    if(request.amount().compareTo(outstanding)>0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Payment exceeds outstanding balance");
    BigDecimal remaining=request.amount();
    for(Installment row:rows) {
      BigDecimal due=due(row);
      BigDecimal applied=remaining.min(due);
      row.amountPaid=row.amountPaid.add(applied);
      remaining=remaining.subtract(applied);
      if(remaining.signum()==0) break;
    }
    installments.saveAll(rows);
    LoanPayment payment=new LoanPayment();payment.account=account;payment.reference=reference;
    payment.amount=request.amount();payment.recordedAt=LocalDateTime.now();
    payment.recordedBy=users.findByEmailIgnoreCase(actor).orElseThrow();
    try { payments.saveAndFlush(payment); }
    catch(DataIntegrityViolationException ex) { throw conflict("Payment reference already used"); }
    if(outstanding(rows).signum()==0){account.status="CLOSED";account.closedAt=LocalDateTime.now();accounts.save(account);}
    return response(account);
  }
  // One-time academic demo charge: 1% of the unpaid installment after seven full grace days.
  private void accruePenalties(List<Installment> rows,LocalDate today) {
    for(Installment row:rows) if(row.penaltyDue.signum()==0 && row.dueDate.plusDays(7).isBefore(today)
        && due(row).signum()>0) {
      row.penaltyDue=row.principalDue.add(row.interestDue).subtract(row.amountPaid)
        .multiply(new BigDecimal("0.01")).setScale(2,RoundingMode.HALF_UP);
    }
  }
  private BigDecimal due(Installment row) {
    return row.principalDue.add(row.interestDue).add(row.penaltyDue).subtract(row.amountPaid);
  }
  private BigDecimal dueIncludingPendingPenalty(Installment row,LocalDate today) {
    BigDecimal unpaid=due(row);
    if(row.penaltyDue.signum()==0 && row.dueDate.plusDays(7).isBefore(today) && unpaid.signum()>0)
      return unpaid.add(unpaid.multiply(new BigDecimal("0.01")).setScale(2,RoundingMode.HALF_UP));
    return unpaid;
  }
  private BigDecimal outstanding(List<Installment> rows) {
    return rows.stream().map(this::due).reduce(BigDecimal.ZERO,BigDecimal::add);
  }
  @Transactional
  public LoanAccountResponse details(Long id,String borrowerEmail) {
    if(borrowerEmail!=null) applications.findByIdAndBorrowerEmailIgnoreCase(id,borrowerEmail).orElseThrow(
      ()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Application not found"));
    LoanApplication application=locked(id);
    LoanAccount account=accounts.findByApplicationId(application.getId()).orElseThrow(
      ()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Loan has not been disbursed"));
    if("ACTIVE".equals(account.status)) {
      List<Installment> rows=installments.findByAccountIdOrderByNumberAsc(account.id);
      accruePenalties(rows,LocalDate.now());installments.saveAll(rows);
    }
    return response(account);
  }
  private LoanAccountResponse response(LoanAccount account) {
    List<Installment> rows=installments.findByAccountIdOrderByNumberAsc(account.id);
    LocalDate today=LocalDate.now();
    var schedule=rows.stream().map(row->new LoanAccountResponse.InstallmentView(row.number,row.dueDate,
      row.principalDue,row.interestDue,row.penaltyDue,row.amountPaid,due(row),
      due(row).signum()==0?"PAID":row.dueDate.isBefore(today)?"OVERDUE":"DUE")).toList();
    var history=payments.findByAccountIdOrderByRecordedAtAsc(account.id).stream()
      .map(p->new LoanAccountResponse.PaymentView(p.reference,p.amount,p.recordedAt,p.recordedBy.getFullName())).toList();
    return new LoanAccountResponse(account.application.getId(),account.status,account.principal,account.annualRate,
      account.disbursementReference,account.disbursedAt,account.closedAt,outstanding(rows),schedule,history);
  }
  @Transactional(readOnly=true)
  public PortfolioReportResponse portfolio() {
    int active=0,closed=0,overdue=0,soon=0;
    BigDecimal disbursed=BigDecimal.ZERO,balance=BigDecimal.ZERO;
    LocalDate today=LocalDate.now();
    for(LoanAccount account:accounts.findAll()) {
      if("ACTIVE".equals(account.status)) active++;else closed++;
      disbursed=disbursed.add(account.principal);
      List<Installment> rows=installments.findByAccountIdOrderByNumberAsc(account.id);
      for(Installment row:rows) {
        BigDecimal unpaid="ACTIVE".equals(account.status)?dueIncludingPendingPenalty(row,today):due(row);
        balance=balance.add(unpaid);
        if(unpaid.signum()<=0) continue;
        if(row.dueDate.isBefore(today)) overdue++;
        else if(!row.dueDate.isAfter(today.plusDays(7))) soon++;
      }
    }
    return new PortfolioReportResponse(active,closed,overdue,soon,disbursed,balance);
  }
}
