package ar.edu.utn.frc.tup.bda.turnos.repository;

import ar.edu.utn.frc.tup.bda.turnos.domain.Peluquero;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PeluqueroRepository extends JpaRepository<Peluquero, Long> {
    List<Peluquero> findByActivoTrueOrderByIdAsc();
}

