package org.example.gui;

import org.example.core.MemoriaManager;
import org.example.models.BloqueMemoria;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

/**
 * Panel de interfaz gráfica para la gestión y visualización de memoria en MiniSO.
 *
 * Esta clase implementa una interfaz visual completa para simular y demostrar
 * algoritmos de gestión de memoria utilizados en sistemas operativos reales.
 *
 * Funcionalidades principales:
 * - Visualización gráfica de bloques de memoria en tiempo real
 * - Simulación de algoritmos de asignación (First Fit, Best Fit, Worst Fit, Paginación)
 * - Asignación y liberación interactiva de memoria a procesos
 * - Estadísticas detalladas de uso y fragmentación
 * - Configuración dinámica del tamaño total de memoria
 *
 * Componentes de la interfaz:
 * - Panel de configuración (algoritmo y tamaño de memoria)
 * - Visualización gráfica de bloques de memoria
 * - Panel de control para gestionar procesos
 * - Área de estadísticas y logs detallados
 * - Leyenda visual para interpretar colores
 *
 * La visualización utiliza colores distintivos:
 * - Verde: Memoria libre/disponible
 * - Rojo: Memoria ocupada por procesos
 * - Azul: Memoria reservada del sistema
 * - Amarillo: Fragmentación externa
 *
 * @author Equipo QueSO - Universidad Veracruzana
 * @version 1.0
 */
public class MemoriaPanel extends JPanel {
    /** ComboBox para seleccionar el algoritmo de asignación de memoria */
    private JComboBox<String> algoritmoCombo;

    /** Spinner para configurar el tamaño total de memoria del sistema */
    private JSpinner memoriaTotalSpinner;

    /** Panel personalizado que renderiza la visualización gráfica de memoria */
    private JPanel memoriaVisualPanel;

    /** Área de texto para mostrar estadísticas detalladas y logs */
    private JTextArea estadisticasArea;

    /** Campo de texto para ingresar ID del proceso */
    private JTextField procesoIdField;

    /** Campo de texto para ingresar nombre del proceso */
    private JTextField procesoNombreField;

    /** Spinner para especificar cantidad de memoria a asignar */
    private JSpinner procesoMemoriaSpinner;

    /** Botón para asignar memoria a un proceso */
    private JButton asignarBtn;

    /** Botón para liberar memoria de un proceso */
    private JButton liberarBtn;

    /** Botón para reiniciar completamente el sistema de memoria */
    private JButton reiniciarBtn;

    /** Etiqueta que muestra resumen de estadísticas de memoria */
    private JLabel estadisticasLabel;

    /** Instancia del gestor de memoria que maneja los algoritmos */
    private MemoriaManager memoriaManager;

    /** Tamaño por defecto de memoria del sistema en KB */
    private static final int MEMORIA_DEFAULT = 512;

    /** Color para bloques de memoria libre (verde claro) */
    private static final Color COLOR_LIBRE = new Color(144, 238, 144);

    /** Color para bloques de memoria ocupada (rojo claro) */
    private static final Color COLOR_OCUPADO = new Color(255, 99, 132);

    /** Color para bloques de memoria del sistema (azul) */
    private static final Color COLOR_SISTEMA = new Color(54, 162, 235);

    /** Color para representar fragmentación (amarillo) */
    private static final Color COLOR_FRAGMENTADO = new Color(255, 206, 86);

