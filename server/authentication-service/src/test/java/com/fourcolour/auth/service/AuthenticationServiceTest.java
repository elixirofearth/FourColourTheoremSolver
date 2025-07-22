package com.fourcolour.auth.service;

import com.fourcolour.auth.entity.Session;
import com.fourcolour.auth.entity.User;
import com.fourcolour.auth.repository.SessionRepository;
import com.fourcolour.auth.repository.UserRepository;
import com.fourcolour.common.dto.LoginRequest;
import com.fourcolour.common.dto.RegisterRequest;
import com.fourcolour.common.dto.TokenResponse;
import com.fourcolour.common.service.LoggerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LoggerClient loggerClient;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;
    private Session testSession;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        testUser = new User();
        testUser.setId(1);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("$2a$10$hashedpassword");
        testUser.setCreatedAt(LocalDateTime.now());

        testSession = new Session();
        testSession.setId(1);
        testSession.setUserId(1);
        testSession.setToken("jwt-token");
        testSession.setExpiresAt(LocalDateTime.now().plusHours(24));
        testSession.setCreatedAt(LocalDateTime.now());

        // Setup common mocks
        doNothing().when(loggerClient).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
    }

    // ==================== REGISTRATION TESTS ====================

    @Test
    void register_WithValidRequest_ShouldReturnTokenResponse() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(testUser.getId())).thenReturn("jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(LocalDateTime.now().plusHours(24));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        TokenResponse response = authenticationService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Test User", response.getName());
        assertEquals(1, response.getUserId());
        assertEquals("test@example.com", response.getEmail());

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(testUser.getId());
        verify(sessionRepository).save(any(Session.class));
        verify(loggerClient).logEvent(eq("authentication-service"), eq("user_registered"), 
                eq("1"), anyString(), eq(1), any());
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.register(registerRequest);
        });

        assertEquals("User with this email already exists", exception.getMessage());
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(loggerClient, never()).logEvent(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
    }

    @Test
    void register_WithNullName_ShouldCreateUserWithNullName() {
        registerRequest.setName(null);
        
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("$2a$10$hashedpassword");
        
        User userWithNullName = new User();
        userWithNullName.setId(1);
        userWithNullName.setName(null);
        userWithNullName.setEmail("test@example.com");
        userWithNullName.setPasswordHash("$2a$10$hashedpassword");
        
        when(userRepository.save(any(User.class))).thenReturn(userWithNullName);
        when(jwtService.generateToken(1)).thenReturn("jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(LocalDateTime.now().plusHours(24));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        TokenResponse response = authenticationService.register(registerRequest);

        assertNotNull(response);
        assertNull(response.getName());
        verify(userRepository).save(argThat(user -> user.getName() == null));
    }

    // ==================== LOGIN TESTS ====================

    @Test
    void login_WithValidCredentials_ShouldReturnTokenResponse() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(testUser.getId())).thenReturn("jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(LocalDateTime.now().plusHours(24));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        TokenResponse response = authenticationService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Test User", response.getName());
        assertEquals(1, response.getUserId());
        assertEquals("test@example.com", response.getEmail());

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPasswordHash());
        verify(jwtService).generateToken(testUser.getId());
        verify(sessionRepository).save(any(Session.class));
        verify(loggerClient).logEvent(eq("authentication-service"), eq("user_login"), 
                eq("1"), anyString(), eq(1), any());
    }

    @Test
    void login_WithNonExistentUser_ShouldThrowException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.login(loginRequest);
        });

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(loggerClient).logEvent(eq("authentication-service"), eq("login_failed"), 
                eq("unknown"), anyString(), eq(2), any());
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.login(loginRequest);
        });

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPasswordHash());
        verify(jwtService, never()).generateToken(anyInt());
        verify(loggerClient).logEvent(eq("authentication-service"), eq("login_failed"), 
                eq("1"), anyString(), eq(2), any());
    }

    // ==================== LOGOUT TESTS ====================

    @Test
    void logout_WithValidToken_ShouldDeleteSession() {
        String token = "Bearer jwt-token";
        when(jwtService.getUserIdFromToken("jwt-token")).thenReturn(1);
        doNothing().when(sessionRepository).deleteByToken("jwt-token");

        authenticationService.logout(token);

        verify(sessionRepository).deleteByToken("jwt-token");
        verify(loggerClient).logEvent(eq("authentication-service"), eq("user_logout"), 
                eq("1"), anyString(), eq(1), any());
    }

    @Test
    void logout_WithTokenWithoutBearer_ShouldDeleteSession() {
        String token = "jwt-token";
        when(jwtService.getUserIdFromToken("jwt-token")).thenReturn(1);
        doNothing().when(sessionRepository).deleteByToken("jwt-token");

        authenticationService.logout(token);

        verify(sessionRepository).deleteByToken("jwt-token");
        verify(loggerClient).logEvent(eq("authentication-service"), eq("user_logout"), 
                eq("1"), anyString(), eq(1), any());
    }

    @Test
    void logout_WithInvalidToken_ShouldStillDeleteSession() {
        String token = "Bearer invalid-token";
        when(jwtService.getUserIdFromToken("invalid-token")).thenThrow(new RuntimeException("Invalid token"));
        doNothing().when(sessionRepository).deleteByToken("invalid-token");

        authenticationService.logout(token);

        verify(sessionRepository).deleteByToken("invalid-token");
        // Should not log user logout event if token is invalid
        verify(loggerClient, never()).logEvent(eq("authentication-service"), eq("user_logout"), 
                anyString(), anyString(), eq(1), any());
    }

    // ==================== TOKEN VERIFICATION TESTS ====================

    @Test
    void verifyToken_WithValidToken_ShouldReturnTrue() {
        String token = "Bearer jwt-token";
        when(sessionRepository.findByTokenAndNotExpired("jwt-token", any(LocalDateTime.class)))
                .thenReturn(Optional.of(testSession));
        when(jwtService.validateToken("jwt-token")).thenReturn(true);

        boolean result = authenticationService.verifyToken(token);

        assertTrue(result);
        verify(sessionRepository).findByTokenAndNotExpired("jwt-token", any(LocalDateTime.class));
        verify(jwtService).validateToken("jwt-token");
    }

    @Test
    void verifyToken_WithExpiredSession_ShouldReturnFalse() {
        String token = "Bearer jwt-token";
        when(sessionRepository.findByTokenAndNotExpired("jwt-token", any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        boolean result = authenticationService.verifyToken(token);

        assertFalse(result);
        verify(sessionRepository).findByTokenAndNotExpired("jwt-token", any(LocalDateTime.class));
        verify(jwtService, never()).validateToken(anyString());
    }

    @Test
    void verifyToken_WithInvalidJwt_ShouldReturnFalse() {
        String token = "Bearer jwt-token";
        when(sessionRepository.findByTokenAndNotExpired("jwt-token", any(LocalDateTime.class)))
                .thenReturn(Optional.of(testSession));
        when(jwtService.validateToken("jwt-token")).thenReturn(false);

        boolean result = authenticationService.verifyToken(token);

        assertFalse(result);
        verify(sessionRepository).findByTokenAndNotExpired("jwt-token", any(LocalDateTime.class));
        verify(jwtService).validateToken("jwt-token");
    }

    @Test
    void verifyToken_WithTokenWithoutBearer_ShouldWork() {
        String token = "jwt-token";
        when(sessionRepository.findByTokenAndNotExpired("jwt-token", any(LocalDateTime.class)))
                .thenReturn(Optional.of(testSession));
        when(jwtService.validateToken("jwt-token")).thenReturn(true);

        boolean result = authenticationService.verifyToken(token);

        assertTrue(result);
        verify(sessionRepository).findByTokenAndNotExpired("jwt-token", any(LocalDateTime.class));
        verify(jwtService).validateToken("jwt-token");
    }

    // ==================== GET USER ID FROM TOKEN TESTS ====================

    @Test
    void getUserIdFromToken_WithValidToken_ShouldReturnUserId() {
        String token = "Bearer jwt-token";
        when(jwtService.getUserIdFromToken("jwt-token")).thenReturn(1);

        Integer userId = authenticationService.getUserIdFromToken(token);

        assertEquals(1, userId);
        verify(jwtService).getUserIdFromToken("jwt-token");
    }

    @Test
    void getUserIdFromToken_WithTokenWithoutBearer_ShouldReturnUserId() {
        String token = "jwt-token";
        when(jwtService.getUserIdFromToken("jwt-token")).thenReturn(1);

        Integer userId = authenticationService.getUserIdFromToken(token);

        assertEquals(1, userId);
        verify(jwtService).getUserIdFromToken("jwt-token");
    }

    // ==================== TOKEN REFRESH TESTS ====================

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewToken() {
        String oldToken = "Bearer old-jwt-token";
        LocalDateTime futureExpiry = LocalDateTime.now().plusHours(1);
        
        Session oldSession = new Session();
        oldSession.setId(1);
        oldSession.setUserId(1);
        oldSession.setToken("old-jwt-token");
        oldSession.setExpiresAt(futureExpiry);

        when(sessionRepository.findByToken("old-jwt-token")).thenReturn(Optional.of(oldSession));
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(1)).thenReturn("new-jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(LocalDateTime.now().plusHours(24));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);
        doNothing().when(sessionRepository).deleteByToken("old-jwt-token");

        TokenResponse response = authenticationService.refreshToken(oldToken);

        assertNotNull(response);
        assertEquals("new-jwt-token", response.getToken());
        assertEquals("Test User", response.getName());
        assertEquals(1, response.getUserId());

        verify(sessionRepository).findByToken("old-jwt-token");
        verify(sessionRepository).deleteByToken("old-jwt-token");
        verify(sessionRepository).save(any(Session.class));
        verify(loggerClient).logEvent(eq("authentication-service"), eq("token_refreshed"), 
                eq("1"), anyString(), eq(1), any());
    }

    @Test
    void refreshToken_WithNonExistentSession_ShouldThrowException() {
        String oldToken = "Bearer non-existent-token";
        when(sessionRepository.findByToken("non-existent-token")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.refreshToken(oldToken);
        });

        assertEquals("Invalid token", exception.getMessage());
        verify(sessionRepository).findByToken("non-existent-token");
        verify(sessionRepository, never()).deleteByToken(anyString());
        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void refreshToken_WithExpiredTokenBeyondGracePeriod_ShouldThrowException() {
        String oldToken = "Bearer expired-token";
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(5); // 5 minutes ago
        
        Session expiredSession = new Session();
        expiredSession.setUserId(1);
        expiredSession.setToken("expired-token");
        expiredSession.setExpiresAt(expiredTime);

        when(sessionRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredSession));
        doNothing().when(sessionRepository).deleteByToken("expired-token");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.refreshToken(oldToken);
        });

        assertEquals("Token expired and grace period exceeded", exception.getMessage());
        verify(sessionRepository).findByToken("expired-token");
        verify(sessionRepository).deleteByToken("expired-token");
        verify(userRepository, never()).findById(anyInt());
    }

    @Test
    void refreshToken_WithNonExistentUser_ShouldThrowException() {
        String oldToken = "Bearer valid-token";
        LocalDateTime futureExpiry = LocalDateTime.now().plusHours(1);
        
        Session validSession = new Session();
        validSession.setUserId(999); // Non-existent user ID
        validSession.setToken("valid-token");
        validSession.setExpiresAt(futureExpiry);

        when(sessionRepository.findByToken("valid-token")).thenReturn(Optional.of(validSession));
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.refreshToken(oldToken);
        });

        assertEquals("User not found", exception.getMessage());
        verify(sessionRepository).findByToken("valid-token");
        verify(userRepository).findById(999);
        verify(sessionRepository, never()).deleteByToken(anyString());
    }

    @Test
    void refreshToken_WithTokenWithoutBearer_ShouldWork() {
        String oldToken = "old-jwt-token";
        LocalDateTime futureExpiry = LocalDateTime.now().plusHours(1);
        
        Session oldSession = new Session();
        oldSession.setUserId(1);
        oldSession.setToken("old-jwt-token");
        oldSession.setExpiresAt(futureExpiry);

        when(sessionRepository.findByToken("old-jwt-token")).thenReturn(Optional.of(oldSession));
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(1)).thenReturn("new-jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(LocalDateTime.now().plusHours(24));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);
        doNothing().when(sessionRepository).deleteByToken("old-jwt-token");

        TokenResponse response = authenticationService.refreshToken(oldToken);

        assertNotNull(response);
        assertEquals("new-jwt-token", response.getToken());
        verify(sessionRepository).findByToken("old-jwt-token");
    }

    // ==================== CREATE USER SESSION TESTS ====================

    @Test
    void createUserSession_ShouldCreateSessionAndReturnTokenResponse() {
        when(jwtService.generateToken(testUser.getId())).thenReturn("jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(LocalDateTime.now().plusHours(24));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        // Use reflection to test private method
        TokenResponse response = org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                authenticationService, "createUserSession", testUser);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Test User", response.getName());
        assertEquals(1, response.getUserId());
        assertEquals("test@example.com", response.getEmail());

        verify(jwtService).generateToken(testUser.getId());
        verify(jwtService).getExpirationTime();
        verify(sessionRepository).save(any(Session.class));
        verify(loggerClient).logEvent(eq("authentication-service"), eq("session_created"), 
                eq("1"), anyString(), eq(1), any());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    void register_WithEmptyPassword_ShouldStillHashPassword() {
        registerRequest.setPassword("");
        
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("")).thenReturn("$2a$10$emptyhashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(testUser.getId())).thenReturn("jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(LocalDateTime.now().plusHours(24));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        TokenResponse response = authenticationService.register(registerRequest);

        assertNotNull(response);
        verify(passwordEncoder).encode("");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_WithEmptyPassword_ShouldStillAttemptMatch() {
        loginRequest.setPassword("");
        
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("", testUser.getPasswordHash())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.login(loginRequest);
        });

        assertEquals("Invalid credentials", exception.getMessage());
        verify(passwordEncoder).matches("", testUser.getPasswordHash());
    }

    @Test
    void verifyToken_WithEmptyToken_ShouldReturnFalse() {
        String token = "";
        when(sessionRepository.findByTokenAndNotExpired("", any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        boolean result = authenticationService.verifyToken(token);

        assertFalse(result);
        verify(sessionRepository).findByTokenAndNotExpired("", any(LocalDateTime.class));
    }

    @Test
    void logout_WithEmptyToken_ShouldStillCallDelete() {
        String token = "";
        when(jwtService.getUserIdFromToken("")).thenThrow(new RuntimeException("Empty token"));
        doNothing().when(sessionRepository).deleteByToken("");

        authenticationService.logout(token);

        verify(sessionRepository).deleteByToken("");
    }

    @Test
    void register_WithVeryLongEmail_ShouldHandleGracefully() {
        StringBuilder longEmail = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longEmail.append("a");
        }
        longEmail.append("@example.com");
        registerRequest.setEmail(longEmail.toString());
        
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(testUser.getId())).thenReturn("jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(LocalDateTime.now().plusHours(24));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        TokenResponse response = authenticationService.register(registerRequest);

        assertNotNull(response);
        verify(userRepository).existsByEmail(longEmail.toString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_WithSpecialCharactersInPassword_ShouldWork() {
        loginRequest.setPassword("P@ssw0rd!@#$%^&*()");
        
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(testUser.getId())).thenReturn("jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(LocalDateTime.now().plusHours(24));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        TokenResponse response = authenticationService.login(loginRequest);

        assertNotNull(response);
        verify(passwordEncoder).matches("P@ssw0rd!@#$%^&*()", testUser.getPasswordHash());
    }

    @Test
    void refreshToken_WithTokenJustWithinGracePeriod_ShouldSucceed() {
        String oldToken = "Bearer barely-valid-token";
        LocalDateTime justExpired = LocalDateTime.now().minusMinutes(1); // 1 minute ago
        
        Session barelyValidSession = new Session();
        barelyValidSession.setUserId(1);
        barelyValidSession.setToken("barely-valid-token");
        barelyValidSession.setExpiresAt(justExpired); // Expired but within grace period

        when(sessionRepository.findByToken("barely-valid-token")).thenReturn(Optional.of(barelyValidSession));
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(1)).thenReturn("new-jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(LocalDateTime.now().plusHours(24));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);
        doNothing().when(sessionRepository).deleteByToken("barely-valid-token");

        TokenResponse response = authenticationService.refreshToken(oldToken);

        assertNotNull(response);
        assertEquals("new-jwt-token", response.getToken());
        verify(sessionRepository).deleteByToken("barely-valid-token");
        verify(sessionRepository).save(any(Session.class));
    }
}