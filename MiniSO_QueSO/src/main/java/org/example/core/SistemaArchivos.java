package org.example.core;

import org.example.models.Archivo;
import org.example.models.Directorio;
import java.util.*;

public class SistemaArchivos {

    private Directorio raiz;
    private Directorio directorioActual;
    private String usuarioActual;
    private Map<String, String> configuracion;

    // Constructor
    public SistemaArchivos(String usuarioAdmin) {
        this.usuarioActual = usuarioAdmin;
        this.configuracion = new HashMap<>();
        inicializarSistema();
    }

    private void inicializarSistema() {
        // Crear directorio raíz
        raiz = new Directorio(usuarioActual);
        directorioActual = raiz;

        // Crear estructura básica del sistema
        crearEstructuraBasica();

        // Configuración por defecto
        configuracion.put("max_file_size", "1048576"); // 1MB
        configuracion.put("max_files_per_dir", "1000");
        configuracion.put("case_sensitive", "false");
    }

    private void crearEstructuraBasica() {
        try {
            // Crear directorios del sistema
            Directorio home = raiz.crearSubdirectorio("home");
            Directorio bin = raiz.crearSubdirectorio("bin");
            Directorio etc = raiz.crearSubdirectorio("etc");
            Directorio tmp = raiz.crearSubdirectorio("tmp");
            Directorio usr = raiz.crearSubdirectorio("usr");

            // Crear directorio del usuario
            Directorio userHome = home.crearSubdirectorio(usuarioActual);

            // Crear subdirectorios en home del usuario
            userHome.crearSubdirectorio("Documentos");
            userHome.crearSubdirectorio("Descargas");
            userHome.crearSubdirectorio("Escritorio");
            userHome.crearSubdirectorio("Imágenes");
            userHome.crearSubdirectorio("Videos");

            // Crear algunos archivos de ejemplo
            userHome.crearArchivo("bienvenida.txt",
                    "¡Bienvenido al MiniSO!\n\nEste es tu directorio personal.\n" +
                            "Aquí puedes crear y organizar tus archivos.\n\n" +
                            "Comandos básicos:\n" +
                            "- Crear archivo: Clic derecho > Nuevo archivo\n" +
                            "- Crear carpeta: Clic derecho > Nueva carpeta\n" +
                            "- Navegar: Doble clic en carpetas\n\n" +
                            "¡Disfruta explorando el sistema!"
            );

            userHome.getSubdirectorio("Documentos").crearArchivo("readme.md",
                    "# Mi Directorio de Documentos\n\n" +
                            "Este directorio está destinado para guardar documentos importantes.\n\n" +
                            "## Organización sugerida:\n" +
                            "- Proyectos/\n" +
                            "- Reportes/\n" +
                            "- Presentaciones/\n"
            );

            // Archivos del sistema en /etc
            etc.crearArchivo("system.conf",
                    "# Configuración del Sistema MiniSO\n" +
                            "version=1.0\n" +
                            "admin=" + usuarioActual + "\n" +
                            "max_users=10\n" +
                            "debug=false\n"
            );

            // Scripts en /bin
            bin.crearArchivo("hello.sh",
                    "#!/bin/bash\n" +
                            "echo 'Hola desde MiniSO!'\n" +
                            "echo 'Sistema operativo académico'\n"
            );

        } catch (Exception e) {
            System.err.println("Error creando estructura básica: " + e.getMessage());
        }
    }

    // Operaciones de navegación
    public boolean cambiarDirectorio(String ruta) {
        Directorio destino;

        if (ruta.equals("/")) {
            destino = raiz;
        } else if (ruta.equals("~")) {
            destino = getDirectorioHome();
        } else {
            destino = directorioActual.navegarA(ruta);
        }

        if (destino != null) {
            directorioActual = destino;
            return true;
        }
        return false;
    }

    public Directorio getDirectorioHome() {
        Directorio home = raiz.getSubdirectorio("home");
        return home != null ? home.getSubdirectorio(usuarioActual) : raiz;
    }

    public String getRutaActual() {
        return directorioActual.getRutaCompleta();
    }

    // Operaciones con archivos
    public Archivo crearArchivo(String nombre, String contenido) {
        validarNombreArchivo(nombre);
        validarContenido(contenido);

        return directorioActual.crearArchivo(nombre, contenido);
    }

    public Archivo crearArchivo(String nombre) {
        return crearArchivo(nombre, "");
    }

    public boolean eliminarArchivo(String nombre) {
        return directorioActual.eliminarArchivo(nombre);
    }

    public Archivo abrirArchivo(String nombre) {
        return directorioActual.getArchivo(nombre);
    }

