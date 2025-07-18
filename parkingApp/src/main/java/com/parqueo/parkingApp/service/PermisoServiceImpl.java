package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.model.Permiso;
import com.parqueo.parkingApp.repository.PermisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PermisoServiceImpl implements PermisoService {

    private final PermisoRepository permisoRepository;

    @Override
    public List<Permiso> obtenerTodos() {
        return permisoRepository.findAll();
    }

    @Override
    public Permiso obtenerPorId(Long id) {
        return permisoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permiso no encontrado con id: " + id));
    }

    @Override
    public Optional<Permiso> obtenerPorNombre(String nombre) {
        return permisoRepository.findByNombre(nombre);
    }

    @Override
    public Permiso crear(Permiso permiso) {
        if (permisoRepository.existsByNombre(permiso.getNombre())) {
            throw new IllegalArgumentException("Ya existe un permiso con el nombre: " + permiso.getNombre());
        }
        return permisoRepository.save(permiso);
    }

    @Override
    public Permiso actualizar(Long id, Permiso permiso) {
        Permiso existente = obtenerPorId(id);
        
        // Verificar si el nuevo nombre ya existe en otro permiso
        if (!existente.getNombre().equals(permiso.getNombre()) && 
            permisoRepository.existsByNombre(permiso.getNombre())) {
            throw new IllegalArgumentException("Ya existe un permiso con el nombre: " + permiso.getNombre());
        }
        
        existente.setNombre(permiso.getNombre());
        existente.setDescripcion(permiso.getDescripcion());
        
        return permisoRepository.save(existente);
    }

    @Override
    public void eliminar(Long id) {
        if (!permisoRepository.existsById(id)) {
            throw new IllegalArgumentException("Permiso no encontrado con id: " + id);
        }
        permisoRepository.deleteById(id);
    }

    @Override
    public boolean existePorNombre(String nombre) {
        return permisoRepository.existsByNombre(nombre);
    }
} 