package com.parqueo.parkingApp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.parqueo.parkingApp.dto.EscaneoQRDto;
import com.parqueo.parkingApp.mapper.EscaneoQRMapper;
import com.parqueo.parkingApp.model.EscaneoQR;
import com.parqueo.parkingApp.model.Reserva;
import com.parqueo.parkingApp.repository.EscaneoQRRepository;
import com.parqueo.parkingApp.repository.ReservaRepository;
import com.parqueo.parkingApp.repository.EspacioDisponibleRepository;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EscaneoQRServiceImpl implements EscaneoQRService {

    private final EscaneoQRRepository escaneoRepo;
    private final ReservaRepository reservaRepo;
    private final EscaneoQRMapper mapper;
    private final EspacioDisponibleRepository espacioRepo;

    @Override
    public List<EscaneoQRDto> obtenerTodos() {
        return escaneoRepo.findAll().stream()
                .map(EscaneoQRMapper::toDto)
                .toList();
    }

    @Override
    public EscaneoQRDto obtenerPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        EscaneoQR escaneo = escaneoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Escaneo no encontrado con ID: " + id));
        return EscaneoQRMapper.toDto(escaneo);
    }

    @Override
    public EscaneoQRDto crear(EscaneoQRDto dto) {
        validarDatosEscaneo(dto);
        EscaneoQR nuevo = EscaneoQRMapper.toEntity(dto);
        nuevo.setToken(UUID.randomUUID().toString());
        nuevo.setTimestampEnt(LocalDateTime.now());
        return EscaneoQRMapper.toDto(escaneoRepo.save(nuevo));
    }

    @Override
    public EscaneoQRDto actualizar(Long id, EscaneoQRDto dto) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        if (!escaneoRepo.existsById(id)) {
            throw new EntityNotFoundException("Escaneo no encontrado con ID: " + id);
        }

        validarDatosEscaneo(dto);

        EscaneoQR actualizado = EscaneoQRMapper.toEntity(dto);
        actualizado.setId(id);

        return EscaneoQRMapper.toDto(escaneoRepo.save(actualizado));
    }

    @Override
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        if (!escaneoRepo.existsById(id)) {
            throw new EntityNotFoundException("Escaneo no encontrado con ID: " + id);
        }
        
        escaneoRepo.deleteById(id);
    }

    @Override
    public EscaneoQRDto registrarEntrada(Long reservaId) {
        if (reservaId == null) {
            throw new IllegalArgumentException("El ID de la reserva no puede ser null");
        }
        Reserva reserva = reservaRepo.findById(reservaId)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada con ID: " + reservaId));

        // Buscar el QR de ENTRADA
        EscaneoQR qr = escaneoRepo.findByReserva(reserva)
                .orElseThrow(() -> new EntityNotFoundException("QR de entrada no encontrado para esta reserva"));

        // Marcar entrada
        qr.setTimestampEnt(LocalDateTime.now());

        // Convertir a QR de SALIDA
        qr.setTipo("SALIDA");
        qr.setToken(UUID.randomUUID().toString());
        qr.setFechaExpiracion(null); // o ponle una expiración si lo deseas
        escaneoRepo.save(qr);

        // Cambiar estado de la reserva a ACTIVO
        reserva.setEstado(Reserva.EstadoReserva.ACTIVO);
        reservaRepo.save(reserva);

        // Cambiar estado del espacio a OCUPADO
        if (reserva.getEspacio() != null) {
            reserva.getEspacio().setEstado(com.parqueo.parkingApp.model.EspacioDisponible.EstadoEspacio.OCUPADO);
            espacioRepo.save(reserva.getEspacio());
        }

        return EscaneoQRMapper.toDto(qr);
    }

    @Override
    public EscaneoQRDto registrarSalida(Long reservaId) {
        if (reservaId == null) {
            throw new IllegalArgumentException("El ID de la reserva no puede ser null");
        }
        Reserva reserva = reservaRepo.findById(reservaId)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada con ID: " + reservaId));

        // Buscar el QR de SALIDA
        EscaneoQR qrSalida = escaneoRepo.findByReserva(reserva)
                .orElseThrow(() -> new EntityNotFoundException("QR de salida no encontrado para esta reserva"));
        if (!"SALIDA".equals(qrSalida.getTipo())) {
            throw new IllegalStateException("El QR actual no es de salida o ya fue usado");
        }
        qrSalida.setTipo("SALIDA_USADA");
        qrSalida.setTimestampSal(LocalDateTime.now());
        escaneoRepo.save(qrSalida);

        // Cambiar estado de la reserva a FINALIZADO
        reserva.setEstado(Reserva.EstadoReserva.FINALIZADO);
        reservaRepo.save(reserva);

        // Cambiar estado del espacio a DISPONIBLE
        if (reserva.getEspacio() != null) {
            reserva.getEspacio().setEstado(com.parqueo.parkingApp.model.EspacioDisponible.EstadoEspacio.DISPONIBLE);
            espacioRepo.save(reserva.getEspacio());
        }

        return EscaneoQRMapper.toDto(qrSalida);
    }

    @Override
    public EscaneoQRDto obtenerPorReserva(Long reservaId) {
        if (reservaId == null) {
            throw new IllegalArgumentException("El ID de la reserva no puede ser null");
        }
        Reserva reserva = reservaRepo.findById(reservaId)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada con ID: " + reservaId));

        EscaneoQR escaneo = escaneoRepo.findByReserva(reserva)
                .orElseThrow(() -> new EntityNotFoundException("Escaneo no encontrado para esta reserva"));

        return EscaneoQRMapper.toDto(escaneo);
    }

    @Override
    public List<EscaneoQRDto> buscarPorUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser null");
        }
        
        return escaneoRepo.findByReservaUsuarioId(usuarioId).stream()
                .map(EscaneoQRMapper::toDto)
                .toList();
    }

    @Override
    public List<EscaneoQRDto> buscarPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser null");
        }
        
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        
        // Implementar búsqueda por fecha usando una consulta personalizada
        return escaneoRepo.findByTimestampEntBetween(fechaInicio, fechaFin).stream()
                .map(EscaneoQRMapper::toDto)
                .toList();
    }

    private void validarDatosEscaneo(EscaneoQRDto dto) {
        if (dto.getReservaId() == null) {
            throw new IllegalArgumentException("El ID de la reserva no puede estar vacío");
        }
        
        if (dto.getTimestampEnt() == null) {
            throw new IllegalArgumentException("El timestamp de entrada no puede estar vacío");
        }
    }
}
