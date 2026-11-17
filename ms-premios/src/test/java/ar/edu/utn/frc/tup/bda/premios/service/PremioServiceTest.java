package ar.edu.utn.frc.tup.bda.premios.service;

import ar.edu.utn.frc.tup.bda.commons.dto.*;
import ar.edu.utn.frc.tup.bda.commons.service.EmailNotificationService;
import ar.edu.utn.frc.tup.bda.premios.domain.CuentaPuntos;
import ar.edu.utn.frc.tup.bda.premios.domain.MovimientoPuntos;
import ar.edu.utn.frc.tup.bda.premios.domain.Premio;
import ar.edu.utn.frc.tup.bda.premios.domain.TipoMovimiento;
import ar.edu.utn.frc.tup.bda.premios.repository.CuentaPuntosRepository;
import ar.edu.utn.frc.tup.bda.premios.repository.MovimientoPuntosRepository;
import ar.edu.utn.frc.tup.bda.premios.repository.PremioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PremioServiceTest {

    @Mock
    private PremioRepository premioRepository;
    @Mock
    private CuentaPuntosRepository cuentaPuntosRepository;
    @Mock
    private MovimientoPuntosRepository movimientoPuntosRepository;
    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private PremioService premioService;

    @Test
    @DisplayName("RN-03: Canjear premio con saldo suficiente descuenta puntos y devuelve porcentajeDescuento")
    void canjearPremio_conSaldoSuficiente_descuentaPuntosYRetornaDescuento() {
        Premio premio = Premio.builder()
                .id(1L)
                .nombre("Descuento Plata 25%")
                .puntosRequeridos(250)
                .porcentajeDescuento(new BigDecimal("25.00"))
                .activo(true)
                .build();

        CuentaPuntos cuenta = CuentaPuntos.builder()
                .id(10L)
                .clienteId("sub-cliente-1")
                .emailCliente("cliente1@peluqueria.com")
                .saldoPuntos(300)
                .fechaActualizacion(LocalDateTime.now())
                .build();

        when(premioRepository.findById(1L)).thenReturn(Optional.of(premio));
        when(cuentaPuntosRepository.findByClienteId("sub-cliente-1")).thenReturn(Optional.of(cuenta));
        when(movimientoPuntosRepository.save(any(MovimientoPuntos.class))).thenAnswer(inv -> {
            MovimientoPuntos m = inv.getArgument(0);
            m.setId(99L);
            return m;
        });

        CanjePuntosResponseDTO response = premioService.canjearPremio(CanjePuntosRequestDTO.builder()
                .clienteId("sub-cliente-1")
                .emailCliente("cliente1@peluqueria.com")
                .premioId(1L)
                .build());

        assertThat(response.getPorcentajeDescuento()).isEqualByComparingTo("25.00");
        assertThat(response.getPuntosDescontados()).isEqualTo(250);
        assertThat(response.getSaldoRestante()).isEqualTo(50);
        verify(cuentaPuntosRepository).save(cuenta);
    }

    @Test
    @DisplayName("RN-03: Canjear premio con saldo insuficiente rechaza con 422 Unprocessable Entity")
    void canjearPremio_conSaldoInsuficiente_lanza422() {
        Premio premio = Premio.builder()
                .id(2L)
                .nombre("Corte Mitad de Precio 50%")
                .puntosRequeridos(500)
                .porcentajeDescuento(new BigDecimal("50.00"))
                .activo(true)
                .build();

        CuentaPuntos cuenta = CuentaPuntos.builder()
                .id(10L)
                .clienteId("sub-cliente-1")
                .emailCliente("cliente1@peluqueria.com")
                .saldoPuntos(100)
                .fechaActualizacion(LocalDateTime.now())
                .build();

        when(premioRepository.findById(2L)).thenReturn(Optional.of(premio));
        when(cuentaPuntosRepository.findByClienteId("sub-cliente-1")).thenReturn(Optional.of(cuenta));

        assertThatThrownBy(() -> premioService.canjearPremio(CanjePuntosRequestDTO.builder()
                .clienteId("sub-cliente-1")
                .emailCliente("cliente1@peluqueria.com")
                .premioId(2L)
                .build()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
    }

    @Test
    @DisplayName("RN-04: Acreditar puntos por el mismo turno dos veces rechaza con 409 Conflict")
    void acreditarPuntos_turnoYaAcreditado_lanza409() {
        when(movimientoPuntosRepository.existsByTurnoIdAndTipo(55L, TipoMovimiento.ACREDITACION))
                .thenReturn(true);

        assertThatThrownBy(() -> premioService.acreditarPuntos(AcreditacionPuntosRequestDTO.builder()
                .clienteId("sub-cliente-1")
                .emailCliente("cliente1@peluqueria.com")
                .turnoId(55L)
                .puntosQueOtorga(100)
                .build()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }
}

