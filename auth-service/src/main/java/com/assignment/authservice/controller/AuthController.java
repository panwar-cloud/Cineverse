package com.assignment.authservice.controller;

import com.assignment.authservice.dto.AuthResponse;
import com.assignment.authservice.dto.LoginRequest;
import com.assignment.authservice.dto.RegisterRequest;
import com.assignment.authservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    // Role-based Access Control Verification Endpoints
    @GetMapping("/test/user")
    public ResponseEntity<Map<String, Object>> testUserEndpoint(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Success! Accessible by USER, THEATRE_OWNER, and ADMIN roles.");
        response.put("principal", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test/theatre")
    public ResponseEntity<Map<String, Object>> testTheatreEndpoint(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Success! Accessible by THEATRE_OWNER and ADMIN roles.");
        response.put("principal", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test/admin")
    public ResponseEntity<Map<String, Object>> testAdminEndpoint(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Success! Restricted to ADMIN role only.");
        response.put("principal", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        return ResponseEntity.ok(response);
    }
}
