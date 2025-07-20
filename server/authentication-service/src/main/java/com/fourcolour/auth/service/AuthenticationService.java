package com.fourcolour.auth.service;

import com.fourcolour.auth.entity.Session;
import com.fourcolour.auth.entity.User;
import com.fourcolour.auth.repository.SessionRepository;
import com.fourcolour.auth.repository.UserRepository;
import com.fourcolour.common.dto.LoginRequest;
import com.fourcolour.common.dto.RegisterRequest;
import com.fourcolour.common.dto.TokenResponse;
import com.fourcolour.common.service.LoggerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoggerClient loggerClient;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());

        user = userRepository.save(user);

        // Log successful registration
        loggerClient.logEvent(
                "authentication-service",
                "user_registered",
                user.getId().toString(),
                "User registered successfully: " + user.getEmail(),
                1,
                Map.of("email", user.getEmail(), "name", user.getName() != null ? user.getName() : "")
        );

        // Generate token and create session
        return createUserSession(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        // Find user by email
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (!userOpt.isPresent()) {
            // Log failed login attempt
            loggerClient.logEvent(
                    "authentication-service",
                    "login_failed",
                    "unknown",
                    "Login failed - user not found: " + request.getEmail(),
                    2,
                    Map.of("email", request.getEmail(), "reason", "user_not_found")
            );
            throw new RuntimeException("Invalid credentials");
        }

        User user = userOpt.get();

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // Log failed login attempt
            loggerClient.logEvent(
                    "authentication-service",
                    "login_failed",
                    user.getId().toString(),
                    "Login failed - invalid password: " + user.getEmail(),
                    2,
                    Map.of("email", user.getEmail(), "reason", "invalid_password")
            );
            throw new RuntimeException("Invalid credentials");
        }

        // Log successful login
        loggerClient.logEvent(
                "authentication-service",
                "user_login",
                user.getId().toString(),
                "User logged in successfully: " + user.getEmail(),
                1,
                Map.of("email", user.getEmail())
        );

        // Generate token and create session
        return createUserSession(user);
    }

    @Transactional
    public void logout(String token) {
        // Clean the token (remove Bearer prefix if present)
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        
        // Get user ID for logging before deleting session
        try {
            Integer userId = jwtService.getUserIdFromToken(cleanToken);
            sessionRepository.deleteByToken(cleanToken);
            
            // Log successful logout
            loggerClient.logEvent(
                    "authentication-service",
                    "user_logout",
                    userId.toString(),
                    "User logged out successfully",
                    1,
                    Map.of("token_length", String.valueOf(cleanToken.length()))
            );
        } catch (Exception e) {
            sessionRepository.deleteByToken(cleanToken);
        }
    }

    public boolean verifyToken(String token) {
        // Clean the token
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        
        // Check if token exists in sessions and is not expired
        Optional<Session> sessionOpt = sessionRepository.findByTokenAndNotExpired(cleanToken, LocalDateTime.now());
        
        if (!sessionOpt.isPresent()) {
            return false;
        }

        // Validate JWT structure
        return jwtService.validateToken(cleanToken);
    }

    public Integer getUserIdFromToken(String token) {
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        return jwtService.getUserIdFromToken(cleanToken);
    }

    @Transactional
    public TokenResponse refreshToken(String oldToken) {
        String cleanToken = oldToken.startsWith("Bearer ") ? oldToken.substring(7) : oldToken;
        
        // First, try to find the session by token (regardless of expiration)
        Optional<Session> sessionOpt = sessionRepository.findByToken(cleanToken);
        if (!sessionOpt.isPresent()) {
            throw new RuntimeException("Invalid token");
        }

        Session session = sessionOpt.get();
        
        // Check if the session is within grace period (2 minutes after expiration)
        LocalDateTime gracePeriod = session.getExpiresAt().plusMinutes(2);
        if (LocalDateTime.now().isAfter(gracePeriod)) {
            // Session is too old, delete it and throw error
            sessionRepository.deleteByToken(cleanToken);
            throw new RuntimeException("Token expired and grace period exceeded");
        }
        
        // Find user
        Optional<User> userOpt = userRepository.findById(session.getUserId());
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        // Log token refresh
        loggerClient.logEvent(
                "authentication-service",
                "token_refreshed",
                user.getId().toString(),
                "Token refreshed for user: " + user.getEmail(),
                1,
                Map.of("email", user.getEmail())
        );

        // Delete old session
        sessionRepository.deleteByToken(cleanToken);

        // Create new session
        return createUserSession(user);
    }

    private TokenResponse createUserSession(User user) {
        // Generate JWT token
        String token = jwtService.generateToken(user.getId());
        LocalDateTime expiresAt = jwtService.getExpirationTime();

        // Create session
        Session session = new Session();
        session.setUserId(user.getId());
        session.setToken(token);
        session.setExpiresAt(expiresAt);
        sessionRepository.save(session);

        // Log session creation
        loggerClient.logEvent(
                "authentication-service",
                "session_created",
                user.getId().toString(),
                "Session created for user: " + user.getEmail(),
                1,
                Map.of("email", user.getEmail(), "expires_at", expiresAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        );

        // Return token response
        return new TokenResponse(
                token,
                user.getName(),
                user.getId(),
                user.getEmail(),
                expiresAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
} 