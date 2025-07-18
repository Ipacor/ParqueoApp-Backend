package com.parqueo.parkingApp.controller;

import com.parqueo.parkingApp.dto.EscaneoQRDto;
import com.parqueo.parkingApp.service.EscaneoQRService;
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
@RequestMapping("/api/escaneos")
@RequiredArgsConstructor
public class EscaneoQRController {

    private final EscaneoQRService escaneoQRService;

    @GetMapping
    @PreAuthorize("hasAuthority('ESCANEOQR_LEER')")
    public ResponseEntity<List<EscaneoQRDto>> listar() {
        List<EscaneoQRDto> escaneos = escaneoQRService.obtenerTodos();
        return ResponseEntity.ok(escaneos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ESCANEOQR_LEER')")
    public ResponseEntity<EscaneoQRDto> obtenerPorId(@PathVariable Long id) {
        try {
            EscaneoQRDto escaneo = escaneoQRService.obtenerPorId(id);
            return ResponseEntity.ok(escaneo);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ESCANEOQR_CREAR')")
    public ResponseEntity<?> crear(@Valid @RequestBody EscaneoQRDto dto) {
        try {
            EscaneoQRDto creado = escaneoQRService.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ESCANEOQR_EDITAR')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody EscaneoQRDto dto) {
        try {
            EscaneoQRDto actualizado = escaneoQRService.actualizar(id, dto);
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
    @PreAuthorize("hasAuthority('ESCANEOQR_ELIMINAR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            escaneoQRService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/entrada/{reservaId}")
    @PreAuthorize("hasAuthority('ESCANEOQR_REGISTRAR_ENTRADA')")
    public ResponseEntity<?> registrarEntrada(@PathVariable Long reservaId) {
        try {
            EscaneoQRDto escaneo = escaneoQRService.registrarEntrada(reservaId);
            return ResponseEntity.ok(escaneo);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @PutMapping("/salida/{reservaId}")
    @PreAuthorize("hasAuthority('ESCANEOQR_REGISTRAR_SALIDA')")
    public ResponseEntity<?> registrarSalida(@PathVariable Long reservaId) {
        try {
            EscaneoQRDto escaneo = escaneoQRService.registrarSalida(reservaId);
            return ResponseEntity.ok(escaneo);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @GetMapping("/reserva/{reservaId}")
    @PreAuthorize("hasAuthority('ESCANEOQR_BUSCAR_POR_RESERVA')")
    public ResponseEntity<EscaneoQRDto> obtenerPorReserva(@PathVariable Long reservaId) {
        try {
            EscaneoQRDto escaneo = escaneoQRService.obtenerPorReserva(reservaId);
            return ResponseEntity.ok(escaneo);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAuthority('ESCANEOQR_BUSCAR_POR_USUARIO')")
    public ResponseEntity<List<EscaneoQRDto>> buscarPorUsuario(@PathVariable Long usuarioId) {
        try {
            List<EscaneoQRDto> escaneos = escaneoQRService.buscarPorUsuario(usuarioId);
            return ResponseEntity.ok(escaneos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/fecha")
    @PreAuthorize("hasAuthority('ESCANEOQR_BUSCAR_POR_FECHA')")
    public ResponseEntity<List<EscaneoQRDto>> buscarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            List<EscaneoQRDto> escaneos = escaneoQRService.buscarPorFecha(fechaInicio, fechaFin);
            return ResponseEntity.ok(escaneos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
