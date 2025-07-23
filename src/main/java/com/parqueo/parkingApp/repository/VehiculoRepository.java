package com.parqueo.parkingApp.repository;

import com.parqueo.parkingApp.model.Vehiculo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    boolean existsByPlaca(String placa);
    Optional<Vehiculo> findByPlaca(String placa);
    List<Vehiculo> findByUsuarioId(Long usuarioId);
    List<Vehiculo> findByTipo(Vehiculo.TipoVehiculo tipo);
    
    // Métodos para filtrar por estado activo
    List<Vehiculo> findByActivo(Boolean activo);
    List<Vehiculo> findByUsuarioIdAndActivo(Long usuarioId, Boolean activo);
    List<Vehiculo> findByTipoAndActivo(Vehiculo.TipoVehiculo tipo, Boolean activo);
    
    // Método para obtener solo vehículos activos
    @Query("SELECT v FROM Vehiculo v WHERE v.activo = true")
    List<Vehiculo> findAllActivos();
}
