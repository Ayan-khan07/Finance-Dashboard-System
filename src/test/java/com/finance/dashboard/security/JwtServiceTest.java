package com.finance.dashboard.security;

import com.finance.dashboard.config.JwtProperties;
import com.finance.dashboard.enums.Role;
import com.finance.dashboard.enums.UserStatus;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("unit-test-secret-key-with-more-than-32-bytes!!".getBytes(StandardCharsets.UTF_8));

    private static final CustomUserDetails USER =
            new CustomUserDetails(1L, "alice", "hash", Role.ANALYST, UserStatus.ACTIVE);

    @Test
    void generatedTokenRoundTripsToUsername() {
        JwtService service = new JwtService(new JwtProperties(SECRET, 60));

        String token = service.generateToken(USER);

        assertEquals("alice", service.extractUsername(token));
        assertEquals(3600, service.getExpirationSeconds());
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtService service = new JwtService(new JwtProperties(SECRET, 60));
        String token = service.generateToken(USER);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThrows(JwtException.class, () -> service.extractUsername(tampered));
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService service = new JwtService(new JwtProperties(SECRET, -5));
        String token = service.generateToken(USER);

        assertThrows(JwtException.class, () -> service.extractUsername(token));
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String otherSecret = Base64.getEncoder()
                .encodeToString("a-completely-different-secret-key-0123456789".getBytes(StandardCharsets.UTF_8));
        String foreignToken = new JwtService(new JwtProperties(otherSecret, 60)).generateToken(USER);

        JwtService service = new JwtService(new JwtProperties(SECRET, 60));

        assertThrows(JwtException.class, () -> service.extractUsername(foreignToken));
    }

    @Test
    void shortSecretPreventsStartup() {
        String shortSecret = Base64.getEncoder().encodeToString("too-short".getBytes(StandardCharsets.UTF_8));

        assertThrows(IllegalStateException.class, () -> new JwtService(new JwtProperties(shortSecret, 60)));
    }
}
