package org.example.models;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Modelo de datos que representa un directorio en el sistema de archivos MiniSO
 *
 * Esta clase implementa la funcionalidad completa de un directorio en el sistema
 * de archivos jerárquico simulado, proporcionando:
 *
 * - Gestión de archivos y subdirectorios anidados
 * - Sistema de permisos integrado con validaciones de seguridad
 * - Navegación por rutas absolutas y relativas
 * - Operaciones de búsqueda recursiva en toda la jerarquía
 * - Cálculo automático de tamaños y estadísticas
 * - Operaciones de copia y movimiento con validación de permisos
 * - Mantenimiento automático de metadatos temporales
 *
 * Los directorios forman una estructura de árbol donde cada nodo puede contener
 * tanto archivos como otros directorios, implementando un modelo jerárquico
 * similar a los sistemas de archivos Unix/Linux.
 */
public class Directorio {

    // === ATRIBUTOS PRINCIPALES DEL DIRECTORIO ===

    /** Nombre del directorio (sin ruta) */
    private String nombre;

    /** Ruta completa del directorio en el sistema de archivos */
    private String ruta;

    /** Usuario propietario del directorio */
    private String propietario;

    /** Fecha y hora de creación del directorio */
    private LocalDateTime fechaCreacion;

    /** Fecha y hora de la última modificación */
    private LocalDateTime fechaModificacion;

    /** Permisos de acceso al directorio (lectura, escritura, ejecución) */
    private Archivo.Permisos permisos;

    /** Referencia al directorio padre (null para el directorio raíz) */
    private Directorio padre;

    /** Mapa de subdirectorios contenidos, indexados por nombre */
    private Map<String, Directorio> subdirectorios;

    /** Mapa de archivos contenidos, indexados por nombre */
    private Map<String, Archivo> archivos;

    // === CONSTRUCTORES ===

    /**
     * Constructor principal para crear un directorio completo
     *
     * Inicializa un directorio con todos sus metadatos y estructuras internas.
     * Establece permisos completos (rwx) por defecto para facilitar la operación
     * en el entorno académico.
     *
     * @param nombre Nombre del directorio (sin ruta)
     * @param ruta Ruta completa donde se ubica el directorio
     * @param propietario Usuario propietario del directorio
     */
    public Directorio(String nombre, String ruta, String propietario) {
        this.nombre = nombre;
        this.ruta = ruta;
        this.propietario = propietario;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
        this.permisos = new Archivo.Permisos(true, true, true); // rwx por defecto
        this.padre = null; // Se establece externamente cuando se agrega a un padre
        this.subdirectorios = new HashMap<>();
        this.archivos = new HashMap<>();
    }

    /**
     * Constructor simplificado para el directorio raíz
     *
     * Crea el directorio raíz del sistema de archivos con la ruta "/" y
     * establece al propietario especificado como dueño del sistema.
     *
     * @param propietario Usuario que será propietario del sistema de archivos
     */
    public Directorio(String propietario) {
        this("/", "/", propietario);
    }

    // === OPERACIONES CON SUBDIRECTORIOS ===

    /**
     * Crea un nuevo subdirectorio dentro de este directorio
     *
     * Valida permisos de escritura, verifica que no exista un directorio
     * con el mismo nombre y establece la relación padre-hijo apropiada.
     *
     * @param nombreDir Nombre del nuevo subdirectorio
     * @return Directorio recién creado
     * @throws IllegalArgumentException si ya existe un directorio con ese nombre
     * @throws SecurityException si no tiene permisos de escritura
     */
    public Directorio crearSubdirectorio(String nombreDir) {
        // Validar que no exista ya un directorio con ese nombre
        if (subdirectorios.containsKey(nombreDir)) {
            throw new IllegalArgumentException("El directorio '" + nombreDir + "' ya existe");
        }

        // Verificar permisos de escritura
        if (!permisos.isEscritura()) {
            throw new SecurityException("No tiene permisos de escritura en el directorio: " + nombre);
        }

        // Construir ruta del nuevo directorio
        String nuevaRuta = ruta.endsWith("/") ? ruta + nombreDir : ruta + "/" + nombreDir;

        // Crear el nuevo directorio
        Directorio nuevoDir = new Directorio(nombreDir, nuevaRuta, propietario);
        nuevoDir.setPadre(this); // Establecer relación padre-hijo

        // Agregar a la colección de subdirectorios
        subdirectorios.put(nombreDir, nuevoDir);
        actualizarFechaModificacion(); // Actualizar timestamp

        return nuevoDir;
    }

