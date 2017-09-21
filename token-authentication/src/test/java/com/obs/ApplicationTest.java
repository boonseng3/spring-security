package com.obs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obs.security.dto.AuthenticationSuccessResponse;
import com.obs.security.filter.TokenAuthenticationFilter;
import org.assertj.core.api.Assertions;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Filter springSecurityFilterChain;

    private MockMvc mvc;

    private final String defaultUsernameParam = "username";

    private final String defaultPasswordParam = "password";

    private final String defaultUsername = "user";

    private final String defaultPassword = "P@ssw0rd";

    private final String defaultLoginUrl = "/api/login";

    @LocalServerPort
    private int serverPort;

    private final String uuidPattern = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    public void loginWithValidCredentials() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set(defaultUsernameParam, defaultUsername);
        params.set(defaultPasswordParam, defaultPassword);
        MvcResult result = mvc.perform(MockMvcRequestBuilders.post(defaultLoginUrl)
                .params(params)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        System.out.println("result.getResponse().getContentAsString() = " + result.getResponse().getContentAsString());
        AuthenticationSuccessResponse responseObj = objectMapper.readValue(result.getResponse().getContentAsString(),
                AuthenticationSuccessResponse.class);

       Assertions.assertThat(Pattern.compile(uuidPattern).matcher(responseObj.getToken()).matches()).isTrue();
    }

    @Test
    public void loginWithInvalidCredentials() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set(defaultUsernameParam, UUID.randomUUID().toString());
        params.set(defaultPasswordParam, UUID.randomUUID().toString());
        mvc.perform(MockMvcRequestBuilders.post(defaultLoginUrl)
                .params(params)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("Invalid credentials."));

    }

    @Test
    public void accessProtectedResourceWithoutToken() throws Exception {
         mvc.perform(MockMvcRequestBuilders.get("/api/v1/test"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("Authentication Failed: Token not present."));
    }

    @Test
    public void accessProtectedResourceWithInvalidToken() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/v1/test")
        .header(TokenAuthenticationFilter.HEADER_SECURITY_TOKEN, "abc-asas"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("Authentication Failed: Invalid token."));
    }

    @Test
    public void accessProtectedResourceNotCoveredByFilterWithoutToken() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/test"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("Unauthorized"));
    }
}