package com.arsalan.tenanttable.bootstrap;

import com.arsalan.tenanttable.common.enums.PlatformRole;
import com.arsalan.tenanttable.user.entity.User;
import com.arsalan.tenanttable.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuperAdminSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${platform.admin.name}")
    private String fullName;

    @Value("${platform.admin.email}")
    private String email;

    @Value("${platform.admin.password}")
    private String password;

    @Value("${platform.admin.phone}")
    private String phoneNumber;

    @Override
    public void run(String @NonNull ... args) throws Exception {
        if (userRepository.existsByPlatformRole(PlatformRole.SUPER_ADMIN)) {
            log.info("Super Admin already exists. Skipping seeding.");
            return;
        }

        User superAdmin = User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .phoneNumber(phoneNumber)
                .platformRole(PlatformRole.SUPER_ADMIN)
                .emailVerified(true)
                .build();

        userRepository.save(superAdmin);

        log.info("Super Admin created successfully.");
    }
}