    /**
     * Elimina un subdirectorio existente
     *
     * Valida permisos y puede forzar la eliminación de directorios no vacíos
     * para simplificar la operación en el entorno de simulación.
     *
     * @param nombreDir Nombre del subdirectorio a eliminar
     * @return true si se eliminó exitosamente, false si no se encontró
     * @throws SecurityException si no tiene permisos de escritura
     */
    public boolean eliminarSubdirectorio(String nombreDir) {
        // Verificar permisos de escritura
        if (!permisos.isEscritura()) {
            throw new SecurityException("No tiene permisos de escritura en el directorio: " + nombre);
        }

        Directorio dirAEliminar = subdirectorios.get(nombreDir);
        if (dirAEliminar != null) {
            // Verificar si está vacío, si no, vaciarlo para simplificar la operación
            if (!dirAEliminar.estaVacio()) {
                dirAEliminar.vaciar(); // Eliminar todo el contenido recursivamente
            }

            // Remover de la colección
            subdirectorios.remove(nombreDir);
            actualizarFechaModificacion();
            return true;
        }
        return false;
    }

    /**
     * Obtiene un subdirectorio específico por nombre
     *
     * @param nombreDir Nombre del subdirectorio buscado
     * @return Directorio encontrado o null si no existe
     */
    public Directorio getSubdirectorio(String nombreDir) {
        return subdirectorios.get(nombreDir);
    }

    /**
     * Obtiene una lista de todos los subdirectorios
     *
     * Retorna una copia de la lista para evitar modificaciones externas
     * accidentales de la estructura interna.
     *
     * @return List<Directorio> copia de todos los subdirectorios
     */
    public List<Directorio> getSubdirectorios() {
        return new ArrayList<>(subdirectorios.values());
    }

    // === OPERACIONES CON ARCHIVOS ===

    /**
     * Crea un nuevo archivo con contenido en este directorio
     *
     * Valida permisos de escritura y unicidad de nombres antes de crear
     * el archivo con el contenido especificado.
     *
     * @param nombreArchivo Nombre del archivo a crear
     * @param contenido Contenido inicial del archivo
     * @return Archivo recién creado
     * @throws IllegalArgumentException si ya existe un archivo con ese nombre
     * @throws SecurityException si no tiene permisos de escritura
     */
    public Archivo crearArchivo(String nombreArchivo, String contenido) {
        // Validar que no exista ya un archivo con ese nombre
        if (archivos.containsKey(nombreArchivo)) {
            throw new IllegalArgumentException("El archivo '" + nombreArchivo + "' ya existe");
        }

        // Verificar permisos de escritura
        if (!permisos.isEscritura()) {
            throw new SecurityException("No tiene permisos de escritura en el directorio: " + nombre);
        }

        // Crear el nuevo archivo
        Archivo nuevoArchivo = new Archivo(nombreArchivo, contenido, propietario, ruta);
        archivos.put(nombreArchivo, nuevoArchivo);
        actualizarFechaModificacion();

        return nuevoArchivo;
    }

    /**
     * Crea un archivo vacío en este directorio
     *
     * Versión simplificada del método de creación que crea un archivo
     * sin contenido inicial.
     *
     * @param nombreArchivo Nombre del archivo a crear
     * @return Archivo recién creado (vacío)
     */
    public Archivo crearArchivo(String nombreArchivo) {
        return crearArchivo(nombreArchivo, "");
    }

    /**
     * Elimina un archivo específico del directorio
     *
     * Valida permisos de escritura antes de proceder con la eliminación.
     *
     * @param nombreArchivo Nombre del archivo a eliminar
     * @return true si se eliminó exitosamente, false si no se encontró
     * @throws SecurityException si no tiene permisos de escritura
     */
    public boolean eliminarArchivo(String nombreArchivo) {
        // Verificar permisos de escritura
        if (!permisos.isEscritura()) {
            throw new SecurityException("No tiene permisos de escritura en el directorio: " + nombre);
        }

        // Intentar eliminar el archivo
        if (archivos.remove(nombreArchivo) != null) {
            actualizarFechaModificacion();
            return true;
        }
        return false;
    }

    /**
     * Obtiene un archivo específico por nombre
     *
     * @param nombreArchivo Nombre del archivo buscado
     * @return Archivo encontrado o null si no existe
     */
    public Archivo getArchivo(String nombreArchivo) {
        return archivos.get(nombreArchivo);
    }

