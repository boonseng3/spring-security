package com.obs.repo;

import com.obs.Application;
import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@FlywayTest
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableJpaRepositories(basePackages = {"com.obs.repo"})
@EntityScan(basePackages = {"com.obs.entity"})
@DataJpaTest
@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestExecutionListeners(mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS,
        listeners = {FlywayTestExecutionListener.class})
public class RoleRepoTest {
    @Autowired
    private RoleRepo roleRepo;

    @Test
    @Transactional
    public void findByRole() throws Exception {
        assertThat(roleRepo.findByRole("ROLE_USER")).hasValueSatisfying(role -> {
            assertThat(role).hasFieldOrPropertyWithValue("role", "ROLE_USER")
                    .hasFieldOrPropertyWithValue("description", null);
        });

    }

    @Test
    @Transactional
    public void findByInvalidRole() throws Exception {
        assertThat(roleRepo.findByRole(UUID.randomUUID().toString())).isEmpty();

    }

}