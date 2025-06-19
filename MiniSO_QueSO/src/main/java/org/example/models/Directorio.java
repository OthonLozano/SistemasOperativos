package org.example.models;

import java.time.LocalDateTime;
import java.util.*;

public class Directorio {
    private String nombre;
    private String ruta;
    private String propietario;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Archivo.Permisos permisos;
    private Directorio padre;
    private Map<String, Directorio> subdirectorios;
    private Map<String, Archivo> archivos;

    // Constructor principal
    public Directorio(String nombre, String ruta, String propietario) {
        this.nombre = nombre;
        this.ruta = ruta;
        this.propietario = propietario;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
        this.permisos = new Archivo.Permisos(true, true, true); // rwx por defecto
        this.padre = null;
        this.subdirectorios = new HashMap<>();
        this.archivos = new HashMap<>();
    }

    // Constructor para directorio raíz
    public Directorio(String propietario) {
        this("/", "/", propietario);
    }

    // Operaciones con subdirectorios
    public Directorio crearSubdirectorio(String nombreDir) {
        if (subdirectorios.containsKey(nombreDir)) {
            throw new IllegalArgumentException("El directorio '" + nombreDir + "' ya existe");
        }

        if (!permisos.isEscritura()) {
            throw new SecurityException("No tiene permisos de escritura en el directorio: " + nombre);
        }

        String nuevaRuta = ruta.endsWith("/") ? ruta + nombreDir : ruta + "/" + nombreDir;
        Directorio nuevoDir = new Directorio(nombreDir, nuevaRuta, propietario);
        nuevoDir.setPadre(this);

        subdirectorios.put(nombreDir, nuevoDir);
        actualizarFechaModificacion();

        return nuevoDir;
    }

    public boolean eliminarSubdirectorio(String nombreDir) {
        if (!permisos.isEscritura()) {
            throw new SecurityException("No tiene permisos de escritura en el directorio: " + nombre);
        }

        Directorio dirAEliminar = subdirectorios.get(nombreDir);
        if (dirAEliminar != null) {
            // Verificar que esté vacío o forzar eliminación
            if (!dirAEliminar.estaVacio()) {
                // Para simplificar, permitimos eliminar directorios no vacíos
                dirAEliminar.vaciar();
            }

            subdirectorios.remove(nombreDir);
            actualizarFechaModificacion();
            return true;
        }
        return false;
    }

    public Directorio getSubdirectorio(String nombreDir) {
        return subdirectorios.get(nombreDir);
    }

    public List<Directorio> getSubdirectorios() {
        return new ArrayList<>(subdirectorios.values());
    }

    // Operaciones con archivos
    public Archivo crearArchivo(String nombreArchivo, String contenido) {
        if (archivos.containsKey(nombreArchivo)) {
            throw new IllegalArgumentException("El archivo '" + nombreArchivo + "' ya existe");
        }

        if (!permisos.isEscritura()) {
            throw new SecurityException("No tiene permisos de escritura en el directorio: " + nombre);
        }

        Archivo nuevoArchivo = new Archivo(nombreArchivo, contenido, propietario, ruta);
        archivos.put(nombreArchivo, nuevoArchivo);
        actualizarFechaModificacion();

        return nuevoArchivo;
    }

    public Archivo crearArchivo(String nombreArchivo) {
        return crearArchivo(nombreArchivo, "");
    }

    public boolean eliminarArchivo(String nombreArchivo) {
        if (!permisos.isEscritura()) {
            throw new SecurityException("No tiene permisos de escritura en el directorio: " + nombre);
        }

        if (archivos.remove(nombreArchivo) != null) {
            actualizarFechaModificacion();
            return true;
        }
        return false;
    }

    public Archivo getArchivo(String nombreArchivo) {
        return archivos.get(nombreArchivo);
    }

    public List<Archivo> getArchivos() {
        return new ArrayList<>(archivos.values());
    }

    // Operaciones de navegación
    public Directorio navegarA(String rutaRelativa) {
        if (rutaRelativa.equals(".")) {
            return this;
        }

        if (rutaRelativa.equals("..")) {
            return padre != null ? padre : this;
        }

        if (rutaRelativa.startsWith("/")) {
            // Ruta absoluta - buscar desde la raíz
            return getRaiz().navegarA(rutaRelativa.substring(1));
        }

        // Ruta relativa
        String[] partes = rutaRelativa.split("/");
        Directorio actual = this;

        for (String parte : partes) {
            if (parte.isEmpty()) continue;

            if (parte.equals("..")) {
                actual = actual.padre != null ? actual.padre : actual;
            } else {
                actual = actual.subdirectorios.get(parte);
                if (actual == null) {
                    return null; // Directorio no encontrado
                }
            }
        }

        return actual;
    }

    public Directorio getRaiz() {
        Directorio actual = this;
        while (actual.padre != null) {
            actual = actual.padre;
        }
        return actual;
    }

    // Operaciones de búsqueda
    public List<Archivo> buscarArchivos(String patron) {
        List<Archivo> resultados = new ArrayList<>();
        buscarArchivosRecursivo(patron.toLowerCase(), resultados);
        return resultados;
    }

