package ar.edu.utn.frc.tup.bda.turnos.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "peluqueros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Peluquero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "email_calendar", nullable = false, unique = true)
    private String emailCalendar;

    @Column(nullable = false)
    private Boolean activo;
}

