package org.example.gui;

import org.example.core.SistemaArchivos;
import org.example.models.Archivo;
import org.example.models.Directorio;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ArchivosPanel extends JPanel {

    // Componentes GUI
    private JTree arbolDirectorios;
    private DefaultTreeModel modeloArbol;
    private DefaultMutableTreeNode nodoRaiz;
    private JTable tablaArchivos;
    private DefaultTableModel modeloTabla;
    private JTextField rutaField;
    private JTextField busquedaField;
    private JTextArea editorArea;
    private JLabel estadisticasLabel;

    // Botones
    private JButton nuevaCarpetaBtn;
    private JButton nuevoArchivoBtn;
    private JButton eliminarBtn;
    private JButton editarBtn;
    private JButton guardarBtn;
    private JButton buscarBtn;
    private JButton homeBtn;

    // Lógica del sistema - SOLO LOCAL
    private SistemaArchivos sistemaArchivos;
    private Archivo archivoEditando;

    public ArchivosPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        initializeSistemaArchivos(); // SIEMPRE LOCAL
        actualizarVista();
    }

    private void initializeComponents() {
        // Árbol de directorios
        nodoRaiz = new DefaultMutableTreeNode("📁 /");
        modeloArbol = new DefaultTreeModel(nodoRaiz);
        arbolDirectorios = new JTree(modeloArbol);
        arbolDirectorios.setRootVisible(true);
        arbolDirectorios.setShowsRootHandles(true);

        // Tabla de archivos
        String[] columnNames = {"", "Nombre", "Tipo", "Tamaño", "Modificado", "Permisos"};
        modeloTabla = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaArchivos = new JTable(modeloTabla);
        tablaArchivos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaArchivos.getColumnModel().getColumn(0).setMaxWidth(30); // Columna de icono
        tablaArchivos.getColumnModel().getColumn(2).setMaxWidth(80);  // Tipo
        tablaArchivos.getColumnModel().getColumn(3).setMaxWidth(80);  // Tamaño
        tablaArchivos.getColumnModel().getColumn(5).setMaxWidth(80);  // Permisos

        // Campos de texto
        rutaField = new JTextField();
        rutaField.setEditable(false);
        rutaField.setFont(new Font("Consolas", Font.PLAIN, 12));

        busquedaField = new JTextField(20);

        // Editor de texto
        editorArea = new JTextArea(8, 40);
        editorArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        editorArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Botones
        nuevaCarpetaBtn = new JButton("📁 Nueva Carpeta");
        nuevoArchivoBtn = new JButton("📄 Nuevo Archivo");
        eliminarBtn = new JButton("🗑️ Eliminar");
        editarBtn = new JButton("✏️ Editar");
        guardarBtn = new JButton("💾 Guardar");
        buscarBtn = new JButton("🔍 Buscar");
        homeBtn = new JButton("🏠 Inicio");

        // Labels
        estadisticasLabel = new JLabel();
        estadisticasLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        // Estados iniciales
        guardarBtn.setEnabled(false);
        editarBtn.setEnabled(false);
        eliminarBtn.setEnabled(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panel superior - Navegación
        JPanel navegacionPanel = createNavegacionPanel();
        add(navegacionPanel, BorderLayout.NORTH);

        // Panel central - División principal
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Panel izquierdo - Árbol de directorios
        JPanel arbolPanel = createArbolPanel();
        mainSplit.setLeftComponent(arbolPanel);

        // Panel derecho - División vertical
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        // Panel superior derecho - Lista de archivos
        JPanel archivosPanel = createArchivosPanel();
        rightSplit.setTopComponent(archivosPanel);

        // Panel inferior derecho - Editor
        JPanel editorPanel = createEditorPanel();
        rightSplit.setBottomComponent(editorPanel);

        rightSplit.setDividerLocation(300);
        mainSplit.setRightComponent(rightSplit);

        mainSplit.setDividerLocation(250);
        add(mainSplit, BorderLayout.CENTER);

        // Panel inferior - Estadísticas
        JPanel estadisticasPanel = createEstadisticasPanel();
        add(estadisticasPanel, BorderLayout.SOUTH);
    }

    private JPanel createNavegacionPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new TitledBorder("Navegación"));

        // Panel izquierdo - Ruta
        JPanel rutaPanel = new JPanel(new BorderLayout());
        rutaPanel.add(new JLabel("Ruta: "), BorderLayout.WEST);
        rutaPanel.add(rutaField, BorderLayout.CENTER);

        // Panel derecho - Botones de navegación
        JPanel botonesPanel = new JPanel(new FlowLayout());
        botonesPanel.add(homeBtn);
        botonesPanel.add(busquedaField);
        botonesPanel.add(buscarBtn);

        panel.add(rutaPanel, BorderLayout.CENTER);
        panel.add(botonesPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createArbolPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Directorios"));

        JScrollPane scrollPane = new JScrollPane(arbolDirectorios);
        scrollPane.setPreferredSize(new Dimension(250, 400));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createArchivosPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Archivos y Carpetas"));

        // Tabla con scroll
        JScrollPane scrollPane = new JScrollPane(tablaArchivos);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel botonesPanel = new JPanel(new FlowLayout());
        botonesPanel.add(nuevaCarpetaBtn);
        botonesPanel.add(nuevoArchivoBtn);
        botonesPanel.add(eliminarBtn);
        botonesPanel.add(editarBtn);

        panel.add(botonesPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Editor de Texto"));

        JScrollPane scrollPane = new JScrollPane(editorArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel de botones del editor
        JPanel editorBotones = new JPanel(new FlowLayout());
        editorBotones.add(guardarBtn);
        editorBotones.add(new JLabel("| Archivo: "));

        JLabel archivoLabel = new JLabel("Ninguno");
        archivoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        editorBotones.add(archivoLabel);

        panel.add(editorBotones, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createEstadisticasPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Estadísticas"));
        panel.add(estadisticasLabel, BorderLayout.CENTER);
        return panel;
    }

    private void setupEventHandlers() {
        // Árbol de directorios
        arbolDirectorios.addTreeSelectionListener(e -> {
            TreePath path = e.getPath();
            if (path != null) {
                navegarDesdePath(path);
            }
        });

        // Tabla de archivos - Doble clic
        tablaArchivos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    abrirElementoSeleccionado();
                }
            }
        });

        // Selección en tabla
        tablaArchivos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarBotonesSegunSeleccion();
            }
        });

        // Botones
        homeBtn.addActionListener(e -> irAHome());
        nuevaCarpetaBtn.addActionListener(e -> crearNuevaCarpeta());
        nuevoArchivoBtn.addActionListener(e -> crearNuevoArchivo());
        eliminarBtn.addActionListener(e -> eliminarElemento());
        editarBtn.addActionListener(e -> editarArchivo());
        guardarBtn.addActionListener(e -> guardarArchivo());
        buscarBtn.addActionListener(e -> buscarArchivos());

        // Campo de búsqueda - Enter
        busquedaField.addActionListener(e -> buscarArchivos());
    }

    private void initializeSistemaArchivos() {
        // SIMPLIFICADO: Solo sistema local
        sistemaArchivos = new SistemaArchivos("usuario_local");
        System.out.println("✅ Sistema de archivos local inicializado");
        System.out.println("ℹ️ Simulador independiente de archivos");
    }

    private void actualizarVista() {
        actualizarArbolDirectorios();
        actualizarTablaArchivos();
        actualizarRuta();
        actualizarEstadisticas();
    }

    private void actualizarArbolDirectorios() {
        nodoRaiz.removeAllChildren();
        construirArbol(nodoRaiz, sistemaArchivos.getRaiz());
        modeloArbol.reload();

        // Expandir nodos
        for (int i = 0; i < arbolDirectorios.getRowCount(); i++) {
            arbolDirectorios.expandRow(i);
        }
    }

    private void construirArbol(DefaultMutableTreeNode nodo, Directorio directorio) {
        for (Directorio subdir : directorio.getSubdirectorios()) {
            DefaultMutableTreeNode nodoHijo = new DefaultMutableTreeNode("📁 " + subdir.getNombre());
            nodo.add(nodoHijo);
            construirArbol(nodoHijo, subdir);
        }
    }

    private void actualizarTablaArchivos() {
        modeloTabla.setRowCount(0);

        Directorio dirActual = sistemaArchivos.getDirectorioActual();

        // Agregar directorios
        for (Directorio subdir : dirActual.getSubdirectorios()) {
            Object[] rowData = {
                    "📁",
                    subdir.getNombre(),
                    "Carpeta",
                    subdir.getTamañoFormateado(),
                    subdir.getFechaModificacion().toLocalDate().toString(),
                    subdir.getPermisos().toString()
            };
            modeloTabla.addRow(rowData);
        }

        // Agregar archivos
        for (Archivo archivo : dirActual.getArchivos()) {
            Object[] rowData = {
                    archivo.getIcono(),
                    archivo.getNombre(),
                    archivo.getTipo().toString(),
                    archivo.getTamañoFormateado(),
                    archivo.getFechaModificacion().toLocalDate().toString(),
                    archivo.getPermisos().toString()
            };
            modeloTabla.addRow(rowData);
        }
    }

    private void actualizarRuta() {
        rutaField.setText(sistemaArchivos.getRutaActual());
    }

    private void actualizarEstadisticas() {
        estadisticasLabel.setText(sistemaArchivos.getEstadisticas());
    }

    private void actualizarBotonesSegunSeleccion() {
        int filaSeleccionada = tablaArchivos.getSelectedRow();
        boolean haySeleccion = filaSeleccionada >= 0;

        eliminarBtn.setEnabled(haySeleccion);

        if (haySeleccion) {
            String nombre = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
            String tipo = (String) modeloTabla.getValueAt(filaSeleccionada, 2);

            // Habilitar editar solo para archivos de texto
            Archivo archivo = sistemaArchivos.getDirectorioActual().getArchivo(nombre);
            editarBtn.setEnabled(archivo != null &&
                    (archivo.getTipo() == Archivo.TipoArchivo.TEXTO ||
                            archivo.getTipo() == Archivo.TipoArchivo.OTRO));
        } else {
            editarBtn.setEnabled(false);
        }
    }

    private void navegarDesdePath(TreePath path) {
        StringBuilder ruta = new StringBuilder("/");

        // Construir ruta desde el path del árbol
        for (int i = 1; i < path.getPathCount(); i++) {
            String nombre = path.getPathComponent(i).toString();
            // Quitar el emoji del nombre
            nombre = nombre.substring(2);
            ruta.append(nombre);
            if (i < path.getPathCount() - 1) {
                ruta.append("/");
            }
        }

        if (sistemaArchivos.cambiarDirectorio(ruta.toString())) {
            actualizarTablaArchivos();
            actualizarRuta();
            actualizarEstadisticas();
        }
    }

    private void abrirElementoSeleccionado() {
        int filaSeleccionada = tablaArchivos.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String nombre = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
            String tipo = (String) modeloTabla.getValueAt(filaSeleccionada, 2);

            if (tipo.equals("Carpeta")) {
                // Navegar al directorio
                if (sistemaArchivos.cambiarDirectorio(nombre)) {
                    actualizarVista();
                }
            } else {
                // Abrir archivo para editar
                editarArchivo();
            }
        }
    }

    private void irAHome() {
        if (sistemaArchivos.cambiarDirectorio("~")) {
            actualizarVista();
        }
    }

    private void crearNuevaCarpeta() {
        String nombre = JOptionPane.showInputDialog(this,
                "Nombre de la nueva carpeta:",
                "Nueva Carpeta",
                JOptionPane.QUESTION_MESSAGE);

        if (nombre != null && !nombre.trim().isEmpty()) {
            try {
                sistemaArchivos.crearDirectorio(nombre.trim());
                actualizarVista();
                JOptionPane.showMessageDialog(this,
                        "✅ Carpeta '" + nombre + "' creada exitosamente",
                        "Carpeta Creada",
                        JOptionPane.INFORMATION_MESSAGE);

                System.out.println("📁 Carpeta creada: " + nombre);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "❌ Error creando carpeta: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    private void crearNuevoArchivo() {
        String nombre = JOptionPane.showInputDialog(this,
                "Nombre del nuevo archivo (incluye extensión):",
                "Nuevo Archivo",
                JOptionPane.QUESTION_MESSAGE);

        if (nombre != null && !nombre.trim().isEmpty()) {
            try {
                // SIMPLIFICADO: Solo contenido básico
                String contenidoInicial = "# Archivo creado en MiniSO\n" +
                        "# Simulador de Sistema de Archivos\n" +
                        "# Fecha: " + java.time.LocalDateTime.now() + "\n\n" +
                        "Contenido del archivo...\n";

                Archivo archivo = sistemaArchivos.crearArchivo(nombre.trim(), contenidoInicial);

                actualizarVista();
                JOptionPane.showMessageDialog(this,
                        "✅ Archivo '" + nombre + "' creado exitosamente",
                        "Archivo Creado",
                        JOptionPane.INFORMATION_MESSAGE);

                System.out.println("📄 Archivo creado: " + nombre);

                // Seleccionar el archivo recién creado en la tabla
                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    if (modeloTabla.getValueAt(i, 1).equals(nombre.trim())) {
                        tablaArchivos.setRowSelectionInterval(i, i);
                        break;
                    }
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "❌ Error creando archivo: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    private void eliminarElemento() {
        int filaSeleccionada = tablaArchivos.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String nombre = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
            String tipo = (String) modeloTabla.getValueAt(filaSeleccionada, 2);

            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de eliminar '" + nombre + "'?",
                    "Confirmar Eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirmacion == JOptionPane.YES_OPTION) {
                try {
                    boolean eliminado;
                    if (tipo.equals("Carpeta")) {
                        eliminado = sistemaArchivos.eliminarDirectorio(nombre);
                    } else {
                        eliminado = sistemaArchivos.eliminarArchivo(nombre);
                    }

                    if (eliminado) {
                        actualizarVista();
                        JOptionPane.showMessageDialog(this,
                                "✅ '" + nombre + "' eliminado exitosamente",
                                "Eliminación Exitosa",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "❌ No se pudo eliminar '" + nombre + "'",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                            "❌ Error eliminando: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void editarArchivo() {
        int filaSeleccionada = tablaArchivos.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String nombre = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
            archivoEditando = sistemaArchivos.abrirArchivo(nombre);

            if (archivoEditando != null) {
                try {
                    String contenido = archivoEditando.leerContenido();
                    editorArea.setText(contenido);
                    editorArea.setCaretPosition(0);
                    guardarBtn.setEnabled(true);

                    // Actualizar label del archivo
                    Component[] components = ((JPanel) ((JPanel) editorArea.getParent().getParent()).getComponent(1)).getComponents();
                    for (Component comp : components) {
                        if (comp instanceof JLabel && ((JLabel) comp).getText().startsWith("Ninguno")) {
                            ((JLabel) comp).setText(archivoEditando.getNombre());
                            break;
                        }
                    }

                } catch (Exception e) {
                    /*JOptionPane.showMessageDialog(this,
                            "❌ Error abriendo archivo: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);*/
                }
            }
        }
    }

    private void guardarArchivo() {
        if (archivoEditando != null) {
            try {
                String nuevoContenido = editorArea.getText();
                archivoEditando.modificarContenido(nuevoContenido);

                actualizarVista();
                JOptionPane.showMessageDialog(this,
                        "✅ Archivo guardado exitosamente",
                        "Guardado",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "❌ Error guardando archivo: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void buscarArchivos() {
        String patron = busquedaField.getText().trim();
        if (!patron.isEmpty()) {
            List<Archivo> resultados = sistemaArchivos.buscarArchivos(patron);

            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No se encontraron archivos que coincidan con: " + patron,
                        "Búsqueda",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                StringBuilder mensaje = new StringBuilder("Archivos encontrados:\n\n");
                for (Archivo archivo : resultados) {
                    mensaje.append("📄 ").append(archivo.getRutaCompleta()).append("\n");
                }

                JTextArea textArea = new JTextArea(mensaje.toString());
                textArea.setEditable(false);
                textArea.setRows(10);
                textArea.setColumns(50);

                JScrollPane scrollPane = new JScrollPane(textArea);
                JOptionPane.showMessageDialog(this, scrollPane,
                        "Resultados de Búsqueda (" + resultados.size() + " encontrados)",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}