package com.obs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obs.security.filter.JwtAuthenticationFilter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Autowired
    private KeyStore jwtKeystore;

    @Value("${application.jwt.keystore.type}")
    private String jwtKeystoreType;
    @Value("${application.jwt.keystore.filename}")
    private String jwtKeystoreFilename;
    @Value("${application.jwt.keystore.alias}")
    private String jwtKeystoreAlias;
    @Value("${application.jwt.keystore.password}")
    private String jwtKeystorePassword;

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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andReturn();

        String jws = result.getResponse().getContentAsString();
        String[] jwsTokens = StringUtils.delimitedListToStringArray(jws, ".");
        String header = new String(Base64.getDecoder().decode(jwsTokens[0].getBytes(StandardCharsets.UTF_8)));
        String payload = new String(Base64.getDecoder().decode(jwsTokens[1].getBytes(StandardCharsets.UTF_8)));
        String signature = jwsTokens[2];

        System.out.println("header = " + header);
        System.out.println("payload = " + payload);
        System.out.println("signature = " + signature);

        assertThat(header).isEqualTo("{\"alg\":\"RS256\"}");
        assertThat(payload).isEqualTo("{\"sub\":\"user\"}");

        Key key = jwtKeystore.getKey(jwtKeystoreAlias, jwtKeystorePassword.toCharArray());
        Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(jws);

        assertThat(claims.getHeader()).containsEntry("alg", "RS256");
        assertThat(claims.getBody()).containsEntry("sub", "user");
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
    public void accessProtectedResourceWithValidToken() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set(defaultUsernameParam, defaultUsername);
        params.set(defaultPasswordParam, defaultPassword);
        MvcResult result = mvc.perform(MockMvcRequestBuilders.post(defaultLoginUrl)
                .params(params)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andReturn();

        String jws = result.getResponse().getContentAsString();

        mvc.perform(MockMvcRequestBuilders.get("/api/v1/test").header(JwtAuthenticationFilter.HEADER_SECURITY_TOKEN, "Bearer " + jws))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("{\"token\": \"test\"}"));

        mvc.perform(MockMvcRequestBuilders.get("/test").header(JwtAuthenticationFilter.HEADER_SECURITY_TOKEN, "Bearer " + jws))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("{\"token\": \"test\"}"));
    }

    @Test
    public void accessProtectedResourceWithoutToken() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/v1/test"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("Unauthorized"));
    }

    @Test
    public void accessProtectedResourceWithInvalidToken() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/v1/test")
                .header(JwtAuthenticationFilter.HEADER_SECURITY_TOKEN, "Bearer asfdsadas.sadasd.adad"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("Unauthorized"));
    }

    @Test
    public void accessProtectedResourceNotCoveredByFilterWithoutToken() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/test"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("Unauthorized"));
    }
}