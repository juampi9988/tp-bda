package ar.edu.utn.frc.tup.bda.turnos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicioRequestDTO {
    @NotBlank(message = "El nombre del servicio es obligatorio")
    private String nombre;

    @NotNull(message = "La duración en minutos es obligatoria")
    @Positive(message = "La duración en minutos debe ser mayor a cero")
    private Integer duracionMinutos;

    @NotNull(message = "El precio base es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    private BigDecimal precio;

    @NotNull(message = "Los puntos que otorga el servicio son obligatorios")
    @Positive(message = "Los puntos que otorga deben ser mayores a cero")
    private Integer puntosQueOtorga;

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;
}

