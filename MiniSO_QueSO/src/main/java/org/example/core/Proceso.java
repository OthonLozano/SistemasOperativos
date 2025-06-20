package org.example.core;

/**
 * Representa un proceso en el sistema operativo académico MiniSO.
 *
 * Esta clase modela un proceso con todas sus características esenciales
 * para la planificación de CPU, incluyendo tiempos de ejecución, estados,
 * prioridades y métricas de rendimiento.
 *
 * Un proceso pasa por diferentes estados durante su ciclo de vida:
 * NUEVO → LISTO → EJECUTANDO → TERMINADO (o BLOQUEADO en implementaciones avanzadas)
 *
 * Las métricas calculadas incluyen:
 * - Tiempo de espera: tiempo total esperando en colas
 * - Tiempo de respuesta: tiempo desde llegada hasta primera ejecución
 * - Tiempo de finalización: tiempo total desde llegada hasta terminación
 */
public class Proceso {
    /** Identificador único del proceso en el sistema */
    private int pid;

    /** Nombre descriptivo del proceso para identificación humana */
    private String nombre;

    /** Tiempo en que el proceso llega al sistema (unidades de tiempo) */
    private int tiempoLlegada;

    /** Tiempo total de CPU que requiere el proceso para completarse */
    private int tiempoEjecucion;

    /** Tiempo de CPU que aún necesita el proceso para terminar */
    private int tiempoRestante;

    /** Prioridad del proceso (1 = máxima prioridad, 5 = mínima prioridad) */
    private int prioridad;

    /** Estado actual del proceso en su ciclo de vida */
    private EstadoProceso estado;

    /** Tiempo total que el proceso ha esperado en colas de listos */
    private int tiempoEspera;

    /** Tiempo desde llegada hasta primera ejecución (-1 si no ha ejecutado) */
    private int tiempoRespuesta;

    /** Tiempo en que el proceso completó su ejecución (-1 si no ha terminado) */
    private int tiempoFinalizacion;

    /**
     * Estados posibles de un proceso durante su ciclo de vida.
     *
     * Basado en el modelo clásico de cinco estados de los sistemas operativos:
     */
    public enum EstadoProceso {
        /** Proceso recién creado, aún no agregado al planificador */
        NUEVO,

        /** Proceso listo para ejecutar, esperando asignación de CPU */
        LISTO,

        /** Proceso actualmente ejecutándose en la CPU */
        EJECUTANDO,

        /** Proceso esperando algún recurso (I/O, semáforo, etc.) */
        BLOQUEADO,

        /** Proceso que ha completado toda su ejecución */
        TERMINADO
    }

    /**
     * Constructor principal para crear un nuevo proceso.
     *
     * Inicializa un proceso con sus características básicas y establece
     * valores por defecto para las métricas que se calcularán durante la ejecución.
     *
     * @param pid Identificador único del proceso (debe ser > 0)
     * @param nombre Nombre descriptivo del proceso
     * @param tiempoLlegada Tiempo en que el proceso llega al sistema (≥ 0)
     * @param tiempoEjecucion Tiempo total de CPU requerido (> 0)
     * @param prioridad Prioridad del proceso (1-5, donde 1 es la más alta)
     */
    public Proceso(int pid, String nombre, int tiempoLlegada, int tiempoEjecucion, int prioridad) {
        // Validaciones básicas de parámetros
        if (pid <= 0) {
            throw new IllegalArgumentException("El PID debe ser mayor a 0");
        }
        if (tiempoEjecucion <= 0) {
            throw new IllegalArgumentException("El tiempo de ejecución debe ser mayor a 0");
        }
        if (tiempoLlegada < 0) {
            throw new IllegalArgumentException("El tiempo de llegada no puede ser negativo");
        }

        // Asignación de atributos principales
        this.pid = pid;
        this.nombre = nombre != null ? nombre : "Proceso_" + pid;
        this.tiempoLlegada = tiempoLlegada;
        this.tiempoEjecucion = tiempoEjecucion;
        this.tiempoRestante = tiempoEjecucion; // Inicialmente igual al tiempo total
        this.prioridad = Math.max(1, Math.min(5, prioridad)); // Normalizar entre 1-5

        // Estado inicial del proceso
        this.estado = EstadoProceso.NUEVO;

        // Inicialización de métricas de rendimiento
        this.tiempoEspera = 0; // No ha esperado aún
        this.tiempoRespuesta = -1; // -1 indica que aún no ha ejecutado
        this.tiempoFinalizacion = -1; // -1 indica que aún no ha terminado
    }

