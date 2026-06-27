package com.arsalan.tenanttable.auth.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiry}")
    private long accessTokenExpiry;

    @Value("${jwt.refresh-expiry}")
    private long refreshTokenExpiry;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
    }


    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public <T> T extractClaims(
            String token,
            Function<Claims, T> claimsResolver
    ) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private String generateToken(
            Map<String, Object> claims,
            UserDetails userDetails,
            long expiry
    ) {
        return Jwts
                .builder()
                .id(UUID.randomUUID().toString())
                .issuer("tenant-table-api")
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiry))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateAccessToken(
            Map<String, Object> claims,
            UserDetails userDetails
    ) {
        return generateToken(
                claims,
                userDetails,
                accessTokenExpiry
        );
    }

    public String generateRefreshToken(
            UserDetails userDetails
    ) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "refresh");

        return generateToken(
                claims,
                userDetails,
                refreshTokenExpiry
        );
    }

    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername());
    }

    public String extractRole(String token) {
        return extractClaims(token,
                claims -> claims.get("tenantRole", String.class));
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(
                extractClaims(token,
                        claims -> claims.get("userId", String.class))
        );
    }
}
