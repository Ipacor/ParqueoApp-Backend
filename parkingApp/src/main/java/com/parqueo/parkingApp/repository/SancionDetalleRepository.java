package com.parqueo.parkingApp.repository;

import com.parqueo.parkingApp.model.SancionDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SancionDetalleRepository extends JpaRepository<SancionDetalle, Long> {
    List<SancionDetalle> findBySancionId(Long sancionId);
}
