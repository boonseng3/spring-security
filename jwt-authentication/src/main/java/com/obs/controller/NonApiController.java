package com.obs.controller;

import com.obs.security.dto.AuthenticationSuccessResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class NonApiController {
    @GetMapping("/test")
    public AuthenticationSuccessResponse test() {
        return new AuthenticationSuccessResponse().setToken("test");
    }
}
