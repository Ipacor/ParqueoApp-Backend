package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.EspacioDisponibleDto;
import com.parqueo.parkingApp.mapper.EspacioMapper;
import com.parqueo.parkingApp.model.EspacioDisponible;
import com.parqueo.parkingApp.model.EspacioDisponible.EstadoEspacio;
import com.parqueo.parkingApp.repository.EspacioDisponibleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EspacioDisponibleServiceImplTest {
    @Mock
    private EspacioDisponibleRepository repo;

    @InjectMocks
    private EspacioDisponibleServiceImpl service;

    private EspacioDisponible espacio;
    private EspacioDisponibleDto dto;

    @BeforeEach
    void setUp() {
        // Solo inicializa los objetos, no hagas mocks aquí
        espacio = EspacioDisponible.builder()
                .id(1L)
                .ubicacion("A1")
                .numeroEspacio("101")
                .zona("Zona1")
                .estado(EstadoEspacio.DISPONIBLE)
                .capacidadMaxima(1)
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .build();
        dto = EspacioDisponibleDto.builder()
                .id(1L)
                .ubicacion("A1")
                .numeroEspacio("101")
                .zona("Zona1")
                .estado(EstadoEspacio.DISPONIBLE)
                .capacidadMaxima(1)
                .activo(true)
                .fechaRegistro(espacio.getFechaRegistro())
                .build();
    }

    @Test
    void obtenerTodos_debeRetornarLista() {
        when(repo.findAll()).thenReturn(List.of(espacio));
        try (MockedStatic<EspacioMapper> mapperMock = mockStatic(EspacioMapper.class)) {
            mapperMock.when(() -> EspacioMapper.toDto(any(EspacioDisponible.class))).thenReturn(dto);
            List<EspacioDisponibleDto> result = service.obtenerTodos();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUbicacion()).isEqualTo("A1");
        }
    }

    @Test
    void obtenerPorId_existente_debeRetornarDto() {
        when(repo.findById(1L)).thenReturn(Optional.of(espacio));
        try (MockedStatic<EspacioMapper> mapperMock = mockStatic(EspacioMapper.class)) {
            mapperMock.when(() -> EspacioMapper.toDto(any(EspacioDisponible.class))).thenReturn(dto);
            EspacioDisponibleDto result = service.obtenerPorId(1L);
            assertThat(result.getId()).isEqualTo(1L);
        }
    }

    @Test
    void obtenerPorId_noExistente_lanzaExcepcion() {
        when(repo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.obtenerPorId(1L));
    }

    @Test
    void crear_debeGuardarYRetornarDto() {
        try (MockedStatic<EspacioMapper> mapperMock = mockStatic(EspacioMapper.class)) {
            EspacioDisponible entidad = espacio;
            mapperMock.when(() -> EspacioMapper.toEntity(dto)).thenReturn(entidad);
            when(repo.save(any(EspacioDisponible.class))).thenReturn(entidad);
            mapperMock.when(() -> EspacioMapper.toDto(any(EspacioDisponible.class))).thenReturn(dto);
            EspacioDisponibleDto result = service.crear(dto);
            assertThat(result.getUbicacion()).isEqualTo("A1");
        }
    }

    @Test
    void actualizar_existente_debeActualizarYRetornarDto() {
        when(repo.existsById(1L)).thenReturn(true);
        when(repo.findById(1L)).thenReturn(Optional.of(espacio));
        try (MockedStatic<EspacioMapper> mapperMock = mockStatic(EspacioMapper.class)) {
            EspacioDisponible entidad = espacio;
            mapperMock.when(() -> EspacioMapper.toEntity(dto)).thenReturn(entidad);
            when(repo.save(any(EspacioDisponible.class))).thenReturn(entidad);
            mapperMock.when(() -> EspacioMapper.toDto(any(EspacioDisponible.class))).thenReturn(dto);
            EspacioDisponibleDto result = service.actualizar(1L, dto);
            assertThat(result.getId()).isEqualTo(1L);
        }
    }

    @Test
    void actualizar_noExistente_lanzaExcepcion() {
        when(repo.existsById(1L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> service.actualizar(1L, dto));
    }

    @Test
    void eliminar_existente_debeEliminar() {
        when(repo.existsById(1L)).thenReturn(true);
        doNothing().when(repo).deleteById(1L);
        service.eliminar(1L);
        verify(repo, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_noExistente_lanzaExcepcion() {
        when(repo.existsById(1L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> service.eliminar(1L));
    }

    @Test
    void buscarPorEstado_debeRetornarLista() {
        when(repo.findByEstado(EspacioDisponible.EstadoEspacio.DISPONIBLE)).thenReturn(List.of(espacio));
        try (MockedStatic<EspacioMapper> mapperMock = mockStatic(EspacioMapper.class)) {
            mapperMock.when(() -> EspacioMapper.toDto(any(EspacioDisponible.class))).thenReturn(dto);
            List<EspacioDisponibleDto> result = service.buscarPorEstado(EspacioDisponible.EstadoEspacio.DISPONIBLE);
            assertThat(result).hasSize(1);
        }
    }

    @Test
    void buscarPorZona_debeRetornarLista() {
        when(repo.findByZona("Zona1")).thenReturn(List.of(espacio));
        try (MockedStatic<EspacioMapper> mapperMock = mockStatic(EspacioMapper.class)) {
            mapperMock.when(() -> EspacioMapper.toDto(any(EspacioDisponible.class))).thenReturn(dto);
            List<EspacioDisponibleDto> result = service.buscarPorZona("Zona1");
            assertThat(result).hasSize(1);
        }
    }

    @Test
    void buscarDisponibles_debeRetornarLista() {
        when(repo.findByEstado(EspacioDisponible.EstadoEspacio.DISPONIBLE)).thenReturn(List.of(espacio));
        try (MockedStatic<EspacioMapper> mapperMock = mockStatic(EspacioMapper.class)) {
            mapperMock.when(() -> EspacioMapper.toDto(any(EspacioDisponible.class))).thenReturn(dto);
            List<EspacioDisponibleDto> result = service.buscarDisponibles();
            assertThat(result).hasSize(1);
        }
    }

    @Test
    void crear_conDatosInvalidos_lanzaExcepcion() {
        EspacioDisponibleDto dtoInvalido = EspacioDisponibleDto.builder()
                .ubicacion("")
                .numeroEspacio("")
                .zona("")
                .estado(null)
                .build();
        assertThrows(IllegalArgumentException.class, () -> service.crear(dtoInvalido));
    }
} 