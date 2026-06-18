package com.codesync.auth.service;

import com.codesync.auth.dto.AuthRequest;
import com.codesync.auth.dto.AuthResponse;
import com.codesync.auth.dto.RegisterRequest;
import com.codesync.auth.dto.UserProfileDto;
import com.codesync.auth.entity.User;
import com.codesync.auth.exception.CustomException;
import com.codesync.auth.repository.UserRepository;
import com.codesync.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .isActive(true)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
    }

    @Test
    void testRegister_Success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        String result = authService.register(registerRequest);

        assertEquals("User registered successfully.", result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegister_UsernameTaken() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(CustomException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLogin_Success() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsernameOrEmail("testuser");
        authRequest.setPassword("password123");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token");
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(authRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("testuser", response.getUser().getUsername());
    }

    @Test
    void testGetProfile_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserProfileDto profile = authService.getProfile("testuser");

        assertNotNull(profile);
        assertEquals("testuser", profile.getUsername());
    }

    @Test
    void testDeactivateAccount() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        authService.deactivateAccount("testuser");

        assertFalse(user.isActive());
        verify(userRepository, times(1)).save(user);
    }
}
