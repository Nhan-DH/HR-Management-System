package com.dona.spring_rest.feature.auth;

import com.dona.spring_rest.feature.auth.dto.LoginRequest;
import com.dona.spring_rest.feature.auth.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
