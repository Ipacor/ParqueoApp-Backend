package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.model.Permiso;

import java.util.List;
import java.util.Optional;

public interface PermisoService {
    
    List<Permiso> obtenerTodos();
    
    Permiso obtenerPorId(Long id);
    
    Optional<Permiso> obtenerPorNombre(String nombre);
    
    Permiso crear(Permiso permiso);
    
    Permiso actualizar(Long id, Permiso permiso);
    
    void eliminar(Long id);
    
    boolean existePorNombre(String nombre);
} 