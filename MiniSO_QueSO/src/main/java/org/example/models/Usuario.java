package org.example.models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Clase modelo que representa un usuario del sistema MiniSO.
 * Maneja la autenticación, autorización, permisos y configuración de usuarios.
 * Implementa un sistema de seguridad con hash de contraseñas y gestión de sesiones.
 */
public class Usuario {

    // ======================== ATRIBUTOS DE INSTANCIA ========================

    /** Identificador único del usuario en el sistema */
    private String nombreUsuario;

    /** Hash SHA-256 de la contraseña del usuario con salt para seguridad */
    private String hashContraseña;

    /** Nombre completo del usuario */
    private String nombre;

    /** Dirección de correo electrónico del usuario */
    private String email;

    /** Tipo de usuario que determina los permisos base */
    private TipoUsuario tipo;

    /** Estado actual del usuario (activo, inactivo, bloqueado, etc.) */
    private EstadoUsuario estado;

    /** Fecha y hora de creación del usuario */
    private LocalDateTime fechaCreacion;

    /** Última fecha y hora de acceso al sistema */
    private LocalDateTime ultimoAcceso;

    /** Conjunto de permisos específicos asignados al usuario */
    private Set<Permiso> permisos;

    /** Configuraciones personalizadas del usuario */
    private Map<String, String> configuracion;

    /** Historial de acciones del usuario para auditoría */
    private List<String> historialAccesos;

    // ======================== ENUMERACIONES ========================

    /**
     * Enumeración que define los tipos de usuarios disponibles en el sistema.
     * Cada tipo tiene permisos predefinidos y un código de identificación.
     */
    public enum TipoUsuario {
        /** Administrador con control total del sistema */
        ADMINISTRADOR("admin", "Control total del sistema"),

        /** Usuario estándar con permisos básicos */
        USUARIO_ESTANDAR("user", "Acceso básico al sistema"),

        /** Usuario invitado con permisos limitados de solo lectura */
        INVITADO("guest", "Acceso limitado de solo lectura"),

        /** Usuario del sistema para procesos internos */
        SISTEMA("system", "Usuario del sistema interno");

        /** Código corto identificador del tipo */
        private final String codigo;

        /** Descripción detallada del tipo de usuario */
        private final String descripcion;

        /**
         * Constructor del enum TipoUsuario.
         *
         * @param codigo Código corto del tipo de usuario
         * @param descripcion Descripción detallada del tipo
         */
        TipoUsuario(String codigo, String descripcion) {
            this.codigo = codigo;
            this.descripcion = descripcion;
        }

        /**
         * Obtiene el código del tipo de usuario.
         *
         * @return El código del tipo de usuario
         */
        public String getCodigo() { return codigo; }

        /**
         * Obtiene la descripción del tipo de usuario.
         *
         * @return La descripción del tipo de usuario
         */
        public String getDescripcion() { return descripcion; }
    }

    /**
     * Enumeración que define los posibles estados de un usuario en el sistema.
     */
    public enum EstadoUsuario {
        /** Usuario activo que puede iniciar sesión */
        ACTIVO("Usuario activo"),

        /** Usuario desactivado temporalmente */
        INACTIVO("Usuario desactivado"),

        /** Usuario bloqueado por razones de seguridad */
        BLOQUEADO("Usuario bloqueado por seguridad"),

        /** Usuario suspendido temporalmente por administración */
        SUSPENDIDO("Usuario suspendido temporalmente");

        /** Descripción del estado del usuario */
        private final String descripcion;

        /**
         * Constructor del enum EstadoUsuario.
         *
         * @param descripcion Descripción del estado
         */
        EstadoUsuario(String descripcion) {
            this.descripcion = descripcion;
        }

        /**
         * Obtiene la descripción del estado.
         *
         * @return La descripción del estado
         */
        public String getDescripcion() { return descripcion; }
    }

    /**
     * Enumeración que define todos los permisos disponibles en el sistema.
     * Los permisos se organizan por categorías: archivos, sistema, procesos, memoria y especiales.
     */
    public enum Permiso {
        // === PERMISOS DE ARCHIVOS ===
        /** Permiso para leer archivos del sistema */
        LEER_ARCHIVOS("Leer archivos"),

