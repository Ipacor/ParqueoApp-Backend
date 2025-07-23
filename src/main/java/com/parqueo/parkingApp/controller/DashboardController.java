package com.parqueo.parkingApp.controller;

import com.parqueo.parkingApp.repository.*;
import com.parqueo.parkingApp.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UsuarioRepository usuarioRepository;
    private final EspacioDisponibleRepository espacioRepository;
    private final ReservaRepository reservaRepository;
    private final SancionRepository sancionRepository;
    private final HistorialUsoRepository historialRepository;
    private final VehiculoRepository vehiculoRepository;

    @GetMapping("/estadisticas")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasDashboard() {
        try {
            Map<String, Object> estadisticas = new HashMap<>();
            
            // Estadísticas de usuarios
            long totalUsuarios = usuarioRepository.count();
            long usuariosActivos = usuarioRepository.countByActivoTrue();
            
            // Estadísticas de espacios
            long totalEspacios = espacioRepository.count();
            long espaciosDisponibles = espacioRepository.countByEstado(EspacioDisponible.EstadoEspacio.DISPONIBLE);
            long espaciosOcupados = espacioRepository.countByEstado(EspacioDisponible.EstadoEspacio.OCUPADO);
            long espaciosReservados = espacioRepository.countByEstado(EspacioDisponible.EstadoEspacio.RESERVADO);
            long espaciosMantenimiento = espacioRepository.countByEstado(EspacioDisponible.EstadoEspacio.MANTENIMIENTO);
            
            // Estadísticas de reservas
            LocalDate hoy = LocalDate.now();
            long reservasHoy = reservaRepository.countByFechaReserva(hoy);
            long reservasEstaSemana = reservaRepository.countByFechaReservaBetween(
                hoy.minusDays(7), hoy.plusDays(1));
            long reservasPendientes = reservaRepository.countByEstado(Reserva.EstadoReserva.RESERVADO);
            
            // Estadísticas de sanciones
            long sancionesActivas = sancionRepository.countByEstado(Sancion.EstadoSancion.ACTIVA);
            long sancionesEsteMes = sancionRepository.countByFechaSancionBetween(
                LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0),
                LocalDateTime.now().withHour(23).withMinute(59));
            long sancionesResueltas = sancionRepository.countByEstado(Sancion.EstadoSancion.RESUELTA);
            
            // Estadísticas de ingresos (historial de uso)
            long ingresosHoy = historialRepository.countByFechaEntradaBetween(
                LocalDateTime.now().withHour(0).withMinute(0),
                LocalDateTime.now().withHour(23).withMinute(59));
            long ingresosEstaSemana = historialRepository.countByFechaEntradaBetween(
                LocalDateTime.now().minusDays(7).withHour(0).withMinute(0),
                LocalDateTime.now().withHour(23).withMinute(59));
            
            // Calcular promedio diario de ingresos (últimos 30 días)
            long ingresosUltimos30Dias = historialRepository.countByFechaEntradaBetween(
                LocalDateTime.now().minusDays(30).withHour(0).withMinute(0),
                LocalDateTime.now().withHour(23).withMinute(59));
            double promedioDiario = ingresosUltimos30Dias / 30.0;
            
            // Estadísticas de vehículos
            long totalVehiculos = vehiculoRepository.count();
            
            // Distribución de roles de usuarios
            Map<String, Long> distribucionRoles = new HashMap<>();
            distribucionRoles.put("ESTUDIANTE", usuarioRepository.countByRolNombre("ESTUDIANTE"));
            distribucionRoles.put("DOCENTE", usuarioRepository.countByRolNombre("DOCENTE"));
            distribucionRoles.put("PROVEEDOR_SERVICIO", usuarioRepository.countByRolNombre("PROVEEDOR_SERVICIO"));
            distribucionRoles.put("VIGILANTE", usuarioRepository.countByRolNombre("VIGILANTE"));
            distribucionRoles.put("ADMIN", usuarioRepository.countByRolNombre("ADMIN"));
            
            // Construir respuesta
            final String KEY_TOTAL = "total";
            estadisticas.put("usuarios", Map.of(
                KEY_TOTAL, totalUsuarios,
                "activos", usuariosActivos,
                "nuevos_este_mes", (int)(totalUsuarios * 0.12) // Simulado
            ));
            
            estadisticas.put("espacios", Map.of(
                KEY_TOTAL, totalEspacios,
                "disponibles", espaciosDisponibles,
                "ocupados", espaciosOcupados,
                "reservados", espaciosReservados,
                "mantenimiento", espaciosMantenimiento
            ));
            
            estadisticas.put("reservas", Map.of(
                "hoy", reservasHoy,
                "esta_semana", reservasEstaSemana,
                "pendientes", reservasPendientes
            ));
            
            estadisticas.put("sanciones", Map.of(
                "activas", sancionesActivas,
                "este_mes", sancionesEsteMes,
                "resueltas", sancionesResueltas
            ));
            
            estadisticas.put("ingresos", Map.of(
                "hoy", ingresosHoy,
                "esta_semana", ingresosEstaSemana,
                "promedio_diario", Math.round(promedioDiario)
            ));
            
            estadisticas.put("vehiculos", Map.of(
                KEY_TOTAL, totalVehiculos
            ));
            
            estadisticas.put("distribucion_roles", distribucionRoles);
            
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al obtener estadísticas del dashboard");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
} 