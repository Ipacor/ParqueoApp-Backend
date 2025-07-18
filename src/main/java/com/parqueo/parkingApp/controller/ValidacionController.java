package com.parqueo.parkingApp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
import com.parqueo.parkingApp.model.EscaneoQR;
import com.parqueo.parkingApp.repository.EscaneoQRRepository;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import java.time.LocalDateTime;
import com.parqueo.parkingApp.service.EscaneoQRService;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/validaciones")
public class ValidacionController {
    @Autowired
    private EscaneoQRRepository escaneoQRRepository;
    @Autowired
    private EscaneoQRService escaneoQRService;

    @GetMapping("/usuario")
    @PreAuthorize("hasAuthority('VALIDACION_LEER_USUARIO')")
    public Map<String, Object> getUsuarioValidations() {
        Map<String, Object> reglas = new HashMap<>();
        reglas.put("username", Map.of(
            "required", true,
            "minLength", 3,
            "maxLength", 50,
            "messageRequired", "El nombre de usuario es obligatorio",
            "messageLength", "El nombre de usuario debe tener entre 3 y 50 caracteres"
        ));
        reglas.put("password", Map.of(
            "required", true,
            "minLength", 6,
            "maxLength", 100,
            "messageRequired", "La contraseña es obligatoria",
            "messageLength", "La contraseña debe tener al menos 6 caracteres"
        ));
        reglas.put("nombreCompleto", Map.of(
            "required", true,
            "minLength", 2,
            "maxLength", 100,
            "messageRequired", "El nombre completo es obligatorio",
            "messageLength", "El nombre completo debe tener entre 2 y 100 caracteres"
        ));
        reglas.put("email", Map.of(
            "required", true,
            "pattern", "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$",
            "maxLength", 100,
            "messageRequired", "El email es obligatorio",
            "messagePattern", "El formato del email no es válido"
        ));
        reglas.put("rol", Map.of(
            "required", true,
            "messageRequired", "El rol es obligatorio"
        ));
        return reglas;
    }