    /**
     * Obtiene una lista de todos los archivos del directorio
     *
     * Retorna una copia de la lista para proteger la estructura interna.
     *
     * @return List<Archivo> copia de todos los archivos
     */
    public List<Archivo> getArchivos() {
        return new ArrayList<>(archivos.values());
    }

    // === OPERACIONES DE NAVEGACIÓN ===

    /**
     * Navega a un directorio usando una ruta relativa o absoluta
     *
     * Implementa navegación completa con soporte para:
     * - "." (directorio actual)
     * - ".." (directorio padre)
     * - Rutas relativas (ej: "subdir/otro")
     * - Rutas absolutas (ej: "/home/usuario")
     *
     * @param rutaRelativa Ruta hacia el directorio de destino
     * @return Directorio de destino o null si no se encuentra
     */
    public Directorio navegarA(String rutaRelativa) {
        // Directorio actual
        if (rutaRelativa.equals(".")) {
            return this;
        }

        // Directorio padre
        if (rutaRelativa.equals("..")) {
            return padre != null ? padre : this; // Si es raíz, regresar a sí mismo
        }

        // Ruta absoluta - comenzar desde la raíz
        if (rutaRelativa.startsWith("/")) {
            return getRaiz().navegarA(rutaRelativa.substring(1));
        }

        // Ruta relativa - navegar paso a paso
        String[] partes = rutaRelativa.split("/");
        Directorio actual = this;

        for (String parte : partes) {
            if (parte.isEmpty()) continue; // Ignorar partes vacías

            if (parte.equals("..")) {
                // Subir un nivel
                actual = actual.padre != null ? actual.padre : actual;
            } else {
                // Bajar a subdirectorio
                actual = actual.subdirectorios.get(parte);
                if (actual == null) {
                    return null; // Directorio no encontrado
                }
            }
        }

        return actual;
    }

    /**
     * Obtiene el directorio raíz del sistema de archivos
     *
     * Recorre la jerarquía hacia arriba hasta encontrar el directorio
     * que no tiene padre (el directorio raíz).
     *
     * @return Directorio raíz del sistema
     */
    public Directorio getRaiz() {
        Directorio actual = this;
        while (actual.padre != null) {
            actual = actual.padre;
        }
        return actual;
    }

    // === OPERACIONES DE BÚSQUEDA ===

    /**
     * Busca archivos por patrón de nombre de forma recursiva
     *
     * Realiza una búsqueda exhaustiva en todo el subárbol comenzando
     * desde este directorio, incluyendo todos los subdirectorios.
     *
     * @param patron Patrón de búsqueda (insensible a mayúsculas/minúsculas)
     * @return List<Archivo> archivos que coinciden con el patrón
     */
    public List<Archivo> buscarArchivos(String patron) {
        List<Archivo> resultados = new ArrayList<>();
        buscarArchivosRecursivo(patron.toLowerCase(), resultados);
        return resultados;
    }

    /**
     * Método auxiliar recursivo para búsqueda de archivos
     *
     * Implementa la lógica de búsqueda recursiva que examina el directorio
     * actual y todos sus subdirectorios en profundidad.
     *
     * @param patron Patrón de búsqueda en minúsculas
     * @param resultados Lista donde acumular los resultados encontrados
     */
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

    /**
     * Busca directorios por patrón de nombre de forma recursiva
     *
     * Similar a la búsqueda de archivos, pero enfocada en encontrar
     * directorios que coincidan con el patrón especificado.
     *
     * @param patron Patrón de búsqueda (insensible a mayúsculas/minúsculas)
     * @return List<Directorio> directorios que coinciden con el patrón
     */
    public List<Directorio> buscarDirectorios(String patron) {
        List<Directorio> resultados = new ArrayList<>();
        buscarDirectoriosRecursivo(patron.toLowerCase(), resultados);
        return resultados;
    }

    /**
     * Método auxiliar recursivo para búsqueda de directorios
     *
     * @param patron Patrón de búsqueda en minúsculas
     * @param resultados Lista donde acumular los directorios encontrados
     */
    private void buscarDirectoriosRecursivo(String patron, List<Directorio> resultados) {
        // Buscar en subdirectorios del directorio actual
        for (Directorio subdir : subdirectorios.values()) {
            if (subdir.nombre.toLowerCase().contains(patron)) {
                resultados.add(subdir);
            }
            // Continuar búsqueda recursiva
            subdir.buscarDirectoriosRecursivo(patron, resultados);
        }
    }

