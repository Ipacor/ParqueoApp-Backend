package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.ReservaDto;
import com.parqueo.parkingApp.mapper.ReservaMapper;
import com.parqueo.parkingApp.model.Reserva;
import com.parqueo.parkingApp.model.Usuario;
import com.parqueo.parkingApp.model.Vehiculo;
import com.parqueo.parkingApp.model.EspacioDisponible;
import com.parqueo.parkingApp.model.EscaneoQR;
import com.parqueo.parkingApp.model.HistorialUso;
import com.parqueo.parkingApp.model.Notificacion;
import com.parqueo.parkingApp.repository.ReservaRepository;
import com.parqueo.parkingApp.repository.UsuarioRepository;
import com.parqueo.parkingApp.repository.VehiculoRepository;
import com.parqueo.parkingApp.repository.EspacioDisponibleRepository;
import com.parqueo.parkingApp.repository.EscaneoQRRepository;
import com.parqueo.parkingApp.repository.SancionRepository;
import com.parqueo.parkingApp.service.SancionService;
import com.parqueo.parkingApp.dto.SancionDto;
import com.parqueo.parkingApp.model.Sancion;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.time.format.DateTimeFormatter;

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
    
    @Autowired
    private NotificacionService notificacionService;
    @Autowired
    private SancionRepository sancionRepository;
    @Autowired
    private SancionService sancionService;

    @Override
    public List<ReservaDto> obtenerTodos() {
        // Obtener todas las reservas
        List<Reserva> reservas = reservaRepo.findAll();
        
        // Cargar los QRs asociados a cada reserva
        for (Reserva reserva : reservas) {
            try {
                EscaneoQR escaneoQR = escaneoQRRepo.findByReserva(reserva)
                        .orElse(null);
                if (escaneoQR != null) {
                    reserva.setEscaneoQR(escaneoQR);
                }
            } catch (Exception e) {
                // Si no se puede cargar el QR, continuar sin él
                System.err.println("No se pudo cargar el QR para la reserva " + reserva.getId() + ": " + e.getMessage());
            }
        }
        
        // Mapear a DTOs incluyendo la información del QR
        return reservas.stream()
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
        
        // Cargar el QR asociado a la reserva
        try {
            EscaneoQR escaneoQR = escaneoQRRepo.findByReserva(reserva)
                    .orElse(null);
            if (escaneoQR != null) {
                reserva.setEscaneoQR(escaneoQR);
            }
        } catch (Exception e) {
            // Si no se puede cargar el QR, continuar sin él
            System.err.println("No se pudo cargar el QR para la reserva " + id + ": " + e.getMessage());
        }
        
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
        
        // Validar que el espacio no esté ya reservado
        List<Reserva> reservasExistentes = reservaRepo.findByEstado(Reserva.EstadoReserva.RESERVADO);
        boolean espacioOcupado = reservasExistentes.stream()
            .anyMatch(r -> r.getEspacio().getId().equals(espacio.getId()));
        
        if (espacioOcupado) {
            throw new RuntimeException("El espacio ya está reservado");
        }
        
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
        
        // Crear notificación de reserva creada
        String tituloNotificacion = "Reserva Creada";
        String mensajeNotificacion = String.format("Tu reserva #%d ha sido creada exitosamente. Espacio: %s, Fecha: %s a %s", 
                guardada.getId(), 
                espacio.getNumeroEspacio(),
                dto.getFechaHoraInicio().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                dto.getFechaHoraFin().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        
        notificacionService.crearNotificacion(usuario, tituloNotificacion, mensajeNotificacion, Notificacion.TipoNotificacion.RESERVA_CREADA);
        
        // Cargar el QR para incluirlo en la respuesta
        guardada.setEscaneoQR(escaneoQR);
        
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
                
                // Crear notificación de entrada registrada
                String tituloEntrada = "Entrada Registrada";
                String mensajeEntrada = String.format("Has ingresado al espacio %s. Tu reserva #%d está ahora activa.", 
                        espacio.getNumeroEspacio(), actualizada.getId());
                notificacionService.crearNotificacion(usuario, tituloEntrada, mensajeEntrada, Notificacion.TipoNotificacion.ENTRADA_REGISTRADA);
                
            } else if (dto.getEstado() == Reserva.EstadoReserva.FINALIZADO) {
                historialUsoService.registrarEvento(usuario, espacio, actualizada, vehiculo, HistorialUso.AccionHistorial.SALIDA);
                
                // Crear notificación de salida registrada
                String tituloSalida = "Salida Registrada";
                String mensajeSalida = String.format("Has salido del espacio %s. Tu reserva #%d ha finalizado.", 
                        espacio.getNumeroEspacio(), actualizada.getId());
                notificacionService.crearNotificacion(usuario, tituloSalida, mensajeSalida, Notificacion.TipoNotificacion.SALIDA_REGISTRADA);
                
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
    @Transactional
    public void eliminar(Long id) {
        System.out.println("=== ELIMINANDO RESERVA #" + id + " ===");
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        try {
        Reserva reserva = reservaRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada con ID: " + id));
            System.out.println("Reserva encontrada: " + reserva.getId());
            
            // Eliminar registros de historial asociados primero
            System.out.println("Eliminando registros de historial asociados...");
            historialUsoService.eliminarPorReserva(id);
            System.out.println("Registros de historial eliminados");
            
            // Eliminar escaneos QR asociados
            System.out.println("Eliminando escaneos QR asociados...");
            escaneoQRRepo.deleteByReservaId(id);
            System.out.println("Escaneos QR eliminados");
            
        EspacioDisponible espacio = reserva.getEspacio();
        if (espacio != null) {
                System.out.println("Liberando espacio: " + espacio.getNumeroEspacio());
            espacio.setEstado(EspacioDisponible.EstadoEspacio.DISPONIBLE);
            espacioRepo.save(espacio);
                System.out.println("Espacio liberado correctamente");
            } else {
                System.out.println("No se encontró espacio asociado a la reserva");
            }
            
        reservaRepo.deleteById(id);
            System.out.println("Reserva eliminada correctamente");
        } catch (Exception e) {
            System.out.println("ERROR eliminando reserva: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<ReservaDto> buscarPorUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser null");
        }
        
        // Obtener todas las reservas del usuario
        List<Reserva> reservas = reservaRepo.findByUsuarioId(usuarioId);
        
        // Cargar los QRs asociados a cada reserva
        for (Reserva reserva : reservas) {
            try {
                EscaneoQR escaneoQR = escaneoQRRepo.findByReserva(reserva)
                        .orElse(null);
                if (escaneoQR != null) {
                    reserva.setEscaneoQR(escaneoQR);
                }
            } catch (Exception e) {
                // Si no se puede cargar el QR, continuar sin él
                System.err.println("No se pudo cargar el QR para la reserva " + reserva.getId() + ": " + e.getMessage());
            }
        }
        
        // Mapear a DTOs incluyendo la información del QR
        return reservas.stream()
                .map(ReservaMapper::toDto)
                .toList();
    }

    @Override
    public List<ReservaDto> buscarPorVehiculo(Long vehiculoId) {
        if (vehiculoId == null) {
            throw new IllegalArgumentException("El ID del vehículo no puede ser null");
        }
        
        // Obtener todas las reservas del vehículo
        List<Reserva> reservas = reservaRepo.findByVehiculoId(vehiculoId);
        
        // Cargar los QRs asociados a cada reserva
        for (Reserva reserva : reservas) {
            try {
                EscaneoQR escaneoQR = escaneoQRRepo.findByReserva(reserva)
                        .orElse(null);
                if (escaneoQR != null) {
                    reserva.setEscaneoQR(escaneoQR);
                }
            } catch (Exception e) {
                // Si no se puede cargar el QR, continuar sin él
                System.err.println("No se pudo cargar el QR para la reserva " + reserva.getId() + ": " + e.getMessage());
            }
        }
        
        // Mapear a DTOs incluyendo la información del QR
        return reservas.stream()
                .map(ReservaMapper::toDto)
                .toList();
    }

    @Override
    public List<ReservaDto> buscarPorEspacio(Long espacioId) {
        if (espacioId == null) {
            throw new IllegalArgumentException("El ID del espacio no puede ser null");
        }
        
        // Obtener todas las reservas del espacio
        List<Reserva> reservas = reservaRepo.findByEspacioId(espacioId);
        
        // Cargar los QRs asociados a cada reserva
        for (Reserva reserva : reservas) {
            try {
                EscaneoQR escaneoQR = escaneoQRRepo.findByReserva(reserva)
                        .orElse(null);
                if (escaneoQR != null) {
                    reserva.setEscaneoQR(escaneoQR);
                }
            } catch (Exception e) {
                // Si no se puede cargar el QR, continuar sin él
                System.err.println("No se pudo cargar el QR para la reserva " + reserva.getId() + ": " + e.getMessage());
            }
        }
        
        // Mapear a DTOs incluyendo la información del QR
        return reservas.stream()
                .map(ReservaMapper::toDto)
                .toList();
    }

    @Override
    public List<ReservaDto> buscarPorEstado(Reserva.EstadoReserva estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser null");
        }
        
        // Obtener todas las reservas del estado
        List<Reserva> reservas = reservaRepo.findByEstado(estado);
        
        // Cargar los QRs asociados a cada reserva
        for (Reserva reserva : reservas) {
            try {
                EscaneoQR escaneoQR = escaneoQRRepo.findByReserva(reserva)
                        .orElse(null);
                if (escaneoQR != null) {
                    reserva.setEscaneoQR(escaneoQR);
                }
            } catch (Exception e) {
                // Si no se puede cargar el QR, continuar sin él
                System.err.println("No se pudo cargar el QR para la reserva " + reserva.getId() + ": " + e.getMessage());
            }
        }
        
        // Mapear a DTOs incluyendo la información del QR
        return reservas.stream()
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
        
        // Obtener todas las reservas en el rango de fechas
        List<Reserva> reservas = reservaRepo.findByFechaHoraInicioBetween(fechaInicio, fechaFin);
        
        // Cargar los QRs asociados a cada reserva
        for (Reserva reserva : reservas) {
            try {
                EscaneoQR escaneoQR = escaneoQRRepo.findByReserva(reserva)
                        .orElse(null);
                if (escaneoQR != null) {
                    reserva.setEscaneoQR(escaneoQR);
                }
            } catch (Exception e) {
                // Si no se puede cargar el QR, continuar sin él
                System.err.println("No se pudo cargar el QR para la reserva " + reserva.getId() + ": " + e.getMessage());
            }
        }
        
        // Mapear a DTOs incluyendo la información del QR
        return reservas.stream()
                .map(ReservaMapper::toDto)
                .toList();
    }

    @Override
    public List<ReservaDto> buscarReservasActivas() {
        // Obtener todas las reservas activas
        List<Reserva> reservas = reservaRepo.findReservasActivas(Reserva.EstadoReserva.ACTIVO, LocalDateTime.now());
        
        // Cargar los QRs asociados a cada reserva
        for (Reserva reserva : reservas) {
            try {
                EscaneoQR escaneoQR = escaneoQRRepo.findByReserva(reserva)
                        .orElse(null);
                if (escaneoQR != null) {
                    reserva.setEscaneoQR(escaneoQR);
                }
            } catch (Exception e) {
                // Si no se puede cargar el QR, continuar sin él
                System.err.println("No se pudo cargar el QR para la reserva " + reserva.getId() + ": " + e.getMessage());
            }
        }
        
        // Mapear a DTOs incluyendo la información del QR
        return reservas.stream()
                .map(ReservaMapper::toDto)
                .toList();
    }

    // Lógica para liberar espacios cuando el QR de entrada expira o pasan 20 minutos sin escaneo
    @Override
    @Scheduled(fixedRate = 30000) // cada 30 segundos
    @Transactional
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
                EspacioDisponible espacio = reserva.getEspacio();
                // NO liberar el espacio automáticamente, solo cambiar estado de reserva
                // El espacio sigue OCUPADO si el vehículo no ha salido
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

                // Crear notificación de reserva expirada
                String tituloNotificacion = "Reserva Expirada";
                String mensajeNotificacion = String.format("Tu reserva #%d ha expirado automáticamente. El espacio %s sigue ocupado hasta que registres tu salida.", 
                        reserva.getId(), 
                        espacio.getNumeroEspacio());

                notificacionService.crearNotificacion(reserva.getUsuario(), tituloNotificacion, mensajeNotificacion, Notificacion.TipoNotificacion.RESERVA_EXPIRADA);

                // Log para debugging
                System.out.println("Reserva " + reserva.getId() + " expirada por fecha de fin: " + reserva.getFechaHoraFin());
                System.out.println("Espacio " + espacio.getId() + " sigue ocupado - Estado: " + espacio.getEstado());
                System.out.println("Fecha actual: " + ahora);
            }
        }
        
        // SEGUNDA LÓGICA: Verificar QRs expirados (para reservas que aún no han expirado por fecha)
        // Usar una consulta más eficiente que cargue las relaciones necesarias
        List<EscaneoQR> escaneos = escaneoQRRepo.findAllWithReservaAndEspacio();
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
            
            boolean debeExpirar = false;
            String motivo = "";
            
            // Verificar si el QR ha expirado
            if (escaneo.getFechaExpiracion() != null && escaneo.getFechaExpiracion().isBefore(ahora)) {
                debeExpirar = true;
                motivo = "QR expirado";
            }
            
            // Verificar si han pasado 30 minutos desde el inicio de la reserva sin escaneo
            LocalDateTime tiempoReferencia = reserva.getFechaHoraInicio();
            
            if (tiempoReferencia != null && tiempoReferencia.plusMinutes(30).isBefore(ahora)) {
                debeExpirar = true;
                motivo = "Sin escaneo por 30 minutos";
            }
            
            if (debeExpirar) {
                // NO liberar el espacio automáticamente, solo cambiar estado de reserva
                // El espacio sigue OCUPADO si el vehículo no ha salido
                EspacioDisponible espacio = reserva.getEspacio();
                
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
                
                // Crear notificación de reserva expirada
                String tituloNotificacion = "Reserva Expirada";
                String mensajeNotificacion = String.format("Tu reserva #%d ha expirado por: %s. El espacio %s sigue ocupado hasta que registres tu salida.", 
                        reserva.getId(), 
                        motivo,
                        espacio.getNumeroEspacio());
                
                notificacionService.crearNotificacion(reserva.getUsuario(), tituloNotificacion, mensajeNotificacion, Notificacion.TipoNotificacion.RESERVA_EXPIRADA);
                
                // Log para debugging
                System.out.println("Reserva " + reserva.getId() + " expirada por: " + motivo);
                System.out.println("Espacio " + espacio.getId() + " sigue ocupado - Estado: " + espacio.getEstado());
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
        
        // NO liberar el espacio automáticamente, solo cambiar estado de reserva
        // El espacio sigue OCUPADO si el vehículo no ha salido
        EspacioDisponible espacio = reserva.getEspacio();
        
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
        
        // Crear notificación de reserva expirada manualmente
        String tituloNotificacion = "Reserva Expirada";
        String mensajeNotificacion = String.format("Tu reserva #%d ha sido expirada manualmente por un administrador. El espacio %s sigue ocupado hasta que registres tu salida.", 
                reservaId, 
                espacio.getNumeroEspacio());
        
        notificacionService.crearNotificacion(reserva.getUsuario(), tituloNotificacion, mensajeNotificacion, Notificacion.TipoNotificacion.RESERVA_EXPIRADA);
        
        System.out.println("Reserva " + reservaId + " expirada manualmente");
        System.out.println("Espacio " + espacio.getId() + " sigue ocupado - Estado: " + espacio.getEstado());
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

    @Override
    @Scheduled(fixedRate = 300000) // Cada 5 minutos
    public void expirarReservasAutomaticamente() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Reserva> reservasActivas = reservaRepo.findByEstadoWithRelations(Reserva.EstadoReserva.RESERVADO);
        
        for (Reserva reserva : reservasActivas) {
            if (reserva.getFechaHoraFin() != null && reserva.getFechaHoraFin().isBefore(ahora)) {
                // NO liberar el espacio automáticamente, solo cambiar estado de reserva
                // El espacio sigue OCUPADO si el vehículo no ha salido
                reserva.setEstado(Reserva.EstadoReserva.EXPIRADO);
                reservaRepo.save(reserva);
                
                // Crear notificación de expiración
                notificacionService.crearNotificacion(
                    reserva.getUsuario(),
                    "Reserva Expirada",
                    "Tu reserva #" + reserva.getId() + " ha expirado automáticamente. El espacio " + 
                    reserva.getEspacio().getNumeroEspacio() + " sigue ocupado hasta que registres tu salida.",
                    com.parqueo.parkingApp.model.Notificacion.TipoNotificacion.RESERVA_EXPIRADA
                );
            }
        }
    }
    
    /**
     * Método programado para aplicar sanciones automáticas por reservas expiradas
     * Se ejecuta cada 10 minutos
     */
    @Override
    @Scheduled(fixedRate = 600000) // Cada 10 minutos
    public void aplicarSancionesAutomaticas() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Reserva> reservasExpiradas = reservaRepo.findByEstadoWithRelations(Reserva.EstadoReserva.EXPIRADO);
        
        for (Reserva reserva : reservasExpiradas) {
            // Verificar si ya se aplicó una sanción para esta reserva
            boolean yaTieneSancion = sancionRepository.findByUsuarioId(reserva.getUsuario().getId()).stream()
                .anyMatch(s -> s.getMotivo() != null && s.getMotivo().contains("Reserva #" + reserva.getId()));
            
            if (!yaTieneSancion) {
                // Aplicar sanción automática
                try {
                    // Crear DTO de sanción
                    SancionDto sancionDto = new SancionDto();
                    sancionDto.setUsuarioId(reserva.getUsuario().getId());
                    sancionDto.setVehiculoId(reserva.getVehiculo().getId());
                    sancionDto.setMotivo("Exceder tiempo de reserva - Reserva #" + reserva.getId() + 
                        " expirada sin registrar salida. Espacio: " + reserva.getEspacio().getNumeroEspacio());
                    sancionDto.setEstado(Sancion.EstadoSancion.ACTIVA);
                    sancionDto.setRegistroSancion(ahora);
                    sancionDto.setReglaId(1L); // ID de regla por defecto para exceder tiempo
                    
                    // Crear sanción usando el servicio
                    sancionService.crearConRegistrador(sancionDto, null); // null para sistema automático
                    
                    // NO liberar el espacio automáticamente
                    EspacioDisponible espacio = reserva.getEspacio();
                    System.out.println("Sanción automática aplicada para reserva #" + reserva.getId() + 
                        " - Usuario: " + reserva.getUsuario().getUsername() + 
                        " - Espacio sigue ocupado: " + (espacio != null ? espacio.getNumeroEspacio() : "N/A"));
                    
                } catch (Exception e) {
                    System.err.println("Error al aplicar sanción automática para reserva #" + reserva.getId() + ": " + e.getMessage());
                }
            }
        }
    }

    @Override
    @Scheduled(fixedRate = 600000) // Cada 10 minutos
    public void crearRecordatoriosAutomaticos() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime en20Minutos = ahora.plusMinutes(20);
        LocalDateTime en30Minutos = ahora.plusMinutes(30);
        LocalDateTime en1Hora = ahora.plusHours(1);
        
        List<Reserva> reservasActivas = reservaRepo.findByEstadoWithRelations(Reserva.EstadoReserva.RESERVADO);
        
        for (Reserva reserva : reservasActivas) {
            LocalDateTime fechaFin = reserva.getFechaHoraFin();
            
            // Recordatorio 20 minutos antes de expirar (NUEVO - para sanciones)
            if (fechaFin != null && fechaFin.isAfter(ahora) && fechaFin.isBefore(en20Minutos)) {
                String mensajeSancion = "⚠ ATENCIÓN: Tu reserva #" + reserva.getId() + " expira en menos de 20 minutos. " +
                    "Si no registras tu salida a tiempo, se aplicará una sanción automática. " +
                    "Espacio: " + reserva.getEspacio().getNumeroEspacio() + 
                    ", Expira: " + fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                
                notificacionService.crearNotificacion(
                    reserva.getUsuario(),
                    "   Advertencia: Sanción Inminente",
                    mensajeSancion,
                    com.parqueo.parkingApp.model.Notificacion.TipoNotificacion.RECORDATORIO_EXPIRACION
                );
            }
            
            // Recordatorio 30 minutos antes de expirar
            if (fechaFin != null && fechaFin.isAfter(en20Minutos) && fechaFin.isBefore(en30Minutos)) {
                notificacionService.crearNotificacion(
                    reserva.getUsuario(),
                    "Recordatorio: Reserva Expira Pronto",
                    "Tu reserva #" + reserva.getId() + " expira en menos de 30 minutos. " +
                    "Espacio: " + reserva.getEspacio().getNumeroEspacio() + 
                    ", Expira: " + fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    com.parqueo.parkingApp.model.Notificacion.TipoNotificacion.RECORDATORIO_EXPIRACION
                );
            }
            
            // Recordatorio 1 hora antes de expirar
            if (fechaFin != null && fechaFin.isAfter(en30Minutos) && fechaFin.isBefore(en1Hora)) {
                notificacionService.crearNotificacion(
                    reserva.getUsuario(),
                    "Recordatorio: Reserva Expira en 1 Hora",
                    "Tu reserva #" + reserva.getId() + " expira en 1 hora. " +
                    "Espacio: " + reserva.getEspacio().getNumeroEspacio() + 
                    ", Expira: " + fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    com.parqueo.parkingApp.model.Notificacion.TipoNotificacion.RECORDATORIO_EXPIRACION
                );
            }
        }
    }
}