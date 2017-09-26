package com.obs.controller;

import com.obs.dto.Profile;
import com.obs.repo.ProfileRepo;
import com.obs.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class ApiController {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ProfileRepo profileRepo;

    /**
     * User is allowed to access their own profile.
     * Admin is allowed to access all user profile.
     *
     * @param id
     * @return
     */
    @PreAuthorize("hasPermission(#id, 'com.obs.dto.Profile', 'GET_PROFILE') || hasPermission(0, 'com.obs.dto.Profile', 'GET_PROFILE')")
    @GetMapping("/profile/{id}")
    public Profile profile(@PathVariable Long id) {
        return Optional.ofNullable(profileRepo.findOne(id))
                .map(profile1 -> new Profile().setName(profile1.getFirstName()
                        + (StringUtils.hasText(profile1.getLastName()) ? " " + profile1.getLastName() : "")))
                .<RuntimeException>orElseThrow(() -> {
                    throw new RuntimeException("Profile record not found.");
                });
    }
}
