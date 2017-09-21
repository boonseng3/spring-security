package com.obs.security.service;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EhCacheTokenService {
    private static final Logger logger = LoggerFactory.getLogger(EhCacheTokenService.class);
    private static final Cache authTokenCache = CacheManager.getInstance().getCache("aclCache");

    @Autowired
    private TokenGenerationStrategy tokenGenerationStrategy;

    public String generateNewToken() {
        return tokenGenerationStrategy.generateToken();
    }

    public void store(String token, Authentication authentication) {
        authTokenCache.put(new Element(token, authentication));
    }

    public Optional<Authentication> retrieve(String token) {
        return Optional.ofNullable(authTokenCache.get(token))
                .map(element -> (Authentication) element.getObjectValue());
    }
}
