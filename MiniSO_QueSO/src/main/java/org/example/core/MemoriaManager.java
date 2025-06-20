package org.example.core;

import org.example.models.BloqueMemoria;
import java.util.*;

/**
 * Gestor de memoria para el sistema operativo académico MiniSO.
 *
 * Esta clase implementa diferentes algoritmos de asignación de memoria:
 * - First Fit (Primer Ajuste)
 * - Best Fit (Mejor Ajuste)
 * - Worst Fit (Peor Ajuste)
 * - Paginación
 *
 * La memoria se gestiona mediante bloques que pueden estar libres, ocupados,
 * reservados para el sistema o fragmentados.
 */
public class MemoriaManager {
    /** Lista de bloques de memoria que componen el espacio total de memoria */
    private List<BloqueMemoria> bloques;

    /** Tamaño total de memoria disponible en KB */
    private int memoriaTotal;

    /** Cantidad de memoria actualmente disponible para asignación en KB */
    private int memoriaDisponible;

    /** Algoritmo de asignación de memoria actualmente en uso */
    private AlgoritmoAsignacion algoritmo;

    /** Mapa que relaciona cada proceso con sus bloques de memoria asignados */
    private Map<Integer, List<BloqueMemoria>> procesosEnMemoria;

    /**
     * Enumeración que define los algoritmos de asignación de memoria disponibles.
     */
    public enum AlgoritmoAsignacion {
        FIRST_FIT,      // Primer ajuste: asigna el primer bloque disponible que sea suficiente
        BEST_FIT,       // Mejor ajuste: asigna el bloque más pequeño que sea suficiente
        WORST_FIT,      // Peor ajuste: asigna el bloque más grande disponible
        PAGINACION      // Paginación: divide la memoria en páginas de tamaño fijo
    }

    /**
     * Constructor principal del gestor de memoria.
     *
     * Inicializa el sistema de memoria con el tamaño especificado,
     * reservando automáticamente el 10% para el sistema operativo.
     *
     * @param memoriaTotal Tamaño total de memoria en KB
     */
    public MemoriaManager(int memoriaTotal) {
        this.memoriaTotal = memoriaTotal;
        this.bloques = new ArrayList<>();
        this.procesosEnMemoria = new HashMap<>();
        this.algoritmo = AlgoritmoAsignacion.FIRST_FIT; // Algoritmo por defecto

        inicializarMemoria();
    }

    /**
     * Inicializa la estructura de memoria del sistema.
     *
     * Crea dos bloques iniciales:
     * 1. Bloque del sistema (10% de la memoria total) - Reservado para el SO
     * 2. Bloque libre (90% restante) - Disponible para procesos de usuario
     */
    private void inicializarMemoria() {
        // Reservar memoria del sistema (10% del total)
        int memoriaSistema = memoriaTotal / 10;
        bloques.add(new BloqueMemoria(0, memoriaSistema, BloqueMemoria.TipoBloque.SISTEMA));

        // Resto de memoria disponible para procesos de usuario
        int memoriaUsuario = memoriaTotal - memoriaSistema;
        bloques.add(new BloqueMemoria(1, memoriaUsuario, BloqueMemoria.TipoBloque.LIBRE));

        // Actualizar contador de memoria disponible
        calcularMemoriaDisponible();
    }

