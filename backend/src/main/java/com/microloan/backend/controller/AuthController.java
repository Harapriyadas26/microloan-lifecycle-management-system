package com.microloan.backend.controller;

import com.microloan.backend.dto.AuthResponse;
import com.microloan.backend.dto.LoginRequest;
import com.microloan.backend.dto.RegisterRequest;
import com.microloan.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@io.swagger.v3.oas.annotations.security.SecurityRequirements
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @io.swagger.v3.oas.annotations.Operation(summary="register")
 @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @io.swagger.v3.oas.annotations.Operation(summary="login")
 @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }
}