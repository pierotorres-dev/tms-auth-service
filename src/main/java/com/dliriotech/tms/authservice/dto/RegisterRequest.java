package com.dliriotech.tms.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private Integer idEmpresa;
}