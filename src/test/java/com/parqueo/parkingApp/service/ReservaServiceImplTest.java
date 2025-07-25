package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.ReservaDto;
import com.parqueo.parkingApp.mapper.ReservaMapper;
import com.parqueo.parkingApp.model.*;
import com.parqueo.parkingApp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    @Mock private ReservaRepository reservaRepo;
    @Mock private UsuarioRepository usuarioRepo;
    @Mock private VehiculoRepository vehiculoRepo;
    @Mock private EspacioDisponibleRepository espacioRepo;
    @Mock private EscaneoQRRepository escaneoQRRepo;
    @Mock private HistorialUsoService historialUsoService;
    @Mock private NotificacionService notificacionService;
    @Mock private SancionRepository sancionRepository;
    @Mock private SancionService sancionService;

    @InjectMocks
    private ReservaServiceImpl reservaService;

    private Reserva reserva;
    private ReservaDto reservaDto;
    private Usuario usuario;
    private Vehiculo vehiculo;
    private EspacioDisponible espacio;
    private EscaneoQR escaneoQR;

    @BeforeEach
    void setUp() {
        // Configurar usuario
        usuario = Usuario.builder()
                .id(1L)
                .username("testuser")
                .nombreCompleto("Test User")
                .email("test@example.com")
                .activo(true)
                .build();

        // Configurar vehículo
        vehiculo = Vehiculo.builder()
                .id(1L)
                .placa("ABC123")
                .marca("Toyota")
                .modelo("Corolla")
                .color("Blanco")
                .tipo(Vehiculo.TipoVehiculo.AUTOMOVIL)
                .usuario(usuario)
                .activo(true)
                .build();

        // Configurar espacio
        espacio = EspacioDisponible.builder()
                .id(1L)
                .ubicacion("Zona A")
                .numeroEspacio("A1")
                .zona("A")
                .estado(EspacioDisponible.EstadoEspacio.DISPONIBLE)
                .build();

        // Configurar escaneo QR
        escaneoQR = EscaneoQR.builder()
                .id(1L)
                .token("QR123456")
                .tipo("ENTRADA")
                .fechaInicioValidez(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusHours(1))
                .build();

        // Configurar reserva
        reserva = Reserva.builder()
                .id(1L)
                .usuario(usuario)
                .vehiculo(vehiculo)
                .espacio(espacio)
                .fechaHoraInicio(LocalDateTime.now().plusHours(1))
                .fechaHoraFin(LocalDateTime.now().plusHours(2))
                .estado(Reserva.EstadoReserva.ACTIVO)
                .escaneoQR(escaneoQR)
                .build();

        // Configurar DTO
        reservaDto = ReservaDto.builder()
                .id(1L)
                .usuarioId(1L)
                .vehiculoId(1L)
                .espacioId(1L)
                .fechaHoraInicio(LocalDateTime.now().plusHours(1))
                .fechaHoraFin(LocalDateTime.now().plusHours(2))
                .estado(Reserva.EstadoReserva.ACTIVO)
                .build();

        // Inyectar manualmente los servicios que no son finales
        ReflectionTestUtils.setField(reservaService, "historialUsoService", historialUsoService);
        ReflectionTestUtils.setField(reservaService, "notificacionService", notificacionService);
        ReflectionTestUtils.setField(reservaService, "sancionRepository", sancionRepository);
        ReflectionTestUtils.setField(reservaService, "sancionService", sancionService);
    }

    @Test
    void obtenerTodos_debeRetornarListaReservas() {
        // Configurar mock específico para este test
        when(reservaRepo.findAll()).thenReturn(List.of(reserva));
        when(escaneoQRRepo.findByReserva(any())).thenReturn(Optional.of(escaneoQR));
        
        List<ReservaDto> result = reservaService.obtenerTodos();
        
        assertThat(result).isNotNull();
        verify(reservaRepo).findAll();
        verify(escaneoQRRepo, times(1)).findByReserva(any());
    }

    @Test
    void obtenerPorId_existente_debeRetornarReserva() {
        // Configurar mock específico para este test
        when(reservaRepo.findById(1L)).thenReturn(Optional.of(reserva));
        when(escaneoQRRepo.findByReserva(any())).thenReturn(Optional.of(escaneoQR));
        
        ReservaDto result = reservaService.obtenerPorId(1L);
        
        assertThat(result).isNotNull();
        verify(reservaRepo).findById(1L);
        verify(escaneoQRRepo).findByReserva(any());
    }

    @Test
    void obtenerPorId_nullId_debeLanzarExcepcion() {
        assertThatThrownBy(() -> reservaService.obtenerPorId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El ID no puede ser null");
    }

    @Test
    void obtenerPorId_noExistente_debeLanzarExcepcion() {
        when(reservaRepo.findById(999L)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> reservaService.obtenerPorId(999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class)
                .hasMessage("Reserva no encontrada con ID: 999");
    }

    @Test
    void crear_reservaValida_debeGuardarYRetornarReserva() {
        // Configurar mocks necesarios para crear una reserva válida
        when(espacioRepo.findById(anyLong())).thenReturn(Optional.of(espacio));
        when(usuarioRepo.findById(anyLong())).thenReturn(Optional.of(usuario));
        when(vehiculoRepo.findById(anyLong())).thenReturn(Optional.of(vehiculo));
        when(reservaRepo.findByEstado(Reserva.EstadoReserva.RESERVADO)).thenReturn(List.of());
        when(reservaRepo.save(any())).thenReturn(reserva);
        when(escaneoQRRepo.save(any())).thenReturn(escaneoQR);
        when(espacioRepo.save(any())).thenReturn(espacio);

        ReservaDto result = reservaService.crear(reservaDto);

        assertThat(result).isNotNull();
        verify(reservaRepo).save(any());
        verify(escaneoQRRepo).save(any());
    }

    @Test
    void crear_espacioOcupado_debeLanzarExcepcion() {
        // Simula que ya existe una reserva activa para ese espacio
        Reserva reservaOcupada = Reserva.builder()
            .id(2L)
            .espacio(espacio)
            .estado(Reserva.EstadoReserva.RESERVADO)
            .build();

        when(espacioRepo.findById(anyLong())).thenReturn(Optional.of(espacio));
        when(usuarioRepo.findById(anyLong())).thenReturn(Optional.of(usuario));
        when(vehiculoRepo.findById(anyLong())).thenReturn(Optional.of(vehiculo));
        when(reservaRepo.findByEstado(Reserva.EstadoReserva.RESERVADO)).thenReturn(List.of(reservaOcupada));

        assertThatThrownBy(() -> reservaService.crear(reservaDto))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("El espacio ya está reservado");
    }

    @Test
    void actualizar_reservaExistente_debeActualizarYRetornarReserva() {
        // Configurar mocks específicos para este test
        when(reservaRepo.findById(1L)).thenReturn(Optional.of(reserva));
        when(espacioRepo.findById(anyLong())).thenReturn(Optional.of(espacio));
        when(usuarioRepo.findById(anyLong())).thenReturn(Optional.of(usuario));
        when(vehiculoRepo.findById(anyLong())).thenReturn(Optional.of(vehiculo));
        when(reservaRepo.save(any())).thenReturn(reserva);
        when(espacioRepo.save(any())).thenReturn(espacio);
        
        ReservaDto result = reservaService.actualizar(1L, reservaDto);
        
        assertThat(result).isNotNull();
        verify(reservaRepo).save(any());
    }

    @Test
    void eliminar_reservaExistente_debeEliminarReserva() {
        // Configurar mocks específicos para este test
        when(reservaRepo.findById(1L)).thenReturn(Optional.of(reserva));
        
        reservaService.eliminar(1L);
        
        verify(reservaRepo).deleteById(1L);
        verify(escaneoQRRepo).deleteByReservaId(1L);
    }

    @Test
    void buscarPorUsuario_debeRetornarReservasDelUsuario() {
        // Configurar mock específico para este test
        when(reservaRepo.findByUsuarioId(1L)).thenReturn(List.of(reserva));
        when(escaneoQRRepo.findByReserva(any())).thenReturn(Optional.of(escaneoQR));
        
        List<ReservaDto> result = reservaService.buscarPorUsuario(1L);
        
        assertThat(result).isNotNull();
        verify(reservaRepo).findByUsuarioId(1L);
    }

    @Test
    void buscarPorVehiculo_debeRetornarReservasDelVehiculo() {
        // Configurar mock específico para este test
        when(reservaRepo.findByVehiculoId(1L)).thenReturn(List.of(reserva));
        when(escaneoQRRepo.findByReserva(any())).thenReturn(Optional.of(escaneoQR));
        
        List<ReservaDto> result = reservaService.buscarPorVehiculo(1L);
        
        assertThat(result).isNotNull();
        verify(reservaRepo).findByVehiculoId(1L);
    }

    @Test
    void buscarPorEspacio_debeRetornarReservasDelEspacio() {
        // Configurar mock específico para este test
        when(reservaRepo.findByEspacioId(1L)).thenReturn(List.of(reserva));
        when(escaneoQRRepo.findByReserva(any())).thenReturn(Optional.of(escaneoQR));
        
        List<ReservaDto> result = reservaService.buscarPorEspacio(1L);
        
        assertThat(result).isNotNull();
        verify(reservaRepo).findByEspacioId(1L);
    }

    @Test
    void buscarPorEstado_debeRetornarReservasConEstado() {
        // Configurar mock específico para este test
        when(reservaRepo.findByEstado(Reserva.EstadoReserva.ACTIVO)).thenReturn(List.of(reserva));
        when(escaneoQRRepo.findByReserva(any())).thenReturn(Optional.of(escaneoQR));
        
        List<ReservaDto> result = reservaService.buscarPorEstado(Reserva.EstadoReserva.ACTIVO);
        
        assertThat(result).isNotNull();
        verify(reservaRepo).findByEstado(Reserva.EstadoReserva.ACTIVO);
    }

    @Test
    void buscarReservasActivas_debeRetornarReservasActivas() {
        // Configurar mock específico para este test usando el método correcto
        when(reservaRepo.findReservasActivas(any(), any())).thenReturn(List.of(reserva));
        when(escaneoQRRepo.findByReserva(any())).thenReturn(Optional.of(escaneoQR));
        
        List<ReservaDto> result = reservaService.buscarReservasActivas();
        
        assertThat(result).isNotNull();
        verify(reservaRepo).findReservasActivas(any(), any());
    }

    @Test
    void forzarExpiracionReserva_debeExpirarReserva() {
        // Configurar reserva en estado RESERVADO para este test
        Reserva reservaReservada = Reserva.builder()
                .id(1L)
                .usuario(usuario)
                .vehiculo(vehiculo)
                .espacio(espacio)
                .fechaHoraInicio(LocalDateTime.now().plusHours(1))
                .fechaHoraFin(LocalDateTime.now().plusHours(2))
                .estado(Reserva.EstadoReserva.RESERVADO) // Estado correcto para forzar expiración
                .escaneoQR(escaneoQR)
                .build();
        
        // Configurar mocks específicos para este test
        when(reservaRepo.findById(1L)).thenReturn(Optional.of(reservaReservada));
        when(reservaRepo.save(any())).thenReturn(reservaReservada);
        
        reservaService.forzarExpiracionReserva(1L);
        
        verify(reservaRepo).save(any());
        // No verificar eliminación de QR porque forzarExpiracionReserva no lo elimina
    }
}
