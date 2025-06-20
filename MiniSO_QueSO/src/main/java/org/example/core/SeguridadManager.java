package org.example.core;

import org.example.models.Usuario;
import org.example.models.Usuario.TipoUsuario;
import org.example.models.Usuario.EstadoUsuario;
import org.example.models.Usuario.Permiso;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Gestor de seguridad y autenticación para el sistema operativo académico MiniSO.
 *
 * Esta clase implementa un sistema completo de seguridad que incluye:
 * - Autenticación de usuarios con contraseñas
 * - Gestión de sesiones y permisos
 * - Protección contra ataques de fuerza bruta
 * - Auditoría completa de acciones del sistema
 * - Administración de usuarios y roles
 *
 * El sistema sigue principios de seguridad como:
 * - Principio de menor privilegio
 * - Defensa en profundidad
 * - Auditoría completa de acciones
 * - Protección contra ataques comunes
 */
public class SeguridadManager {
    /** Mapa de usuarios registrados en el sistema (clave: nombre de usuario) */
    private Map<String, Usuario> usuarios;

    /** Usuario que tiene la sesión activa actualmente (null si no hay sesión) */
    private Usuario usuarioActual;

    /** Registro de auditoría con todas las acciones del sistema */
    private List<String> logSistema;

    /** Contador de intentos fallidos de autenticación por usuario */
    private Map<String, Integer> intentosFallidos;

    /** Timestamp del último intento fallido por usuario */
    private Map<String, LocalDateTime> ultimosIntentos;

    /** Configuración de políticas de seguridad del sistema */
    private ConfiguracionSeguridad config;

    /**
     * Configuración centralizada de políticas de seguridad del sistema.
     *
     * Permite ajustar parámetros de seguridad sin modificar el código,
     * siguiendo el principio de configuración externa.
     */
    public static class ConfiguracionSeguridad {
        /** Número máximo de intentos fallidos antes del bloqueo temporal */
        public int maxIntentosFallidos = 3;

        /** Tiempo de bloqueo temporal en minutos tras exceder intentos fallidos */
        public int tiempoBloqueo = 15;

        /** Duración máxima de una sesión en minutos */
        public int tiempoSesion = 30;

        /** Indica si se requieren contraseñas complejas (mayúsc, núm, símbolos) */
        public boolean requerirContraseñaCompleja = false;

        /** Habilita/deshabilita el sistema de auditoría completo */
        public boolean auditarAcciones = true;

        /** Permite múltiples sesiones simultáneas del mismo usuario */
        public boolean permitirMultiplesSesiones = false;

        /**
         * Constructor por defecto con valores seguros.
         */
        public ConfiguracionSeguridad() {}
    }

    /**
     * Constructor principal del gestor de seguridad.
     *
     * Inicializa todas las estructuras de datos necesarias y crea
     * los usuarios por defecto del sistema con configuración segura.
     */
    public SeguridadManager() {
        // Inicialización de estructuras de datos
        this.usuarios = new HashMap<>();
        this.usuarioActual = null; // No hay sesión activa inicialmente
        this.logSistema = new ArrayList<>();
        this.intentosFallidos = new HashMap<>();
        this.ultimosIntentos = new HashMap<>();
        this.config = new ConfiguracionSeguridad();

        // Configuración inicial del sistema
        inicializarSistema();
    }

    /**
     * Inicializa el sistema con usuarios por defecto y configuración básica.
     *
     * Crea tres tipos de usuarios fundamentales:
     * 1. Administrador: Control total del sistema
     * 2. Invitado: Acceso limitado de solo lectura
     * 3. Sistema: Usuario interno para procesos del SO
     */
    private void inicializarSistema() {
        // Crear usuario administrador con privilegios completos
        Usuario admin = new Usuario("admin", "admin123", "Administrador", TipoUsuario.ADMINISTRADOR);
        admin.setEmail("admin@miniso.edu");
        usuarios.put("admin", admin);

        // Crear usuario invitado con acceso limitado
        Usuario invitado = new Usuario("guest", "guest", "Invitado", TipoUsuario.INVITADO);
        usuarios.put("guest", invitado);

        // Crear usuario del sistema para procesos internos
        Usuario sistema = new Usuario("system", "system", "Sistema", TipoUsuario.SISTEMA);
        usuarios.put("system", sistema);

        // Registrar la inicialización en el log de auditoría
        registrarLog("Sistema de seguridad inicializado");
    }

