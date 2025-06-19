package org.example.gui;

import org.example.core.MemoriaManager;
import org.example.models.BloqueMemoria;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class MemoriaPanel extends JPanel {

    // Componentes GUI
    private JComboBox<String> algoritmoCombo;
    private JSpinner memoriaTotalSpinner;
    private JPanel memoriaVisualPanel;
    private JTextArea estadisticasArea;
    private JTextField procesoIdField;
    private JTextField procesoNombreField;
    private JSpinner procesoMemoriaSpinner;
    private JButton asignarBtn;
    private JButton liberarBtn;
    private JButton reiniciarBtn;
    private JLabel estadisticasLabel;

    // Lógica del sistema - SOLO LOCAL
    private MemoriaManager memoriaManager;
    private static final int MEMORIA_DEFAULT = 512; // 512 KB

    // Colores para visualización
    private static final Color COLOR_LIBRE = new Color(144, 238, 144);      // Verde claro
    private static final Color COLOR_OCUPADO = new Color(255, 99, 132);     // Rojo claro
    private static final Color COLOR_SISTEMA = new Color(54, 162, 235);     // Azul
    private static final Color COLOR_FRAGMENTADO = new Color(255, 206, 86); // Amarillo

    public MemoriaPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        initializeMemoriaManager(); // SIEMPRE LOCAL
        actualizarVisualizacion();
    }

    private void initializeComponents() {
        // Combo de algoritmos
        algoritmoCombo = new JComboBox<>(new String[]{
                "First Fit", "Best Fit", "Worst Fit", "Paginación"
        });

        // Spinner para memoria total
        memoriaTotalSpinner = new JSpinner(new SpinnerNumberModel(MEMORIA_DEFAULT, 128, 2048, 64));

        // Panel visual de memoria
        memoriaVisualPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                dibujarMemoria(g);
            }
        };
        memoriaVisualPanel.setPreferredSize(new Dimension(600, 300));
        memoriaVisualPanel.setBackground(Color.WHITE);
        memoriaVisualPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Área de estadísticas
        estadisticasArea = new JTextArea(6, 30);
        estadisticasArea.setEditable(false);
        estadisticasArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        estadisticasArea.setBackground(new Color(248, 248, 248));

        // Campos para asignar memoria
        procesoIdField = new JTextField("1", 5);
        procesoNombreField = new JTextField("Proceso1", 10);
        procesoMemoriaSpinner = new JSpinner(new SpinnerNumberModel(32, 4, 256, 4));

        // Botones
        asignarBtn = new JButton("📥 Asignar Memoria");
        liberarBtn = new JButton("📤 Liberar Memoria");
        reiniciarBtn = new JButton("🔄 Reiniciar");

        // Label de estadísticas
        estadisticasLabel = new JLabel();
        estadisticasLabel.setFont(new Font("Arial", Font.BOLD, 12));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panel superior - Configuración
        JPanel configPanel = createConfigPanel();
        add(configPanel, BorderLayout.NORTH);

        // Panel central - División
        JSplitPane centralSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centralSplit.setLeftComponent(createVisualizacionPanel());
        centralSplit.setRightComponent(createControlPanel());
        centralSplit.setDividerLocation(650);
        add(centralSplit, BorderLayout.CENTER);

        // Panel inferior - Estadísticas
        JPanel estadisticasPanel = createEstadisticasPanel();
        add(estadisticasPanel, BorderLayout.SOUTH);
    }

    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("Configuración de Memoria"));

        panel.add(new JLabel("Algoritmo:"));
        panel.add(algoritmoCombo);

        panel.add(Box.createHorizontalStrut(20));

        panel.add(new JLabel("Memoria Total (KB):"));
        panel.add(memoriaTotalSpinner);

        return panel;
    }

    private JPanel createVisualizacionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Visualización de Memoria"));

        // Panel de memoria visual
        JScrollPane scrollPane = new JScrollPane(memoriaVisualPanel);
        scrollPane.setPreferredSize(new Dimension(600, 300));

        panel.add(scrollPane, BorderLayout.CENTER);

        // Leyenda
        JPanel leyendaPanel = createLeyendaPanel();
        panel.add(leyendaPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLeyendaPanel() {
        JPanel leyenda = new JPanel(new FlowLayout());
        leyenda.setBorder(new TitledBorder("Leyenda"));

        leyenda.add(createColorLabel("Libre", COLOR_LIBRE));
        leyenda.add(createColorLabel("Ocupado", COLOR_OCUPADO));
        leyenda.add(createColorLabel("Sistema", COLOR_SISTEMA));
        leyenda.add(createColorLabel("Fragmentado", COLOR_FRAGMENTADO));

        return leyenda;
    }

    private JLabel createColorLabel(String texto, Color color) {
        JLabel label = new JLabel("  " + texto);
        label.setOpaque(true);
        label.setBackground(color);
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        label.setPreferredSize(new Dimension(80, 20));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Control de Procesos"));

        // Formulario de asignación
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("ID Proceso:"), gbc);
        gbc.gridx = 1;
        formPanel.add(procesoIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        formPanel.add(procesoNombreField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Memoria (KB):"), gbc);
        gbc.gridx = 1;
        formPanel.add(procesoMemoriaSpinner, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Botones
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        buttonPanel.add(asignarBtn);
        buttonPanel.add(liberarBtn);
        buttonPanel.add(reiniciarBtn);

        panel.add(buttonPanel, BorderLayout.CENTER);

        // Estadísticas detalladas
        JScrollPane statsScroll = new JScrollPane(estadisticasArea);
        panel.add(statsScroll, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createEstadisticasPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Estadísticas de Memoria"));

        panel.add(estadisticasLabel, BorderLayout.CENTER);

        return panel;
    }

    private void setupEventHandlers() {
        // Cambio de algoritmo
        algoritmoCombo.addActionListener(e -> {
            updateAlgoritmo();
            actualizarVisualizacion();
        });

        // Cambio de memoria total
        memoriaTotalSpinner.addChangeListener(e -> {
            reiniciarMemoria();
        });

        // Botón asignar
        asignarBtn.addActionListener(e -> asignarMemoria());

        // Botón liberar
        liberarBtn.addActionListener(e -> liberarMemoria());

        // Botón reiniciar
        reiniciarBtn.addActionListener(e -> reiniciarMemoria());
    }

    private void initializeMemoriaManager() {
        // SIMPLIFICADO: Solo gestor local
        memoriaManager = new MemoriaManager(MEMORIA_DEFAULT);
        appendToLog("✅ Gestor de memoria local inicializado\n");
        appendToLog("ℹ️ Simulador independiente - 512 KB de memoria\n");
    }

    private void appendToLog(String texto) {
        String contenidoActual = estadisticasArea.getText();
        estadisticasArea.setText(contenidoActual + texto);
        estadisticasArea.setCaretPosition(estadisticasArea.getDocument().getLength());
    }

    private void updateAlgoritmo() {
        String selected = (String) algoritmoCombo.getSelectedItem();
        MemoriaManager.AlgoritmoAsignacion algoritmo;

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
                algoritmo = MemoriaManager.AlgoritmoAsignacion.FIRST_FIT;
        }

        memoriaManager.setAlgoritmo(algoritmo);
    }

    private void asignarMemoria() {
        try {
            int procesoId = Integer.parseInt(procesoIdField.getText().trim());
            String nombre = procesoNombreField.getText().trim();
            int memoria = (Integer) procesoMemoriaSpinner.getValue();

            if (nombre.isEmpty()) {
                nombre = "Proceso" + procesoId;
            }

            // SIMPLIFICADO: Solo gestor local
            boolean asignado = memoriaManager.asignarMemoria(procesoId, nombre, memoria);

            if (asignado) {
                JOptionPane.showMessageDialog(this,
                        "✅ Memoria asignada exitosamente",
                        "Asignación Exitosa",
                        JOptionPane.INFORMATION_MESSAGE);

                appendToLog("✅ Memoria asignada: " + nombre + " (" + memoria + " KB)\n");

                // Incrementar para el próximo proceso
                procesoIdField.setText(String.valueOf(procesoId + 1));
                procesoNombreField.setText("Proceso" + (procesoId + 1));
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ No hay memoria suficiente disponible",
                        "Error de Asignación",
                        JOptionPane.ERROR_MESSAGE);
                appendToLog("❌ Error: No hay memoria suficiente\n");
            }

            actualizarVisualizacion();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "❌ ID de proceso debe ser un número",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void liberarMemoria() {
        try {
            int procesoId = Integer.parseInt(procesoIdField.getText().trim());
            memoriaManager.liberarMemoria(procesoId);

            JOptionPane.showMessageDialog(this,
                    "✅ Memoria liberada para proceso " + procesoId,
                    "Liberación Exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            appendToLog("📤 Memoria liberada: Proceso " + procesoId + "\n");
            actualizarVisualizacion();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "❌ ID de proceso debe ser un número",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reiniciarMemoria() {
        int nuevaMemoria = (Integer) memoriaTotalSpinner.getValue();

        // SIMPLIFICADO: Siempre crear nuevo manager local
        memoriaManager = new MemoriaManager(nuevaMemoria);
        updateAlgoritmo();

        // Resetear campos
        procesoIdField.setText("1");
        procesoNombreField.setText("Proceso1");

        appendToLog("🔄 Memoria reiniciada: " + nuevaMemoria + " KB\n");
        actualizarVisualizacion();
    }

    private void actualizarVisualizacion() {
        // Actualizar panel visual
        memoriaVisualPanel.repaint();

        // Actualizar estadísticas
        estadisticasLabel.setText(memoriaManager.getEstadisticas());

        // Actualizar estadísticas detalladas
        actualizarEstadisticasDetalladas();
    }

    private void actualizarEstadisticasDetalladas() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DETALLES DE MEMORIA ===\n");
        sb.append("Algoritmo: ").append(algoritmoCombo.getSelectedItem()).append("\n\n");

        List<BloqueMemoria> bloques = memoriaManager.getBloques();
        sb.append("BLOQUES DE MEMORIA:\n");

        for (BloqueMemoria bloque : bloques) {
            sb.append(String.format("• %s\n", bloque.getInfo()));
        }

        sb.append("\nPROCESOS EN MEMORIA:\n");
        memoriaManager.getProcesosEnMemoria().forEach((procesoId, bloquesDelProceso) -> {
            sb.append(String.format("• Proceso %d: %d bloques\n", procesoId, bloquesDelProceso.size()));
        });

        estadisticasArea.setText(sb.toString());
        estadisticasArea.setCaretPosition(0);
    }

    private void dibujarMemoria(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<BloqueMemoria> bloques = memoriaManager.getBloques();
        int panelWidth = memoriaVisualPanel.getWidth();
        int panelHeight = memoriaVisualPanel.getHeight();

        if (bloques.isEmpty() || panelWidth <= 0 || panelHeight <= 0) {
            return;
        }

        int totalMemoria = memoriaManager.getMemoriaTotal();
        int y = 10;
        int alturaBloque = 40;
        int espacioEntreFilas = 50;
        int bloquesPerRow = Math.max(1, panelWidth / 120);

        for (int i = 0; i < bloques.size(); i++) {
            BloqueMemoria bloque = bloques.get(i);

            // Calcular posición
            int row = i / bloquesPerRow;
            int col = i % bloquesPerRow;
            int x = 10 + col * 120;
            int currentY = y + row * espacioEntreFilas;

            // Calcular ancho proporcional al tamaño
            int ancho = Math.max(80, (bloque.getTamaño() * 200) / totalMemoria);

            // Seleccionar color
            Color color = getColorForBloque(bloque);

            // Dibujar bloque
            g2d.setColor(color);
            g2d.fillRoundRect(x, currentY, ancho, alturaBloque, 8, 8);

            // Borde
            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect(x, currentY, ancho, alturaBloque, 8, 8);

            // Texto
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            FontMetrics fm = g2d.getFontMetrics();

            String texto1 = bloque.getTipo().toString();
            String texto2 = bloque.getTamaño() + " KB";
            String texto3 = bloque.isOcupado() ? bloque.getNombreProceso() : "";

            int textX = x + (ancho - fm.stringWidth(texto1)) / 2;
            g2d.drawString(texto1, textX, currentY + 15);

            textX = x + (ancho - fm.stringWidth(texto2)) / 2;
            g2d.drawString(texto2, textX, currentY + 25);

            if (!texto3.isEmpty()) {
                textX = x + (ancho - fm.stringWidth(texto3)) / 2;
                g2d.drawString(texto3, textX, currentY + 35);
            }
        }

        g2d.dispose();
    }

    private Color getColorForBloque(BloqueMemoria bloque) {
        switch (bloque.getTipo()) {
            case LIBRE:
                return COLOR_LIBRE;
            case OCUPADO:
                return COLOR_OCUPADO;
            case SISTEMA:
                return COLOR_SISTEMA;
            case FRAGMENTADO:
                return COLOR_FRAGMENTADO;
            default:
                return Color.LIGHT_GRAY;
        }
    }
}