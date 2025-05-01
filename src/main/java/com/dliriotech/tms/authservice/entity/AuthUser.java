package com.dliriotech.tms.authservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("auth_user")
public class AuthUser {

    @Id
    private Integer id;

    @Column("user_name")
    private String userName;

    private String password;

    private String role;

    private String name;

    @Column("last_name")
    private String lastName;

    @Column("phone_number")
    private String phoneNumber;

    private String email;
}