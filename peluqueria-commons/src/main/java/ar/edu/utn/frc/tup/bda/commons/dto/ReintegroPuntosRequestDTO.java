package ar.edu.utn.frc.tup.bda.commons.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contrato de solicitud para reintegrar puntos cuando un turno con premio canjeado se cancela.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReintegroPuntosRequestDTO {
    @NotBlank(message = "El clienteId es obligatorio")
    private String clienteId;

    @NotBlank(message = "El email del cliente es obligatorio")
    @Email(message = "El formato del email del cliente es inválido")
    private String emailCliente;

    @NotNull(message = "El turnoId es obligatorio")
    private Long turnoId;

    @NotNull(message = "El premioId original es obligatorio para el reintegro")
    private Long premioId;
}

