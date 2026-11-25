package com.finance.dashboard.config;

import com.finance.dashboard.entity.User;
import com.finance.dashboard.enums.Role;
import com.finance.dashboard.enums.UserStatus;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Solves the chicken-and-egg problem: only ADMINs can create users, so the very first ADMIN
 * has to come from somewhere. If (and only if) the users table is empty and
 * BOOTSTRAP_ADMIN_PASSWORD is set, one ADMIN account is created at startup.
 * Remove the variable after the first run.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements ApplicationRunner {

    private final BootstrapAdminProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        if (!StringUtils.hasText(properties.password())) {
            log.warn("No users exist and BOOTSTRAP_ADMIN_PASSWORD is not set. "
                    + "Set it (and optionally BOOTSTRAP_ADMIN_USERNAME / BOOTSTRAP_ADMIN_EMAIL) "
                    + "and restart to create the first ADMIN account.");
            return;
        }
        if (properties.password().length() < 8) {
            throw new IllegalStateException("BOOTSTRAP_ADMIN_PASSWORD must be at least 8 characters");
        }

        User admin = User.builder()
                .username(properties.username().trim().toLowerCase())
                .email(properties.email().trim().toLowerCase())
                .password(passwordEncoder.encode(properties.password()))
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        try {
            userRepository.save(admin);
            log.info("Bootstrap ADMIN '{}' created. Remove BOOTSTRAP_ADMIN_PASSWORD from the environment now.",
                    admin.getUsername());
        } catch (DataIntegrityViolationException e) {
            // Another instance won the race; nothing to do.
            log.info("Bootstrap ADMIN already created by another instance.");
        }
    }
}
