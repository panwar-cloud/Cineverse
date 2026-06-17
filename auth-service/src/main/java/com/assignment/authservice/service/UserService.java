package com.assignment.authservice.service;

import com.assignment.authservice.dto.AuthResponse;
import com.assignment.authservice.dto.LoginRequest;
import com.assignment.authservice.dto.RegisterRequest;

public interface UserService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
