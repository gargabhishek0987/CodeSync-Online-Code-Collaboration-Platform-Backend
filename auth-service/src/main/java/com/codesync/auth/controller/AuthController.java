package com.codesync.auth.controller;

import com.codesync.auth.dto.AuthRequest;
import com.codesync.auth.dto.AuthResponse;
import com.codesync.auth.dto.RegisterRequest;
import com.codesync.auth.dto.UserProfileDto;
import com.codesync.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        String message = authService.register(registerRequest);
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        AuthResponse authResponse = authService.login(authRequest);
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile(Authentication authentication) {
        UserProfileDto profile = authService.getProfile(authentication.getName());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateProfile(Authentication authentication, 
                                                        @Valid @RequestBody UserProfileDto profileDto) {
        UserProfileDto updatedProfile = authService.updateProfile(authentication.getName(), profileDto);
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // Logout logic is typically handled client-side by dropping the JWT.
        // If we implement a token blacklist, we would do it here.
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }
}
