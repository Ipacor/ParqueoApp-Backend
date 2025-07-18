package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.HistorialUsoDto;
import com.parqueo.parkingApp.mapper.HistorialUsoMapper;
import com.parqueo.parkingApp.model.HistorialUso;
import com.parqueo.parkingApp.model.Usuario;
import com.parqueo.parkingApp.model.EspacioDisponible;
import com.parqueo.parkingApp.model.Reserva;
import com.parqueo.parkingApp.model.Vehiculo;
import com.parqueo.parkingApp.repository.HistorialUsoRepository;
import com.parqueo.parkingApp.service.HistorialUsoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistorialUsoServiceImpl implements HistorialUsoService {

    private final HistorialUsoRepository repository;
    private final HistorialUsoMapper mapper;

    @Override
    public List<HistorialUsoDto> obtenerTodos() {
        return repository.findAll().stream()
                .map(HistorialUsoMapper::toDto)
                .toList();
    }

    @Override
    public HistorialUsoDto obtenerPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        HistorialUso historial = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Historial no encontrado con ID: " + id));
        return HistorialUsoMapper.toDto(historial);
    }

    @Override
    public HistorialUsoDto crear(HistorialUsoDto dto) {
        validarDatosHistorial(dto);
        
        HistorialUso nuevo = HistorialUsoMapper.toEntity(dto);
        return HistorialUsoMapper.toDto(repository.save(nuevo));
    }

    @Override
    public HistorialUsoDto actualizar(Long id, HistorialUsoDto dto) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Historial no encontrado con ID: " + id);
        }

        validarDatosHistorial(dto);

        HistorialUso actualizado = HistorialUsoMapper.toEntity(dto);
        actualizado.setId(id);

        return HistorialUsoMapper.toDto(repository.save(actualizado));
    }

    @Override
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Historial no encontrado con ID: " + id);
        }
        
        repository.deleteById(id);
    }

    @Override
    public List<HistorialUsoDto> buscarPorUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser null");
        }
        
        return repository.findByUsuarioId(usuarioId).stream()
                .map(HistorialUsoMapper::toDto)
                .toList();
    }

    @Override
    public List<HistorialUsoDto> buscarPorVehiculo(Long vehiculoId) {
        if (vehiculoId == null) {
            throw new IllegalArgumentException("El ID del vehículo no puede ser null");
        }
        
        // Por ahora retornamos vacío ya que no hay relación con vehículo en el modelo
        return List.of();
    }

    @Override
    public List<HistorialUsoDto> buscarPorEspacio(Long espacioId) {
        if (espacioId == null) {
            throw new IllegalArgumentException("El ID del espacio no puede ser null");
        }
        
        return repository.findByEspacioId(espacioId).stream()
                .map(HistorialUsoMapper::toDto)
                .toList();
    }

    @Override
    public List<HistorialUsoDto> buscarPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser null");
        }
        
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        
        return repository.findAll().stream()
                .filter(historial -> historial.getFechaUso().isAfter(fechaInicio) && historial.getFechaUso().isBefore(fechaFin))
                .map(HistorialUsoMapper::toDto)
                .toList();
    }

    @Override
    public List<HistorialUsoDto> buscarUsosActivos() {
        return repository.findAll().stream()
                .filter(historial -> "ENTRADA".equals(historial.getAccion()))
                .map(HistorialUsoMapper::toDto)
                .toList();
    }

    public void registrarEvento(Usuario usuario, HistorialUso.AccionHistorial accion) {
        registrarEvento(usuario, null, null, null, accion);
    }

    public void registrarEvento(Usuario usuario, EspacioDisponible espacio, Reserva reserva, Vehiculo vehiculo, HistorialUso.AccionHistorial accion) {
        HistorialUso historial = new HistorialUso();
        historial.setUsuario(usuario);
        historial.setEspacio(espacio);
        historial.setReserva(reserva);
        historial.setVehiculo(vehiculo);
        historial.setAccion(accion);
        historial.setFechaUso(LocalDateTime.now());
        String notas = "";
        if (accion == HistorialUso.AccionHistorial.RESERVA && reserva != null) {
            notas = "Reserva del " + reserva.getFechaHoraInicio() + " al " + reserva.getFechaHoraFin();
        } else if (accion == HistorialUso.AccionHistorial.ENTRADA) {
            notas = "Entrada registrada en espacio " + (espacio != null ? espacio.getId() : "-");
        } else if (accion == HistorialUso.AccionHistorial.SALIDA) {
            notas = "Salida registrada en espacio " + (espacio != null ? espacio.getId() : "-");
        } else if (accion == HistorialUso.AccionHistorial.CANCELACION) {
            notas = "Reserva cancelada";
        } else if (accion == HistorialUso.AccionHistorial.SANCION) {
            notas = "Sanción aplicada al usuario " + (usuario != null ? usuario.getId() : "-");
        } else if (accion == HistorialUso.AccionHistorial.DESBLOQUEO) {
            notas = "Desbloqueo de sanción para usuario " + (usuario != null ? usuario.getId() : "-");
        }
        historial.setNotas(notas);
        repository.save(historial);
    }

    private void validarDatosHistorial(HistorialUsoDto dto) {
        if (dto.getUsuarioId() == null) {
            throw new IllegalArgumentException("El ID del usuario no puede estar vacío");
        }
        
        if (dto.getFechaUso() == null) {
            throw new IllegalArgumentException("La fecha de uso no puede estar vacía");
        }
        
        if (dto.getAccion() == null || dto.getAccion().trim().isEmpty()) {
            throw new IllegalArgumentException("La acción no puede estar vacía");
        }
    }
}
