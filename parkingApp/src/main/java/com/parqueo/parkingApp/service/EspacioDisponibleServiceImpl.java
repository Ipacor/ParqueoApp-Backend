package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.EspacioDisponibleDto;
import com.parqueo.parkingApp.mapper.EspacioMapper;
import com.parqueo.parkingApp.model.EspacioDisponible;
import com.parqueo.parkingApp.repository.EspacioDisponibleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EspacioDisponibleServiceImpl implements EspacioDisponibleService {

    private final EspacioDisponibleRepository repo;
    private final EspacioMapper mapper;

    @Override
    public List<EspacioDisponibleDto> obtenerTodos() {
        return repo.findAll().stream().map(EspacioMapper::toDto).toList();
    }

    @Override
    public EspacioDisponibleDto obtenerPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        EspacioDisponible espacio = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Espacio no encontrado con ID: " + id));
        return EspacioMapper.toDto(espacio);
    }

    @Override
    public EspacioDisponibleDto crear(EspacioDisponibleDto dto) {
        validarDatosEspacio(dto);
        
        EspacioDisponible espacio = EspacioMapper.toEntity(dto);
        // Inicializar valores por defecto si están en null
        if (espacio.getFechaRegistro() == null) {
            espacio.setFechaRegistro(java.time.LocalDateTime.now());
        }
        if (espacio.getActivo() == null) {
            espacio.setActivo(Boolean.TRUE);
        }
        return EspacioMapper.toDto(repo.save(espacio));
    }

    @Override
    public EspacioDisponibleDto actualizar(Long id, EspacioDisponibleDto dto) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Espacio no encontrado con ID: " + id);
        }

        EspacioDisponible espacioExistente = repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Espacio no encontrado con ID: " + id));

        validarDatosEspacio(dto);
        
        EspacioDisponible espacio = EspacioMapper.toEntity(dto);
        espacio.setId(id);
        // Preservar fechaRegistro original si no viene en el DTO
        if (dto.getFechaRegistro() == null) {
            espacio.setFechaRegistro(espacioExistente.getFechaRegistro());
        }
        // Preservar el valor de 'activo'
        if (dto.getActivo() == null) {
            espacio.setActivo(espacioExistente.getActivo());
        }
        if (espacio.getActivo() == null) {
            espacio.setActivo(Boolean.TRUE);
        }
        // PROTECCIÓN: Si el espacio está RESERVADO u OCUPADO, mantener el estado actual
        if (espacioExistente.getEstado() == EspacioDisponible.EstadoEspacio.RESERVADO ||
            espacioExistente.getEstado() == EspacioDisponible.EstadoEspacio.OCUPADO) {
            espacio.setEstado(espacioExistente.getEstado());
        }
        return EspacioMapper.toDto(repo.save(espacio));
    }

    @Override
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Espacio no encontrado con ID: " + id);
        }
        
        repo.deleteById(id);
    }

    @Override
    public List<EspacioDisponibleDto> buscarPorEstado(EspacioDisponible.EstadoEspacio estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser null");
        }
        
        return repo.findByEstado(estado).stream()
                .map(EspacioMapper::toDto)
                .toList();
    }

    @Override
    public List<EspacioDisponibleDto> buscarPorZona(String zona) {
        if (zona == null || zona.trim().isEmpty()) {
            throw new IllegalArgumentException("La zona no puede estar vacía");
        }
        
        return repo.findByZona(zona).stream()
                .map(EspacioMapper::toDto)
                .toList();
    }

    @Override
    public List<EspacioDisponibleDto> buscarDisponibles() {
        return repo.findByEstado(EspacioDisponible.EstadoEspacio.DISPONIBLE).stream()
                .map(EspacioMapper::toDto)
                .toList();
    }

    private void validarDatosEspacio(EspacioDisponibleDto dto) {
        if (dto.getUbicacion() == null || dto.getUbicacion().trim().isEmpty()) {
            throw new IllegalArgumentException("La ubicación no puede estar vacía");
        }
        
        if (dto.getNumeroEspacio() == null || dto.getNumeroEspacio().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de espacio no puede estar vacío");
        }
        
        if (dto.getZona() == null || dto.getZona().trim().isEmpty()) {
            throw new IllegalArgumentException("La zona no puede estar vacía");
        }
        
        if (dto.getEstado() == null) {
            throw new IllegalArgumentException("El estado no puede estar vacío");
        }
    }
}