    /**
     * Autentica un usuario en el sistema verificando credenciales y políticas de seguridad.
     *
     * Proceso de autenticación:
     * 1. Verificar bloqueo temporal por intentos fallidos
     * 2. Validar existencia del usuario
     * 3. Verificar estado del usuario (activo/bloqueado)
     * 4. Validar contraseña
     * 5. Gestionar sesiones múltiples según configuración
     * 6. Establecer sesión activa y limpiar intentos fallidos
     *
     * @param nombreUsuario Nombre de usuario para autenticar
     * @param contraseña Contraseña en texto plano
     * @return true si la autenticación fue exitosa, false en caso contrario
     */
    public boolean iniciarSesion(String nombreUsuario, String contraseña) {
        // Registrar el intento de autenticación para auditoría
        registrarLog("Intento de inicio de sesión: " + nombreUsuario);

        // VERIFICACIÓN 1: Protección contra fuerza bruta
        if (estaTemporalmenteBloqueado(nombreUsuario)) {
            registrarLog("Usuario temporalmente bloqueado: " + nombreUsuario);
            return false;
        }

        // VERIFICACIÓN 2: Existencia del usuario
        Usuario usuario = usuarios.get(nombreUsuario);
        if (usuario == null) {
            registrarIntentoFallido(nombreUsuario);
            registrarLog("Usuario no encontrado: " + nombreUsuario);
            return false;
        }

        // VERIFICACIÓN 3: Estado del usuario
        if (!usuario.puedeIniciarSesion()) {
            registrarLog("Usuario no puede iniciar sesión - Estado: " + usuario.getEstado());
            return false;
        }

        // VERIFICACIÓN 4: Validación de contraseña
        if (!usuario.verificarContraseña(contraseña)) {
            registrarIntentoFallido(nombreUsuario);
            registrarLog("Contraseña incorrecta para usuario: " + nombreUsuario);
            return false;
        }

        // AUTENTICACIÓN EXITOSA: Gestión de sesión

        // Verificar política de sesiones múltiples
        if (!config.permitirMultiplesSesiones && usuarioActual != null) {
            cerrarSesion(); // Cerrar sesión anterior si no se permiten múltiples
        }

        // Establecer nueva sesión activa
        usuarioActual = usuario;
        usuario.iniciarSesion();

        // Limpiar registro de intentos fallidos tras autenticación exitosa
        limpiarIntentosFallidos(nombreUsuario);

        registrarLog("Inicio de sesión exitoso: " + nombreUsuario);
        return true;
    }

    /**
     * Cierra la sesión activa del usuario actual.
     *
     * Registra la acción en el log de auditoría y limpia el estado de sesión.
     */
    public void cerrarSesion() {
        if (usuarioActual != null) {
            registrarLog("Cierre de sesión: " + usuarioActual.getNombreUsuario());
            usuarioActual.cerrarSesion();
            usuarioActual = null; // Limpiar sesión activa
        }
    }

    /**
     * Verifica si hay una sesión de usuario activa.
     *
     * @return true si hay un usuario autenticado
     */
    public boolean haySesionActiva() {
        return usuarioActual != null;
    }

