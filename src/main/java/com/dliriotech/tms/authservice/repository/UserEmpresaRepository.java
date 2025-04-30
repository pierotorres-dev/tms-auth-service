package com.dliriotech.tms.authservice.repository;

import com.dliriotech.tms.authservice.entity.UserEmpresa;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserEmpresaRepository extends ReactiveCrudRepository<UserEmpresa, Integer> {
    Flux<UserEmpresa> findByUserId(Integer userId);
    Mono<UserEmpresa> findByUserIdAndEmpresaId(Integer userId, Integer empresaId);
    Mono<Void> deleteByUserIdAndEmpresaId(Integer userId, Integer empresaId);
}