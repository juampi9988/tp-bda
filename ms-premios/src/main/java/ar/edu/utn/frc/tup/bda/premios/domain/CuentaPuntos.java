package ar.edu.utn.frc.tup.bda.premios.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cuentas_puntos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaPuntos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false, unique = true)
    private String clienteId;

    @Column(name = "email_cliente", nullable = false)
    private String emailCliente;

    @Column(name = "saldo_puntos", nullable = false)
    private Integer saldoPuntos;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;
}

