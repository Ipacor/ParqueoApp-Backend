package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.model.Rol;
import com.parqueo.parkingApp.model.Permiso;
import com.parqueo.parkingApp.repository.RolRepository;
import com.parqueo.parkingApp.repository.PermisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    @Override
    public List<Rol> obtenerTodos() {
        return rolRepository.findAll();
    }

    @Override
    public Rol obtenerPorId(Long id) {
        return rolRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));
    }

    @Override
    @Transactional
    public Rol crear(Rol rol, List<String> permisos) {
        List<Permiso> permisosEntidades = permisoRepository.findAllByNombreIn(permisos);
        rol.setPermisos(permisosEntidades.stream().collect(Collectors.toSet()));
        return rolRepository.save(rol);
    }

    @Override
    @Transactional
    public Rol actualizar(Long id, String nombre, String descripcion, List<String> permisos) {
        Rol rol = obtenerPorId(id);
        rol.setNombre(nombre);
        rol.setDescripcion(descripcion);
        List<Permiso> permisosEntidades = permisoRepository.findAllByNombreIn(permisos);
        rol.setPermisos(permisosEntidades.stream().collect(Collectors.toSet()));
        return rolRepository.save(rol);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        rolRepository.deleteById(id);
    }
} 