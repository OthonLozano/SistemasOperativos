package org.example.models;

/**
 * Modelo de datos que representa un bloque de memoria en el sistema MiniSO
 *
 * Esta clase encapsula la funcionalidad de un bloque individual de memoria
 * en el simulador de gestión de memoria del sistema operativo. Cada bloque
 * representa una porción contigua de memoria que puede estar:
 *
 * - Libre: Disponible para asignación a procesos
 * - Ocupado: Asignado a un proceso específico
 * - Sistema: Reservado para el sistema operativo
 * - Fragmentado: Resultado de fragmentación externa
 *
 * Los bloques son la unidad básica de gestión en los algoritmos de asignación
 * de memoria (First Fit, Best Fit, Worst Fit) y también se utilizan como
 * páginas en el algoritmo de paginación.
 */
public class BloqueMemoria {

    // === ATRIBUTOS PRINCIPALES DEL BLOQUE ===

    /** Identificador único del bloque de memoria */
    private int id;

    /** Tamaño del bloque en kilobytes (KB) */
    private int tamaño;

    /** Indica si el bloque está ocupado por algún proceso */
    private boolean ocupado;

    /** ID del proceso que ocupa el bloque (-1 si está libre) */
    private int procesoId;

    /** Nombre descriptivo del proceso que ocupa el bloque */
    private String nombreProceso;

    /** Tipo actual del bloque según su estado de uso */
    private TipoBloque tipo;

    /**
     * Enumeración que define los posibles estados de un bloque de memoria
     *
     * Cada tipo representa un estado específico en el ciclo de vida
     * de la memoria y determina cómo se visualiza y maneja el bloque.
     */
    public enum TipoBloque {
        /** Bloque disponible para asignación a nuevos procesos */
        LIBRE,

        /** Bloque actualmente asignado y en uso por un proceso */
        OCUPADO,

        /** Bloque reservado permanentemente para el sistema operativo */
        SISTEMA,

        /** Bloque resultante de fragmentación externa (no utilizable) */
        FRAGMENTADO
    }

    // === CONSTRUCTORES ===

    /**
     * Constructor básico para crear un bloque de memoria libre
     *
     * Inicializa un bloque en estado libre, listo para ser asignado
     * a cualquier proceso que lo requiera. Este es el constructor
     * más utilizado durante la inicialización del sistema de memoria.
     *
     * @param id Identificador único del bloque
     * @param tamaño Tamaño del bloque en kilobytes
     */
    public BloqueMemoria(int id, int tamaño) {
        this.id = id;
        this.tamaño = tamaño;
        this.ocupado = false;           // Inicialmente libre
        this.procesoId = -1;            // Sin proceso asignado
        this.nombreProceso = "";        // Sin nombre de proceso
        this.tipo = TipoBloque.LIBRE;   // Estado inicial libre
    }

    /**
     * Constructor especializado con tipo específico
     *
     * Permite crear bloques con un tipo predeterminado, útil para
     * inicializar bloques del sistema o manejar casos especiales
     * como la fragmentación.
     *
     * @param id Identificador único del bloque
     * @param tamaño Tamaño del bloque en kilobytes
     * @param tipo Tipo específico del bloque a crear
     */
    public BloqueMemoria(int id, int tamaño, TipoBloque tipo) {
        this(id, tamaño); // Llamar al constructor básico
        this.tipo = tipo;

        // Configuración especial para bloques del sistema
        if (tipo == TipoBloque.SISTEMA) {
            this.ocupado = true;
            this.nombreProceso = "SISTEMA";
            // El procesoId permanece en -1 para distinguir del espacio de usuario
        }
    }

    // === MÉTODOS DE GESTIÓN DE ESTADO ===

    /**
     * Asigna el bloque a un proceso específico
     *
     * Cambia el estado del bloque de libre a ocupado y registra
     * la información del proceso que lo está utilizando. Esta operación
     * es fundamental en todos los algoritmos de asignación de memoria.
     *
     * @param procesoId Identificador del proceso que ocupará el bloque
     * @param nombreProceso Nombre descriptivo del proceso
     */
    public void asignar(int procesoId, String nombreProceso) {
        this.ocupado = true;
        this.procesoId = procesoId;
        this.nombreProceso = nombreProceso;
        this.tipo = TipoBloque.OCUPADO;
    }

    /**
     * Libera el bloque y lo marca como disponible
     *
     * Restaura el bloque a su estado libre inicial, limpiando toda
     * la información del proceso que lo ocupaba. Esta operación es
     * crucial para el reciclaje de memoria cuando los procesos terminan.
     */
    public void liberar() {
        this.ocupado = false;
        this.procesoId = -1;            // Sin proceso asignado
        this.nombreProceso = "";        // Limpiar nombre
        this.tipo = TipoBloque.LIBRE;   // Volver a estado libre
    }

    // === MÉTODOS DE ACCESO (GETTERS) ===

    /**
     * Obtiene el identificador único del bloque
     *
     * @return int ID del bloque
     */
    public int getId() {
        return id;
    }

    /**
     * Obtiene el tamaño del bloque en kilobytes
     *
     * @return int tamaño en KB
     */
    public int getTamaño() {
        return tamaño;
    }

