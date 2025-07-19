package com.parqueo.parkingApp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "API ParqueoApp funcionando correctamente");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "ParqueoApp Backend");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/time")
    public ResponseEntity<Map<String, Object>> getServerTime() {
        Map<String, Object> response = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        response.put("serverTime", now);
        response.put("serverTimeZone", ZoneId.systemDefault().getId());
        response.put("formattedTime", now.toString());
        return ResponseEntity.ok(response);
    }
} 