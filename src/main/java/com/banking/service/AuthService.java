package com.banking.service;

import com.banking.dtos.request.LoginRequest;
import com.banking.dtos.request.RegisterRequest;
import com.banking.dtos.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}