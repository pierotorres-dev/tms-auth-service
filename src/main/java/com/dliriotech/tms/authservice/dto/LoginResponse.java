package com.dliriotech.tms.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class LoginResponse {
    private Integer userId;
    private String userName;
    private String role;
    private List<EmpresaInfo> empresas;
    private String token;
    private String refreshToken;
    private String sessionToken;
    private String name;
    private String lastName;
}