package com.parqueo.parkingApp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.parqueo.parkingApp.dto.VehiculoDto;
import com.parqueo.parkingApp.mapper.VehiculoMapper;
import com.parqueo.parkingApp.model.Vehiculo;
import com.parqueo.parkingApp.model.Usuario;
import com.parqueo.parkingApp.repository.VehiculoRepository;
import com.parqueo.parkingApp.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository vehiculoRepo;
    private final UsuarioRepository usuarioRepo;
    private final VehiculoMapper mapper;

    @Override
    public List<VehiculoDto> obtenerTodos() {
        return vehiculoRepo.findAll().stream().map(VehiculoMapper::toDto).toList();
    }

    @Override
    public VehiculoDto obtenerPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        Vehiculo vehiculo = vehiculoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado con ID: " + id));
        return VehiculoMapper.toDto(vehiculo);
    }

    @Override
    public VehiculoDto crear(VehiculoDto dto) {
        validarDatosVehiculo(dto);
        
        if (vehiculoRepo.existsByPlaca(dto.getPlaca())) {
            throw new RuntimeException("Ya existe un vehículo con esa placa");
        }
        
        // Buscar el usuario
        Usuario usuario = usuarioRepo.findById(dto.getUsuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + dto.getUsuarioId()));
        
        Vehiculo vehiculo = VehiculoMapper.toEntity(dto);
        vehiculo.setUsuario(usuario);
        return VehiculoMapper.toDto(vehiculoRepo.save(vehiculo));
    }

    @Override
    public VehiculoDto actualizar(Long id, VehiculoDto dto) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }

        Vehiculo vehiculoExistente = vehiculoRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado con ID: " + id));

        validarDatosVehiculo(dto);

        // Verificar que la placa no esté duplicada (excepto para el mismo vehículo)
        Optional<Vehiculo> vehiculoConPlaca = vehiculoRepo.findByPlaca(dto.getPlaca());
        if (vehiculoConPlaca.isPresent() && !vehiculoConPlaca.get().getId().equals(id)) {
            throw new RuntimeException("Ya existe un vehículo con esa placa");
        }

        // Buscar el usuario
        Usuario usuario = usuarioRepo.findById(dto.getUsuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + dto.getUsuarioId()));

        // Actualizar solo los campos permitidos
        vehiculoExistente.setPlaca(dto.getPlaca());
        vehiculoExistente.setModelo(dto.getModelo());
        vehiculoExistente.setMarca(dto.getMarca());
        vehiculoExistente.setColor(dto.getColor());
        vehiculoExistente.setTipo(dto.getTipo());
        vehiculoExistente.setUsuario(usuario);
        if (dto.getActivo() != null) {
            vehiculoExistente.setActivo(dto.getActivo());
        }
        if (dto.getFechaRegistro() != null) {
            vehiculoExistente.setFechaRegistro(dto.getFechaRegistro());
        }
        // No toques fechaRegistro si es null

        Vehiculo vehiculoActualizado = vehiculoRepo.save(vehiculoExistente);
        return VehiculoMapper.toDto(vehiculoActualizado);
    }

    @Override
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        
        if (!vehiculoRepo.existsById(id)) {
            throw new EntityNotFoundException("Vehículo no encontrado con ID: " + id);
        }
        
        vehiculoRepo.deleteById(id);
    }

    @Override
    public Optional<VehiculoDto> buscarPorPlaca(String placa) {
        if (placa == null || placa.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return vehiculoRepo.findByPlaca(placa).map(VehiculoMapper::toDto);
    }

    @Override
    public List<VehiculoDto> buscarPorUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser null");
        }
        
        // Por defecto solo retornar vehículos activos
        return vehiculoRepo.findByUsuarioIdAndActivo(usuarioId, true).stream()
                .map(VehiculoMapper::toDto)
                .toList();
    }

    /**
     * Obtiene todos los vehículos de un usuario (activos e inactivos) - para administradores
     */
    public List<VehiculoDto> buscarTodosPorUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser null");
        }
        
        // Retornar todos los vehículos del usuario, incluyendo inactivos
        return vehiculoRepo.findByUsuarioId(usuarioId).stream()
                .map(VehiculoMapper::toDto)
                .toList();
    }

    @Override
    public List<VehiculoDto> buscarPorTipo(Vehiculo.TipoVehiculo tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de vehículo no puede ser null");
        }
        
        return vehiculoRepo.findByTipoAndActivo(tipo, true).stream()
                .map(VehiculoMapper::toDto)
                .toList();
    }
    
    /**
     * Obtiene todos los vehículos (activos e inactivos) - para administradores
     */
    public List<VehiculoDto> obtenerTodosIncluyendoInactivos() {
        return vehiculoRepo.findAll().stream()
                .map(VehiculoMapper::toDto)
                .toList();
    }
    
    /**
     * Obtiene solo vehículos activos
     */
    public List<VehiculoDto> obtenerSoloActivos() {
        return vehiculoRepo.findAllActivos().stream()
                .map(VehiculoMapper::toDto)
                .toList();
    }

    private void validarDatosVehiculo(VehiculoDto dto) {
        if (dto.getPlaca() == null || dto.getPlaca().trim().isEmpty()) {
            throw new IllegalArgumentException("La placa no puede estar vacía");
        }
        if (!dto.getPlaca().matches("^[A-Z]{3}[0-9]{3}$")) {
            throw new IllegalArgumentException("La placa debe tener el formato AAA000");
        }
        
        if (dto.getModelo() == null || dto.getModelo().trim().isEmpty()) {
            throw new IllegalArgumentException("El modelo no puede estar vacío");
        }
        
        if (dto.getMarca() == null || dto.getMarca().trim().isEmpty()) {
            throw new IllegalArgumentException("La marca no puede estar vacía");
        }
        
        if (dto.getColor() == null || dto.getColor().trim().isEmpty()) {
            throw new IllegalArgumentException("El color no puede estar vacío");
        }
        
        if (dto.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de vehículo no puede estar vacío");
        }
        
        if (dto.getUsuarioId() == null) {
            throw new IllegalArgumentException("El ID del usuario no puede estar vacío");
        }
    }
}
