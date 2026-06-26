package com.arsalan.tenanttable.auth.service;

import com.arsalan.tenanttable.auth.enitity.Otp;
import com.arsalan.tenanttable.auth.enums.OtpPurpose;
import com.arsalan.tenanttable.auth.repository.OtpRepository;
import com.arsalan.tenanttable.exception.InvalidOtpException;
import com.arsalan.tenanttable.exception.OtpAttemptsExceededException;
import com.arsalan.tenanttable.mail.IEmailService;
import com.arsalan.tenanttable.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OtpServiceImpl implements IOtpService{
    @Value("${otp.expiry}")
    private long otpExpiry;

    @Value("${otp.length}")
    private int otpLength;

    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;
    private static final SecureRandom RANDOM = new SecureRandom();

    private String generateOtpCode(int length){
        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            otp.append(RANDOM.nextInt(10));

        return otp.toString();
    }

    private void invalidatePreviousOtps(User user, OtpPurpose purpose){
        List<Otp> activeOtps = otpRepository
                .findAllByUserAndPurposeAndUsedFalse(user,purpose);
        activeOtps.forEach(otp -> otp.setUsed(true));
        otpRepository.saveAll(activeOtps);
    }

    private Otp createOtp(User user, String otp, OtpPurpose purpose) {
        return Otp.builder()
                .user(user)
                .otpHash(passwordEncoder.encode(otp))
                .purpose(purpose)
                .expiresAt(Instant.now().plusSeconds(otpExpiry))
                .build();
    }

    private void sendOtpEmail(User user, String otp, OtpPurpose purpose) {
        long expiryMinutes = otpExpiry / 60;
        switch (purpose) {
            case EMAIL_VERIFICATION ->
                    emailService.sendVerificationEmail(
                            user.getEmail(),
                            user.getFullName(),
                            otp,
                            expiryMinutes
                    );

            case RESET_PASSWORD ->
                    emailService.sendPasswordResetEmail(
                            user.getEmail(),
                            user.getFullName(),
                            otp,
                            expiryMinutes
                    );

            default ->
                    emailService.sendOtpEmail(
                            user.getEmail(),
                            user.getFullName(),
                            otp,
                            expiryMinutes
                    );
        }
    }

    @Override
    @Transactional
    public void generateOtp(User user, OtpPurpose purpose) {
        invalidatePreviousOtps(user, purpose);

        String otp = generateOtpCode(otpLength);
        Otp otpEntity = createOtp(user, otp, purpose);
        otpRepository.save(otpEntity);

        sendOtpEmail(user,otp,purpose);
    }

    @Override
    @Transactional
    public void verifyOtp(User user, String otp, OtpPurpose purpose) {
        Otp storedOtp = otpRepository
                .findTopByUserAndPurposeAndUsedFalseOrderByCreatedAtDesc(user, purpose)
                .orElseThrow(() ->
                        new InvalidOtpException("Invalid or expired OTP"));

        if (storedOtp.getExpiresAt().isBefore(Instant.now())) {
            storedOtp.setUsed(true);
            throw new InvalidOtpException("Invalid or expired OTP");
        }

        if (!passwordEncoder.matches(otp, storedOtp.getOtpHash())) {
            storedOtp.setAttempts(storedOtp.getAttempts() + 1);
            if (storedOtp.getAttempts() >= storedOtp.getMaxAttempts()) {
                storedOtp.setUsed(true);
                throw new OtpAttemptsExceededException(
                        "Maximum OTP verification attempts exceeded."
                );
            }
            throw new InvalidOtpException("Invalid or expired OTP");
        }

        storedOtp.setUsed(true);
        storedOtp.setVerifiedAt(Instant.now());
        otpRepository.save(storedOtp);
    }

    @Override
    public void resendOtp(User user, OtpPurpose purpose) {
        generateOtp(user,purpose);
    }
}
