package com.codesync.auth.config;

import com.codesync.auth.entity.Provider;
import com.codesync.auth.entity.Role;
import com.codesync.auth.entity.User;
import com.codesync.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            log.info("Creating default admin user...");
            User admin = User.builder()
                    .username("admin")
                    .email("admin@codesync.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .provider(Provider.LOCAL)
                    .isActive(true)
                    .build();
            userRepository.save(admin);
            log.info("Default admin user created: admin / admin123");
        } else {
            // Ensure they have the ADMIN role in case they were registered normally
            User admin = userRepository.findByUsername("admin").get();
            if (admin.getRole() != Role.ADMIN) {
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
                log.info("Promoted 'admin' to ADMIN role.");
            }
        }
    }
}
