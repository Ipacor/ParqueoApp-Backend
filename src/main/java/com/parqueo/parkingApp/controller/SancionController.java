package com.parqueo.parkingApp.controller;

import com.parqueo.parkingApp.dto.SancionDto;
import com.parqueo.parkingApp.dto.SancionDetalleDto;
import com.parqueo.parkingApp.model.Sancion;
import com.parqueo.parkingApp.model.SancionDetalle;
import com.parqueo.parkingApp.service.SancionService;
import com.parqueo.parkingApp.service.SancionDetalleService;
import com.parqueo.parkingApp.service.ReglasEstacionamientoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import com.parqueo.parkingApp.model.Usuario;
import com.parqueo.parkingApp.repository.UsuarioRepository;
import org.springframework.web.bind.annotation.*;
import com.parqueo.parkingApp.repository.ReglasEstacionamientoRepository;
import com.parqueo.parkingApp.repository.SancionRepository;
import com.parqueo.parkingApp.model.ReglasEstacionamiento;
import com.parqueo.parkingApp.service.NotificacionService;
import com.parqueo.parkingApp.model.Notificacion;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sanciones")
@RequiredArgsConstructor
public class SancionController {

    private final SancionService service;
    private final SancionDetalleService detalleService;
    private final ReglasEstacionamientoService reglasService;
    private final UsuarioRepository usuarioRepository;
    private final ReglasEstacionamientoRepository reglasEstacionamientoRepository;
    private final SancionRepository sancionRepository;
    private final NotificacionService notificacionService;

