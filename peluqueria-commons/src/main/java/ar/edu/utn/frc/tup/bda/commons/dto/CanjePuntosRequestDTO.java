package ar.edu.utn.frc.tup.bda.commons.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contrato de solicitud para canjear un premio desde ms-turnos hacia ms-premios.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanjePuntosRequestDTO {
    @NotBlank(message = "El clienteId (sub de Keycloak) es obligatorio")
    private String clienteId;

    @NotBlank(message = "El email del cliente es obligatorio")
    @Email(message = "El formato del email del cliente es inválido")
    private String emailCliente;

    @NotNull(message = "El ID del premio a canjear es obligatorio")
    private Long premioId;

    private Long turnoId;
}

