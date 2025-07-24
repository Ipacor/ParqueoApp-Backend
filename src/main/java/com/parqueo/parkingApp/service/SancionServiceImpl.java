package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.SancionDto;
import com.parqueo.parkingApp.mapper.SancionMapper;
import com.parqueo.parkingApp.model.Sancion;
import com.parqueo.parkingApp.repository.SancionRepository;
import com.parqueo.parkingApp.service.SancionService;
import com.parqueo.parkingApp.service.HistorialUsoService;
import com.parqueo.parkingApp.model.HistorialUso;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.parqueo.parkingApp.repository.UsuarioRepository;
import com.parqueo.parkingApp.repository.VehiculoRepository;
import com.parqueo.parkingApp.repository.ReglasEstacionamientoRepository;
import com.parqueo.parkingApp.model.ReglasEstacionamiento;
import com.parqueo.parkingApp.service.SancionDetalleService;
import com.parqueo.parkingApp.service.NotificacionService;
import com.parqueo.parkingApp.model.Notificacion;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SancionServiceImpl implements SancionService {

    private final SancionRepository repository;
    private final SancionMapper mapper;
    @Autowired
    private HistorialUsoService historialUsoService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private ReglasEstacionamientoRepository reglasEstacionamientoRepository;
    @Autowired
    private SancionDetalleService sancionDetalleService;
    
    @Autowired
    private NotificacionService notificacionService;

    @Override
    public List<SancionDto> obtenerTodos() {
        return repository.findAllWithRegistrador().stream()
                .map(SancionMapper::toDto)
                .toList();
    }

    @Override
    public SancionDto obtenerPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        Sancion sancion = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sanción no encontrada con ID: " + id));
        return SancionMapper.toDto(sancion);
    }

    @Override
    public SancionDto crear(SancionDto dto) {
        validarDatosSancion(dto);
        Sancion nueva = SancionMapper.toEntity(dto);
        nueva.setUsuario(usuarioRepository.findById(dto.getUsuarioId())
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado")));
        nueva.setVehiculo(vehiculoRepository.findById(dto.getVehiculoId())
            .orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado")));
        Sancion guardada = repository.save(nueva);
        historialUsoService.registrarEvento(guardada.getUsuario(), HistorialUso.AccionHistorial.SANCION);
        return SancionMapper.toDto(guardada);
    }

    @Override
    public SancionDto actualizar(Long id, SancionDto dto) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Sanción no encontrada con ID: " + id);
        }

        validarDatosSancion(dto);

        Sancion original = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sanción no encontrada con ID: " + id));
        Sancion actualizada = SancionMapper.toEntity(dto);
        actualizada.setId(id);
        actualizada.setUsuario(usuarioRepository.findById(dto.getUsuarioId())
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado")));
        actualizada.setVehiculo(vehiculoRepository.findById(dto.getVehiculoId())
            .orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado")));
        actualizada.setRegistradaPor(original.getRegistradaPor());
        if (original.getEstado() != dto.getEstado() && (dto.getEstado() == Sancion.EstadoSancion.RESUELTA || dto.getEstado() == Sancion.EstadoSancion.ANULADA)) {
            actualizada.setFechaResolucion(java.time.LocalDateTime.now());
        } else {
            actualizada.setFechaResolucion(original.getFechaResolucion());
        }
        if (dto.getObservaciones() != null) {
            actualizada.setObservaciones(dto.getObservaciones());
        } else {
            actualizada.setObservaciones(original.getObservaciones());
        }
        if (original.getEstado() != dto.getEstado() && dto.getEstado() == Sancion.EstadoSancion.RESUELTA) {
            historialUsoService.registrarEvento(actualizada.getUsuario(), HistorialUso.AccionHistorial.DESBLOQUEO);
            
            // Crear notificación de sanción resuelta
            String tituloNotificacion = "Sanción Resuelta";
            String mensajeNotificacion = String.format("Tu sanción #%d ha sido resuelta por un administrador. Ya puedes volver a usar el sistema.", 
                    actualizada.getId());
            notificacionService.crearNotificacion(actualizada.getUsuario(), tituloNotificacion, mensajeNotificacion, Notificacion.TipoNotificacion.DESBLOQUEO);
        }
        
        if (original.getEstado() != dto.getEstado() && dto.getEstado() == Sancion.EstadoSancion.ANULADA) {
            // Crear notificación de sanción anulada
            String tituloNotificacion = "Sanción Anulada";
            String mensajeNotificacion = String.format("Tu sanción #%d ha sido anulada por un administrador. La sanción ya no tiene efecto.", 
                    actualizada.getId());
            notificacionService.crearNotificacion(actualizada.getUsuario(), tituloNotificacion, mensajeNotificacion, Notificacion.TipoNotificacion.DESBLOQUEO);
        }
        Sancion guardada = repository.save(actualizada);
        actualizarEstadoUsuarioPorSanciones(guardada.getUsuario());
        return SancionMapper.toDto(guardada);
    }

    @Override
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Sanción no encontrada con ID: " + id);
        }
        
        repository.deleteById(id);
    }

    @Override
    public List<SancionDto> buscarPorUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser null");
        }
        
        return repository.findByUsuarioId(usuarioId).stream()
                .map(SancionMapper::toDto)
                .toList();
    }

    @Override
    public List<SancionDto> buscarPorVehiculo(Long vehiculoId) {
        if (vehiculoId == null) {
            throw new IllegalArgumentException("El ID del vehículo no puede ser null");
        }
        
        return repository.findByVehiculoId(vehiculoId).stream()
                .map(SancionMapper::toDto)
                .toList();
    }

    @Override
    public List<SancionDto> buscarPorEstado(Sancion.EstadoSancion estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser null");
        }
        
        return repository.findByEstado(estado).stream()
                .map(SancionMapper::toDto)
                .toList();
    }

    @Override
    public List<SancionDto> buscarPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser null");
        }
        
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        
        return repository.findByRegistroSancionBetween(fechaInicio, fechaFin).stream()
                .map(SancionMapper::toDto)
                .toList();
    }

    @Override
    public List<SancionDto> buscarSancionesActivas() {
        return repository.findByEstado(Sancion.EstadoSancion.ACTIVA).stream()
                .map(SancionMapper::toDto)
                .toList();
    }

    @Override
    public Sancion guardarEntidad(Sancion sancion) {
        return repository.save(sancion);
    }

    @Override
    public SancionDto crearConRegistrador(SancionDto dto, com.parqueo.parkingApp.model.Usuario registradaPor) {
        // BLOQUEO: Verificar si el usuario ya está suspendido actualmente
        List<Sancion> sancionesUsuario = repository.findByUsuarioId(dto.getUsuarioId());
        boolean suspendido = sancionesUsuario.stream().anyMatch(s ->
            s.getTipoCastigo() != null &&
            s.getTipoCastigo().toLowerCase().contains("suspensión") &&
            s.getEstado() == Sancion.EstadoSancion.ACTIVA &&
            s.getFechaInicioSuspension() != null &&
            s.getFechaFinSuspension() != null &&
            java.time.LocalDateTime.now().isAfter(s.getFechaInicioSuspension()) &&
            java.time.LocalDateTime.now().isBefore(s.getFechaFinSuspension())
        );
        if (suspendido) {
            throw new IllegalArgumentException("El usuario ya está suspendido. No se puede crear una nueva sanción hasta que termine la suspensión actual.");
        }
        validarDatosSancion(dto);
        Sancion nueva = mapper.toEntity(dto);
        nueva.setUsuario(usuarioRepository.findById(dto.getUsuarioId())
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado")));
        nueva.setVehiculo(vehiculoRepository.findById(dto.getVehiculoId())
            .orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado")));
        nueva.setRegistradaPor(registradaPor);

        // --- INICIO LÓGICA DE REINCIDENCIA Y SANCIÓN AUTOMÁTICA ---
        Long reglaId = dto.getReglaId();
        if (reglaId != null) {
            ReglasEstacionamiento regla = reglasEstacionamientoRepository.findById(reglaId)
                .orElseThrow(() -> new EntityNotFoundException("Regla no encontrada"));
            ReglasEstacionamiento.TipoFalta tipoFalta = regla.getTipoFalta();
            // Contar reincidencias (sanciones activas o resueltas de ese tipo de falta)
            long reincidencias = sancionesUsuario.stream()
                .filter(s -> s.getDetalles().stream().anyMatch(det -> {
                    if (det.getRegla() != null && det.getRegla().getTipoFalta() == tipoFalta) {
                        return s.getEstado() == Sancion.EstadoSancion.ACTIVA || s.getEstado() == Sancion.EstadoSancion.RESUELTA;
                    }
                    return false;
                }))
                .count();
            // Determinar el castigo según la tabla
            String tipoCastigo = "Amonestación";
            LocalDateTime inicioSusp = null;
            LocalDateTime finSusp = null;
            if (tipoFalta == ReglasEstacionamiento.TipoFalta.LEVE) {
                if (reincidencias == 0 || reincidencias == 1) {
                    tipoCastigo = "Llamada de atención";
                } else if (reincidencias == 2) {
                    tipoCastigo = "Suspensión temporal 1 semana";
                    inicioSusp = LocalDateTime.now();
                    finSusp = inicioSusp.plusWeeks(1);
                } else if (reincidencias >= 3) {
                    tipoCastigo = "Suspensión 1 mes";
                    inicioSusp = LocalDateTime.now();
                    finSusp = inicioSusp.plusMonths(1);
                }
            } else if (tipoFalta == ReglasEstacionamiento.TipoFalta.GRAVE) {
                if (reincidencias == 0) {
                    tipoCastigo = "Llamada de atención";
                } else if (reincidencias == 1) {
                    tipoCastigo = "Suspensión temporal 1 semana";
                    inicioSusp = LocalDateTime.now();
                    finSusp = inicioSusp.plusWeeks(1);
                } else if (reincidencias >= 2) {
                    tipoCastigo = "Suspensión 1 mes";
                    inicioSusp = LocalDateTime.now();
                    finSusp = inicioSusp.plusMonths(1);
                }
            }
            nueva.setTipoCastigo(tipoCastigo);
            nueva.setFechaInicioSuspension(inicioSusp);
            nueva.setFechaFinSuspension(finSusp);
        }
        // --- FIN LÓGICA DE REINCIDENCIA Y SANCIÓN AUTOMÁTICA ---

        Sancion guardada = repository.save(nueva);
        historialUsoService.registrarEvento(guardada.getUsuario(), HistorialUso.AccionHistorial.SANCION);

        // Crear notificación de sanción aplicada con mensaje más claro
        String tituloNotificacion = "🚨 Sanción Aplicada";
        String mensajeNotificacion;
        
        if (guardada.getTipoCastigo() != null && guardada.getTipoCastigo().toLowerCase().contains("suspensión")) {
            // Mensaje específico para suspensiones
            String duracionSuspension = "";
            if (guardada.getFechaInicioSuspension() != null && guardada.getFechaFinSuspension() != null) {
                long dias = java.time.Duration.between(guardada.getFechaInicioSuspension(), guardada.getFechaFinSuspension()).toDays();
                duracionSuspension = String.format("Tu cuenta ha sido suspendida por %d días", dias);
            }
            
            mensajeNotificacion = String.format("Se ha aplicado una sanción por exceder el tiempo de reserva #%d. %s. " +
                    "Espacio: %s, Motivo: %s. " +
                    "Durante la suspensión no podrás acceder al sistema de reservas.", 
                    guardada.getId(),
                    duracionSuspension,
                    guardada.getVehiculo() != null ? guardada.getVehiculo().getPlaca() : "N/A",
                    guardada.getMotivo());
        } else {
            // Mensaje para otros tipos de sanción
            mensajeNotificacion = String.format("Se ha aplicado una sanción por: %s. Motivo: %s", 
                    guardada.getTipoCastigo() != null ? guardada.getTipoCastigo() : "Infracción",
                    guardada.getMotivo());
        }
        
        notificacionService.crearNotificacion(guardada.getUsuario(), tituloNotificacion, mensajeNotificacion, Notificacion.TipoNotificacion.SANCION_APLICADA);

        // Lógica de activar/inactivar usuario según sanciones de suspensión
        actualizarEstadoUsuarioPorSanciones(guardada.getUsuario());

        return mapper.toDto(guardada);
    }

    /**
     * Activa o inactiva el usuario según sus sanciones de suspensión activas.
     * Si tiene alguna sanción ACTIVA con tipoCastigo que contenga 'suspensión', queda inactivo.
     * Si no tiene ninguna sanción ACTIVA de suspensión, queda activo.
     */
    private void actualizarEstadoUsuarioPorSanciones(com.parqueo.parkingApp.model.Usuario usuario) {
        List<Sancion> sanciones = repository.findByUsuarioId(usuario.getId());
        boolean tieneSuspensionActiva = sanciones.stream().anyMatch(s ->
            s.getEstado() == Sancion.EstadoSancion.ACTIVA &&
            s.getTipoCastigo() != null &&
            s.getTipoCastigo().toLowerCase().contains("suspensión") &&
            s.getFechaInicioSuspension() != null &&
            s.getFechaFinSuspension() != null &&
            LocalDateTime.now().isAfter(s.getFechaInicioSuspension()) &&
            LocalDateTime.now().isBefore(s.getFechaFinSuspension())
        );
        
        if (tieneSuspensionActiva) {
            // SUSPENDER USUARIO
            if (Boolean.TRUE.equals(usuario.getActivo())) {
                usuario.setActivo(false);
                usuarioRepository.save(usuario);
                System.out.println("Usuario " + usuario.getUsername() + " SUSPENDIDO por sanción activa");
            }
            
            // Desactivar todos los vehículos asociados
            List<com.parqueo.parkingApp.model.Vehiculo> vehiculosUsuario = vehiculoRepository.findByUsuarioId(usuario.getId());
            for (com.parqueo.parkingApp.model.Vehiculo vehiculo : vehiculosUsuario) {
                if (Boolean.TRUE.equals(vehiculo.getActivo())) {
                    vehiculo.setActivo(false);
                    vehiculoRepository.save(vehiculo);
                    System.out.println("Vehículo " + vehiculo.getPlaca() + " DESACTIVADO por suspensión del usuario");
                }
            }
        } else {
            // ACTIVAR USUARIO (si no tiene suspensiones activas)
            if (!Boolean.TRUE.equals(usuario.getActivo())) {
                usuario.setActivo(true);
                usuarioRepository.save(usuario);
                System.out.println("Usuario " + usuario.getUsername() + " ACTIVADO - sin suspensiones activas");
            }
            
            // Activar todos los vehículos asociados
            List<com.parqueo.parkingApp.model.Vehiculo> vehiculosUsuario = vehiculoRepository.findByUsuarioId(usuario.getId());
            for (com.parqueo.parkingApp.model.Vehiculo vehiculo : vehiculosUsuario) {
                if (!Boolean.TRUE.equals(vehiculo.getActivo())) {
                    vehiculo.setActivo(true);
                    vehiculoRepository.save(vehiculo);
                    System.out.println("Vehículo " + vehiculo.getPlaca() + " ACTIVADO - usuario sin suspensiones");
                }
            }
        }
    }

    private void validarDatosSancion(SancionDto dto) {
        if (dto.getUsuarioId() == null) {
            throw new IllegalArgumentException("El ID del usuario no puede estar vacío");
        }
        if (dto.getVehiculoId() == null) {
            throw new IllegalArgumentException("El ID del vehículo no puede estar vacío");
        }
        if (dto.getMotivo() == null || dto.getMotivo().trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo no puede estar vacío");
        }
        if (dto.getMotivo().trim().length() < 10) {
            throw new IllegalArgumentException("El motivo debe tener al menos 10 caracteres");
        }
        if (dto.getMotivo().trim().length() > 500) {
            throw new IllegalArgumentException("El motivo no puede tener más de 500 caracteres");
        }
        if (dto.getEstado() == null) {
            throw new IllegalArgumentException("El estado no puede estar vacío");
        }
        if (dto.getRegistroSancion() == null) {
            throw new IllegalArgumentException("La fecha de registro no puede estar vacía");
        }
        if (dto.getReglaId() == null) {
            throw new IllegalArgumentException("La regla infringida no puede estar vacía");
        }
        if (dto.getObservaciones() != null && dto.getObservaciones().length() > 1000) {
            throw new IllegalArgumentException("Las observaciones no pueden tener más de 1000 caracteres");
        }
    }
}