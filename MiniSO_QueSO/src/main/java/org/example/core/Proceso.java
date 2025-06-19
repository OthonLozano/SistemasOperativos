package org.example.core;

public class Proceso {
    // Atributos del proceso
    private int pid;                    //  único del proceso
    private String nombre;              // Nombre del proceso
    private int tiempoLlegada;         // Tiempo en que llega el proceso
    private int tiempoEjecucion;       // Tiempo total que necesita para ejecutarse
    private int tiempoRestante;        // Tiempo que le falta por ejecutar
    private int prioridad;             // Prioridad del proceso (1 = alta, 5 = baja)
    private EstadoProceso estado;      // Estado actual del proceso
    private int tiempoEspera;          // Tiempo que ha esperado
    private int tiempoRespuesta;       // Tiempo de respuesta
    private int tiempoFinalizacion;    // Tiempo en que terminó el proceso

    // Enum para los estados del proceso
    public enum EstadoProceso {
        NUEVO,          // Recién creado
        LISTO,          // Esperando a ser ejecutado
        EJECUTANDO,     // Actualmente en ejecución
        BLOQUEADO,      // Esperando algún recurso
        TERMINADO       // Ha finalizado su ejecución
    }

    // Constructor
    public Proceso(int pid, String nombre, int tiempoLlegada, int tiempoEjecucion, int prioridad) {
        this.pid = pid;
        this.nombre = nombre;
        this.tiempoLlegada = tiempoLlegada;
        this.tiempoEjecucion = tiempoEjecucion;
        this.tiempoRestante = tiempoEjecucion;
        this.prioridad = prioridad;
        this.estado = EstadoProceso.NUEVO;
        this.tiempoEspera = 0;
        this.tiempoRespuesta = -1;
        this.tiempoFinalizacion = -1;
    }

    // Getters y Setters
    public int getPid() { return pid; }
    public String getNombre() { return nombre; }
    public int getTiempoLlegada() { return tiempoLlegada; }
    public int getTiempoEjecucion() { return tiempoEjecucion; }
    public int getTiempoRestante() { return tiempoRestante; }
    public int getPrioridad() { return prioridad; }
    public EstadoProceso getEstado() { return estado; }
    public int getTiempoEspera() { return tiempoEspera; }
    public int getTiempoRespuesta() { return tiempoRespuesta; }
    public int getTiempoFinalizacion() { return tiempoFinalizacion; }

    public void setTiempoRestante(int tiempoRestante) { this.tiempoRestante = tiempoRestante; }
    public void setEstado(EstadoProceso estado) { this.estado = estado; }
    public void setTiempoEspera(int tiempoEspera) { this.tiempoEspera = tiempoEspera; }
    public void setTiempoRespuesta(int tiempoRespuesta) { this.tiempoRespuesta = tiempoRespuesta; }
    public void setTiempoFinalizacion(int tiempoFinalizacion) { this.tiempoFinalizacion = tiempoFinalizacion; }

    // Método para ejecutar el proceso por una unidad de tiempo
    public void ejecutar() {
        if (tiempoRestante > 0) {
            tiempoRestante--;
            if (tiempoRestante == 0) {
                estado = EstadoProceso.TERMINADO;
            }
        }
    }

    // Método para verificar si el proceso ha terminado
    public boolean haTerminado() {
        return estado == EstadoProceso.TERMINADO;
    }

    // Método para mostrar información detallada del proceso
    public void mostrarInfo() {
        System.out.println("═══════════════════════════════════════");
        System.out.println("           INFORMACIÓN DEL PROCESO");
        System.out.println("═══════════════════════════════════════");
        System.out.println("PID: " + pid);
        System.out.println("Nombre: " + nombre);
        System.out.println("Estado: " + estado);
        System.out.println("Tiempo de llegada: " + tiempoLlegada);
        System.out.println("Tiempo de ejecución: " + tiempoEjecucion);
        System.out.println("Tiempo restante: " + tiempoRestante);
        System.out.println("Prioridad: " + prioridad);
        System.out.println("Tiempo de espera: " + tiempoEspera);
        if (tiempoRespuesta != -1) {
            System.out.println("Tiempo de respuesta: " + tiempoRespuesta);
        }
        if (tiempoFinalizacion != -1) {
            System.out.println("Tiempo de finalización: " + tiempoFinalizacion);
        }
        System.out.println("═══════════════════════════════════════");
    }

    // Método toString para mostrar información del proceso en una línea
    @Override
    public String toString() {
        return String.format("PID: %d | Nombre: %-10s | Estado: %-10s | Tiempo restante: %2d | Prioridad: %d",
                pid, nombre, estado, tiempoRestante, prioridad);
    }
}