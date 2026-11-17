package ar.edu.utn.frc.tup.bda.commons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contrato de respuesta de ms-premios tras restituir los puntos por cancelación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReintegroPuntosResponseDTO {
    private Long movimientoId;
    private Long turnoId;
    private Integer puntosReintegrados;
    private Integer nuevoSaldoDisponible;
}

