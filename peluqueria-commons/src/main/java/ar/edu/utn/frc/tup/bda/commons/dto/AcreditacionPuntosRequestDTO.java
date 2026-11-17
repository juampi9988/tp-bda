package ar.edu.utn.frc.tup.bda.commons.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contrato de solicitud para acreditar puntos cuando un turno pasa a COMPLETADO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcreditacionPuntosRequestDTO {
    @NotBlank(message = "El clienteId (sub de Keycloak) es obligatorio")
    private String clienteId;

    @NotBlank(message = "El email del cliente es obligatorio")
    @Email(message = "El formato del email del cliente es inválido")
    private String emailCliente;

    @NotNull(message = "El turnoId es obligatorio para trazabilidad")
    private Long turnoId;

    @NotNull(message = "Los puntos a acreditar son obligatorios")
    @Positive(message = "Los puntos a acreditar deben ser mayores a cero")
    private Integer puntosQueOtorga;
}