    public boolean editarArchivo(String nombre, String nuevoContenido) {
        Archivo archivo = directorioActual.getArchivo(nombre);
        if (archivo != null) {
            try {
                validarContenido(nuevoContenido);
                archivo.modificarContenido(nuevoContenido);
                return true;
            } catch (Exception e) {
                System.err.println("Error editando archivo: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    // Operaciones con directorios
    public Directorio crearDirectorio(String nombre) {
        validarNombreDirectorio(nombre);
        return directorioActual.crearSubdirectorio(nombre);
    }

    public boolean eliminarDirectorio(String nombre) {
        return directorioActual.eliminarSubdirectorio(nombre);
    }

    // Operaciones de copia y movimiento
    public boolean copiarArchivo(String nombreArchivo, String rutaDestino) {
        Archivo archivo = directorioActual.getArchivo(nombreArchivo);
        if (archivo != null) {
            Directorio destino = raiz.navegarA(rutaDestino);
            if (destino != null) {
                try {
                    directorioActual.copiarArchivo(archivo, destino);
                    return true;
                } catch (Exception e) {
                    System.err.println("Error copiando archivo: " + e.getMessage());
                }
            }
        }
        return false;
    }

    public boolean moverArchivo(String nombreArchivo, String rutaDestino) {
        Directorio destino = raiz.navegarA(rutaDestino);
        if (destino != null) {
            try {
                directorioActual.moverArchivo(nombreArchivo, destino);
                return true;
            } catch (Exception e) {
                System.err.println("Error moviendo archivo: " + e.getMessage());
            }
        }
        return false;
    }

    // Operaciones de búsqueda
    public List<Archivo> buscarArchivos(String patron) {
        return raiz.buscarArchivos(patron);
    }

    public List<Directorio> buscarDirectorios(String patron) {
        return raiz.buscarDirectorios(patron);
    }

    public List<Archivo> buscarArchivosPorTipo(Archivo.TipoArchivo tipo) {
        List<Archivo> resultados = new ArrayList<>();
        buscarPorTipoRecursivo(raiz, tipo, resultados);
        return resultados;
    }

    private void buscarPorTipoRecursivo(Directorio dir, Archivo.TipoArchivo tipo, List<Archivo> resultados) {
        for (Archivo archivo : dir.getArchivos()) {
            if (archivo.getTipo() == tipo) {
                resultados.add(archivo);
            }
        }

        for (Directorio subdir : dir.getSubdirectorios()) {
            buscarPorTipoRecursivo(subdir, tipo, resultados);
        }
    }

    // Validaciones
    private void validarNombreArchivo(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo no puede estar vacío");
        }

        if (nombre.contains("/") || nombre.contains("\\")) {
            throw new IllegalArgumentException("El nombre del archivo no puede contener / o \\");
        }

        if (nombre.length() > 255) {
            throw new IllegalArgumentException("El nombre del archivo es demasiado largo");
        }

        // Caracteres prohibidos
        String caracteresProhibidos = "<>:\"|?*";
        for (char c : caracteresProhibidos.toCharArray()) {
            if (nombre.indexOf(c) >= 0) {
                throw new IllegalArgumentException("El nombre contiene caracteres prohibidos: " + c);
            }
        }
    }

    private void validarNombreDirectorio(String nombre) {
        validarNombreArchivo(nombre); // Mismas reglas que archivos

        if (nombre.equals(".") || nombre.equals("..")) {
            throw new IllegalArgumentException("Nombre de directorio reservado: " + nombre);
        }
    }

    private void validarContenido(String contenido) {
        if (contenido == null) return;

        int maxTamaño = Integer.parseInt(configuracion.get("max_file_size"));
        if (contenido.length() > maxTamaño) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido");
        }
    }

    // Información del sistema
    public List<String> listarContenidoActual() {
        return directorioActual.listarContenido();
    }

    public String getEstadisticas() {
        int totalArchivos = contarArchivosRecursivo(raiz);
        int totalDirectorios = contarDirectoriosRecursivo(raiz);
        int tamañoTotal = raiz.getTamañoTotal();

        return String.format(
                "Archivos: %d | Directorios: %d | Tamaño total: %s | Usuario: %s",
                totalArchivos, totalDirectorios, formatearTamaño(tamañoTotal), usuarioActual
        );
    }

    private int contarArchivosRecursivo(Directorio dir) {
        int count = dir.getNumeroArchivos();
        for (Directorio subdir : dir.getSubdirectorios()) {
            count += contarArchivosRecursivo(subdir);
        }
        return count;
    }

    private int contarDirectoriosRecursivo(Directorio dir) {
        int count = dir.getNumeroSubdirectorios();
        for (Directorio subdir : dir.getSubdirectorios()) {
            count += contarDirectoriosRecursivo(subdir);
        }
        return count;
    }

    private String formatearTamaño(int bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }

    // Getters
    public Directorio getRaiz() { return raiz; }
    public Directorio getDirectorioActual() { return directorioActual; }
    public String getUsuarioActual() { return usuarioActual; }

    // Configuración
    public void setConfiguracion(String clave, String valor) {
        configuracion.put(clave, valor);
    }

    public String getConfiguracion(String clave) {
        return configuracion.get(clave);
    }

    // Utilidades de depuración
    public void imprimirArbolDirectorios() {
        imprimirArbol(raiz, "", true);
    }

    private void imprimirArbol(Directorio dir, String prefijo, boolean esUltimo) {
        System.out.println(prefijo + (esUltimo ? "└── " : "├── ") + dir.getNombre() + "/");

        List<Directorio> subdirs = dir.getSubdirectorios();
        List<Archivo> archivos = dir.getArchivos();

        // Imprimir subdirectorios
        for (int i = 0; i < subdirs.size(); i++) {
            boolean esUltimoSubdir = (i == subdirs.size() - 1) && archivos.isEmpty();
            imprimirArbol(subdirs.get(i), prefijo + (esUltimo ? "    " : "│   "), esUltimoSubdir);
        }

        // Imprimir archivos
        for (int i = 0; i < archivos.size(); i++) {
            boolean esUltimoArchivo = (i == archivos.size() - 1);
            System.out.println(prefijo + (esUltimo ? "    " : "│   ") +
                    (esUltimoArchivo ? "└── " : "├── ") +
                    archivos.get(i).getNombre());
        }
    }
}