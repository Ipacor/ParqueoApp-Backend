package com.parqueo.parkingApp.repository;

import com.parqueo.parkingApp.model.HistorialUso;
import com.parqueo.parkingApp.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialUsoRepository extends JpaRepository<HistorialUso, Long> {

    List<HistorialUso> findByUsuarioId(Long usuarioId);
    
    List<HistorialUso> findByUsuario(Usuario usuario);

    List<HistorialUso> findByEspacioId(Long espacioId);
    
    List<HistorialUso> findByReservaId(Long reservaId);
    
    // Métodos para estadísticas del dashboard
    @Query("SELECT COUNT(h) FROM HistorialUso h WHERE h.fechaUso BETWEEN :fechaInicio AND :fechaFin")
    long countByFechaEntradaBetween(@Param("fechaInicio") java.time.LocalDateTime fechaInicio, @Param("fechaFin") java.time.LocalDateTime fechaFin);
}
