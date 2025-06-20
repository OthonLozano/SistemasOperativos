package org.example.models;

import java.time.LocalDateTime;

/**
 * Modelo de datos que representa un archivo en el sistema MiniSO
 *
 * Esta clase encapsula todas las propiedades y comportamientos de un archivo
 * en el sistema de archivos simulado, incluyendo:
 *
 * - Metadatos básicos (nombre, tamaño, fechas, propietario)
 * - Sistema de permisos estilo Unix (rwx)
 * - Clasificación automática por tipo de archivo
 * - Operaciones fundamentales (leer, escribir, ejecutar, copiar)
 * - Gestión de rutas y ubicación en el sistema de archivos
 *
 * La clase implementa un modelo completo de archivo que simula el comportamiento
 * de sistemas de archivos reales, incluyendo validaciones de seguridad y
 * manejo de metadatos temporal.
 */
public class Archivo {

    // === ATRIBUTOS PRINCIPALES DEL ARCHIVO ===

    /** Nombre completo del archivo incluyendo extensión */
    private String nombre;

    /** Extensión del archivo (ej: .txt, .java, .pdf) */
    private String extension;

    /** Tamaño del archivo en bytes */
    private int tamaño;

    /** Contenido textual del archivo (para archivos de texto) */
    private String contenido;

    /** Fecha y hora de creación del archivo */
    private LocalDateTime fechaCreacion;

    /** Fecha y hora de la última modificación */
    private LocalDateTime fechaModificacion;

    /** Tipo de archivo basado en su extensión */
    private TipoArchivo tipo;

    /** Permisos de acceso al archivo (lectura, escritura, ejecución) */
    private Permisos permisos;

    /** Usuario propietario del archivo */
    private String propietario;

    /** Ruta completa donde se ubica el archivo en el sistema */
    private String ruta;

    /**
     * Enumeración que define los tipos de archivo soportados por el sistema
     *
     * Cada tipo incluye su extensión típica para facilitar la clasificación
     * automática de archivos según su nombre.
     */
    public enum TipoArchivo {
        /** Archivos de texto plano y documentos editables */
        TEXTO(".txt"),

        /** Archivos de imagen en diversos formatos */
        IMAGEN(".jpg"),

        /** Archivos de video y multimedia */
        VIDEO(".mp4"),

        /** Archivos de audio y sonido */
        AUDIO(".mp3"),

        /** Documentos de oficina y PDF */
        DOCUMENTO(".doc"),

        /** Archivos ejecutables y aplicaciones */
        EJECUTABLE(".exe"),

        /** Scripts y archivos de comandos */
        SCRIPT(".sh"),

        /** Directorios (tipo especial) */
        CARPETA(""),

        /** Archivos de tipo no reconocido */
        OTRO("");

        /** Extensión típica asociada a este tipo de archivo */
        private final String extensionTipica;

        /**
         * Constructor del enum con extensión típica
         *
         * @param extensionTipica Extensión más común para este tipo
         */
        TipoArchivo(String extensionTipica) {
            this.extensionTipica = extensionTipica;
        }

        /**
         * Obtiene la extensión típica del tipo de archivo
         *
         * @return String con la extensión (ej: ".txt", ".jpg")
         */
        public String getExtensionTipica() {
            return extensionTipica;
        }
    }

    /**
     * Clase interna que representa el sistema de permisos de un archivo
     *
     * Implementa un modelo de permisos similar al sistema Unix/Linux,
     * con tres tipos básicos de acceso: lectura, escritura y ejecución.
     * Los permisos se validan en cada operación sensible.
     */
    public static class Permisos {

        /** Permiso de lectura - permite leer el contenido del archivo */
        private boolean lectura;

        /** Permiso de escritura - permite modificar el contenido del archivo */
        private boolean escritura;

        /** Permiso de ejecución - permite ejecutar el archivo como programa */
        private boolean ejecucion;

        /**
         * Constructor completo de permisos
         *
         * @param lectura true si se permite leer el archivo
         * @param escritura true si se permite escribir en el archivo
         * @param ejecucion true si se permite ejecutar el archivo
         */
        public Permisos(boolean lectura, boolean escritura, boolean ejecucion) {
            this.lectura = lectura;
            this.escritura = escritura;
            this.ejecucion = ejecucion;
        }

        /**
         * Constructor por defecto con permisos estándar
         *
         * Establece permisos de lectura y escritura activados,
         * y ejecución desactivada (comportamiento seguro por defecto).
         */
        public Permisos() {
            this(true, true, false);
        }

        // === GETTERS Y SETTERS DE PERMISOS ===

        /**
         * Verifica si el archivo tiene permiso de lectura
         * @return true si se puede leer el archivo
         */
        public boolean isLectura() { return lectura; }

