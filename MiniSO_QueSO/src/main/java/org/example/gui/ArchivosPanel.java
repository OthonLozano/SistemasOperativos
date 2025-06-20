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

/**
 * Panel de interfaz gráfica para la gestión del sistema de archivos en MiniSO.
 *
 * Esta clase implementa una interfaz gráfica completa para el explorador de archivos
 * que simula el comportamiento de exploradores reales como Windows Explorer,
 * Nautilus (Linux) o Finder (macOS).
 *
 * Componentes principales de la interfaz:
 * - Árbol de directorios navegable (panel izquierdo)
 * - Tabla de contenido del directorio actual (panel superior derecho)
 * - Editor de texto integrado (panel inferior derecho)
 * - Barra de navegación con ruta actual y búsqueda
 * - Panel de estadísticas del sistema
 *
 * Funcionalidades implementadas:
 * - Navegación por directorios mediante árbol y doble clic
 * - Creación y eliminación de archivos y carpetas
 * - Edición de archivos de texto en línea
 * - Búsqueda de archivos por nombre
 * - Visualización de metadatos (tamaño, fecha, permisos)
 */
public class ArchivosPanel extends JPanel {
    /** Árbol jerárquico que muestra la estructura de directorios */
    private JTree arbolDirectorios;

    /** Modelo de datos para el árbol de directorios */
    private DefaultTreeModel modeloArbol;

    /** Nodo raíz del árbol de directorios */
    private DefaultMutableTreeNode nodoRaiz;

    /** Tabla que muestra el contenido del directorio actual */
    private JTable tablaArchivos;

    /** Modelo de datos para la tabla de archivos */
    private DefaultTableModel modeloTabla;

    /** Campo de texto que muestra la ruta actual (solo lectura) */
    private JTextField rutaField;

    /** Campo de texto para introducir patrones de búsqueda */
    private JTextField busquedaField;

    /** Área de texto para editar contenido de archivos */
    private JTextArea editorArea;

    /** Etiqueta que muestra estadísticas del sistema de archivos */
    private JLabel estadisticasLabel;

    /** Botón para crear una nueva carpeta en el directorio actual */
    private JButton nuevaCarpetaBtn;

    /** Botón para crear un nuevo archivo en el directorio actual */
    private JButton nuevoArchivoBtn;

    /** Botón para eliminar el elemento seleccionado */
    private JButton eliminarBtn;

    /** Botón para editar el archivo seleccionado */
    private JButton editarBtn;

    /** Botón para guardar cambios en el archivo editado */
    private JButton guardarBtn;

    /** Botón para ejecutar búsqueda de archivos */
    private JButton buscarBtn;

    /** Botón para navegar rápidamente al directorio home */
    private JButton homeBtn;


    /** Instancia del sistema de archivos virtual */
    private SistemaArchivos sistemaArchivos;

    /** Referencia al archivo que se está editando actualmente */
    private Archivo archivoEditando;

