package com.parqueo.parkingApp.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parqueo.parkingApp.dto.VehiculoDto;
import com.parqueo.parkingApp.model.Vehiculo;
import com.parqueo.parkingApp.service.VehiculoService;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService vehiculoService;

    @GetMapping
    @PreAuthorize("hasAuthority('VEHICULO_LEER')")
    public ResponseEntity<List<VehiculoDto>> listar() {
        return ResponseEntity.ok(vehiculoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICULO_LEER')")
    public ResponseEntity<VehiculoDto> obtenerPorId(@PathVariable Long id) {
        try {
            VehiculoDto vehiculo = vehiculoService.obtenerPorId(id);
            return ResponseEntity.ok(vehiculo);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/placa/{placa}")
    @PreAuthorize("hasAuthority('VEHICULO_BUSCAR_POR_PLACA')")
    public ResponseEntity<VehiculoDto> buscarPorPlaca(@PathVariable String placa) {
        Optional<VehiculoDto> vehiculo = vehiculoService.buscarPorPlaca(placa);
        return vehiculo.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAuthority('VEHICULO_BUSCAR_POR_USUARIO')")
    public ResponseEntity<List<VehiculoDto>> buscarPorUsuario(@PathVariable Long usuarioId) {
        try {
            List<VehiculoDto> vehiculos = vehiculoService.buscarPorUsuario(usuarioId);
            return ResponseEntity.ok(vehiculos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasAuthority('VEHICULO_BUSCAR_POR_TIPO')")
    public ResponseEntity<List<VehiculoDto>> buscarPorTipo(@PathVariable Vehiculo.TipoVehiculo tipo) {
        try {
            List<VehiculoDto> vehiculos = vehiculoService.buscarPorTipo(tipo);
            return ResponseEntity.ok(vehiculos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VEHICULO_CREAR')")
    public ResponseEntity<?> crear(@Valid @RequestBody VehiculoDto dto) {
        try {
            VehiculoDto creado = vehiculoService.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICULO_EDITAR')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody VehiculoDto dto) {
        try {
            VehiculoDto actualizado = vehiculoService.actualizar(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICULO_ELIMINAR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            vehiculoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