    /** @return Identificador único del proceso */
    public int getPid() {
        return pid;
    }

    /** @return Nombre descriptivo del proceso */
    public String getNombre() {
        return nombre;
    }

    /** @return Tiempo en que el proceso llegó al sistema */
    public int getTiempoLlegada() {
        return tiempoLlegada;
    }

    /** @return Tiempo total de CPU que requiere el proceso */
    public int getTiempoEjecucion() {
        return tiempoEjecucion;
    }

    /** @return Tiempo de CPU que aún necesita el proceso */
    public int getTiempoRestante() {
        return tiempoRestante;
    }

    /** @return Prioridad del proceso (1=alta, 5=baja) */
    public int getPrioridad() {
        return prioridad;
    }

    /** @return Estado actual del proceso */
    public EstadoProceso getEstado() {
        return estado;
    }

    /** @return Tiempo total que el proceso ha esperado */
    public int getTiempoEspera() {
        return tiempoEspera;
    }

    /** @return Tiempo de respuesta (-1 si no ha ejecutado) */
    public int getTiempoRespuesta() {
        return tiempoRespuesta;
    }

    /** @return Tiempo de finalización (-1 si no ha terminado) */
    public int getTiempoFinalizacion() {
        return tiempoFinalizacion;
    }

    /**
     * Establece el tiempo restante de ejecución del proceso.
     *
     * @param tiempoRestante Nuevo tiempo restante (≥ 0)
     */
    public void setTiempoRestante(int tiempoRestante) {
        if (tiempoRestante < 0) {
            throw new IllegalArgumentException("El tiempo restante no puede ser negativo");
        }
        this.tiempoRestante = tiempoRestante;
    }

    /**
     * Cambia el estado del proceso.
     *
     * @param estado Nuevo estado del proceso
     */
    public void setEstado(EstadoProceso estado) {
        this.estado = estado;
    }

    /**
     * Establece el tiempo total de espera del proceso.
     *
     * @param tiempoEspera Tiempo de espera calculado (≥ 0)
     */
    public void setTiempoEspera(int tiempoEspera) {
        this.tiempoEspera = Math.max(0, tiempoEspera);
    }

    /**
     * Establece el tiempo de respuesta del proceso.
     *
     * @param tiempoRespuesta Tiempo desde llegada hasta primera ejecución
     */
    public void setTiempoRespuesta(int tiempoRespuesta) {
        this.tiempoRespuesta = tiempoRespuesta;
    }

    /**
     * Establece el tiempo en que el proceso terminó su ejecución.
     *
     * @param tiempoFinalizacion Tiempo de finalización del proceso
     */
    public void setTiempoFinalizacion(int tiempoFinalizacion) {
        this.tiempoFinalizacion = tiempoFinalizacion;
    }

    /**
     * Ejecuta el proceso por una unidad de tiempo.
     *
     * Este método simula la ejecución del proceso en la CPU:
     * 1. Reduce el tiempo restante en una unidad
     * 2. Si el tiempo restante llega a 0, marca el proceso como terminado
     *
     * Solo ejecuta si el proceso tiene tiempo restante > 0.
     */
    public void ejecutar() {
        // Verificar que el proceso tiene trabajo pendiente
        if (tiempoRestante > 0) {
            // Simular ejecución por una unidad de tiempo
            tiempoRestante--;

            // Verificar si el proceso ha completado su ejecución
            if (tiempoRestante == 0) {
                // Cambiar estado a terminado automáticamente
                estado = EstadoProceso.TERMINADO;
            }
        }
    }

