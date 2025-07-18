package com.parqueo.parkingApp.controller;

import com.parqueo.parkingApp.model.Rol;
import com.parqueo.parkingApp.repository.RolRepository;
import com.parqueo.parkingApp.service.RolService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {
    private final RolService rolService;

    @GetMapping
    public ResponseEntity<List<Rol>> listarRoles() {
        return ResponseEntity.ok(rolService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rol> obtenerRol(@PathVariable Long id) {
        return ResponseEntity.ok(rolService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<Rol> crearRol(@RequestBody RolDto dto) {
        Rol nuevo = new Rol();
        nuevo.setNombre(dto.getNombre());
        nuevo.setDescripcion(dto.getDescripcion());
        Rol creado = rolService.crear(nuevo, dto.getPermisos());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rol> actualizarRol(@PathVariable Long id, @RequestBody RolDto dto) {
        Rol actualizado = rolService.actualizar(id, dto.getNombre(), dto.getDescripcion(), dto.getPermisos());
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRol(@PathVariable Long id) {
        rolService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class RolDto {
        private String nombre;
        private String descripcion;
        private List<String> permisos;
    }
} 