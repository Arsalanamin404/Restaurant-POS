package com.arsalan.tenanttable.auth.service;

import com.arsalan.tenanttable.auth.enitity.RefreshToken;
import com.arsalan.tenanttable.user.entity.User;


public interface IRefreshTokenService {

    RefreshToken createRefreshToken(
            User user,
            String token,
            String ipAddress,
            String userAgent
    );

    RefreshToken rotateRefreshToken(
            RefreshToken refreshToken,
            String newToken,
            String ipAddress,
            String userAgent
    );

    RefreshToken verifyRefreshToken(String token);

    void revokeRefreshToken(String token);

    void revokeAllRefreshTokens(User user);

    void delete(RefreshToken refreshToken);

}