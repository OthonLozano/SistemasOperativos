package org.example.models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

public class Usuario {
    private String nombreUsuario;
    private String hashContraseña;
    private String nombre;
    private String email;
    private TipoUsuario tipo;
    private EstadoUsuario estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoAcceso;
    private Set<Permiso> permisos;
    private Map<String, String> configuracion;
    private List<String> historialAccesos;

    public enum TipoUsuario {
        ADMINISTRADOR("admin", "Control total del sistema"),
        USUARIO_ESTANDAR("user", "Acceso básico al sistema"),
        INVITADO("guest", "Acceso limitado de solo lectura"),
        SISTEMA("system", "Usuario del sistema interno");

        private final String codigo;
        private final String descripcion;

        TipoUsuario(String codigo, String descripcion) {
            this.codigo = codigo;
            this.descripcion = descripcion;
        }

        public String getCodigo() { return codigo; }
        public String getDescripcion() { return descripcion; }
    }

    public enum EstadoUsuario {
        ACTIVO("Usuario activo"),
        INACTIVO("Usuario desactivado"),
        BLOQUEADO("Usuario bloqueado por seguridad"),
        SUSPENDIDO("Usuario suspendido temporalmente");

        private final String descripcion;

        EstadoUsuario(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() { return descripcion; }
    }

    public enum Permiso {
        // Permisos de archivos
        LEER_ARCHIVOS("Leer archivos"),
        ESCRIBIR_ARCHIVOS("Escribir archivos"),
        EJECUTAR_ARCHIVOS("Ejecutar archivos"),
        ELIMINAR_ARCHIVOS("Eliminar archivos"),

        // Permisos de sistema
        CREAR_USUARIOS("Crear usuarios"),
        MODIFICAR_USUARIOS("Modificar usuarios"),
        ELIMINAR_USUARIOS("Eliminar usuarios"),
        VER_LOGS_SISTEMA("Ver logs del sistema"),

        // Permisos de procesos
        CREAR_PROCESOS("Crear procesos"),
        TERMINAR_PROCESOS("Terminar procesos"),
        VER_TODOS_PROCESOS("Ver todos los procesos"),

        // Permisos de memoria
        VER_MEMORIA_SISTEMA("Ver memoria del sistema"),
        MODIFICAR_MEMORIA("Modificar configuración de memoria"),

        // Permisos especiales
        ACCESO_MODO_ADMIN("Acceso a modo administrador"),
        CAMBIAR_CONFIGURACION_SISTEMA("Cambiar configuración del sistema"),
        BACKUP_RESTAURAR("Realizar backup y restauración");

        private final String descripcion;

        Permiso(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() { return descripcion; }
    }

    // Constructor principal
    public Usuario(String nombreUsuario, String contraseña, String nombre, TipoUsuario tipo) {
        this.nombreUsuario = nombreUsuario;
        this.hashContraseña = hashearContraseña(contraseña);
        this.nombre = nombre;
        this.email = "";
        this.tipo = tipo;
        this.estado = EstadoUsuario.ACTIVO;
        this.fechaCreacion = LocalDateTime.now();
        this.ultimoAcceso = null;
        this.permisos = new HashSet<>();
        this.configuracion = new HashMap<>();
        this.historialAccesos = new ArrayList<>();

        asignarPermisosSegunTipo();
        configurarDefaults();
    }

    // Constructor simplificado
    public Usuario(String nombreUsuario, String contraseña, TipoUsuario tipo) {
        this(nombreUsuario, contraseña, nombreUsuario, tipo);
    }

    private void asignarPermisosSegunTipo() {
        switch (tipo) {
            case ADMINISTRADOR:
                // Administrador tiene todos los permisos
                permisos.addAll(Arrays.asList(Permiso.values()));
                break;

            case USUARIO_ESTANDAR:
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
                permisos.addAll(Arrays.asList(
                        Permiso.LEER_ARCHIVOS,
                        Permiso.VER_MEMORIA_SISTEMA
                ));
                break;

            case SISTEMA:
                // Usuario del sistema - permisos mínimos específicos
                permisos.addAll(Arrays.asList(
                        Permiso.LEER_ARCHIVOS,
                        Permiso.ESCRIBIR_ARCHIVOS,
                        Permiso.CREAR_PROCESOS
                ));
                break;
        }
    }

    private void configurarDefaults() {
        configuracion.put("tema", "claro");
        configuracion.put("idioma", "es");
        configuracion.put("mostrar_archivos_ocultos", "false");
        configuracion.put("tamaño_fuente", "12");
        configuracion.put("sesion_auto_logout", "30"); // minutos
    }

    // Métodos de autenticación
    public boolean verificarContraseña(String contraseña) {
        return hashContraseña.equals(hashearContraseña(contraseña));
    }

    public void cambiarContraseña(String contraseñaActual, String nuevaContraseña) {
        if (!verificarContraseña(contraseñaActual)) {
            throw new SecurityException("Contraseña actual incorrecta");
        }

        validarContraseña(nuevaContraseña);
        this.hashContraseña = hashearContraseña(nuevaContraseña);
        registrarAcceso("Contraseña cambiada");
    }

