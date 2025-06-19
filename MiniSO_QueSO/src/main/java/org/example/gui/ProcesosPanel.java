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

public class ProcesosPanel extends JPanel {

    // Componentes GUI
    private JComboBox<String> algoritmoCombo;
    private JSpinner quantumSpinner;
    private JTable procesosTable;
    private DefaultTableModel tableModel;
    private JTextArea logArea;
    private JButton agregarProcesoBtn;
    private JButton ejecutarBtn;
    private JButton limpiarBtn;
    private JButton pausarBtn;

    // Componentes de visualización mejorada
    private JLabel tiempoActualLabel;
    private JLabel procesoEjecutandoLabel;
    private JProgressBar progresoProceso;
    private JLabel estadisticasLabel;
    private JSlider velocidadSlider;

    // Lógica del sistema - SOLO LOCAL
    private Planificador planificador;
    private boolean simulacionEnCurso = false;
    private boolean simulacionPausada = false;
    private int contadorPID = 1;
    private SwingWorker<Void, String> workerActual;

    public ProcesosPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupTableRenderer();
        initializePlanificador(); // SIEMPRE LOCAL
    }

    private void initializeComponents() {
        // Combo de algoritmos
        algoritmoCombo = new JComboBox<>(new String[]{"FIFO", "Round Robin", "Prioridad"});
        algoritmoCombo.setSelectedIndex(0);

        // Spinner para quantum
        quantumSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        quantumSpinner.setEnabled(false); // Habilitado solo para RR

        // Tabla de procesos
        String[] columnNames = {"PID", "Nombre", "T.Llegada", "T.Ejecución", "Prioridad", "Estado", "T.Restante", "Progreso"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla de solo lectura
            }
        };
        procesosTable = new JTable(tableModel);
        procesosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        procesosTable.setRowHeight(25); // Más alto para mejor visualización

        // Área de log
        logArea = new JTextArea(8, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(248, 248, 248));

        // Botones
        agregarProcesoBtn = new JButton("➕ Agregar Proceso");
        ejecutarBtn = new JButton("▶️ Ejecutar Simulación");
        pausarBtn = new JButton("⏸️ Pausar");
        limpiarBtn = new JButton("🗑️ Limpiar Todo");

        // Componentes de visualización
        tiempoActualLabel = new JLabel("Tiempo del Sistema: 0");
        tiempoActualLabel.setFont(new Font("Arial", Font.BOLD, 14));

        procesoEjecutandoLabel = new JLabel("Proceso Ejecutando: Ninguno");
        procesoEjecutandoLabel.setFont(new Font("Arial", Font.BOLD, 12));

        progresoProceso = new JProgressBar(0, 100);
        progresoProceso.setStringPainted(true);
        progresoProceso.setString("0%");

        estadisticasLabel = new JLabel("Estadísticas: No iniciado");
        estadisticasLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        velocidadSlider = new JSlider(100, 2000, 1000);
        velocidadSlider.setBorder(new TitledBorder("Velocidad (ms)"));

        // Estados iniciales
        pausarBtn.setEnabled(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panel superior - Configuración y Estado
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(createConfigPanel(), BorderLayout.NORTH);
        topPanel.add(createEstadoPanel(), BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Panel central - División horizontal
        JSplitPane centralSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centralSplit.setLeftComponent(createTablaPanel());
        centralSplit.setRightComponent(createLogPanel());
        centralSplit.setDividerLocation(600);
        add(centralSplit, BorderLayout.CENTER);

        // Panel inferior - Botones y Control
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(createButtonPanel(), BorderLayout.CENTER);
        bottomPanel.add(createControlPanel(), BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("Configuración del Planificador"));

        panel.add(new JLabel("Algoritmo:"));
        panel.add(algoritmoCombo);

        panel.add(Box.createHorizontalStrut(20));

        panel.add(new JLabel("Quantum:"));
        panel.add(quantumSpinner);

        return panel;
    }

    private JPanel createEstadoPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 5));
        panel.setBorder(new TitledBorder("Estado de Ejecución"));

        panel.add(tiempoActualLabel);
        panel.add(procesoEjecutandoLabel);
        panel.add(progresoProceso);
        panel.add(estadisticasLabel);

        return panel;
    }

    private JPanel createTablaPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Lista de Procesos"));

        JScrollPane scrollPane = new JScrollPane(procesosTable);
        scrollPane.setPreferredSize(new Dimension(600, 300));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Log de Ejecución Detallado"));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        panel.add(agregarProcesoBtn);
        panel.add(ejecutarBtn);
        panel.add(pausarBtn);
        panel.add(limpiarBtn);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(velocidadSlider);
        return panel;
    }

    private void setupTableRenderer() {
        // Renderer personalizado para colorear las filas según el estado
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    // OBTENER ESTADO COMO STRING - ARREGLADO
                    Object estadoObj = table.getValueAt(row, 5); // Columna Estado
                    String estado = estadoObj != null ? estadoObj.toString() : "NUEVO";

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
                            c.setBackground(Color.WHITE);
                    }
                }

                return c;
            }
        };

        // Aplicar renderer a todas las columnas
        for (int i = 0; i < procesosTable.getColumnCount(); i++) {
            procesosTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private void setupEventHandlers() {
        // Cambio de algoritmo
        algoritmoCombo.addActionListener(e -> {
            String selected = (String) algoritmoCombo.getSelectedItem();
            quantumSpinner.setEnabled("Round Robin".equals(selected));
            initializePlanificador();
        });

        // Cambio de quantum
        quantumSpinner.addChangeListener(e -> {
            if ("Round Robin".equals(algoritmoCombo.getSelectedItem())) {
                initializePlanificador();
            }
        });

        // Botón agregar proceso
        agregarProcesoBtn.addActionListener(e -> mostrarDialogoAgregarProceso());

        // Botón ejecutar
        ejecutarBtn.addActionListener(e -> ejecutarSimulacion());

        // Botón pausar
        pausarBtn.addActionListener(e -> pausarReanudarSimulacion());

        // Botón limpiar
        limpiarBtn.addActionListener(e -> limpiarTodo());
    }

    private void initializePlanificador() {
        // SIMPLIFICADO: Solo planificador local
        String algoritmo = (String) algoritmoCombo.getSelectedItem();
        int quantum = (Integer) quantumSpinner.getValue();

        switch (algoritmo) {
            case "Round Robin":
                planificador = new Planificador("RR", quantum);
                break;
            case "Prioridad":
                planificador = new Planificador("PRIORIDAD");
                break;
            default:
                planificador = new Planificador("FIFO");
                break;
        }

        logArea.append("🔄 Planificador local inicializado: " + algoritmo +
                (algoritmo.equals("Round Robin") ? " (Quantum: " + quantum + ")" : "") + "\n");

        // Resetear visualización
        tiempoActualLabel.setText("Tiempo del Sistema: 0");
        procesoEjecutandoLabel.setText("Proceso Ejecutando: Ninguno");
        progresoProceso.setValue(0);
        progresoProceso.setString("0%");
        estadisticasLabel.setText("Estadísticas: Planificador listo");
    }

    private void mostrarDialogoAgregarProceso() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Agregar Proceso", true);
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Campos del formulario
        JTextField nombreField = new JTextField("Proceso" + contadorPID, 15);
        JSpinner llegadaSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        JSpinner ejecucionSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1)); // Aumenté default
        JSpinner prioridadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));

        // Layout del formulario
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        panel.add(nombreField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Tiempo Llegada:"), gbc);
        gbc.gridx = 1;
        panel.add(llegadaSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Tiempo Ejecución:"), gbc);
        gbc.gridx = 1;
        panel.add(ejecucionSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Prioridad (1=alta):"), gbc);
        gbc.gridx = 1;
        panel.add(prioridadSpinner, gbc);

        // Botones
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("Agregar");
        JButton cancelButton = new JButton("Cancelar");

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

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void agregarProceso(String nombre, int llegada, int ejecucion, int prioridad) {
        // SIMPLIFICADO: Solo crear proceso local
        int pid = contadorPID;
        Proceso proceso = new Proceso(pid, nombre, llegada, ejecucion, prioridad);

        // Agregar al planificador
        planificador.agregarProceso(proceso);

        // Agregar a la tabla con progreso - CONVERTIR ENUM A STRING
        Object[] rowData = {
                pid,
                proceso.getNombre(),
                llegada,
                proceso.getTiempoEjecucion(),
                proceso.getPrioridad(),
                proceso.getEstado().toString(), // CONVERTIR A STRING
                proceso.getTiempoRestante(),
                "0%"
        };
        tableModel.addRow(rowData);

        logArea.append("✅ Proceso creado: " + nombre +
                " (PID: " + pid + ", Duración: " + ejecucion + ", Prioridad: " + prioridad + ")\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());

        // Actualizar estadísticas
        estadisticasLabel.setText("Procesos creados: " + tableModel.getRowCount());
    }

    private void ejecutarSimulacion() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Agregue al menos un proceso antes de ejecutar.",
                    "Sin Procesos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ejecutarBtn.setEnabled(false);
        pausarBtn.setEnabled(true);
        agregarProcesoBtn.setEnabled(false);
        simulacionEnCurso = true;
        simulacionPausada = false;

        logArea.append("\n🚀 INICIANDO SIMULACIÓN 🚀\n");
        logArea.append("Algoritmo: " + algoritmoCombo.getSelectedItem() + "\n");
        logArea.append("═══════════════════════════════════════\n");

        // Ejecutar planificación local con visualización mejorada
        workerActual = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    int ciclo = 0;
                    // Ejecutar hasta que todos terminen
                    while (planificador.ejecutarCiclo() && !isCancelled()) {
                        ciclo++;

                        // Información detallada del ciclo
                        String info = String.format("🔄 CICLO %d - Tiempo: %d",
                                ciclo, planificador.getTiempoActual());
                        publish(info);

                        // Pausar si está pausado
                        while (simulacionPausada && !isCancelled()) {
                            Thread.sleep(100);
                        }

                        if (isCancelled()) break;

                        // Actualizar visualización en EDT
                        SwingUtilities.invokeLater(() -> {
                            actualizarVisualizacionCompleta();
                        });

                        // Verificar si todos los procesos han terminado
                        if (planificador.getProcesosTerminados().size() >= tableModel.getRowCount()) {
                            publish("✅ Todos los procesos han terminado");
                            break;
                        }

                        // Velocidad controlable
                        Thread.sleep(velocidadSlider.getValue());
                    }
                } catch (Exception e) {
                    publish("❌ Error en simulación: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    logArea.append(message + "\n");
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                }
            }

            @Override
            protected void done() {
                if (!isCancelled()) {
                    logArea.append("\n🏁 ¡SIMULACIÓN COMPLETADA! 🏁\n");
                    mostrarEstadisticasFinales();

                    // Actualización final para asegurar que todo esté correcto
                    SwingUtilities.invokeLater(() -> {
                        // Marcar todos como terminados si es necesario
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

    private void pausarReanudarSimulacion() {
        if (simulacionEnCurso) {
            if (simulacionPausada) {
                simulacionPausada = false;
                pausarBtn.setText("⏸️ Pausar");
                logArea.append("▶️ Simulación reanudada\n");
            } else {
                simulacionPausada = true;
                pausarBtn.setText("▶️ Reanudar");
                logArea.append("⏸️ Simulación pausada\n");
            }
        }
    }

    private void actualizarVisualizacionCompleta() {
        // Actualizar tiempo del sistema
        int tiempoSistema = planificador.getTiempoActual();
        tiempoActualLabel.setText("Tiempo del Sistema: " + tiempoSistema);

        // Obtener procesos terminados
        List<Proceso> terminados = planificador.getProcesosTerminados();

        // Variables para la visualización
        String procesoEjecutando = "Ninguno";
        int progreso = 0;

        // Calcular tiempos acumulados para FIFO
        int tiempoAcumulado = 0;
        boolean hayProcesoEjecutando = false;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int pid = (Integer) tableModel.getValueAt(i, 0);
            String nombreProceso = (String) tableModel.getValueAt(i, 1);
            int tiempoTotal = (Integer) tableModel.getValueAt(i, 3);

            // Verificar si está terminado
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
                // Calcular cuándo debe empezar y terminar este proceso
                int tiempoInicio = tiempoAcumulado;
                int tiempoFin = tiempoAcumulado + tiempoTotal;

                if (tiempoSistema >= tiempoInicio && tiempoSistema < tiempoFin && !hayProcesoEjecutando) {
                    // Este proceso está ejecutando
                    tableModel.setValueAt("EJECUTANDO", i, 5);
                    int tiempoEjecutado = tiempoSistema - tiempoInicio;
                    int tiempoRestante = tiempoTotal - tiempoEjecutado;
                    tableModel.setValueAt(tiempoRestante, i, 6);

                    progreso = (tiempoEjecutado * 100) / tiempoTotal;
                    procesoEjecutando = nombreProceso;
                    hayProcesoEjecutando = true;

                    tableModel.setValueAt(progreso + "%", i, 7);
                } else if (tiempoSistema < tiempoInicio) {
                    // Proceso esperando
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

            // Acumular tiempo para el siguiente proceso
            tiempoAcumulado += tiempoTotal;
        }

        // Si todos los procesos han terminado pero la simulación sigue
        if (!hayProcesoEjecutando && terminados.size() == tableModel.getRowCount()) {
            procesoEjecutando = "Todos Completados";
            progreso = 100;
        }

        // Actualizar labels de visualización
        procesoEjecutandoLabel.setText("Proceso Ejecutando: " + procesoEjecutando);
        progresoProceso.setValue(progreso);
        progresoProceso.setString(progreso + "%");

        // Actualizar estadísticas
        int completados = terminados.size();
        int total = tableModel.getRowCount();
        estadisticasLabel.setText(String.format("Completados: %d/%d procesos", completados, total));

        // Forzar repintado de la tabla
        procesosTable.repaint();
    }

    private void mostrarEstadisticasFinales() {
        logArea.append("\n📊 ESTADÍSTICAS FINALES:\n");
        logArea.append("───────────────────────────────────────\n");

        List<Proceso> terminados = planificador.getProcesosTerminados();

        if (!terminados.isEmpty()) {
            double promedioEspera = terminados.stream()
                    .mapToInt(Proceso::getTiempoEspera)
                    .average().orElse(0);

            double promedioRespuesta = terminados.stream()
                    .mapToInt(Proceso::getTiempoRespuesta)
                    .average().orElse(0);

            logArea.append(String.format("• Procesos completados: %d\n", terminados.size()));
            logArea.append(String.format("• Tiempo total de simulación: %d\n", planificador.getTiempoActual()));
            logArea.append(String.format("• Tiempo promedio de espera: %.2f\n", promedioEspera));
            logArea.append(String.format("• Tiempo promedio de respuesta: %.2f\n", promedioRespuesta));
            logArea.append(String.format("• Algoritmo utilizado: %s\n", algoritmoCombo.getSelectedItem()));

            if ("Round Robin".equals(algoritmoCombo.getSelectedItem())) {
                logArea.append(String.format("• Quantum utilizado: %d\n", quantumSpinner.getValue()));
            }
        }

        logArea.append("═══════════════════════════════════════\n");

        estadisticasLabel.setText("✅ Simulación completada - Ver estadísticas en log");
    }

    private void limpiarTodo() {
        // Cancelar simulación si está en curso
        if (workerActual != null && !workerActual.isDone()) {
            workerActual.cancel(true);
        }

        tableModel.setRowCount(0);
        logArea.setText("");
        contadorPID = 1;
        simulacionEnCurso = false;
        simulacionPausada = false;

        initializePlanificador();

        // Resetear controles
        ejecutarBtn.setEnabled(true);
        pausarBtn.setEnabled(false);
        pausarBtn.setText("⏸️ Pausar");
        agregarProcesoBtn.setEnabled(true);

        logArea.append("🗑️ Simulación reiniciada\n");
        logArea.append("💡 Agregue procesos para comenzar una nueva simulación\n");
    }
}