    /**
     * Verifica si el bloque está ocupado
     *
     * @return true si el bloque está en uso, false si está libre
     */
    public boolean isOcupado() {
        return ocupado;
    }

    /**
     * Obtiene el ID del proceso que ocupa el bloque
     *
     * @return int ID del proceso, o -1 si el bloque está libre
     */
    public int getProcesoId() {
        return procesoId;
    }

    /**
     * Obtiene el nombre del proceso que ocupa el bloque
     *
     * @return String nombre del proceso, o cadena vacía si está libre
     */
    public String getNombreProceso() {
        return nombreProceso;
    }

    /**
     * Obtiene el tipo actual del bloque
     *
     * @return TipoBloque enum que indica el estado del bloque
     */
    public TipoBloque getTipo() {
        return tipo;
    }

    // === MÉTODOS DE MODIFICACIÓN (SETTERS) ===

    /**
     * Establece el tipo del bloque manualmente
     *
     * Permite cambiar el tipo del bloque sin afectar otros atributos.
     * Útil para marcar bloques como fragmentados o para operaciones
     * especiales de administración de memoria.
     *
     * @param tipo Nuevo tipo del bloque
     */
    public void setTipo(TipoBloque tipo) {
        this.tipo = tipo;
    }

    /**
     * Modifica el tamaño del bloque
     *
     * Permite ajustar el tamaño del bloque dinámicamente, útil
     * en operaciones de división y fusión de bloques durante
     * la gestión de memoria.
     *
     * @param tamaño Nuevo tamaño en kilobytes
     */
    public void setTamaño(int tamaño) {
        this.tamaño = tamaño;
    }

    // === MÉTODOS DE INFORMACIÓN Y UTILIDAD ===

    /**
     * Genera información detallada del bloque en formato legible
     *
     * Crea una representación textual que incluye ID, tamaño, tipo
     * y el proceso que lo ocupa (si aplica). Este formato es ideal
     * para logging y debugging del sistema de memoria.
     *
     * @return String con información formateada del bloque
     */
    public String getInfo() {
        return String.format("Bloque %d: %dKB - %s %s",
                id,
                tamaño,
                tipo,
                ocupado ? "(" + nombreProceso + ")" : "");
    }

    /**
     * Representación textual del bloque
     *
     * Utiliza el método getInfo() para proporcionar una representación
     * consistente del bloque en diferentes contextos (logging, debugging, UI).
     *
     * @return String con la representación textual del bloque
     */
    @Override
    public String toString() {
        return getInfo();
    }

    // === MÉTODOS DE UTILIDAD ADICIONALES ===

    /**
     * Verifica si el bloque puede ser asignado a un proceso
     *
     * Un bloque es asignable si está libre y tiene suficiente tamaño
     * para satisfacer una solicitud de memoria.
     *
     * @param tamañoRequerido Tamaño mínimo requerido en KB
     * @return true si el bloque puede ser asignado
     */
    public boolean puedeAsignar(int tamañoRequerido) {
        return tipo == TipoBloque.LIBRE && tamaño >= tamañoRequerido;
    }

    /**
     * Verifica si el bloque pertenece a un proceso específico
     *
     * Útil para operaciones de búsqueda y liberación de memoria
     * de procesos particulares.
     *
     * @param procesoId ID del proceso a verificar
     * @return true si el bloque pertenece al proceso especificado
     */
    public boolean perteneceAProceso(int procesoId) {
        return this.ocupado && this.procesoId == procesoId;
    }

    /**
     * Calcula el porcentaje de utilización si se asignara un tamaño específico
     *
     * Método útil para algoritmos que consideran la eficiencia de uso
     * del espacio al asignar memoria.
     *
     * @param tamañoAsignar Tamaño que se pretende asignar
     * @return double porcentaje de utilización (0.0 a 100.0)
     */
    public double calcularUtilizacion(int tamañoAsignar) {
        if (tamaño == 0) return 0.0;
        return Math.min(100.0, (tamañoAsignar * 100.0) / tamaño);
    }

    /**
     * Obtiene una descripción del estado del bloque para UI
     *
     * Proporciona texto descriptivo apropiado para mostrar en
     * interfaces de usuario y reportes.
     *
     * @return String con descripción del estado
     */
    public String getDescripcionEstado() {
        switch (tipo) {
            case LIBRE:
                return "Disponible para asignación";
            case OCUPADO:
                return "En uso por " + nombreProceso;
            case SISTEMA:
                return "Reservado para el sistema";
            case FRAGMENTADO:
                return "Fragmentado (no utilizable)";
            default:
                return "Estado desconocido";
        }
    }

    /**
     * Verifica si el bloque es del sistema operativo
     *
     * @return true si es un bloque reservado para el sistema
     */
    public boolean esSistema() {
        return tipo == TipoBloque.SISTEMA;
    }

    /**
     * Verifica si el bloque está disponible para uso
     *
     * @return true si el bloque puede ser utilizado por procesos
     */
    public boolean estaDisponible() {
        return tipo == TipoBloque.LIBRE;
    }
}