package com.microloan.controller;

import com.microloan.dto.*;
import com.microloan.model.Payment;
import com.microloan.service.LoanModuleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:5173")
public class PaymentController {
    private final LoanModuleService service;

    public PaymentController(LoanModuleService service) {
        this.service = service;
    }

    @PostMapping
    public Payment record(@Valid @RequestBody PaymentRequest request) {
        return service.recordPayment(request);
    }

    @PostMapping("/reconcile")
    public Payment reconcile(@Valid @RequestBody ReconcilePaymentRequest request) {
        return service.reconcilePayment(request);
    }

    @GetMapping
    public List<Payment> all() {
        return service.getAllPayments();
    }

    @GetMapping("/loan/{loanId}")
    public List<Payment> byLoan(@PathVariable Long loanId) {
        return service.getPaymentsForLoan(loanId);
    }
}
