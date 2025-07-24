package com.parqueo.parkingApp.controller;

import com.parqueo.parkingApp.model.Notificacion;
import com.parqueo.parkingApp.model.Usuario;
import com.parqueo.parkingApp.repository.UsuarioRepository;
import com.parqueo.parkingApp.service.NotificacionService;
import com.parqueo.parkingApp.dto.NotificacionDto;
import com.parqueo.parkingApp.mapper.NotificacionMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService service;
    private final UsuarioRepository usuarioRepository;

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAuthority('NOTIFICACION_LEER')")
    public ResponseEntity<List<NotificacionDto>> obtenerNotificacionesUsuario(@PathVariable Long usuarioId) {
        try {
            System.out.println("=== SOLICITUD DE NOTIFICACIONES PARA USUARIO " + usuarioId + " ===");
            List<Notificacion> notificaciones = service.obtenerNotificacionesUsuario(usuarioId);
            System.out.println("Notificaciones encontradas: " + notificaciones.size());
            List<NotificacionDto> dtos = notificaciones.stream()
                    .map(NotificacionMapper::toDto)
                    .filter(dto -> dto != null) // Filtrar DTOs nulos
                    .toList();
            System.out.println("DTOs creados: " + dtos.size());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            System.err.println("Error al obtener notificaciones para usuario " + usuarioId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of()); // Retornar lista vacía en lugar de error
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
    
    @PostMapping("/sistema/{usuarioId}")
    @PreAuthorize("hasAuthority('NOTIFICACION_CREAR')")
    public ResponseEntity<?> enviarNotificacionSistema(
            @PathVariable Long usuarioId,
            @RequestBody Map<String, String> request) {
        try {
            String titulo = request.get("titulo");
            String mensaje = request.get("mensaje");
            
            if (titulo == null || mensaje == null) {
                return ResponseEntity.badRequest().body("Título y mensaje son obligatorios");
            }
            
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
            
            service.crearNotificacion(usuario, titulo, mensaje, Notificacion.TipoNotificacion.SISTEMA);
            
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor: " + e.getMessage());
        }
    }

    @GetMapping("/usuario/{usuarioId}/estadisticas")
    @PreAuthorize("hasAuthority('NOTIFICACION_LEER')")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(@PathVariable Long usuarioId) {
        try {
            Map<String, Object> estadisticas = service.obtenerEstadisticasGenerales(usuarioId);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/usuario/{usuarioId}/estadisticas-por-tipo")
    @PreAuthorize("hasAuthority('NOTIFICACION_LEER')")
    public ResponseEntity<Map<String, Long>> obtenerEstadisticasPorTipo(@PathVariable Long usuarioId) {
        try {
            Map<String, Long> estadisticas = service.obtenerEstadisticasPorTipo(usuarioId);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/usuario/{usuarioId}/tipo/{tipo}")
    @PreAuthorize("hasAuthority('NOTIFICACION_LEER')")
    public ResponseEntity<List<NotificacionDto>> obtenerNotificacionesPorTipo(
            @PathVariable Long usuarioId,
            @PathVariable String tipo) {
        try {
            Notificacion.TipoNotificacion tipoNotificacion = Notificacion.TipoNotificacion.valueOf(tipo.toUpperCase());
            List<Notificacion> notificaciones = service.obtenerNotificacionesPorTipo(usuarioId, tipoNotificacion);
            List<NotificacionDto> dtos = notificaciones.stream().map(NotificacionMapper::toDto).toList();
            return ResponseEntity.ok(dtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/usuario/{usuarioId}/tipo/{tipo}")
    @PreAuthorize("hasAuthority('NOTIFICACION_ELIMINAR')")
    public ResponseEntity<Void> eliminarNotificacionesPorTipo(
            @PathVariable Long usuarioId,
            @PathVariable String tipo) {
        try {
            Notificacion.TipoNotificacion tipoNotificacion = Notificacion.TipoNotificacion.valueOf(tipo.toUpperCase());
            service.eliminarNotificacionesPorTipo(usuarioId, tipoNotificacion);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/recordatorio-entrada/{usuarioId}")
    @PreAuthorize("hasAuthority('NOTIFICACION_CREAR')")
    public ResponseEntity<?> crearRecordatorioEntrada(
            @PathVariable Long usuarioId,
            @RequestBody Map<String, String> request) {
        try {
            String espacio = request.get("espacio");
            String fecha = request.get("fecha");
            
            if (espacio == null || fecha == null) {
                return ResponseEntity.badRequest().body("Espacio y fecha son obligatorios");
            }
            
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
            
            service.crearRecordatorioEntrada(usuario, espacio, fecha);
            
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor: " + e.getMessage());
        }
    }

    @PostMapping("/recordatorio-salida/{usuarioId}")
    @PreAuthorize("hasAuthority('NOTIFICACION_CREAR')")
    public ResponseEntity<?> crearRecordatorioSalida(
            @PathVariable Long usuarioId,
            @RequestBody Map<String, String> request) {
        try {
            String espacio = request.get("espacio");
            String fecha = request.get("fecha");
            
            if (espacio == null || fecha == null) {
                return ResponseEntity.badRequest().body("Espacio y fecha son obligatorios");
            }
            
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
            
            service.crearRecordatorioSalida(usuario, espacio, fecha);
            
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor: " + e.getMessage());
        }
    }

    @PostMapping("/mantenimiento/{usuarioId}")
    @PreAuthorize("hasAuthority('NOTIFICACION_CREAR')")
    public ResponseEntity<?> enviarNotificacionMantenimiento(
            @PathVariable Long usuarioId,
            @RequestBody Map<String, String> request) {
        try {
            String mensaje = request.get("mensaje");
            
            if (mensaje == null) {
                return ResponseEntity.badRequest().body("Mensaje es obligatorio");
            }
            
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
            
            service.crearNotificacionMantenimiento(usuario, mensaje);
            
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor: " + e.getMessage());
        }
    }
} 