package org.example.models;

import java.time.LocalDateTime;

public class Archivo {
    private String nombre;
    private String extension;
    private int tamaño; // en bytes
    private String contenido;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private TipoArchivo tipo;
    private Permisos permisos;
    private String propietario;
    private String ruta;

    public enum TipoArchivo {
        TEXTO(".txt"),
        IMAGEN(".jpg"),
        VIDEO(".mp4"),
        AUDIO(".mp3"),
        DOCUMENTO(".doc"),
        EJECUTABLE(".exe"),
        SCRIPT(".sh"),
        CARPETA(""),
        OTRO("");

        private final String extensionTipica;

        TipoArchivo(String extensionTipica) {
            this.extensionTipica = extensionTipica;
        }

        public String getExtensionTipica() {
            return extensionTipica;
        }
    }

    public static class Permisos {
        private boolean lectura;
        private boolean escritura;
        private boolean ejecucion;

        public Permisos(boolean lectura, boolean escritura, boolean ejecucion) {
            this.lectura = lectura;
            this.escritura = escritura;
            this.ejecucion = ejecucion;
        }

        // Constructor por defecto (lectura y escritura)
        public Permisos() {
            this(true, true, false);
        }

        // Getters y Setters
        public boolean isLectura() { return lectura; }
        public void setLectura(boolean lectura) { this.lectura = lectura; }

        public boolean isEscritura() { return escritura; }
        public void setEscritura(boolean escritura) { this.escritura = escritura; }

        public boolean isEjecucion() { return ejecucion; }
        public void setEjecucion(boolean ejecucion) { this.ejecucion = ejecucion; }

        @Override
        public String toString() {
            return (lectura ? "r" : "-") +
                    (escritura ? "w" : "-") +
                    (ejecucion ? "x" : "-");
        }
    }

    // Constructor principal
    public Archivo(String nombre, String contenido, String propietario, String ruta) {
        this.nombre = nombre;
        this.contenido = contenido != null ? contenido : "";
        this.propietario = propietario;
        this.ruta = ruta;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
        this.permisos = new Permisos();

        // Determinar extensión y tipo
        determinarExtensionYTipo();

        // Calcular tamaño
        this.tamaño = this.contenido.length();
    }

    // Constructor simplificado
    public Archivo(String nombre, String propietario) {
        this(nombre, "", propietario, "/");
    }

    private void determinarExtensionYTipo() {
        int puntoIndex = nombre.lastIndexOf('.');

        if (puntoIndex > 0 && puntoIndex < nombre.length() - 1) {
            this.extension = nombre.substring(puntoIndex);
            this.tipo = determinarTipoPorExtension(extension);
        } else {
            this.extension = "";
            this.tipo = TipoArchivo.OTRO;
        }
    }

    private TipoArchivo determinarTipoPorExtension(String ext) {
        ext = ext.toLowerCase();

        switch (ext) {
            case ".txt":
            case ".md":
            case ".log":
                return TipoArchivo.TEXTO;
            case ".jpg":
            case ".png":
            case ".gif":
            case ".bmp":
                return TipoArchivo.IMAGEN;
            case ".mp4":
            case ".avi":
            case ".mov":
                return TipoArchivo.VIDEO;
            case ".mp3":
            case ".wav":
            case ".flac":
                return TipoArchivo.AUDIO;
            case ".doc":
            case ".docx":
            case ".pdf":
                return TipoArchivo.DOCUMENTO;
            case ".exe":
            case ".msi":
                return TipoArchivo.EJECUTABLE;
            case ".sh":
            case ".bat":
            case ".cmd":
                return TipoArchivo.SCRIPT;
            default:
                return TipoArchivo.OTRO;
        }
    }

    // Métodos de operación
    public void modificarContenido(String nuevoContenido) {
        if (permisos.isEscritura()) {
            this.contenido = nuevoContenido;
            this.tamaño = nuevoContenido.length();
            this.fechaModificacion = LocalDateTime.now();
        } else {
            throw new SecurityException("No tiene permisos de escritura en el archivo: " + nombre);
        }
    }

    public String leerContenido() {
        if (permisos.isLectura()) {
            return contenido;
        } else {
            throw new SecurityException("No tiene permisos de lectura en el archivo: " + nombre);
        }
    }

    public void ejecutar() {
        if (permisos.isEjecucion()) {
            // Simulación de ejecución
            System.out.println("Ejecutando archivo: " + nombre);
        } else {
            throw new SecurityException("No tiene permisos de ejecución en el archivo: " + nombre);
        }
    }

    public void renombrar(String nuevoNombre) {
        this.nombre = nuevoNombre;
        this.fechaModificacion = LocalDateTime.now();
        determinarExtensionYTipo();
    }

    public Archivo copiar(String nuevaRuta) {
        Archivo copia = new Archivo(this.nombre, this.contenido, this.propietario, nuevaRuta);
        copia.setPermisos(new Permisos(this.permisos.lectura, this.permisos.escritura, this.permisos.ejecucion));
        return copia;
    }

    // Métodos de información
    public String getNombreCompleto() {
        return nombre;
    }

    public String getNombreSinExtension() {
        int puntoIndex = nombre.lastIndexOf('.');
        return puntoIndex > 0 ? nombre.substring(0, puntoIndex) : nombre;
    }

    public String getTamañoFormateado() {
        if (tamaño < 1024) {
            return tamaño + " B";
        } else if (tamaño < 1024 * 1024) {
            return String.format("%.1f KB", tamaño / 1024.0);
        } else {
            return String.format("%.1f MB", tamaño / (1024.0 * 1024.0));
        }
    }

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
                return "📄";
        }
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getExtension() { return extension; }

    public int getTamaño() { return tamaño; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }

    public TipoArchivo getTipo() { return tipo; }
    public void setTipo(TipoArchivo tipo) { this.tipo = tipo; }

    public Permisos getPermisos() { return permisos; }
    public void setPermisos(Permisos permisos) { this.permisos = permisos; }

    public String getPropietario() { return propietario; }
    public void setPropietario(String propietario) { this.propietario = propietario; }

    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }

    public String getRutaCompleta() {
        return ruta.endsWith("/") ? ruta + nombre : ruta + "/" + nombre;
    }

    @Override
    public String toString() {
        return String.format("%s %s [%s] %s - %s",
                getIcono(), nombre, getTamañoFormateado(),
                permisos.toString(), propietario);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Archivo archivo = (Archivo) obj;
        return nombre.equals(archivo.nombre) && ruta.equals(archivo.ruta);
    }

    @Override
    public int hashCode() {
        return (nombre + ruta).hashCode();
    }
}