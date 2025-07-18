package com.parqueo.parkingApp.controller;

import com.parqueo.parkingApp.dto.EspacioDisponibleDto;
import com.parqueo.parkingApp.model.EspacioDisponible;
import com.parqueo.parkingApp.service.EspacioDisponibleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/espacios")
@RequiredArgsConstructor
public class EspacioDisponibleController {

    private final EspacioDisponibleService servicio;

    @GetMapping
    @PreAuthorize("hasAuthority('ESPACIO_LEER')")
    public ResponseEntity<List<EspacioDisponibleDto>> listar() {
        return ResponseEntity.ok(servicio.obtenerTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ESPACIO_LEER')")
    public ResponseEntity<EspacioDisponibleDto> obtener(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(servicio.obtenerPorId(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAuthority('ESPACIO_BUSCAR_POR_ESTADO')")
    public ResponseEntity<List<EspacioDisponibleDto>> buscarPorEstado(@PathVariable EspacioDisponible.EstadoEspacio estado) {
        try {
            return ResponseEntity.ok(servicio.buscarPorEstado(estado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/zona/{zona}")
    @PreAuthorize("hasAuthority('ESPACIO_BUSCAR_POR_ZONA')")
    public ResponseEntity<List<EspacioDisponibleDto>> buscarPorZona(@PathVariable String zona) {
        try {
            return ResponseEntity.ok(servicio.buscarPorZona(zona));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/disponibles")
    @PreAuthorize("hasAuthority('ESPACIO_LISTAR_DISPONIBLES')")
    public ResponseEntity<List<EspacioDisponibleDto>> buscarDisponibles() {
        return ResponseEntity.ok(servicio.buscarDisponibles());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ESPACIO_CREAR')")
    public ResponseEntity<EspacioDisponibleDto> crear(@Valid @RequestBody EspacioDisponibleDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ESPACIO_EDITAR')")
    public ResponseEntity<EspacioDisponibleDto> actualizar(@PathVariable Long id, @Valid @RequestBody EspacioDisponibleDto dto) {
        return ResponseEntity.ok(servicio.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ESPACIO_ELIMINAR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
