package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.UsuarioDto;
import com.parqueo.parkingApp.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    List<UsuarioDto> obtenerTodos();
    UsuarioDto obtenerPorId(Long id);
    UsuarioDto crear(UsuarioDto dto);
    UsuarioDto actualizar(Long id, UsuarioDto dto);
    void eliminar(Long id);
    void desactivarUsuario(Long id);
    void activarUsuario(Long id);
    void eliminarUsuarioCompleto(Long id);
    Optional<UsuarioDto> findByUsername(String username);
    Optional<UsuarioDto> findByEmail(String email);
    List<UsuarioDto> findByRolNombre(String rolNombre);
    List<UsuarioDto> obtenerUsuariosSuspendidos();
    List<UsuarioDto> obtenerUsuariosConSancionActiva();
}