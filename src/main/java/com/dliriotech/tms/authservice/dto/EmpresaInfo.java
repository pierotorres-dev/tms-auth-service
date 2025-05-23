package com.dliriotech.tms.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class EmpresaInfo {
    private Integer id;
    private String nombre;
    private String email;
}