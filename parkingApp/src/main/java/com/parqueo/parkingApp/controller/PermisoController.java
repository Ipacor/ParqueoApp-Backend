package com.parqueo.parkingApp.controller;

import com.parqueo.parkingApp.model.Permiso;
import com.parqueo.parkingApp.service.PermisoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permisos")
@RequiredArgsConstructor
public class PermisoController {

    private final PermisoService permisoService;

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<List<Permiso>> listarPermisos() {
        return ResponseEntity.ok(permisoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<Permiso> obtenerPermiso(@PathVariable Long id) {
        try {
            Permiso permiso = permisoService.obtenerPorId(id);
            return ResponseEntity.ok(permiso);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/nombre/{nombre}")
    @PreAuthorize("hasAuthority('PERMISO_BUSCAR_POR_NOMBRE')")
    public ResponseEntity<Permiso> obtenerPorNombre(@PathVariable String nombre) {
        return permisoService.obtenerPorNombre(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISO_CREAR')")
    public ResponseEntity<?> crearPermiso(@Valid @RequestBody Permiso permiso) {
        try {
            Permiso creado = permisoService.crear(permiso);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISO_EDITAR')")
    public ResponseEntity<?> actualizarPermiso(@PathVariable Long id, @Valid @RequestBody Permiso permiso) {
        try {
            Permiso actualizado = permisoService.actualizar(id, permiso);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISO_ELIMINAR')")
    public ResponseEntity<?> eliminarPermiso(@PathVariable Long id) {
        try {
            permisoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }
} 