    // === OPERACIONES DE INFORMACIÓN Y ESTADÍSTICAS ===

    /**
     * Verifica si el directorio está completamente vacío
     *
     * @return true si no contiene archivos ni subdirectorios
     */
    public boolean estaVacio() {
        return archivos.isEmpty() && subdirectorios.isEmpty();
    }

    /**
     * Obtiene el número de archivos en el directorio actual
     *
     * No incluye archivos de subdirectorios (no es recursivo).
     *
     * @return int número de archivos directos
     */
    public int getNumeroArchivos() {
        return archivos.size();
    }

    /**
     * Obtiene el número de subdirectorios inmediatos
     *
     * No incluye subdirectorios anidados (no es recursivo).
     *
     * @return int número de subdirectorios directos
     */
    public int getNumeroSubdirectorios() {
        return subdirectorios.size();
    }

    /**
     * Calcula el tamaño total del directorio de forma recursiva
     *
     * Suma el tamaño de todos los archivos contenidos en este directorio
     * y en todos sus subdirectorios, proporcionando el tamaño total
     * del subárbol completo.
     *
     * @return int tamaño total en bytes
     */
    public int getTamañoTotal() {
        int tamaño = 0;

        // Sumar tamaño de archivos directos
        for (Archivo archivo : archivos.values()) {
            tamaño += archivo.getTamaño();
        }

        // Sumar tamaño de subdirectorios recursivamente
        for (Directorio subdir : subdirectorios.values()) {
            tamaño += subdir.getTamañoTotal();
        }

        return tamaño;
    }

    /**
     * Obtiene el tamaño total formateado en unidades legibles
     *
     * Convierte el tamaño en bytes a una representación más amigable
     * usando las unidades apropiadas (B, KB, MB).
     *
     * @return String con el tamaño formateado
     */
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

    /**
     * Lista el contenido del directorio en formato legible
     *
     * Genera una lista ordenada del contenido, mostrando primero los
     * subdirectorios y luego los archivos, cada uno con su ícono
     * representativo.
     *
     * @return List<String> contenido formateado con iconos
     */
    public List<String> listarContenido() {
        List<String> contenido = new ArrayList<>();

        // Agregar subdirectorios primero (ordenados alfabéticamente)
        List<String> nombresSubdirs = new ArrayList<>(subdirectorios.keySet());
        Collections.sort(nombresSubdirs);
        for (String nombreSubdir : nombresSubdirs) {
            contenido.add("📁 " + nombreSubdir + "/");
        }

        // Agregar archivos (ordenados alfabéticamente)
        List<String> nombresArchivos = new ArrayList<>(archivos.keySet());
        Collections.sort(nombresArchivos);
        for (String nombreArchivo : nombresArchivos) {
            Archivo archivo = archivos.get(nombreArchivo);
            contenido.add(archivo.getIcono() + " " + nombreArchivo);
        }

        return contenido;
    }

    /**
     * Vacía completamente el directorio
     *
     * Elimina todos los archivos y subdirectorios contenidos,
     * requiere permisos de escritura.
     *
     * @throws SecurityException si no tiene permisos de escritura
     */
    public void vaciar() {
        if (!permisos.isEscritura()) {
            throw new SecurityException("No tiene permisos de escritura en el directorio: " + nombre);
        }

        archivos.clear();
        subdirectorios.clear();
        actualizarFechaModificacion();
    }

    // === OPERACIONES DE COPIA Y MOVIMIENTO ===

    /**
     * Copia un archivo a otro directorio
     *
     * Valida permisos tanto en el directorio origen (lectura) como en el
     * destino (escritura) antes de realizar la operación de copia.
     *
     * @param archivo Archivo a copiar
     * @param destino Directorio de destino
     * @throws SecurityException si no tiene permisos apropiados
     */
    public void copiarArchivo(Archivo archivo, Directorio destino) {
        // Verificar permisos de lectura en origen
        if (!permisos.isLectura()) {
            throw new SecurityException("No tiene permisos de lectura en el directorio: " + nombre);
        }

        // Verificar permisos de escritura en destino
        if (!destino.permisos.isEscritura()) {
            throw new SecurityException("No tiene permisos de escritura en el directorio destino: " + destino.nombre);
        }

        // Realizar la copia
        Archivo copia = archivo.copiar(destino.ruta);
        destino.archivos.put(archivo.getNombre(), copia);
        destino.actualizarFechaModificacion();
    }

