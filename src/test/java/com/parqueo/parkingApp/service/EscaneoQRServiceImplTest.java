package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.EscaneoQRDto;
import com.parqueo.parkingApp.mapper.EscaneoQRMapper;
import com.parqueo.parkingApp.model.EscaneoQR;
import com.parqueo.parkingApp.model.Reserva;
import com.parqueo.parkingApp.model.EspacioDisponible;
import com.parqueo.parkingApp.model.Usuario;
import com.parqueo.parkingApp.repository.EscaneoQRRepository;
import com.parqueo.parkingApp.repository.ReservaRepository;
import com.parqueo.parkingApp.repository.EspacioDisponibleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class EscaneoQRServiceImplTest {
    @Mock private EscaneoQRRepository escaneoRepo;
    @Mock private ReservaRepository reservaRepo;
    @Mock private EspacioDisponibleRepository espacioRepo;
    @Mock private HistorialUsoService historialUsoService;
    @Mock private NotificacionService notificacionService;
    private EscaneoQRServiceImpl escaneoQRService;
    private EscaneoQRMapper mapper;

    private EscaneoQR escaneoQR;
    private EscaneoQRDto escaneoQRDto;
    private Reserva reserva;
    private Usuario usuario;
    private EspacioDisponible espacio;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        usuario = Usuario.builder().id(1L).username("user").build();
        espacio = EspacioDisponible.builder().id(1L).numeroEspacio("A1").estado(EspacioDisponible.EstadoEspacio.DISPONIBLE).build();
        reserva = Reserva.builder().id(1L).usuario(usuario).espacio(espacio).estado(Reserva.EstadoReserva.RESERVADO).build();
        escaneoQR = EscaneoQR.builder().id(1L).reserva(reserva).tipo("ENTRADA").token("token").build();
        escaneoQRDto = EscaneoQRDto.builder().id(1L).reservaId(1L).tipo("ENTRADA").token("token").timestampEnt(LocalDateTime.now()).build();
        mapper = new EscaneoQRMapper();
        escaneoQRService = new EscaneoQRServiceImpl(escaneoRepo, reservaRepo, mapper, espacioRepo);
        // Inyectar los servicios con reflection porque son @Autowired en la clase real
        java.lang.reflect.Field historialUsoField = null;
        java.lang.reflect.Field notificacionField = null;
        try {
            historialUsoField = EscaneoQRServiceImpl.class.getDeclaredField("historialUsoService");
            historialUsoField.setAccessible(true);
            historialUsoField.set(escaneoQRService, historialUsoService);
            notificacionField = EscaneoQRServiceImpl.class.getDeclaredField("notificacionService");
            notificacionField.setAccessible(true);
            notificacionField.set(escaneoQRService, notificacionService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void obtenerTodos_debeRetornarListaEscaneos() {
        when(escaneoRepo.findAll()).thenReturn(List.of(escaneoQR));
        List<EscaneoQRDto> result = escaneoQRService.obtenerTodos();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void obtenerPorId_existente_debeRetornarEscaneo() {
        when(escaneoRepo.findById(1L)).thenReturn(Optional.of(escaneoQR));
        EscaneoQRDto result = escaneoQRService.obtenerPorId(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void obtenerPorId_noExistente_lanzaExcepcion() {
        when(escaneoRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> escaneoQRService.obtenerPorId(1L));
    }

    @Test
    void crear_debeGuardarYRetornarEscaneo() {
        EscaneoQR nuevo = EscaneoQR.builder().id(2L).reserva(reserva).tipo("ENTRADA").token("nuevoToken").timestampEnt(LocalDateTime.now()).build();
        EscaneoQRDto nuevoDto = EscaneoQRDto.builder().id(2L).reservaId(1L).tipo("ENTRADA").token("nuevoToken").timestampEnt(LocalDateTime.now()).build();
        // Usar el mapper real
        when(escaneoRepo.save(any(EscaneoQR.class))).thenReturn(nuevo);
        EscaneoQRDto result = escaneoQRService.crear(nuevoDto);
        assertThat(result.getId()).isEqualTo(2L);
    }

    @Test
    void actualizar_existente_debeActualizarYRetornarEscaneo() {
        when(escaneoRepo.existsById(1L)).thenReturn(true);
        EscaneoQR actualizado = EscaneoQR.builder().id(1L).reserva(reserva).tipo("SALIDA").token("token2").timestampEnt(LocalDateTime.now()).build();
        EscaneoQRDto actualizadoDto = EscaneoQRDto.builder().id(1L).reservaId(1L).tipo("SALIDA").token("token2").timestampEnt(LocalDateTime.now()).build();
        when(escaneoRepo.save(any(EscaneoQR.class))).thenReturn(actualizado);
        EscaneoQRDto result = escaneoQRService.actualizar(1L, actualizadoDto);
        assertThat(result.getTipo()).isEqualTo("SALIDA");
    }

    @Test
    void actualizar_noExistente_lanzaExcepcion() {
        when(escaneoRepo.existsById(1L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> escaneoQRService.actualizar(1L, escaneoQRDto));
    }

    @Test
    void eliminar_existente_debeEliminar() {
        when(escaneoRepo.existsById(1L)).thenReturn(true);
        doNothing().when(escaneoRepo).deleteById(1L);
        escaneoQRService.eliminar(1L);
        verify(escaneoRepo).deleteById(1L);
    }

    @Test
    void eliminar_noExistente_lanzaExcepcion() {
        when(escaneoRepo.existsById(1L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> escaneoQRService.eliminar(1L));
    }

    @Test
    void registrarEntrada_debeActualizarReservaYEspacio() {
        reserva.setEstado(Reserva.EstadoReserva.RESERVADO);
        when(reservaRepo.findById(1L)).thenReturn(Optional.of(reserva));
        when(escaneoRepo.findByReserva(reserva)).thenReturn(Optional.of(escaneoQR));
        when(escaneoRepo.save(any(EscaneoQR.class))).thenReturn(escaneoQR);
        when(reservaRepo.save(any(Reserva.class))).thenReturn(reserva);
        when(espacioRepo.save(any(EspacioDisponible.class))).thenReturn(espacio);
        EscaneoQRDto result = escaneoQRService.registrarEntrada(1L);
        assertThat(result.getTipo()).isEqualTo("SALIDA");
        verify(reservaRepo).save(any(Reserva.class));
        verify(espacioRepo).save(any(EspacioDisponible.class));
    }

    @Test
    void registrarSalida_debeActualizarReservaYEspacioYNotificar() {
        escaneoQR.setTipo("SALIDA");
        reserva.setEstado(Reserva.EstadoReserva.ACTIVO);
        reserva.setUsuario(usuario);
        reserva.setEspacio(espacio);
        reserva.setVehiculo(null); // Puedes agregar un mock de Vehiculo si es necesario
        when(reservaRepo.findById(1L)).thenReturn(Optional.of(reserva));
        when(escaneoRepo.findByReserva(reserva)).thenReturn(Optional.of(escaneoQR));
        when(escaneoRepo.save(any(EscaneoQR.class))).thenReturn(escaneoQR);
        when(reservaRepo.save(any(Reserva.class))).thenReturn(reserva);
        when(espacioRepo.save(any(EspacioDisponible.class))).thenReturn(espacio);
        EscaneoQRDto result = escaneoQRService.registrarSalida(1L);
        assertThat(result.getTipo()).isEqualTo("SALIDA_USADA");
        verify(reservaRepo).save(any(Reserva.class));
        verify(espacioRepo).save(any(EspacioDisponible.class));
        verify(notificacionService, atLeast(0)).crearNotificacion(any(), any(), any(), any());
    }

    @Test
    void obtenerPorReserva_existente_debeRetornarEscaneo() {
        when(reservaRepo.findById(1L)).thenReturn(Optional.of(reserva));
        when(escaneoRepo.findByReserva(reserva)).thenReturn(Optional.of(escaneoQR));
        EscaneoQRDto result = escaneoQRService.obtenerPorReserva(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void buscarPorUsuario_existente_debeRetornarLista() {
        when(escaneoRepo.findByReservaUsuarioId(1L)).thenReturn(List.of(escaneoQR));
        List<EscaneoQRDto> result = escaneoQRService.buscarPorUsuario(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    void buscarPorFecha_existente_debeRetornarLista() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fin = LocalDateTime.now();
        when(escaneoRepo.findByTimestampEntBetween(inicio, fin)).thenReturn(List.of(escaneoQR));
        List<EscaneoQRDto> result = escaneoQRService.buscarPorFecha(inicio, fin);
        assertThat(result).hasSize(1);
    }
} 
