package com.obs.security.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Key;
import java.security.KeyStore;

/**
 * Generate a token using Jwt
 */
public class JwtTokenGenerationStrategy implements TokenGenerationStrategy {

    @Autowired
    private KeyStore jwtKeystore;
    @Value("${application.jwt.keystore.alias}")
    private String jwtKeystoreAlias;
    @Value("${application.jwt.keystore.password}")
    private String jwtKeystorePassword;

    @Override
    public String generateToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Key key = jwtKeystore.getKey(jwtKeystoreAlias, jwtKeystorePassword.toCharArray());
            String jwt = Jwts.builder().setSubject(authentication.getName())
                    .signWith(SignatureAlgorithm.RS256, key)
                    .compact();
            return jwt;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