    /**
     * Obtiene el usuario que tiene la sesión activa.
     *
     * @return Usuario actual o null si no hay sesión activa
     */
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Crea un nuevo usuario en el sistema.
     *
     * Requiere privilegios de administrador y valida que no exista
     * un usuario con el mismo nombre.
     *
     * @param nombreUsuario Nombre único del usuario
     * @param contraseña Contraseña en texto plano
     * @param nombre Nombre completo del usuario
     * @param tipo Tipo de usuario (ADMINISTRADOR, USUARIO_ESTANDAR, etc.)
     * @return true si el usuario fue creado exitosamente
     */
    public boolean crearUsuario(String nombreUsuario, String contraseña, String nombre, TipoUsuario tipo) {
        // Verificar que el usuario actual tiene permisos de administrador
        verificarPermisoAdmin();

        // Verificar que el nombre de usuario no esté en uso
        if (usuarios.containsKey(nombreUsuario)) {
            registrarLog("Error: Usuario ya existe: " + nombreUsuario);
            return false;
        }

        try {
            // Crear nuevo usuario con validaciones internas
            Usuario nuevoUsuario = new Usuario(nombreUsuario, contraseña, nombre, tipo);
            usuarios.put(nombreUsuario, nuevoUsuario);
            registrarLog("Usuario creado: " + nombreUsuario + " (" + tipo + ")");
            return true;
        } catch (Exception e) {
            // Capturar errores de validación (contraseña débil, etc.)
            registrarLog("Error creando usuario " + nombreUsuario + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un usuario del sistema con protecciones de seguridad.
     *
     * Protecciones implementadas:
     * - No se puede eliminar el administrador principal
     * - No se puede eliminar el usuario con sesión activa
     *
     * @param nombreUsuario Nombre del usuario a eliminar
     * @return true si el usuario fue eliminado exitosamente
     * @throws SecurityException si se intenta eliminar un usuario protegido
     */
    public boolean eliminarUsuario(String nombreUsuario) {
        verificarPermisoAdmin();

        // Protección: No eliminar administrador principal
        if ("admin".equals(nombreUsuario)) {
            throw new SecurityException("No se puede eliminar el usuario administrador principal");
        }

        // Protección: No eliminar usuario con sesión activa
        if (usuarioActual != null && usuarioActual.getNombreUsuario().equals(nombreUsuario)) {
            throw new SecurityException("No se puede eliminar el usuario actualmente conectado");
        }

        // Intentar eliminar el usuario
        Usuario usuario = usuarios.remove(nombreUsuario);
        if (usuario != null) {
            registrarLog("Usuario eliminado: " + nombreUsuario);
            return true;
        }

        registrarLog("Error: Usuario no encontrado para eliminar: " + nombreUsuario);
        return false;
    }

    /**
     * Modifica la información básica de un usuario.
     *
     * @param nombreUsuario Usuario a modificar
     * @param nuevoNombre Nuevo nombre completo (null para mantener actual)
     * @param nuevoEmail Nuevo email (null para mantener actual)
     * @return true si la modificación fue exitosa
     */
    public boolean modificarUsuario(String nombreUsuario, String nuevoNombre, String nuevoEmail) {
        verificarPermisoModificarUsuarios();

        Usuario usuario = usuarios.get(nombreUsuario);
        if (usuario != null) {
            // Actualizar nombre si se proporciona uno válido
            if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                usuario.setNombre(nuevoNombre);
            }

            // Actualizar email si se proporciona
            if (nuevoEmail != null) {
                usuario.setEmail(nuevoEmail);
            }

            registrarLog("Usuario modificado: " + nombreUsuario);
            return true;
        }

        return false;
    }

    /**
     * Cambia el tipo/rol de un usuario en el sistema.
     *
     * @param nombreUsuario Usuario a modificar
     * @param nuevoTipo Nuevo tipo de usuario
     * @return true si el cambio fue exitoso
     * @throws SecurityException si se intenta degradar al administrador principal
     */
    public boolean cambiarTipoUsuario(String nombreUsuario, TipoUsuario nuevoTipo) {
        verificarPermisoAdmin();

        // Protección: No cambiar tipo del administrador principal
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

    /**
     * Cambia el estado de un usuario (activo, inactivo, bloqueado, suspendido).
     *
     * @param nombreUsuario Usuario a modificar
     * @param nuevoEstado Nuevo estado del usuario
     * @return true si el cambio fue exitoso
     * @throws SecurityException si se intenta desactivar al administrador principal
     */
    public boolean cambiarEstadoUsuario(String nombreUsuario, EstadoUsuario nuevoEstado) {
        verificarPermisoAdmin();

        // Protección: No desactivar administrador principal
        if ("admin".equals(nombreUsuario) && nuevoEstado != EstadoUsuario.ACTIVO) {
            throw new SecurityException("No se puede desactivar el usuario administrador principal");
        }

        Usuario usuario = usuarios.get(nombreUsuario);
        if (usuario != null) {
            // Aplicar el cambio de estado según el tipo
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

    /**
     * Restablece la contraseña de un usuario (función administrativa).
     *
     * @param nombreUsuario Usuario cuya contraseña se va a restablecer
     * @param nuevaContraseña Nueva contraseña en texto plano
     * @return true si el restablecimiento fue exitoso
     */
    public boolean restablecerContraseña(String nombreUsuario, String nuevaContraseña) {
        verificarPermisoAdmin();

        Usuario usuario = usuarios.get(nombreUsuario);
        if (usuario != null) {
            try {
                // Usar método administrativo que omite verificación de contraseña actual
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

    /**
     * Verifica si el usuario actual tiene un permiso específico.
     *
     * @param permiso Permiso a verificar
     * @return true si el usuario tiene el permiso
     */
    public boolean verificarPermiso(Permiso permiso) {
        if (usuarioActual == null) {
            return false;
        }

        boolean tienePermiso = usuarioActual.tienePermiso(permiso);
        if (!tienePermiso) {
            // Registrar intento de acceso denegado para auditoría
            registrarLog("Acceso denegado - Permiso requerido: " + permiso.getDescripcion() +
                    " Usuario: " + usuarioActual.getNombreUsuario());
        }

        return tienePermiso;
    }

    /**
     * Verifica un permiso y lanza excepción si no se tiene.
     *
     * @param permiso Permiso requerido
     * @throws SecurityException si el usuario no tiene el permiso
     */
    public void verificarPermisoOExcepcion(Permiso permiso) throws SecurityException {
        if (usuarioActual == null) {
            throw new SecurityException("No hay usuario autenticado");
        }

        usuarioActual.verificarPermiso(permiso);
    }

    /**
     * Asigna un permiso específico a un usuario.
     *
     * @param nombreUsuario Usuario al que se asignará el permiso
     * @param permiso Permiso a asignar
     * @return true si el permiso fue asignado exitosamente
     */
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

    /**
     * Remueve un permiso específico de un usuario.
     *
     * @param nombreUsuario Usuario al que se removerá el permiso
     * @param permiso Permiso a remover
     * @return true si el permiso fue removido exitosamente
     */
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

    /**
     * Verifica que el usuario actual sea administrador.
     *
     * @throws SecurityException si no hay usuario autenticado o no es administrador
     */
    private void verificarPermisoAdmin() {
        if (usuarioActual == null || !usuarioActual.esAdministrador()) {
            throw new SecurityException("Se requieren privilegios de administrador");
        }
    }

    /**
     * Verifica permisos para modificar usuarios.
     *
     * @throws SecurityException si no se tienen los permisos necesarios
     */
    private void verificarPermisoModificarUsuarios() {
        verificarPermisoOExcepcion(Permiso.MODIFICAR_USUARIOS);
    }


    /**
     * Registra un intento fallido de autenticación y aplica políticas de seguridad.
     *
     * @param nombreUsuario Usuario que tuvo el intento fallido
     */
    private void registrarIntentoFallido(String nombreUsuario) {
        // Incrementar contador de intentos fallidos
        intentosFallidos.put(nombreUsuario,
                intentosFallidos.getOrDefault(nombreUsuario, 0) + 1);

        // Registrar timestamp del intento
        ultimosIntentos.put(nombreUsuario, LocalDateTime.now());

        int intentos = intentosFallidos.get(nombreUsuario);
        registrarLog("Intento fallido #" + intentos + " para usuario: " + nombreUsuario);

        // Verificar si se alcanzó el límite de intentos
        if (intentos >= config.maxIntentosFallidos) {
            registrarLog("Usuario temporalmente bloqueado por intentos fallidos: " + nombreUsuario);
        }
    }

    /**
     * Limpia el registro de intentos fallidos tras autenticación exitosa.
     *
     * @param nombreUsuario Usuario cuyo registro se va a limpiar
     */
    private void limpiarIntentosFallidos(String nombreUsuario) {
        intentosFallidos.remove(nombreUsuario);
        ultimosIntentos.remove(nombreUsuario);
    }

    /**
     * Verifica si un usuario está temporalmente bloqueado por intentos fallidos.
     *
     * @param nombreUsuario Usuario a verificar
     * @return true si el usuario está bloqueado temporalmente
     */
    private boolean estaTemporalmenteBloqueado(String nombreUsuario) {
        // Verificar si hay registro de intentos fallidos
        if (!intentosFallidos.containsKey(nombreUsuario)) {
            return false;
        }

        int intentos = intentosFallidos.get(nombreUsuario);
        // Verificar si se alcanzó el límite
        if (intentos < config.maxIntentosFallidos) {
            return false;
        }

        LocalDateTime ultimoIntento = ultimosIntentos.get(nombreUsuario);
        if (ultimoIntento == null) {
            return false;
        }

        // Calcular tiempo transcurrido desde el último intento
        long minutosTranscurridos = ChronoUnit.MINUTES.between(ultimoIntento, LocalDateTime.now());

        // Verificar si el período de bloqueo ha expirado
        if (minutosTranscurridos >= config.tiempoBloqueo) {
            // El bloqueo ha expirado, limpiar registro
            limpiarIntentosFallidos(nombreUsuario);
            return false;
        }

        return true; // Usuario aún bloqueado
    }

    /**
     * Registra una acción en el log de auditoría del sistema.
     *
     * @param mensaje Descripción de la acción realizada
     */
    private void registrarLog(String mensaje) {
        if (config.auditarAcciones) {
            String entrada = LocalDateTime.now() + " - " + mensaje;
            logSistema.add(entrada);

            // Mantener solo los últimos 1000 registros para evitar crecimiento excesivo
            if (logSistema.size() > 1000) {
                logSistema.remove(0); // Eliminar el más antiguo
            }
        }
    }

    /**
     * Obtiene el log completo del sistema.
     *
     * @return Lista de entradas del log de auditoría
     * @throws SecurityException si no se tienen permisos para ver logs
     */
    public List<String> getLogSistema() {
        verificarPermisoOExcepcion(Permiso.VER_LOGS_SISTEMA);
        return new ArrayList<>(logSistema); // Retornar copia para evitar modificaciones
    }

    /**
     * Obtiene las entradas más recientes del log.
     *
     * @param cantidad Número de entradas recientes a obtener
     * @return Lista con las últimas entradas del log
     * @throws SecurityException si no se tienen permisos para ver logs
     */
    public List<String> getLogReciente(int cantidad) {
        verificarPermisoOExcepcion(Permiso.VER_LOGS_SISTEMA);

        int size = logSistema.size();
        int fromIndex = Math.max(0, size - cantidad);
        return new ArrayList<>(logSistema.subList(fromIndex, size));
    }

    /**
     * Obtiene la lista de todos los usuarios del sistema.
     *
     * @return Lista de usuarios registrados
     * @throws SecurityException si no se tienen permisos adecuados
     */
    public List<Usuario> getUsuarios() {
        verificarPermisoOExcepcion(Permiso.VER_LOGS_SISTEMA);
        return new ArrayList<>(usuarios.values());
    }

    /**
     * Obtiene información de un usuario específico.
     *
     * @param nombreUsuario Nombre del usuario a consultar
     * @return Usuario solicitado o null si no existe
     * @throws SecurityException si no se tienen permisos para modificar usuarios
     */
    public Usuario getUsuario(String nombreUsuario) {
        verificarPermisoModificarUsuarios();
        return usuarios.get(nombreUsuario);
    }

    /**
     * Genera estadísticas resumidas del sistema de seguridad.
     *
     * @return String con estadísticas formateadas
     * @throws SecurityException si no se tienen privilegios de administrador
     */
    public String getEstadisticasSeguridad() {
        verificarPermisoAdmin();

        // Calcular métricas del sistema
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

    /**
     * Genera un reporte completo de seguridad del sistema.
     *
     * @return Mapa estructurado con estadísticas detalladas
     * @throws SecurityException si no se tienen privilegios de administrador
     */
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


    /**
     * Obtiene la configuración actual de seguridad.
     *
     * @return Configuración de seguridad actual
     * @throws SecurityException si no se tienen privilegios de administrador
     */
    public ConfiguracionSeguridad getConfiguracion() {
        verificarPermisoAdmin();
        return config;
    }

    /**
     * Actualiza la configuración de seguridad del sistema.
     *
     * @param nuevaConfig Nueva configuración a aplicar
     * @throws SecurityException si no se tienen privilegios de administrador
     */
    public void actualizarConfiguracion(ConfiguracionSeguridad nuevaConfig) {
        verificarPermisoAdmin();
        this.config = nuevaConfig;
        registrarLog("Configuración de seguridad actualizada");
    }


    /**
     * Verifica si existe un usuario con el nombre especificado.
     *
     * @param nombreUsuario Nombre de usuario a verificar
     * @return true si el usuario existe
     */
    public boolean existeUsuario(String nombreUsuario) {
        return usuarios.containsKey(nombreUsuario);
    }

    /**
     * Limpia todos los logs del sistema de auditoría.
     *
     * @throws SecurityException si no se tienen privilegios de administrador
     */
    public void limpiarLogsSistema() {
        verificarPermisoAdmin();
        int cantidadAnterior = logSistema.size();
        logSistema.clear();
        registrarLog("Logs del sistema limpiados (" + cantidadAnterior + " entradas eliminadas)");
    }

    /**
     * Exporta la configuración actual del sistema a un archivo.
     *
     * En una implementación completa, este método escribiría la configuración
     * a un archivo externo para respaldo o transferencia a otros sistemas.
     *
     * @throws SecurityException si no se tienen privilegios de administrador
     */
    public void exportarConfiguracion() {
        verificarPermisoAdmin();
        // En una implementación real, esto exportaría la configuración a un archivo
        // Ejemplo: escribir JSON o XML con todas las configuraciones actuales
        registrarLog("Configuración exportada");
    }

    /**
     * Importa configuración desde un archivo externo.
     *
     * En una implementación completa, este método leería configuración
     * desde un archivo y actualizaría las políticas del sistema.
     *
     * @throws SecurityException si no se tienen privilegios de administrador
     */
    public void importarConfiguracion() {
        verificarPermisoAdmin();
        // En una implementación real, esto importaría la configuración desde un archivo
        // Ejemplo: leer JSON o XML y actualizar config con validaciones
        registrarLog("Configuración importada");
    }
}