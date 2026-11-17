package ar.edu.utn.frc.tup.bda.turnos.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "turnos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private String clienteId;

    @Column(name = "email_cliente", nullable = false)
    private String emailCliente;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "peluquero_id", nullable = false)
    private Peluquero peluquero;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin", nullable = false)
    private LocalDateTime fechaHoraFin;

    /**
     * Referencia lógica hacia el Premio canjeado en ms-premios (nullable).
     */
    @Column(name = "premio_canjeado_id")
    private Long premioCanjeadoId;

    @Column(name = "porcentaje_descuento_aplicado", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeDescuentoAplicado;

    @Column(name = "precio_final", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioFinal;

    @Column(name = "google_event_id")
    private String googleEventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTurno estado;
}