    private void buscarArchivosRecursivo(String patron, List<Archivo> resultados) {
        // Buscar en archivos del directorio actual
        for (Archivo archivo : archivos.values()) {
            if (archivo.getNombre().toLowerCase().contains(patron)) {
                resultados.add(archivo);
            }
        }

        // Buscar recursivamente en subdirectorios
        for (Directorio subdir : subdirectorios.values()) {
            subdir.buscarArchivosRecursivo(patron, resultados);
        }
    }

    public List<Directorio> buscarDirectorios(String patron) {
        List<Directorio> resultados = new ArrayList<>();
        buscarDirectoriosRecursivo(patron.toLowerCase(), resultados);
        return resultados;
    }

    private void buscarDirectoriosRecursivo(String patron, List<Directorio> resultados) {
        // Buscar en subdirectorios del directorio actual
        for (Directorio subdir : subdirectorios.values()) {
            if (subdir.nombre.toLowerCase().contains(patron)) {
                resultados.add(subdir);
            }
            subdir.buscarDirectoriosRecursivo(patron, resultados);
        }
    }

    // Operaciones de información
    public boolean estaVacio() {
        return archivos.isEmpty() && subdirectorios.isEmpty();
    }

    public int getNumeroArchivos() {
        return archivos.size();
    }

    public int getNumeroSubdirectorios() {
        return subdirectorios.size();
    }

    public int getTamañoTotal() {
        int tamaño = 0;

        // Sumar tamaño de archivos
        for (Archivo archivo : archivos.values()) {
            tamaño += archivo.getTamaño();
        }

        // Sumar tamaño de subdirectorios recursivamente
        for (Directorio subdir : subdirectorios.values()) {
            tamaño += subdir.getTamañoTotal();
        }

        return tamaño;
    }

    public String getTamañoFormateado() {
        int tamaño = getTamañoTotal();
        if (tamaño < 1024) {
            return tamaño + " B";
        } else if (tamaño < 1024 * 1024) {
            return String.format("%.1f KB", tamaño / 1024.0);
        } else {
            return String.format("%.1f MB", tamaño / (1024.0 * 1024.0));
        }
    }

    public List<String> listarContenido() {
        List<String> contenido = new ArrayList<>();

        // Agregar subdirectorios primero
        List<String> nombresSubdirs = new ArrayList<>(subdirectorios.keySet());
        Collections.sort(nombresSubdirs);
        for (String nombreSubdir : nombresSubdirs) {
            contenido.add("📁 " + nombreSubdir + "/");
        }

        // Agregar archivos
        List<String> nombresArchivos = new ArrayList<>(archivos.keySet());
        Collections.sort(nombresArchivos);
        for (String nombreArchivo : nombresArchivos) {
            Archivo archivo = archivos.get(nombreArchivo);
            contenido.add(archivo.getIcono() + " " + nombreArchivo);
        }

        return contenido;
    }

    public void vaciar() {
        if (!permisos.isEscritura()) {
            throw new SecurityException("No tiene permisos de escritura en el directorio: " + nombre);
        }

        archivos.clear();
        subdirectorios.clear();
        actualizarFechaModificacion();
    }

    // Operaciones de copia y movimiento
    public void copiarArchivo(Archivo archivo, Directorio destino) {
        if (!permisos.isLectura()) {
            throw new SecurityException("No tiene permisos de lectura en el directorio: " + nombre);
        }

        if (!destino.permisos.isEscritura()) {
            throw new SecurityException("No tiene permisos de escritura en el directorio destino: " + destino.nombre);
        }

        Archivo copia = archivo.copiar(destino.ruta);
        destino.archivos.put(archivo.getNombre(), copia);
        destino.actualizarFechaModificacion();
    }

    public void moverArchivo(String nombreArchivo, Directorio destino) {
        Archivo archivo = archivos.get(nombreArchivo);
        if (archivo != null) {
            copiarArchivo(archivo, destino);
            eliminarArchivo(nombreArchivo);
        }
    }

    // Métodos de utilidad
    private void actualizarFechaModificacion() {
        this.fechaModificacion = LocalDateTime.now();
        if (padre != null) {
            padre.actualizarFechaModificacion();
        }
    }

    public String getRutaCompleta() {
        return ruta;
    }

    public String getIcono() {
        return "📁";
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }

    public String getPropietario() { return propietario; }
    public void setPropietario(String propietario) { this.propietario = propietario; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }

    public Archivo.Permisos getPermisos() { return permisos; }
    public void setPermisos(Archivo.Permisos permisos) { this.permisos = permisos; }

    public Directorio getPadre() { return padre; }
    public void setPadre(Directorio padre) { this.padre = padre; }

    @Override
    public String toString() {
        return String.format("📁 %s [%d archivos, %d dirs] %s - %s",
                nombre, getNumeroArchivos(), getNumeroSubdirectorios(),
                getTamañoFormateado(), propietario);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Directorio that = (Directorio) obj;
        return nombre.equals(that.nombre) && ruta.equals(that.ruta);
    }

    @Override
    public int hashCode() {
        return (nombre + ruta).hashCode();
    }
}
