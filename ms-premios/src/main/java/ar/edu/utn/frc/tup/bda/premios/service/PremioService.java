package ar.edu.utn.frc.tup.bda.premios.service;

import ar.edu.utn.frc.tup.bda.commons.dto.*;
import ar.edu.utn.frc.tup.bda.commons.service.EmailNotificationService;
import ar.edu.utn.frc.tup.bda.premios.domain.CuentaPuntos;
import ar.edu.utn.frc.tup.bda.premios.domain.MovimientoPuntos;
import ar.edu.utn.frc.tup.bda.premios.domain.Premio;
import ar.edu.utn.frc.tup.bda.premios.domain.TipoMovimiento;
import ar.edu.utn.frc.tup.bda.premios.dto.CuentaPuntosDetalleDTO;
import ar.edu.utn.frc.tup.bda.premios.dto.PremioRequestDTO;
import ar.edu.utn.frc.tup.bda.premios.repository.CuentaPuntosRepository;
import ar.edu.utn.frc.tup.bda.premios.repository.MovimientoPuntosRepository;
import ar.edu.utn.frc.tup.bda.premios.repository.PremioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PremioService {

    private final PremioRepository premioRepository;
    private final CuentaPuntosRepository cuentaPuntosRepository;
    private final MovimientoPuntosRepository movimientoPuntosRepository;
    private final EmailNotificationService emailNotificationService;

    @Transactional(readOnly = true)
    public List<Premio> listarPremiosDisponibles() {
        return premioRepository.findByActivoTrueOrderByPuntosRequeridosAsc();
    }

    @Transactional
    public Premio crearPremio(PremioRequestDTO dto) {
        Premio premio = Premio.builder()
                .nombre(dto.getNombre())
                .puntosRequeridos(dto.getPuntosRequeridos())
                .porcentajeDescuento(dto.getPorcentajeDescuento())
                .activo(dto.getActivo())
                .build();
        return premioRepository.save(premio);
    }

    @Transactional
    public Premio actualizarPremio(Long id, PremioRequestDTO dto) {
        Premio premio = premioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el premio con ID: " + id));
        premio.setNombre(dto.getNombre());
        premio.setPuntosRequeridos(dto.getPuntosRequeridos());
        premio.setPorcentajeDescuento(dto.getPorcentajeDescuento());
        premio.setActivo(dto.getActivo());
        return premioRepository.save(premio);
    }

    @Transactional(readOnly = true)
    public CuentaPuntosDetalleDTO consultarMiCuenta(String clienteId, String emailCliente) {
        return cuentaPuntosRepository.findByClienteId(clienteId)
                .map(cuenta -> {
                    List<CuentaPuntosDetalleDTO.MovimientoDetalleDTO> historial = movimientoPuntosRepository
                            .findByCuentaPuntosIdOrderByFechaHoraDesc(cuenta.getId())
                            .stream()
                            .map(m -> CuentaPuntosDetalleDTO.MovimientoDetalleDTO.builder()
                                    .id(m.getId())
                                    .tipo(m.getTipo())
                                    .puntos(m.getPuntos())
                                    .turnoId(m.getTurnoId())
                                    .nombrePremio(m.getPremio() != null ? m.getPremio().getNombre() : null)
                                    .fechaHora(m.getFechaHora())
                                    .build())
                            .toList();
                    return CuentaPuntosDetalleDTO.builder()
                            .clienteId(cuenta.getClienteId())
                            .emailCliente(cuenta.getEmailCliente())
                            .saldoPuntos(cuenta.getSaldoPuntos())
                            .fechaActualizacion(cuenta.getFechaActualizacion())
                            .historial(historial)
                            .build();
                })
                .orElseGet(() -> CuentaPuntosDetalleDTO.builder()
                        .clienteId(clienteId)
                        .emailCliente(emailCliente)
                        .saldoPuntos(0)
                        .fechaActualizacion(LocalDateTime.now())
                        .historial(Collections.emptyList())
                        .build());
    }

    /**
     * RN-03 / RN-04: Suficiencia de puntos para canje.
     * Valida que el premio esté activo y que el saldoPuntos >= puntosRequeridos.
     * Si no alcanza, rechaza con 422 Unprocessable Entity.
     */
    @Transactional
    public CanjePuntosResponseDTO canjearPremio(CanjePuntosRequestDTO request) {
        Premio premio = premioRepository.findById(request.getPremioId())
                .orElseThrow(() -> new EntityNotFoundException("El premio con ID " + request.getPremioId() + " no existe"));

        if (!Boolean.TRUE.equals(premio.getActivo())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "RN-03: El premio '" + premio.getNombre() + "' no se encuentra activo para canje");
        }

        CuentaPuntos cuenta = cuentaPuntosRepository.findByClienteId(request.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "RN-03: El cliente no posee una cuenta con puntos acumulados para realizar el canje"));

        if (cuenta.getSaldoPuntos() < premio.getPuntosRequeridos()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    String.format("RN-03: Saldo de puntos insuficiente. Saldo actual: %d, Puntos requeridos: %d",
                            cuenta.getSaldoPuntos(), premio.getPuntosRequeridos()));
        }

        int nuevoSaldo = cuenta.getSaldoPuntos() - premio.getPuntosRequeridos();
        cuenta.setSaldoPuntos(nuevoSaldo);
        cuenta.setEmailCliente(request.getEmailCliente());
        cuenta.setFechaActualizacion(LocalDateTime.now());
        cuentaPuntosRepository.save(cuenta);

        MovimientoPuntos movimiento = MovimientoPuntos.builder()
                .cuentaPuntos(cuenta)
                .premio(premio)
                .turnoId(request.getTurnoId())
                .tipo(TipoMovimiento.CANJE)
                .puntos(-premio.getPuntosRequeridos())
                .fechaHora(LocalDateTime.now())
                .build();
        movimiento = movimientoPuntosRepository.save(movimiento);

        // RN-06: Notificación por correo electrónico no bloqueante
        emailNotificationService.enviarNotificacionNoBloqueante(
                request.getEmailCliente(),
                "Comprobante de Canje de Premio - Peluquería",
                String.format("¡Hola! Canjeaste el premio '%s' (%s%% de descuento). " +
                              "Se descontaron %d puntos. Tu saldo restante es de %d puntos.",
                        premio.getNombre(), premio.getPorcentajeDescuento(),
                        premio.getPuntosRequeridos(), nuevoSaldo)
        );

        return CanjePuntosResponseDTO.builder()
                .movimientoId(movimiento.getId())
                .premioId(premio.getId())
                .nombrePremio(premio.getNombre())
                .puntosDescontados(premio.getPuntosRequeridos())
                .porcentajeDescuento(premio.getPorcentajeDescuento())
                .saldoRestante(nuevoSaldo)
                .build();
    }

    /**
     * RN-04: Acreditación exclusiva por turno completado.
     * Crea la cuenta si no existía, acredita los puntos del servicio y notifica por email.
     */
    @Transactional
    public AcreditacionPuntosResponseDTO acreditarPuntos(AcreditacionPuntosRequestDTO request) {
        if (movimientoPuntosRepository.existsByTurnoIdAndTipo(request.getTurnoId(), TipoMovimiento.ACREDITACION)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "RN-04: Los puntos para el turno ID " + request.getTurnoId() + " ya fueron acreditados previamente");
        }

        CuentaPuntos cuenta = cuentaPuntosRepository.findByClienteId(request.getClienteId())
                .orElseGet(() -> CuentaPuntos.builder()
                        .clienteId(request.getClienteId())
                        .emailCliente(request.getEmailCliente())
                        .saldoPuntos(0)
                        .fechaActualizacion(LocalDateTime.now())
                        .build());

        int nuevoSaldo = cuenta.getSaldoPuntos() + request.getPuntosQueOtorga();
        cuenta.setSaldoPuntos(nuevoSaldo);
        cuenta.setEmailCliente(request.getEmailCliente());
        cuenta.setFechaActualizacion(LocalDateTime.now());
        cuenta = cuentaPuntosRepository.save(cuenta);

        MovimientoPuntos movimiento = MovimientoPuntos.builder()
                .cuentaPuntos(cuenta)
                .turnoId(request.getTurnoId())
                .tipo(TipoMovimiento.ACREDITACION)
                .puntos(request.getPuntosQueOtorga())
                .fechaHora(LocalDateTime.now())
                .build();
        movimiento = movimientoPuntosRepository.save(movimiento);

        // RN-06: Notificación por correo electrónico no bloqueante
        emailNotificationService.enviarNotificacionNoBloqueante(
                request.getEmailCliente(),
                "¡Sumaste puntos en tu visita! - Peluquería",
                String.format("¡Gracias por tu visita! Por el turno #%d sumaste +%d puntos. " +
                              "Tu nuevo saldo disponible es de %d puntos.",
                        request.getTurnoId(), request.getPuntosQueOtorga(), nuevoSaldo)
        );

        return AcreditacionPuntosResponseDTO.builder()
                .movimientoId(movimiento.getId())
                .turnoId(request.getTurnoId())
                .puntosSumados(request.getPuntosQueOtorga())
                .nuevoSaldoDisponible(nuevoSaldo)
                .build();
    }

    /*
      RN-05: Reintegro por cancelación temprana de un turno con premio aplicado.
     */
    @Transactional
    public ReintegroPuntosResponseDTO reintegrarPuntos(ReintegroPuntosRequestDTO request) {
        if (movimientoPuntosRepository.existsByTurnoIdAndTipo(request.getTurnoId(), TipoMovimiento.REINTEGRO)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "RN-05: Los puntos del turno ID " + request.getTurnoId() + " ya fueron reintegrados");
        }

        Premio premio = premioRepository.findById(request.getPremioId())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el premio con ID: " + request.getPremioId()));

        CuentaPuntos cuenta = cuentaPuntosRepository.findByClienteId(request.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la cuenta de puntos del cliente: " + request.getClienteId()));

        int puntosAReintegrar = premio.getPuntosRequeridos();
        int nuevoSaldo = cuenta.getSaldoPuntos() + puntosAReintegrar;
        cuenta.setSaldoPuntos(nuevoSaldo);
        cuenta.setFechaActualizacion(LocalDateTime.now());
        cuentaPuntosRepository.save(cuenta);

        MovimientoPuntos movimiento = MovimientoPuntos.builder()  // se encarga de guardar el movimiento de reintegro en la BBDD
                .cuentaPuntos(cuenta)                            // esto evita en la linea 211 que se reintegren 2 veces los mismos puntos
                .premio(premio)
                .turnoId(request.getTurnoId())
                .tipo(TipoMovimiento.REINTEGRO)
                .puntos(puntosAReintegrar)
                .fechaHora(LocalDateTime.now())
                .build();
        movimiento = movimientoPuntosRepository.save(movimiento);

        // RN-06: Notificación por correo electrónico no bloqueante
        emailNotificationService.enviarNotificacionNoBloqueante(
                request.getEmailCliente(),
                "Reintegro de Puntos por Cancelación de Turno - Peluquería",
                String.format("Se canceló el turno #%d y se restituyeron +%d puntos correspondientes al premio '%s'. " +
                              "Tu saldo disponible es de %d puntos.",
                        request.getTurnoId(), puntosAReintegrar, premio.getNombre(), nuevoSaldo)
        );

        return ReintegroPuntosResponseDTO.builder()
                .movimientoId(movimiento.getId())
                .turnoId(request.getTurnoId())
                .puntosReintegrados(puntosAReintegrar)
                .nuevoSaldoDisponible(nuevoSaldo)
                .build();
    }
}

