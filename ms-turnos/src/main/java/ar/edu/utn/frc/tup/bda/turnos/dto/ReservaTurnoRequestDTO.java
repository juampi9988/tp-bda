package ar.edu.utn.frc.tup.bda.turnos.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaTurnoRequestDTO {
    @NotNull(message = "El ID del peluquero es obligatorio")
    private Long peluqueroId;

    @NotNull(message = "El ID del servicio es obligatorio")
    private Long servicioId;

    @NotNull(message = "La fecha y hora de inicio son obligatorias")
    @Future(message = "RN-01: La fecha y hora del turno deben ser futuras")
    private LocalDateTime fechaHoraInicio;

    /**
     * Opcional: ID del premio a canjear en ms-premios.
     */
    private Long premioId;
}

