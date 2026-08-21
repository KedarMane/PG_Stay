package com.pgms.config;

import com.pgms.entity.Role;
import com.pgms.entity.User;
import com.pgms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Seeds a default Admin account on first startup so someone can log in initially.
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@pgms.com")) {
            User admin = User.builder()
                    .name("Super Admin")
                    .email("admin@pgms.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .profileCompleted(true)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            System.out.println(">>> Default admin created: admin@pgms.com / Admin@123 (CHANGE THIS PASSWORD)");
        }
    }
}