    public void cambiarContraseñaAdmin(String nuevaContraseña) {
        // Solo para uso administrativo
        validarContraseña(nuevaContraseña);
        this.hashContraseña = hashearContraseña(nuevaContraseña);
        registrarAcceso("Contraseña cambiada por administrador");
    }

    private void validarContraseña(String contraseña) {
        if (contraseña == null || contraseña.length() < 4) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 4 caracteres");
        }

        // Validaciones adicionales para mayor seguridad
        if (contraseña.toLowerCase().contains(nombreUsuario.toLowerCase())) {
            throw new IllegalArgumentException("La contraseña no puede contener el nombre de usuario");
        }

        if (contraseña.equals("1234") || contraseña.equals("admin") || contraseña.equals("password")) {
            throw new IllegalArgumentException("Contraseña demasiado común. Use una más segura");
        }
    }

    private String hashearContraseña(String contraseña) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Agregar salt simple para mayor seguridad
            String contraseñaConSalt = contraseña + "miniso_salt_2025";
            byte[] hash = md.digest(contraseñaConSalt.getBytes());

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

    // Métodos de gestión de permisos
    public boolean tienePermiso(Permiso permiso) {
        return permisos.contains(permiso);
    }

    public void agregarPermiso(Permiso permiso) {
        permisos.add(permiso);
        registrarAcceso("Permiso agregado: " + permiso.getDescripcion());
    }

    public void removerPermiso(Permiso permiso) {
        permisos.remove(permiso);
        registrarAcceso("Permiso removido: " + permiso.getDescripcion());
    }

    public void verificarPermiso(Permiso permiso) throws SecurityException {
        if (!tienePermiso(permiso)) {
            throw new SecurityException("Usuario " + nombreUsuario +
                    " no tiene permiso: " + permiso.getDescripcion());
        }
    }

    // Métodos de sesión
    public void iniciarSesion() {
        if (estado != EstadoUsuario.ACTIVO) {
            throw new SecurityException("Usuario no activo: " + estado.getDescripcion());
        }

        this.ultimoAcceso = LocalDateTime.now();
        registrarAcceso("Inicio de sesión");
    }

    public void cerrarSesion() {
        registrarAcceso("Cierre de sesión");
    }

    public boolean puedeIniciarSesion() {
        return estado == EstadoUsuario.ACTIVO;
    }

    // Métodos de administración
    public void activar() {
        this.estado = EstadoUsuario.ACTIVO;
        registrarAcceso("Usuario activado");
    }

    public void desactivar() {
        this.estado = EstadoUsuario.INACTIVO;
        registrarAcceso("Usuario desactivado");
    }

    public void bloquear(String razon) {
        this.estado = EstadoUsuario.BLOQUEADO;
        registrarAcceso("Usuario bloqueado: " + razon);
    }

    public void suspender(String razon) {
        this.estado = EstadoUsuario.SUSPENDIDO;
        registrarAcceso("Usuario suspendido: " + razon);
    }

    // Gestión de configuración
    public void setConfiguracion(String clave, String valor) {
        configuracion.put(clave, valor);
    }

    public String getConfiguracion(String clave) {
        return configuracion.get(clave);
    }

    public String getConfiguracion(String clave, String valorPorDefecto) {
        return configuracion.getOrDefault(clave, valorPorDefecto);
    }

    // Historial y logs
    private void registrarAcceso(String accion) {
        String entrada = LocalDateTime.now() + " - " + accion;
        historialAccesos.add(entrada);

        // Mantener solo los últimos 100 registros
        if (historialAccesos.size() > 100) {
            historialAccesos.remove(0);
        }
    }

    public List<String> getHistorialAccesos() {
        return new ArrayList<>(historialAccesos);
    }

    public List<String> getHistorialReciente(int cantidad) {
        int size = historialAccesos.size();
        int fromIndex = Math.max(0, size - cantidad);
        return new ArrayList<>(historialAccesos.subList(fromIndex, size));
    }

    // Métodos de información
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

    public boolean esAdministrador() {
        return tipo == TipoUsuario.ADMINISTRADOR;
    }

    public boolean esUsuarioSistema() {
        return tipo == TipoUsuario.SISTEMA;
    }

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

    // Getters y Setters
    public String getNombreUsuario() { return nombreUsuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public TipoUsuario getTipo() { return tipo; }
    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
        // Reasignar permisos según el nuevo tipo
        permisos.clear();
        asignarPermisosSegunTipo();
    }

    public EstadoUsuario getEstado() { return estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }

    public Set<Permiso> getPermisos() { return new HashSet<>(permisos); }
    public Map<String, String> getConfiguracion() { return new HashMap<>(configuracion); }

    @Override
    public String toString() {
        return String.format("%s %s (%s) - %s %s",
                getIcono(), nombreUsuario, nombre,
                tipo.getCodigo(), getEstadoIcono());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Usuario usuario = (Usuario) obj;
        return nombreUsuario.equals(usuario.nombreUsuario);
    }

    @Override
    public int hashCode() {
        return nombreUsuario.hashCode();
    }
}