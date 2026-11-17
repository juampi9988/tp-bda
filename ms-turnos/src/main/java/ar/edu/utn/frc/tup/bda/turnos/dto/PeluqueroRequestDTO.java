package ar.edu.utn.frc.tup.bda.turnos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeluqueroRequestDTO {
    @NotBlank(message = "El nombre del peluquero es obligatorio")
    private String nombre;

    @NotBlank(message = "El email de Google Calendar del peluquero es obligatorio")
    @Email(message = "El email de Google Calendar debe tener un formato válido")
    private String emailCalendar;

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;
}

