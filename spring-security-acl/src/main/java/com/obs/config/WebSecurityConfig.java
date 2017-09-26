package com.obs.config;

import com.obs.security.CustomPermissionFactory;
import com.obs.security.domain.CustomPermission;
import com.obs.service.impl.CustomUserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomUserServiceImpl userDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/", "/home").permitAll()
                .anyRequest().authenticated()
                .and()
                .csrf().and()
                .formLogin()
                .permitAll()
                .and()
                .logout()
                .permitAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // ACL
    @Bean
    public MutableAclService aclService(@Autowired DataSource dataSource) throws PropertyVetoException {
        final JdbcMutableAclService mutableAclService = new JdbcMutableAclService(dataSource, lookupStrategy(dataSource), aclCache());
        mutableAclService.setClassIdentityQuery("SELECT LAST_INSERT_ID()");
        mutableAclService.setSidIdentityQuery("SELECT LAST_INSERT_ID()");
        return mutableAclService;
    }

    @Bean
    public BasicLookupStrategy lookupStrategy(@Autowired DataSource dataSource) throws PropertyVetoException {
        return new BasicLookupStrategy(dataSource, aclCache(), aclAuthorizationStrategy(), grantingStrategy());
    }

    @Bean
    public AclCache aclCache() {
        // TODO: How to handle distribute cache if need to scale
        return new EhCacheBasedAclCache(ehCacheManagerFactoryBean().getObject().getCache("aclCache"), grantingStrategy(), aclAuthorizationStrategy());
    }


    @Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
        EhCacheManagerFactoryBean cacheManagerFactoryBean = new EhCacheManagerFactoryBean();

        cacheManagerFactoryBean.setConfigLocation(new ClassPathResource("ehcache.xml"));
        cacheManagerFactoryBean.setShared(true);
        return cacheManagerFactoryBean;
    }


    @Bean
    public AclAuthorizationStrategy aclAuthorizationStrategy() {
        return new AclAuthorizationStrategyImpl(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Bean
    public PermissionGrantingStrategy grantingStrategy() {
        PermissionGrantingStrategy bean = new DefaultPermissionGrantingStrategy(auditLogger());
        return bean;
    }

    @Bean
    public AuditLogger auditLogger() {
        return new ConsoleAuditLogger();
    }

    @Bean
    public PermissionEvaluator permissionEvaluator(MutableAclService aclService) {
        AclPermissionEvaluator bean = new AclPermissionEvaluator(aclService);
        bean.setPermissionFactory(customPermissionFactory());
        return bean;
    }

    @Bean
    public PermissionFactory customPermissionFactory(){
        return new CustomPermissionFactory();
    }
}
