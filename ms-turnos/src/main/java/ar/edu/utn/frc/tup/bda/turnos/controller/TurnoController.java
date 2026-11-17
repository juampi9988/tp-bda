package ar.edu.utn.frc.tup.bda.turnos.controller;

import ar.edu.utn.frc.tup.bda.turnos.domain.Turno;
import ar.edu.utn.frc.tup.bda.turnos.dto.ReservaTurnoRequestDTO;
import ar.edu.utn.frc.tup.bda.turnos.service.TurnoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")  // El cliente solo ve sus turnos, el admin, todos
    public ResponseEntity<List<Turno>> listarTurnos(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication) {
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(turnoService.listarTurnos(jwt.getSubject(), esAdmin));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")   // Reserva un turno -> 201
    public ResponseEntity<Turno> reservarTurno(
            @Valid @RequestBody ReservaTurnoRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        String clienteId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        Turno turno = turnoService.reservarTurno(
                request,
                clienteId,
                email != null ? email : "cliente@peluqueria.com",
                jwt.getTokenValue()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(turno);
    }

    @PatchMapping("/{id}/completar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Turno> completarTurno(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(turnoService.completarTurno(id, jwt.getTokenValue()));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<Turno> cancelarTurno(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication) {
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(turnoService.cancelarTurno(
                id,
                jwt.getSubject(),
                esAdmin,
                jwt.getTokenValue()
        ));
    }
}

