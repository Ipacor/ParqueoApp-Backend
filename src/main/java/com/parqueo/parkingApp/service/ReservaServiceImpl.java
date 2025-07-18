package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.ReservaDto;
import com.parqueo.parkingApp.mapper.ReservaMapper;
import com.parqueo.parkingApp.model.Reserva;
import com.parqueo.parkingApp.model.Usuario;
import com.parqueo.parkingApp.model.Vehiculo;
import com.parqueo.parkingApp.model.EspacioDisponible;
import com.parqueo.parkingApp.model.EscaneoQR;
import com.parqueo.parkingApp.model.HistorialUso;
import com.parqueo.parkingApp.repository.ReservaRepository;
import com.parqueo.parkingApp.repository.UsuarioRepository;
import com.parqueo.parkingApp.repository.VehiculoRepository;
import com.parqueo.parkingApp.repository.EspacioDisponibleRepository;
import com.parqueo.parkingApp.repository.EscaneoQRRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepo;
    private final ReservaMapper mapper;
    private final UsuarioRepository usuarioRepo;
    private final VehiculoRepository vehiculoRepo;
    private final EspacioDisponibleRepository espacioRepo;
    private final EscaneoQRRepository escaneoQRRepo;
    @Autowired
    private HistorialUsoService historialUsoService;

    @Override
    public List<ReservaDto> obtenerTodos() {
        return reservaRepo.findAll().stream()
                .map(ReservaMapper::toDto)
                .toList();
    }

    @Override
    public ReservaDto obtenerPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        Reserva reserva = reservaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada con ID: " + id));
        return ReservaMapper.toDto(reserva);
    }

    @Override
    public ReservaDto crear(ReservaDto dto) {
        validarDatosReserva(dto);
        validarFechasReserva(dto.getFechaHoraInicio(), dto.getFechaHoraFin());
        Reserva nueva = ReservaMapper.toEntity(dto);
        // Asignar entidades relacionadas
        Usuario usuario = usuarioRepo.findById(dto.getUsuarioId())
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        Vehiculo vehiculo = vehiculoRepo.findById(dto.getVehiculoId())
            .orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado"));
        EspacioDisponible espacio = espacioRepo.findById(dto.getEspacioId())
            .orElseThrow(() -> new EntityNotFoundException("Espacio no encontrado"));
        nueva.setUsuario(usuario);
        nueva.setVehiculo(vehiculo);
        nueva.setEspacio(espacio);
        nueva.setEstado(Reserva.EstadoReserva.RESERVADO);
        // Marcar el espacio como RESERVADO y guardar
        espacio.setEstado(EspacioDisponible.EstadoEspacio.RESERVADO);
        espacioRepo.save(espacio);
        Reserva guardada = reservaRepo.save(nueva);
        // Crear EscaneoQR con token único
        EscaneoQR escaneoQR = new EscaneoQR();
        escaneoQR.setReserva(guardada);
        escaneoQR.setToken(UUID.randomUUID().toString());
        escaneoQR.setTipo("ENTRADA");
        escaneoQR.setFechaExpiracion(dto.getFechaHoraInicio().plusMinutes(20));
        escaneoQR.setFechaInicioValidez(dto.getFechaHoraInicio().minusMinutes(20));
        escaneoQRRepo.save(escaneoQR);
        // Registrar evento de creación de reserva
        historialUsoService.registrarEvento(usuario, espacio, guardada, vehiculo, HistorialUso.AccionHistorial.RESERVA);
        return ReservaMapper.toDto(guardada);
    }

    @Override
    public ReservaDto actualizar(Long id, ReservaDto dto) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        Reserva original = reservaRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada con ID: " + id));
        // Validación: no permitir cambiar el estado si la reserva está FINALIZADO, CANCELADO o EXPIRADO
        if (original.getEstado() == Reserva.EstadoReserva.FINALIZADO ||
            original.getEstado() == Reserva.EstadoReserva.CANCELADO ||
            original.getEstado() == Reserva.EstadoReserva.EXPIRADO) {
            if (dto.getEstado() != original.getEstado()) {
                throw new IllegalStateException("No se puede cambiar el estado de una reserva FINALIZADO, CANCELADO o EXPIRADO");
            }
        }
        validarDatosReserva(dto);
        validarFechasReserva(dto.getFechaHoraInicio(), dto.getFechaHoraFin());
        Reserva actualizada = ReservaMapper.toEntity(dto);
        actualizada.setId(id);
        // Mantener la fecha de creación original
        actualizada.setFechaCreacion(original.getFechaCreacion());
        // Asignar entidades relacionadas
        Usuario usuario = usuarioRepo.findById(dto.getUsuarioId())
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        Vehiculo vehiculo = vehiculoRepo.findById(dto.getVehiculoId())
            .orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado"));
        EspacioDisponible espacio = espacioRepo.findById(dto.getEspacioId())
            .orElseThrow(() -> new EntityNotFoundException("Espacio no encontrado"));
        actualizada.setUsuario(usuario);
        actualizada.setVehiculo(vehiculo);
        actualizada.setEspacio(espacio);
        // Detectar cambio de estado y registrar evento
        if (original.getEstado() != dto.getEstado()) {
            if (dto.getEstado() == Reserva.EstadoReserva.ACTIVO) {
                historialUsoService.registrarEvento(usuario, espacio, actualizada, vehiculo, HistorialUso.AccionHistorial.ENTRADA);
            } else if (dto.getEstado() == Reserva.EstadoReserva.FINALIZADO) {
                historialUsoService.registrarEvento(usuario, espacio, actualizada, vehiculo, HistorialUso.AccionHistorial.SALIDA);
            } else if (dto.getEstado() == Reserva.EstadoReserva.CANCELADO) {
                historialUsoService.registrarEvento(usuario, espacio, actualizada, vehiculo, HistorialUso.AccionHistorial.CANCELACION);
            }
        }
        // Sincronizar estado del espacio según el estado de la reserva
        if (dto.getEstado() == Reserva.EstadoReserva.RESERVADO) {
            espacio.setEstado(EspacioDisponible.EstadoEspacio.RESERVADO);
        } else if (dto.getEstado() == Reserva.EstadoReserva.ACTIVO) {
            espacio.setEstado(EspacioDisponible.EstadoEspacio.OCUPADO);
        } else if (dto.getEstado() == Reserva.EstadoReserva.FINALIZADO ||
                   dto.getEstado() == Reserva.EstadoReserva.CANCELADO ||
                   dto.getEstado() == Reserva.EstadoReserva.EXPIRADO) {
            espacio.setEstado(EspacioDisponible.EstadoEspacio.DISPONIBLE);
        }
        espacioRepo.save(espacio);
        return ReservaMapper.toDto(reservaRepo.save(actualizada));
    }

    @Override
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        Reserva reserva = reservaRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada con ID: " + id));
        EspacioDisponible espacio = reserva.getEspacio();
        if (espacio != null) {
            espacio.setEstado(EspacioDisponible.EstadoEspacio.DISPONIBLE);
            espacioRepo.save(espacio);
        }
        reservaRepo.deleteById(id);
    }

    @Override
    public List<ReservaDto> buscarPorUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser null");
        }
        // Excluir reservas expiradas
        return reservaRepo.findByUsuarioId(usuarioId).stream()
                .filter(r -> r.getEstado() != Reserva.EstadoReserva.EXPIRADO)
                .map(ReservaMapper::toDto)
                .toList();
    }

    @Override
    public List<ReservaDto> buscarPorVehiculo(Long vehiculoId) {
        if (vehiculoId == null) {
            throw new IllegalArgumentException("El ID del vehículo no puede ser null");
        }
        
        return reservaRepo.findByVehiculoId(vehiculoId).stream()
                .map(ReservaMapper::toDto)
                .toList();
    }

    @Override
    public List<ReservaDto> buscarPorEspacio(Long espacioId) {
        if (espacioId == null) {
            throw new IllegalArgumentException("El ID del espacio no puede ser null");
        }
        
        return reservaRepo.findByEspacioId(espacioId).stream()
                .map(ReservaMapper::toDto)
                .toList();
    }

    @Override
    public List<ReservaDto> buscarPorEstado(Reserva.EstadoReserva estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser null");
        }
        
        return reservaRepo.findByEstado(estado).stream()
                .map(ReservaMapper::toDto)
                .toList();
    }

    @Override
    public List<ReservaDto> buscarPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser null");
        }
        
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        
        return reservaRepo.findByFechaHoraInicioBetween(fechaInicio, fechaFin).stream()
                .map(ReservaMapper::toDto)
                .toList();
    }

    @Override
    public List<ReservaDto> buscarReservasActivas() {
        return reservaRepo.findReservasActivas(Reserva.EstadoReserva.ACTIVO, LocalDateTime.now()).stream()
                .map(ReservaMapper::toDto)
                .toList();
    }

    // Lógica para liberar espacio si pasan 20 minutos sin escaneo de entrada
    @Scheduled(fixedRate = 60000) // cada 60 segundos
    public void liberarEspaciosReservadosExpirados() {
        List<EscaneoQR> escaneos = escaneoQRRepo.findAll();
        LocalDateTime ahora = LocalDateTime.now();
        for (EscaneoQR escaneo : escaneos) {
            if (escaneo.getTimestampEnt() != null) continue; // Ya ingresó
            Reserva reserva = escaneo.getReserva();
            if (reserva == null) continue;
            if (reserva.getEstado() != Reserva.EstadoReserva.RESERVADO) continue;
            LocalDateTime creado = escaneo.getTimestampEnt();
            if (creado == null) creado = reserva.getFechaHoraInicio();
            if (creado != null && creado.plusMinutes(20).isBefore(ahora)) {
                // Liberar espacio
                EspacioDisponible espacio = reserva.getEspacio();
                if (espacio != null) {
                    espacio.setEstado(EspacioDisponible.EstadoEspacio.DISPONIBLE);
                    espacioRepo.save(espacio);
                }
                // Cambiar estado de la reserva a EXPIRADO
                reserva.setEstado(Reserva.EstadoReserva.EXPIRADO);
                reservaRepo.save(reserva);
            }
        }
    }

    private void validarDatosReserva(ReservaDto dto) {
        if (dto.getUsuarioId() == null) {
            throw new IllegalArgumentException("El ID del usuario no puede estar vacío");
        }
        
        if (dto.getVehiculoId() == null) {
            throw new IllegalArgumentException("El ID del vehículo no puede estar vacío");
        }
        
        if (dto.getEspacioId() == null) {
            throw new IllegalArgumentException("El ID del espacio no puede estar vacío");
        }
        
        if (dto.getEstado() == null) {
            throw new IllegalArgumentException("El estado no puede estar vacío");
        }
    }

    private void validarFechasReserva(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null) {
            throw new IllegalArgumentException("La fecha de inicio no puede estar vacía");
        }
        
        if (fechaFin == null) {
            throw new IllegalArgumentException("La fecha de fin no puede estar vacía");
        }
        
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        
        if (fechaInicio.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha de inicio no puede estar en el pasado");
        }
    }
}

