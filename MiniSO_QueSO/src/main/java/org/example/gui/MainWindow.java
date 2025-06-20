package org.example.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Ventana principal del sistema MiniSO
 *
 * Esta clase representa la ventana contenedora principal de la aplicación,
 * implementando una interfaz de pestañas que organiza los diferentes módulos
 * del sistema operativo simulado:
 *
 * - 🔄 Módulo de Procesos: Simulación de planificación de procesos
 * - 🧠 Módulo de Memoria: Gestión y visualización de memoria
 * - 📁 Módulo de Archivos: Sistema de archivos simulado
 * - 🔒 Módulo de Seguridad: Gestión de usuarios y permisos
 *
 * La ventana proporciona una navegación intuitiva entre módulos y mantiene
 * la identidad visual del proyecto académico MiniSO.
 */
public class MainWindow extends JFrame {

    // === COMPONENTES PRINCIPALES DE LA INTERFAZ ===

    /** Panel de pestañas que contiene todos los módulos del sistema */
    private JTabbedPane tabbedPane;

    /** Panel del módulo de gestión de procesos */
    private JPanel procesosPanel;

    /** Panel del módulo de gestión de memoria */
    private JPanel memoriaPanel;

    /** Panel del módulo del sistema de archivos */
    private JPanel archivosPanel;

    /** Panel del módulo de seguridad y usuarios */
    private JPanel seguridadPanel;

    // === CONSTANTES DE CONFIGURACIÓN ===

    /** Ancho por defecto de la ventana principal */
    private static final int WINDOW_WIDTH = 1000;

    /** Alto por defecto de la ventana principal */
    private static final int WINDOW_HEIGHT = 700;

    /** Color principal del tema de la aplicación */
    private static final Color PRIMARY_COLOR = new Color(51, 122, 183);

    /** Color de fondo del footer */
    private static final Color FOOTER_COLOR = new Color(245, 245, 245);

    /**
     * Constructor principal de la ventana
     *
     * Inicializa todos los componentes, configura el layout y hace visible
     * la ventana. Este constructor orquesta todo el proceso de creación
     * de la interfaz principal.
     */
    public MainWindow() {
        initializeComponents();
        setupLayout();
        setVisible(true);
    }

    /**
     * Inicializa todos los componentes de la ventana principal
     *
     * Configura las propiedades básicas de la ventana, crea el panel de pestañas
     * y inicializa cada uno de los módulos del sistema como paneles independientes.
     * Cada módulo es autónomo y maneja su propia lógica de negocio.
     */
    private void initializeComponents() {
        // === CONFIGURACIÓN DE LA VENTANA PRINCIPAL ===
        setTitle("MiniSO - Sistema Operativo Académico - Equipo QueSO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null); // Centrar en pantalla
        setIconImage(createIcon());  // Establecer ícono personalizado

        // === CONFIGURACIÓN DEL PANEL DE PESTAÑAS ===
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 12));

        // Configurar comportamiento de las pestañas
        tabbedPane.setTabPlacement(JTabbedPane.TOP);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        // === CREACIÓN DE MÓDULOS INDEPENDIENTES ===
        // Cada panel es una instancia independiente que maneja su propia funcionalidad
        procesosPanel = new ProcesosPanel();   // Simulación de planificación de procesos
        memoriaPanel = new MemoriaPanel();     // Gestión de memoria virtual y física
        archivosPanel = new ArchivosPanel();   // Sistema de archivos jerárquico
        seguridadPanel = new SeguridadPanel(); // Gestión de usuarios y permisos

        // === CONFIGURACIÓN DE PESTAÑAS CON ICONOS ===
        // Agregar cada módulo como una pestaña con ícono descriptivo
        tabbedPane.addTab("🔄 Procesos", procesosPanel);
        tabbedPane.addTab("🧠 Memoria", memoriaPanel);
        tabbedPane.addTab("📁 Archivos", archivosPanel);
        tabbedPane.addTab("🔒 Seguridad", seguridadPanel);

