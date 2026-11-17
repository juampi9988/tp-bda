package ar.edu.utn.frc.tup.bda.premios.controller;

import ar.edu.utn.frc.tup.bda.commons.dto.*;
import ar.edu.utn.frc.tup.bda.premios.domain.Premio;
import ar.edu.utn.frc.tup.bda.premios.dto.CuentaPuntosDetalleDTO;
import ar.edu.utn.frc.tup.bda.premios.dto.PremioRequestDTO;
import ar.edu.utn.frc.tup.bda.premios.service.PremioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/premios")
@RequiredArgsConstructor
public class PremioController {

    private final PremioService premioService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<List<Premio>> listarCatalogoPremios() {
        return ResponseEntity.ok(premioService.listarPremiosDisponibles());
    }

    @GetMapping("/mi-cuenta")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<CuentaPuntosDetalleDTO> consultarSaldoPropio(@AuthenticationPrincipal Jwt jwt) {
        String clienteId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        return ResponseEntity.ok(premioService.consultarMiCuenta(
                clienteId,
                email != null ? email : "cliente@peluqueria.com"
        ));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Premio> crearPremio(@Valid @RequestBody PremioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(premioService.crearPremio(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Premio> actualizarPremio(@PathVariable Long id, @Valid @RequestBody PremioRequestDTO dto) {
        return ResponseEntity.ok(premioService.actualizarPremio(id, dto));
    }

    // los siguientes son llamados por ms-turnos

    @PostMapping("/canjes")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<CanjePuntosResponseDTO> canjearPremio(@Valid @RequestBody CanjePuntosRequestDTO request) {
        return ResponseEntity.ok(premioService.canjearPremio(request));
    }

    @PostMapping("/acreditaciones")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AcreditacionPuntosResponseDTO> acreditarPuntos(@Valid @RequestBody AcreditacionPuntosRequestDTO request) {
        return ResponseEntity.ok(premioService.acreditarPuntos(request));
    }

    @PostMapping("/reintegros")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<ReintegroPuntosResponseDTO> reintegrarPuntos(@Valid @RequestBody ReintegroPuntosRequestDTO request) {
        return ResponseEntity.ok(premioService.reintegrarPuntos(request));
    }
}

