package com.dliriotech.tms.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String userName;

    @NotBlank
    @Size(min = 6)
    private String password;

    @NotBlank
    private String role;

    @NotBlank
    private String name;

    @NotBlank
    private String lastName;

    @NotBlank
    @Size(min = 9)
    private String phoneNumber;

    private String email;
}