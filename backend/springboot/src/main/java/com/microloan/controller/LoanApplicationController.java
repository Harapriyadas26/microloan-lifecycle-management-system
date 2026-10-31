package com.microloan.controller;

import com.microloan.dto.LoanApplicationRequest;
import com.microloan.model.LoanApplication;
import com.microloan.service.LoanModuleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "http://localhost:5173")
public class LoanApplicationController {
    private final LoanModuleService service;

    public LoanApplicationController(LoanModuleService service) {
        this.service = service;
    }

    @PostMapping
    public LoanApplication submit(@Valid @RequestBody LoanApplicationRequest request) {
        return service.submitApplication(request);
    }

    @GetMapping("/{applicationId}")
    public LoanApplication get(@PathVariable Long applicationId) {
        return service.getApplication(applicationId);
    }

    @GetMapping("/borrower/{borrowerId}")
    public List<LoanApplication> byBorrower(@PathVariable Long borrowerId) {
        return service.getBorrowerApplications(borrowerId);
    }
}
