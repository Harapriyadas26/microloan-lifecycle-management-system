package com.microloan.controller;

import com.microloan.model.RepaymentSchedule;
import com.microloan.service.LoanModuleService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "http://localhost:5173")
public class RepaymentController {
    private final LoanModuleService service;

    public RepaymentController(LoanModuleService service) {
        this.service = service;
    }

    @PostMapping("/{loanId}/schedule")
    public List<RepaymentSchedule> generate(@PathVariable Long loanId) {
        return service.generateSchedule(loanId);
    }

    @GetMapping("/{loanId}/schedule")
    public List<RepaymentSchedule> get(@PathVariable Long loanId) {
        return service.getSchedule(loanId);
    }
}
