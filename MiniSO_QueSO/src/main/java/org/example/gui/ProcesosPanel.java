package org.example.gui;

import org.example.core.Planificador;
import org.example.core.Proceso;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Panel de gestión y simulación de procesos del sistema MiniSO
 *
 * Esta clase implementa la interfaz gráfica para la simulación de algoritmos
 * de planificación de procesos, incluyendo:
 * - FIFO (First In, First Out)
 * - Round Robin con quantum configurable
 * - Planificación por prioridades
 *
 * Proporciona visualización en tiempo real del estado de los procesos,
 * estadísticas de ejecución y control de velocidad de simulación.
 *
 * @author Equipo QueSO - Universidad Veracruzana
 * @version 1.0
 */
public class ProcesosPanel extends JPanel {
    /** ComboBox para seleccionar el algoritmo de planificación */
    private JComboBox<String> algoritmoCombo;

    /** Spinner para configurar el quantum en Round Robin */
    private JSpinner quantumSpinner;

    /** Tabla que muestra la lista de procesos y su estado */
    private JTable procesosTable;

    /** Modelo de datos para la tabla de procesos */
    private DefaultTableModel tableModel;

    /** Área de texto para mostrar el log detallado de ejecución */
    private JTextArea logArea;

    /** Botón para agregar nuevos procesos al sistema */
    private JButton agregarProcesoBtn;

    /** Botón para iniciar la simulación de planificación */
    private JButton ejecutarBtn;

    /** Botón para limpiar todos los procesos y reiniciar */
    private JButton limpiarBtn;

    /** Botón para pausar/reanudar la simulación en curso */
    private JButton pausarBtn;

    /** Label que muestra el tiempo actual del sistema */
    private JLabel tiempoActualLabel;

    /** Label que indica qué proceso está ejecutándose actualmente */
    private JLabel procesoEjecutandoLabel;

    /** Barra de progreso del proceso actual */
    private JProgressBar progresoProceso;

    /** Label con estadísticas generales de la simulación */
    private JLabel estadisticasLabel;

    /** Slider para controlar la velocidad de simulación */
    private JSlider velocidadSlider;

    /** Instancia del planificador que maneja la lógica de procesos */
    private Planificador planificador;

    /** Flag que indica si hay una simulación en ejecución */
    private boolean simulacionEnCurso = false;

    /** Flag que indica si la simulación está pausada */
    private boolean simulacionPausada = false;

    /** Contador automático para asignar PIDs únicos */
    private int contadorPID = 1;

    /** Worker para ejecutar la simulación en background sin bloquear la UI */
    private SwingWorker<Void, String> workerActual;

