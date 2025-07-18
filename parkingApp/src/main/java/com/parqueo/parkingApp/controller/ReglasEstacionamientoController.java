package com.parqueo.parkingApp.controller;

import com.parqueo.parkingApp.dto.ReglasEstacionamientoDto;
import com.parqueo.parkingApp.model.ReglasEstacionamiento;
import com.parqueo.parkingApp.service.ReglasEstacionamientoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/reglas")
@RequiredArgsConstructor
public class ReglasEstacionamientoController {

    private final ReglasEstacionamientoService service;

    @GetMapping
    @PreAuthorize("hasAuthority('REGLA_LEER')")
    public ResponseEntity<List<ReglasEstacionamientoDto>> listar() {
        List<ReglasEstacionamientoDto> reglas = service.obtenerTodos();
        return ResponseEntity.ok(reglas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REGLA_LEER')")
    public ResponseEntity<ReglasEstacionamientoDto> obtener(@PathVariable Long id) {
        try {
            ReglasEstacionamientoDto regla = service.obtenerPorId(id);
            return ResponseEntity.ok(regla);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('REGLA_CREAR')")
    public ResponseEntity<?> crear(@Valid @RequestBody ReglasEstacionamientoDto dto) {
        try {
            ReglasEstacionamientoDto creada = service.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('REGLA_EDITAR')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody ReglasEstacionamientoDto dto) {
        try {
            ReglasEstacionamientoDto actualizada = service.actualizar(id, dto);
            return ResponseEntity.ok(actualizada);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('REGLA_ELIMINAR')")
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

    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasAuthority('REGLA_BUSCAR_POR_TIPO')")
    public ResponseEntity<List<ReglasEstacionamientoDto>> buscarPorTipo(@PathVariable String tipo) {
        try {
            List<ReglasEstacionamientoDto> reglas = service.buscarPorTipo(tipo);
            return ResponseEntity.ok(reglas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/activas")
    @PreAuthorize("hasAuthority('REGLA_LISTAR_ACTIVAS')")
    public ResponseEntity<List<ReglasEstacionamientoDto>> buscarActivas() {
        List<ReglasEstacionamientoDto> reglas = service.buscarActivas();
        return ResponseEntity.ok(reglas);
    }
}