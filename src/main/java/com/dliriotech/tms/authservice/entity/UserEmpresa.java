package com.dliriotech.tms.authservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("user_empresas")
public class UserEmpresa {

    @Column("user_id")
    private Integer userId;

    @Column("empresa_id")
    private Integer empresaId;
}