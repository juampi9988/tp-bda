package ar.edu.utn.frc.tup.bda.premios.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "premios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Premio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "puntos_requeridos", nullable = false)
    private Integer puntosRequeridos;

    @Column(name = "porcentaje_descuento", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeDescuento;

    @Column(nullable = false)
    private Boolean activo;
}

