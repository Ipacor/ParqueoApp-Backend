package com.parqueo.parkingApp.repository;

import com.parqueo.parkingApp.model.EspacioDisponible;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EspacioDisponibleRepository extends JpaRepository<EspacioDisponible, Long> {
    boolean existsByUbicacion(String ubicacion);
    List<EspacioDisponible> findByEstado(EspacioDisponible.EstadoEspacio estado);
    List<EspacioDisponible> findByZona(String zona);
    
    // Métodos para estadísticas del dashboard
    long countByEstado(EspacioDisponible.EstadoEspacio estado);
}
