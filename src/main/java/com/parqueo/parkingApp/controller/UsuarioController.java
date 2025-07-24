package com.parqueo.parkingApp.controller;

import com.parqueo.parkingApp.dto.UsuarioDto;
import com.parqueo.parkingApp.model.Usuario;
import com.parqueo.parkingApp.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<List<UsuarioDto>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<UsuarioDto> obtenerUsuario(@PathVariable Long id) {
        try {
            UsuarioDto dto = usuarioService.obtenerPorId(id);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USUARIO_CREAR')")
    public ResponseEntity<?> crearUsuario(@Valid @RequestBody UsuarioDto usuarioDto) {
        try {
            UsuarioDto creado = usuarioService.crear(usuarioDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registroPublico(@Valid @RequestBody UsuarioDto usuarioDto) {
        System.out.println("=== REGISTRO PÚBLICO ===");
        System.out.println("Usuario recibido: " + usuarioDto);
        System.out.println("Rol: " + usuarioDto.getRolNombre());
        System.out.println("RolId: " + usuarioDto.getRolId());
        
        try {
            // Validar que el rol sea uno de los permitidos para registro público
            String rol = usuarioDto.getRolNombre() != null ? usuarioDto.getRolNombre() : "";
            System.out.println("Rol validado: " + rol);
            
            if (!rol.equals("ESTUDIANTE") && !rol.equals("DOCENTE") && !rol.equals("PROVEEDOR_SERVICIO")) {
                System.out.println("Rol no permitido: " + rol);
                return ResponseEntity.badRequest().body("Solo se permiten registros para ESTUDIANTE, DOCENTE y PROVEEDOR_SERVICIO");
            }
            
            System.out.println("Creando usuario...");
            UsuarioDto creado = usuarioService.crear(usuarioDto);
            System.out.println("Usuario creado exitosamente: " + creado);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (RuntimeException e) {
            System.out.println("RuntimeException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Optional<UsuarioDto> usuario = usuarioService.findByUsername(loginRequest.getUsername());
            
            if (usuario.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
            }
            
            UsuarioDto usuarioEncontrado = usuario.get();
            
            // Verificar contraseña usando el encoder
            if (!passwordEncoder.matches(loginRequest.getPassword(), usuarioEncontrado.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Contraseña incorrecta");
            }
            
            return ResponseEntity.ok(usuarioEncontrado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIO_EDITAR')")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioDto usuarioDto) {
        try {
            UsuarioDto actualizado = usuarioService.actualizar(id, usuarioDto);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIO_ELIMINAR')")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        try {
            usuarioService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @PutMapping("/{id}/desactivar")
    @PreAuthorize("hasAuthority('USUARIO_DESACTIVAR')")
    public ResponseEntity<?> desactivarUsuario(@PathVariable Long id) {
        try {
            usuarioService.desactivarUsuario(id);
            return ResponseEntity.ok().body("Usuario desactivado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @PutMapping("/{id}/activar")
    @PreAuthorize("hasAuthority('USUARIO_ACTIVAR')")
    public ResponseEntity<?> activarUsuario(@PathVariable Long id) {
        try {
            usuarioService.activarUsuario(id);
            return ResponseEntity.ok().body("Usuario activado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @DeleteMapping("/{id}/completo")
    @PreAuthorize("hasAuthority('USUARIO_ELIMINAR_COMPLETO')")
    public ResponseEntity<?> eliminarUsuarioCompleto(@PathVariable Long id) {
        try {
            usuarioService.eliminarUsuarioCompleto(id);
            return ResponseEntity.ok().body("Usuario y todas sus relaciones eliminadas correctamente");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasAuthority('USUARIO_BUSCAR_POR_USERNAME')")
    public ResponseEntity<UsuarioDto> obtenerPorUsername(@PathVariable String username) {
        Optional<UsuarioDto> usuario = usuarioService.findByUsername(username);
        return usuario.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAuthority('USUARIO_BUSCAR_POR_EMAIL')")
    public ResponseEntity<UsuarioDto> obtenerPorEmail(@PathVariable String email) {
        Optional<UsuarioDto> usuario = usuarioService.findByEmail(email);
        return usuario.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rol/{rolNombre}")
    @PreAuthorize("hasAuthority('USUARIO_BUSCAR_POR_ROL')")
    public ResponseEntity<List<UsuarioDto>> obtenerPorRol(@PathVariable String rolNombre) {
        try {
            List<UsuarioDto> usuarios = usuarioService.findByRolNombre(rolNombre);
            return ResponseEntity.ok(usuarios);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/suspendidos")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<List<UsuarioDto>> obtenerUsuariosSuspendidos() {
        List<UsuarioDto> suspendidos = usuarioService.obtenerUsuariosSuspendidos();
        return ResponseEntity.ok(suspendidos);
    }

    @GetMapping("/con-sancion-activa")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<List<UsuarioDto>> obtenerUsuariosConSancionActiva() {
        List<UsuarioDto> usuarios = usuarioService.obtenerUsuariosConSancionActiva();
        return ResponseEntity.ok(usuarios);
    }
}

// Clase para el request de login
class LoginRequest {
    private String username;
    private String password;

    // Getters y Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
