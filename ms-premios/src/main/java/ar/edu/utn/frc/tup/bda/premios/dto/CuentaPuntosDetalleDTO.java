package ar.edu.utn.frc.tup.bda.premios.dto;

import ar.edu.utn.frc.tup.bda.premios.domain.TipoMovimiento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaPuntosDetalleDTO {
    private String clienteId;
    private String emailCliente;
    private Integer saldoPuntos;
    private LocalDateTime fechaActualizacion;
    private List<MovimientoDetalleDTO> historial;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovimientoDetalleDTO {
        private Long id;
        private TipoMovimiento tipo;
        private Integer puntos;
        private Long turnoId;
        private String nombrePremio;
        private LocalDateTime fechaHora;
    }
}

