package ar.edu.utn.frc.tup.bda.premios.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_puntos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoPuntos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_puntos_id", nullable = false)
    private CuentaPuntos cuentaPuntos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "premio_id")
    private Premio premio;

    /**
     * Referencia lógica hacia el Turno en ms-turnos (sin clave foránea cruzada entre bases de datos).
     */
    @Column(name = "turno_id")
    private Long turnoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private Integer puntos;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
}