        // Configurar tooltips informativos para cada pestaña
        tabbedPane.setToolTipTextAt(0, "Simulación de algoritmos de planificación de procesos");
        tabbedPane.setToolTipTextAt(1, "Gestión y visualización de memoria del sistema");
        tabbedPane.setToolTipTextAt(2, "Explorador de archivos y gestión de almacenamiento");
        tabbedPane.setToolTipTextAt(3, "Administración de usuarios y control de acceso");
    }

    /**
     * Configura el layout principal de la ventana
     *
     * Organiza la ventana en tres secciones principales usando BorderLayout:
     * - Norte: Header con título y información del equipo
     * - Centro: Panel de pestañas con los módulos principales
     * - Sur: Footer con información de estado
     */
    private void setupLayout() {
        // Establecer layout principal de la ventana
        setLayout(new BorderLayout());

        // === SECCIÓN SUPERIOR: HEADER ===
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // === SECCIÓN CENTRAL: MÓDULOS PRINCIPALES ===
        add(tabbedPane, BorderLayout.CENTER);

        // === SECCIÓN INFERIOR: FOOTER ===
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }

    /**
     * Crea el panel de encabezado de la aplicación
     *
     * Diseña un header atractivo con el título del proyecto y la información
     * del equipo de desarrollo, usando el color corporativo de la aplicación.
     *
     * @return JPanel configurado como header de la aplicación
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setBorder(new EmptyBorder(10, 10, 10, 10));

        // === TÍTULO PRINCIPAL ===
        JLabel titleLabel = new JLabel("MiniSO - Simuladores de Sistema Operativo");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        // === INFORMACIÓN DEL EQUIPO ===
        JLabel teamLabel = new JLabel("Equipo QueSO - Universidad Veracruzana");
        teamLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        teamLabel.setForeground(Color.WHITE);

        // Ensamblar header
        header.add(titleLabel, BorderLayout.WEST);
        header.add(teamLabel, BorderLayout.EAST);

        return header;
    }

    /**
     * Crea el panel de pie de página de la aplicación
     *
     * Diseña un footer discreto que muestra el estado actual del sistema
     * y confirma que todos los simuladores están operativos.
     *
     * @return JPanel configurado como footer de la aplicación
     */
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(FOOTER_COLOR);
        footer.setBorder(new EmptyBorder(5, 5, 5, 5));

        // === INDICADOR DE ESTADO ===
        JLabel statusLabel = new JLabel("Estado: Simuladores Independientes Activos ✅");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(60, 60, 60)); // Gris oscuro para mejor legibilidad

        footer.add(statusLabel);
        return footer;
    }

    /**
     * Crea un ícono personalizado para la ventana de la aplicación
     *
     * Genera programáticamente un ícono simple pero distintivo que representa
     * el concepto de "Sistema Operativo" usando las iniciales "SO" sobre un
     * fondo del color corporativo de la aplicación.
     *
     * @return Image que representa el ícono de la aplicación
     */
    private Image createIcon() {
        // === CONFIGURACIÓN DEL CANVAS ===
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();

        // Habilitar antialiasing para mejor calidad visual
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // === DIBUJO DEL FONDO DEL ÍCONO ===
        g2d.setColor(PRIMARY_COLOR);
        g2d.fillRoundRect(4, 4, 24, 24, 8, 8); // Rectángulo redondeado

        // === DIBUJO DEL TEXTO "SO" ===
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));

        // Centrar texto en el ícono
        FontMetrics fm = g2d.getFontMetrics();
        String text = "SO";
        int x = (32 - fm.stringWidth(text)) / 2;
        int y = (32 - fm.getHeight()) / 2 + fm.getAscent();

        g2d.drawString(text, x, y);

        // Liberar recursos gráficos
        g2d.dispose();

        return icon;
    }

    /**
     * Método principal de entrada a la aplicación
     *
     * Punto de inicio del programa que configura el entorno Swing y crea
     * la ventana principal en el Event Dispatch Thread para garantizar
     * la seguridad de hilos en la interfaz gráfica.
     *
     * El método incluye configuración opcional del Look and Feel del sistema
     * para una mejor integración con el sistema operativo host.
     *
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        // === CONFIGURACIÓN DEL ENTORNO SWING ===

        // Configurar propiedades del sistema para mejor rendimiento
        System.setProperty("swing.aatext", "true"); // Antialiasing de texto
        System.setProperty("awt.useSystemAAFontSettings", "on"); // Suavizado de fuentes del sistema

        // Ejecutar creación de la GUI en el Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // === CONFIGURACIÓN OPCIONAL DEL LOOK AND FEEL ===
                // Uncomment para usar el aspecto nativo del sistema operativo
                // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());

                // Configurar comportamiento de las ventanas
                JFrame.setDefaultLookAndFeelDecorated(false);

                // === CREACIÓN E INICIALIZACIÓN DE LA APLICACIÓN ===
                new MainWindow();

                // Mensaje de confirmación en consola (útil para debugging)
                System.out.println("🚀 MiniSO iniciado exitosamente");
                System.out.println("📚 Sistema Operativo Académico - Equipo QueSO");
                System.out.println("🏫 Universidad Veracruzana - 2025");

            } catch (Exception e) {
                // Manejo de errores en la inicialización
                System.err.println("❌ Error iniciando MiniSO: " + e.getMessage());
                e.printStackTrace();

                // Mostrar diálogo de error al usuario
                JOptionPane.showMessageDialog(null,
                        "Error iniciando la aplicación MiniSO.\n" +
                                "Por favor, verifique la instalación de Java.",
                        "Error de Inicialización",
                        JOptionPane.ERROR_MESSAGE);

                // Terminar aplicación con código de error
                System.exit(1);
            }
        });
    }

    // === MÉTODOS AUXILIARES Y DE UTILIDAD ===

    /**
     * Obtiene la pestaña actualmente seleccionada
     *
     * @return Índice de la pestaña activa (0-3)
     */
    public int getPestañaActiva() {
        return tabbedPane.getSelectedIndex();
    }

    /**
     * Cambia a una pestaña específica programáticamente
     *
     * @param indice Índice de la pestaña a seleccionar (0-3)
     */
    public void cambiarPestaña(int indice) {
        if (indice >= 0 && indice < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(indice);
        }
    }

    /**
     * Obtiene referencia al panel de un módulo específico
     *
     * @param modulo Nombre del módulo ("procesos", "memoria", "archivos", "seguridad")
     * @return JPanel del módulo solicitado, o null si no existe
     */
    public JPanel getModulo(String modulo) {
        switch (modulo.toLowerCase()) {
            case "procesos":
                return procesosPanel;
            case "memoria":
                return memoriaPanel;
            case "archivos":
                return archivosPanel;
            case "seguridad":
                return seguridadPanel;
            default:
                return null;
        }
    }
}