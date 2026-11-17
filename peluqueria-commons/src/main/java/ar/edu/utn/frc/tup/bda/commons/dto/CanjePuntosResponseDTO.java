package ar.edu.utn.frc.tup.bda.commons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Contrato de respuesta de ms-premios tras procesar un canje de puntos exitoso.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanjePuntosResponseDTO {
    private Long movimientoId;
    private Long premioId;
    private String nombrePremio;
    private Integer puntosDescontados;
    private BigDecimal porcentajeDescuento;
    private Integer saldoRestante;
}

