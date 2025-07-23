package com.parqueo.parkingApp.repository;

import com.parqueo.parkingApp.model.HistorialUso;
import com.parqueo.parkingApp.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialUsoRepository extends JpaRepository<HistorialUso, Long> {

    List<HistorialUso> findByUsuarioId(Long usuarioId);
    
    List<HistorialUso> findByUsuario(Usuario usuario);

    List<HistorialUso> findByEspacioId(Long espacioId);
}
