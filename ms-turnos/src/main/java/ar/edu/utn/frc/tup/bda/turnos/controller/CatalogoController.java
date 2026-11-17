package ar.edu.utn.frc.tup.bda.turnos.controller;

import ar.edu.utn.frc.tup.bda.turnos.domain.Peluquero;
import ar.edu.utn.frc.tup.bda.turnos.domain.Servicio;
import ar.edu.utn.frc.tup.bda.turnos.dto.PeluqueroRequestDTO;
import ar.edu.utn.frc.tup.bda.turnos.dto.ServicioRequestDTO;
import ar.edu.utn.frc.tup.bda.turnos.service.TurnoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CatalogoController {

    private final TurnoService turnoService;

    @GetMapping("/peluqueros")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<List<Peluquero>> listarPeluqueros() {
        return ResponseEntity.ok(turnoService.listarPeluquerosDisponibles());
    }

    @PostMapping("/peluqueros")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Peluquero> crearPeluquero(@Valid @RequestBody PeluqueroRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turnoService.crearPeluquero(dto));
    }

    @PutMapping("/peluqueros/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Peluquero> actualizarPeluquero(@PathVariable Long id, @Valid @RequestBody PeluqueroRequestDTO dto) {
        return ResponseEntity.ok(turnoService.actualizarPeluquero(id, dto));
    }

    @GetMapping("/servicios")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<List<Servicio>> listarServicios() {
        return ResponseEntity.ok(turnoService.listarServiciosDisponibles());
    }

    @PostMapping("/servicios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Servicio> crearServicio(@Valid @RequestBody ServicioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turnoService.crearServicio(dto));
    }

    @PutMapping("/servicios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Servicio> actualizarServicio(@PathVariable Long id, @Valid @RequestBody ServicioRequestDTO dto) {
        return ResponseEntity.ok(turnoService.actualizarServicio(id, dto));
    }
}

