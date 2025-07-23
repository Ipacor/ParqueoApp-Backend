package com.parqueo.parkingApp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Esta configuración habilita las tareas programadas (@Scheduled)
    // Las tareas se ejecutarán automáticamente según los intervalos definidos
} 