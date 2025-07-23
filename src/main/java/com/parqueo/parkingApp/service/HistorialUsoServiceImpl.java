package com.parqueo.parkingApp.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.parqueo.parkingApp.dto.HistorialUsoDto;
import com.parqueo.parkingApp.mapper.HistorialUsoMapper;
import com.parqueo.parkingApp.model.EspacioDisponible;
import com.parqueo.parkingApp.model.HistorialUso;
import com.parqueo.parkingApp.model.Reserva;
import com.parqueo.parkingApp.model.Usuario;
import com.parqueo.parkingApp.model.Vehiculo;
import com.parqueo.parkingApp.repository.HistorialUsoRepository;
import com.parqueo.parkingApp.repository.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistorialUsoServiceImpl implements HistorialUsoService {

    private final HistorialUsoRepository repository;
    private final HistorialUsoMapper mapper;
    private final UsuarioRepository usuarioRepository;

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
        
        // Mejorar las notas con más detalles
        String notas = generarNotasDetalladas(accion, reserva, espacio, vehiculo);
        historial.setNotas(notas);
        
        repository.save(historial);
    }

    private String generarNotasDetalladas(HistorialUso.AccionHistorial accion, Reserva reserva, 
                                         EspacioDisponible espacio, Vehiculo vehiculo) {
        switch (accion) {
            case RESERVA:
                if (reserva != null) {
                    return String.format("Reserva #%d: %s a %s en espacio %s", 
                        reserva.getId(), 
                        reserva.getFechaHoraInicio().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                        reserva.getFechaHoraFin().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                        espacio != null ? espacio.getNumeroEspacio() : "N/A");
                }
                return "Reserva creada";
            case ENTRADA:
                return String.format("Entrada registrada en espacio %s con vehículo %s", 
                    espacio != null ? espacio.getNumeroEspacio() : "N/A",
                    vehiculo != null ? vehiculo.getPlaca() : "N/A");
            case SALIDA:
                return String.format("Salida registrada en espacio %s", 
                    espacio != null ? espacio.getNumeroEspacio() : "N/A");
            case CANCELACION:
                return "Reserva cancelada por el usuario";
            case SANCION:
                return String.format("Sanción aplicada - Motivo: %s", 
                    reserva != null ? reserva.getMotivoCancelacion() : "No especificado");
            case DESBLOQUEO:
                return "Desbloqueo de sanción realizado por administrador";
            case EXPIRACION:
                return String.format("Reserva expirada automáticamente en espacio %s", 
                    espacio != null ? espacio.getNumeroEspacio() : "N/A");
            default:
                return "";
        }
    }

    @Override
    public List<HistorialUsoDto> obtenerMiHistorial() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("No hay usuario autenticado");
        }
        Usuario usuario = usuarioRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con nombre de usuario: " + authentication.getName()));

        return repository.findByUsuario(usuario).stream()
                .map(HistorialUsoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void eliminarPorReserva(Long reservaId) {
        if (reservaId == null) {
            throw new IllegalArgumentException("El ID de la reserva no puede ser null");
        }
        
        System.out.println("Eliminando registros de historial para reserva #" + reservaId);
        List<HistorialUso> registros = repository.findByReservaId(reservaId);
        System.out.println("Encontrados " + registros.size() + " registros de historial para eliminar");
        
        for (HistorialUso registro : registros) {
            repository.deleteById(registro.getId());
            System.out.println("Eliminado registro de historial #" + registro.getId());
        }
        
        System.out.println("Todos los registros de historial eliminados para reserva #" + reservaId);
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
