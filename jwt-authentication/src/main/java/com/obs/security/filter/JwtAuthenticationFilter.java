package com.obs.security.filter;

import com.obs.security.token.AuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    public static final String HEADER_SECURITY_TOKEN = "Authorization";

    private AuthenticationManager authenticationManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HEADER_SECURITY_TOKEN);

        try {
            if (StringUtils.hasText(header) && StringUtils.startsWithIgnoreCase(header, "Bearer")) {
                String[] headerTokens = StringUtils.split(header, " ");
                if (headerTokens.length == 2) {
                    String token = headerTokens[1];
                    AuthenticationToken authRequest = new AuthenticationToken(token);
                    logger.info("token found: {}", token);

                    Authentication authResult = this.getAuthenticationManager().authenticate(authRequest);
                    SecurityContextHolder.getContext().setAuthentication(authResult);
                }
            }
        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            logger.debug("Authentication request for failed", ex );
        }


        filterChain.doFilter(request, response);
    }

    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    public JwtAuthenticationFilter setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        return this;
    }
}