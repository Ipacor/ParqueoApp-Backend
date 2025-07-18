package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.ReglasEstacionamientoDto;
import com.parqueo.parkingApp.mapper.ReglasEstacionamientoMapper;
import com.parqueo.parkingApp.model.ReglasEstacionamiento;
import com.parqueo.parkingApp.repository.ReglasEstacionamientoRepository;
import com.parqueo.parkingApp.service.ReglasEstacionamientoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.parqueo.parkingApp.model.Usuario;
import com.parqueo.parkingApp.model.Vehiculo;
import com.parqueo.parkingApp.model.EspacioDisponible;
import com.parqueo.parkingApp.model.Sancion;
import com.parqueo.parkingApp.model.SancionDetalle;
import com.parqueo.parkingApp.model.HistorialUso;
import com.parqueo.parkingApp.service.SancionService;
import com.parqueo.parkingApp.service.HistorialUsoService;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReglasEstacionamientoServiceImpl implements ReglasEstacionamientoService {

    private final ReglasEstacionamientoRepository repository;
    private final ReglasEstacionamientoMapper mapper;
    @Autowired
    private SancionService sancionService;
    @Autowired
    private HistorialUsoService historialUsoService;

    @Override
    public List<ReglasEstacionamientoDto> obtenerTodos() {
        return repository.findAll().stream()
                .map(ReglasEstacionamientoMapper::toDto)
                .toList();
    }

    @Override
    public ReglasEstacionamientoDto obtenerPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        ReglasEstacionamiento regla = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Regla no encontrada con id: " + id));
        return mapper.toDto(regla);
    }

    @Override
    public ReglasEstacionamientoDto crear(ReglasEstacionamientoDto dto) {
        validarDatosRegla(dto);
        
        ReglasEstacionamiento nueva = mapper.toEntity(dto);
        return mapper.toDto(repository.save(nueva));
    }

    @Override
    public ReglasEstacionamientoDto actualizar(Long id, ReglasEstacionamientoDto dto) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Regla no encontrada con id: " + id);
        }

        validarDatosRegla(dto);

        ReglasEstacionamiento actualizada = mapper.toEntity(dto);
        actualizada.setId(id);

        return mapper.toDto(repository.save(actualizada));
    }

    @Override
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Regla no encontrada con id: " + id);
        }
        
        repository.deleteById(id);
    }

    @Override
    public List<ReglasEstacionamientoDto> buscarPorTipo(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo no puede estar vacío");
        }
        
        return repository.findAll().stream()
                .filter(regla -> regla.getTipoFalta().name().toLowerCase().contains(tipo.toLowerCase()))
                .map(ReglasEstacionamientoMapper::toDto)
                .toList();
    }

    @Override
    public List<ReglasEstacionamientoDto> buscarActivas() {
        // Por ahora retornamos todas las reglas ya que no hay campo activa
        return repository.findAll().stream()
                .map(ReglasEstacionamientoMapper::toDto)
                .toList();
    }

    private void validarDatosRegla(ReglasEstacionamientoDto dto) {
        if (dto.getDescripcion() == null || dto.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción no puede estar vacía");
        }
        if (dto.getDescripcion().trim().length() < 5) {
            throw new IllegalArgumentException("La descripción debe tener al menos 5 caracteres");
        }
        if (dto.getDescripcion().trim().length() > 255) {
            throw new IllegalArgumentException("La descripción no puede tener más de 255 caracteres");
        }
        if (dto.getTipoFalta() == null) {
            throw new IllegalArgumentException("El tipo de falta no puede estar vacío");
        }
        if (!dto.getTipoFalta().name().equals("LEVE") && !dto.getTipoFalta().name().equals("GRAVE")) {
            throw new IllegalArgumentException("El tipo de falta debe ser LEVE o GRAVE");
        }
    }

    public void procesarInfraccion(Usuario usuario, Vehiculo vehiculo, EspacioDisponible espacio, String tipoInfraccion, String descripcion) {
        // Buscar regla correspondiente
        List<ReglasEstacionamiento> reglas = repository.findAll();
        ReglasEstacionamiento regla = reglas.stream()
            .filter(r -> r.getTipoFalta().name().equalsIgnoreCase(tipoInfraccion))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No existe una regla para la infracción: " + tipoInfraccion));
        // Crear sanción
        Sancion sancion = new Sancion();
        sancion.setUsuario(usuario);
        sancion.setVehiculo(vehiculo);
        sancion.setMotivo(descripcion);
        sancion.setEstado(Sancion.EstadoSancion.ACTIVA);
        sancion.setRegistroSancion(LocalDateTime.now());
        Sancion sancionGuardada = sancionService.guardarEntidad(sancion);
        // Crear detalle
        SancionDetalle detalle = new SancionDetalle();
        detalle.setDescripcion(descripcion);
        detalle.setFechaSancion(LocalDateTime.now());
        detalle.setEstado(SancionDetalle.EstadoDetalle.ACTIVO);
        detalle.setSancion(sancionGuardada);
        detalle.setRegla(regla);
        sancionGuardada.getDetalles().add(detalle);
        sancionService.guardarEntidad(sancionGuardada);
        // Registrar en historial de uso
        historialUsoService.registrarEvento(usuario, HistorialUso.AccionHistorial.SANCION);
    }
}
