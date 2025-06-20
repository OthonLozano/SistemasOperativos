package org.example.core;

import org.example.models.Archivo;
import org.example.models.Directorio;
import java.util.*;

/**
 * Sistema de archivos virtual para el sistema operativo académico MiniSO.
 *
 * Esta clase implementa un sistema de archivos jerárquico que simula
 * la funcionalidad básica de sistemas de archivos reales como ext4, NTFS o FAT32.
 *
 * Características principales:
 * - Estructura jerárquica de directorios (árbol)
 * - Operaciones CRUD completas (Create, Read, Update, Delete)
 * - Navegación por rutas absolutas y relativas
 * - Búsqueda de archivos por nombre y tipo
 * - Validaciones de nombres y tamaños
 * - Configuración flexible del sistema
 *
 * El sistema sigue convenciones similares a Unix/Linux:
 * - Directorio raíz: "/"
 * - Directorio home: "~"
 * - Separador de rutas: "/"
 * - Estructura estándar: /home, /bin, /etc, /tmp, /usr
 */
public class SistemaArchivos {
    /** Directorio raíz del sistema de archivos (equivalente a "/" en Unix) */
    private Directorio raiz;

    /** Directorio donde se encuentra actualmente el usuario (working directory) */
    private Directorio directorioActual;

    /** Usuario propietario del sistema de archivos */
    private String usuarioActual;

    /** Configuración del sistema con parámetros ajustables */
    private Map<String, String> configuracion;

    /**
     * Constructor principal del sistema de archivos.
     *
     * Inicializa un sistema de archivos completo con estructura estándar
     * y configuración por defecto, similar a sistemas Unix/Linux.
     *
     * @param usuarioAdmin Nombre del usuario administrador/propietario
     */
    public SistemaArchivos(String usuarioAdmin) {
        this.usuarioActual = usuarioAdmin;
        this.configuracion = new HashMap<>();
        inicializarSistema();
    }

    /**
     * Inicializa el sistema de archivos con configuración y estructura básica.
     *
     * Crea el directorio raíz, establece el directorio actual, crea la
     * estructura de directorios estándar y configura parámetros por defecto.
     */
    private void inicializarSistema() {
        // Crear directorio raíz como punto de entrada del sistema
        raiz = new Directorio(usuarioActual);
        directorioActual = raiz; // Iniciar en la raíz

        // Crear estructura de directorios estándar del sistema
        crearEstructuraBasica();

        // Establecer configuración por defecto del sistema
        configuracion.put("max_file_size", "1048576"); // 1MB máximo por archivo
        configuracion.put("max_files_per_dir", "1000"); // Máximo 1000 archivos por directorio
        configuracion.put("case_sensitive", "false"); // No sensible a mayúsculas/minúsculas
    }

