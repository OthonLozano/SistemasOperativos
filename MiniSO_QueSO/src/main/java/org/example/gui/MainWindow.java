package org.example.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MainWindow extends JFrame {

    // Componentes principales
    private JTabbedPane tabbedPane;
    private JPanel procesosPanel;
    private JPanel memoriaPanel;
    private JPanel archivosPanel;
    private JPanel seguridadPanel;

    // Constructor
    public MainWindow() {
        initializeComponents();
        setupLayout();
        setVisible(true);
    }

    private void initializeComponents() {
        // Configuración de la ventana principal
        setTitle("MiniSO - Sistema Operativo Académico - Equipo QueSO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setIconImage(createIcon());

        // Crear el panel de pestañas
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 12));

        // Crear paneles para cada módulo (AHORA INDEPENDIENTES)
        procesosPanel = new ProcesosPanel();
        memoriaPanel = new MemoriaPanel();
        archivosPanel = new ArchivosPanel();
        seguridadPanel = new SeguridadPanel();

        // Agregar pestañas - SIN DASHBOARD
        tabbedPane.addTab("🔄 Procesos", procesosPanel);
        tabbedPane.addTab("🧠 Memoria", memoriaPanel);
        tabbedPane.addTab("📁 Archivos", archivosPanel);
        tabbedPane.addTab("🔒 Seguridad", seguridadPanel);
    }

    private void setupLayout() {
        // Layout principal
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Panel principal con pestañas
        add(tabbedPane, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(51, 122, 183));
        header.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("MiniSO - Simuladores de Sistema Operativo");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JLabel teamLabel = new JLabel("Equipo QueSO - Universidad Veracruzana");
        teamLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        teamLabel.setForeground(Color.WHITE);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(teamLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(new Color(245, 245, 245));
        footer.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel statusLabel = new JLabel("Estado: Simuladores Independientes Activos ✅");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        footer.add(statusLabel);
        return footer;
    }

    private Image createIcon() {
        // Crear un ícono simple para la ventana
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setColor(new Color(51, 122, 183));
        g2d.fillRoundRect(4, 4, 24, 24, 8, 8);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("SO", 8, 20);
        g2d.dispose();
        return icon;
    }

    // Método main para ejecutar la aplicación
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
//            try {
//                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            new MainWindow();
        });
    }
}