package com.microloan.backend.service;

import com.microloan.backend.dto.IncomeVerificationResponse;
import com.microloan.backend.entity.IncomeVerification;
import com.microloan.backend.entity.LoanApplication;
import com.microloan.backend.repository.BorrowerProfileRepository;
import com.microloan.backend.repository.IncomeVerificationRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class IncomeVerificationService {
  private final IncomeVerificationRepository records;
  private final BorrowerProfileRepository profiles;
  public IncomeVerificationService(IncomeVerificationRepository records, BorrowerProfileRepository profiles) {
    this.records = records; this.profiles = profiles;
  }
  public IncomeVerificationResponse get(Long applicationId) {
    return records.findByApplicationId(applicationId).map(this::response).orElse(null);
  }
  public IncomeVerificationResponse simulate(LoanApplication loan, String scenario) {
    if (!List.of("MATCH", "REVIEW", "CONFLICT").contains(scenario))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose MATCH, REVIEW or CONFLICT");
    BigDecimal declared = profiles.findByUserEmailIgnoreCase(loan.getBorrower().getEmail())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Profile required"))
      .getMonthlyIncome();
    // Six fictional monthly salary credits from a bank simulator, never a real bank connection.
    BigDecimal observed = median(credits(scenario));
    BigDecimal difference = declared.subtract(observed).abs().multiply(new BigDecimal("100"))
      .divide(declared, 2, RoundingMode.HALF_UP);
    String status = difference.compareTo(new BigDecimal("10")) <= 0 ? "VERIFIED"
      : difference.compareTo(new BigDecimal("25")) <= 0 ? "MANUAL_REVIEW" : "CONFLICT";
    IncomeVerification record = records.findByApplicationId(loan.getId()).orElseGet(IncomeVerification::new);
    record.application = loan; record.declaredIncome = declared; record.observedIncome = observed;
    record.status = status; record.scenario = scenario; record.checkedAt = LocalDateTime.now();
    return response(records.saveAndFlush(record));
  }
  private IncomeVerificationResponse response(IncomeVerification r) {
    return new IncomeVerificationResponse(r.declaredIncome, r.observedIncome, r.status, r.scenario,
      r.checkedAt, "FICTIONAL_BANK_SIMULATOR", credits(r.scenario));
  }
  private List<BigDecimal> credits(String scenario) {
    int base = switch (scenario) { case "MATCH" -> 45000; case "REVIEW" -> 37000; default -> 26000; };
    return List.of(base - 200, base + 200, base, base - 100, base + 100, base)
      .stream().map(BigDecimal::valueOf).toList();
  }
  private BigDecimal median(List<BigDecimal> values) {
    var sorted = values.stream().sorted().toList();
    return sorted.get(2).add(sorted.get(3)).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
  }
}
