package com.obs.security.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

public class AuthenticationToken extends AbstractAuthenticationToken {

    private String token;

    public AuthenticationToken(String token) {
        //noinspection unchecked
        super(Collections.EMPTY_LIST);
        super.setAuthenticated(true);
        this.token = token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }
}
