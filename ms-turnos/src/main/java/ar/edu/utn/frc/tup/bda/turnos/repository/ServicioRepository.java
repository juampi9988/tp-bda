package ar.edu.utn.frc.tup.bda.turnos.repository;

import ar.edu.utn.frc.tup.bda.turnos.domain.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    List<Servicio> findByActivoTrueOrderByIdAsc();
}

