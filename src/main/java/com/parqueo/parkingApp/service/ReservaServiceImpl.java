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
        // Extender la validez del QR: 30 minutos antes y 30 minutos después del inicio
        escaneoQR.setFechaExpiracion(dto.getFechaHoraInicio().plusMinutes(30));
        escaneoQR.setFechaInicioValidez(dto.getFechaHoraInicio().minusMinutes(30));
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
        // Incluir todas las reservas del usuario, incluyendo las expiradas
        return reservaRepo.findByUsuarioId(usuarioId).stream()
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

    // Lógica para liberar espacios cuando el QR de entrada expira o pasan 20 minutos sin escaneo
    @Scheduled(fixedRate = 30000) // cada 30 segundos
    public void liberarEspaciosReservadosExpirados() {
        System.out.println("=== EJECUTANDO JOB DE EXPIRACIÓN - " + LocalDateTime.now() + " ===");
        
        LocalDateTime ahora = LocalDateTime.now();
        
        // PRIMERA LÓGICA: Verificar reservas que ya pasaron su fechaHoraFin
        List<Reserva> reservasReservadas = reservaRepo.findByEstadoWithRelations(Reserva.EstadoReserva.RESERVADO);
        System.out.println("Procesando " + reservasReservadas.size() + " reservas en estado RESERVADO");
        
        for (Reserva reserva : reservasReservadas) {
            // Log para todas las reservas
                System.out.println("=== REVISANDO RESERVA " + reserva.getId() + " ===");
                System.out.println("Estado actual: " + reserva.getEstado());
                System.out.println("Fecha fin: " + reserva.getFechaHoraFin());
                System.out.println("Fecha actual: " + ahora);
                System.out.println("¿Está expirada? " + (reserva.getFechaHoraFin() != null && reserva.getFechaHoraFin().isBefore(ahora)));
            
            // Verificar si la reserva ha pasado su fecha de fin
            if (reserva.getFechaHoraFin() != null && reserva.getFechaHoraFin().isBefore(ahora)) {
                // Liberar espacio
                EspacioDisponible espacio = reserva.getEspacio();
                if (espacio != null) {
                    espacio.setEstado(EspacioDisponible.EstadoEspacio.DISPONIBLE);
                    espacioRepo.save(espacio);
                }
                
                // Cambiar estado de la reserva a EXPIRADO
                reserva.setEstado(Reserva.EstadoReserva.EXPIRADO);
                reservaRepo.save(reserva);
                
                // Registrar en el historial
                historialUsoService.registrarEvento(
                    reserva.getUsuario(), 
                    espacio, 
                    reserva, 
                    reserva.getVehiculo(), 
                    HistorialUso.AccionHistorial.EXPIRACION
                );
                
                // Log para debugging
                System.out.println("Reserva " + reserva.getId() + " expirada por fecha de fin: " + reserva.getFechaHoraFin());
                System.out.println("Espacio " + espacio.getId() + " liberado - Estado: " + espacio.getEstado());
                System.out.println("Fecha actual: " + ahora);
            }
        }
        
        // SEGUNDA LÓGICA: Verificar QRs expirados (para reservas que aún no han expirado por fecha)
        List<EscaneoQR> escaneos = escaneoQRRepo.findAll();
        System.out.println("Procesando " + escaneos.size() + " escaneos QR");
        
        for (EscaneoQR escaneo : escaneos) {
            // Solo procesar QRs de ENTRADA que no han sido usados
            if (!"ENTRADA".equals(escaneo.getTipo()) || escaneo.getTimestampEnt() != null) {
                continue;
            }
            
            Reserva reserva = escaneo.getReserva();
            if (reserva == null || reserva.getEstado() != Reserva.EstadoReserva.RESERVADO) {
                continue;
            }
            
            // Solo procesar si la reserva no ha expirado por fecha de fin
            if (reserva.getFechaHoraFin() != null && reserva.getFechaHoraFin().isBefore(ahora)) {
                continue; // Ya se procesó en la primera lógica
            }
            
            boolean debeLiberar = false;
            String motivo = "";
            
            // Verificar si el QR ha expirado
            if (escaneo.getFechaExpiracion() != null && escaneo.getFechaExpiracion().isBefore(ahora)) {
                debeLiberar = true;
                motivo = "QR expirado";
            }
            
            // Verificar si han pasado 30 minutos desde el inicio de la reserva sin escaneo
            LocalDateTime tiempoReferencia = reserva.getFechaHoraInicio();
            
            if (tiempoReferencia != null && tiempoReferencia.plusMinutes(30).isBefore(ahora)) {
                debeLiberar = true;
                motivo = "Sin escaneo por 30 minutos";
            }
            
            if (debeLiberar) {
                // Liberar espacio
                EspacioDisponible espacio = reserva.getEspacio();
                if (espacio != null) {
                    espacio.setEstado(EspacioDisponible.EstadoEspacio.DISPONIBLE);
                    espacioRepo.save(espacio);
                }
                
                // Cambiar estado de la reserva a EXPIRADO
                reserva.setEstado(Reserva.EstadoReserva.EXPIRADO);
                reservaRepo.save(reserva);
                
                // Registrar en el historial
                historialUsoService.registrarEvento(
                    reserva.getUsuario(), 
                    espacio, 
                    reserva, 
                    reserva.getVehiculo(), 
                    HistorialUso.AccionHistorial.EXPIRACION
                );
                
                // Log para debugging
                System.out.println("Reserva " + reserva.getId() + " expirada por: " + motivo);
                System.out.println("Espacio " + espacio.getId() + " liberado - Estado: " + espacio.getEstado());
                System.out.println("Fecha actual: " + ahora);
            }
        }
        
        System.out.println("=== FIN JOB DE EXPIRACIÓN ===");
    }

    public void forzarExpiracionReserva(Long reservaId) {
        Reserva reserva = reservaRepo.findById(reservaId)
            .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada con ID: " + reservaId));
        
        if (reserva.getEstado() != Reserva.EstadoReserva.RESERVADO) {
            throw new IllegalStateException("La reserva no está en estado RESERVADO. Estado actual: " + reserva.getEstado());
        }
        
        // Liberar espacio
        EspacioDisponible espacio = reserva.getEspacio();
        if (espacio != null) {
            espacio.setEstado(EspacioDisponible.EstadoEspacio.DISPONIBLE);
            espacioRepo.save(espacio);
        }
        
        // Cambiar estado de la reserva a EXPIRADO
        reserva.setEstado(Reserva.EstadoReserva.EXPIRADO);
        reservaRepo.save(reserva);
        
        // Registrar en el historial
        historialUsoService.registrarEvento(
            reserva.getUsuario(), 
            espacio, 
            reserva, 
            reserva.getVehiculo(), 
            HistorialUso.AccionHistorial.EXPIRACION
        );
        
        System.out.println("Reserva " + reservaId + " expirada manualmente");
        System.out.println("Espacio " + espacio.getId() + " liberado - Estado: " + espacio.getEstado());
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
        
        // Permitir reservas sin restricciones de tiempo (solo validar que fechaFin > fechaInicio)
        // Comentado temporalmente para permitir reservas inmediatas
        /*
        LocalDateTime ahora = LocalDateTime.now();
        if (fechaInicio.isBefore(ahora.minusHours(1))) {
            throw new IllegalArgumentException("La fecha de inicio no puede estar más de 1 hora en el pasado");
        }
        */
    }
}

