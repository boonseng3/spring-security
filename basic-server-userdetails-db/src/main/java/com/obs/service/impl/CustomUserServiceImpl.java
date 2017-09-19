package com.obs.service.impl;

import com.obs.entity.User;
import com.obs.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class CustomUserServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username).<RuntimeException>orElseThrow(() -> {
            throw new RuntimeException("Record not found.");
        });
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPassword(),
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));

    }
}
