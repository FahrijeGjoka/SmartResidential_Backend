package com.smartresidential.backend.services.interfaces;

import com.smartresidential.backend.dto.auth.LoginResponse;
import com.smartresidential.backend.dto.auth.LoginRequest;
import com.smartresidential.backend.dto.auth.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest request);

    void verifyEmail(String identifier, String token);

    LoginResponse login(LoginRequest request);
}