    @GetMapping
    @PreAuthorize("hasAuthority('SANCION_LEER')")
    public ResponseEntity<List<SancionDto>> listar() {
        List<SancionDto> sanciones = service.obtenerTodos();
        return ResponseEntity.ok(sanciones);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SANCION_LEER')")
    public ResponseEntity<SancionDto> obtenerPorId(@PathVariable Long id) {
        try {
            SancionDto sancion = service.obtenerPorId(id);
            return ResponseEntity.ok(sancion);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SANCION_CREAR')")
    public ResponseEntity<?> crear(@Valid @RequestBody SancionDto dto, Authentication authentication) {
        try {
            String username = authentication.getName();
            Usuario usuarioActual = usuarioRepository.findByUsername(username).orElse(null);
            SancionDto creada = service.crearConRegistrador(dto, usuarioActual);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SANCION_EDITAR')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody SancionDto dto) {
        try {
            SancionDto actualizada = service.actualizar(id, dto);
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
    @PreAuthorize("hasAuthority('SANCION_ELIMINAR')")
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
    @PreAuthorize("hasAuthority('SANCION_BUSCAR_POR_USUARIO')")
    public ResponseEntity<List<SancionDto>> buscarPorUsuario(@PathVariable Long usuarioId) {
        try {
            List<SancionDto> sanciones = service.buscarPorUsuario(usuarioId);
            return ResponseEntity.ok(sanciones);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/vehiculo/{vehiculoId}")
    @PreAuthorize("hasAuthority('SANCION_BUSCAR_POR_VEHICULO')")
    public ResponseEntity<List<SancionDto>> buscarPorVehiculo(@PathVariable Long vehiculoId) {
        try {
            List<SancionDto> sanciones = service.buscarPorVehiculo(vehiculoId);
            return ResponseEntity.ok(sanciones);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAuthority('SANCION_BUSCAR_POR_ESTADO')")
    public ResponseEntity<List<SancionDto>> buscarPorEstado(@PathVariable Sancion.EstadoSancion estado) {
        try {
            List<SancionDto> sanciones = service.buscarPorEstado(estado);
            return ResponseEntity.ok(sanciones);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/fecha")
    @PreAuthorize("hasAuthority('SANCION_BUSCAR_POR_FECHA')")
    public ResponseEntity<List<SancionDto>> buscarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            List<SancionDto> sanciones = service.buscarPorFecha(fechaInicio, fechaFin);
            return ResponseEntity.ok(sanciones);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/activas")
    @PreAuthorize("hasAuthority('SANCION_LISTAR_ACTIVAS')")
    public ResponseEntity<List<SancionDto>> buscarSancionesActivas() {
        List<SancionDto> sanciones = service.buscarSancionesActivas();
        return ResponseEntity.ok(sanciones);
    }

    @GetMapping("/estados")
    @PreAuthorize("hasAuthority('SANCION_LISTAR_ESTADOS')")
    public ResponseEntity<List<Sancion.EstadoSancion>> obtenerEstados() {
        return ResponseEntity.ok(List.of(Sancion.EstadoSancion.values()));
    }

    @PostMapping("/completa")
    @PreAuthorize("hasAuthority('SANCION_CREAR')")
    public ResponseEntity<?> crearSancionCompleta(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            // Extraer datos del request
            Long usuarioId = Long.valueOf(request.get("usuarioId").toString());
            Long vehiculoId = Long.valueOf(request.get("vehiculoId").toString());
            Long reglaId = Long.valueOf(request.get("reglaId").toString());
            String motivo = request.get("motivo").toString();
            String descripcionDetalle = request.get("descripcionDetalle") != null ? 
                request.get("descripcionDetalle").toString() : "";

            String username = authentication.getName();
            Usuario usuarioActual = usuarioRepository.findByUsername(username).orElse(null);

            // Crear la sanción usando Builder
            SancionDto sancionDto = SancionDto.builder()
                .usuarioId(usuarioId)
                .vehiculoId(vehiculoId)
                .motivo(motivo)
                .estado(Sancion.EstadoSancion.ACTIVA)
                .registroSancion(LocalDateTime.now())
                .reglaId(reglaId)
                .build();

            SancionDto sancionCreada = service.crearConRegistrador(sancionDto, usuarioActual);

            // Crear el detalle de la sanción usando Builder
            SancionDetalleDto detalleDto = SancionDetalleDto.builder()
                .sancionId(sancionCreada.getId())
                .reglaId(reglaId)
                .descripcion(descripcionDetalle.isEmpty() ? 
                    "Sanción aplicada por infracción" : descripcionDetalle)
                .estado("ACTIVO")
                .fechaSancion(LocalDateTime.now())
                .build();

            // Crear el detalle usando el servicio inyectado
            SancionDetalleDto detalleCreado = detalleService.crear(detalleDto);

            // Enviar notificación al usuario sancionado
            Usuario usuarioSancionado = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
            
            String tituloNotificacion = "Sanción Aplicada";
            String mensajeNotificacion = String.format("Se ha aplicado una sanción por: %s. Motivo: %s", 
                    reglasEstacionamientoRepository.findById(reglaId).map(ReglasEstacionamiento::getDescripcion).orElse("Infracción"),
                    motivo);
            
            notificacionService.crearNotificacion(usuarioSancionado, tituloNotificacion, mensajeNotificacion, Notificacion.TipoNotificacion.SANCION);

            return ResponseEntity.status(HttpStatus.CREATED).body(sancionCreada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor: " + e.getMessage());
        }
    }

    @GetMapping("/reglas-disponibles")
    @PreAuthorize("hasAuthority('SANCION_CREAR')")
    public ResponseEntity<List<Map<String, Object>>> obtenerReglasDisponibles() {
        try {
            List<Map<String, Object>> reglas = reglasService.obtenerTodos().stream()
                .map(regla -> {
                    Map<String, Object> reglaMap = new HashMap<>();
                    reglaMap.put("id", regla.getId());
                    reglaMap.put("descripcion", regla.getDescripcion());
                    reglaMap.put("tipoFalta", regla.getTipoFalta());
                    return reglaMap;
                })
                .toList();
            return ResponseEntity.ok(reglas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @GetMapping("/niveles-gravedad")
    @PreAuthorize("hasAuthority('SANCION_CREAR')")
    public ResponseEntity<List<Map<String, Object>>> obtenerNivelesGravedad() {
        try {
            List<Map<String, Object>> niveles = List.of(
                Map.of("valor", "LEVE", "descripcion", "Llamada de atención"),
                Map.of("valor", "INTERMEDIA", "descripcion", "Suspensión temporal"),
                Map.of("valor", "GRAVE", "descripcion", "Suspensión definitiva")
            );
            return ResponseEntity.ok(niveles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @GetMapping("/castigo-estimado")
    @PreAuthorize("hasAuthority('SANCION_CREAR')")
    public ResponseEntity<Map<String, Object>> obtenerCastigoEstimado(@RequestParam Long usuarioId, @RequestParam Long reglaId) {
        Map<String, Object> result = new HashMap<>();
        ReglasEstacionamiento regla = reglasEstacionamientoRepository.findById(reglaId)
            .orElseThrow(() -> new RuntimeException("Regla no encontrada"));
        var tipoFalta = regla.getTipoFalta();
        // Buscar sanciones activas o resueltas del usuario
        var sancionesUsuario = sancionRepository.findByUsuarioId(usuarioId);
        long reincidencias = sancionesUsuario.stream()
            .filter(s -> s.getDetalles() != null && s.getDetalles().stream().anyMatch(d -> d.getRegla().getTipoFalta() == tipoFalta))
            .filter(s -> s.getEstado().equals("ACTIVA") || s.getEstado().equals("RESUELTA"))
            .count();
        // Determinar castigo según reincidencias y tipoFalta
        String tipoCastigo = "Amonestación";
        LocalDateTime inicio = null;
        LocalDateTime fin = null;
        if (tipoFalta.name().equals("LEVE")) {
            if (reincidencias == 0) tipoCastigo = "Amonestación";
            else if (reincidencias == 1) {
                tipoCastigo = "Suspensión temporal (7 días)";
                inicio = LocalDateTime.now();
                fin = inicio.plusDays(7);
            } else if (reincidencias >= 2) {
                tipoCastigo = "Suspensión total";
            }
        } else if (tipoFalta.name().equals("GRAVE")) {
            if (reincidencias == 0) tipoCastigo = "Suspensión temporal (7 días)";
            else if (reincidencias == 1) {
                tipoCastigo = "Suspensión temporal (30 días)";
                inicio = LocalDateTime.now();
                fin = inicio.plusDays(30);
            } else if (reincidencias >= 2) {
                tipoCastigo = "Suspensión total";
            }
        }
        result.put("tipoCastigo", tipoCastigo);
        result.put("fechaInicioSuspension", inicio);
        result.put("fechaFinSuspension", fin);
        result.put("reincidencias", reincidencias);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/con-detalle")
    @PreAuthorize("hasAuthority('SANCION_LEER')")
    public ResponseEntity<?> obtenerSancionConDetalle(@PathVariable Long id) {
        try {
            SancionDto sancion = service.obtenerPorId(id);
            List<SancionDetalleDto> detalles = detalleService.buscarPorSancion(id);
            SancionDetalleDto detalle = detalles.isEmpty() ? null : detalles.get(0);
            return ResponseEntity.ok(Map.of(
                "sancion", sancion,
                "detalle", detalle
            ));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/desbloquear")
    @PreAuthorize("hasAuthority('SANCION_EDITAR')")
    public ResponseEntity<?> desbloquearSancion(@PathVariable Long id, Authentication authentication) {
        try {
            // Obtener la sanción
            SancionDto sancion = service.obtenerPorId(id);
            if (sancion == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Cambiar estado a RESUELTA
            sancion.setEstado(Sancion.EstadoSancion.RESUELTA);
            SancionDto actualizada = service.actualizar(id, sancion);
            
            // Obtener el usuario sancionado
            Usuario usuarioSancionado = usuarioRepository.findById(sancion.getUsuarioId())
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
            
            // Crear notificación de desbloqueo
            String tituloNotificacion = "Sanción Desbloqueada";
            String mensajeNotificacion = String.format("Tu sanción #%d ha sido desbloqueada por un administrador. Ya puedes volver a usar el sistema.", 
                    sancion.getId());
            
            notificacionService.crearNotificacion(usuarioSancionado, tituloNotificacion, mensajeNotificacion, Notificacion.TipoNotificacion.DESBLOQUEO);
            
            return ResponseEntity.ok(actualizada);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor: " + e.getMessage());
        }
    }
}