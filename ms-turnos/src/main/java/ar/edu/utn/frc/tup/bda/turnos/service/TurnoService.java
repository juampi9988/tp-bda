package ar.edu.utn.frc.tup.bda.turnos.service;

import ar.edu.utn.frc.tup.bda.commons.dto.*;
import ar.edu.utn.frc.tup.bda.commons.service.EmailNotificationService;
import ar.edu.utn.frc.tup.bda.turnos.client.GoogleCalendarClient;
import ar.edu.utn.frc.tup.bda.turnos.client.PremiosClient;
import ar.edu.utn.frc.tup.bda.turnos.domain.EstadoTurno;
import ar.edu.utn.frc.tup.bda.turnos.domain.Peluquero;
import ar.edu.utn.frc.tup.bda.turnos.domain.Servicio;
import ar.edu.utn.frc.tup.bda.turnos.domain.Turno;
import ar.edu.utn.frc.tup.bda.turnos.dto.PeluqueroRequestDTO;
import ar.edu.utn.frc.tup.bda.turnos.dto.ReservaTurnoRequestDTO;
import ar.edu.utn.frc.tup.bda.turnos.dto.ServicioRequestDTO;
import ar.edu.utn.frc.tup.bda.turnos.repository.PeluqueroRepository;
import ar.edu.utn.frc.tup.bda.turnos.repository.ServicioRepository;
import ar.edu.utn.frc.tup.bda.turnos.repository.TurnoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TurnoService {

    private static final LocalTime HORARIO_APERTURA = LocalTime.of(9, 0);
    private static final LocalTime HORARIO_CIERRE = LocalTime.of(20, 0);

    private final PeluqueroRepository peluqueroRepository;
    private final ServicioRepository servicioRepository;
    private final TurnoRepository turnoRepository;
    private final PremiosClient premiosClient;
    private final GoogleCalendarClient googleCalendarClient;
    private final EmailNotificationService emailNotificationService;

    // =========================================================================
    // ABMC de Peluqueros y Servicios
    // =========================================================================

    @Transactional(readOnly = true)
    public List<Peluquero> listarPeluquerosDisponibles() {
        return peluqueroRepository.findByActivoTrueOrderByIdAsc();
    }

    @Transactional
    public Peluquero crearPeluquero(PeluqueroRequestDTO dto) {
        return peluqueroRepository.save(Peluquero.builder()
                .nombre(dto.getNombre())
                .emailCalendar(dto.getEmailCalendar())
                .activo(dto.getActivo())
                .build());
    }

    @Transactional
    public Peluquero actualizarPeluquero(Long id, PeluqueroRequestDTO dto) {
        Peluquero peluquero = peluqueroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el peluquero con ID: " + id));
        peluquero.setNombre(dto.getNombre());
        peluquero.setEmailCalendar(dto.getEmailCalendar());
        peluquero.setActivo(dto.getActivo());
        return peluqueroRepository.save(peluquero);
    }

    @Transactional(readOnly = true)
    public List<Servicio> listarServiciosDisponibles() {
        return servicioRepository.findByActivoTrueOrderByIdAsc();
    }

    @Transactional
    public Servicio crearServicio(ServicioRequestDTO dto) {
        return servicioRepository.save(Servicio.builder()
                .nombre(dto.getNombre())
                .duracionMinutos(dto.getDuracionMinutos())
                .precio(dto.getPrecio())
                .puntosQueOtorga(dto.getPuntosQueOtorga())
                .activo(dto.getActivo())
                .build());
    }

    @Transactional
    public Servicio actualizarServicio(Long id, ServicioRequestDTO dto) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el servicio con ID: " + id));
        servicio.setNombre(dto.getNombre());
        servicio.setDuracionMinutos(dto.getDuracionMinutos());
        servicio.setPrecio(dto.getPrecio());
        servicio.setPuntosQueOtorga(dto.getPuntosQueOtorga());
        servicio.setActivo(dto.getActivo());
        return servicioRepository.save(servicio);
    }

    // =========================================================================
    // Procesos Centrales de Negocio
    // =========================================================================

    @Transactional(readOnly = true)
    public List<Turno> listarTurnos(String clienteId, boolean esAdmin) {
        if (esAdmin) {
            return turnoRepository.findAllByOrderByFechaHoraInicioDesc();
        }
        return turnoRepository.findByClienteIdOrderByFechaHoraInicioDesc(clienteId);
    }

    /**
     * Flujo 1: Reservar Turno (Colaboración con ms-premios + Google Calendar API + SMTP).
     */
    @Transactional
    public Turno reservarTurno(ReservaTurnoRequestDTO request, String clienteId, String emailCliente, String bearerToken) {
        Peluquero peluquero = peluqueroRepository.findById(request.getPeluqueroId())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el peluquero con ID: " + request.getPeluqueroId()));
        if (!Boolean.TRUE.equals(peluquero.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El peluquero seleccionado no se encuentra activo");
        }

        Servicio servicio = servicioRepository.findById(request.getServicioId())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el servicio con ID: " + request.getServicioId()));
        if (!Boolean.TRUE.equals(servicio.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El servicio seleccionado no se encuentra activo");
        }

        LocalDateTime inicio = request.getFechaHoraInicio();
        LocalDateTime fin = inicio.plusMinutes(servicio.getDuracionMinutos());

        // RN-01: Validar franja horaria laboral del salón (09:00 a 20:00 hs) y mismo día
        if (!inicio.toLocalDate().equals(fin.toLocalDate())
                || inicio.toLocalTime().isBefore(HORARIO_APERTURA)
                || fin.toLocalTime().isAfter(HORARIO_CIERRE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "RN-01: El turno debe desarrollarse dentro de la franja horaria de atención del local (09:00 a 20:00 hs)");
        }

        // RN-01: Validar no solapamiento con otros turnos RESERVADO en db-turnos
        if (turnoRepository.existeSolapamientoHorario(peluquero.getId(), EstadoTurno.RESERVADO, inicio, fin)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "RN-01: El peluquero ya posee un turno RESERVADO que se superpone con el rango horario solicitado");
        }

        // RN-02: Consultar disponibilidad en tiempo real en Google Calendar (antes de efectuar cambios)
        googleCalendarClient.verificarDisponibilidadExterna(peluquero.getEmailCalendar(), inicio, fin);

        // RN-03: Si el cliente indicó un premioId, invocar sincrónicamente a ms-premios para validar saldo y descontar puntos
        BigDecimal porcentajeDescuento = BigDecimal.ZERO;
        if (request.getPremioId() != null) {
            CanjePuntosResponseDTO canjeResponse = premiosClient.solicitarCanjePremio(
                    CanjePuntosRequestDTO.builder()
                            .clienteId(clienteId)
                            .emailCliente(emailCliente)
                            .premioId(request.getPremioId())
                            .build(),
                    bearerToken
            );
            porcentajeDescuento = canjeResponse.getPorcentajeDescuento();
        }

        // Calcular precioFinal = precioBase * (1 - porcentajeDescuento / 100)
        BigDecimal factorDescuento = BigDecimal.ONE.subtract(
                porcentajeDescuento.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
        );
        BigDecimal precioFinal = servicio.getPrecio()
                .multiply(factorDescuento)
                .setScale(2, RoundingMode.HALF_UP);

        // Agendar evento en Google Calendar luego de haber validado y asegurado el canje
        String googleEventId = googleCalendarClient.agendarEventoEnCalendar(
                peluquero.getEmailCalendar(),
                emailCliente,
                servicio.getNombre(),
                inicio,
                fin
        );

        // Crea el objeto Turno y lo guarda en la BBDD con el descuento (si aplica) y el precio 
        Turno turno = Turno.builder()
                .clienteId(clienteId)
                .emailCliente(emailCliente)
                .peluquero(peluquero)
                .servicio(servicio)
                .fechaHoraInicio(inicio)
                .fechaHoraFin(fin)
                .premioCanjeadoId(request.getPremioId())
                .porcentajeDescuentoAplicado(porcentajeDescuento)
                .precioFinal(precioFinal)
                .googleEventId(googleEventId)
                .estado(EstadoTurno.RESERVADO)
                .build();
        turno = turnoRepository.save(turno);

        // RN-06: Notificación por correo electrónico no bloqueante
        emailNotificationService.enviarNotificacionNoBloqueante(
                emailCliente,
                "Confirmación de Reserva de Turno #" + turno.getId() + " - Peluquería",
                String.format("¡Hola! Tu turno #%d con %s para el servicio '%s' fue reservado para el %s. " +
                              "Descuento aplicado: %s%%. Precio final a abonar: $%s.",
                        turno.getId(), peluquero.getNombre(), servicio.getNombre(),
                        inicio, porcentajeDescuento, precioFinal)
        );

        return turno;
    }

    /**
     * Flujo 2: Completar Turno y Sumar Puntos (Colaboración con ms-premios).
     */
    @Transactional
    public Turno completarTurno(Long turnoId, String bearerToken) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el turno con ID: " + turnoId));

        // RN-04: Solo un turno en estado RESERVADO puede pasar a COMPLETADO
        if (turno.getEstado() != EstadoTurno.RESERVADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "RN-04: Solo un turno en estado RESERVADO puede completarse. Estado actual: " + turno.getEstado());
        }

        turno.setEstado(EstadoTurno.COMPLETADO);
        turno = turnoRepository.save(turno);

        // Invocar sincrónicamente a ms-premios para acreditar los puntos del servicio
        premiosClient.solicitarAcreditacionPuntos(
                AcreditacionPuntosRequestDTO.builder()
                        .clienteId(turno.getClienteId())
                        .emailCliente(turno.getEmailCliente())
                        .turnoId(turno.getId())
                        .puntosQueOtorga(turno.getServicio().getPuntosQueOtorga())
                        .build(),
                bearerToken
        );

        return turno;
    }

    /**
     * Flujo 3: Cancelar Turno y Reintegro Condicional de Puntos (Colaboración Condicional + Google Calendar + SMTP).
     */
    @Transactional
    public Turno cancelarTurno(Long turnoId, String solicitanteId, boolean esAdmin, String bearerToken) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el turno con ID: " + turnoId));

        // Un cliente solo puede cancelar sus propias reservas
        if (!esAdmin && !turno.getClienteId().equals(solicitanteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tenés permisos para cancelar un turno perteneciente a otro cliente");
        }

        // RN-04: Solo un turno en estado RESERVADO puede pasar a CANCELADO
        if (turno.getEstado() != EstadoTurno.RESERVADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "RN-04: Solo un turno en estado RESERVADO puede cancelarse. Estado actual: " + turno.getEstado());
        }

        turno.setEstado(EstadoTurno.CANCELADO);
        turno = turnoRepository.save(turno);

        // RN-05: Si el turno tenía un premio canjeado asociado, restituir los puntos en ms-premios
        if (turno.getPremioCanjeadoId() != null) {
            premiosClient.solicitarReintegroPuntos(
                    ReintegroPuntosRequestDTO.builder()
                            .clienteId(turno.getClienteId())
                            .emailCliente(turno.getEmailCliente())
                            .turnoId(turno.getId())
                            .premioId(turno.getPremioCanjeadoId())
                            .build(),
                    bearerToken
            );
        }

        // Liberar el bloque en Google Calendar
        googleCalendarClient.eliminarEventoEnCalendar(turno.getPeluquero().getEmailCalendar(), turno.getGoogleEventId());

        // RN-06: Notificación por correo electrónico no bloqueante
        emailNotificationService.enviarNotificacionNoBloqueante(
                turno.getEmailCliente(),
                "Cancelación de Turno #" + turno.getId() + " - Peluquería",
                String.format("Tu turno #%d con %s del día %s ha sido CANCELADO.",
                        turno.getId(), turno.getPeluquero().getNombre(), turno.getFechaHoraInicio())
        );

        return turno;
    }
}

