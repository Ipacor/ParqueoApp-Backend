package com.parqueo.parkingApp.repository;

import com.parqueo.parkingApp.model.HistorialUso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialUsoRepository extends JpaRepository<HistorialUso, Long> {

    List<HistorialUso> findByUsuarioId(Long usuarioId);

    List<HistorialUso> findByEspacioId(Long espacioId);
}
