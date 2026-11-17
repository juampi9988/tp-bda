package ar.edu.utn.frc.tup.bda.commons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contrato de respuesta al acreditar puntos en ms-premios.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcreditacionPuntosResponseDTO {
    private Long movimientoId;
    private Long turnoId;
    private Integer puntosSumados;
    private Integer nuevoSaldoDisponible;
}

