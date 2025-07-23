package com.parqueo.parkingApp.repository;

import com.parqueo.parkingApp.model.EspacioDisponible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EspacioDisponibleRepository extends JpaRepository<EspacioDisponible, Long> {
    boolean existsByUbicacion(String ubicacion);
    List<EspacioDisponible> findByEstado(EspacioDisponible.EstadoEspacio estado);
    List<EspacioDisponible> findByZona(String zona);
    
    // Métodos para estadísticas del dashboard
    long countByEstado(EspacioDisponible.EstadoEspacio estado);
    
    // Método para encontrar espacios ocupados por mucho tiempo (más de 4 horas)
    @Query("SELECT e FROM EspacioDisponible e WHERE e.estado = 'OCUPADO' " +
           "AND e.ultimaActualizacion IS NOT NULL " +
           "AND e.ultimaActualizacion < :horaLimite")
    List<EspacioDisponible> findEspaciosOcupadosLargoTiempo(@Param("horaLimite") java.time.LocalDateTime horaLimite);
}
