package com.obs.security.provider;

import com.obs.security.service.EhCacheTokenService;
import com.obs.security.token.AuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;

import java.util.Optional;

public class TokenAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private EhCacheTokenService ehCacheTokenService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Optional<Object> token = Optional.ofNullable(authentication.getCredentials());
        String tokenStr = token.map(Object::toString)
                .filter(StringUtils::hasText)
                .<BadCredentialsException>orElseThrow(() -> {
                    throw new BadCredentialsException("Token not present.");
                });
        return ehCacheTokenService.retrieve(tokenStr)
                .<BadCredentialsException>orElseThrow(() -> {
                    throw new BadCredentialsException("Invalid token.");
                });
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(AuthenticationToken.class);
    }
}