        /**
         * Establece el permiso de lectura
         * @param lectura nuevo estado del permiso de lectura
         */
        public void setLectura(boolean lectura) { this.lectura = lectura; }

        /**
         * Verifica si el archivo tiene permiso de escritura
         * @return true si se puede escribir en el archivo
         */
        public boolean isEscritura() { return escritura; }

        /**
         * Establece el permiso de escritura
         * @param escritura nuevo estado del permiso de escritura
         */
        public void setEscritura(boolean escritura) { this.escritura = escritura; }

        /**
         * Verifica si el archivo tiene permiso de ejecución
         * @return true si se puede ejecutar el archivo
         */
        public boolean isEjecucion() { return ejecucion; }

        /**
         * Establece el permiso de ejecución
         * @param ejecucion nuevo estado del permiso de ejecución
         */
        public void setEjecucion(boolean ejecucion) { this.ejecucion = ejecucion; }

        /**
         * Convierte los permisos a representación textual estilo Unix
         *
         * @return String en formato "rwx" donde cada letra indica el permiso
         *         (r=lectura, w=escritura, x=ejecución, -=sin permiso)
         */
        @Override
        public String toString() {
            return (lectura ? "r" : "-") +
                    (escritura ? "w" : "-") +
                    (ejecucion ? "x" : "-");
        }
    }

    // === CONSTRUCTORES ===

    /**
     * Constructor principal para crear un archivo completo
     *
     * Inicializa todos los atributos del archivo, determina automáticamente
     * el tipo basado en la extensión y establece las fechas de creación.
     *
     * @param nombre Nombre del archivo (con extensión)
     * @param contenido Contenido inicial del archivo (puede ser null)
     * @param propietario Usuario propietario del archivo
     * @param ruta Ruta donde se ubica el archivo
     */
    public Archivo(String nombre, String contenido, String propietario, String ruta) {
        this.nombre = nombre;
        this.contenido = contenido != null ? contenido : ""; // Evitar null
        this.propietario = propietario;
        this.ruta = ruta;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
        this.permisos = new Permisos(); // Permisos por defecto

        // Análisis automático del archivo
        determinarExtensionYTipo();

        // Calcular tamaño basado en contenido
        this.tamaño = this.contenido.length();
    }

    /**
     * Constructor simplificado para archivos básicos
     *
     * Crea un archivo con contenido vacío en la ruta raíz.
     * Útil para crear archivos que se llenarán posteriormente.
     *
     * @param nombre Nombre del archivo
     * @param propietario Usuario propietario
     */
    public Archivo(String nombre, String propietario) {
        this(nombre, "", propietario, "/");
    }

    // === MÉTODOS PRIVADOS DE ANÁLISIS ===

    /**
     * Determina la extensión y tipo del archivo basado en su nombre
     *
     * Analiza el nombre del archivo para extraer la extensión y
     * clasificarlo en uno de los tipos predefinidos.
     */
    private void determinarExtensionYTipo() {
        int puntoIndex = nombre.lastIndexOf('.');

        if (puntoIndex > 0 && puntoIndex < nombre.length() - 1) {
            // Archivo con extensión válida
            this.extension = nombre.substring(puntoIndex);
            this.tipo = determinarTipoPorExtension(extension);
        } else {
            // Archivo sin extensión
            this.extension = "";
            this.tipo = TipoArchivo.OTRO;
        }
    }

    /**
     * Clasifica el archivo según su extensión
     *
     * Mapea extensiones comunes a tipos de archivo predefinidos
     * para facilitar el manejo y visualización en el sistema.
     *
     * @param ext Extensión del archivo (incluyendo el punto)
     * @return TipoArchivo correspondiente a la extensión
     */
    private TipoArchivo determinarTipoPorExtension(String ext) {
        ext = ext.toLowerCase(); // Normalizar a minúsculas

        switch (ext) {
            // === ARCHIVOS DE TEXTO ===
            case ".txt":
            case ".md":
            case ".log":
                return TipoArchivo.TEXTO;

            // === ARCHIVOS DE IMAGEN ===
            case ".jpg":
            case ".png":
            case ".gif":
            case ".bmp":
                return TipoArchivo.IMAGEN;

            // === ARCHIVOS DE VIDEO ===
            case ".mp4":
            case ".avi":
            case ".mov":
                return TipoArchivo.VIDEO;

            // === ARCHIVOS DE AUDIO ===
            case ".mp3":
            case ".wav":
            case ".flac":
                return TipoArchivo.AUDIO;

            // === DOCUMENTOS ===
            case ".doc":
            case ".docx":
            case ".pdf":
                return TipoArchivo.DOCUMENTO;

            // === EJECUTABLES ===
            case ".exe":
            case ".msi":
                return TipoArchivo.EJECUTABLE;

            // === SCRIPTS ===
            case ".sh":
            case ".bat":
            case ".cmd":
                return TipoArchivo.SCRIPT;

            default:
                return TipoArchivo.OTRO;
        }
    }

