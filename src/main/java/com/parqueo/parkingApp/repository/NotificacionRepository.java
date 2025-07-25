package com.parqueo.parkingApp.repository;

import com.parqueo.parkingApp.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    List<Notificacion> findByUsuarioIdAndLeidaOrderByFechaCreacionDesc(Long usuarioId, Boolean leida);

    Long countByUsuarioIdAndLeida(Long usuarioId, Boolean leida);

    List<Notificacion> findByUsuarioIdAndTipoOrderByFechaCreacionDesc(Long usuarioId, Notificacion.TipoNotificacion tipo);
} 