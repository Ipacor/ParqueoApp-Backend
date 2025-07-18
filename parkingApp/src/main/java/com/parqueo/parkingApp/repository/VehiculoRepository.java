package com.parqueo.parkingApp.repository;

import com.parqueo.parkingApp.model.Vehiculo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    boolean existsByPlaca(String placa);
    Optional<Vehiculo> findByPlaca(String placa);
    List<Vehiculo> findByUsuarioId(Long usuarioId);
    List<Vehiculo> findByTipo(Vehiculo.TipoVehiculo tipo);
}
