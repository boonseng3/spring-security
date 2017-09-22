package com.obs.config;

import com.obs.security.filter.JwtAuthenticationFilter;
import com.obs.security.filter.LoginAuthenticationFilter;
import com.obs.security.handler.UsernamePasswordAuthenticationFailureHandler;
import com.obs.security.handler.UsernamePasswordAuthenticationSuccessHandler;
import com.obs.security.provider.TokenAuthenticationProvider;
import com.obs.security.service.TokenGenerationStrategy;
import com.obs.security.service.JwtTokenGenerationStrategy;
import com.obs.service.CustomUserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.security.KeyStore;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);
    @Autowired
    private CustomUserServiceImpl userDetailsService;

    @Value("${application.jwt.keystore.type}")
    private String jwtKeystoreType;
    @Value("${application.jwt.keystore.filename}")
    private String jwtKeystoreFilename;
    @Value("${application.jwt.keystore.alias}")
    private String jwtKeystoreAlias;
    @Value("${application.jwt.keystore.password}")
    private String jwtKeystorePassword;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .anonymous().disable();
        http.addFilterBefore(loginAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                // the default entry point will return 403, overrride to return some more appropriate
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint())
        ;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider())
                .authenticationProvider(tokenAuthenticationProvider());
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            logger.debug("Authentication failure", authException);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");

        };
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean()
            throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public UsernamePasswordAuthenticationSuccessHandler usernamePasswordAuthenticationSuccessHandler() {
        return new UsernamePasswordAuthenticationSuccessHandler();
    }

    @Bean
    public UsernamePasswordAuthenticationFailureHandler usernamePasswordAuthenticationFailureHandler() {
        return new UsernamePasswordAuthenticationFailureHandler();
    }


    public JwtAuthenticationFilter tokenAuthenticationFilter() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManagerBean());
        return filter;
    }


    public LoginAuthenticationFilter loginAuthenticationFilter() throws Exception {
        LoginAuthenticationFilter filter = new LoginAuthenticationFilter("/api/login");
        filter.setAuthenticationManager(authenticationManagerBean());
        filter.setAuthenticationSuccessHandler(usernamePasswordAuthenticationSuccessHandler());
        filter.setAuthenticationFailureHandler(usernamePasswordAuthenticationFailureHandler());
        return filter;
    }

    @Bean
    public TokenAuthenticationProvider tokenAuthenticationProvider() {
        return new TokenAuthenticationProvider();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TokenGenerationStrategy tokenGenerationStrategy() {
        return new JwtTokenGenerationStrategy();
    }

    @Bean
    public KeyStore jwtKeystore() {
        try {
            KeyStore jks = KeyStore.getInstance(jwtKeystoreType);
            jks.load(new FileInputStream(new ClassPathResource(jwtKeystoreFilename).getFile()), jwtKeystorePassword.toCharArray());
            return jks;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
