package com.orderflow.orderservice.config;

import com.orderflow.orderservice.entity.Role;
import com.orderflow.orderservice.entity.User;
import com.orderflow.orderservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail("admin@orderflow.com")) {
            log.info("Admin user already exists, skipping...");
            return;
        }

        User admin = User.builder()
                .email("admin@orderflow.com")
                .password(passwordEncoder.encode("admin123"))
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);
        log.info("Admin user seeded: admin@orderflow.com / admin123");
    }
}
