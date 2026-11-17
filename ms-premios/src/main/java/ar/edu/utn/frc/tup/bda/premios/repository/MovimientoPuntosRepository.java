package ar.edu.utn.frc.tup.bda.premios.repository;

import ar.edu.utn.frc.tup.bda.premios.domain.MovimientoPuntos;
import ar.edu.utn.frc.tup.bda.premios.domain.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovimientoPuntosRepository extends JpaRepository<MovimientoPuntos, Long> {
    List<MovimientoPuntos> findByCuentaPuntosIdOrderByFechaHoraDesc(Long cuentaPuntosId);
    boolean existsByTurnoIdAndTipo(Long turnoId, TipoMovimiento tipo);
    Optional<MovimientoPuntos> findFirstByCuentaPuntosClienteIdAndPremioIdAndTipoOrderByFechaHoraDesc(
            String clienteId, Long premioId, TipoMovimiento tipo);
}

