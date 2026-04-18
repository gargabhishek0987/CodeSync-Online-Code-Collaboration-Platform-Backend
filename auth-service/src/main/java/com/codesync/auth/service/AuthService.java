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
    void changePassword(String username, com.codesync.auth.dto.PasswordUpdateRequest request);
    void updateEmail(String username, com.codesync.auth.dto.EmailUpdateRequest request);
    java.util.List<UserProfileDto> getAllUsers();
    void deleteUser(Long userId);
    void deactivateAccount(String username);
}
