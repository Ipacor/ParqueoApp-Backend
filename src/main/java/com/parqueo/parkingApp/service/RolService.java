package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.model.Rol;
import java.util.List;

public interface RolService {
    List<Rol> obtenerTodos();
    Rol obtenerPorId(Long id);
    Rol crear(Rol rol, List<String> permisos);
    Rol actualizar(Long id, String nombre, String descripcion, List<String> permisos);
    void eliminar(Long id);
} 