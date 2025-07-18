package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.UsuarioDto;
import com.parqueo.parkingApp.mapper.UsuarioMapper;
import com.parqueo.parkingApp.model.Usuario;
import com.parqueo.parkingApp.model.Rol;
import com.parqueo.parkingApp.model.Vehiculo;
import com.parqueo.parkingApp.model.Sancion;
import com.parqueo.parkingApp.repository.UsuarioRepository;
import com.parqueo.parkingApp.repository.ReservaRepository;
import com.parqueo.parkingApp.repository.VehiculoRepository;
import com.parqueo.parkingApp.repository.SancionRepository;
import com.parqueo.parkingApp.repository.HistorialUsoRepository;
import com.parqueo.parkingApp.repository.EscaneoQRRepository;
import com.parqueo.parkingApp.repository.SancionDetalleRepository;
import com.parqueo.parkingApp.repository.RolRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper mapper;
    private final ReservaRepository reservaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final SancionRepository sancionRepository;
    private final HistorialUsoRepository historialUsoRepository;
    private final EscaneoQRRepository escaneoQRRepository;
    private final SancionDetalleRepository sancionDetalleRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UsuarioDto> obtenerTodos() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioMapper::toDto)
                .toList();
    }

    @Override
    public UsuarioDto obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID " + id));
        return UsuarioMapper.toDto(usuario);
    }

    @Override
    public UsuarioDto crear(UsuarioDto dto) {
        validarDatosUsuario(dto);
        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya está registrado.");
        }
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("El correo electrónico ya está registrado.");
        }
        // Validación: solo puede existir un administrador
        if ("ADMINISTRADOR".equalsIgnoreCase(dto.getRolNombre()) && usuarioRepository.existsByRol(rolRepository.findByNombre("ADMINISTRADOR").orElseThrow())) {
            throw new RuntimeException("Ya existe un usuario administrador en el sistema. Solo puede haber un administrador.");
        }
        Usuario usuario = UsuarioMapper.toEntity(dto);
        // Encriptar la contraseña antes de guardar
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        // Asignar el rol desde el repositorio
        Rol rol = rolRepository.findById(dto.getRolId()).orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        usuario.setRol(rol);
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        usuarioGuardado = usuarioRepository.findById(usuarioGuardado.getId()).orElse(usuarioGuardado);
        return UsuarioMapper.toDto(usuarioGuardado);
    }

    @Override
    public UsuarioDto actualizar(Long id, UsuarioDto dto) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID " + id));
        validarDatosUsuario(dto);
        if (!dto.getUsername().equals(usuarioExistente.getUsername()) && usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya está registrado.");
        }
        if (!dto.getEmail().equals(usuarioExistente.getEmail()) && usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("El correo electrónico ya está registrado.");
        }
        // Validación: solo puede existir un administrador
        if ("ADMINISTRADOR".equalsIgnoreCase(dto.getRolNombre()) &&
            !"ADMINISTRADOR".equalsIgnoreCase(usuarioExistente.getRol().getNombre()) &&
            usuarioRepository.existsByRol(rolRepository.findByNombre("ADMINISTRADOR").orElseThrow())) {
            throw new RuntimeException("Ya existe un usuario administrador en el sistema. Solo puede haber un administrador.");
        }
        usuarioExistente.setUsername(dto.getUsername());
        usuarioExistente.setPassword(dto.getPassword());
        usuarioExistente.setNombreCompleto(dto.getNombreCompleto());
        usuarioExistente.setEmail(dto.getEmail());
        Rol rol = rolRepository.findById(dto.getRolId()).orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        usuarioExistente.setRol(rol);
        if (dto.getActivo() != null) {
            usuarioExistente.setActivo(dto.getActivo());
        }
        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
        usuarioActualizado = usuarioRepository.findById(usuarioActualizado.getId()).orElse(usuarioActualizado);
        UsuarioDto dtoResult = UsuarioMapper.toDto(usuarioActualizado);
        return dtoResult;
    }

    @Override
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID " + id));
        // Validación: no se puede eliminar el único administrador
        Rol adminRol = rolRepository.findByNombre("ADMINISTRADOR").orElseThrow();
        if (adminRol.equals(usuario.getRol()) && usuarioRepository.countByRol(adminRol) == 1) {
            throw new RuntimeException("No se puede eliminar el único administrador del sistema. Debe haber al menos un administrador.");
        }
        if (!usuario.getVehiculos().isEmpty() || !usuario.getReservas().isEmpty() || !usuario.getSanciones().isEmpty() || !usuario.getHistorialUso().isEmpty()) {
            throw new RuntimeException("No se puede eliminar el usuario porque tiene vehículos, reservas, sanciones o historial asociado. Use desactivarUsuario() en su lugar.");
        }
        usuarioRepository.deleteById(id);
    }

    /**
     * Desactiva un usuario en lugar de eliminarlo (recomendado)
     */
    public void desactivarUsuario(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID " + id));
        Rol adminRol = rolRepository.findByNombre("ADMINISTRADOR").orElseThrow();
        if (adminRol.equals(usuario.getRol()) && usuarioRepository.countByRol(adminRol) == 1) {
            throw new RuntimeException("No se puede desactivar el único administrador del sistema.");
        }
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    /**
     * Activa un usuario que estaba desactivado
     */
    public void activarUsuario(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID " + id));
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }

    /**
     * Elimina un usuario y todas sus relaciones (eliminación completa)
     */
    public void eliminarUsuarioCompleto(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID " + id));
        Rol adminRol = rolRepository.findByNombre("ADMINISTRADOR").orElseThrow();
        if (adminRol.equals(usuario.getRol()) && usuarioRepository.countByRol(adminRol) == 1) {
            throw new RuntimeException("No se puede eliminar el único administrador del sistema.");
        }
        // 1. Eliminar todas las sanciones asociadas a los vehículos del usuario
        List<Long> vehiculoIds = usuario.getVehiculos().stream()
            .map(Vehiculo::getId)
            .toList();
        if (!vehiculoIds.isEmpty()) {
            List<Sancion> sancionesVehiculos = sancionRepository.findAll().stream()
                .filter(s -> s.getVehiculo() != null && vehiculoIds.contains(s.getVehiculo().getId()))
                .toList();
            sancionRepository.deleteAll(sancionesVehiculos);
        }
        // 2. Eliminar todas las sanciones asociadas directamente al usuario
        sancionRepository.deleteAll(usuario.getSanciones());
        // 3. Eliminar reservas y vehículos
        reservaRepository.deleteAll(usuario.getReservas());
        vehiculoRepository.deleteAll(usuario.getVehiculos());
        // 4. Eliminar usuario (historialUso se elimina en cascada)
        usuarioRepository.delete(usuario);
    }

    @Override
    public Optional<UsuarioDto> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return usuarioRepository.findByUsername(username)
                .map(UsuarioMapper::toDto);
    }

    @Override
    public Optional<UsuarioDto> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return usuarioRepository.findByEmail(email)
                .map(UsuarioMapper::toDto);
    }

    @Override
    public List<UsuarioDto> findByRolNombre(String rolNombre) {
        Rol rol = rolRepository.findByNombre(rolNombre)
            .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + rolNombre));
        return usuarioRepository.findByRol(rol).stream()
            .map(UsuarioMapper::toDto)
            .toList();
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosSuspendidos() {
        // Busca usuarios con sanción activa de suspensión (temporal o ciclo lectivo)
        List<Usuario> suspendidos = usuarioRepository.findUsuariosSuspendidos("suspensión");
        return suspendidos.stream().map(UsuarioMapper::toDto).toList();
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosConSancionActiva() {
        List<Usuario> usuarios = usuarioRepository.findUsuariosConSancionActiva();
        return usuarios.stream().map(UsuarioMapper::toDto).toList();
    }

    // === Validaciones básicas ===
    private void validarDatosUsuario(UsuarioDto dto) {
        // Validar campos obligatorios
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        if (dto.getNombreCompleto() == null || dto.getNombreCompleto().isBlank()) {
            throw new IllegalArgumentException("El nombre completo no puede estar vacío");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        // Validar rol
        if (dto.getRolId() == null) {
            throw new IllegalArgumentException("El ID del rol no puede estar vacío");
        }
    }
}
