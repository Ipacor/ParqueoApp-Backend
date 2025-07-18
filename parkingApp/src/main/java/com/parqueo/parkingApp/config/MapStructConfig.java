package com.parqueo.parkingApp.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
    "com.parqueo.parkingApp.mapper",
    "com.parqueo.parkingApp.service",
    "com.parqueo.parkingApp.controller",
    "com.parqueo.parkingApp.repository"
})
public class MapStructConfig {
    // Esta configuración asegura que Spring escanee todos los mappers generados por MapStruct
} 