    /**
     * Verifica si el proceso ha completado toda su ejecución.
     *
     * @return true si el proceso está en estado TERMINADO
     */
    public boolean haTerminado() {
        return estado == EstadoProceso.TERMINADO;
    }

    /**
     * Muestra información completa y detallada del proceso.
     *
     * Útil para depuración y análisis detallado del estado del proceso.
     * Muestra todos los atributos y métricas en un formato legible.
     */
    public void mostrarInfo() {
        System.out.println("═══════════════════════════════════════");
        System.out.println("           INFORMACIÓN DEL PROCESO");
        System.out.println("═══════════════════════════════════════");

        // Información básica del proceso
        System.out.println("PID: " + pid);
        System.out.println("Nombre: " + nombre);
        System.out.println("Estado: " + estado);
        System.out.println("Prioridad: " + prioridad + " (1=alta, 5=baja)");

        // Información de tiempos
        System.out.println("Tiempo de llegada: " + tiempoLlegada);
        System.out.println("Tiempo de ejecución: " + tiempoEjecucion);
        System.out.println("Tiempo restante: " + tiempoRestante);

        // Métricas de rendimiento (solo si están disponibles)
        System.out.println("Tiempo de espera: " + tiempoEspera);

        if (tiempoRespuesta != -1) {
            System.out.println("Tiempo de respuesta: " + tiempoRespuesta);
        } else {
            System.out.println("Tiempo de respuesta: No calculado aún");
        }

        if (tiempoFinalizacion != -1) {
            System.out.println("Tiempo de finalización: " + tiempoFinalizacion);
        } else {
            System.out.println("Tiempo de finalización: Proceso aún en ejecución");
        }

        System.out.println("═══════════════════════════════════════");
    }

    /**
     * Representación en cadena compacta del proceso.
     *
     * Muestra la información más relevante del proceso en una sola línea,
     * útil para logs y visualización rápida del estado.
     *
     * @return String formateado con información clave del proceso
     */
    @Override
    public String toString() {
        return String.format("PID: %d | Nombre: %-10s | Estado: %-10s | Tiempo restante: %2d | Prioridad: %d",
                pid, nombre, estado, tiempoRestante, prioridad);
    }

    /**
     * Calcula el porcentaje de progreso del proceso.
     *
     * @return Porcentaje de ejecución completado (0.0 - 100.0)
     */
    public double getPorcentajeProgreso() {
        if (tiempoEjecucion == 0) return 100.0;

        double ejecutado = tiempoEjecucion - tiempoRestante;
        return (ejecutado / tiempoEjecucion) * 100.0;
    }

    /**
     * Verifica si el proceso es de alta prioridad.
     *
     * @return true si la prioridad es 1 o 2
     */
    public boolean esAltaPrioridad() {
        return prioridad <= 2;
    }

    /**
     * Obtiene una descripción textual del estado del proceso.
     *
     * @return Descripción amigable del estado actual
     */
    public String getDescripcionEstado() {
        switch (estado) {
            case NUEVO:
                return "Proceso recién creado";
            case LISTO:
                return "Esperando asignación de CPU";
            case EJECUTANDO:
                return "Ejecutándose en CPU";
            case BLOQUEADO:
                return "Esperando recurso";
            case TERMINADO:
                return "Ejecución completada";
            default:
                return "Estado desconocido";
        }
    }


    /**
     * Compara dos procesos para determinar igualdad.
     *
     * Dos procesos son iguales si tienen el mismo PID.
     *
     * @param obj Objeto a comparar
     * @return true si los procesos tienen el mismo PID
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Proceso proceso = (Proceso) obj;
        return pid == proceso.pid;
    }

    /**
     * Calcula el código hash del proceso basado en su PID.
     *
     * @return Código hash del proceso
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(pid);
    }
}