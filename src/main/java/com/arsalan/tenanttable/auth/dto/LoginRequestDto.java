package com.arsalan.tenanttable.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {
    @NotBlank(message = "email is required")
    @Email(message = "please enter a valid email")
    private String email;

    @NotBlank(message = "password is required")
    private String password;

}
