package com.obs.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obs.security.dto.AuthenticationSuccessResponse;
import com.obs.security.service.EhCacheTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UsernamePasswordAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private EhCacheTokenService ehCacheTokenService;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String token = ehCacheTokenService.generateNewToken();
        ehCacheTokenService.store(token, authentication);
        AuthenticationSuccessResponse authenticationSuccessResponse = new AuthenticationSuccessResponse()
                .setToken(token);
        response.getWriter()
                .append(objectMapper.writeValueAsString(authenticationSuccessResponse));
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
    }
}
