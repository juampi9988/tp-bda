package ar.edu.utn.frc.tup.bda.premios.dto;

import jakarta.validation.constraints.DecimalMax;
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
public class PremioRequestDTO {
    @NotBlank(message = "El nombre del premio es obligatorio")
    private String nombre;

    @NotNull(message = "Los puntos requeridos son obligatorios")
    @Positive(message = "Los puntos requeridos deben ser mayores a 0")
    private Integer puntosRequeridos;

    @NotNull(message = "El porcentaje de descuento es obligatorio")
    @DecimalMin(value = "1.00", message = "El descuento mínimo es 1%")
    @DecimalMax(value = "100.00", message = "El descuento máximo es 100%")
    private BigDecimal porcentajeDescuento;

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;
}

