package com.parqueo.parkingApp.controller;

import com.parqueo.parkingApp.dto.HistorialUsoDto;
import com.parqueo.parkingApp.service.HistorialUsoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/historial-uso")
@RequiredArgsConstructor
public class HistorialUsoController {

    private final HistorialUsoService service;

    @GetMapping
    @PreAuthorize("hasAuthority('HISTORIAL_LEER')")
    public ResponseEntity<List<HistorialUsoDto>> listar() {
        List<HistorialUsoDto> historiales = service.obtenerTodos();
        return ResponseEntity.ok(historiales);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('HISTORIAL_LEER')")
    public ResponseEntity<HistorialUsoDto> obtenerPorId(@PathVariable Long id) {
        try {
            HistorialUsoDto historial = service.obtenerPorId(id);
            return ResponseEntity.ok(historial);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('HISTORIAL_CREAR')")
    public ResponseEntity<?> crear(@Valid @RequestBody HistorialUsoDto dto) {
        try {
            HistorialUsoDto creado = service.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HISTORIAL_EDITAR')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody HistorialUsoDto dto) {
        try {
            HistorialUsoDto actualizado = service.actualizar(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HISTORIAL_ELIMINAR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            service.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAuthority('HISTORIAL_BUSCAR_POR_USUARIO')")
    public ResponseEntity<List<HistorialUsoDto>> buscarPorUsuario(@PathVariable Long usuarioId) {
        try {
            List<HistorialUsoDto> historiales = service.buscarPorUsuario(usuarioId);
            return ResponseEntity.ok(historiales);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/espacio/{espacioId}")
    @PreAuthorize("hasAuthority('HISTORIAL_BUSCAR_POR_ESPACIO')")
    public ResponseEntity<List<HistorialUsoDto>> buscarPorEspacio(@PathVariable Long espacioId) {
        try {
            List<HistorialUsoDto> historiales = service.buscarPorEspacio(espacioId);
            return ResponseEntity.ok(historiales);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/fecha")
    @PreAuthorize("hasAuthority('HISTORIAL_BUSCAR_POR_FECHA')")
    public ResponseEntity<List<HistorialUsoDto>> buscarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            List<HistorialUsoDto> historiales = service.buscarPorFecha(fechaInicio, fechaFin);
            return ResponseEntity.ok(historiales);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/activos")
    @PreAuthorize("hasAuthority('HISTORIAL_LISTAR_ACTIVOS')")
    public ResponseEntity<List<HistorialUsoDto>> buscarUsosActivos() {
        List<HistorialUsoDto> historiales = service.buscarUsosActivos();
        return ResponseEntity.ok(historiales);
    }
}
