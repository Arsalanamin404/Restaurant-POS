package com.arsalan.tenanttable.auth.service;

import com.arsalan.tenanttable.auth.enitity.RefreshToken;
import com.arsalan.tenanttable.auth.repository.RefreshTokenRepository;
import com.arsalan.tenanttable.exception.InvalidRefreshTokenException;
import com.arsalan.tenanttable.exception.ResourceNotFoundException;
import com.arsalan.tenanttable.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements IRefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiry}")
    private long refreshTokenExpiry;

    @Override
    public RefreshToken createRefreshToken(User user, String token, String ipAddress, String userAgent) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiry))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .revoked(false)
                .lastUsedAt(Instant.now())
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken rotateRefreshToken(
            RefreshToken refreshToken,
            String newToken,
            String ipAddress,
            String userAgent
    ) {
        refreshToken.setToken(newToken);
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshTokenExpiry));
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setUserAgent(userAgent);
        refreshToken.setRevoked(false);
        refreshToken.setLastUsedAt(Instant.now());
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional()
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

        if (refreshToken.isRevoked())
            throw new InvalidRefreshTokenException("Refresh token has been revoked.");

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            // Delete expired tokens
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidRefreshTokenException("Refresh token has expired.");
        }

        return refreshToken;
    }

    @Override
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = verifyRefreshToken(token);

        if (!refreshToken.isRevoked()) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        }
    }

    @Override
    public void revokeAllRefreshTokens(User user) {
        List<RefreshToken> refreshTokens = refreshTokenRepository.findByUser(user);
        refreshTokens.forEach(token -> token.setRevoked(true));
        refreshTokenRepository.saveAll(refreshTokens);
    }

    @Override
    public void delete(RefreshToken refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken.getToken());
    }
}