    // === MÉTODOS DE OPERACIÓN CON VALIDACIÓN DE PERMISOS ===

    /**
     * Modifica el contenido del archivo
     *
     * Actualiza el contenido del archivo, recalcula el tamaño y
     * actualiza la fecha de modificación. Requiere permisos de escritura.
     *
     * @param nuevoContenido Nuevo contenido para el archivo
     * @throws SecurityException si no tiene permisos de escritura
     */
    public void modificarContenido(String nuevoContenido) {
        if (permisos.isEscritura()) {
            this.contenido = nuevoContenido;
            this.tamaño = nuevoContenido.length();
            this.fechaModificacion = LocalDateTime.now();
        } else {
            throw new SecurityException("No tiene permisos de escritura en el archivo: " + nombre);
        }
    }

    /**
     * Lee el contenido del archivo
     *
     * Retorna el contenido completo del archivo como string.
     * Requiere permisos de lectura.
     *
     * @return String con el contenido del archivo
     * @throws SecurityException si no tiene permisos de lectura
     */
    public String leerContenido() {
        if (permisos.isLectura()) {
            return contenido;
        } else {
            throw new SecurityException("No tiene permisos de lectura en el archivo: " + nombre);
        }
    }

    /**
     * Ejecuta el archivo como programa
     *
     * Simula la ejecución del archivo. En un sistema real, esto
     * invocaría el intérprete o ejecutor apropiado.
     * Requiere permisos de ejecución.
     *
     * @throws SecurityException si no tiene permisos de ejecución
     */
    public void ejecutar() {
        if (permisos.isEjecucion()) {
            // Simulación de ejecución para el entorno académico
            System.out.println("Ejecutando archivo: " + nombre);
            // En un sistema real, aquí se invocaría el proceso correspondiente
        } else {
            throw new SecurityException("No tiene permisos de ejecución en el archivo: " + nombre);
        }
    }

    /**
     * Renombra el archivo
     *
     * Cambia el nombre del archivo y reanaliza su tipo basado en la nueva extensión.
     * Actualiza la fecha de modificación.
     *
     * @param nuevoNombre Nuevo nombre para el archivo (con extensión)
     */
    public void renombrar(String nuevoNombre) {
        this.nombre = nuevoNombre;
        this.fechaModificacion = LocalDateTime.now();
        determinarExtensionYTipo(); // Reanalizar tipo por nueva extensión
    }

    /**
     * Crea una copia del archivo en una nueva ubicación
     *
     * Genera una copia exacta del archivo con el mismo contenido y permisos,
     * pero en la ruta especificada. La copia mantiene el mismo propietario.
     *
     * @param nuevaRuta Ruta donde crear la copia
     * @return Archivo nuevo objeto Archivo que representa la copia
     */
    public Archivo copiar(String nuevaRuta) {
        Archivo copia = new Archivo(this.nombre, this.contenido, this.propietario, nuevaRuta);
        // Copiar permisos exactos del archivo original
        copia.setPermisos(new Permisos(this.permisos.lectura, this.permisos.escritura, this.permisos.ejecucion));
        return copia;
    }

    // === MÉTODOS DE INFORMACIÓN Y UTILIDAD ===

    /**
     * Obtiene el nombre completo del archivo
     *
     * @return String con el nombre completo incluyendo extensión
     */
    public String getNombreCompleto() {
        return nombre;
    }

    /**
     * Obtiene el nombre del archivo sin la extensión
     *
     * Útil para mostrar nombres limpios en interfaces o para
     * operaciones que requieren solo el nombre base.
     *
     * @return String con el nombre sin extensión, o nombre completo si no tiene extensión
     */
    public String getNombreSinExtension() {
        int puntoIndex = nombre.lastIndexOf('.');
        return puntoIndex > 0 ? nombre.substring(0, puntoIndex) : nombre;
    }

    /**
     * Formatea el tamaño del archivo en unidades legibles
     *
     * Convierte el tamaño en bytes a una representación más legible
     * usando las unidades apropiadas (B, KB, MB).
     *
     * @return String formateado con el tamaño y unidad (ej: "1.5 KB", "2.3 MB")
     */
    public String getTamañoFormateado() {
        if (tamaño < 1024) {
            return tamaño + " B";
        } else if (tamaño < 1024 * 1024) {
            return String.format("%.1f KB", tamaño / 1024.0);
        } else {
            return String.format("%.1f MB", tamaño / (1024.0 * 1024.0));
        }
    }

