package com.arsalan.tenanttable.user.dto;

import com.arsalan.tenanttable.common.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AllUsersResponseDto {
    private UUID id;
    private String fullName;
    private String email;
    private Role role;
    private String phoneNumber;

}
