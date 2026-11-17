package ar.edu.utn.frc.tup.bda.turnos.repository;

import ar.edu.utn.frc.tup.bda.turnos.domain.EstadoTurno;
import ar.edu.utn.frc.tup.bda.turnos.domain.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    /**
     * RN-01: Detecta si existe algún turno en estado RESERVADO para el mismo peluquero
     * cuyo rango [fechaHoraInicio, fechaHoraFin) se solape con [inicio, fin).
     */
    @Query("""
            SELECT COUNT(t) > 0 FROM Turno t
            WHERE t.peluquero.id = :peluqueroId
              AND t.estado = :estado
              AND t.fechaHoraInicio < :fin
              AND t.fechaHoraFin > :inicio
            """)
    boolean existeSolapamientoHorario(
            @Param("peluqueroId") Long peluqueroId,
            @Param("estado") EstadoTurno estado,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    List<Turno> findByClienteIdOrderByFechaHoraInicioDesc(String clienteId);

    List<Turno> findAllByOrderByFechaHoraInicioDesc();
}

