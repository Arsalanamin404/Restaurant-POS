package com.arsalan.tenanttable.auth.enitity;

import com.arsalan.tenanttable.auth.enums.OtpPurpose;
import com.arsalan.tenanttable.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "otps",
        indexes = {
                @Index(name = "idx_otp_user", columnList = "user_id"),
                @Index(name = "idx_otp_expires_at", columnList = "expiresAt")
        }
)

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Otp {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id",nullable = false)
        private User user;

        @Column(nullable = false)
        private String otpHash;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private OtpPurpose purpose;

        @Column(nullable = false)
        private Instant expiresAt;

        private Instant verifiedAt;

        @Builder.Default
        @Column(nullable = false)
        private int attempts = 0;

        @Builder.Default
        @Column(nullable = false)
        private int maxAttempts = 5;

        @Builder.Default
        @Column(nullable = false)
        private boolean used = false;

        @CreationTimestamp
        @Column(nullable = false, updatable = false)
        private Instant createdAt;
}
