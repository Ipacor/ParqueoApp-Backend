package com.parqueo.parkingApp.controller;

import com.parqueo.parkingApp.model.Notificacion;
import com.parqueo.parkingApp.service.NotificacionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService service;

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAuthority('NOTIFICACION_LEER')")
    public ResponseEntity<List<Notificacion>> obtenerNotificacionesUsuario(@PathVariable Long usuarioId) {
        try {
            List<Notificacion> notificaciones = service.obtenerNotificacionesUsuario(usuarioId);
            return ResponseEntity.ok(notificaciones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/usuario/{usuarioId}/no-leidas")
    @PreAuthorize("hasAuthority('NOTIFICACION_LEER')")
    public ResponseEntity<List<Notificacion>> obtenerNotificacionesNoLeidas(@PathVariable Long usuarioId) {
        try {
            List<Notificacion> notificaciones = service.obtenerNotificacionesNoLeidas(usuarioId);
            return ResponseEntity.ok(notificaciones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/usuario/{usuarioId}/contar-no-leidas")
    @PreAuthorize("hasAuthority('NOTIFICACION_LEER')")
    public ResponseEntity<Long> contarNotificacionesNoLeidas(@PathVariable Long usuarioId) {
        try {
            Long count = service.contarNotificacionesNoLeidas(usuarioId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/marcar-leida")
    @PreAuthorize("hasAuthority('NOTIFICACION_EDITAR')")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Long id) {
        try {
            service.marcarComoLeida(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/usuario/{usuarioId}/marcar-todas-leidas")
    @PreAuthorize("hasAuthority('NOTIFICACION_EDITAR')")
    public ResponseEntity<Void> marcarTodasComoLeidas(@PathVariable Long usuarioId) {
        try {
            service.marcarTodasComoLeidas(usuarioId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICACION_ELIMINAR')")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Long id) {
        try {
            service.eliminarNotificacion(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/usuario/{usuarioId}/antiguas")
    @PreAuthorize("hasAuthority('NOTIFICACION_ELIMINAR')")
    public ResponseEntity<Void> eliminarNotificacionesAntiguas(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "30") int dias) {
        try {
            service.eliminarNotificacionesAntiguas(usuarioId, dias);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 