    @GetMapping("/qr")
    @PreAuthorize("hasAuthority('VALIDACION_VALIDAR_QR')")
    public ResponseEntity<Map<String, Object>> validarQR(
            @RequestParam String token,
            @RequestParam String tipo) {
        Map<String, Object> result = new HashMap<>();
        EscaneoQR qr = escaneoQRRepository.findByTokenAndTipo(token, tipo).orElse(null);
        EscaneoQR qrCualquierTipo = null;
        if (qr == null) {
            qrCualquierTipo = escaneoQRRepository.findAll().stream()
                .filter(q -> token.equals(q.getToken()))
                .findFirst().orElse(null);
        }
        if (qr == null && qrCualquierTipo == null) {
            result.put("valido", false);
            result.put("motivo", "QR no encontrado o tipo incorrecto");
            result.put("tipo", tipo);
            return ResponseEntity.ok(result);
        }
        EscaneoQR qrUsar = qr != null ? qr : qrCualquierTipo;
        result.put("tipo", qrUsar.getTipo());
        if ("ENTRADA".equals(tipo)) {
            if (qrUsar.getFechaInicioValidez() != null && LocalDateTime.now().isBefore(qrUsar.getFechaInicioValidez())) {
                result.put("valido", false);
                result.put("motivo", "QR de entrada aún no es válido. Solo se puede usar a partir de: " + qrUsar.getFechaInicioValidez());
                return ResponseEntity.ok(result);
            }
            if (qrUsar.getFechaExpiracion() != null && qrUsar.getFechaExpiracion().isBefore(LocalDateTime.now())) {
                result.put("valido", false);
                result.put("motivo", "QR de entrada expirado");
                return ResponseEntity.ok(result);
            }
            if ("ENTRADA_USADA".equals(qrUsar.getTipo()) || "SALIDA".equals(qrUsar.getTipo())) {
                result.put("valido", true);
                result.put("motivo", "QR ya validado para entrada. Use el QR de salida para registrar la salida.");
                if (qrUsar.getReserva() != null) {
                    Map<String, Object> reservaInfo = new HashMap<>();
                    if (qrUsar.getReserva().getUsuario() != null) {
                        reservaInfo.put("usuario", qrUsar.getReserva().getUsuario().getNombreCompleto());
                    }
                    if (qrUsar.getReserva().getVehiculo() != null) {
                        reservaInfo.put("vehiculo", qrUsar.getReserva().getVehiculo().getPlaca());
                    }
                    if (qrUsar.getReserva().getEspacio() != null) {
                        reservaInfo.put("espacio", qrUsar.getReserva().getEspacio().getNumeroEspacio());
                    }
                    reservaInfo.put("fechaInicio", qrUsar.getReserva().getFechaHoraInicio());
                    reservaInfo.put("fechaFin", qrUsar.getReserva().getFechaHoraFin());
                    result.put("reservaInfo", reservaInfo);
                }
                return ResponseEntity.ok(result);
            }
        } else if ("SALIDA".equals(tipo)) {
            if ("SALIDA_USADA".equals(qrUsar.getTipo()) || "FINALIZADO".equals(qrUsar.getTipo())) {
                result.put("valido", true);
                result.put("motivo", "QR ya validado para salida. La reserva está finalizada.");
                if (qrUsar.getReserva() != null) {
                    Map<String, Object> reservaInfo = new HashMap<>();
                    if (qrUsar.getReserva().getUsuario() != null) {
                        reservaInfo.put("usuario", qrUsar.getReserva().getUsuario().getNombreCompleto());
                    }
                    if (qrUsar.getReserva().getVehiculo() != null) {
                        reservaInfo.put("vehiculo", qrUsar.getReserva().getVehiculo().getPlaca());
                    }
                    if (qrUsar.getReserva().getEspacio() != null) {
                        reservaInfo.put("espacio", qrUsar.getReserva().getEspacio().getNumeroEspacio());
                    }
                    reservaInfo.put("fechaInicio", qrUsar.getReserva().getFechaHoraInicio());
                    reservaInfo.put("fechaFin", qrUsar.getReserva().getFechaHoraFin());
                    result.put("reservaInfo", reservaInfo);
                }
                return ResponseEntity.ok(result);
            }
        }
        result.put("valido", true);
        result.put("motivo", "QR válido");
        if (qrUsar.getReserva() != null) {
            Map<String, Object> reservaInfo = new HashMap<>();
            if (qrUsar.getReserva().getUsuario() != null) {
                reservaInfo.put("usuario", qrUsar.getReserva().getUsuario().getNombreCompleto());
            }
            if (qrUsar.getReserva().getVehiculo() != null) {
                reservaInfo.put("vehiculo", qrUsar.getReserva().getVehiculo().getPlaca());
            }
            if (qrUsar.getReserva().getEspacio() != null) {
                reservaInfo.put("espacio", qrUsar.getReserva().getEspacio().getNumeroEspacio());
            }
            reservaInfo.put("fechaInicio", qrUsar.getReserva().getFechaHoraInicio());
            reservaInfo.put("fechaFin", qrUsar.getReserva().getFechaHoraFin());
            result.put("reservaInfo", reservaInfo);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/registrar-entrada")
    @PreAuthorize("hasAuthority('VALIDACION_VALIDAR_QR')")
    public ResponseEntity<Map<String, Object>> registrarEntrada(@RequestParam String token) {
        Map<String, Object> result = new HashMap<>();
        EscaneoQR qr = escaneoQRRepository.findByTokenAndTipo(token, "ENTRADA").orElse(null);
        
        if (qr == null) {
            result.put("exito", false);
            result.put("motivo", "QR de entrada no encontrado");
            return ResponseEntity.ok(result);
        }
        
        if (qr.getReserva() == null) {
            result.put("exito", false);
            result.put("motivo", "Reserva no encontrada para este QR");
            return ResponseEntity.ok(result);
        }
        
        if (qr.getReserva().getEstado() != com.parqueo.parkingApp.model.Reserva.EstadoReserva.RESERVADO) {
            result.put("exito", false);
            result.put("motivo", "La reserva no está en estado RESERVADO");
            return ResponseEntity.ok(result);
        }
        
        try {
            escaneoQRService.registrarEntrada(qr.getReserva().getId());
            result.put("exito", true);
            result.put("motivo", "Entrada registrada exitosamente");
            
            // Incluir información de la reserva
            Map<String, Object> reservaInfo = new HashMap<>();
            if (qr.getReserva().getUsuario() != null) {
                reservaInfo.put("usuario", qr.getReserva().getUsuario().getNombreCompleto());
            }
            if (qr.getReserva().getVehiculo() != null) {
                reservaInfo.put("vehiculo", qr.getReserva().getVehiculo().getPlaca());
            }
            if (qr.getReserva().getEspacio() != null) {
                reservaInfo.put("espacio", qr.getReserva().getEspacio().getNumeroEspacio());
            }
            result.put("reservaInfo", reservaInfo);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("exito", false);
            result.put("motivo", "Error al registrar entrada: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    @PostMapping("/registrar-salida")
    @PreAuthorize("hasAuthority('VALIDACION_VALIDAR_QR')")
    public ResponseEntity<Map<String, Object>> registrarSalida(@RequestParam String token) {
        Map<String, Object> result = new HashMap<>();
        EscaneoQR qr = escaneoQRRepository.findByTokenAndTipo(token, "SALIDA").orElse(null);
        
        if (qr == null) {
            result.put("exito", false);
            result.put("motivo", "QR de salida no encontrado");
            return ResponseEntity.ok(result);
        }
        
        if (qr.getReserva() == null) {
            result.put("exito", false);
            result.put("motivo", "Reserva no encontrada para este QR");
            return ResponseEntity.ok(result);
        }
        
        if (qr.getReserva().getEstado() != com.parqueo.parkingApp.model.Reserva.EstadoReserva.ACTIVO) {
            result.put("exito", false);
            result.put("motivo", "La reserva no está en estado ACTIVO");
            return ResponseEntity.ok(result);
        }
        
        try {
            escaneoQRService.registrarSalida(qr.getReserva().getId());
            result.put("exito", true);
            result.put("motivo", "Salida registrada exitosamente");
            
            // Incluir información de la reserva
            Map<String, Object> reservaInfo = new HashMap<>();
            if (qr.getReserva().getUsuario() != null) {
                reservaInfo.put("usuario", qr.getReserva().getUsuario().getNombreCompleto());
            }
            if (qr.getReserva().getVehiculo() != null) {
                reservaInfo.put("vehiculo", qr.getReserva().getVehiculo().getPlaca());
            }
            if (qr.getReserva().getEspacio() != null) {
                reservaInfo.put("espacio", qr.getReserva().getEspacio().getNumeroEspacio());
            }
            result.put("reservaInfo", reservaInfo);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("exito", false);
            result.put("motivo", "Error al registrar salida: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
} 