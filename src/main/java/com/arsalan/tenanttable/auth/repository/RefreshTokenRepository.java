package com.arsalan.tenanttable.auth.repository;

import com.arsalan.tenanttable.auth.enitity.RefreshToken;
import com.arsalan.tenanttable.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUser(User user);

    void deleteByToken(String token);

    void deleteAllByUser(User user);
}