    /**
     * Crea la estructura básica de directorios del sistema operativo.
     *
     * Implementa la jerarquía estándar de Unix/Linux:
     * - /home: Directorios de usuarios
     * - /bin: Archivos ejecutables y scripts
     * - /etc: Archivos de configuración del sistema
     * - /tmp: Archivos temporales
     * - /usr: Programas y utilidades de usuario
     */
    private void crearEstructuraBasica() {
        try {
            // Crear directorios principales del sistema (nivel raíz)
            Directorio home = raiz.crearSubdirectorio("home");    // Directorios de usuarios
            Directorio bin = raiz.crearSubdirectorio("bin");      // Ejecutables del sistema
            Directorio etc = raiz.crearSubdirectorio("etc");      // Configuración del sistema
            Directorio tmp = raiz.crearSubdirectorio("tmp");      // Archivos temporales
            Directorio usr = raiz.crearSubdirectorio("usr");      // Programas de usuario

            // Crear directorio personal del usuario actual
            Directorio userHome = home.crearSubdirectorio(usuarioActual);

            // Crear subdirectorios comunes en el home del usuario
            userHome.crearSubdirectorio("Documentos");    // Documentos del usuario
            userHome.crearSubdirectorio("Descargas");     // Archivos descargados
            userHome.crearSubdirectorio("Escritorio");    // Archivos del escritorio
            userHome.crearSubdirectorio("Imágenes");      // Archivos de imagen
            userHome.crearSubdirectorio("Videos");        // Archivos de video

            // Crear archivo de bienvenida con información del sistema
            userHome.crearArchivo("bienvenida.txt",
                    "¡Bienvenido al MiniSO!\n\nEste es tu directorio personal.\n" +
                            "Aquí puedes crear y organizar tus archivos.\n\n" +
                            "Comandos básicos:\n" +
                            "- Crear archivo: Clic derecho > Nuevo archivo\n" +
                            "- Crear carpeta: Clic derecho > Nueva carpeta\n" +
                            "- Navegar: Doble clic en carpetas\n\n" +
                            "¡Disfruta explorando el sistema!"
            );

            // Crear archivo README en el directorio de Documentos
            userHome.getSubdirectorio("Documentos").crearArchivo("readme.md",
                    "# Mi Directorio de Documentos\n\n" +
                            "Este directorio está destinado para guardar documentos importantes.\n\n" +
                            "## Organización sugerida:\n" +
                            "- Proyectos/\n" +
                            "- Reportes/\n" +
                            "- Presentaciones/\n"
            );

            // Crear archivo de configuración del sistema en /etc
            etc.crearArchivo("system.conf",
                    "# Configuración del Sistema MiniSO\n" +
                            "version=1.0\n" +
                            "admin=" + usuarioActual + "\n" +
                            "max_users=10\n" +
                            "debug=false\n"
            );

            // Crear script de ejemplo en /bin
            bin.crearArchivo("hello.sh",
                    "#!/bin/bash\n" +
                            "echo 'Hola desde MiniSO!'\n" +
                            "echo 'Sistema operativo académico'\n"
            );

        } catch (Exception e) {
            // Manejo de errores durante la creación de la estructura
            System.err.println("Error creando estructura básica: " + e.getMessage());
        }
    }

    /**
     * Cambia el directorio actual a la ruta especificada.
     *
     * Soporta navegación con:
     * - Rutas absolutas: "/home/usuario"
     * - Rutas relativas: "Documentos/proyecto"
     * - Directorio raíz: "/"
     * - Directorio home: "~"
     *
     * @param ruta Ruta del directorio destino
     * @return true si el cambio fue exitoso, false si la ruta no existe
     */
    public boolean cambiarDirectorio(String ruta) {
        Directorio destino;

        // Manejo de rutas especiales
        if (ruta.equals("/")) {
            // Navegar al directorio raíz
            destino = raiz;
        } else if (ruta.equals("~")) {
            // Navegar al directorio home del usuario
            destino = getDirectorioHome();
        } else {
            // Navegar usando ruta relativa o absoluta
            destino = directorioActual.navegarA(ruta);
        }

        // Actualizar directorio actual si la navegación fue exitosa
        if (destino != null) {
            directorioActual = destino;
            return true;
        }
        return false; // Ruta no válida o no existe
    }

    /**
     * Obtiene el directorio home del usuario actual.
     *
     * @return Directorio home del usuario o raíz si no existe
     */
    public Directorio getDirectorioHome() {
        Directorio home = raiz.getSubdirectorio("home");
        return home != null ? home.getSubdirectorio(usuarioActual) : raiz;
    }

    /**
     * Obtiene la ruta completa del directorio actual.
     *
     * @return Ruta absoluta del directorio donde se encuentra el usuario
     */
    public String getRutaActual() {
        return directorioActual.getRutaCompleta();
    }

    /**
     * Crea un nuevo archivo en el directorio actual con contenido.
     *
     * @param nombre Nombre del archivo (debe ser válido según las reglas del sistema)
     * @param contenido Contenido inicial del archivo
     * @return Archivo creado
     * @throws IllegalArgumentException si el nombre o contenido no son válidos
     */
    public Archivo crearArchivo(String nombre, String contenido) {
        // Validar nombre según las reglas del sistema
        validarNombreArchivo(nombre);

        // Validar tamaño del contenido
        validarContenido(contenido);

        // Crear archivo en el directorio actual
        return directorioActual.crearArchivo(nombre, contenido);
    }

    /**
     * Crea un nuevo archivo vacío en el directorio actual.
     *
     * @param nombre Nombre del archivo
     * @return Archivo creado
     */
    public Archivo crearArchivo(String nombre) {
        return crearArchivo(nombre, ""); // Archivo sin contenido inicial
    }

