package org.example.core;

import org.example.models.BloqueMemoria;
import java.util.*;

public class MemoriaManager {

    private List<BloqueMemoria> bloques;
    private int memoriaTotal;
    private int memoriaDisponible;
    private AlgoritmoAsignacion algoritmo;
    private Map<Integer, List<BloqueMemoria>> procesosEnMemoria;

    public enum AlgoritmoAsignacion {
        FIRST_FIT,      // Primer ajuste
        BEST_FIT,       // Mejor ajuste
        WORST_FIT,      // Peor ajuste
        PAGINACION      // Paginación
    }

    // Constructor
    public MemoriaManager(int memoriaTotal) {
        this.memoriaTotal = memoriaTotal;
        this.bloques = new ArrayList<>();
        this.procesosEnMemoria = new HashMap<>();
        this.algoritmo = AlgoritmoAsignacion.FIRST_FIT;

        inicializarMemoria();
    }

    private void inicializarMemoria() {
        // Reservar memoria del sistema (10%)
        int memoriaSistema = memoriaTotal / 10;
        bloques.add(new BloqueMemoria(0, memoriaSistema, BloqueMemoria.TipoBloque.SISTEMA));

        // Resto de memoria disponible
        int memoriaUsuario = memoriaTotal - memoriaSistema;
        bloques.add(new BloqueMemoria(1, memoriaUsuario, BloqueMemoria.TipoBloque.LIBRE));

        calcularMemoriaDisponible();
    }

    // Asignar memoria a un proceso
    public boolean asignarMemoria(int procesoId, String nombreProceso, int tamaño) {
        switch (algoritmo) {
            case FIRST_FIT:
                return firstFit(procesoId, nombreProceso, tamaño);
            case BEST_FIT:
                return bestFit(procesoId, nombreProceso, tamaño);
            case WORST_FIT:
                return worstFit(procesoId, nombreProceso, tamaño);
            case PAGINACION:
                return asignarPorPaginacion(procesoId, nombreProceso, tamaño);
            default:
                return firstFit(procesoId, nombreProceso, tamaño);
        }
    }

    private boolean firstFit(int procesoId, String nombreProceso, int tamaño) {
        for (int i = 0; i < bloques.size(); i++) {
            BloqueMemoria bloque = bloques.get(i);

            if (bloque.getTipo() == BloqueMemoria.TipoBloque.LIBRE && bloque.getTamaño() >= tamaño) {
                dividirBloque(i, procesoId, nombreProceso, tamaño);
                registrarProcesoEnMemoria(procesoId, bloque);
                calcularMemoriaDisponible();
                return true;
            }
        }
        return false; // No hay espacio suficiente
    }

    private boolean bestFit(int procesoId, String nombreProceso, int tamaño) {
        int mejorIndice = -1;
        int menorDiferencia = Integer.MAX_VALUE;

        for (int i = 0; i < bloques.size(); i++) {
            BloqueMemoria bloque = bloques.get(i);

            if (bloque.getTipo() == BloqueMemoria.TipoBloque.LIBRE && bloque.getTamaño() >= tamaño) {
                int diferencia = bloque.getTamaño() - tamaño;
                if (diferencia < menorDiferencia) {
                    menorDiferencia = diferencia;
                    mejorIndice = i;
                }
            }
        }

        if (mejorIndice != -1) {
            dividirBloque(mejorIndice, procesoId, nombreProceso, tamaño);
            registrarProcesoEnMemoria(procesoId, bloques.get(mejorIndice));
            calcularMemoriaDisponible();
            return true;
        }
        return false;
    }

    private boolean worstFit(int procesoId, String nombreProceso, int tamaño) {
        int peorIndice = -1;
        int mayorDiferencia = -1;

        for (int i = 0; i < bloques.size(); i++) {
            BloqueMemoria bloque = bloques.get(i);

            if (bloque.getTipo() == BloqueMemoria.TipoBloque.LIBRE && bloque.getTamaño() >= tamaño) {
                int diferencia = bloque.getTamaño() - tamaño;
                if (diferencia > mayorDiferencia) {
                    mayorDiferencia = diferencia;
                    peorIndice = i;
                }
            }
        }

        if (peorIndice != -1) {
            dividirBloque(peorIndice, procesoId, nombreProceso, tamaño);
            registrarProcesoEnMemoria(procesoId, bloques.get(peorIndice));
            calcularMemoriaDisponible();
            return true;
        }
        return false;
    }

