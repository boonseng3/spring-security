package com.obs.security.dto;

import java.io.Serializable;

public class AuthenticationSuccessResponse implements Serializable {
    private String token;

    public String getToken() {
        return token;
    }

    public AuthenticationSuccessResponse setToken(String token) {
        this.token = token;
        return this;
    }
}
