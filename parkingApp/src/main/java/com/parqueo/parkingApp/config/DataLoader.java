package com.parqueo.parkingApp.config;

import com.parqueo.parkingApp.model.Permiso;
import com.parqueo.parkingApp.model.Rol;
import com.parqueo.parkingApp.model.Usuario;
import com.parqueo.parkingApp.repository.PermisoRepository;
import com.parqueo.parkingApp.repository.RolRepository;
import com.parqueo.parkingApp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (rolRepository.count() > 0 || usuarioRepository.count() > 0 || permisoRepository.count() > 0) {
            System.out.println("La base de datos ya contiene datos de seguridad, no se cargaron datos iniciales.");
            return;
        }

        System.out.println("Cargando datos iniciales (permisos, roles y usuarios)...");

        // --- 1. Crear todos los Permisos ---
        Set<Permiso> todosLosPermisos = Arrays.stream(new String[]{
            // Usuarios
            "USUARIO_LEER", "USUARIO_CREAR", "USUARIO_EDITAR", "USUARIO_ELIMINAR", 
            "USUARIO_DESACTIVAR", "USUARIO_ACTIVAR", "USUARIO_ELIMINAR_COMPLETO",
            "USUARIO_BUSCAR_POR_USERNAME", "USUARIO_BUSCAR_POR_EMAIL", "USUARIO_BUSCAR_POR_ROL",
            
            // Reservas
            "RESERVA_LEER", "RESERVA_CREAR", "RESERVA_EDITAR", "RESERVA_ELIMINAR",
            "RESERVA_BUSCAR_POR_USUARIO", "RESERVA_BUSCAR_POR_VEHICULO", "RESERVA_BUSCAR_POR_ESPACIO",
            "RESERVA_BUSCAR_POR_ESTADO", "RESERVA_BUSCAR_POR_FECHA", "RESERVA_LIBERAR_EXPIRADOS",
            "RESERVA_VER_COMPLETA", "RESERVA_LISTAR_ACTIVAS",
            
            // Vehículos
            "VEHICULO_LEER", "VEHICULO_CREAR", "VEHICULO_EDITAR", "VEHICULO_ELIMINAR",
            "VEHICULO_BUSCAR_POR_PLACA", "VEHICULO_BUSCAR_POR_USUARIO", "VEHICULO_BUSCAR_POR_TIPO",
            
            // Sanciones
            "SANCION_LEER", "SANCION_CREAR", "SANCION_EDITAR", "SANCION_ELIMINAR",
            "SANCION_BUSCAR_POR_USUARIO", "SANCION_BUSCAR_POR_VEHICULO", "SANCION_BUSCAR_POR_ESTADO",
            "SANCION_BUSCAR_POR_FECHA", "SANCION_LISTAR_ACTIVAS", "SANCION_LISTAR_ESTADOS",
            
            // Escaneo QR
            "ESCANEOQR_LEER", "ESCANEOQR_CREAR", "ESCANEOQR_EDITAR", "ESCANEOQR_ELIMINAR",
            "ESCANEOQR_REGISTRAR_ENTRADA", "ESCANEOQR_REGISTRAR_SALIDA", "ESCANEOQR_BUSCAR_POR_RESERVA",
            "ESCANEOQR_BUSCAR_POR_USUARIO", "ESCANEOQR_BUSCAR_POR_FECHA",
            
            // Permisos (Gestión de permisos)
            "PERMISO_LEER", "PERMISO_CREAR", "PERMISO_EDITAR", "PERMISO_ELIMINAR",
            "PERMISO_BUSCAR_POR_NOMBRE",
            
            // Validaciones
            "VALIDACION_LEER_USUARIO", "VALIDACION_VALIDAR_QR",
            
            // Historial de Uso
            "HISTORIAL_LEER", "HISTORIAL_CREAR", "HISTORIAL_EDITAR", "HISTORIAL_ELIMINAR",
            "HISTORIAL_BUSCAR_POR_USUARIO", "HISTORIAL_BUSCAR_POR_ESPACIO", "HISTORIAL_BUSCAR_POR_FECHA",
            "HISTORIAL_LISTAR_ACTIVOS",
            
            // Espacios
            "ESPACIO_LEER", "ESPACIO_CREAR", "ESPACIO_EDITAR", "ESPACIO_ELIMINAR",
            "ESPACIO_BUSCAR_POR_ESTADO", "ESPACIO_BUSCAR_POR_ZONA", "ESPACIO_LISTAR_DISPONIBLES",
            
            // Detalles de Sanciones
            "SANCION_DETALLE_LEER", "SANCION_DETALLE_CREAR", "SANCION_DETALLE_EDITAR", "SANCION_DETALLE_ELIMINAR",
            "SANCION_DETALLE_BUSCAR_POR_SANCION", "SANCION_DETALLE_LISTAR_NIVELES",
            
            // Reglas de Estacionamiento
            "REGLA_LEER", "REGLA_CREAR", "REGLA_EDITAR", "REGLA_ELIMINAR",
            "REGLA_BUSCAR_POR_TIPO", "REGLA_LISTAR_ACTIVAS", "REGLA_LISTAR_TIPOS_SANCION"
        }).map(nombre -> {
            Permiso p = new Permiso();
            p.setNombre(nombre);
            p.setDescripcion("Permiso para " + nombre.toLowerCase().replace("_", " "));
            return p;
        }).collect(Collectors.toSet());
        
        permisoRepository.saveAll(todosLosPermisos);

        // --- 2. Crear Roles y asignar permisos ---
        
        // ADMINISTRADOR - Todos los permisos
        Rol administrador = new Rol("ADMINISTRADOR", "Rol con acceso completo al sistema");
        administrador.setPermisos(todosLosPermisos);
        rolRepository.save(administrador);

        // ESTUDIANTE - Permisos limitados
        Rol estudiante = new Rol("ESTUDIANTE", "Rol para estudiantes");
        Set<Permiso> permisosEstudiante = todosLosPermisos.stream()
            .filter(p -> p.getNombre().startsWith("RESERVA_") || 
                        p.getNombre().startsWith("VEHICULO_") ||
                        p.getNombre().equals("USUARIO_LEER") ||
                        p.getNombre().equals("ESCANEOQR_REGISTRAR_ENTRADA") ||
                        p.getNombre().equals("ESCANEOQR_REGISTRAR_SALIDA") ||
                        p.getNombre().equals("HISTORIAL_LEER") ||
                        p.getNombre().equals("HISTORIAL_BUSCAR_POR_USUARIO") ||
                        p.getNombre().equals("ESPACIO_LEER") ||
                        p.getNombre().equals("ESPACIO_LISTAR_DISPONIBLES") ||
                        p.getNombre().equals("VALIDACION_LEER_USUARIO") ||
                        p.getNombre().equals("VALIDACION_VALIDAR_QR") ||
                        p.getNombre().equals("REGLA_LEER"))
            .collect(Collectors.toSet());
        estudiante.setPermisos(permisosEstudiante);
        rolRepository.save(estudiante);

        // DOCENTE - Permisos similares a estudiante
        Rol docente = new Rol("DOCENTE", "Rol para docentes");
        docente.setPermisos(permisosEstudiante);
        rolRepository.save(docente);

        // VIGILANTE - Permisos de monitoreo y control
        Rol vigilante = new Rol("VIGILANTE", "Rol para vigilantes");
        Set<Permiso> permisosVigilante = todosLosPermisos.stream()
            .filter(p -> p.getNombre().startsWith("ESCANEOQR_") ||
                        p.getNombre().startsWith("SANCION_") ||
                        p.getNombre().startsWith("SANCION_DETALLE_") ||
                        p.getNombre().equals("RESERVA_LEER") ||
                        p.getNombre().equals("VEHICULO_LEER") ||
                        p.getNombre().equals("USUARIO_LEER") ||
                        p.getNombre().equals("HISTORIAL_LEER") ||
                        p.getNombre().equals("ESPACIO_LEER") ||
                        p.getNombre().equals("VALIDACION_VALIDAR_QR") ||
                        p.getNombre().equals("REGLA_LEER"))
            .collect(Collectors.toSet());
        vigilante.setPermisos(permisosVigilante);
        rolRepository.save(vigilante);

        // PROVEEDOR_SERVICIO - Permisos limitados
        Rol proveedorServicio = new Rol("PROVEEDOR_SERVICIO", "Rol para proveedores de servicio");
        Set<Permiso> permisosProveedor = todosLosPermisos.stream()
            .filter(p -> p.getNombre().startsWith("RESERVA_") ||
                        p.getNombre().equals("VEHICULO_LEER") ||
                        p.getNombre().equals("USUARIO_LEER") ||
                        p.getNombre().equals("HISTORIAL_LEER") ||
                        p.getNombre().equals("ESPACIO_LEER") ||
                        p.getNombre().equals("ESPACIO_LISTAR_DISPONIBLES"))
            .collect(Collectors.toSet());
        proveedorServicio.setPermisos(permisosProveedor);
        rolRepository.save(proveedorServicio);

        // --- 3. Crear Usuarios de ejemplo ---
        
        // Usuario Administrador
        Usuario admin = new Usuario();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@parqueo.com");
        admin.setNombreCompleto("Administrador del Sistema");
        admin.setRol(administrador);
        admin.setActivo(true);
        usuarioRepository.save(admin);

        // Usuario Estudiante
        Usuario estudiante1 = new Usuario();
        estudiante1.setUsername("estudiante1");
        estudiante1.setPassword(passwordEncoder.encode("estudiante123"));
        estudiante1.setEmail("estudiante1@universidad.com");
        estudiante1.setNombreCompleto("Juan Pérez Estudiante");
        estudiante1.setRol(estudiante);
        estudiante1.setActivo(true);
        usuarioRepository.save(estudiante1);

        // Usuario Docente
        Usuario docente1 = new Usuario();
        docente1.setUsername("docente1");
        docente1.setPassword(passwordEncoder.encode("docente123"));
        docente1.setEmail("docente1@universidad.com");
        docente1.setNombreCompleto("María García Docente");
        docente1.setRol(docente);
        docente1.setActivo(true);
        usuarioRepository.save(docente1);

        // Usuario Vigilante
        Usuario vigilante1 = new Usuario();
        vigilante1.setUsername("vigilante1");
        vigilante1.setPassword(passwordEncoder.encode("vigilante123"));
        vigilante1.setEmail("vigilante1@parqueo.com");
        vigilante1.setNombreCompleto("Carlos López Vigilante");
        vigilante1.setRol(vigilante);
        vigilante1.setActivo(true);
        usuarioRepository.save(vigilante1);

        // Usuario Proveedor de Servicio
        Usuario proveedor1 = new Usuario();
        proveedor1.setUsername("proveedor1");
        proveedor1.setPassword(passwordEncoder.encode("proveedor123"));
        proveedor1.setEmail("proveedor1@empresa.com");
        proveedor1.setNombreCompleto("Ana Rodríguez Proveedora");
        proveedor1.setRol(proveedorServicio);
        proveedor1.setActivo(true);
        usuarioRepository.save(proveedor1);

        System.out.println("Datos iniciales cargados exitosamente:");
        System.out.println("- " + todosLosPermisos.size() + " permisos creados");
        System.out.println("- 5 roles creados con sus permisos asignados");
        System.out.println("- 5 usuarios de ejemplo creados");
    }
} 