package com.arsalan.tenanttable.auth.repository;

import com.arsalan.tenanttable.auth.enitity.Otp;
import com.arsalan.tenanttable.auth.enums.OtpPurpose;
import com.arsalan.tenanttable.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OtpRepository extends JpaRepository<Otp, UUID> {
    //TODO: Fetch the most recent unused OTP for the given user and purpose
    Optional<Otp> findTopByUserAndPurposeAndUsedFalseOrderByCreatedAtDesc(
            User user,
            OtpPurpose purpose
    );

    List<Otp> findAllByUserAndPurposeAndUsedFalse(
            User user,
            OtpPurpose purpose
    );

    void deleteAllByExpiresAtBefore(Instant instant);
}