    /**
     * Asigna memoria a un proceso utilizando el algoritmo configurado.
     *
     * @param procesoId Identificador único del proceso
     * @param nombreProceso Nombre descriptivo del proceso
     * @param tamaño Cantidad de memoria requerida en KB
     * @return true si la asignación fue exitosa, false si no hay memoria suficiente
     */
    public boolean asignarMemoria(int procesoId, String nombreProceso, int tamaño) {
        // Validar parámetros de entrada
        if (tamaño <= 0) {
            throw new IllegalArgumentException("El tamaño debe ser mayor a 0");
        }

        // Delegar a la implementación específica según el algoritmo configurado
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
                // Fallback al algoritmo por defecto
                return firstFit(procesoId, nombreProceso, tamaño);
        }
    }

    /**
     * Implementación del algoritmo First Fit (Primer Ajuste).
     *
     * Busca el primer bloque libre que tenga suficiente espacio para el proceso.
     * Es el algoritmo más rápido pero puede generar fragmentación externa.
     *
     * @param procesoId ID del proceso a asignar
     * @param nombreProceso Nombre del proceso
     * @param tamaño Memoria requerida en KB
     * @return true si se pudo asignar la memoria
     */
    private boolean firstFit(int procesoId, String nombreProceso, int tamaño) {
        // Recorrer todos los bloques buscando el primero que sea suficiente
        for (int i = 0; i < bloques.size(); i++) {
            BloqueMemoria bloque = bloques.get(i);

            // Verificar que el bloque esté libre y tenga suficiente espacio
            if (bloque.getTipo() == BloqueMemoria.TipoBloque.LIBRE && bloque.getTamaño() >= tamaño) {
                // Dividir el bloque y asignar la memoria
                dividirBloque(i, procesoId, nombreProceso, tamaño);
                registrarProcesoEnMemoria(procesoId, bloque);
                calcularMemoriaDisponible();
                return true;
            }
        }
        return false; // No se encontró espacio suficiente
    }

    /**
     * Implementación del algoritmo Best Fit (Mejor Ajuste).
     *
     * Busca el bloque libre más pequeño que sea suficiente para el proceso.
     * Minimiza la fragmentación externa pero es más lento que First Fit.
     *
     * @param procesoId ID del proceso a asignar
     * @param nombreProceso Nombre del proceso
     * @param tamaño Memoria requerida en KB
     * @return true si se pudo asignar la memoria
     */
    private boolean bestFit(int procesoId, String nombreProceso, int tamaño) {
        int mejorIndice = -1;
        int menorDiferencia = Integer.MAX_VALUE;

        // Buscar el bloque con menor desperdicio de espacio
        for (int i = 0; i < bloques.size(); i++) {
            BloqueMemoria bloque = bloques.get(i);

            // Solo considerar bloques libres con suficiente espacio
            if (bloque.getTipo() == BloqueMemoria.TipoBloque.LIBRE && bloque.getTamaño() >= tamaño) {
                int diferencia = bloque.getTamaño() - tamaño;

                // Si esta diferencia es menor, este es un mejor candidato
                if (diferencia < menorDiferencia) {
                    menorDiferencia = diferencia;
                    mejorIndice = i;
                }
            }
        }

        // Si se encontró un bloque adecuado, proceder con la asignación
        if (mejorIndice != -1) {
            dividirBloque(mejorIndice, procesoId, nombreProceso, tamaño);
            registrarProcesoEnMemoria(procesoId, bloques.get(mejorIndice));
            calcularMemoriaDisponible();
            return true;
        }
        return false;
    }

    /**
     * Implementación del algoritmo Worst Fit (Peor Ajuste).
     *
     * Busca el bloque libre más grande disponible para asignar al proceso.
     * Intenta dejar fragmentos grandes que puedan ser útiles para procesos futuros.
     *
     * @param procesoId ID del proceso a asignar
     * @param nombreProceso Nombre del proceso
     * @param tamaño Memoria requerida en KB
     * @return true si se pudo asignar la memoria
     */
    private boolean worstFit(int procesoId, String nombreProceso, int tamaño) {
        int peorIndice = -1;
        int mayorDiferencia = -1;

        // Buscar el bloque más grande que pueda contener el proceso
        for (int i = 0; i < bloques.size(); i++) {
            BloqueMemoria bloque = bloques.get(i);

            // Solo considerar bloques libres con suficiente espacio
            if (bloque.getTipo() == BloqueMemoria.TipoBloque.LIBRE && bloque.getTamaño() >= tamaño) {
                int diferencia = bloque.getTamaño() - tamaño;

                // Si esta diferencia es mayor, este es el peor ajuste (que es lo que buscamos)
                if (diferencia > mayorDiferencia) {
                    mayorDiferencia = diferencia;
                    peorIndice = i;
                }
            }
        }

        // Si se encontró un bloque adecuado, proceder con la asignación
        if (peorIndice != -1) {
            dividirBloque(peorIndice, procesoId, nombreProceso, tamaño);
            registrarProcesoEnMemoria(procesoId, bloques.get(peorIndice));
            calcularMemoriaDisponible();
            return true;
        }
        return false;
    }

    /**
     * Implementación del algoritmo de Paginación.
     *
     * Divide la memoria en páginas de tamaño fijo (32KB) y asigna las páginas
     * necesarias para el proceso, incluso si no son contiguas.
     * Elimina la fragmentación externa pero puede crear fragmentación interna.
     *
     * @param procesoId ID del proceso a asignar
     * @param nombreProceso Nombre del proceso
     * @param tamaño Memoria requerida en KB
     * @return true si se pudieron asignar todas las páginas necesarias
     */
    private boolean asignarPorPaginacion(int procesoId, String nombreProceso, int tamaño) {
        // Configuración de paginación
        final int TAMAÑO_PAGINA = 32; // 32KB por página (tamaño realista para demostración)

        // Calcular número de páginas necesarias (redondear hacia arriba)
        int paginasNecesarias = (int) Math.ceil((double) tamaño / TAMAÑO_PAGINA);

        // Estructuras para rastrear la asignación
        List<BloqueMemoria> paginasAsignadas = new ArrayList<>();
        List<Integer> indicesUsados = new ArrayList<>();

        // Buscar espacios libres y asignar páginas
        for (int i = 0; i < bloques.size() && paginasAsignadas.size() < paginasNecesarias; i++) {
            BloqueMemoria bloque = bloques.get(i);

            // Solo procesar bloques libres
            if (bloque.getTipo() == BloqueMemoria.TipoBloque.LIBRE) {
                // Calcular cuántas páginas completas caben en este bloque
                int paginasEnBloque = bloque.getTamaño() / TAMAÑO_PAGINA;

                if (paginasEnBloque > 0) {
                    // Determinar cuántas páginas usar de este bloque
                    int paginasAUsar = Math.min(paginasEnBloque, paginasNecesarias - paginasAsignadas.size());

                    // Crear páginas individuales para mejor visualización
                    for (int j = 0; j < paginasAUsar; j++) {
                        // Crear una nueva página con ID único
                        BloqueMemoria pagina = new BloqueMemoria(
                                procesoId * 1000 + paginasAsignadas.size(), // ID único basado en proceso y número de página
                                TAMAÑO_PAGINA
                        );
                        // Asignar la página al proceso con nombre descriptivo
                        pagina.asignar(procesoId, nombreProceso + "_P" + (paginasAsignadas.size() + 1));
                        paginasAsignadas.add(pagina);
                    }

                    // Reducir el tamaño del bloque libre o marcarlo para eliminación
                    int memoriaUsada = paginasAUsar * TAMAÑO_PAGINA;
                    if (bloque.getTamaño() > memoriaUsada) {
                        // Bloque parcialmente usado - reducir su tamaño
                        bloque.setTamaño(bloque.getTamaño() - memoriaUsada);
                    } else {
                        // Bloque completamente usado - marcar para eliminación
                        indicesUsados.add(i);
                    }
                }
            }
        }

        // Verificar si se asignaron todas las páginas necesarias
        if (paginasAsignadas.size() >= paginasNecesarias) {
            // Eliminar bloques completamente usados (en orden inverso para mantener índices válidos)
            Collections.sort(indicesUsados, Collections.reverseOrder());
            for (int indice : indicesUsados) {
                bloques.remove(indice);
            }

            // Agregar las páginas asignadas a la lista de bloques para visualización
            bloques.addAll(paginasAsignadas);

            // Registrar el proceso y sus páginas
            procesosEnMemoria.put(procesoId, new ArrayList<>(paginasAsignadas));
            calcularMemoriaDisponible();
            return true;
        }

        return false; // No se pudieron asignar todas las páginas necesarias
    }

    /**
     * Divide un bloque libre en dos partes: una asignada al proceso y otra que permanece libre.
     *
     * @param indice Índice del bloque a dividir en la lista de bloques
     * @param procesoId ID del proceso que recibirá la memoria
     * @param nombreProceso Nombre del proceso
     * @param tamaño Cantidad de memoria a asignar del bloque
     */
    private void dividirBloque(int indice, int procesoId, String nombreProceso, int tamaño) {
        BloqueMemoria bloqueOriginal = bloques.get(indice);

        // Crear nuevo bloque para el proceso con un ID derivado del original
        BloqueMemoria bloqueAsignado = new BloqueMemoria(
                bloqueOriginal.getId() * 10, // ID único basado en el bloque original
                tamaño
        );
        bloqueAsignado.asignar(procesoId, nombreProceso);

        // Calcular espacio restante después de la asignación
        int espacioRestante = bloqueOriginal.getTamaño() - tamaño;

        // Reemplazar el bloque original con el bloque asignado
        bloques.set(indice, bloqueAsignado);

        // Si queda espacio, crear un nuevo bloque libre para el resto
        if (espacioRestante > 0) {
            BloqueMemoria bloqueLibre = new BloqueMemoria(
                    bloqueOriginal.getId() * 10 + 1, // ID único para el bloque restante
                    espacioRestante
            );
            // Insertar el bloque libre inmediatamente después del asignado
            bloques.add(indice + 1, bloqueLibre);
        }
    }

    /**
     * Registra un proceso en el mapa de procesos en memoria.
     *
     * @param procesoId ID del proceso
     * @param bloque Bloque de memoria asignado al proceso
     */
    private void registrarProcesoEnMemoria(int procesoId, BloqueMemoria bloque) {
        // Usar computeIfAbsent para crear la lista si no existe
        procesosEnMemoria.computeIfAbsent(procesoId, k -> new ArrayList<>()).add(bloque);
    }

    /**
     * Libera toda la memoria asignada a un proceso específico.
     *
     * El comportamiento varía según el algoritmo:
     * - Para paginación: elimina las páginas y consolida la memoria liberada
     * - Para otros algoritmos: marca los bloques como libres y consolida bloques adyacentes
     *
     * @param procesoId ID del proceso cuya memoria se va a liberar
     */
    public void liberarMemoria(int procesoId) {
        List<BloqueMemoria> bloquesDelProceso = procesosEnMemoria.get(procesoId);

        if (bloquesDelProceso != null) {
            if (algoritmo == AlgoritmoAsignacion.PAGINACION) {
                // Manejo especial para paginación
                liberarMemoriaPaginacion(procesoId, bloquesDelProceso);
            } else {
                // Liberación estándar para algoritmos de particiones
                liberarMemoriaEstandar(bloquesDelProceso);
            }

            // Remover el proceso del registro
            procesosEnMemoria.remove(procesoId);
            calcularMemoriaDisponible();
        }
    }

    /**
     * Libera memoria para el algoritmo de paginación.
     *
     * @param procesoId ID del proceso
     * @param bloquesDelProceso Lista de páginas asignadas al proceso
     */
    private void liberarMemoriaPaginacion(int procesoId, List<BloqueMemoria> bloquesDelProceso) {
        // Eliminar todas las páginas del proceso de la lista de bloques
        for (BloqueMemoria pagina : bloquesDelProceso) {
            bloques.remove(pagina);
        }

        // Calcular memoria total liberada
        int memoriaLiberada = bloquesDelProceso.stream()
                .mapToInt(BloqueMemoria::getTamaño)
                .sum();

        // Intentar consolidar la memoria liberada en un bloque libre existente
        boolean consolidado = false;
        for (BloqueMemoria bloque : bloques) {
            if (bloque.getTipo() == BloqueMemoria.TipoBloque.LIBRE) {
                // Agregar la memoria liberada al primer bloque libre encontrado
                bloque.setTamaño(bloque.getTamaño() + memoriaLiberada);
                consolidado = true;
                break;
            }
        }

        // Si no hay bloques libres existentes, crear uno nuevo
        if (!consolidado) {
            BloqueMemoria nuevoLibre = new BloqueMemoria(
                    9999 + procesoId, // ID único para el nuevo bloque libre
                    memoriaLiberada
            );
            bloques.add(nuevoLibre);
        }
    }

    /**
     * Libera memoria para algoritmos estándar (First/Best/Worst Fit).
     *
     * @param bloquesDelProceso Lista de bloques asignados al proceso
     */
    private void liberarMemoriaEstandar(List<BloqueMemoria> bloquesDelProceso) {
        // Marcar todos los bloques del proceso como libres
        for (BloqueMemoria bloque : bloquesDelProceso) {
            bloque.liberar();
        }

        // Consolidar bloques libres adyacentes para reducir fragmentación
        consolidarBloquesLibres();
    }

    /**
     * Consolida bloques libres adyacentes en un solo bloque.
     *
     * Este método reduce la fragmentación externa fusionando bloques
     * libres consecutivos en la memoria.
     */
    private void consolidarBloquesLibres() {
        // Recorrer la lista buscando bloques libres adyacentes
        for (int i = 0; i < bloques.size() - 1; i++) {
            BloqueMemoria actual = bloques.get(i);
            BloqueMemoria siguiente = bloques.get(i + 1);

            // Si ambos bloques están libres, fusionarlos
            if (actual.getTipo() == BloqueMemoria.TipoBloque.LIBRE &&
                    siguiente.getTipo() == BloqueMemoria.TipoBloque.LIBRE) {

                // Fusionar: aumentar el tamaño del bloque actual
                actual.setTamaño(actual.getTamaño() + siguiente.getTamaño());

                // Eliminar el bloque siguiente (ya fusionado)
                bloques.remove(i + 1);

                // Revisar nuevamente desde la posición actual por si hay más bloques adyacentes
                i--;
            }
        }
    }

    /**
     * Recalcula la cantidad de memoria disponible sumando todos los bloques libres.
     */
    private void calcularMemoriaDisponible() {
        memoriaDisponible = 0;
        for (BloqueMemoria bloque : bloques) {
            if (bloque.getTipo() == BloqueMemoria.TipoBloque.LIBRE) {
                memoriaDisponible += bloque.getTamaño();
            }
        }
    }

    /**
     * Calcula el porcentaje de fragmentación en la memoria.
     *
     * La fragmentación se mide como el porcentaje de bloques libres
     * respecto al total de bloques. Más bloques libres pequeños
     * indican mayor fragmentación.
     *
     * @return Porcentaje de fragmentación (0.0 - 100.0)
     */
    private double calcularFragmentacion() {
        int bloquesLibres = 0;

        // Contar bloques libres
        for (BloqueMemoria bloque : bloques) {
            if (bloque.getTipo() == BloqueMemoria.TipoBloque.LIBRE) {
                bloquesLibres++;
            }
        }

        // Si no hay memoria disponible, no hay fragmentación
        if (memoriaDisponible == 0) return 0.0;

        // Fragmentación = (bloques libres / total bloques) * 100
        // Más de 1 bloque libre indica fragmentación
        return (bloquesLibres > 1) ? ((double) bloquesLibres / bloques.size()) * 100 : 0.0;
    }

    /** @return Copia de la lista de bloques de memoria */
    public List<BloqueMemoria> getBloques() {
        return new ArrayList<>(bloques);
    }

    /** @return Tamaño total de memoria en KB */
    public int getMemoriaTotal() {
        return memoriaTotal;
    }

    /** @return Memoria actualmente disponible en KB */
    public int getMemoriaDisponible() {
        return memoriaDisponible;
    }

    /** @return Memoria actualmente en uso en KB */
    public int getMemoriaUsada() {
        return memoriaTotal - memoriaDisponible;
    }

    /** @return Algoritmo de asignación actualmente configurado */
    public AlgoritmoAsignacion getAlgoritmo() {
        return algoritmo;
    }

    /** @return Mapa de procesos y sus bloques asignados */
    public Map<Integer, List<BloqueMemoria>> getProcesosEnMemoria() {
        return procesosEnMemoria;
    }

    /**
     * Cambia el algoritmo de asignación de memoria.
     *
     * @param algoritmo Nuevo algoritmo a utilizar
     */
    public void setAlgoritmo(AlgoritmoAsignacion algoritmo) {
        this.algoritmo = algoritmo;
    }

    /**
     * Genera una cadena con estadísticas actuales del sistema de memoria.
     *
     * @return String formateado con estadísticas de memoria y fragmentación
     */
    public String getEstadisticas() {
        return String.format(
                "Memoria Total: %d KB | Usada: %d KB | Disponible: %d KB | Fragmentación: %.1f%%",
                memoriaTotal, getMemoriaUsada(), memoriaDisponible,
                calcularFragmentacion()
        );
    }

    /**
     * Reinicia completamente el sistema de memoria.
     *
     * Limpia todos los bloques y procesos, y reinicializa la memoria
     * al estado original con solo los bloques del sistema y usuario.
     */
    public void reiniciar() {
        bloques.clear();
        procesosEnMemoria.clear();
        inicializarMemoria();
    }
}