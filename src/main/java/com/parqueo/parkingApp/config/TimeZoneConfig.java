package com.parqueo.parkingApp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@Configuration
public class TimeZoneConfig {

    @PostConstruct
    public void init() {
        // Establecer la zona horaria por defecto para toda la aplicación
        TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
        System.setProperty("user.timezone", "America/Lima");
    }

    @Bean
    @Primary
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization() {
        return builder -> {
            builder.timeZone(TimeZone.getTimeZone("America/Lima"));
        };
    }
} 