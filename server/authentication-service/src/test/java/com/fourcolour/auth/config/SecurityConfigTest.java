package com.fourcolour.auth.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @InjectMocks
    private SecurityConfig securityConfig;

    @Test
    void passwordEncoder_ShouldCreateBCryptPasswordEncoder() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        
        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);
    }

    @Test
    void passwordEncoder_ShouldEncryptPasswords() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String plainPassword = "testPassword123";
        
        String encodedPassword = passwordEncoder.encode(plainPassword);
        
        assertNotNull(encodedPassword);
        assertNotEquals(plainPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(plainPassword, encodedPassword));
    }

    @Test
    void passwordEncoder_ShouldGenerateDifferentHashesForSamePassword() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String plainPassword = "testPassword123";
        
        String hash1 = passwordEncoder.encode(plainPassword);
        String hash2 = passwordEncoder.encode(plainPassword);
        
        assertNotEquals(hash1, hash2);
        assertTrue(passwordEncoder.matches(plainPassword, hash1));
        assertTrue(passwordEncoder.matches(plainPassword, hash2));
    }

    @Test
    void passwordEncoder_ShouldNotMatchIncorrectPassword() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String correctPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        
        String encodedPassword = passwordEncoder.encode(correctPassword);
        
        assertFalse(passwordEncoder.matches(wrongPassword, encodedPassword));
    }
}