        /** Permiso para escribir y modificar archivos */
        ESCRIBIR_ARCHIVOS("Escribir archivos"),

        /** Permiso para ejecutar archivos y scripts */
        EJECUTAR_ARCHIVOS("Ejecutar archivos"),

        /** Permiso para eliminar archivos del sistema */
        ELIMINAR_ARCHIVOS("Eliminar archivos"),

        // === PERMISOS DE SISTEMA ===
        /** Permiso para crear nuevos usuarios */
        CREAR_USUARIOS("Crear usuarios"),

        /** Permiso para modificar usuarios existentes */
        MODIFICAR_USUARIOS("Modificar usuarios"),

        /** Permiso para eliminar usuarios del sistema */
        ELIMINAR_USUARIOS("Eliminar usuarios"),

        /** Permiso para visualizar logs del sistema */
        VER_LOGS_SISTEMA("Ver logs del sistema"),

        // === PERMISOS DE PROCESOS ===
        /** Permiso para crear nuevos procesos */
        CREAR_PROCESOS("Crear procesos"),

        /** Permiso para terminar procesos */
        TERMINAR_PROCESOS("Terminar procesos"),

        /** Permiso para ver todos los procesos del sistema */
        VER_TODOS_PROCESOS("Ver todos los procesos"),

        // === PERMISOS DE MEMORIA ===
        /** Permiso para visualizar el estado de la memoria del sistema */
        VER_MEMORIA_SISTEMA("Ver memoria del sistema"),

        /** Permiso para modificar la configuración de memoria */
        MODIFICAR_MEMORIA("Modificar configuración de memoria"),

        // === PERMISOS ESPECIALES ===
        /** Acceso a funciones de administración avanzadas */
        ACCESO_MODO_ADMIN("Acceso a modo administrador"),

        /** Permiso para cambiar configuraciones críticas del sistema */
        CAMBIAR_CONFIGURACION_SISTEMA("Cambiar configuración del sistema"),

        /** Permiso para realizar operaciones de backup y restauración */
        BACKUP_RESTAURAR("Realizar backup y restauración");

        /** Descripción legible del permiso */
        private final String descripcion;

        /**
         * Constructor del enum Permiso.
         *
         * @param descripcion Descripción del permiso
         */
        Permiso(String descripcion) {
            this.descripcion = descripcion;
        }

        /**
         * Obtiene la descripción del permiso.
         *
         * @return La descripción del permiso
         */
        public String getDescripcion() { return descripcion; }
    }

    // ======================== CONSTRUCTORES ========================

    /**
     * Constructor principal para crear un nuevo usuario.
     * Inicializa todos los atributos necesarios y asigna permisos según el tipo.
     *
     * @param nombreUsuario Identificador único del usuario
     * @param contraseña Contraseña en texto plano (será hasheada)
     * @param nombre Nombre completo del usuario
     * @param tipo Tipo de usuario que determina los permisos
     * @throws IllegalArgumentException Si los parámetros no son válidos
     */
    public Usuario(String nombreUsuario, String contraseña, String nombre, TipoUsuario tipo) {
        // Validación básica de parámetros
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío");
        }

        // Inicialización de atributos básicos
        this.nombreUsuario = nombreUsuario.trim();
        this.hashContraseña = hashearContraseña(contraseña);
        this.nombre = nombre != null ? nombre.trim() : nombreUsuario;
        this.email = "";
        this.tipo = tipo;
        this.estado = EstadoUsuario.ACTIVO;

        // Inicialización de fechas
        this.fechaCreacion = LocalDateTime.now();
        this.ultimoAcceso = null;

        // Inicialización de colecciones
        this.permisos = new HashSet<>();
        this.configuracion = new HashMap<>();
        this.historialAccesos = new ArrayList<>();

