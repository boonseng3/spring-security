package com.obs.basic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    private MockMvc mvc;

    private final String defaultUsernameParam = "username";

    private final String defaultPasswordParam = "password";

    private final String defaultUsername = "user";

    private final String defaultPassword = "P@ssw0rd";

    private final String defaultLoginUrl = "/login";

    private final String profileUrl = "/profile";

    @LocalServerPort
    private int serverPort;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    public void loginWithoutCsrf() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set(defaultUsernameParam, defaultUsername);
        params.set(defaultPasswordParam, defaultPassword);
        mvc.perform(MockMvcRequestBuilders.post(defaultLoginUrl)
                .params(params)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(status().reason("Could not verify the provided CSRF token because your session was not found."));
    }

    @Test
    public void loginWithCsrf() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set(defaultUsernameParam, defaultUsername);
        params.set(defaultPasswordParam, defaultPassword);
        mvc.perform(MockMvcRequestBuilders.post(defaultLoginUrl)
                .params(params)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void loginWithCsrfWithInvalidCredentials() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set(defaultUsernameParam, defaultUsername);
        params.set(defaultPasswordParam, "");
        mvc.perform(MockMvcRequestBuilders.post(defaultLoginUrl)
                .params(params)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    public void accessProtectedResourceWithoutLogin() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(profileUrl)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    public void accessProtectedResourceWithLogin() throws Exception {
        accessProtectedResourceWithoutLogin();
        loginWithCsrf();
        mvc.perform(MockMvcRequestBuilders.get(profileUrl)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost/login"));
    }
}