    /**
     * Mueve un archivo a otro directorio
     *
     * Implementa movimiento como una operación de copia seguida
     * de eliminación del archivo original.
     *
     * @param nombreArchivo Nombre del archivo a mover
     * @param destino Directorio de destino
     */
    public void moverArchivo(String nombreArchivo, Directorio destino) {
        Archivo archivo = archivos.get(nombreArchivo);
        if (archivo != null) {
            copiarArchivo(archivo, destino);  // Copiar al destino
            eliminarArchivo(nombreArchivo);   // Eliminar del origen
        }
    }

    // === MÉTODOS DE UTILIDAD INTERNA ===

    /**
     * Actualiza la fecha de modificación de forma recursiva
     *
     * Actualiza el timestamp de este directorio y propaga el cambio
     * hacia arriba en la jerarquía hasta llegar al directorio raíz.
     * Esto mantiene la coherencia temporal en todo el árbol.
     */
    private void actualizarFechaModificacion() {
        this.fechaModificacion = LocalDateTime.now();
        if (padre != null) {
            padre.actualizarFechaModificacion(); // Propagación recursiva
        }
    }

    /**
     * Obtiene la ruta completa del directorio
     *
     * @return String con la ruta absoluta
     */
    public String getRutaCompleta() {
        return ruta;
    }

    /**
     * Obtiene el emoji representativo de un directorio
     *
     * @return String con el emoji de carpeta
     */
    public String getIcono() {
        return "📁";
    }

    // === GETTERS Y SETTERS ===

    /**
     * Obtiene el nombre del directorio
     * @return nombre del directorio
     */
    public String getNombre() { return nombre; }

    /**
     * Establece el nombre del directorio
     * @param nombre nuevo nombre
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Obtiene la ruta del directorio
     * @return ruta completa
     */
    public String getRuta() { return ruta; }

    /**
     * Establece la ruta del directorio
     * @param ruta nueva ruta
     */
    public void setRuta(String ruta) { this.ruta = ruta; }

    /**
     * Obtiene el propietario del directorio
     * @return nombre del propietario
     */
    public String getPropietario() { return propietario; }

    /**
     * Establece el propietario del directorio
     * @param propietario nuevo propietario
     */
    public void setPropietario(String propietario) { this.propietario = propietario; }

    /**
     * Obtiene la fecha de creación
     * @return LocalDateTime de creación
     */
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    /**
     * Obtiene la fecha de modificación
     * @return LocalDateTime de última modificación
     */
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }

    /**
     * Obtiene los permisos del directorio
     * @return objeto Permisos actual
     */
    public Archivo.Permisos getPermisos() { return permisos; }

    /**
     * Establece nuevos permisos para el directorio
     * @param permisos nueva configuración de permisos
     */
    public void setPermisos(Archivo.Permisos permisos) { this.permisos = permisos; }

    /**
     * Obtiene el directorio padre
     * @return Directorio padre o null si es raíz
     */
    public Directorio getPadre() { return padre; }

    /**
     * Establece el directorio padre
     * @param padre nuevo directorio padre
     */
    public void setPadre(Directorio padre) { this.padre = padre; }

    // === MÉTODOS HEREDADOS DE OBJECT ===

    /**
     * Representación textual del directorio
     *
     * Genera una cadena informativa que incluye el ícono, nombre,
     * estadísticas de contenido, tamaño total y propietario.
     *
     * @return String con información formateada del directorio
     */
    @Override
    public String toString() {
        return String.format("📁 %s [%d archivos, %d dirs] %s - %s",
                nombre, getNumeroArchivos(), getNumeroSubdirectorios(),
                getTamañoFormateado(), propietario);
    }

    /**
     * Compara dos directorios por igualdad
     *
     * Dos directorios se consideran iguales si tienen el mismo nombre
     * y están en la misma ruta (misma ubicación en el sistema).
     *
     * @param obj Objeto a comparar
     * @return true si los directorios son iguales
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Directorio that = (Directorio) obj;
        return nombre.equals(that.nombre) && ruta.equals(that.ruta);
    }

    /**
     * Genera código hash para el directorio
     *
     * Utiliza la combinación de nombre y ruta para generar un hash único,
     * consistente con el método equals().
     *
     * @return int código hash del directorio
     */
    @Override
    public int hashCode() {
        return (nombre + ruta).hashCode();
    }
}