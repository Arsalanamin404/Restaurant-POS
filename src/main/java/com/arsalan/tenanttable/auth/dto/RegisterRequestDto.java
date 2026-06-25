package com.arsalan.tenanttable.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDto {
    @NotBlank(message = "fullName is required")
    private String fullName;

    @NotBlank(message = "email is required")
    @Email(message = "please enter a valid email")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    private String password;

    @NotBlank(message = "phoneNumber is required")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Please enter a valid 10 digit phone number"
    )
    private String phoneNumber;
}
