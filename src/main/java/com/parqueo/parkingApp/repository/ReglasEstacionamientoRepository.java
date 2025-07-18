package com.parqueo.parkingApp.repository;

import com.parqueo.parkingApp.model.ReglasEstacionamiento;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReglasEstacionamientoRepository extends JpaRepository<ReglasEstacionamiento, Long> {
    Optional<ReglasEstacionamiento> findById(Long id);
}
