package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.SancionDetalleDto;
import com.parqueo.parkingApp.mapper.SancionDetalleMapper;
import com.parqueo.parkingApp.model.SancionDetalle;
import com.parqueo.parkingApp.model.Sancion;
import com.parqueo.parkingApp.model.ReglasEstacionamiento;
import com.parqueo.parkingApp.repository.SancionDetalleRepository;
import com.parqueo.parkingApp.repository.SancionRepository;
import com.parqueo.parkingApp.repository.ReglasEstacionamientoRepository;
import com.parqueo.parkingApp.service.SancionDetalleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SancionDetalleServiceImpl implements SancionDetalleService {

    private final SancionDetalleRepository repository;
    private final SancionDetalleMapper mapper;

    @Autowired
    private SancionRepository sancionRepository;
    @Autowired
    private ReglasEstacionamientoRepository reglasRepository;

    @Override
    public List<SancionDetalleDto> obtenerTodos() {
        return repository.findAll().stream()
                .map(SancionDetalleMapper::toDto)
                .toList();
    }

    @Override
    public SancionDetalleDto obtenerPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        SancionDetalle detalle = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Detalle de sanción no encontrado con ID: " + id));
        return SancionDetalleMapper.toDto(detalle);
    }

    @Override
    public SancionDetalleDto crear(SancionDetalleDto dto) {
        validarDatosSancionDetalle(dto);
        SancionDetalle nuevo = SancionDetalleMapper.toEntity(dto);
        // Buscar y setear la sanción
        Sancion sancion = sancionRepository.findById(dto.getSancionId())
            .orElseThrow(() -> new EntityNotFoundException("Sanción no encontrada"));
        nuevo.setSancion(sancion);
        // Buscar y setear la regla
        ReglasEstacionamiento regla = reglasRepository.findById(dto.getReglaId())
            .orElseThrow(() -> new EntityNotFoundException("Regla no encontrada"));
        nuevo.setRegla(regla);
        return SancionDetalleMapper.toDto(repository.save(nuevo));
    }

    @Override
    public SancionDetalleDto actualizar(Long id, SancionDetalleDto dto) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Detalle de sanción no encontrado con ID: " + id);
        }

        validarDatosSancionDetalle(dto);

        SancionDetalle actualizado = SancionDetalleMapper.toEntity(dto);
        actualizado.setId(id);

        return SancionDetalleMapper.toDto(repository.save(actualizado));
    }

    @Override
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Detalle de sanción no encontrado con ID: " + id);
        }
        
        repository.deleteById(id);
    }

    @Override
    public List<SancionDetalleDto> buscarPorSancion(Long sancionId) {
        if (sancionId == null) {
            throw new IllegalArgumentException("El ID de la sanción no puede ser null");
        }
        
        return repository.findBySancionId(sancionId).stream()
                .map(SancionDetalleMapper::toDto)
                .toList();
    }

    private void validarDatosSancionDetalle(SancionDetalleDto dto) {
        if (dto.getSancionId() == null) {
            throw new IllegalArgumentException("El ID de la sanción no puede estar vacío");
        }
        if (dto.getReglaId() == null) {
            throw new IllegalArgumentException("El ID de la regla no puede estar vacío");
        }
        if (dto.getDescripcion() == null || dto.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción no puede estar vacía");
        }
        if (dto.getDescripcion().trim().length() < 5) {
            throw new IllegalArgumentException("La descripción debe tener al menos 5 caracteres");
        }
        if (dto.getDescripcion().trim().length() > 500) {
            throw new IllegalArgumentException("La descripción no puede tener más de 500 caracteres");
        }
        if (dto.getFechaSancion() == null) {
            throw new IllegalArgumentException("La fecha de sanción no puede estar vacía");
        }
        if (dto.getEstado() == null || dto.getEstado().trim().isEmpty()) {
            throw new IllegalArgumentException("El estado no puede estar vacío");
        }
        if (!dto.getEstado().equals("ACTIVO") && !dto.getEstado().equals("RESUELTO") && !dto.getEstado().equals("ANULADO")) {
            throw new IllegalArgumentException("El estado debe ser ACTIVO, RESUELTO o ANULADO");
        }
    }
}