    /**
     * Constructor principal del panel de memoria.
     *
     * Inicializa todos los componentes de la interfaz, configura el layout,
     * establece los manejadores de eventos e inicializa el gestor de memoria.
     */
    public MemoriaPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        initializeMemoriaManager(); // Inicializar gestor local independiente
        actualizarVisualizacion();
    }

    /**
     * Inicializa todos los componentes de la interfaz gráfica.
     *
     * Configura las propiedades básicas de cada componente, incluyendo
     * valores por defecto, tamaños, fuentes y comportamientos.
     */
    private void initializeComponents() {
        // Configuración del selector de algoritmos
        algoritmoCombo = new JComboBox<>(new String[]{
                "First Fit", "Best Fit", "Worst Fit", "Paginación"
        });

        // Configuración del spinner para memoria total
        memoriaTotalSpinner = new JSpinner(new SpinnerNumberModel(
                MEMORIA_DEFAULT, // valor inicial
                128,             // mínimo (128 KB)
                2048,            // máximo (2 MB)
                64               // incremento (64 KB)
        ));

        // Creación del panel de visualización con renderizado personalizado
        memoriaVisualPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                dibujarMemoria(g); // Método personalizado de renderizado
            }
        };
        memoriaVisualPanel.setPreferredSize(new Dimension(600, 300));
        memoriaVisualPanel.setBackground(Color.WHITE);
        memoriaVisualPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Configuración del área de estadísticas detalladas
        estadisticasArea = new JTextArea(6, 30);
        estadisticasArea.setEditable(false); // Solo lectura
        estadisticasArea.setFont(new Font("Consolas", Font.PLAIN, 12)); // Fuente monoespaciada
        estadisticasArea.setBackground(new Color(248, 248, 248)); // Fondo gris claro

        // Campos de entrada para información de procesos
        procesoIdField = new JTextField("1", 5);
        procesoNombreField = new JTextField("Proceso1", 10);
        procesoMemoriaSpinner = new JSpinner(new SpinnerNumberModel(
                32,  // valor inicial (32 KB)
                4,   // mínimo (4 KB)
                256, // máximo (256 KB)
                4    // incremento (4 KB)
        ));

        // Inicialización de botones con iconos emoji
        asignarBtn = new JButton("📥 Asignar Memoria");
        liberarBtn = new JButton("📤 Liberar Memoria");
        reiniciarBtn = new JButton("🔄 Reiniciar");

        // Configuración de la etiqueta de estadísticas
        estadisticasLabel = new JLabel();
        estadisticasLabel.setFont(new Font("Arial", Font.BOLD, 12));
    }

    /**
     * Configura el layout principal del panel de memoria.
     *
     * Organiza los componentes en una estructura jerárquica que facilita
     * la visualización y control del sistema de memoria.
     */
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10)); // Margen externo

        // Panel superior - Configuración del sistema
        JPanel configPanel = createConfigPanel();
        add(configPanel, BorderLayout.NORTH);

        // Panel central - División principal entre visualización y control
        JSplitPane centralSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centralSplit.setLeftComponent(createVisualizacionPanel());
        centralSplit.setRightComponent(createControlPanel());
        centralSplit.setDividerLocation(650); // Posición inicial del divisor
        add(centralSplit, BorderLayout.CENTER);

        // Panel inferior - Estadísticas resumidas
        JPanel estadisticasPanel = createEstadisticasPanel();
        add(estadisticasPanel, BorderLayout.SOUTH);
    }

    /**
     * Crea el panel de configuración con algoritmo y tamaño de memoria.
     *
     * @return Panel configurado con controles de configuración
     */
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("Configuración de Memoria"));

        // Selector de algoritmo
        panel.add(new JLabel("Algoritmo:"));
        panel.add(algoritmoCombo);

        panel.add(Box.createHorizontalStrut(20)); // Espaciador horizontal

        // Configuración de memoria total
        panel.add(new JLabel("Memoria Total (KB):"));
        panel.add(memoriaTotalSpinner);

        return panel;
    }

    /**
     * Crea el panel de visualización gráfica de memoria.
     *
     * @return Panel configurado con visualización y leyenda
     */
    private JPanel createVisualizacionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Visualización de Memoria"));

        // Panel de visualización con scroll para contenido extenso
        JScrollPane scrollPane = new JScrollPane(memoriaVisualPanel);
        scrollPane.setPreferredSize(new Dimension(600, 300));

        panel.add(scrollPane, BorderLayout.CENTER);

        // Leyenda explicativa de colores
        JPanel leyendaPanel = createLeyendaPanel();
        panel.add(leyendaPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Crea el panel de leyenda con códigos de colores.
     *
     * @return Panel con leyenda visual de estados de memoria
     */
    private JPanel createLeyendaPanel() {
        JPanel leyenda = new JPanel(new FlowLayout());
        leyenda.setBorder(new TitledBorder("Leyenda"));

        // Crear etiquetas coloreadas para cada estado
        leyenda.add(createColorLabel("Libre", COLOR_LIBRE));
        leyenda.add(createColorLabel("Ocupado", COLOR_OCUPADO));
        leyenda.add(createColorLabel("Sistema", COLOR_SISTEMA));
        leyenda.add(createColorLabel("Fragmentado", COLOR_FRAGMENTADO));

        return leyenda;
    }

    /**
     * Crea una etiqueta coloreada para la leyenda.
     *
     * @param texto Texto descriptivo del estado
     * @param color Color representativo del estado
     * @return Etiqueta configurada con color y texto
     */
    private JLabel createColorLabel(String texto, Color color) {
        JLabel label = new JLabel("  " + texto);
        label.setOpaque(true); // Permitir color de fondo
        label.setBackground(color);
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        label.setPreferredSize(new Dimension(80, 20));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    /**
     * Crea el panel de control para gestión de procesos.
     *
     * @return Panel configurado con formulario y botones de control
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Control de Procesos"));

        // Formulario de entrada de datos para procesos
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Espaciado entre componentes

        // Campo ID del proceso
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("ID Proceso:"), gbc);
        gbc.gridx = 1;
        formPanel.add(procesoIdField, gbc);

        // Campo nombre del proceso
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        formPanel.add(procesoNombreField, gbc);

        // Campo cantidad de memoria
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Memoria (KB):"), gbc);
        gbc.gridx = 1;
        formPanel.add(procesoMemoriaSpinner, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Panel de botones de acción
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        buttonPanel.add(asignarBtn);
        buttonPanel.add(liberarBtn);
        buttonPanel.add(reiniciarBtn);

        panel.add(buttonPanel, BorderLayout.CENTER);

        // Área de estadísticas detalladas con scroll
        JScrollPane statsScroll = new JScrollPane(estadisticasArea);
        panel.add(statsScroll, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Crea el panel de estadísticas resumidas.
     *
     * @return Panel configurado para mostrar estadísticas
     */
    private JPanel createEstadisticasPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Estadísticas de Memoria"));

        panel.add(estadisticasLabel, BorderLayout.CENTER);

        return panel;
    }

    // ======================== CONFIGURACIÓN DE EVENTOS ========================

    /**
     * Configura todos los manejadores de eventos de la interfaz.
     *
     * Establece los listeners para cambios en controles y acciones de botones.
     */
    private void setupEventHandlers() {
        // Listener para cambio de algoritmo de asignación
        algoritmoCombo.addActionListener(e -> {
            updateAlgoritmo(); // Actualizar algoritmo en el gestor
            actualizarVisualizacion(); // Refrescar visualización
        });

        // Listener para cambio en el tamaño total de memoria
        memoriaTotalSpinner.addChangeListener(e -> {
            reiniciarMemoria(); // Reiniciar con nuevo tamaño
        });

        // Asignación de acciones a botones
        asignarBtn.addActionListener(e -> asignarMemoria());
        liberarBtn.addActionListener(e -> liberarMemoria());
        reiniciarBtn.addActionListener(e -> reiniciarMemoria());
    }

    /**
     * Inicializa el gestor de memoria local.
     *
     * Crea una instancia independiente del gestor de memoria para simular
     * el comportamiento de gestión de memoria en un sistema operativo.
     */
    private void initializeMemoriaManager() {
        // Crear gestor de memoria local para simulación
        memoriaManager = new MemoriaManager(MEMORIA_DEFAULT);
        appendToLog("✅ Gestor de memoria local inicializado\n");
        appendToLog("ℹ️ Simulador independiente - 512 KB de memoria\n");
    }

    /**
     * Agrega texto al área de logs/estadísticas.
     *
     * @param texto Mensaje a agregar al log
     */
    private void appendToLog(String texto) {
        String contenidoActual = estadisticasArea.getText();
        estadisticasArea.setText(contenidoActual + texto);
        // Mover cursor al final para mostrar último mensaje
        estadisticasArea.setCaretPosition(estadisticasArea.getDocument().getLength());
    }

    /**
     * Actualiza el algoritmo de asignación en el gestor de memoria.
     *
     * Convierte la selección del ComboBox al enum correspondiente
     * y configura el gestor de memoria.
     */
    private void updateAlgoritmo() {
        String selected = (String) algoritmoCombo.getSelectedItem();
        MemoriaManager.AlgoritmoAsignacion algoritmo;

        // Mapear selección de interfaz a enum del sistema
        switch (selected) {
            case "First Fit":
                algoritmo = MemoriaManager.AlgoritmoAsignacion.FIRST_FIT;
                break;
            case "Best Fit":
                algoritmo = MemoriaManager.AlgoritmoAsignacion.BEST_FIT;
                break;
            case "Worst Fit":
                algoritmo = MemoriaManager.AlgoritmoAsignacion.WORST_FIT;
                break;
            case "Paginación":
                algoritmo = MemoriaManager.AlgoritmoAsignacion.PAGINACION;
                break;
            default:
                algoritmo = MemoriaManager.AlgoritmoAsignacion.FIRST_FIT; // Fallback
        }

        // Aplicar algoritmo seleccionado
        memoriaManager.setAlgoritmo(algoritmo);
    }

    /**
     * Asigna memoria a un proceso según los parámetros ingresados.
     *
     * Valida la entrada del usuario, ejecuta la asignación y muestra
     * el resultado tanto en diálogos como en el log de estadísticas.
     */
    private void asignarMemoria() {
        try {
            // Obtener parámetros de la interfaz
            int procesoId = Integer.parseInt(procesoIdField.getText().trim());
            String nombre = procesoNombreField.getText().trim();
            int memoria = (Integer) procesoMemoriaSpinner.getValue();

            // Generar nombre por defecto si está vacío
            if (nombre.isEmpty()) {
                nombre = "Proceso" + procesoId;
            }

            // Intentar asignar memoria usando el gestor local
            boolean asignado = memoriaManager.asignarMemoria(procesoId, nombre, memoria);

            if (asignado) {
                // Mostrar confirmación de éxito
                JOptionPane.showMessageDialog(this,
                        "✅ Memoria asignada exitosamente",
                        "Asignación Exitosa",
                        JOptionPane.INFORMATION_MESSAGE);

                appendToLog("✅ Memoria asignada: " + nombre + " (" + memoria + " KB)\n");

                // Auto-incrementar ID para próximo proceso
                procesoIdField.setText(String.valueOf(procesoId + 1));
                procesoNombreField.setText("Proceso" + (procesoId + 1));
            } else {
                // Mostrar error por memoria insuficiente
                JOptionPane.showMessageDialog(this,
                        "❌ No hay memoria suficiente disponible",
                        "Error de Asignación",
                        JOptionPane.ERROR_MESSAGE);
                appendToLog("❌ Error: No hay memoria suficiente\n");
            }

            // Actualizar visualización independientemente del resultado
            actualizarVisualizacion();

        } catch (NumberFormatException e) {
            // Manejar error de formato en ID de proceso
            JOptionPane.showMessageDialog(this,
                    "❌ ID de proceso debe ser un número",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Libera la memoria asignada a un proceso específico.
     *
     * Utiliza el ID de proceso para identificar y liberar todos los
     * bloques de memoria asociados a ese proceso.
     */
    private void liberarMemoria() {
        try {
            // Obtener ID del proceso a liberar
            int procesoId = Integer.parseInt(procesoIdField.getText().trim());

            // Ejecutar liberación de memoria
            memoriaManager.liberarMemoria(procesoId);

            // Mostrar confirmación de liberación exitosa
            JOptionPane.showMessageDialog(this,
                    "✅ Memoria liberada para proceso " + procesoId,
                    "Liberación Exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            appendToLog("📤 Memoria liberada: Proceso " + procesoId + "\n");
            actualizarVisualizacion();

        } catch (NumberFormatException e) {
            // Manejar error de formato en ID de proceso
            JOptionPane.showMessageDialog(this,
                    "❌ ID de proceso debe ser un número",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reinicia completamente el sistema de memoria.
     *
     * Crea un nuevo gestor de memoria con el tamaño configurado,
     * limpia todos los procesos y reinicia los campos de entrada.
     */
    private void reiniciarMemoria() {
        // Obtener nuevo tamaño de memoria
        int nuevaMemoria = (Integer) memoriaTotalSpinner.getValue();

        // Crear nuevo gestor de memoria con tamaño actualizado
        memoriaManager = new MemoriaManager(nuevaMemoria);
        updateAlgoritmo(); // Reestablecer algoritmo seleccionado

        // Resetear campos de entrada a valores por defecto
        procesoIdField.setText("1");
        procesoNombreField.setText("Proceso1");

        appendToLog("🔄 Memoria reiniciada: " + nuevaMemoria + " KB\n");
        actualizarVisualizacion();
    }

    /**
     * Actualiza todos los componentes visuales con el estado actual.
     *
     * Sincroniza la interfaz gráfica con el estado del gestor de memoria,
     * incluyendo visualización, estadísticas y logs.
     */
    private void actualizarVisualizacion() {
        // Forzar repintado del panel visual personalizado
        memoriaVisualPanel.repaint();

        // Actualizar estadísticas resumidas
        estadisticasLabel.setText(memoriaManager.getEstadisticas());

        // Actualizar estadísticas detalladas en el área de texto
        actualizarEstadisticasDetalladas();
    }

    /**
     * Actualiza el área de estadísticas con información detallada.
     *
     * Genera un reporte completo del estado actual de la memoria,
     * incluyendo detalles de bloques y procesos activos.
     */
    private void actualizarEstadisticasDetalladas() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DETALLES DE MEMORIA ===\n");
        sb.append("Algoritmo: ").append(algoritmoCombo.getSelectedItem()).append("\n\n");

        // Listar todos los bloques de memoria
        List<BloqueMemoria> bloques = memoriaManager.getBloques();
        sb.append("BLOQUES DE MEMORIA:\n");

        for (BloqueMemoria bloque : bloques) {
            sb.append(String.format("• %s\n", bloque.getInfo()));
        }

        // Listar procesos activos en memoria
        sb.append("\nPROCESOS EN MEMORIA:\n");
        memoriaManager.getProcesosEnMemoria().forEach((procesoId, bloquesDelProceso) -> {
            sb.append(String.format("• Proceso %d: %d bloques\n", procesoId, bloquesDelProceso.size()));
        });

        // Actualizar área de texto y posicionar al inicio
        estadisticasArea.setText(sb.toString());
        estadisticasArea.setCaretPosition(0);
    }

    /**
     * Dibuja la representación visual de la memoria en el panel.
     *
     * Renderiza cada bloque de memoria como un rectángulo coloreado
     * con información textual sobre su estado y contenido.
     *
     * @param g Contexto gráfico para dibujar
     */
    private void dibujarMemoria(Graphics g) {
        // Crear contexto gráfico mejorado con anti-aliasing
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Obtener datos actuales del sistema
        List<BloqueMemoria> bloques = memoriaManager.getBloques();
        int panelWidth = memoriaVisualPanel.getWidth();
        int panelHeight = memoriaVisualPanel.getHeight();

        // Validar que hay datos para dibujar
        if (bloques.isEmpty() || panelWidth <= 0 || panelHeight <= 0) {
            return;
        }

        // Parámetros de diseño visual
        int totalMemoria = memoriaManager.getMemoriaTotal();
        int y = 10; // Posición Y inicial
        int alturaBloque = 40; // Altura fija de cada bloque
        int espacioEntreFilas = 50; // Espacio vertical entre filas
        int bloquesPerRow = Math.max(1, panelWidth / 120); // Bloques por fila

        // Dibujar cada bloque de memoria
        for (int i = 0; i < bloques.size(); i++) {
            BloqueMemoria bloque = bloques.get(i);

            // Calcular posición en grilla
            int row = i / bloquesPerRow; // Fila actual
            int col = i % bloquesPerRow; // Columna actual
            int x = 10 + col * 120; // Posición X con espaciado
            int currentY = y + row * espacioEntreFilas; // Posición Y actual

            // Calcular ancho proporcional al tamaño del bloque
            int ancho = Math.max(80, (bloque.getTamaño() * 200) / totalMemoria);

            // Seleccionar color según el tipo de bloque
            Color color = getColorForBloque(bloque);

            // Dibujar rectángulo coloreado para el bloque
            g2d.setColor(color);
            g2d.fillRoundRect(x, currentY, ancho, alturaBloque, 8, 8);

            // Dibujar borde negro del bloque
            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect(x, currentY, ancho, alturaBloque, 8, 8);

            // Configurar fuente para texto informativo
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            FontMetrics fm = g2d.getFontMetrics();

            // Preparar texto informativo del bloque
            String texto1 = bloque.getTipo().toString(); // Tipo de bloque
            String texto2 = bloque.getTamaño() + " KB"; // Tamaño
            String texto3 = bloque.isOcupado() ? bloque.getNombreProceso() : ""; // Proceso

            // Dibujar línea 1: Tipo de bloque (centrado)
            int textX = x + (ancho - fm.stringWidth(texto1)) / 2;
            g2d.drawString(texto1, textX, currentY + 15);

            // Dibujar línea 2: Tamaño del bloque (centrado)
            textX = x + (ancho - fm.stringWidth(texto2)) / 2;
            g2d.drawString(texto2, textX, currentY + 25);

            // Dibujar línea 3: Nombre del proceso (si aplica, centrado)
            if (!texto3.isEmpty()) {
                textX = x + (ancho - fm.stringWidth(texto3)) / 2;
                g2d.drawString(texto3, textX, currentY + 35);
            }
        }

        // Liberar recursos del contexto gráfico
        g2d.dispose();
    }

    /**
     * Determina el color apropiado para un bloque de memoria.
     *
     * @param bloque Bloque de memoria a colorear
     * @return Color correspondiente al tipo de bloque
     */
    private Color getColorForBloque(BloqueMemoria bloque) {
        switch (bloque.getTipo()) {
            case LIBRE:
                return COLOR_LIBRE; // Verde para memoria disponible
            case OCUPADO:
                return COLOR_OCUPADO; // Rojo para memoria en uso
            case SISTEMA:
                return COLOR_SISTEMA; // Azul para memoria del sistema
            case FRAGMENTADO:
                return COLOR_FRAGMENTADO; // Amarillo para fragmentación
            default:
                return Color.LIGHT_GRAY; // Gris por defecto
        }
    }
}