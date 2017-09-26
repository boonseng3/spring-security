package com.obs.repo;

import com.obs.entity.Profile;
import com.obs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepo extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUser(User user);
}