    /**
     * Elimina un archivo del directorio actual.
     *
     * @param nombre Nombre del archivo a eliminar
     * @return true si el archivo fue eliminado exitosamente
     */
    public boolean eliminarArchivo(String nombre) {
        return directorioActual.eliminarArchivo(nombre);
    }

    /**
     * Abre un archivo para lectura desde el directorio actual.
     *
     * @param nombre Nombre del archivo a abrir
     * @return Objeto Archivo o null si no existe
     */
    public Archivo abrirArchivo(String nombre) {
        return directorioActual.getArchivo(nombre);
    }

    /**
     * Edita el contenido de un archivo existente.
     *
     * @param nombre Nombre del archivo a editar
     * @param nuevoContenido Nuevo contenido para el archivo
     * @return true si la edición fue exitosa
     */
    public boolean editarArchivo(String nombre, String nuevoContenido) {
        Archivo archivo = directorioActual.getArchivo(nombre);
        if (archivo != null) {
            try {
                // Validar el nuevo contenido antes de aplicar cambios
                validarContenido(nuevoContenido);
                archivo.modificarContenido(nuevoContenido);
                return true;
            } catch (Exception e) {
                System.err.println("Error editando archivo: " + e.getMessage());
                return false;
            }
        }
        return false; // Archivo no encontrado
    }

    /**
     * Crea un nuevo subdirectorio en el directorio actual.
     *
     * @param nombre Nombre del directorio (debe ser válido)
     * @return Directorio creado
     * @throws IllegalArgumentException si el nombre no es válido
     */
    public Directorio crearDirectorio(String nombre) {
        validarNombreDirectorio(nombre);
        return directorioActual.crearSubdirectorio(nombre);
    }

    /**
     * Elimina un subdirectorio del directorio actual.
     *
     * @param nombre Nombre del directorio a eliminar
     * @return true si el directorio fue eliminado exitosamente
     */
    public boolean eliminarDirectorio(String nombre) {
        return directorioActual.eliminarSubdirectorio(nombre);
    }

    /**
     * Copia un archivo del directorio actual a otra ubicación.
     *
     * @param nombreArchivo Nombre del archivo a copiar
     * @param rutaDestino Ruta del directorio destino
     * @return true si la copia fue exitosa
     */
    public boolean copiarArchivo(String nombreArchivo, String rutaDestino) {
        // Obtener el archivo a copiar
        Archivo archivo = directorioActual.getArchivo(nombreArchivo);
        if (archivo != null) {
            // Resolver la ruta destino
            Directorio destino = raiz.navegarA(rutaDestino);
            if (destino != null) {
                try {
                    // Realizar la copia
                    directorioActual.copiarArchivo(archivo, destino);
                    return true;
                } catch (Exception e) {
                    System.err.println("Error copiando archivo: " + e.getMessage());
                }
            }
        }
        return false; // Error en la operación
    }

    /**
     * Mueve un archivo del directorio actual a otra ubicación.
     *
     * @param nombreArchivo Nombre del archivo a mover
     * @param rutaDestino Ruta del directorio destino
     * @return true si el movimiento fue exitoso
     */
    public boolean moverArchivo(String nombreArchivo, String rutaDestino) {
        // Resolver la ruta destino
        Directorio destino = raiz.navegarA(rutaDestino);
        if (destino != null) {
            try {
                // Realizar el movimiento (copia + eliminación)
                directorioActual.moverArchivo(nombreArchivo, destino);
                return true;
            } catch (Exception e) {
                System.err.println("Error moviendo archivo: " + e.getMessage());
            }
        }
        return false; // Error en la operación
    }

    /**
     * Busca archivos en todo el sistema que coincidan con un patrón.
     *
     * @param patron Patrón de búsqueda (substring del nombre)
     * @return Lista de archivos que coinciden con el patrón
     */
    public List<Archivo> buscarArchivos(String patron) {
        // Búsqueda recursiva desde la raíz
        return raiz.buscarArchivos(patron);
    }

