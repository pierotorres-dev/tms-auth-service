package com.dliriotech.tms.authservice.repository;

import com.dliriotech.tms.authservice.entity.Empresa;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface EmpresaRepository extends ReactiveCrudRepository<Empresa, Integer> {
}