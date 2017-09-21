package com.obs.security.service;

import java.util.UUID;

/**
 * Generate a token using UUID string.
 */
public class UuidTokenGenerationStrategy implements TokenGenerationStrategy {
    @Override
    public String generateToken() {
        return UUID.randomUUID().toString();
    }
}