    /**
     * Busca directorios en todo el sistema que coincidan con un patrón.
     *
     * @param patron Patrón de búsqueda (substring del nombre)
     * @return Lista de directorios que coinciden con el patrón
     */
    public List<Directorio> buscarDirectorios(String patron) {
        // Búsqueda recursiva desde la raíz
        return raiz.buscarDirectorios(patron);
    }

    /**
     * Busca archivos por tipo en todo el sistema.
     *
     * @param tipo Tipo de archivo a buscar (TEXTO, IMAGEN, VIDEO, etc.)
     * @return Lista de archivos del tipo especificado
     */
    public List<Archivo> buscarArchivosPorTipo(Archivo.TipoArchivo tipo) {
        List<Archivo> resultados = new ArrayList<>();
        buscarPorTipoRecursivo(raiz, tipo, resultados);
        return resultados;
    }

    /**
     * Método auxiliar para búsqueda recursiva por tipo de archivo.
     *
     * @param dir Directorio actual de la búsqueda
     * @param tipo Tipo de archivo buscado
     * @param resultados Lista para acumular resultados
     */
    private void buscarPorTipoRecursivo(Directorio dir, Archivo.TipoArchivo tipo, List<Archivo> resultados) {
        // Buscar en archivos del directorio actual
        for (Archivo archivo : dir.getArchivos()) {
            if (archivo.getTipo() == tipo) {
                resultados.add(archivo);
            }
        }

        // Continuar búsqueda en subdirectorios (recursión)
        for (Directorio subdir : dir.getSubdirectorios()) {
            buscarPorTipoRecursivo(subdir, tipo, resultados);
        }
    }

    /**
     * Valida que un nombre de archivo cumple con las reglas del sistema.
     *
     * Reglas aplicadas:
     * - No puede estar vacío
     * - No puede contener separadores de ruta (/ \)
     * - Longitud máxima de 255 caracteres
     * - No puede contener caracteres prohibidos: <>:"|?*
     *
     * @param nombre Nombre del archivo a validar
     * @throws IllegalArgumentException si el nombre no es válido
     */
    private void validarNombreArchivo(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo no puede estar vacío");
        }

        // Verificar separadores de ruta
        if (nombre.contains("/") || nombre.contains("\\")) {
            throw new IllegalArgumentException("El nombre del archivo no puede contener / o \\");
        }

        // Verificar longitud máxima
        if (nombre.length() > 255) {
            throw new IllegalArgumentException("El nombre del archivo es demasiado largo");
        }

