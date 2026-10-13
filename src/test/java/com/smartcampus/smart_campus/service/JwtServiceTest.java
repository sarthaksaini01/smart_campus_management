package com.smartcampus.smart_campus.service;

import com.smartcampus.smart_campus.entity.Role;
import com.smartcampus.smart_campus.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static String randomSecret() {
        return Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded());
    }

    private static User user() {
        User user = new User();
        user.setEmail("admin@test.com");
        user.setRole(Role.ADMIN);
        return user;
    }

    @Test
    void tokenCarriesEmailAndRole() {
        JwtService jwt = new JwtService(randomSecret(), 60_000);

        Claims claims = jwt.parse(jwt.generateToken(user()));

        assertEquals("admin@test.com", claims.getSubject());
        assertEquals("ADMIN", claims.get("role", String.class));
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService jwt = new JwtService(randomSecret(), -1_000);

        String token = jwt.generateToken(user());

        assertThrows(JwtException.class, () -> jwt.parse(token));
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtService issuer = new JwtService(randomSecret(), 60_000);
        JwtService other = new JwtService(randomSecret(), 60_000);

        String token = issuer.generateToken(user());

        assertThrows(JwtException.class, () -> other.parse(token));
    }

    @Test
    void garbageTokenIsRejected() {
        JwtService jwt = new JwtService(randomSecret(), 60_000);

        assertThrows(JwtException.class, () -> jwt.parse("not-a-token"));
    }
}