package com.sonidovivo.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InternalTokenValidator {

    private final String expectedToken;

    public InternalTokenValidator(@Value("${app.internal-token}") String expectedToken) {
        this.expectedToken = expectedToken;
    }

    public boolean esValido(String token) {
        if (token == null) {
            return false;
        }

        return MessageDigest.isEqual(
                expectedToken.getBytes(StandardCharsets.UTF_8),
                token.getBytes(StandardCharsets.UTF_8));
    }
}