        // Verificar caracteres prohibidos según estándares de sistemas de archivos
        String caracteresProhibidos = "<>:\"|?*";
        for (char c : caracteresProhibidos.toCharArray()) {
            if (nombre.indexOf(c) >= 0) {
                throw new IllegalArgumentException("El nombre contiene caracteres prohibidos: " + c);
            }
        }
    }

    /**
     * Valida que un nombre de directorio cumple con las reglas del sistema.
     *
     * Aplica las mismas reglas que los archivos, además de:
     * - No puede ser "." o ".." (nombres reservados)
     *
     * @param nombre Nombre del directorio a validar
     * @throws IllegalArgumentException si el nombre no es válido
     */
    private void validarNombreDirectorio(String nombre) {
        // Aplicar mismas reglas que archivos
        validarNombreArchivo(nombre);

        // Verificar nombres reservados del sistema
        if (nombre.equals(".") || nombre.equals("..")) {
            throw new IllegalArgumentException("Nombre de directorio reservado: " + nombre);
        }
    }

    /**
     * Valida que el contenido de un archivo no excede los límites del sistema.
     *
     * @param contenido Contenido a validar
     * @throws IllegalArgumentException si el contenido excede el tamaño máximo
     */
    private void validarContenido(String contenido) {
        if (contenido == null) return; // Contenido nulo es válido

        // Obtener tamaño máximo de la configuración
        int maxTamaño = Integer.parseInt(configuracion.get("max_file_size"));
        if (contenido.length() > maxTamaño) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido");
        }
    }

    /**
     * Lista el contenido del directorio actual.
     *
     * @return Lista de nombres de archivos y directorios en el directorio actual
     */
    public List<String> listarContenidoActual() {
        return directorioActual.listarContenido();
    }

    /**
     * Obtiene estadísticas generales del sistema de archivos.
     *
     * @return String formateado con estadísticas del sistema
     */
    public String getEstadisticas() {
        // Calcular métricas del sistema
        int totalArchivos = contarArchivosRecursivo(raiz);
        int totalDirectorios = contarDirectoriosRecursivo(raiz);
        int tamañoTotal = raiz.getTamañoTotal();

        return String.format(
                "Archivos: %d | Directorios: %d | Tamaño total: %s | Usuario: %s",
                totalArchivos, totalDirectorios, formatearTamaño(tamañoTotal), usuarioActual
        );
    }

    /**
     * Cuenta recursivamente todos los archivos en el sistema.
     *
     * @param dir Directorio desde donde iniciar el conteo
     * @return Número total de archivos
     */
    private int contarArchivosRecursivo(Directorio dir) {
        int count = dir.getNumeroArchivos();

        // Sumar archivos de todos los subdirectorios
        for (Directorio subdir : dir.getSubdirectorios()) {
            count += contarArchivosRecursivo(subdir);
        }
        return count;
    }

    /**
     * Cuenta recursivamente todos los directorios en el sistema.
     *
     * @param dir Directorio desde donde iniciar el conteo
     * @return Número total de directorios
     */
    private int contarDirectoriosRecursivo(Directorio dir) {
        int count = dir.getNumeroSubdirectorios();

        // Sumar directorios de todos los subdirectorios
        for (Directorio subdir : dir.getSubdirectorios()) {
            count += contarDirectoriosRecursivo(subdir);
        }
        return count;
    }

    /**
     * Formatea un tamaño en bytes a una representación legible.
     *
     * @param bytes Tamaño en bytes
     * @return String formateado (B, KB, MB)
     */
    private String formatearTamaño(int bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }

    /** @return Directorio raíz del sistema */
    public Directorio getRaiz() {
        return raiz;
    }

    /** @return Directorio actual donde se encuentra el usuario */
    public Directorio getDirectorioActual() {
        return directorioActual;
    }

    /** @return Nombre del usuario actual del sistema */
    public String getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Establece un valor de configuración del sistema.
     *
     * @param clave Nombre del parámetro de configuración
     * @param valor Valor del parámetro
     */
    public void setConfiguracion(String clave, String valor) {
        configuracion.put(clave, valor);
    }

    /**
     * Obtiene un valor de configuración del sistema.
     *
     * @param clave Nombre del parámetro de configuración
     * @return Valor del parámetro o null si no existe
     */
    public String getConfiguracion(String clave) {
        return configuracion.get(clave);
    }

    /**
     * Imprime la estructura completa del sistema de archivos en formato árbol.
     *
     * Útil para depuración y visualización de la jerarquía completa.
     * Formato similar al comando 'tree' de Unix/Linux.
     */
    public void imprimirArbolDirectorios() {
        imprimirArbol(raiz, "", true);
    }

    /**
     * Método auxiliar recursivo para imprimir la estructura en formato árbol.
     *
     * @param dir Directorio actual a imprimir
     * @param prefijo Prefijo de indentación para la visualización
     * @param esUltimo Indica si es el último elemento en su nivel
     */
    private void imprimirArbol(Directorio dir, String prefijo, boolean esUltimo) {
        // Imprimir directorio actual con formato de árbol
        System.out.println(prefijo + (esUltimo ? "└── " : "├── ") + dir.getNombre() + "/");

        // Obtener contenido del directorio
        List<Directorio> subdirs = dir.getSubdirectorios();
        List<Archivo> archivos = dir.getArchivos();

        // Imprimir subdirectorios recursivamente
        for (int i = 0; i < subdirs.size(); i++) {
            boolean esUltimoSubdir = (i == subdirs.size() - 1) && archivos.isEmpty();
            imprimirArbol(subdirs.get(i), prefijo + (esUltimo ? "    " : "│   "), esUltimoSubdir);
        }

        // Imprimir archivos del directorio actual
        for (int i = 0; i < archivos.size(); i++) {
            boolean esUltimoArchivo = (i == archivos.size() - 1);
            System.out.println(prefijo + (esUltimo ? "    " : "│   ") +
                    (esUltimoArchivo ? "└── " : "├── ") +
                    archivos.get(i).getNombre());
        }
    }
}