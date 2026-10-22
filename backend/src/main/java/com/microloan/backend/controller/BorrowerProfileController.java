package com.microloan.backend.controller;

import com.microloan.backend.dto.BorrowerProfileRequest;
import com.microloan.backend.dto.BorrowerProfileResponse;
import com.microloan.backend.service.BorrowerProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@io.swagger.v3.oas.annotations.security.SecurityRequirement(name="bearerAuth")
@RestController
@RequestMapping("/api/borrower/profile")
@PreAuthorize("hasRole('BORROWER')")
public class BorrowerProfileController {

    private final BorrowerProfileService profileService;

    public BorrowerProfileController(BorrowerProfileService profileService) {
        this.profileService = profileService;
    }

    @io.swagger.v3.oas.annotations.Operation(summary="save Profile")
 @PutMapping
    public ResponseEntity<BorrowerProfileResponse> saveProfile(
            Authentication authentication,
            @Valid @RequestBody BorrowerProfileRequest request) {

        return ResponseEntity.ok(
                profileService.saveProfile(authentication.getName(), request)
        );
    }

    @io.swagger.v3.oas.annotations.Operation(summary="get Profile")
 @GetMapping
    public ResponseEntity<BorrowerProfileResponse> getProfile(
            Authentication authentication) {

        return ResponseEntity.ok(
                profileService.getProfile(authentication.getName())
        );
    }
}