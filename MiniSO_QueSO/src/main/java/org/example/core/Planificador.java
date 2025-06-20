package org.example.core;

import java.util.*;

/**
 * Planificador de procesos para el sistema operativo académico MiniSO.
 *
 * Esta clase implementa diferentes algoritmos de planificación de CPU:
 * - FIFO/FCFS (First Come First Served)
 * - Round Robin (RR) con quantum configurable
 * - Planificación por Prioridades
 *
 * El planificador gestiona el ciclo de vida de los procesos desde su llegada
 * hasta su terminación, calculando métricas importantes como tiempo de espera,
 * tiempo de respuesta y tiempo de finalización.
 */
public class Planificador {
    /** Cola de procesos que están listos para ejecutar */
    private Queue<Proceso> colaListos;

    /** Lista de procesos que han completado su ejecución */
    private List<Proceso> procesosTerminados;

    /** Proceso que se está ejecutando actualmente en la CPU */
    private Proceso procesoActual;

    /** Contador del tiempo transcurrido desde el inicio del sistema */
    private int tiempoActual;

    /** Nombre del algoritmo de planificación en uso */
    private String algoritmo;

    /** Tamaño del quantum para Round Robin (en unidades de tiempo) */
    private int quantum;

    /** Tiempo restante del quantum actual para el proceso en ejecución */
    private int tiempoQuantumRestante;


    /**
     * Constructor principal del planificador.
     *
     * Inicializa el planificador con el algoritmo especificado y
     * configura un quantum por defecto de 3 unidades para Round Robin.
     *
     * @param algoritmo Nombre del algoritmo de planificación a utilizar
     *                 ("FIFO", "FCFS", "RR", "ROUND_ROBIN", "PRIORIDAD")
     */
    public Planificador(String algoritmo) {
        this.algoritmo = algoritmo;
        this.colaListos = new LinkedList<>(); // Cola FIFO para procesos listos
        this.procesosTerminados = new ArrayList<>();
        this.procesoActual = null; // Inicialmente no hay proceso ejecutándose
        this.tiempoActual = 0; // El sistema inicia en tiempo 0
        this.quantum = 3; // Quantum por defecto para Round Robin
        this.tiempoQuantumRestante = quantum;
    }

    /**
     * Constructor con quantum personalizado para Round Robin.
     *
     * @param algoritmo Nombre del algoritmo de planificación
     * @param quantum Tamaño del quantum en unidades de tiempo
     */
    public Planificador(String algoritmo, int quantum) {
        this(algoritmo); // Llamar al constructor principal
        this.quantum = quantum;
        this.tiempoQuantumRestante = quantum;
    }


    /**
     * Agrega un nuevo proceso al sistema de planificación.
     *
     * El proceso se coloca en la posición adecuada según el algoritmo:
     * - FIFO/FCFS: Al final de la cola
     * - Round Robin: Al final de la cola
     * - Prioridades: Ordenado por prioridad (1 = alta, 5 = baja)
     *
     * @param proceso El proceso a agregar al sistema
     */
    public void agregarProceso(Proceso proceso) {
        // Marcar el proceso como listo para ejecución
        proceso.setEstado(Proceso.EstadoProceso.LISTO);

        // Agregar el proceso según el algoritmo configurado
        switch (algoritmo.toUpperCase()) {
            case "FIFO":
            case "FCFS":
                // First Come First Served: agregar al final de la cola
                colaListos.offer(proceso);
                break;
            case "RR":
            case "ROUND_ROBIN":
                // Round Robin: agregar al final de la cola
                colaListos.offer(proceso);
                break;
            case "PRIORIDAD":
                // Planificación por prioridades: insertar ordenadamente
                agregarPorPrioridad(proceso);
                break;
            default:
                // Algoritmo no reconocido: usar FIFO por defecto
                colaListos.offer(proceso);
        }

        // Confirmar que el proceso fue agregado exitosamente
        System.out.println("✅ Proceso agregado: " + proceso.getNombre() + " (PID: " + proceso.getPid() + ")");
    }

    /**
     * Agrega un proceso manteniendo el orden por prioridad.
     *
     * Los procesos se ordenan de menor a mayor número de prioridad,
     * donde 1 representa la prioridad más alta y 5 la más baja.
     *
     * @param proceso El proceso a insertar ordenadamente
     */
    private void agregarPorPrioridad(Proceso proceso) {
        // Convertir la cola a lista para poder ordenar
        List<Proceso> listaProcesos = new ArrayList<>(colaListos);
        listaProcesos.add(proceso);

        // Ordenar por prioridad ascendente (menor número = mayor prioridad)
        listaProcesos.sort(Comparator.comparingInt(Proceso::getPrioridad));

        // Reconstruir la cola con el nuevo orden
        colaListos.clear();
        colaListos.addAll(listaProcesos);
    }


