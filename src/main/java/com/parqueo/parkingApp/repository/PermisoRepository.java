package com.parqueo.parkingApp.repository;

import com.parqueo.parkingApp.model.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {
    
    Optional<Permiso> findByNombre(String nombre);
    
    boolean existsByNombre(String nombre);

    List<Permiso> findAllByNombreIn(List<String> nombres);
} 