    // MÉTODO CORREGIDO - PAGINACIÓN CON VISUALIZACIÓN
    private boolean asignarPorPaginacion(int procesoId, String nombreProceso, int tamaño) {
        // Simulación de paginación mejorada
        int tamañoPagina = 32; // 32KB por página (más realista para visualización)
        int paginasNecesarias = (int) Math.ceil((double) tamaño / tamañoPagina);

        List<BloqueMemoria> paginasAsignadas = new ArrayList<>();
        List<Integer> indicesUsados = new ArrayList<>();

        // Buscar espacios libres y asignar páginas
        for (int i = 0; i < bloques.size() && paginasAsignadas.size() < paginasNecesarias; i++) {
            BloqueMemoria bloque = bloques.get(i);

            if (bloque.getTipo() == BloqueMemoria.TipoBloque.LIBRE) {
                int paginasEnBloque = bloque.getTamaño() / tamañoPagina;

                if (paginasEnBloque > 0) {
                    int paginasAUsar = Math.min(paginasEnBloque, paginasNecesarias - paginasAsignadas.size());

                    for (int j = 0; j < paginasAUsar; j++) {
                        // Crear página individual
                        BloqueMemoria pagina = new BloqueMemoria(
                                procesoId * 1000 + paginasAsignadas.size(), // ID único
                                tamañoPagina
                        );
                        pagina.asignar(procesoId, nombreProceso + "_P" + (paginasAsignadas.size() + 1));
                        paginasAsignadas.add(pagina);
                    }

                    // Reducir el bloque libre o eliminarlo
                    int memoriaUsada = paginasAUsar * tamañoPagina;
                    if (bloque.getTamaño() > memoriaUsada) {
                        bloque.setTamaño(bloque.getTamaño() - memoriaUsada);
                    } else {
                        indicesUsados.add(i);
                    }
                }
            }
        }

        // Verificar si se asignaron todas las páginas necesarias
        if (paginasAsignadas.size() >= paginasNecesarias) {
            // Eliminar bloques completamente usados (en orden inverso)
            Collections.sort(indicesUsados, Collections.reverseOrder());
            for (int indice : indicesUsados) {
                bloques.remove(indice);
            }

            // Agregar las páginas al final de la lista de bloques para visualización
            bloques.addAll(paginasAsignadas);

            // Registrar el proceso
            procesosEnMemoria.put(procesoId, new ArrayList<>(paginasAsignadas));
            calcularMemoriaDisponible();
            return true;
        }

        return false; // No se pudieron asignar todas las páginas
    }

    private void dividirBloque(int indice, int procesoId, String nombreProceso, int tamaño) {
        BloqueMemoria bloqueOriginal = bloques.get(indice);

        // Crear bloque para el proceso
        BloqueMemoria bloqueAsignado = new BloqueMemoria(
                bloqueOriginal.getId() * 10, tamaño
        );
        bloqueAsignado.asignar(procesoId, nombreProceso);

        // Si queda espacio, crear bloque libre
        int espacioRestante = bloqueOriginal.getTamaño() - tamaño;
        bloques.set(indice, bloqueAsignado);

        if (espacioRestante > 0) {
            BloqueMemoria bloqueLibre = new BloqueMemoria(
                    bloqueOriginal.getId() * 10 + 1, espacioRestante
            );
            bloques.add(indice + 1, bloqueLibre);
        }
    }

    private void registrarProcesoEnMemoria(int procesoId, BloqueMemoria bloque) {
        procesosEnMemoria.computeIfAbsent(procesoId, k -> new ArrayList<>()).add(bloque);
    }

