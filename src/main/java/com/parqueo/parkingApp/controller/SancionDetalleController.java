package com.parqueo.parkingApp.controller;

import com.parqueo.parkingApp.dto.SancionDetalleDto;
import com.parqueo.parkingApp.model.SancionDetalle;
import com.parqueo.parkingApp.service.SancionDetalleService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sancion-detalles")
@RequiredArgsConstructor
public class SancionDetalleController {

    private final SancionDetalleService service;

    @GetMapping
    @PreAuthorize("hasAuthority('SANCION_DETALLE_LEER')")
    public ResponseEntity<List<SancionDetalleDto>> listar() {
        List<SancionDetalleDto> detalles = service.obtenerTodos();
        return ResponseEntity.ok(detalles);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SANCION_DETALLE_LEER')")
    public ResponseEntity<SancionDetalleDto> obtenerPorId(@PathVariable Long id) {
        try {
            SancionDetalleDto detalle = service.obtenerPorId(id);
            return ResponseEntity.ok(detalle);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SANCION_DETALLE_CREAR')")
    public ResponseEntity<?> crear(@Valid @RequestBody SancionDetalleDto dto) {
        try {
            SancionDetalleDto creado = service.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SANCION_DETALLE_EDITAR')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody SancionDetalleDto dto) {
        try {
            SancionDetalleDto actualizado = service.actualizar(id, dto);
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
    @PreAuthorize("hasAuthority('SANCION_DETALLE_ELIMINAR')")
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

    @GetMapping("/sancion/{sancionId}")
    @PreAuthorize("hasAuthority('SANCION_DETALLE_BUSCAR_POR_SANCION')")
    public ResponseEntity<List<SancionDetalleDto>> buscarPorSancion(@PathVariable Long sancionId) {
        try {
            List<SancionDetalleDto> detalles = service.buscarPorSancion(sancionId);
            return ResponseEntity.ok(detalles);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}