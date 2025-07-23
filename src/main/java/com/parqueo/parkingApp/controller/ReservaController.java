package com.parqueo.parkingApp.controller;

import com.parqueo.parkingApp.dto.ReservaDto;
import com.parqueo.parkingApp.model.Reserva;
import com.parqueo.parkingApp.service.ReservaService;
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
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    @GetMapping
    @PreAuthorize("hasAuthority('RESERVA_LEER')")
    public ResponseEntity<List<ReservaDto>> listar() {
        List<ReservaDto> reservas = reservaService.obtenerTodos();
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RESERVA_LEER')")
    public ResponseEntity<ReservaDto> obtenerPorId(@PathVariable Long id) {
        try {
            ReservaDto reserva = reservaService.obtenerPorId(id);
            return ResponseEntity.ok(reserva);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('RESERVA_CREAR')")
    public ResponseEntity<?> crear(@Valid @RequestBody ReservaDto dto) {
        try {
            ReservaDto creada = reservaService.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('RESERVA_EDITAR')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody ReservaDto dto) {
        try {
            ReservaDto actualizada = reservaService.actualizar(id, dto);
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
    @PreAuthorize("hasAuthority('RESERVA_ELIMINAR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        System.out.println("=== PETICIÓN DELETE RECIBIDA PARA RESERVA #" + id + " ===");
        try {
            reservaService.eliminar(id);
            System.out.println("Reserva #" + id + " eliminada exitosamente");
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            System.out.println("Reserva #" + id + " no encontrada: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            System.out.println("Error de argumento para reserva #" + id + ": " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.out.println("Error inesperado eliminando reserva #" + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAuthority('RESERVA_BUSCAR_POR_USUARIO')")
    public ResponseEntity<List<ReservaDto>> buscarPorUsuario(@PathVariable Long usuarioId) {
        try {
            List<ReservaDto> reservas = reservaService.buscarPorUsuario(usuarioId);
            return ResponseEntity.ok(reservas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/vehiculo/{vehiculoId}")
    @PreAuthorize("hasAuthority('RESERVA_BUSCAR_POR_VEHICULO')")
    public ResponseEntity<List<ReservaDto>> buscarPorVehiculo(@PathVariable Long vehiculoId) {
        try {
            List<ReservaDto> reservas = reservaService.buscarPorVehiculo(vehiculoId);
            return ResponseEntity.ok(reservas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/espacio/{espacioId}")
    @PreAuthorize("hasAuthority('RESERVA_BUSCAR_POR_ESPACIO')")
    public ResponseEntity<List<ReservaDto>> buscarPorEspacio(@PathVariable Long espacioId) {
        try {
            List<ReservaDto> reservas = reservaService.buscarPorEspacio(espacioId);
            return ResponseEntity.ok(reservas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAuthority('RESERVA_BUSCAR_POR_ESTADO')")
    public ResponseEntity<List<ReservaDto>> buscarPorEstado(@PathVariable Reserva.EstadoReserva estado) {
        try {
            List<ReservaDto> reservas = reservaService.buscarPorEstado(estado);
            return ResponseEntity.ok(reservas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/fecha")
    @PreAuthorize("hasAuthority('RESERVA_BUSCAR_POR_FECHA')")
    public ResponseEntity<List<ReservaDto>> buscarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            List<ReservaDto> reservas = reservaService.buscarPorFecha(fechaInicio, fechaFin);
            return ResponseEntity.ok(reservas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/activas")
    @PreAuthorize("hasAuthority('RESERVA_LISTAR_ACTIVAS')")
    public ResponseEntity<List<ReservaDto>> buscarReservasActivas() {
        List<ReservaDto> reservas = reservaService.buscarReservasActivas();
        return ResponseEntity.ok(reservas);
    }

    @PostMapping("/liberar-expirados")
    @PreAuthorize("hasAuthority('RESERVA_LIBERAR_EXPIRADOS')")
    public ResponseEntity<String> liberarEspaciosExpirados() {
        reservaService.liberarEspaciosReservadosExpirados();
        return ResponseEntity.ok("Espacios expirados liberados");
    }

    @PostMapping("/forzar-expiracion")
    @PreAuthorize("hasAuthority('RESERVA_LIBERAR_EXPIRADOS')")
    public ResponseEntity<String> forzarExpiracion() {
        reservaService.liberarEspaciosReservadosExpirados();
        return ResponseEntity.ok("Expiración forzada ejecutada correctamente");
    }

    @PostMapping("/actualizar-espacios")
    @PreAuthorize("hasAuthority('RESERVA_LIBERAR_EXPIRADOS')")
    public ResponseEntity<String> actualizarEspacios() {
        reservaService.liberarEspaciosReservadosExpirados();
        return ResponseEntity.ok("Espacios actualizados correctamente");
    }

    @PostMapping("/forzar-expiracion-reserva/{id}")
    @PreAuthorize("hasAuthority('RESERVA_LIBERAR_EXPIRADOS')")
    public ResponseEntity<String> forzarExpiracionReserva(@PathVariable Long id) {
        try {
            reservaService.forzarExpiracionReserva(id);
            return ResponseEntity.ok("Reserva " + id + " expirada correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al expirar reserva: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/completa")
    @PreAuthorize("hasAuthority('RESERVA_VER_COMPLETA')")
    public ResponseEntity<ReservaDto> obtenerReservaCompleta(@PathVariable Long id) {
        ReservaDto reserva = reservaService.obtenerPorId(id);
        return ResponseEntity.ok(reserva);
    }
}