    /**
     * Constructor principal del panel de archivos.
     *
     * Inicializa todos los componentes de la interfaz, configura el layout,
     * establece los manejadores de eventos e inicializa el sistema de archivos.
     */
    public ArchivosPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        initializeSistemaArchivos(); // Inicializar sistema local independiente
        actualizarVista();
    }


    /**
     * Inicializa todos los componentes de la interfaz gráfica.
     *
     * Configura las propiedades básicas de cada componente, incluyendo
     * tamaños, fuentes, modelos de datos y estados iniciales.
     */
    private void initializeComponents() {
        // Inicialización del árbol de directorios
        nodoRaiz = new DefaultMutableTreeNode("📁 /"); // Nodo raíz con icono
        modeloArbol = new DefaultTreeModel(nodoRaiz);
        arbolDirectorios = new JTree(modeloArbol);
        arbolDirectorios.setRootVisible(true); // Mostrar nodo raíz
        arbolDirectorios.setShowsRootHandles(true); // Mostrar controles de expansión

        // Configuración de la tabla de archivos
        String[] columnNames = {"", "Nombre", "Tipo", "Tamaño", "Modificado", "Permisos"};
        modeloTabla = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla de solo lectura
            }
        };
        tablaArchivos = new JTable(modeloTabla);
        tablaArchivos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Solo una fila seleccionable

        // Ajustar anchos de columnas para mejor visualización
        tablaArchivos.getColumnModel().getColumn(0).setMaxWidth(30); // Columna de icono
        tablaArchivos.getColumnModel().getColumn(2).setMaxWidth(80);  // Columna de tipo
        tablaArchivos.getColumnModel().getColumn(3).setMaxWidth(80);  // Columna de tamaño
        tablaArchivos.getColumnModel().getColumn(5).setMaxWidth(80);  // Columna de permisos

        // Configuración de campos de texto
        rutaField = new JTextField();
        rutaField.setEditable(false); // Solo lectura para mostrar ruta actual
        rutaField.setFont(new Font("Consolas", Font.PLAIN, 12)); // Fuente monoespaciada

        busquedaField = new JTextField(20); // Campo de búsqueda con ancho fijo

        // Configuración del editor de texto
        editorArea = new JTextArea(8, 40); // 8 filas, 40 columnas
        editorArea.setFont(new Font("Consolas", Font.PLAIN, 12)); // Fuente monoespaciada
        editorArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding interno

        // Inicialización de botones con iconos emoji
        nuevaCarpetaBtn = new JButton("📁 Nueva Carpeta");
        nuevoArchivoBtn = new JButton("📄 Nuevo Archivo");
        eliminarBtn = new JButton("🗑️ Eliminar");
        editarBtn = new JButton("✏️ Editar");
        guardarBtn = new JButton("💾 Guardar");
        buscarBtn = new JButton("🔍 Buscar");
        homeBtn = new JButton("🏠 Inicio");

        // Configuración de etiquetas
        estadisticasLabel = new JLabel();
        estadisticasLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        // Establecer estados iniciales de los botones
        guardarBtn.setEnabled(false); // Deshabilitado hasta editar un archivo
        editarBtn.setEnabled(false);  // Deshabilitado hasta seleccionar archivo
        eliminarBtn.setEnabled(false); // Deshabilitado hasta seleccionar elemento
    }

    /**
     * Configura el layout principal de la interfaz.
     *
     * Organiza los componentes en una estructura jerárquica usando
     * BorderLayout y JSplitPane para crear una interfaz redimensionable
     * similar a exploradores de archivos modernos.
     */
    private void setupLayout() {
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(10, 10, 10, 10)); // Margen externo

        // Panel superior - Barra de navegación
        JPanel navegacionPanel = createNavegacionPanel();
        add(navegacionPanel, BorderLayout.NORTH);

        // Panel central - División principal horizontal
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Panel izquierdo - Árbol de directorios
        JPanel arbolPanel = createArbolPanel();
        mainSplit.setLeftComponent(arbolPanel);

        // Panel derecho - División vertical
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        // Panel superior derecho - Lista de archivos
        JPanel archivosPanel = createArchivosPanel();
        rightSplit.setTopComponent(archivosPanel);

        // Panel inferior derecho - Editor de texto
        JPanel editorPanel = createEditorPanel();
        rightSplit.setBottomComponent(editorPanel);

        rightSplit.setDividerLocation(300); // Posición inicial del divisor
        mainSplit.setRightComponent(rightSplit);

        mainSplit.setDividerLocation(250); // Posición inicial del divisor principal
        add(mainSplit, BorderLayout.CENTER);

        // Panel inferior - Estadísticas del sistema
        JPanel estadisticasPanel = createEstadisticasPanel();
        add(estadisticasPanel, BorderLayout.SOUTH);
    }

    /**
     * Crea el panel de navegación con ruta actual y búsqueda.
     *
     * @return Panel configurado con controles de navegación
     */
    private JPanel createNavegacionPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new TitledBorder("Navegación"));

        // Panel izquierdo - Visualización de ruta actual
        JPanel rutaPanel = new JPanel(new BorderLayout());
        rutaPanel.add(new JLabel("Ruta: "), BorderLayout.WEST);
        rutaPanel.add(rutaField, BorderLayout.CENTER);

        // Panel derecho - Controles de navegación y búsqueda
        JPanel botonesPanel = new JPanel(new FlowLayout());
        botonesPanel.add(homeBtn);
        botonesPanel.add(busquedaField);
        botonesPanel.add(buscarBtn);

        panel.add(rutaPanel, BorderLayout.CENTER);
        panel.add(botonesPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Crea el panel del árbol de directorios.
     *
     * @return Panel configurado con el árbol navegable
     */
    private JPanel createArbolPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Directorios"));

        JScrollPane scrollPane = new JScrollPane(arbolDirectorios);
        scrollPane.setPreferredSize(new Dimension(250, 400));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea el panel de la tabla de archivos con botones de acción.
     *
     * @return Panel configurado con tabla y controles
     */
    private JPanel createArchivosPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Archivos y Carpetas"));

        // Tabla con scroll para contenido extenso
        JScrollPane scrollPane = new JScrollPane(tablaArchivos);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel de botones de acción
        JPanel botonesPanel = new JPanel(new FlowLayout());
        botonesPanel.add(nuevaCarpetaBtn);
        botonesPanel.add(nuevoArchivoBtn);
        botonesPanel.add(eliminarBtn);
        botonesPanel.add(editarBtn);

        panel.add(botonesPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Crea el panel del editor de texto con controles.
     *
     * @return Panel configurado con editor y botones
     */
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

    /**
     * Crea el panel de estadísticas del sistema.
     *
     * @return Panel configurado para mostrar estadísticas
     */
    private JPanel createEstadisticasPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Estadísticas"));
        panel.add(estadisticasLabel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Configura todos los manejadores de eventos de la interfaz.
     *
     * Establece los listeners para interacciones del usuario como
     * clics, selecciones, y acciones de botones.
     */
    private void setupEventHandlers() {
        // Listener para selección en el árbol de directorios
        arbolDirectorios.addTreeSelectionListener(e -> {
            TreePath path = e.getPath();
            if (path != null) {
                navegarDesdePath(path); // Navegar al directorio seleccionado
            }
        });

        // Listener para doble clic en la tabla de archivos
        tablaArchivos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Detectar doble clic
                    abrirElementoSeleccionado();
                }
            }
        });

        // Listener para cambios de selección en la tabla
        tablaArchivos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Evitar eventos múltiples
                actualizarBotonesSegunSeleccion();
            }
        });

        // Asignación de acciones a botones
        homeBtn.addActionListener(e -> irAHome());
        nuevaCarpetaBtn.addActionListener(e -> crearNuevaCarpeta());
        nuevoArchivoBtn.addActionListener(e -> crearNuevoArchivo());
        eliminarBtn.addActionListener(e -> eliminarElemento());
        editarBtn.addActionListener(e -> editarArchivo());
        guardarBtn.addActionListener(e -> guardarArchivo());
        buscarBtn.addActionListener(e -> buscarArchivos());

        // Permitir búsqueda con tecla Enter
        busquedaField.addActionListener(e -> buscarArchivos());
    }

    /**
     * Inicializa el sistema de archivos virtual.
     *
     * Crea una instancia local del sistema de archivos independiente
     * para simular el comportamiento de un sistema real.
     */
    private void initializeSistemaArchivos() {
        // Crear sistema de archivos local para simulación
        sistemaArchivos = new SistemaArchivos("usuario_local");
        System.out.println("✅ Sistema de archivos local inicializado");
        System.out.println("ℹ️ Simulador independiente de archivos");
    }

    // ======================== MÉTODOS DE ACTUALIZACIÓN DE VISTA ========================

    /**
     * Actualiza todos los componentes de la vista con datos actuales.
     *
     * Sincroniza la interfaz gráfica con el estado actual del sistema
     * de archivos, incluyendo árbol, tabla, ruta y estadísticas.
     */
    private void actualizarVista() {
        actualizarArbolDirectorios();
        actualizarTablaArchivos();
        actualizarRuta();
        actualizarEstadisticas();
    }

    /**
     * Actualiza el árbol de directorios con la estructura actual.
     *
     * Reconstruye completamente el árbol y expande los nodos
     * para mostrar la jerarquía de directorios.
     */
    private void actualizarArbolDirectorios() {
        // Limpiar árbol existente
        nodoRaiz.removeAllChildren();

        // Reconstruir árbol desde la raíz del sistema
        construirArbol(nodoRaiz, sistemaArchivos.getRaiz());

        // Notificar cambios al modelo
        modeloArbol.reload();

        // Expandir todos los nodos para mejor visualización
        for (int i = 0; i < arbolDirectorios.getRowCount(); i++) {
            arbolDirectorios.expandRow(i);
        }
    }

    /**
     * Construye recursivamente el árbol de directorios.
     *
     * @param nodo Nodo padre en el árbol gráfico
     * @param directorio Directorio correspondiente en el sistema
     */
    private void construirArbol(DefaultMutableTreeNode nodo, Directorio directorio) {
        // Agregar cada subdirectorio como nodo hijo
        for (Directorio subdir : directorio.getSubdirectorios()) {
            DefaultMutableTreeNode nodoHijo = new DefaultMutableTreeNode("📁 " + subdir.getNombre());
            nodo.add(nodoHijo);

            // Recursión para subdirectorios anidados
            construirArbol(nodoHijo, subdir);
        }
    }

    /**
     * Actualiza la tabla con el contenido del directorio actual.
     *
     * Muestra directorios y archivos con sus metadatos correspondientes
     * (tipo, tamaño, fecha de modificación, permisos).
     */
    private void actualizarTablaArchivos() {
        // Limpiar contenido existente
        modeloTabla.setRowCount(0);

        Directorio dirActual = sistemaArchivos.getDirectorioActual();

        // Agregar directorios al modelo de tabla
        for (Directorio subdir : dirActual.getSubdirectorios()) {
            Object[] rowData = {
                    "📁", // Icono de carpeta
                    subdir.getNombre(),
                    "Carpeta",
                    subdir.getTamañoFormateado(),
                    subdir.getFechaModificacion().toLocalDate().toString(),
                    subdir.getPermisos().toString()
            };
            modeloTabla.addRow(rowData);
        }

        // Agregar archivos al modelo de tabla
        for (Archivo archivo : dirActual.getArchivos()) {
            Object[] rowData = {
                    archivo.getIcono(), // Icono según tipo de archivo
                    archivo.getNombre(),
                    archivo.getTipo().toString(),
                    archivo.getTamañoFormateado(),
                    archivo.getFechaModificacion().toLocalDate().toString(),
                    archivo.getPermisos().toString()
            };
            modeloTabla.addRow(rowData);
        }
    }

    /**
     * Actualiza el campo de ruta con la ubicación actual.
     */
    private void actualizarRuta() {
        rutaField.setText(sistemaArchivos.getRutaActual());
    }

    /**
     * Actualiza las estadísticas del sistema mostradas en el panel inferior.
     */
    private void actualizarEstadisticas() {
        estadisticasLabel.setText(sistemaArchivos.getEstadisticas());
    }

    /**
     * Actualiza el estado de los botones según la selección actual.
     *
     * Habilita o deshabilita botones dependiendo de qué elemento
     * está seleccionado y qué operaciones son válidas para ese elemento.
     */
    private void actualizarBotonesSegunSeleccion() {
        int filaSeleccionada = tablaArchivos.getSelectedRow();
        boolean haySeleccion = filaSeleccionada >= 0;

        // El botón eliminar siempre está disponible si hay selección
        eliminarBtn.setEnabled(haySeleccion);

        if (haySeleccion) {
            String nombre = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
            String tipo = (String) modeloTabla.getValueAt(filaSeleccionada, 2);

            // Habilitar edición solo para archivos de texto
            Archivo archivo = sistemaArchivos.getDirectorioActual().getArchivo(nombre);
            editarBtn.setEnabled(archivo != null &&
                    (archivo.getTipo() == Archivo.TipoArchivo.TEXTO ||
                            archivo.getTipo() == Archivo.TipoArchivo.OTRO));
        } else {
            editarBtn.setEnabled(false);
        }
    }

    /**
     * Navega a un directorio basado en la selección del árbol.
     *
     * @param path Ruta del árbol seleccionada por el usuario
     */
    private void navegarDesdePath(TreePath path) {
        StringBuilder ruta = new StringBuilder("/");

        // Construir ruta desde el path del árbol (omitir nodo raíz)
        for (int i = 1; i < path.getPathCount(); i++) {
            String nombre = path.getPathComponent(i).toString();
            // Remover emoji del nombre (primeros 2 caracteres)
            nombre = nombre.substring(2);
            ruta.append(nombre);
            if (i < path.getPathCount() - 1) {
                ruta.append("/");
            }
        }

        // Cambiar al directorio y actualizar vista si es exitoso
        if (sistemaArchivos.cambiarDirectorio(ruta.toString())) {
            actualizarTablaArchivos();
            actualizarRuta();
            actualizarEstadisticas();
        }
    }

    /**
     * Abre el elemento seleccionado (directorio o archivo).
     *
     * Si es un directorio, navega hacia él.
     * Si es un archivo, lo abre para edición.
     */
    private void abrirElementoSeleccionado() {
        int filaSeleccionada = tablaArchivos.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String nombre = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
            String tipo = (String) modeloTabla.getValueAt(filaSeleccionada, 2);

            if (tipo.equals("Carpeta")) {
                // Navegar al directorio seleccionado
                if (sistemaArchivos.cambiarDirectorio(nombre)) {
                    actualizarVista();
                }
            } else {
                // Abrir archivo para edición
                editarArchivo();
            }
        }
    }

    /**
     * Navega rápidamente al directorio home del usuario.
     */
    private void irAHome() {
        if (sistemaArchivos.cambiarDirectorio("~")) {
            actualizarVista();
        }
    }

    /**
     * Muestra diálogo para crear una nueva carpeta y la crea.
     *
     * Solicita al usuario el nombre de la carpeta y maneja
     * validaciones y errores durante la creación.
     */
    private void crearNuevaCarpeta() {
        String nombre = JOptionPane.showInputDialog(this,
                "Nombre de la nueva carpeta:",
                "Nueva Carpeta",
                JOptionPane.QUESTION_MESSAGE);

        if (nombre != null && !nombre.trim().isEmpty()) {
            try {
                // Crear carpeta en el sistema de archivos
                sistemaArchivos.crearDirectorio(nombre.trim());
                actualizarVista();

                // Mostrar confirmación al usuario
                JOptionPane.showMessageDialog(this,
                        "✅ Carpeta '" + nombre + "' creada exitosamente",
                        "Carpeta Creada",
                        JOptionPane.INFORMATION_MESSAGE);

                System.out.println("📁 Carpeta creada: " + nombre);

            } catch (Exception e) {
                // Mostrar error si la creación falla
                JOptionPane.showMessageDialog(this,
                        "❌ Error creando carpeta: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    /**
     * Muestra diálogo para crear un nuevo archivo y lo crea con contenido inicial.
     *
     * Solicita el nombre del archivo y crea un archivo con contenido
     * predeterminado que incluye información del sistema.
     */
    private void crearNuevoArchivo() {
        String nombre = JOptionPane.showInputDialog(this,
                "Nombre del nuevo archivo (incluye extensión):",
                "Nuevo Archivo",
                JOptionPane.QUESTION_MESSAGE);

        if (nombre != null && !nombre.trim().isEmpty()) {
            try {
                // Crear contenido inicial descriptivo
                String contenidoInicial = "# Archivo creado en MiniSO\n" +
                        "# Simulador de Sistema de Archivos\n" +
                        "# Fecha: " + java.time.LocalDateTime.now() + "\n\n" +
                        "Contenido del archivo...\n";

                // Crear archivo en el sistema
                Archivo archivo = sistemaArchivos.crearArchivo(nombre.trim(), contenidoInicial);

                actualizarVista();

                // Mostrar confirmación
                JOptionPane.showMessageDialog(this,
                        "✅ Archivo '" + nombre + "' creado exitosamente",
                        "Archivo Creado",
                        JOptionPane.INFORMATION_MESSAGE);

                System.out.println("📄 Archivo creado: " + nombre);

                // Seleccionar automáticamente el archivo recién creado
                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    if (modeloTabla.getValueAt(i, 1).equals(nombre.trim())) {
                        tablaArchivos.setRowSelectionInterval(i, i);
                        break;
                    }
                }

            } catch (Exception e) {
                // Mostrar error si la creación falla
                JOptionPane.showMessageDialog(this,
                        "❌ Error creando archivo: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    /**
     * Elimina el elemento seleccionado después de confirmación del usuario.
     *
     * Muestra un diálogo de confirmación y procede con la eliminación
     * si el usuario confirma la acción.
     */
    private void eliminarElemento() {
        int filaSeleccionada = tablaArchivos.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String nombre = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
            String tipo = (String) modeloTabla.getValueAt(filaSeleccionada, 2);

            // Solicitar confirmación del usuario
            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de eliminar '" + nombre + "'?",
                    "Confirmar Eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirmacion == JOptionPane.YES_OPTION) {
                try {
                    boolean eliminado;

                    // Eliminar según el tipo de elemento
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
                    // Mostrar error si la eliminación falla
                    JOptionPane.showMessageDialog(this,
                            "❌ Error eliminando: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Abre un archivo para edición en el editor integrado.
     *
     * Carga el contenido del archivo seleccionado en el área de texto
     * y habilita el botón de guardado para permitir modificaciones.
     */
    private void editarArchivo() {
        int filaSeleccionada = tablaArchivos.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String nombre = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
            archivoEditando = sistemaArchivos.abrirArchivo(nombre);

            if (archivoEditando != null) {
                try {
                    // Cargar contenido del archivo en el editor
                    String contenido = archivoEditando.leerContenido();
                    editorArea.setText(contenido);
                    editorArea.setCaretPosition(0); // Posicionar cursor al inicio
                    guardarBtn.setEnabled(true); // Habilitar guardado

                    // Actualizar etiqueta con nombre del archivo editado
                    Component[] components = ((JPanel) ((JPanel) editorArea.getParent().getParent()).getComponent(1)).getComponents();
                    for (Component comp : components) {
                        if (comp instanceof JLabel && ((JLabel) comp).getText().startsWith("Ninguno")) {
                            ((JLabel) comp).setText(archivoEditando.getNombre());
                            break;
                        }
                    }

                } catch (Exception e) {
                    // Silenciar errores de lectura para mantener estabilidad de UI
                    // En una implementación completa se podría mostrar mensaje de error
                }
            }
        }
    }

    /**
     * Guarda los cambios realizados en el archivo editado.
     *
     * Toma el contenido actual del editor y lo escribe al archivo,
     * actualizando la vista y mostrando confirmación al usuario.
     */
    private void guardarArchivo() {
        if (archivoEditando != null) {
            try {
                // Obtener contenido del editor y guardarlo
                String nuevoContenido = editorArea.getText();
                archivoEditando.modificarContenido(nuevoContenido);

                // Actualizar vista para reflejar cambios
                actualizarVista();

                // Mostrar confirmación de guardado exitoso
                JOptionPane.showMessageDialog(this,
                        "✅ Archivo guardado exitosamente",
                        "Guardado",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                // Mostrar error si el guardado falla
                JOptionPane.showMessageDialog(this,
                        "❌ Error guardando archivo: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Ejecuta búsqueda de archivos por nombre y muestra resultados.
     *
     * Utiliza el patrón ingresado en el campo de búsqueda para encontrar
     * archivos que coincidan y presenta los resultados en un diálogo.
     */
    private void buscarArchivos() {
        String patron = busquedaField.getText().trim();
        if (!patron.isEmpty()) {
            // Realizar búsqueda en todo el sistema de archivos
            List<Archivo> resultados = sistemaArchivos.buscarArchivos(patron);

            if (resultados.isEmpty()) {
                // Mostrar mensaje si no se encontraron resultados
                JOptionPane.showMessageDialog(this,
                        "No se encontraron archivos que coincidan con: " + patron,
                        "Búsqueda",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Construir mensaje con resultados encontrados
                StringBuilder mensaje = new StringBuilder("Archivos encontrados:\n\n");
                for (Archivo archivo : resultados) {
                    mensaje.append("📄 ").append(archivo.getRutaCompleta()).append("\n");
                }

                // Crear área de texto para mostrar resultados
                JTextArea textArea = new JTextArea(mensaje.toString());
                textArea.setEditable(false);
                textArea.setRows(10);
                textArea.setColumns(50);

                // Mostrar resultados en diálogo con scroll
                JScrollPane scrollPane = new JScrollPane(textArea);
                JOptionPane.showMessageDialog(this, scrollPane,
                        "Resultados de Búsqueda (" + resultados.size() + " encontrados)",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}