    /**
     * Ejecuta un ciclo completo de planificación de CPU.
     *
     * En cada ciclo se realizan las siguientes operaciones:
     * 1. Si no hay proceso ejecutándose, seleccionar el siguiente de la cola
     * 2. Ejecutar el proceso actual por una unidad de tiempo
     * 3. Verificar si el proceso terminó o si se agotó el quantum (Round Robin)
     * 4. Actualizar métricas y estados de los procesos
     * 5. Incrementar el tiempo del sistema
     *
     * @return true si quedan procesos por ejecutar, false si todos terminaron
     */
    public boolean ejecutarCiclo() {
        System.out.println("\n⏰ Tiempo actual: " + tiempoActual);

        // FASE 1: Selección de proceso
        if (procesoActual == null && !colaListos.isEmpty()) {
            // Tomar el siguiente proceso de la cola de listos
            procesoActual = colaListos.poll();
            procesoActual.setEstado(Proceso.EstadoProceso.EJECUTANDO);

            // Calcular tiempo de respuesta si es la primera ejecución del proceso
            if (procesoActual.getTiempoRespuesta() == -1) {
                // Tiempo de respuesta = tiempo actual - tiempo de llegada
                procesoActual.setTiempoRespuesta(tiempoActual - procesoActual.getTiempoLlegada());
            }

            // Reiniciar el quantum para el nuevo proceso
            tiempoQuantumRestante = quantum;
            System.out.println("🔄 Ejecutando proceso: " + procesoActual.getNombre());
        }

        // FASE 2: Ejecución del proceso
        if (procesoActual != null) {
            // Ejecutar el proceso por una unidad de tiempo
            procesoActual.ejecutar();
            tiempoQuantumRestante--;

            // Mostrar estado actual del proceso
            System.out.println("▶️ " + procesoActual);

            // FASE 3: Verificación de terminación
            if (procesoActual.haTerminado()) {
                // El proceso completó su ejecución
                finalizarProceso();
            }
            // FASE 4: Verificación de quantum (solo para Round Robin)
            else if (esRoundRobin() && tiempoQuantumRestante <= 0) {
                // Se agotó el quantum, cambiar de proceso
                cambiarProcesoRoundRobin();
            }
        }

        // FASE 5: Actualización del tiempo del sistema
        tiempoActual++;

        // Retornar true si hay más trabajo por hacer
        return procesoActual != null || !colaListos.isEmpty();
    }

    /**
     * Finaliza un proceso que ha completado su ejecución.
     *
     * Calcula las métricas finales del proceso y lo mueve a la lista
     * de procesos terminados.
     */
    private void finalizarProceso() {
        // Establecer tiempo de finalización
        procesoActual.setTiempoFinalizacion(tiempoActual + 1);

        // Calcular tiempo de espera: T_finalizacion - T_llegada - T_ejecucion
        procesoActual.setTiempoEspera(
                procesoActual.getTiempoFinalizacion() -
                        procesoActual.getTiempoLlegada() -
                        procesoActual.getTiempoEjecucion()
        );

        // Mover el proceso a la lista de terminados
        procesosTerminados.add(procesoActual);
        System.out.println("✅ Proceso terminado: " + procesoActual.getNombre());

        // Liberar la CPU
        procesoActual = null;
        tiempoQuantumRestante = quantum;
    }

    /**
     * Maneja el cambio de proceso en Round Robin cuando se agota el quantum.
     */
    private void cambiarProcesoRoundRobin() {
        // Cambiar estado del proceso a listo
        procesoActual.setEstado(Proceso.EstadoProceso.LISTO);

        // Enviar el proceso al final de la cola
        colaListos.offer(procesoActual);

        System.out.println("⏰ Quantum agotado, proceso " + procesoActual.getNombre() + " va al final de la cola");

        // Liberar la CPU para el siguiente proceso
        procesoActual = null;
        tiempoQuantumRestante = quantum;
    }

    /**
     * Verifica si el algoritmo actual es Round Robin.
     *
     * @return true si el algoritmo es Round Robin
     */
    private boolean esRoundRobin() {
        return algoritmo.equalsIgnoreCase("RR") || algoritmo.equalsIgnoreCase("ROUND_ROBIN");
    }

