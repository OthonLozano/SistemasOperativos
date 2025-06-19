package org.example.models;

public class BloqueMemoria {
    private int id;
    private int tamaño;
    private boolean ocupado;
    private int procesoId;
    private String nombreProceso;
    private TipoBloque tipo;

    public enum TipoBloque {
        LIBRE,      // Bloque disponible
        OCUPADO,    // Bloque asignado a proceso
        SISTEMA,    // Bloque reservado del sistema
        FRAGMENTADO // Fragmentación externa
    }

    // Constructor
    public BloqueMemoria(int id, int tamaño) {
        this.id = id;
        this.tamaño = tamaño;
        this.ocupado = false;
        this.procesoId = -1;
        this.nombreProceso = "";
        this.tipo = TipoBloque.LIBRE;
    }

    // Constructor con tipo específico
    public BloqueMemoria(int id, int tamaño, TipoBloque tipo) {
        this(id, tamaño);
        this.tipo = tipo;
        if (tipo == TipoBloque.SISTEMA) {
            this.ocupado = true;
            this.nombreProceso = "SISTEMA";
        }
    }

    // Asignar bloque a un proceso
    public void asignar(int procesoId, String nombreProceso) {
        this.ocupado = true;
        this.procesoId = procesoId;
        this.nombreProceso = nombreProceso;
        this.tipo = TipoBloque.OCUPADO;
    }

    // Liberar bloque
    public void liberar() {
        this.ocupado = false;
        this.procesoId = -1;
        this.nombreProceso = "";
        this.tipo = TipoBloque.LIBRE;
    }

    // Getters y Setters
    public int getId() { return id; }
    public int getTamaño() { return tamaño; }
    public boolean isOcupado() { return ocupado; }
    public int getProcesoId() { return procesoId; }
    public String getNombreProceso() { return nombreProceso; }
    public TipoBloque getTipo() { return tipo; }

    public void setTipo(TipoBloque tipo) { this.tipo = tipo; }
    public void setTamaño(int tamaño) { this.tamaño = tamaño; }

    // Método para obtener información del bloque
    public String getInfo() {
        return String.format("Bloque %d: %dKB - %s %s",
                id, tamaño, tipo,
                ocupado ? "(" + nombreProceso + ")" : "");
    }

    @Override
    public String toString() {
        return getInfo();
    }
}