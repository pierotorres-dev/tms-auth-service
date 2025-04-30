package com.dliriotech.tms.authservice.repository;

import com.dliriotech.tms.authservice.entity.AuthUser;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface AuthUserRepository extends ReactiveCrudRepository<AuthUser, Integer> {
    Mono<AuthUser> findByUserName(String userName);
}