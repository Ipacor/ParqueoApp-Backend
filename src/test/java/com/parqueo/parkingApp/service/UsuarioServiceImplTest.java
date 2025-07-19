package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.UsuarioDto;
import com.parqueo.parkingApp.mapper.UsuarioMapper;
import com.parqueo.parkingApp.model.Rol;
import com.parqueo.parkingApp.model.Usuario;
import com.parqueo.parkingApp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private VehiculoRepository vehiculoRepository;
    @Mock private SancionRepository sancionRepository;
    @Mock private HistorialUsoRepository historialUsoRepository;
    @Mock private EscaneoQRRepository escaneoQRRepository;
    @Mock private SancionDetalleRepository sancionDetalleRepository;
    @Mock private RolRepository rolRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuario;
    private UsuarioDto usuarioDto;
    private Rol rolUsuario;
    private Rol rolAdmin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rolUsuario = new Rol();
        rolUsuario.setId(1L);
        rolUsuario.setNombre("USUARIO");
        rolAdmin = new Rol();
        rolAdmin.setId(2L);
        rolAdmin.setNombre("ADMINISTRADOR");
        usuario = Usuario.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .nombreCompleto("Test User")
                .email("test@example.com")
                .rol(rolUsuario)
                .activo(true)
                .build();
        usuarioDto = UsuarioMapper.toDto(usuario);
        // Mock global catch-all para todos los tests
        when(rolRepository.findById(anyLong())).thenReturn(Optional.of(rolUsuario));
        when(rolRepository.findByNombre(anyString())).thenReturn(Optional.of(rolUsuario));
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(usuarioRepository.findByRol(any())).thenReturn(List.of(usuario));
        when(usuarioRepository.save(any())).thenReturn(usuario);
        when(usuarioRepository.existsByUsername(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.countByRol(any())).thenReturn(2L);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        doNothing().when(usuarioRepository).deleteById(anyLong());
        doNothing().when(usuarioRepository).delete(any());
        doNothing().when(sancionRepository).deleteAll(anyList());
        doNothing().when(reservaRepository).deleteAll(anyList());
        doNothing().when(vehiculoRepository).deleteAll(anyList());
    }

    @Test
    void obtenerTodos_debeRetornarListaUsuarios() {
        List<UsuarioDto> result = usuarioService.obtenerTodos();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
    }

    @Test
    void obtenerPorId_existente_debeRetornarUsuario() {
        UsuarioDto result = usuarioService.obtenerPorId(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void crear_usuarioNuevo_debeGuardarYRetornarUsuario() {
        UsuarioDto result = usuarioService.crear(usuarioDto);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void actualizar_usuarioExistente_debeActualizarYRetornarUsuario() {
        UsuarioDto result = usuarioService.actualizar(1L, usuarioDto);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void eliminar_usuarioExistente_debeEliminarUsuario() {
        usuario.setVehiculos(new HashSet<>());
        usuario.setReservas(new HashSet<>());
        usuario.setSanciones(new HashSet<>());
        usuario.setHistorialUso(new HashSet<>());
        usuarioService.eliminar(1L);
    }

    @Test
    void desactivarUsuario_usuarioActivo_debeDesactivar() {
        usuario.setActivo(true);
        usuarioService.desactivarUsuario(1L);
        assertThat(usuario.getActivo()).isFalse();
    }

    @Test
    void activarUsuario_usuarioInactivo_debeActivar() {
        usuario.setActivo(false);
        usuarioService.activarUsuario(1L);
        assertThat(usuario.getActivo()).isTrue();
    }

    @Test
    void eliminarUsuarioCompleto_usuarioExistente_debeEliminarUsuarioYRelaciones() {
        usuario.setVehiculos(new HashSet<>());
        usuario.setReservas(new HashSet<>());
        usuario.setSanciones(new HashSet<>());
        usuario.setHistorialUso(new HashSet<>());
        usuarioService.eliminarUsuarioCompleto(1L);
    }

    @Test
    void findByUsername_existente_debeRetornarUsuario() {
        Optional<UsuarioDto> result = usuarioService.findByUsername(usuario.getUsername());
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void findByEmail_existente_debeRetornarUsuario() {
        Optional<UsuarioDto> result = usuarioService.findByEmail(usuario.getEmail());
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByRolNombre_existente_debeRetornarUsuarios() {
        List<UsuarioDto> result = usuarioService.findByRolNombre("USUARIO");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRolNombre()).isEqualTo("USUARIO");
    }

    @Test
    void obtenerUsuariosSuspendidos_debeRetornarUsuariosSuspendidos() {
        when(usuarioRepository.findUsuariosSuspendidos("suspensión")).thenReturn(List.of(usuario));
        List<UsuarioDto> result = usuarioService.obtenerUsuariosSuspendidos();
        assertThat(result).hasSize(1);
    }

    @Test
    void obtenerUsuariosConSancionActiva_debeRetornarUsuarios() {
        when(usuarioRepository.findUsuariosConSancionActiva()).thenReturn(List.of(usuario));
        List<UsuarioDto> result = usuarioService.obtenerUsuariosConSancionActiva();
        assertThat(result).hasSize(1);
    }
} 