package com.arsalan.tenanttable.auth.controller;

import com.arsalan.tenanttable.auth.dto.*;
import com.arsalan.tenanttable.auth.security.CustomUserDetails;
import com.arsalan.tenanttable.auth.service.IAuthService;
import com.arsalan.tenanttable.common.dto.ApiResponse;
import com.arsalan.tenanttable.exception.InvalidRefreshTokenException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final IAuthService authService;

    @Value("${jwt.refresh-token.cookie-name}")
    private String refreshTokenCookieName;

    @Value("${jwt.refresh-expiry}")
    private long refreshTokenExpiration;

    @Value("${cookie.secure}")
    private boolean secureCookie;

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank())
            return forwarded.split(",")[0];

        return request.getRemoteAddr();
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null)
            throw new InvalidRefreshTokenException("Refresh token not found.");

        for (Cookie cookie : request.getCookies()) {
            if (refreshTokenCookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        throw new InvalidRefreshTokenException("Refresh token not found.");
    }

    private ResponseCookie buildRefreshCookie(String refreshToken) {
        return ResponseCookie.from(refreshTokenCookieName, refreshToken)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/api/v1/auth/refresh")
                .maxAge(Duration.ofMillis(refreshTokenExpiration))
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(refreshTokenCookieName, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/api/v1/auth/refresh")
                .maxAge(Duration.ZERO)
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> register(
            @Valid @RequestBody RegisterRequestDto dto, HttpServletRequest request) {

        UserResponseDto user = authService.register(dto);

        ApiResponse<UserResponseDto> response =
                ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "Registration successful. Please verify your email",
                        user,
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequestDto dto,
            HttpServletRequest request
    ) {
        authService.verifyEmail(dto);
        ApiResponse<Void> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Email verified successfully.",
                null,
                request.getRequestURI()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerificationOtp(
            @Valid @RequestBody ResendOtpRequestDto dto,
            HttpServletRequest request
    ) {
        authService.resendVerificationOtp(dto);
        ApiResponse<Void> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Verification email sent successfully.",
                null,
                request.getRequestURI()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody LoginRequestDto dto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        ClientInfo clientInfo = new ClientInfo(getClientIp(request), request.getHeader("User-Agent"));
        AuthResponseDto auth = authService.login(dto,clientInfo);

        ResponseCookie cookie = buildRefreshCookie(auth.getRefreshToken());

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        auth.setRefreshToken(null);

        ApiResponse<AuthResponseDto> apiResponse = ApiResponse.success(
                HttpStatus.OK.value(),
                "Login Successful",
                auth,
                request.getRequestURI()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = extractRefreshToken(request);

        ClientInfo clientInfo = new ClientInfo(
                getClientIp(request),
                request.getHeader("User-Agent")
        );

        AuthResponseDto auth = authService.refreshToken(refreshToken, clientInfo);

        ResponseCookie cookie = buildRefreshCookie(auth.getRefreshToken());

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        auth.setRefreshToken(null);

        ApiResponse<AuthResponseDto> apiResponse = ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Token refreshed successfully",
                        auth,
                        request.getRequestURI()
                );

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ){
        String refreshToken = extractRefreshToken(request);
        authService.logout(refreshToken);

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                clearRefreshCookie().toString()
        );

        ApiResponse<Void> apiResponse = ApiResponse.success(
                HttpStatus.OK.value(),
                "Logged out successfully",
                null,
                request.getRequestURI()
        );

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutFromAllDevices(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        authService.logoutAll(userDetails.getUser());

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                clearRefreshCookie().toString()
        );

        ApiResponse<Void> apiResponse = ApiResponse.success(
                HttpStatus.OK.value(),
                "Logged out from all devices",
                null,
                request.getRequestURI()
        );

        return ResponseEntity.ok(apiResponse);
    }
}
