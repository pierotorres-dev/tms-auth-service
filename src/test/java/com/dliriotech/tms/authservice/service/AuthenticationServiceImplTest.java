package com.dliriotech.tms.authservice.service;

import com.dliriotech.tms.authservice.dto.LoginRequest;
import com.dliriotech.tms.authservice.entity.AuthUser;
import com.dliriotech.tms.authservice.entity.UserEmpresa;
import com.dliriotech.tms.authservice.exception.InvalidCredentialsException;
import com.dliriotech.tms.authservice.repository.AuthUserRepository;
import com.dliriotech.tms.authservice.repository.UserEmpresaRepository;
import com.dliriotech.tms.authservice.security.cache.SessionTokenCache;
import com.dliriotech.tms.authservice.security.jwt.JwtProvider;
import com.dliriotech.tms.authservice.service.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private AuthUserRepository userRepository;

    @Mock
    private UserEmpresaRepository userEmpresaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private SessionTokenCache sessionTokenCache;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    void login_whenValidCredentialsAndSingleEmpresa_shouldReturnLoginResponseWithToken() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUserName("usuario");
        request.setPassword("password");

        AuthUser user = new AuthUser();
        user.setId(1);
        user.setUserName("usuario");
        user.setPassword("encoded");
        user.setRole("ADMIN");

        UserEmpresa userEmpresa = new UserEmpresa();
        userEmpresa.setUserId(1);
        userEmpresa.setEmpresaId(1);

        when(userRepository.findByUserName("usuario")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("password", "encoded")).thenReturn(true);
        when(userEmpresaRepository.findByUserId(1)).thenReturn(Flux.just(userEmpresa));
        when(jwtProvider.createTokenWithEmpresa(user, 1)).thenReturn("token");

        // Act
        StepVerifier.create(authenticationService.login(request))
                // Assert
                .expectNextMatches(response ->
                        response.getUserId().equals(1) &&
                                response.getToken().equals("token") &&
                                response.getRole().equals("ADMIN"))
                .verifyComplete();
    }

    @Test
    void login_whenInvalidCredentials_shouldReturnError() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUserName("usuario");
        request.setPassword("password");

        AuthUser user = new AuthUser();
        user.setPassword("encoded");

        when(userRepository.findByUserName("usuario")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("password", "encoded")).thenReturn(false);

        // Act & Assert
        StepVerifier.create(authenticationService.login(request))
                .expectError(InvalidCredentialsException.class)
                .verify();
    }
}