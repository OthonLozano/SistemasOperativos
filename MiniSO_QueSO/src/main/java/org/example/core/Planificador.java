package org.example.core;

import java.util.*;

public class Planificador {
    private Queue<Proceso> colaListos;           // Cola de procesos listos
    private List<Proceso> procesosTerminados;    // Lista de procesos terminados
    private Proceso procesoActual;               // Proceso actualmente ejecutándose
    private int tiempoActual;                    // Tiempo actual del sistema
    private String algoritmo;                    // Tipo de algoritmo usado
    private int quantum;                         // Para Round Robin
    private int tiempoQuantumRestante;           // Tiempo restante del quantum actual

    // Constructor
    public Planificador(String algoritmo) {
        this.algoritmo = algoritmo;
        this.colaListos = new LinkedList<>();
        this.procesosTerminados = new ArrayList<>();
        this.procesoActual = null;
        this.tiempoActual = 0;
        this.quantum = 3; // Quantum por defecto para Round Robin
        this.tiempoQuantumRestante = quantum;
    }

    // Constructor con quantum personalizado
    public Planificador(String algoritmo, int quantum) {
        this(algoritmo);
        this.quantum = quantum;
        this.tiempoQuantumRestante = quantum;
    }

    // Agregar proceso al sistema
    public void agregarProceso(Proceso proceso) {
        proceso.setEstado(Proceso.EstadoProceso.LISTO);

        switch (algoritmo.toUpperCase()) {
            case "FIFO":
            case "FCFS":
                colaListos.offer(proceso);
                break;
            case "RR":
            case "ROUND_ROBIN":
                colaListos.offer(proceso);
                break;
            case "PRIORIDAD":
                agregarPorPrioridad(proceso);
                break;
            default:
                colaListos.offer(proceso);
        }

        System.out.println("✅ Proceso agregado: " + proceso.getNombre() + " (PID: " + proceso.getPid() + ")");
    }

    // Agregar proceso por prioridad (1 = alta prioridad, 5 = baja prioridad)
    private void agregarPorPrioridad(Proceso proceso) {
        List<Proceso> listaProcesos = new ArrayList<>(colaListos);
        listaProcesos.add(proceso);

        // Ordenar por prioridad (menor número = mayor prioridad)
        listaProcesos.sort(Comparator.comparingInt(Proceso::getPrioridad));

        colaListos.clear();
        colaListos.addAll(listaProcesos);
    }

    // Ejecutar un ciclo de planificación
    public boolean ejecutarCiclo() {
        System.out.println("\n⏰ Tiempo actual: " + tiempoActual);

        // Si no hay proceso actual, tomar el siguiente de la cola
        if (procesoActual == null && !colaListos.isEmpty()) {
            procesoActual = colaListos.poll();
            procesoActual.setEstado(Proceso.EstadoProceso.EJECUTANDO);

            // Establecer tiempo de respuesta si es la primera vez que se ejecuta
            if (procesoActual.getTiempoRespuesta() == -1) {
                procesoActual.setTiempoRespuesta(tiempoActual - procesoActual.getTiempoLlegada());
            }

            tiempoQuantumRestante = quantum; // Reiniciar quantum
            System.out.println("🔄 Ejecutando proceso: " + procesoActual.getNombre());
        }

        // Ejecutar proceso actual
        if (procesoActual != null) {
            procesoActual.ejecutar();
            tiempoQuantumRestante--;

            System.out.println("▶️ " + procesoActual);

            // Verificar si el proceso terminó
            if (procesoActual.haTerminado()) {
                procesoActual.setTiempoFinalizacion(tiempoActual + 1);
                procesoActual.setTiempoEspera(
                        procesoActual.getTiempoFinalizacion() -
                                procesoActual.getTiempoLlegada() -
                                procesoActual.getTiempoEjecucion()
                );

                procesosTerminados.add(procesoActual);
                System.out.println("✅ Proceso terminado: " + procesoActual.getNombre());
                procesoActual = null;
                tiempoQuantumRestante = quantum;
            }
            // Para Round Robin: verificar si se agotó el quantum
            else if (algoritmo.equalsIgnoreCase("RR") || algoritmo.equalsIgnoreCase("ROUND_ROBIN")) {
                if (tiempoQuantumRestante <= 0) {
                    procesoActual.setEstado(Proceso.EstadoProceso.LISTO);
                    colaListos.offer(procesoActual);
                    System.out.println("⏰ Quantum agotado, proceso " + procesoActual.getNombre() + " va al final de la cola");
                    procesoActual = null;
                    tiempoQuantumRestante = quantum;
                }
            }
        }

        tiempoActual++;

        // Retornar true si hay más procesos por ejecutar
        return procesoActual != null || !colaListos.isEmpty();
    }

    // Ejecutar todos los procesos hasta completar
    public void ejecutarTodos() {
        System.out.println("\n🚀 Iniciando planificación con algoritmo: " + algoritmo);
        System.out.println("═══════════════════════════════════════════════════════════");

        while (ejecutarCiclo()) {
            // Pausa opcional para visualizar mejor (puedes comentar esta línea)
            try {
                Thread.sleep(500); // Pausa de 500ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("\n🏁 Todos los procesos han terminado!");
        mostrarEstadisticas();
    }

    // Mostrar estadísticas finales
    public void mostrarEstadisticas() {
        System.out.println("\n📊 ESTADÍSTICAS FINALES");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("Algoritmo utilizado: " + algoritmo);
        if (algoritmo.equalsIgnoreCase("RR") || algoritmo.equalsIgnoreCase("ROUND_ROBIN")) {
            System.out.println("Quantum utilizado: " + quantum);
        }
        System.out.println("Tiempo total de ejecución: " + tiempoActual);
        System.out.println("Procesos completados: " + procesosTerminados.size());

        System.out.println("\n📋 DETALLES DE PROCESOS:");
        System.out.println("PID | Nombre     | T.Llegada | T.Ejecución | T.Espera | T.Respuesta | T.Finalización");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────────");

        for (Proceso p : procesosTerminados) {
            System.out.printf("%3d | %-10s | %9d | %11d | %8d | %11d | %14d%n",
                    p.getPid(), p.getNombre(), p.getTiempoLlegada(), p.getTiempoEjecucion(),
                    p.getTiempoEspera(), p.getTiempoRespuesta(), p.getTiempoFinalizacion());
        }

        // Calcular promedios
        if (!procesosTerminados.isEmpty()) {
            double promedioEspera = procesosTerminados.stream().mapToInt(Proceso::getTiempoEspera).average().orElse(0);
            double promedioRespuesta = procesosTerminados.stream().mapToInt(Proceso::getTiempoRespuesta).average().orElse(0);

            System.out.println("─────────────────────────────────────────────────────────────────────────────────────");
            System.out.printf("PROMEDIOS: Tiempo de espera = %.2f | Tiempo de respuesta = %.2f%n",
                    promedioEspera, promedioRespuesta);
        }
    }

    // Getters
    public int getTiempoActual() { return tiempoActual; }
    public String getAlgoritmo() { return algoritmo; }
    public List<Proceso> getProcesosTerminados() { return procesosTerminados; }
    public int getQuantum() { return quantum; }

    // Setter para quantum
    public void setQuantum(int quantum) {
        this.quantum = quantum;
        this.tiempoQuantumRestante = quantum;
    }
}