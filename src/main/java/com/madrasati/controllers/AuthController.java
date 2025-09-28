package com.madrasati.controllers;

import com.madrasati.services.IdentityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IdentityService identityService;

    public AuthController(IdentityService identityService) { this.identityService = identityService; }

    public record LoginBody(String username, String password) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginBody body) {
        var res = identityService.loginWithRetry(
                new IdentityService.LoginRequest(body.username(), body.password()),
                3,
                Duration.ofMillis(500),
                Duration.ofMillis(200)
        );
        return ResponseEntity.ok(res);
    }
}
