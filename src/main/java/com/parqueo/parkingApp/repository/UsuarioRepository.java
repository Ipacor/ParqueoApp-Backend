package com.parqueo.parkingApp.repository;

import com.parqueo.parkingApp.model.Usuario;
import com.parqueo.parkingApp.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByRol(Rol rol);

    // Ahora el método acepta el enum directamente
    boolean existsByRol(Rol rol);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
    
    long countByRol(Rol rol);

    // Usuarios con sanción activa de suspensión
    @Query("SELECT DISTINCT u FROM Usuario u JOIN u.sanciones s WHERE s.estado = com.parqueo.parkingApp.model.Sancion.EstadoSancion.ACTIVA AND s.tipoCastigo LIKE %:tipoCastigo% AND s.fechaInicioSuspension <= CURRENT_TIMESTAMP AND s.fechaFinSuspension >= CURRENT_TIMESTAMP")
    List<Usuario> findUsuariosSuspendidos(@Param("tipoCastigo") String tipoCastigo);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT u FROM Usuario u JOIN u.sanciones s WHERE s.estado = com.parqueo.parkingApp.model.Sancion.EstadoSancion.ACTIVA")
    List<Usuario> findUsuariosConSancionActiva();
    
    // Métodos para estadísticas del dashboard
    long countByActivoTrue();
    
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol.nombre = :rol")
    long countByRolNombre(@Param("rol") String rol);
    
    // Método para obtener usuarios que NO tienen un rol específico
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre != :rol")
    List<Usuario> findByRolNombreNot(@Param("rol") String rol);
}