    /**
     * Constructor principal del panel de procesos
     *
     * Inicializa todos los componentes de la interfaz, configura el layout,
     * establece los manejadores de eventos y crea un planificador por defecto.
     */
    public ProcesosPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupTableRenderer();
        initializePlanificador(); // Inicializar con configuración por defecto
    }

    /**
     * Inicializa todos los componentes de la interfaz gráfica
     *
     * Configura los valores por defecto, dimensiones y propiedades
     * visuales de cada componente del panel.
     */
    private void initializeComponents() {
        // === CONFIGURACIÓN DE ALGORITMOS ===
        algoritmoCombo = new JComboBox<>(new String[]{"FIFO", "Round Robin", "Prioridad"});
        algoritmoCombo.setSelectedIndex(0); // FIFO por defecto

        // === CONFIGURACIÓN DE QUANTUM ===
        quantumSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        quantumSpinner.setEnabled(false); // Solo habilitado para Round Robin

        // === CONFIGURACIÓN DE TABLA DE PROCESOS ===
        String[] columnNames = {"PID", "Nombre", "T.Llegada", "T.Ejecución", "Prioridad", "Estado", "T.Restante", "Progreso"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla de solo lectura para evitar modificaciones accidentales
            }
        };
        procesosTable = new JTable(tableModel);
        procesosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        procesosTable.setRowHeight(25); // Altura aumentada para mejor legibilidad

        // === CONFIGURACIÓN DEL ÁREA DE LOG ===
        logArea = new JTextArea(8, 50);
        logArea.setEditable(false); // Solo lectura
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12)); // Fuente monoespaciada
        logArea.setBackground(new Color(248, 248, 248)); // Fondo gris claro

        // === CONFIGURACIÓN DE BOTONES ===
        agregarProcesoBtn = new JButton("➕ Agregar Proceso");
        ejecutarBtn = new JButton("▶️ Ejecutar Simulación");
        pausarBtn = new JButton("⏸️ Pausar");
        limpiarBtn = new JButton("🗑️ Limpiar Todo");

        // === COMPONENTES DE VISUALIZACIÓN DE ESTADO ===
        tiempoActualLabel = new JLabel("Tiempo del Sistema: 0");
        tiempoActualLabel.setFont(new Font("Arial", Font.BOLD, 14));

        procesoEjecutandoLabel = new JLabel("Proceso Ejecutando: Ninguno");
        procesoEjecutandoLabel.setFont(new Font("Arial", Font.BOLD, 12));

        progresoProceso = new JProgressBar(0, 100);
        progresoProceso.setStringPainted(true); // Mostrar porcentaje como texto
        progresoProceso.setString("0%");

        estadisticasLabel = new JLabel("Estadísticas: No iniciado");
        estadisticasLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        // === CONTROL DE VELOCIDAD DE SIMULACIÓN ===
        velocidadSlider = new JSlider(100, 2000, 1000); // Rango de 100ms a 2000ms
        velocidadSlider.setBorder(new TitledBorder("Velocidad (ms)"));

        // === ESTADOS INICIALES DE BOTONES ===
        pausarBtn.setEnabled(false); // Inicialmente deshabilitado
    }

    /**
     * Configura el layout principal del panel
     *
     * Organiza los componentes en una estructura jerárquica usando
     * BorderLayout y JSplitPane para una distribución óptima del espacio.
     */
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10)); // Margen externo

        // === PANEL SUPERIOR: CONFIGURACIÓN Y ESTADO ===
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(createConfigPanel(), BorderLayout.NORTH);
        topPanel.add(createEstadoPanel(), BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // === PANEL CENTRAL: DIVISIÓN HORIZONTAL ===
        JSplitPane centralSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centralSplit.setLeftComponent(createTablaPanel());
        centralSplit.setRightComponent(createLogPanel());
        centralSplit.setDividerLocation(600); // Posición inicial del divisor
        add(centralSplit, BorderLayout.CENTER);

        // === PANEL INFERIOR: BOTONES Y CONTROLES ===
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(createButtonPanel(), BorderLayout.CENTER);
        bottomPanel.add(createControlPanel(), BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Crea el panel de configuración del planificador
     *
     * @return JPanel con controles para algoritmo y quantum
     */
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("Configuración del Planificador"));

        // Selector de algoritmo
        panel.add(new JLabel("Algoritmo:"));
        panel.add(algoritmoCombo);

        panel.add(Box.createHorizontalStrut(20)); // Espaciador

        // Configuración de quantum
        panel.add(new JLabel("Quantum:"));
        panel.add(quantumSpinner);

        return panel;
    }

    /**
     * Crea el panel de estado de ejecución
     *
     * @return JPanel con información del estado actual de la simulación
     */
    private JPanel createEstadoPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 5));
        panel.setBorder(new TitledBorder("Estado de Ejecución"));

        panel.add(tiempoActualLabel);
        panel.add(procesoEjecutandoLabel);
        panel.add(progresoProceso);
        panel.add(estadisticasLabel);

        return panel;
    }

    /**
     * Crea el panel que contiene la tabla de procesos
     *
     * @return JPanel con la tabla de procesos en un ScrollPane
     */
    private JPanel createTablaPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Lista de Procesos"));

        JScrollPane scrollPane = new JScrollPane(procesosTable);
        scrollPane.setPreferredSize(new Dimension(600, 300));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Crea el panel del log de ejecución
     *
     * @return JPanel con el área de texto para logs
     */
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Log de Ejecución Detallado"));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Crea el panel de botones principales
     *
     * @return JPanel con los botones de control de la simulación
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        panel.add(agregarProcesoBtn);
        panel.add(ejecutarBtn);
        panel.add(pausarBtn);
        panel.add(limpiarBtn);

        return panel;
    }

    /**
     * Crea el panel de control de velocidad
     *
     * @return JPanel con el slider de velocidad
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(velocidadSlider);
        return panel;
    }

    /**
     * Configura el renderer personalizado para la tabla de procesos
     *
     * Aplica colores diferentes a las filas según el estado del proceso:
     * - Verde: Ejecutando
     * - Amarillo: Listo
     * - Gris: Terminado
     * - Azul: Nuevo
     */
    private void setupTableRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    // Obtener el estado del proceso de la columna correspondiente
                    Object estadoObj = table.getValueAt(row, 5); // Columna "Estado"
                    String estado = estadoObj != null ? estadoObj.toString() : "NUEVO";

                    // Aplicar color según el estado
                    switch (estado) {
                        case "EJECUTANDO":
                            c.setBackground(new Color(144, 238, 144)); // Verde claro
                            break;
                        case "LISTO":
                            c.setBackground(new Color(255, 255, 224)); // Amarillo claro
                            break;
                        case "TERMINADO":
                            c.setBackground(new Color(211, 211, 211)); // Gris claro
                            break;
                        case "NUEVO":
                            c.setBackground(new Color(173, 216, 230)); // Azul claro
                            break;
                        default:
                            c.setBackground(Color.WHITE); // Blanco por defecto
                    }
                }

                return c;
            }
        };

        // Aplicar el renderer a todas las columnas de la tabla
        for (int i = 0; i < procesosTable.getColumnCount(); i++) {
            procesosTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    /**
     * Configura todos los manejadores de eventos de la interfaz
     *
     * Establece los listeners para botones, cambios en combos/spinners
     * y otros eventos de interacción del usuario.
     */
    private void setupEventHandlers() {
        // === MANEJADOR DE CAMBIO DE ALGORITMO ===
        algoritmoCombo.addActionListener(e -> {
            String selected = (String) algoritmoCombo.getSelectedItem();
            // Habilitar quantum solo para Round Robin
            quantumSpinner.setEnabled("Round Robin".equals(selected));
            initializePlanificador(); // Reinicializar con nuevo algoritmo
        });

        // === MANEJADOR DE CAMBIO DE QUANTUM ===
        quantumSpinner.addChangeListener(e -> {
            if ("Round Robin".equals(algoritmoCombo.getSelectedItem())) {
                initializePlanificador(); // Actualizar planificador con nuevo quantum
            }
        });

        // === MANEJADORES DE BOTONES ===
        agregarProcesoBtn.addActionListener(e -> mostrarDialogoAgregarProceso());
        ejecutarBtn.addActionListener(e -> ejecutarSimulacion());
        pausarBtn.addActionListener(e -> pausarReanudarSimulacion());
        limpiarBtn.addActionListener(e -> limpiarTodo());
    }

    /**
     * Inicializa el planificador con la configuración actual
     *
     * Crea una nueva instancia del planificador basada en el algoritmo
     * seleccionado y configura los parámetros correspondientes.
     */
    private void initializePlanificador() {
        String algoritmo = (String) algoritmoCombo.getSelectedItem();
        int quantum = (Integer) quantumSpinner.getValue();

        // Crear planificador según el algoritmo seleccionado
        switch (algoritmo) {
            case "Round Robin":
                planificador = new Planificador("RR", quantum);
                break;
            case "Prioridad":
                planificador = new Planificador("PRIORIDAD");
                break;
            default: // FIFO
                planificador = new Planificador("FIFO");
                break;
        }

        // Registrar inicialización en el log
        logArea.append("🔄 Planificador local inicializado: " + algoritmo +
                (algoritmo.equals("Round Robin") ? " (Quantum: " + quantum + ")" : "") + "\n");

        // Resetear visualización a estado inicial
        tiempoActualLabel.setText("Tiempo del Sistema: 0");
        procesoEjecutandoLabel.setText("Proceso Ejecutando: Ninguno");
        progresoProceso.setValue(0);
        progresoProceso.setString("0%");
        estadisticasLabel.setText("Estadísticas: Planificador listo");
    }

    /**
     * Muestra el diálogo para agregar un nuevo proceso
     *
     * Presenta un formulario modal con campos para:
     * - Nombre del proceso
     * - Tiempo de llegada
     * - Tiempo de ejecución
     * - Prioridad
     */
    private void mostrarDialogoAgregarProceso() {
        // Crear diálogo modal
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Agregar Proceso", true);
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);

        // Panel principal con GridBagLayout para mejor control
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // === CAMPOS DEL FORMULARIO ===
        JTextField nombreField = new JTextField("Proceso" + contadorPID, 15);
        JSpinner llegadaSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        JSpinner ejecucionSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
        JSpinner prioridadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));

        // === LAYOUT DEL FORMULARIO ===
        // Nombre
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        panel.add(nombreField, gbc);

        // Tiempo de llegada
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Tiempo Llegada:"), gbc);
        gbc.gridx = 1;
        panel.add(llegadaSpinner, gbc);

        // Tiempo de ejecución
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Tiempo Ejecución:"), gbc);
        gbc.gridx = 1;
        panel.add(ejecucionSpinner, gbc);

        // Prioridad
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Prioridad (1=alta):"), gbc);
        gbc.gridx = 1;
        panel.add(prioridadSpinner, gbc);

        // === PANEL DE BOTONES ===
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("Agregar");
        JButton cancelButton = new JButton("Cancelar");

        // Manejador del botón OK
        okButton.addActionListener(e -> {
            String nombre = nombreField.getText().trim();
            if (nombre.isEmpty()) nombre = "Proceso" + contadorPID;

            int llegada = (Integer) llegadaSpinner.getValue();
            int ejecucion = (Integer) ejecucionSpinner.getValue();
            int prioridad = (Integer) prioridadSpinner.getValue();

            agregarProceso(nombre, llegada, ejecucion, prioridad);
            contadorPID++;
            dialog.dispose();
        });

        // Manejador del botón Cancelar
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        // Agregar panel de botones al formulario
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    /**
     * Agrega un nuevo proceso al sistema
     *
     * @param nombre Nombre descriptivo del proceso
     * @param llegada Tiempo de llegada al sistema
     * @param ejecucion Tiempo total de ejecución requerido
     * @param prioridad Nivel de prioridad (1=alta, 5=baja)
     */
    private void agregarProceso(String nombre, int llegada, int ejecucion, int prioridad) {
        int pid = contadorPID;

        // Crear nuevo proceso con los parámetros especificados
        Proceso proceso = new Proceso(pid, nombre, llegada, ejecucion, prioridad);

        // Agregar al planificador
        planificador.agregarProceso(proceso);

        // Agregar fila a la tabla de visualización
        Object[] rowData = {
                pid,
                proceso.getNombre(),
                llegada,
                proceso.getTiempoEjecucion(),
                proceso.getPrioridad(),
                proceso.getEstado().toString(), // Convertir enum a string
                proceso.getTiempoRestante(),
                "0%" // Progreso inicial
        };
        tableModel.addRow(rowData);

        // Registrar en el log
        logArea.append("✅ Proceso creado: " + nombre +
                " (PID: " + pid + ", Duración: " + ejecucion + ", Prioridad: " + prioridad + ")\n");
        logArea.setCaretPosition(logArea.getDocument().getLength()); // Scroll automático

        // Actualizar estadísticas
        estadisticasLabel.setText("Procesos creados: " + tableModel.getRowCount());
    }

    /**
     * Inicia la simulación de planificación de procesos
     *
     * Ejecuta la simulación en un hilo separado usando SwingWorker
     * para mantener la interfaz gráfica responsiva durante la ejecución.
     */
    private void ejecutarSimulacion() {
        // Validar que hay procesos para simular
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Agregue al menos un proceso antes de ejecutar.",
                    "Sin Procesos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Configurar estado de la interfaz para simulación
        ejecutarBtn.setEnabled(false);
        pausarBtn.setEnabled(true);
        agregarProcesoBtn.setEnabled(false);
        simulacionEnCurso = true;
        simulacionPausada = false;

        // Registrar inicio de simulación
        logArea.append("\n🚀 INICIANDO SIMULACIÓN 🚀\n");
        logArea.append("Algoritmo: " + algoritmoCombo.getSelectedItem() + "\n");
        logArea.append("═══════════════════════════════════════\n");

        // Crear SwingWorker para ejecución en background
        workerActual = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    int ciclo = 0;

                    // Bucle principal de simulación
                    while (planificador.ejecutarCiclo() && !isCancelled()) {
                        ciclo++;

                        // Publicar información del ciclo actual
                        String info = String.format("🔄 CICLO %d - Tiempo: %d",
                                ciclo, planificador.getTiempoActual());
                        publish(info);

                        // Manejar pausa en la simulación
                        while (simulacionPausada && !isCancelled()) {
                            Thread.sleep(100); // Espera breve durante pausa
                        }

                        if (isCancelled()) break;

                        // Actualizar visualización en el hilo de la UI
                        SwingUtilities.invokeLater(() -> {
                            actualizarVisualizacionCompleta();
                        });

                        // Verificar condición de terminación
                        if (planificador.getProcesosTerminados().size() >= tableModel.getRowCount()) {
                            publish("✅ Todos los procesos han terminado");
                            break;
                        }

                        // Controlar velocidad de simulación
                        Thread.sleep(velocidadSlider.getValue());
                    }
                } catch (Exception e) {
                    publish("❌ Error en simulación: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                // Procesar mensajes del background thread en el EDT
                for (String message : chunks) {
                    logArea.append(message + "\n");
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                }
            }

            @Override
            protected void done() {
                // Finalización de la simulación
                if (!isCancelled()) {
                    logArea.append("\n🏁 ¡SIMULACIÓN COMPLETADA! 🏁\n");
                    mostrarEstadisticasFinales();

                    // Actualización final de la interfaz
                    SwingUtilities.invokeLater(() -> {
                        // Asegurar que todos los procesos se marquen como terminados
                        for (int i = 0; i < tableModel.getRowCount(); i++) {
                            if (!"TERMINADO".equals(tableModel.getValueAt(i, 5))) {
                                tableModel.setValueAt("TERMINADO", i, 5);
                                tableModel.setValueAt(0, i, 6);
                                tableModel.setValueAt("100%", i, 7);
                            }
                        }
                        procesoEjecutandoLabel.setText("Proceso Ejecutando: Todos Completados");
                        progresoProceso.setValue(100);
                        progresoProceso.setString("100%");
                        procesosTable.repaint();
                    });
                }

                // Restaurar estado de botones
                ejecutarBtn.setEnabled(true);
                pausarBtn.setEnabled(false);
                pausarBtn.setText("⏸️ Pausar");
                agregarProcesoBtn.setEnabled(true);
                simulacionEnCurso = false;
                simulacionPausada = false;
            }
        };

        workerActual.execute();
    }

    /**
     * Pausa o reanuda la simulación en curso
     *
     * Alterna entre los estados pausado/ejecutando sin detener
     * completamente la simulación.
     */
    private void pausarReanudarSimulacion() {
        if (simulacionEnCurso) {
            if (simulacionPausada) {
                // Reanudar simulación
                simulacionPausada = false;
                pausarBtn.setText("⏸️ Pausar");
                logArea.append("▶️ Simulación reanudada\n");
            } else {
                // Pausar simulación
                simulacionPausada = true;
                pausarBtn.setText("▶️ Reanudar");
                logArea.append("⏸️ Simulación pausada\n");
            }
        }
    }

    /**
     * Actualiza toda la visualización de la simulación
     *
     * Sincroniza la tabla de procesos, las etiquetas de estado
     * y la barra de progreso con el estado actual del planificador.
     */
    private void actualizarVisualizacionCompleta() {
        // Actualizar tiempo del sistema
        int tiempoSistema = planificador.getTiempoActual();
        tiempoActualLabel.setText("Tiempo del Sistema: " + tiempoSistema);

        // Obtener lista de procesos terminados
        List<Proceso> terminados = planificador.getProcesosTerminados();

        // Variables para tracking del proceso actual
        String procesoEjecutando = "Ninguno";
        int progreso = 0;
        int tiempoAcumulado = 0;
        boolean hayProcesoEjecutando = false;

        // Actualizar estado de cada proceso en la tabla
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int pid = (Integer) tableModel.getValueAt(i, 0);
            String nombreProceso = (String) tableModel.getValueAt(i, 1);
            int tiempoTotal = (Integer) tableModel.getValueAt(i, 3);

            // Verificar si el proceso ya terminó
            boolean estaTerminado = false;
            for (Proceso p : terminados) {
                if (p.getPid() == pid) {
                    tableModel.setValueAt("TERMINADO", i, 5);
                    tableModel.setValueAt(0, i, 6);
                    tableModel.setValueAt("100%", i, 7);
                    estaTerminado = true;
                    break;
                }
            }

            if (!estaTerminado) {
                // Calcular ventana de ejecución del proceso (para FIFO)
                int tiempoInicio = tiempoAcumulado;
                int tiempoFin = tiempoAcumulado + tiempoTotal;

                if (tiempoSistema >= tiempoInicio && tiempoSistema < tiempoFin && !hayProcesoEjecutando) {
                    // Proceso actualmente ejecutando
                    tableModel.setValueAt("EJECUTANDO", i, 5);
                    int tiempoEjecutado = tiempoSistema - tiempoInicio;
                    int tiempoRestante = tiempoTotal - tiempoEjecutado;
                    tableModel.setValueAt(tiempoRestante, i, 6);

                    progreso = (tiempoEjecutado * 100) / tiempoTotal;
                    procesoEjecutando = nombreProceso;
                    hayProcesoEjecutando = true;

                    tableModel.setValueAt(progreso + "%", i, 7);
                } else if (tiempoSistema < tiempoInicio) {
                    // Proceso esperando su turno
                    tableModel.setValueAt("LISTO", i, 5);
                    tableModel.setValueAt(tiempoTotal, i, 6);
                    tableModel.setValueAt("0%", i, 7);
                } else if (tiempoSistema >= tiempoFin) {
                    // Proceso debería estar terminado
                    tableModel.setValueAt("TERMINADO", i, 5);
                    tableModel.setValueAt(0, i, 6);
                    tableModel.setValueAt("100%", i, 7);
                }
            }

            // Acumular tiempo para calcular inicio del siguiente proceso
            tiempoAcumulado += tiempoTotal;
        }

        // Manejar caso donde todos los procesos han terminado
        if (!hayProcesoEjecutando && terminados.size() == tableModel.getRowCount()) {
            procesoEjecutando = "Todos Completados";
            progreso = 100;
        }

        // Actualizar elementos visuales de estado
        procesoEjecutandoLabel.setText("Proceso Ejecutando: " + procesoEjecutando);
        progresoProceso.setValue(progreso);
        progresoProceso.setString(progreso + "%");

        // Actualizar estadísticas de progreso
        int completados = terminados.size();
        int total = tableModel.getRowCount();
        estadisticasLabel.setText(String.format("Completados: %d/%d procesos", completados, total));

        // Forzar repintado de la tabla para mostrar cambios de color
        procesosTable.repaint();
    }

    /**
     * Muestra las estadísticas finales de la simulación
     *
     * Calcula y presenta métricas importantes como:
     * - Número de procesos completados
     * - Tiempo total de simulación
     * - Tiempo promedio de espera
     * - Tiempo promedio de respuesta
     */
    private void mostrarEstadisticasFinales() {
        logArea.append("\n📊 ESTADÍSTICAS FINALES:\n");
        logArea.append("───────────────────────────────────────\n");

        List<Proceso> terminados = planificador.getProcesosTerminados();

        if (!terminados.isEmpty()) {
            // Calcular métricas promedio
            double promedioEspera = terminados.stream()
                    .mapToInt(Proceso::getTiempoEspera)
                    .average().orElse(0);

            double promedioRespuesta = terminados.stream()
                    .mapToInt(Proceso::getTiempoRespuesta)
                    .average().orElse(0);

            // Mostrar estadísticas detalladas
            logArea.append(String.format("• Procesos completados: %d\n", terminados.size()));
            logArea.append(String.format("• Tiempo total de simulación: %d\n", planificador.getTiempoActual()));
            logArea.append(String.format("• Tiempo promedio de espera: %.2f\n", promedioEspera));
            logArea.append(String.format("• Tiempo promedio de respuesta: %.2f\n", promedioRespuesta));
            logArea.append(String.format("• Algoritmo utilizado: %s\n", algoritmoCombo.getSelectedItem()));

            // Información específica para Round Robin
            if ("Round Robin".equals(algoritmoCombo.getSelectedItem())) {
                logArea.append(String.format("• Quantum utilizado: %d\n", quantumSpinner.getValue()));
            }
        }

        logArea.append("═══════════════════════════════════════\n");
        estadisticasLabel.setText("✅ Simulación completada - Ver estadísticas en log");
    }

    /**
     * Limpia todos los procesos y reinicia el simulador
     *
     * Cancela cualquier simulación en curso, limpia la tabla de procesos,
     * reinicia el planificador y restaura la interfaz al estado inicial.
     */
    private void limpiarTodo() {
        // Cancelar simulación en curso si existe
        if (workerActual != null && !workerActual.isDone()) {
            workerActual.cancel(true);
        }

        // Limpiar datos de la simulación
        tableModel.setRowCount(0); // Vaciar tabla
        logArea.setText(""); // Limpiar log
        contadorPID = 1; // Reiniciar contador de PIDs
        simulacionEnCurso = false;
        simulacionPausada = false;

        // Reinicializar planificador con configuración actual
        initializePlanificador();

        // Restaurar estado inicial de botones
        ejecutarBtn.setEnabled(true);
        pausarBtn.setEnabled(false);
        pausarBtn.setText("⏸️ Pausar");
        agregarProcesoBtn.setEnabled(true);

        // Notificar reinicio en el log
        logArea.append("🗑️ Simulación reiniciada\n");
        logArea.append("💡 Agregue procesos para comenzar una nueva simulación\n");
    }
}