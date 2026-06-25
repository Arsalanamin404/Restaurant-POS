package com.arsalan.tenanttable.auth.dto;

import com.arsalan.tenanttable.common.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class UserResponseDto {
    private UUID id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Role role;
    private Instant createdAt;
}