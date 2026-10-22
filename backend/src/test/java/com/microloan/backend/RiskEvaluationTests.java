package com.microloan.backend;

import static org.junit.jupiter.api.Assertions.*;

import com.microloan.backend.entity.*;
import com.microloan.backend.service.RiskEvaluationService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class RiskEvaluationTests {

  final RiskEvaluationService risk = new RiskEvaluationService(
    null,
    null,
    null
  );

  com.microloan.backend.dto.RiskAssessmentResponse calc(
    String income,
    String amount,
    int tenure,
    String debt,
    EmploymentType employment,
    KycStatus status
  ) {
    return risk.calculate(
      new BigDecimal(income),
      employment,
      new BigDecimal(amount),
      tenure,
      new BigDecimal(debt),
      List.of(status)
    );
  }

  @Test
  void dtiBoundaries() {
    assertEquals(
      100,
      calc(
        "10000",
        "1200",
        12,
        "2900",
        EmploymentType.SALARIED,
        KycStatus.VERIFIED
      ).score()
    );
    assertEquals(
      85,
      calc(
        "10000",
        "1200",
        12,
        "2900.01",
        EmploymentType.SALARIED,
        KycStatus.VERIFIED
      ).score()
    );
    assertEquals(
      85,
      calc(
        "10000",
        "1200",
        12,
        "3900",
        EmploymentType.SALARIED,
        KycStatus.VERIFIED
      ).score()
    );
    assertEquals(
      70,
      calc(
        "10000",
        "1200",
        12,
        "3900.01",
        EmploymentType.SALARIED,
        KycStatus.VERIFIED
      ).score()
    );
    assertEquals(
      70,
      calc(
        "10000",
        "1200",
        12,
        "4900",
        EmploymentType.SALARIED,
        KycStatus.VERIFIED
      ).score()
    );
    assertEquals(
      50,
      calc(
        "10000",
        "1200",
        12,
        "4900.01",
        EmploymentType.SALARIED,
        KycStatus.VERIFIED
      ).score()
    );
  }

  @Test
  void ratioBoundaries() {
    assertEquals(
      100,
      calc(
        "10000",
        "60000",
        60,
        "0",
        EmploymentType.SALARIED,
        KycStatus.VERIFIED
      ).score()
    );
    assertEquals(
      90,
      calc(
        "10000",
        "60000.01",
        60,
        "0",
        EmploymentType.SALARIED,
        KycStatus.VERIFIED
      ).score()
    );
    assertEquals(
      90,
      calc(
        "10000",
        "120000",
        60,
        "0",
        EmploymentType.SALARIED,
        KycStatus.VERIFIED
      ).score()
    );
    assertEquals(
      80,
      calc(
        "10000",
        "120000.01",
        60,
        "0",
        EmploymentType.SALARIED,
        KycStatus.VERIFIED
      ).score()
    );
    assertEquals(
      65,
      calc(
        "10000",
        "240000",
        60,
        "0",
        EmploymentType.SALARIED,
        KycStatus.VERIFIED
      ).score()
    );
    assertEquals(
      50,
      calc(
        "10000",
        "240000.01",
        60,
        "0",
        EmploymentType.SALARIED,
        KycStatus.VERIFIED
      ).score()
    );
  }

  @Test
  void kycAndEmploymentRecommendations() {
    assertEquals(
      RiskRecommendation.MANUAL_REVIEW,
      calc(
        "45000",
        "100000",
        24,
        "3000",
        EmploymentType.SALARIED,
        KycStatus.PENDING
      ).recommendation()
    );
    assertEquals(
      RiskRecommendation.MANUAL_REVIEW,
      calc(
        "45000",
        "100000",
        24,
        "3000",
        EmploymentType.SALARIED,
        KycStatus.REJECTED
      ).recommendation()
    );
    var low = calc(
      "10000",
      "120000",
      60,
      "0",
      EmploymentType.DAILY_WAGE,
      KycStatus.VERIFIED
    );
    assertEquals(75, low.score());
    assertEquals(RiskLevel.LOW, low.riskLevel());
    var medium = calc(
      "10000",
      "1200",
      12,
      "4900.01",
      EmploymentType.SALARIED,
      KycStatus.VERIFIED
    );
    assertEquals(RiskLevel.MEDIUM, medium.riskLevel());
    var high = calc(
      "1000",
      "500000",
      3,
      "1000",
      EmploymentType.UNEMPLOYED,
      KycStatus.VERIFIED
    );
    assertEquals(0, high.score());
    assertEquals(RiskRecommendation.REJECT, high.recommendation());
  }

  @Test
  void rejectsZeroAndInvalidInputs() {
    for (String income : List.of("0", "-1"))
      assertThrows(
        org.springframework.web.server.ResponseStatusException.class,
        () ->
          calc(
            income,
            "1000",
            3,
            "0",
            EmploymentType.SALARIED,
            KycStatus.VERIFIED
          )
      );
    assertThrows(
      org.springframework.web.server.ResponseStatusException.class,
      () ->
        calc(
          "1000",
          "1000",
          0,
          "0",
          EmploymentType.SALARIED,
          KycStatus.VERIFIED
        )
    );
  }

  @Test
  void classificationBoundaries() {
    assertEquals(RiskLevel.HIGH, RiskEvaluationService.classify(0));
    assertEquals(RiskLevel.HIGH, RiskEvaluationService.classify(49));
    assertEquals(RiskLevel.MEDIUM, RiskEvaluationService.classify(50));
    assertEquals(RiskLevel.MEDIUM, RiskEvaluationService.classify(74));
    assertEquals(RiskLevel.LOW, RiskEvaluationService.classify(75));
    assertEquals(RiskLevel.LOW, RiskEvaluationService.classify(100));
  }
}
