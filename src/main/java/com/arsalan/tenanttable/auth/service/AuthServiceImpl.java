package com.arsalan.tenanttable.auth.service;

import com.arsalan.tenanttable.auth.dto.*;
import com.arsalan.tenanttable.auth.enitity.RefreshToken;
import com.arsalan.tenanttable.auth.enums.OtpPurpose;
import com.arsalan.tenanttable.auth.security.CustomUserDetails;
import com.arsalan.tenanttable.auth.security.jwt.JwtService;
import com.arsalan.tenanttable.common.enums.Role;
import com.arsalan.tenanttable.exception.*;
import com.arsalan.tenanttable.mail.IEmailService;
import com.arsalan.tenanttable.user.entity.User;
import com.arsalan.tenanttable.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final IRefreshTokenService refreshTokenService;
    private final IOtpService otpService;
    private final IEmailService emailService;

    @Override
    @Transactional
    public UserResponseDto register(RegisterRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ResourceAlreadyExistsException(
                    "User with email '" + dto.getEmail() + "' already exists"
            );
        }

        if (userRepository.existsByPhoneNumber(dto.getPhoneNumber())){
            throw new ResourceAlreadyExistsException(
                    "User with phone number '" + dto.getPhoneNumber() + "' already exists"
            );
        }

        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phoneNumber(dto.getPhoneNumber())
                .role(Role.MANAGER)
                .build();

        User savedUser = userRepository.save(user);

        otpService.generateOtp(savedUser, OtpPurpose.EMAIL_VERIFICATION);

        return UserResponseDto.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .phoneNumber(savedUser.getPhoneNumber())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequestDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException("Email is already verified.");
        }

        otpService.verifyOtp(user,dto.otp(),OtpPurpose.EMAIL_VERIFICATION);
        user.setEmailVerified(true);

        userRepository.save(user);
        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
    }

    @Override
    public void resendVerificationOtp(ResendOtpRequestDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (user.isEmailVerified())
            throw new EmailAlreadyVerifiedException("Email is already verified.");

        otpService.generateOtp(user, OtpPurpose.EMAIL_VERIFICATION);
    }

    @Override
    public AuthResponseDto login(LoginRequestDto dto, ClientInfo clientInfo) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(),dto.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (!userDetails.getUser().isEmailVerified()) {
            throw new EmailNotVerifiedException(
                    "Please verify your email before logging in."
            );
        }

        Map<String, Object> claims = new HashMap<>();

        claims.put("userId",userDetails.getUser().getId().toString());
        claims.put("role",userDetails.getUser().getRole().name());

        String accessToken = jwtService.generateAccessToken(claims,userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        refreshTokenService.createRefreshToken(
                userDetails.getUser(),
                refreshToken,
                clientInfo.ipAddress(),
                clientInfo.userAgent()
                );

        return AuthResponseDto
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

    }

    @Override
    public AuthResponseDto refreshToken(
            String token,
            ClientInfo clientInfo
    ) {
        RefreshToken storedToken = refreshTokenService.verifyRefreshToken(token);

        User user = storedToken.getUser();
        CustomUserDetails userDetails = new CustomUserDetails(user);

        if (!jwtService.validateToken(token, userDetails))
            throw new InvalidRefreshTokenException("Invalid or Expired refresh token.");

        Map<String, Object> claims = new HashMap<>();

        claims.put("userId", user.getId().toString());
        claims.put("role", user.getRole().name());

        String newAccessToken = jwtService.generateAccessToken(claims, userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        refreshTokenService.rotateRefreshToken(
                storedToken,
                newRefreshToken,
                clientInfo.ipAddress(),
                clientInfo.userAgent()
        );

        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }

    @Override
    public void logoutAll(User user) {
        refreshTokenService.revokeAllRefreshTokens(user);
    }
}