        // Configuración inicial del usuario
        asignarPermisosSegunTipo();
        configurarDefaults();
    }

    /**
     * Constructor simplificado que usa el nombre de usuario como nombre completo.
     *
     * @param nombreUsuario Identificador único del usuario
     * @param contraseña Contraseña en texto plano
     * @param tipo Tipo de usuario
     */
    public Usuario(String nombreUsuario, String contraseña, TipoUsuario tipo) {
        this(nombreUsuario, contraseña, nombreUsuario, tipo);
    }

    // ======================== MÉTODOS PRIVADOS DE INICIALIZACIÓN ========================

    /**
     * Asigna los permisos predeterminados según el tipo de usuario.
     * Cada tipo de usuario tiene un conjunto específico de permisos base.
     */
    private void asignarPermisosSegunTipo() {
        // Limpiar permisos existentes para evitar conflictos
        permisos.clear();

        switch (tipo) {
            case ADMINISTRADOR:
                // El administrador tiene todos los permisos disponibles
                permisos.addAll(Arrays.asList(Permiso.values()));
                break;

            case USUARIO_ESTANDAR:
                // Usuario estándar tiene permisos básicos de trabajo
                permisos.addAll(Arrays.asList(
                        Permiso.LEER_ARCHIVOS,
                        Permiso.ESCRIBIR_ARCHIVOS,
                        Permiso.EJECUTAR_ARCHIVOS,
                        Permiso.CREAR_PROCESOS,
                        Permiso.TERMINAR_PROCESOS, // Solo sus propios procesos
                        Permiso.VER_MEMORIA_SISTEMA
                ));
                break;

            case INVITADO:
                // Usuario invitado solo tiene permisos de lectura
                permisos.addAll(Arrays.asList(
                        Permiso.LEER_ARCHIVOS,
                        Permiso.VER_MEMORIA_SISTEMA
                ));
                break;

            case SISTEMA:
                // Usuario del sistema tiene permisos mínimos necesarios
                permisos.addAll(Arrays.asList(
                        Permiso.LEER_ARCHIVOS,
                        Permiso.ESCRIBIR_ARCHIVOS,
                        Permiso.CREAR_PROCESOS
                ));
                break;
        }
    }

    /**
     * Configura los valores por defecto de la configuración del usuario.
     * Establece preferencias básicas como tema, idioma, etc.
     */
    private void configurarDefaults() {
        configuracion.put("tema", "claro");
        configuracion.put("idioma", "es");
        configuracion.put("mostrar_archivos_ocultos", "false");
        configuracion.put("tamaño_fuente", "12");
        configuracion.put("sesion_auto_logout", "30"); // minutos
    }

    // ======================== MÉTODOS DE AUTENTICACIÓN ========================

    /**
     * Verifica si la contraseña proporcionada coincide con la del usuario.
     *
     * @param contraseña Contraseña en texto plano a verificar
     * @return true si la contraseña es correcta, false en caso contrario
     */
    public boolean verificarContraseña(String contraseña) {
        if (contraseña == null) {
            return false;
        }
        return hashContraseña.equals(hashearContraseña(contraseña));
    }

    /**
     * Permite al usuario cambiar su contraseña proporcionando la actual.
     *
     * @param contraseñaActual Contraseña actual del usuario
     * @param nuevaContraseña Nueva contraseña a establecer
     * @throws SecurityException Si la contraseña actual es incorrecta
     * @throws IllegalArgumentException Si la nueva contraseña no es válida
     */
    public void cambiarContraseña(String contraseñaActual, String nuevaContraseña) {
        // Verificar que la contraseña actual es correcta
        if (!verificarContraseña(contraseñaActual)) {
            throw new SecurityException("Contraseña actual incorrecta");
        }

        // Validar y actualizar la nueva contraseña
        validarContraseña(nuevaContraseña);
        this.hashContraseña = hashearContraseña(nuevaContraseña);
        registrarAcceso("Contraseña cambiada");
    }

    /**
     * Permite a un administrador cambiar la contraseña de un usuario.
     * No requiere conocer la contraseña actual.
     *
     * @param nuevaContraseña Nueva contraseña a establecer
     * @throws IllegalArgumentException Si la nueva contraseña no es válida
     */
    public void cambiarContraseñaAdmin(String nuevaContraseña) {
        // Validar y actualizar la contraseña sin verificar la actual
        validarContraseña(nuevaContraseña);
        this.hashContraseña = hashearContraseña(nuevaContraseña);
        registrarAcceso("Contraseña cambiada por administrador");
    }

    /**
     * Valida que una contraseña cumple con los requisitos de seguridad.
     *
     * @param contraseña Contraseña a validar
     * @throws IllegalArgumentException Si la contraseña no cumple los requisitos
     */
    private void validarContraseña(String contraseña) {
        // Verificar longitud mínima
        if (contraseña == null || contraseña.length() < 4) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 4 caracteres");
        }

        // Verificar que no contiene el nombre de usuario
        if (contraseña.toLowerCase().contains(nombreUsuario.toLowerCase())) {
            throw new IllegalArgumentException("La contraseña no puede contener el nombre de usuario");
        }

        // Verificar que no es una contraseña común
        if (contraseña.equals("1234") || contraseña.equals("admin") || contraseña.equals("password")) {
            throw new IllegalArgumentException("Contraseña demasiado común. Use una más segura");
        }
    }

    /**
     * Genera un hash SHA-256 de la contraseña con salt para mayor seguridad.
     *
     * @param contraseña Contraseña en texto plano
     * @return Hash hexadecimal de la contraseña
     */
    private String hashearContraseña(String contraseña) {
        try {
            // Usar SHA-256 para el hash
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Agregar salt para mayor seguridad
            String contraseñaConSalt = contraseña + "miniso_salt_2025";
            byte[] hash = md.digest(contraseñaConSalt.getBytes());

            // Convertir bytes a representación hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            // Fallback simple si SHA-256 no está disponible
            return String.valueOf(contraseña.hashCode());
        }
    }

    // ======================== MÉTODOS DE GESTIÓN DE PERMISOS ========================

    /**
     * Verifica si el usuario tiene un permiso específico.
     *
     * @param permiso Permiso a verificar
     * @return true si el usuario tiene el permiso, false en caso contrario
     */
    public boolean tienePermiso(Permiso permiso) {
        return permisos.contains(permiso);
    }

    /**
     * Agrega un permiso específico al usuario.
     *
     * @param permiso Permiso a agregar
     */
    public void agregarPermiso(Permiso permiso) {
        permisos.add(permiso);
        registrarAcceso("Permiso agregado: " + permiso.getDescripcion());
    }

    /**
     * Remueve un permiso específico del usuario.
     *
     * @param permiso Permiso a remover
     */
    public void removerPermiso(Permiso permiso) {
        permisos.remove(permiso);
        registrarAcceso("Permiso removido: " + permiso.getDescripcion());
    }

    /**
     * Verifica que el usuario tenga un permiso específico, lanza excepción si no lo tiene.
     *
     * @param permiso Permiso requerido
     * @throws SecurityException Si el usuario no tiene el permiso
     */
    public void verificarPermiso(Permiso permiso) throws SecurityException {
        if (!tienePermiso(permiso)) {
            throw new SecurityException("Usuario " + nombreUsuario +
                    " no tiene permiso: " + permiso.getDescripcion());
        }
    }

    // ======================== MÉTODOS DE GESTIÓN DE SESIÓN ========================

    /**
     * Inicia una sesión para el usuario, actualizando la fecha de último acceso.
     *
     * @throws SecurityException Si el usuario no puede iniciar sesión
     */
    public void iniciarSesion() {
        // Verificar que el usuario puede iniciar sesión
        if (estado != EstadoUsuario.ACTIVO) {
            throw new SecurityException("Usuario no activo: " + estado.getDescripcion());
        }

        // Actualizar fecha de último acceso y registrar la acción
        this.ultimoAcceso = LocalDateTime.now();
        registrarAcceso("Inicio de sesión");
    }

    /**
     * Cierra la sesión del usuario registrando la acción.
     */
    public void cerrarSesion() {
        registrarAcceso("Cierre de sesión");
    }

    /**
     * Verifica si el usuario puede iniciar sesión según su estado actual.
     *
     * @return true si puede iniciar sesión, false en caso contrario
     */
    public boolean puedeIniciarSesion() {
        return estado == EstadoUsuario.ACTIVO;
    }

    // ======================== MÉTODOS DE ADMINISTRACIÓN ========================

    /**
     * Activa el usuario permitiéndole iniciar sesión.
     */
    public void activar() {
        this.estado = EstadoUsuario.ACTIVO;
        registrarAcceso("Usuario activado");
    }

    /**
     * Desactiva el usuario impidiéndole iniciar sesión.
     */
    public void desactivar() {
        this.estado = EstadoUsuario.INACTIVO;
        registrarAcceso("Usuario desactivado");
    }

    /**
     * Bloquea el usuario por razones de seguridad.
     *
     * @param razon Motivo del bloqueo
     */
    public void bloquear(String razon) {
        this.estado = EstadoUsuario.BLOQUEADO;
        registrarAcceso("Usuario bloqueado: " + razon);
    }

    /**
     * Suspende temporalmente al usuario.
     *
     * @param razon Motivo de la suspensión
     */
    public void suspender(String razon) {
        this.estado = EstadoUsuario.SUSPENDIDO;
        registrarAcceso("Usuario suspendido: " + razon);
    }

    // ======================== GESTIÓN DE CONFIGURACIÓN ========================

    /**
     * Establece un valor de configuración para el usuario.
     *
     * @param clave Clave de configuración
     * @param valor Valor a establecer
     */
    public void setConfiguracion(String clave, String valor) {
        configuracion.put(clave, valor);
    }

    /**
     * Obtiene un valor de configuración del usuario.
     *
     * @param clave Clave de configuración
     * @return Valor de configuración o null si no existe
     */
    public String getConfiguracion(String clave) {
        return configuracion.get(clave);
    }

    /**
     * Obtiene un valor de configuración con un valor por defecto.
     *
     * @param clave Clave de configuración
     * @param valorPorDefecto Valor a retornar si la clave no existe
     * @return Valor de configuración o el valor por defecto
     */
    public String getConfiguracion(String clave, String valorPorDefecto) {
        return configuracion.getOrDefault(clave, valorPorDefecto);
    }

    // ======================== HISTORIAL Y AUDITORÍA ========================

    /**
     * Registra una acción del usuario en el historial para auditoría.
     * Mantiene solo los últimos 100 registros para optimizar memoria.
     *
     * @param accion Descripción de la acción realizada
     */
    private void registrarAcceso(String accion) {
        String entrada = LocalDateTime.now() + " - " + accion;
        historialAccesos.add(entrada);

        // Mantener solo los últimos 100 registros para optimizar memoria
        if (historialAccesos.size() > 100) {
            historialAccesos.remove(0);
        }
    }

    /**
     * Obtiene una copia del historial completo de accesos del usuario.
     *
     * @return Lista con el historial de accesos
     */
    public List<String> getHistorialAccesos() {
        return new ArrayList<>(historialAccesos);
    }

    /**
     * Obtiene los registros más recientes del historial de accesos.
     *
     * @param cantidad Número de registros recientes a obtener
     * @return Lista con los registros más recientes
     */
    public List<String> getHistorialReciente(int cantidad) {
        int size = historialAccesos.size();
        int fromIndex = Math.max(0, size - cantidad);
        return new ArrayList<>(historialAccesos.subList(fromIndex, size));
    }

    // ======================== MÉTODOS DE INFORMACIÓN ========================

    /**
     * Genera un perfil completo del usuario con toda su información.
     *
     * @return String formateado con la información del usuario
     */
    public String getPerfilCompleto() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PERFIL DE USUARIO ===\n");
        sb.append("Usuario: ").append(nombreUsuario).append("\n");
        sb.append("Nombre: ").append(nombre).append("\n");
        sb.append("Tipo: ").append(tipo.getDescripcion()).append("\n");
        sb.append("Estado: ").append(estado.getDescripcion()).append("\n");
        sb.append("Email: ").append(email.isEmpty() ? "No configurado" : email).append("\n");
        sb.append("Creado: ").append(fechaCreacion.toLocalDate()).append("\n");
        sb.append("Último acceso: ").append(ultimoAcceso != null ?
                ultimoAcceso.toLocalDate() : "Nunca").append("\n");
        sb.append("Permisos: ").append(permisos.size()).append(" asignados\n");

        return sb.toString();
    }

    /**
     * Verifica si el usuario es administrador.
     *
     * @return true si es administrador, false en caso contrario
     */
    public boolean esAdministrador() {
        return tipo == TipoUsuario.ADMINISTRADOR;
    }

    /**
     * Verifica si el usuario es un usuario del sistema.
     *
     * @return true si es usuario del sistema, false en caso contrario
     */
    public boolean esUsuarioSistema() {
        return tipo == TipoUsuario.SISTEMA;
    }

    /**
     * Obtiene el icono emoji representativo del tipo de usuario.
     *
     * @return Emoji que representa el tipo de usuario
     */
    public String getIcono() {
        switch (tipo) {
            case ADMINISTRADOR:
                return "👑";
            case USUARIO_ESTANDAR:
                return "👤";
            case INVITADO:
                return "👥";
            case SISTEMA:
                return "🤖";
            default:
                return "❓";
        }
    }

    /**
     * Obtiene el icono emoji representativo del estado del usuario.
     *
     * @return Emoji que representa el estado del usuario
     */
    public String getEstadoIcono() {
        switch (estado) {
            case ACTIVO:
                return "🟢";
            case INACTIVO:
                return "⚫";
            case BLOQUEADO:
                return "🔴";
            case SUSPENDIDO:
                return "🟡";
            default:
                return "❓";
        }
    }

    // ======================== GETTERS Y SETTERS ========================

    /**
     * Obtiene el nombre de usuario.
     *
     * @return El nombre de usuario
     */
    public String getNombreUsuario() { return nombreUsuario; }

    /**
     * Obtiene el nombre completo del usuario.
     *
     * @return El nombre completo
     */
    public String getNombre() { return nombre; }

    /**
     * Establece el nombre completo del usuario.
     *
     * @param nombre El nuevo nombre completo
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Obtiene el email del usuario.
     *
     * @return El email del usuario
     */
    public String getEmail() { return email; }

    /**
     * Establece el email del usuario.
     *
     * @param email El nuevo email
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Obtiene el tipo de usuario.
     *
     * @return El tipo de usuario
     */
    public TipoUsuario getTipo() { return tipo; }

    /**
     * Establece el tipo de usuario y reasigna permisos automáticamente.
     *
     * @param tipo El nuevo tipo de usuario
     */
    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
        // Reasignar permisos según el nuevo tipo
        asignarPermisosSegunTipo();
    }

    /**
     * Obtiene el estado actual del usuario.
     *
     * @return El estado del usuario
     */
    public EstadoUsuario getEstado() { return estado; }

    /**
     * Obtiene la fecha de creación del usuario.
     *
     * @return La fecha de creación
     */
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    /**
     * Obtiene la fecha del último acceso del usuario.
     *
     * @return La fecha del último acceso o null si nunca ha accedido
     */
    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }

    /**
     * Obtiene una copia del conjunto de permisos del usuario.
     *
     * @return Conjunto de permisos del usuario
     */
    public Set<Permiso> getPermisos() { return new HashSet<>(permisos); }

    /**
     * Obtiene una copia del mapa de configuración del usuario.
     *
     * @return Mapa con la configuración del usuario
     */
    public Map<String, String> getConfiguracion() { return new HashMap<>(configuracion); }

    // ======================== MÉTODOS SOBRESCRITOS ========================

    /**
     * Representación en cadena del usuario para mostrar información básica.
     *
     * @return String con información básica del usuario
     */
    @Override
    public String toString() {
        return String.format("%s %s (%s) - %s %s",
                getIcono(), nombreUsuario, nombre,
                tipo.getCodigo(), getEstadoIcono());
    }

    /**
     * Verifica la igualdad entre usuarios basándose en el nombre de usuario.
     *
     * @param obj Objeto a comparar
     * @return true si son el mismo usuario, false en caso contrario
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Usuario usuario = (Usuario) obj;
        return nombreUsuario.equals(usuario.nombreUsuario);
    }

    /**
     * Genera el código hash basado en el nombre de usuario.
     *
     * @return Código hash del usuario
     */
    @Override
    public int hashCode() {
        return nombreUsuario.hashCode();
    }
}