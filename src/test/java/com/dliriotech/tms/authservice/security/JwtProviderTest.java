package com.dliriotech.tms.authservice.security;

import com.dliriotech.tms.authservice.entity.AuthUser;
import com.dliriotech.tms.authservice.security.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        // Para pruebas, usamos una clave secreta fija y tiempo de expiración corto
        jwtProvider = new JwtProvider();
        // Usar reflection para establecer los valores privados
        ReflectionTestUtils.setField(jwtProvider, "secret", "claveSecretaParaPruebasDeAlMenos32Caracteres");
        ReflectionTestUtils.setField(jwtProvider, "expirationMs", 3600000L);
    }

    @Test
    void validate_withValidToken_shouldReturnTrue() {
        // Arrange
        AuthUser user = new AuthUser();
        user.setId(1);
        user.setUserName("usuario");
        user.setRole("admin");

        Integer idEmpresa = 1;

        String token = jwtProvider.createTokenWithEmpresa(user, idEmpresa);

        // Act & Assert
        assertTrue(jwtProvider.validate(token));
    }
}