    /**
     * Ejecuta todos los procesos hasta que el sistema esté vacío.
     *
     * Este método ejecuta ciclos continuos hasta que no queden procesos
     * por ejecutar, y luego muestra las estadísticas finales.
     */
    public void ejecutarTodos() {
        System.out.println("\n🚀 Iniciando planificación con algoritmo: " + algoritmo);
        System.out.println("═══════════════════════════════════════════════════════════");

        // Ejecutar ciclos hasta que todos los procesos terminen
        while (ejecutarCiclo()) {
            // Pausa opcional para visualización (útil en demos)
            try {
                Thread.sleep(500); // Pausa de 500ms entre ciclos
            } catch (InterruptedException e) {
                // Restaurar el flag de interrupción si ocurre
                Thread.currentThread().interrupt();
            }
        }

        // Mostrar resultados finales
        System.out.println("\n🏁 Todos los procesos han terminado!");
        mostrarEstadisticas();
    }

    /**
     * Muestra estadísticas completas de la ejecución de procesos.
     *
     * Incluye:
     * - Información del algoritmo y configuración
     * - Tiempo total de ejecución del sistema
     * - Tabla detallada de cada proceso con todas sus métricas
     * - Promedios de tiempo de espera y tiempo de respuesta
     */
    public void mostrarEstadisticas() {
        // Encabezado de estadísticas
        System.out.println("\n📊 ESTADÍSTICAS FINALES");
        System.out.println("═══════════════════════════════════════════════════════════");

        // Información del algoritmo
        System.out.println("Algoritmo utilizado: " + algoritmo);
        if (esRoundRobin()) {
            System.out.println("Quantum utilizado: " + quantum);
        }

        // Métricas generales del sistema
        System.out.println("Tiempo total de ejecución: " + tiempoActual);
        System.out.println("Procesos completados: " + procesosTerminados.size());

        // Tabla de detalles de procesos
        mostrarTablaDetallada();

        // Calcular y mostrar promedios
        mostrarPromedios();
    }

    /**
     * Muestra una tabla detallada con las métricas de cada proceso.
     */
    private void mostrarTablaDetallada() {
        System.out.println("\n📋 DETALLES DE PROCESOS:");

        // Encabezados de la tabla
        System.out.println("PID | Nombre     | T.Llegada | T.Ejecución | T.Espera | T.Respuesta | T.Finalización");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────────");

        // Mostrar datos de cada proceso terminado
        for (Proceso p : procesosTerminados) {
            System.out.printf("%3d | %-10s | %9d | %11d | %8d | %11d | %14d%n",
                    p.getPid(), p.getNombre(), p.getTiempoLlegada(), p.getTiempoEjecucion(),
                    p.getTiempoEspera(), p.getTiempoRespuesta(), p.getTiempoFinalizacion());
        }
    }

    /**
     * Calcula y muestra los promedios de las métricas clave.
     */
    private void mostrarPromedios() {
        if (!procesosTerminados.isEmpty()) {
            // Calcular promedio de tiempo de espera
            double promedioEspera = procesosTerminados.stream()
                    .mapToInt(Proceso::getTiempoEspera)
                    .average()
                    .orElse(0);

            // Calcular promedio de tiempo de respuesta
            double promedioRespuesta = procesosTerminados.stream()
                    .mapToInt(Proceso::getTiempoRespuesta)
                    .average()
                    .orElse(0);

            // Mostrar línea separadora y promedios
            System.out.println("─────────────────────────────────────────────────────────────────────────────────────");
            System.out.printf("PROMEDIOS: Tiempo de espera = %.2f | Tiempo de respuesta = %.2f%n",
                    promedioEspera, promedioRespuesta);
        }
    }

    /**
     * Obtiene el tiempo actual del sistema.
     *
     * @return Tiempo transcurrido desde el inicio del sistema
     */
    public int getTiempoActual() {
        return tiempoActual;
    }

    /**
     * Obtiene el nombre del algoritmo de planificación en uso.
     *
     * @return Nombre del algoritmo actual
     */
    public String getAlgoritmo() {
        return algoritmo;
    }

    /**
     * Obtiene la lista de procesos que han terminado su ejecución.
     *
     * @return Lista inmutable de procesos terminados
     */
    public List<Proceso> getProcesosTerminados() {
        return procesosTerminados;
    }

    /**
     * Obtiene el tamaño del quantum configurado para Round Robin.
     *
     * @return Tamaño del quantum en unidades de tiempo
     */
    public int getQuantum() {
        return quantum;
    }

    /**
     * Establece un nuevo valor para el quantum de Round Robin.
     *
     * También actualiza el quantum restante del proceso actual
     * si hay uno ejecutándose.
     *
     * @param quantum Nuevo tamaño del quantum (debe ser > 0)
     */
    public void setQuantum(int quantum) {
        if (quantum <= 0) {
            throw new IllegalArgumentException("El quantum debe ser mayor a 0");
        }

        this.quantum = quantum;
        this.tiempoQuantumRestante = quantum;
    }
}