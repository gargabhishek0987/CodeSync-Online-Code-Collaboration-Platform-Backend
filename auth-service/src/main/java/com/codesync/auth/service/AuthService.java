package com.codesync.auth.service;

import com.codesync.auth.dto.AuthRequest;
import com.codesync.auth.dto.AuthResponse;
import com.codesync.auth.dto.RegisterRequest;
import com.codesync.auth.dto.UserProfileDto;

public interface AuthService {
    String register(RegisterRequest registerRequest);
    AuthResponse login(AuthRequest authRequest);
    UserProfileDto getProfile(String username);
    UserProfileDto updateProfile(String username, UserProfileDto userProfileDto);
    void deactivateAccount(String username);
}