    /**
     * Obtiene el emoji representativo del tipo de archivo
     *
     * Retorna un emoji que representa visualmente el tipo de archivo,
     * facilitando la identificación rápida en interfaces gráficas.
     *
     * @return String con el emoji correspondiente al tipo de archivo
     */
    public String getIcono() {
        switch (tipo) {
            case TEXTO:
                return "📄";
            case IMAGEN:
                return "🖼️";
            case VIDEO:
                return "🎥";
            case AUDIO:
                return "🎵";
            case DOCUMENTO:
                return "📋";
            case EJECUTABLE:
                return "⚙️";
            case SCRIPT:
                return "📜";
            case CARPETA:
                return "📁";
            default:
                return "📄"; // Ícono por defecto para archivos no reconocidos
        }
    }

    // === GETTERS Y SETTERS ===

    /**
     * Obtiene el nombre del archivo
     * @return nombre del archivo
     */
    public String getNombre() { return nombre; }

    /**
     * Establece el nombre del archivo
     * @param nombre nuevo nombre del archivo
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Obtiene la extensión del archivo
     * @return extensión del archivo (con punto inicial)
     */
    public String getExtension() { return extension; }

    /**
     * Obtiene el tamaño del archivo en bytes
     * @return tamaño en bytes
     */
    public int getTamaño() { return tamaño; }

    /**
     * Obtiene el contenido del archivo
     * @return contenido como string
     */
    public String getContenido() { return contenido; }

    /**
     * Establece el contenido del archivo directamente
     * @param contenido nuevo contenido
     */
    public void setContenido(String contenido) { this.contenido = contenido; }

    /**
     * Obtiene la fecha de creación del archivo
     * @return LocalDateTime con la fecha de creación
     */
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    /**
     * Obtiene la fecha de última modificación
     * @return LocalDateTime con la fecha de modificación
     */
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }

    /**
     * Obtiene el tipo de archivo
     * @return TipoArchivo enum con el tipo
     */
    public TipoArchivo getTipo() { return tipo; }

    /**
     * Establece el tipo de archivo manualmente
     * @param tipo nuevo tipo de archivo
     */
    public void setTipo(TipoArchivo tipo) { this.tipo = tipo; }

    /**
     * Obtiene los permisos del archivo
     * @return objeto Permisos con la configuración actual
     */
    public Permisos getPermisos() { return permisos; }

    /**
     * Establece nuevos permisos para el archivo
     * @param permisos nueva configuración de permisos
     */
    public void setPermisos(Permisos permisos) { this.permisos = permisos; }

    /**
     * Obtiene el propietario del archivo
     * @return nombre del usuario propietario
     */
    public String getPropietario() { return propietario; }

    /**
     * Establece el propietario del archivo
     * @param propietario nuevo propietario
     */
    public void setPropietario(String propietario) { this.propietario = propietario; }

    /**
     * Obtiene la ruta del directorio padre
     * @return ruta del directorio que contiene el archivo
     */
    public String getRuta() { return ruta; }

    /**
     * Establece la ruta del archivo
     * @param ruta nueva ruta del archivo
     */
    public void setRuta(String ruta) { this.ruta = ruta; }

    /**
     * Obtiene la ruta completa del archivo
     *
     * Combina la ruta del directorio padre con el nombre del archivo
     * para formar la ruta absoluta completa.
     *
     * @return String con la ruta completa del archivo
     */
    public String getRutaCompleta() {
        return ruta.endsWith("/") ? ruta + nombre : ruta + "/" + nombre;
    }

    // === MÉTODOS HEREDADOS DE OBJECT ===

    /**
     * Representación textual del archivo
     *
     * Genera una cadena informativa que incluye el ícono, nombre,
     * tamaño, permisos y propietario del archivo.
     *
     * @return String con información formateada del archivo
     */
    @Override
    public String toString() {
        return String.format("%s %s [%s] %s - %s",
                getIcono(), nombre, getTamañoFormateado(),
                permisos.toString(), propietario);
    }

    /**
     * Compara dos archivos por igualdad
     *
     * Dos archivos se consideran iguales si tienen el mismo nombre
     * y están en la misma ruta (misma ubicación en el sistema).
     *
     * @param obj Objeto a comparar
     * @return true si los archivos son iguales
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Archivo archivo = (Archivo) obj;
        return nombre.equals(archivo.nombre) && ruta.equals(archivo.ruta);
    }

    /**
     * Genera código hash para el archivo
     *
     * Utiliza la combinación de nombre y ruta para generar un hash único,
     * consistente con el método equals().
     *
     * @return int código hash del archivo
     */
    @Override
    public int hashCode() {
        return (nombre + ruta).hashCode();
    }
}