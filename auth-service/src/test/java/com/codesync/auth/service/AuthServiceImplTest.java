package com.codesync.auth.service;

import com.codesync.auth.dto.AuthRequest;
import com.codesync.auth.dto.AuthResponse;
import com.codesync.auth.dto.RegisterRequest;
import com.codesync.auth.dto.UserProfileDto;
import com.codesync.auth.entity.Provider;
import com.codesync.auth.entity.Role;
import com.codesync.auth.entity.User;
import com.codesync.auth.exception.CustomException;
import com.codesync.auth.repository.UserRepository;
import com.codesync.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for AuthServiceImpl
 *
 * Tools used:
 * - JUnit 5       : Test framework (@Test, @BeforeEach, assertions)
 * - Mockito       : Mock dependencies so we test ONLY the service logic
 * - @ExtendWith   : Wires Mockito into JUnit 5
 *
 * KEY CONCEPT: We use Mocks so that:
 * - No real database is hit
 * - No real JWT is generated
 * - Tests run fast and are isolated
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    // ─── Mocks (fake versions of real dependencies) ─────────────────────────
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;

    // ─── System Under Test (the real class we are testing) ──────────────────
    @InjectMocks
    private AuthServiceImpl authService;

    // ─── Reusable test data ──────────────────────────────────────────────────
    private User mockUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        // A sample user we reuse across tests
        mockUser = User.builder()
                .id(1L)
                .username("john")
                .email("john@test.com")
                .passwordHash("$2a$10$hashedpassword")
                .role(Role.DEVELOPER)
                .provider(Provider.LOCAL)
                .isActive(true)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("john");
        registerRequest.setEmail("john@test.com");
        registerRequest.setPassword("password123");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  REGISTER TESTS
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("register() → Success: new user is saved when username and email are unique")
    void register_success_whenUsernameAndEmailAreUnique() {
        // ARRANGE: set up mocks to simulate "user does not exist yet"
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashed");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // ACT: call the real method
        String result = authService.register(registerRequest);

        // ASSERT: verify the result and interactions
        assertEquals("User registered successfully.", result);
        verify(userRepository, times(1)).save(any(User.class)); // save was called once
        verify(passwordEncoder, times(1)).encode("password123");  // password was hashed
    }

    @Test
    @DisplayName("register() → Fails: throws exception when username already exists")
    void register_throwsException_whenUsernameAlreadyTaken() {
        // ARRANGE: simulate username already taken
        when(userRepository.existsByUsername("john")).thenReturn(true);

        // ACT + ASSERT: expect CustomException to be thrown
        CustomException ex = assertThrows(CustomException.class,
                () -> authService.register(registerRequest));

        assertEquals("Username is already taken!", ex.getMessage());
        verify(userRepository, never()).save(any()); // save should NEVER be called
    }

    @Test
    @DisplayName("register() → Fails: throws exception when email already registered")
    void register_throwsException_whenEmailAlreadyExists() {
        // ARRANGE
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

        // ACT + ASSERT
        CustomException ex = assertThrows(CustomException.class,
                () -> authService.register(registerRequest));

        assertEquals("Email is already registered!", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LOGIN TESTS
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("login() → Success: returns AuthResponse with JWT token")
    void login_success_returnsAuthResponseWithToken() {
        // ARRANGE
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsernameOrEmail("john");
        authRequest.setPassword("password123");

        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn("john");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(jwtTokenProvider.generateToken(mockAuth)).thenReturn("mocked.jwt.token");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));

        // ACT
        AuthResponse response = authService.login(authRequest);

        // ASSERT
        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("john", response.getUser().getUsername());
        assertEquals("john@test.com", response.getUser().getEmail());
    }

    @Test
    @DisplayName("login() → Fails: throws exception when credentials are wrong")
    void login_throwsException_whenBadCredentials() {
        // ARRANGE: AuthenticationManager throws BadCredentialsException for wrong password
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsernameOrEmail("john");
        authRequest.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // ACT + ASSERT
        assertThrows(BadCredentialsException.class, () -> authService.login(authRequest));
        verify(jwtTokenProvider, never()).generateToken(any()); // token should not be generated
    }

    // ════════════════════════════════════════════════════════════════════════
    //  GET PROFILE TESTS
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getProfile() → Returns UserProfileDto for valid username")
    void getProfile_returnsDto_forExistingUser() {
        // ARRANGE
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));

        // ACT
        UserProfileDto dto = authService.getProfile("john");

        // ASSERT
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("john", dto.getUsername());
        assertEquals("john@test.com", dto.getEmail());
        assertEquals(Role.DEVELOPER, dto.getRole());
    }

    @Test
    @DisplayName("getProfile() → Throws CustomException when user not found")
    void getProfile_throwsException_whenUserNotFound() {
        // ARRANGE
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        // ACT + ASSERT
        CustomException ex = assertThrows(CustomException.class,
                () -> authService.getProfile("ghost"));

        assertEquals("User not found", ex.getMessage());
    }

    // ════════════════════════════════════════════════════════════════════════
    //  GET ALL USERS TEST
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getAllUsers() → Returns list of all users as DTOs")
    void getAllUsers_returnsMappedDtoList() {
        // ARRANGE
        User user2 = User.builder()
                .id(2L).username("jane").email("jane@test.com")
                .role(Role.ADMIN).provider(Provider.LOCAL).isActive(true)
                .build();
        when(userRepository.findAll()).thenReturn(List.of(mockUser, user2));

        // ACT
        List<UserProfileDto> users = authService.getAllUsers();

        // ASSERT
        assertEquals(2, users.size());
        assertEquals("john", users.get(0).getUsername());
        assertEquals("jane", users.get(1).getUsername());
        assertEquals(Role.ADMIN, users.get(1).getRole());
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DELETE USER TEST
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("deleteUser() → Calls repository.deleteById with correct ID")
    void deleteUser_callsRepositoryWithCorrectId() {
        // ARRANGE
        doNothing().when(userRepository).deleteById(1L);

        // ACT
        authService.deleteUser(1L);

        // ASSERT: verify deleteById was called with ID = 1
        verify(userRepository, times(1)).deleteById(1L);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CHANGE PASSWORD TEST
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("changePassword() → Fails when old password does not match")
    void changePassword_throwsException_whenOldPasswordWrong() {
        // ARRANGE
        com.codesync.auth.dto.PasswordUpdateRequest req = new com.codesync.auth.dto.PasswordUpdateRequest();
        req.setOldPassword("wrongold");
        req.setNewPassword("newpass");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongold", mockUser.getPasswordHash())).thenReturn(false);

        // ACT + ASSERT
        CustomException ex = assertThrows(CustomException.class,
                () -> authService.changePassword("john", req));

        assertEquals("Old password does not match", ex.getMessage());
        verify(userRepository, never()).save(any()); // password should NOT be updated
    }
}