    // MÉTODO CORREGIDO - LIBERAR MEMORIA CON PAGINACIÓN
    public void liberarMemoria(int procesoId) {
        List<BloqueMemoria> bloquesDelProceso = procesosEnMemoria.get(procesoId);

        if (bloquesDelProceso != null) {
            // Si es paginación, eliminar páginas de la lista de bloques
            if (algoritmo == AlgoritmoAsignacion.PAGINACION) {
                for (BloqueMemoria pagina : bloquesDelProceso) {
                    bloques.remove(pagina);
                }

                // Consolidar memoria liberada en un bloque libre
                int memoriaLiberada = bloquesDelProceso.stream()
                        .mapToInt(BloqueMemoria::getTamaño)
                        .sum();

                // Buscar si hay bloques libres para consolidar
                boolean consolidado = false;
                for (BloqueMemoria bloque : bloques) {
                    if (bloque.getTipo() == BloqueMemoria.TipoBloque.LIBRE) {
                        bloque.setTamaño(bloque.getTamaño() + memoriaLiberada);
                        consolidado = true;
                        break;
                    }
                }

                // Si no hay bloques libres, crear uno nuevo
                if (!consolidado) {
                    BloqueMemoria nuevoLibre = new BloqueMemoria(
                            9999 + procesoId, memoriaLiberada
                    );
                    bloques.add(nuevoLibre);
                }
            } else {
                // Liberación normal para otros algoritmos
                for (BloqueMemoria bloque : bloquesDelProceso) {
                    bloque.liberar();
                }
                consolidarBloquesLibres();
            }

            procesosEnMemoria.remove(procesoId);
            calcularMemoriaDisponible();
        }
    }

    private void consolidarBloquesLibres() {
        // Consolidar bloques libres adyacentes
        for (int i = 0; i < bloques.size() - 1; i++) {
            BloqueMemoria actual = bloques.get(i);
            BloqueMemoria siguiente = bloques.get(i + 1);

            if (actual.getTipo() == BloqueMemoria.TipoBloque.LIBRE &&
                    siguiente.getTipo() == BloqueMemoria.TipoBloque.LIBRE) {

                // Fusionar bloques
                actual.setTamaño(actual.getTamaño() + siguiente.getTamaño());
                bloques.remove(i + 1);
                i--; // Revisar nuevamente desde la posición actual
            }
        }
    }

    private void calcularMemoriaDisponible() {
        memoriaDisponible = 0;
        for (BloqueMemoria bloque : bloques) {
            if (bloque.getTipo() == BloqueMemoria.TipoBloque.LIBRE) {
                memoriaDisponible += bloque.getTamaño();
            }
        }
    }

    // Getters
    public List<BloqueMemoria> getBloques() { return new ArrayList<>(bloques); }
    public int getMemoriaTotal() { return memoriaTotal; }
    public int getMemoriaDisponible() { return memoriaDisponible; }
    public int getMemoriaUsada() { return memoriaTotal - memoriaDisponible; }
    public AlgoritmoAsignacion getAlgoritmo() { return algoritmo; }
    public Map<Integer, List<BloqueMemoria>> getProcesosEnMemoria() { return procesosEnMemoria; }

    // Setters
    public void setAlgoritmo(AlgoritmoAsignacion algoritmo) { this.algoritmo = algoritmo; }

    // Método para obtener estadísticas
    public String getEstadisticas() {
        return String.format(
                "Memoria Total: %d KB | Usada: %d KB | Disponible: %d KB | Fragmentación: %.1f%%",
                memoriaTotal, getMemoriaUsada(), memoriaDisponible,
                calcularFragmentacion()
        );
    }

    private double calcularFragmentacion() {
        int bloquesLibres = 0;
        for (BloqueMemoria bloque : bloques) {
            if (bloque.getTipo() == BloqueMemoria.TipoBloque.LIBRE) {
                bloquesLibres++;
            }
        }

        if (memoriaDisponible == 0) return 0.0;
        return (bloquesLibres > 1) ? ((double) bloquesLibres / bloques.size()) * 100 : 0.0;
    }

    // Reiniciar memoria
    public void reiniciar() {
        bloques.clear();
        procesosEnMemoria.clear();
        inicializarMemoria();
    }
}