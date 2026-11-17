package ar.edu.utn.frc.tup.bda.premios.repository;

import ar.edu.utn.frc.tup.bda.premios.domain.CuentaPuntos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CuentaPuntosRepository extends JpaRepository<CuentaPuntos, Long> {
    Optional<CuentaPuntos> findByClienteId(String clienteId);
}

