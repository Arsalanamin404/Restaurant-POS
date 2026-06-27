package com.arsalan.tenanttable.user.repository;

import com.arsalan.tenanttable.common.enums.PlatformRole;
import com.arsalan.tenanttable.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Boolean existsByEmail(String email);
    Boolean existsByPhoneNumber(String phoneNumber);
    Optional<User> findByEmail(String email);
    boolean existsByPlatformRole(PlatformRole platformRole);
}
