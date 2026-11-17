package ar.edu.utn.frc.tup.bda.premios.repository;

import ar.edu.utn.frc.tup.bda.premios.domain.Premio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PremioRepository extends JpaRepository<Premio, Long> {
    List<Premio> findByActivoTrueOrderByPuntosRequeridosAsc();
}

