package org.example.core;

import org.example.models.Usuario;
import org.example.models.Usuario.TipoUsuario;
import org.example.models.Usuario.EstadoUsuario;
import org.example.models.Usuario.Permiso;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class SeguridadManager {

    private Map<String, Usuario> usuarios;
    private Usuario usuarioActual;
    private List<String> logSistema;
    private Map<String, Integer> intentosFallidos;
    private Map<String, LocalDateTime> ultimosIntentos;
    private ConfiguracionSeguridad config;

    // Configuración de seguridad
    public static class ConfiguracionSeguridad {
        public int maxIntentosFallidos = 3;
        public int tiempoBloqueo = 15; // minutos
        public int tiempoSesion = 30; // minutos
        public boolean requerirContraseñaCompleja = false;
        public boolean auditarAcciones = true;
        public boolean permitirMultiplesSesiones = false;

        public ConfiguracionSeguridad() {}
    }

    // Constructor
    public SeguridadManager() {
        this.usuarios = new HashMap<>();
        this.usuarioActual = null;
        this.logSistema = new ArrayList<>();
        this.intentosFallidos = new HashMap<>();
        this.ultimosIntentos = new HashMap<>();
        this.config = new ConfiguracionSeguridad();

        inicializarSistema();
    }

    private void inicializarSistema() {
        // Crear usuario administrador por defecto
        Usuario admin = new Usuario("admin", "admin123", "Administrador", TipoUsuario.ADMINISTRADOR);
        admin.setEmail("admin@miniso.edu");
        usuarios.put("admin", admin);

        // Crear usuario invitado
        Usuario invitado = new Usuario("guest", "guest", "Invitado", TipoUsuario.INVITADO);
        usuarios.put("guest", invitado);

        // Crear usuario del sistema
        Usuario sistema = new Usuario("system", "sys_internal_2025", "Sistema", TipoUsuario.SISTEMA);
        usuarios.put("system", sistema);

        registrarLog("Sistema de seguridad inicializado");
    }

    // Métodos de autenticación
    public boolean iniciarSesion(String nombreUsuario, String contraseña) {
        registrarLog("Intento de inicio de sesión: " + nombreUsuario);

        // Verificar si el usuario está temporalmente bloqueado
        if (estaTemporalmenteBloqueado(nombreUsuario)) {
            registrarLog("Usuario temporalmente bloqueado: " + nombreUsuario);
            return false;
        }

        Usuario usuario = usuarios.get(nombreUsuario);

        if (usuario == null) {
            registrarIntentoFallido(nombreUsuario);
            registrarLog("Usuario no encontrado: " + nombreUsuario);
            return false;
        }

        if (!usuario.puedeIniciarSesion()) {
            registrarLog("Usuario no puede iniciar sesión - Estado: " + usuario.getEstado());
            return false;
        }

        if (!usuario.verificarContraseña(contraseña)) {
            registrarIntentoFallido(nombreUsuario);
            registrarLog("Contraseña incorrecta para usuario: " + nombreUsuario);
            return false;
        }

        // Autenticación exitosa
        if (!config.permitirMultiplesSesiones && usuarioActual != null) {
            cerrarSesion();
        }

        usuarioActual = usuario;
        usuario.iniciarSesion();
        limpiarIntentosFallidos(nombreUsuario);

        registrarLog("Inicio de sesión exitoso: " + nombreUsuario);
        return true;
    }

    public void cerrarSesion() {
        if (usuarioActual != null) {
            registrarLog("Cierre de sesión: " + usuarioActual.getNombreUsuario());
            usuarioActual.cerrarSesion();
            usuarioActual = null;
        }
    }

    public boolean haySesionActiva() {
        return usuarioActual != null;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    // Gestión de usuarios
    public boolean crearUsuario(String nombreUsuario, String contraseña, String nombre, TipoUsuario tipo) {
        verificarPermisoAdmin();

        if (usuarios.containsKey(nombreUsuario)) {
            registrarLog("Error: Usuario ya existe: " + nombreUsuario);
            return false;
        }

        try {
            Usuario nuevoUsuario = new Usuario(nombreUsuario, contraseña, nombre, tipo);
            usuarios.put(nombreUsuario, nuevoUsuario);
            registrarLog("Usuario creado: " + nombreUsuario + " (" + tipo + ")");
            return true;
        } catch (Exception e) {
            registrarLog("Error creando usuario " + nombreUsuario + ": " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarUsuario(String nombreUsuario) {
        verificarPermisoAdmin();

        if ("admin".equals(nombreUsuario)) {
            throw new SecurityException("No se puede eliminar el usuario administrador principal");
        }

        if (usuarioActual != null && usuarioActual.getNombreUsuario().equals(nombreUsuario)) {
            throw new SecurityException("No se puede eliminar el usuario actualmente conectado");
        }

        Usuario usuario = usuarios.remove(nombreUsuario);
        if (usuario != null) {
            registrarLog("Usuario eliminado: " + nombreUsuario);
            return true;
        }

        registrarLog("Error: Usuario no encontrado para eliminar: " + nombreUsuario);
        return false;
    }

    public boolean modificarUsuario(String nombreUsuario, String nuevoNombre, String nuevoEmail) {
        verificarPermisoModificarUsuarios();

        Usuario usuario = usuarios.get(nombreUsuario);
        if (usuario != null) {
            if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                usuario.setNombre(nuevoNombre);
            }
            if (nuevoEmail != null) {
                usuario.setEmail(nuevoEmail);
            }
            registrarLog("Usuario modificado: " + nombreUsuario);
            return true;
        }

        return false;
    }

    public boolean cambiarTipoUsuario(String nombreUsuario, TipoUsuario nuevoTipo) {
        verificarPermisoAdmin();

        if ("admin".equals(nombreUsuario) && nuevoTipo != TipoUsuario.ADMINISTRADOR) {
            throw new SecurityException("No se puede cambiar el tipo del usuario administrador principal");
        }

        Usuario usuario = usuarios.get(nombreUsuario);
        if (usuario != null) {
            TipoUsuario tipoAnterior = usuario.getTipo();
            usuario.setTipo(nuevoTipo);
            registrarLog("Tipo de usuario cambiado: " + nombreUsuario +
                    " de " + tipoAnterior + " a " + nuevoTipo);
            return true;
        }

        return false;
    }

    public boolean cambiarEstadoUsuario(String nombreUsuario, EstadoUsuario nuevoEstado) {
        verificarPermisoAdmin();

        if ("admin".equals(nombreUsuario) && nuevoEstado != EstadoUsuario.ACTIVO) {
            throw new SecurityException("No se puede desactivar el usuario administrador principal");
        }

        Usuario usuario = usuarios.get(nombreUsuario);
        if (usuario != null) {
            switch (nuevoEstado) {
                case ACTIVO:
                    usuario.activar();
                    break;
                case INACTIVO:
                    usuario.desactivar();
                    break;
                case BLOQUEADO:
                    usuario.bloquear("Bloqueado por administrador");
                    break;
                case SUSPENDIDO:
                    usuario.suspender("Suspendido por administrador");
                    break;
            }
            registrarLog("Estado de usuario cambiado: " + nombreUsuario + " a " + nuevoEstado);
            return true;
        }

        return false;
    }

    public boolean restablecerContraseña(String nombreUsuario, String nuevaContraseña) {
        verificarPermisoAdmin();

        Usuario usuario = usuarios.get(nombreUsuario);
        if (usuario != null) {
            try {
                usuario.cambiarContraseñaAdmin(nuevaContraseña);
                registrarLog("Contraseña restablecida para usuario: " + nombreUsuario);
                return true;
            } catch (Exception e) {
                registrarLog("Error restableciendo contraseña para " + nombreUsuario + ": " + e.getMessage());
                return false;
            }
        }

        return false;
    }

    // Gestión de permisos
    public boolean verificarPermiso(Permiso permiso) {
        if (usuarioActual == null) {
            return false;
        }

        boolean tienePermiso = usuarioActual.tienePermiso(permiso);
        if (!tienePermiso) {
            registrarLog("Acceso denegado - Permiso requerido: " + permiso.getDescripcion() +
                    " Usuario: " + usuarioActual.getNombreUsuario());
        }

        return tienePermiso;
    }

    public void verificarPermisoOExcepcion(Permiso permiso) throws SecurityException {
        if (usuarioActual == null) {
            throw new SecurityException("No hay usuario autenticado");
        }

        usuarioActual.verificarPermiso(permiso);
    }

    public boolean asignarPermiso(String nombreUsuario, Permiso permiso) {
        verificarPermisoAdmin();

        Usuario usuario = usuarios.get(nombreUsuario);
        if (usuario != null) {
            usuario.agregarPermiso(permiso);
            registrarLog("Permiso asignado: " + permiso.getDescripcion() + " a " + nombreUsuario);
            return true;
        }

        return false;
    }

    public boolean removerPermiso(String nombreUsuario, Permiso permiso) {
        verificarPermisoAdmin();

        Usuario usuario = usuarios.get(nombreUsuario);
        if (usuario != null) {
            usuario.removerPermiso(permiso);
            registrarLog("Permiso removido: " + permiso.getDescripcion() + " de " + nombreUsuario);
            return true;
        }

        return false;
    }

    // Métodos de verificación internos
    private void verificarPermisoAdmin() {
        if (usuarioActual == null || !usuarioActual.esAdministrador()) {
            throw new SecurityException("Se requieren privilegios de administrador");
        }
    }

    private void verificarPermisoModificarUsuarios() {
        verificarPermisoOExcepcion(Permiso.MODIFICAR_USUARIOS);
    }

    // Gestión de intentos fallidos
    private void registrarIntentoFallido(String nombreUsuario) {
        intentosFallidos.put(nombreUsuario,
                intentosFallidos.getOrDefault(nombreUsuario, 0) + 1);
        ultimosIntentos.put(nombreUsuario, LocalDateTime.now());

        int intentos = intentosFallidos.get(nombreUsuario);
        registrarLog("Intento fallido #" + intentos + " para usuario: " + nombreUsuario);

        if (intentos >= config.maxIntentosFallidos) {
            registrarLog("Usuario temporalmente bloqueado por intentos fallidos: " + nombreUsuario);
        }
    }

    private void limpiarIntentosFallidos(String nombreUsuario) {
        intentosFallidos.remove(nombreUsuario);
        ultimosIntentos.remove(nombreUsuario);
    }

    private boolean estaTemporalmenteBloqueado(String nombreUsuario) {
        if (!intentosFallidos.containsKey(nombreUsuario)) {
            return false;
        }

        int intentos = intentosFallidos.get(nombreUsuario);
        if (intentos < config.maxIntentosFallidos) {
            return false;
        }

        LocalDateTime ultimoIntento = ultimosIntentos.get(nombreUsuario);
        if (ultimoIntento == null) {
            return false;
        }

        long minutosTranscurridos = ChronoUnit.MINUTES.between(ultimoIntento, LocalDateTime.now());
        if (minutosTranscurridos >= config.tiempoBloqueo) {
            // El bloqueo ha expirado
            limpiarIntentosFallidos(nombreUsuario);
            return false;
        }

        return true;
    }

    // Auditoría y logs
    private void registrarLog(String mensaje) {
        if (config.auditarAcciones) {
            String entrada = LocalDateTime.now() + " - " + mensaje;
            logSistema.add(entrada);

            // Mantener solo los últimos 1000 registros
            if (logSistema.size() > 1000) {
                logSistema.remove(0);
            }
        }
    }

    public List<String> getLogSistema() {
        verificarPermisoOExcepcion(Permiso.VER_LOGS_SISTEMA);
        return new ArrayList<>(logSistema);
    }

    public List<String> getLogReciente(int cantidad) {
        verificarPermisoOExcepcion(Permiso.VER_LOGS_SISTEMA);

        int size = logSistema.size();
        int fromIndex = Math.max(0, size - cantidad);
        return new ArrayList<>(logSistema.subList(fromIndex, size));
    }

    // Información del sistema
    public List<Usuario> getUsuarios() {
        verificarPermisoOExcepcion(Permiso.VER_LOGS_SISTEMA);
        return new ArrayList<>(usuarios.values());
    }

    public Usuario getUsuario(String nombreUsuario) {
        verificarPermisoModificarUsuarios();
        return usuarios.get(nombreUsuario);
    }

    public String getEstadisticasSeguridad() {
        verificarPermisoAdmin();

        int totalUsuarios = usuarios.size();
        long usuariosActivos = usuarios.values().stream()
                .filter(u -> u.getEstado() == EstadoUsuario.ACTIVO)
                .count();
        long administradores = usuarios.values().stream()
                .filter(Usuario::esAdministrador)
                .count();

        return String.format(
                "Usuarios: %d total, %d activos | Admins: %d | Logs: %d entradas | Bloqueos temporales: %d",
                totalUsuarios, usuariosActivos, administradores,
                logSistema.size(), intentosFallidos.size()
        );
    }

    public Map<String, Object> getReporteSeguridad() {
        verificarPermisoAdmin();

        Map<String, Object> reporte = new HashMap<>();

        // Estadísticas de usuarios
        Map<String, Object> estadisticasUsuarios = new HashMap<>();
        estadisticasUsuarios.put("total", usuarios.size());
        estadisticasUsuarios.put("activos", usuarios.values().stream()
                .filter(u -> u.getEstado() == EstadoUsuario.ACTIVO).count());
        estadisticasUsuarios.put("administradores", usuarios.values().stream()
                .filter(Usuario::esAdministrador).count());
        estadisticasUsuarios.put("bloqueados", usuarios.values().stream()
                .filter(u -> u.getEstado() == EstadoUsuario.BLOQUEADO).count());

        reporte.put("usuarios", estadisticasUsuarios);

        // Estadísticas de seguridad
        Map<String, Object> estadisticasSeguridad = new HashMap<>();
        estadisticasSeguridad.put("intentos_fallidos_activos", intentosFallidos.size());
        estadisticasSeguridad.put("logs_sistema", logSistema.size());
        estadisticasSeguridad.put("sesion_activa", usuarioActual != null);
        estadisticasSeguridad.put("usuario_actual",
                usuarioActual != null ? usuarioActual.getNombreUsuario() : "Ninguno");

        reporte.put("seguridad", estadisticasSeguridad);

        // Configuración actual
        Map<String, Object> configuracion = new HashMap<>();
        configuracion.put("max_intentos_fallidos", config.maxIntentosFallidos);
        configuracion.put("tiempo_bloqueo_minutos", config.tiempoBloqueo);
        configuracion.put("tiempo_sesion_minutos", config.tiempoSesion);
        configuracion.put("auditar_acciones", config.auditarAcciones);

        reporte.put("configuracion", configuracion);

        return reporte;
    }

    // Configuración
    public ConfiguracionSeguridad getConfiguracion() {
        verificarPermisoAdmin();
        return config;
    }

    public void actualizarConfiguracion(ConfiguracionSeguridad nuevaConfig) {
        verificarPermisoAdmin();
        this.config = nuevaConfig;
        registrarLog("Configuración de seguridad actualizada");
    }

    // Utilidades
    public boolean existeUsuario(String nombreUsuario) {
        return usuarios.containsKey(nombreUsuario);
    }

    public void limpiarLogsSistema() {
        verificarPermisoAdmin();
        int cantidadAnterior = logSistema.size();
        logSistema.clear();
        registrarLog("Logs del sistema limpiados (" + cantidadAnterior + " entradas eliminadas)");
    }

    public void exportarConfiguracion() {
        verificarPermisoAdmin();
        // En una implementación real, esto exportaría la configuración a un archivo
        registrarLog("Configuración exportada");
    }

    public void importarConfiguracion() {
        verificarPermisoAdmin();
        // En una implementación real, esto importaría la configuración desde un archivo
        registrarLog("Configuración importada");
    }
}