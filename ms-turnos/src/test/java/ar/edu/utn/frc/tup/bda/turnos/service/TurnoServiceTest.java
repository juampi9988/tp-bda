package ar.edu.utn.frc.tup.bda.turnos.service;

import ar.edu.utn.frc.tup.bda.commons.dto.CanjePuntosResponseDTO;
import ar.edu.utn.frc.tup.bda.commons.service.EmailNotificationService;
import ar.edu.utn.frc.tup.bda.turnos.client.GoogleCalendarClient;
import ar.edu.utn.frc.tup.bda.turnos.client.PremiosClient;
import ar.edu.utn.frc.tup.bda.turnos.domain.EstadoTurno;
import ar.edu.utn.frc.tup.bda.turnos.domain.Peluquero;
import ar.edu.utn.frc.tup.bda.turnos.domain.Servicio;
import ar.edu.utn.frc.tup.bda.turnos.domain.Turno;
import ar.edu.utn.frc.tup.bda.turnos.dto.ReservaTurnoRequestDTO;
import ar.edu.utn.frc.tup.bda.turnos.repository.PeluqueroRepository;
import ar.edu.utn.frc.tup.bda.turnos.repository.ServicioRepository;
import ar.edu.utn.frc.tup.bda.turnos.repository.TurnoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurnoServiceTest {

    @Mock
    private PeluqueroRepository peluqueroRepository;
    @Mock
    private ServicioRepository servicioRepository;
    @Mock
    private TurnoRepository turnoRepository;
    @Mock
    private PremiosClient premiosClient;
    @Mock
    private GoogleCalendarClient googleCalendarClient;
    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private TurnoService turnoService;

    private Peluquero crearPeluqueroActivo() {
        return Peluquero.builder()
                .id(1L)
                .nombre("Mateo Rossi")
                .emailCalendar("mateo.rossi@peluqueria.utn.edu.ar")
                .activo(true)
                .build();
    }

    private Servicio crearServicioActivo() {
        return Servicio.builder()
                .id(1L)
                .nombre("Corte Clásico")
                .duracionMinutos(30)
                .precio(new BigDecimal("10000.00"))
                .puntosQueOtorga(100)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("RN-01: Rechaza reserva fuera de la franja horaria laboral (09:00 a 20:00 hs)")
    void reservarTurno_fueraDeHorarioLaboral_lanza400() {
        when(peluqueroRepository.findById(1L)).thenReturn(Optional.of(crearPeluqueroActivo()));
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(crearServicioActivo()));

        LocalDateTime fueraDeHorario = LocalDate.now().plusDays(1).atTime(21, 0);

        ReservaTurnoRequestDTO request = ReservaTurnoRequestDTO.builder()
                .peluqueroId(1L)
                .servicioId(1L)
                .fechaHoraInicio(fueraDeHorario)
                .build();

        assertThatThrownBy(() -> turnoService.reservarTurno(request, "sub-1", "c@test.com", "token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("RN-01: Rechaza reserva cuando existe solapamiento horario con otro turno RESERVADO")
    void reservarTurno_conSolapamientoInterno_lanza409() {
        when(peluqueroRepository.findById(1L)).thenReturn(Optional.of(crearPeluqueroActivo()));
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(crearServicioActivo()));
        when(turnoRepository.existeSolapamientoHorario(eq(1L), eq(EstadoTurno.RESERVADO), any(), any()))
                .thenReturn(true);

        LocalDateTime horarioValido = LocalDate.now().plusDays(1).atTime(10, 0);

        ReservaTurnoRequestDTO request = ReservaTurnoRequestDTO.builder()
                .peluqueroId(1L)
                .servicioId(1L)
                .fechaHoraInicio(horarioValido)
                .build();

        assertThatThrownBy(() -> turnoService.reservarTurno(request, "sub-1", "c@test.com", "token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("Flujo 1 + RN-03: Reserva con canje de premio aplica porcentajeDescuento al precioFinal")
    void reservarTurno_conCanjeExitoso_calculaPrecioFinalConDescuento() {
        when(peluqueroRepository.findById(1L)).thenReturn(Optional.of(crearPeluqueroActivo()));
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(crearServicioActivo()));
        when(turnoRepository.existeSolapamientoHorario(any(), any(), any(), any())).thenReturn(false);
        when(premiosClient.solicitarCanjePremio(any(), eq("token")))
                .thenReturn(CanjePuntosResponseDTO.builder()
                        .premioId(2L)
                        .porcentajeDescuento(new BigDecimal("25.00"))
                        .puntosDescontados(250)
                        .saldoRestante(50)
                        .build());
        when(googleCalendarClient.agendarEventoEnCalendar(any(), any(), any(), any(), any()))
                .thenReturn("gcal-123");
        when(turnoRepository.save(any(Turno.class))).thenAnswer(inv -> {
            Turno t = inv.getArgument(0);
            t.setId(100L);
            return t;
        });

        LocalDateTime horarioValido = LocalDate.now().plusDays(1).atTime(11, 0);
        ReservaTurnoRequestDTO request = ReservaTurnoRequestDTO.builder()
                .peluqueroId(1L)
                .servicioId(1L)
                .fechaHoraInicio(horarioValido)
                .premioId(2L)
                .build();

        Turno turno = turnoService.reservarTurno(request, "sub-1", "c@test.com", "token");

        assertThat(turno.getEstado()).isEqualTo(EstadoTurno.RESERVADO);
        assertThat(turno.getPorcentajeDescuentoAplicado()).isEqualByComparingTo("25.00");
        assertThat(turno.getPrecioFinal()).isEqualByComparingTo("7500.00");
        assertThat(turno.getGoogleEventId()).isEqualTo("gcal-123");
    }

    @Test
    @DisplayName("Un cliente no puede cancelar el turno de otro cliente (403 Forbidden)")
    void cancelarTurno_otroCliente_lanza403() {
        Turno turnoAjeno = Turno.builder()
                .id(50L)
                .clienteId("sub-duenio-original")
                .estado(EstadoTurno.RESERVADO)
                .build();
        when(turnoRepository.findById(50L)).thenReturn(Optional.of(turnoAjeno));

        assertThatThrownBy(() -> turnoService.cancelarTurno(50L, "sub-intruso", false, "token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }
}

