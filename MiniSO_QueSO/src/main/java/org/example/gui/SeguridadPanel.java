package org.example.gui;

import org.example.core.SeguridadManager;
import org.example.models.Usuario;
import org.example.models.Usuario.TipoUsuario;
import org.example.models.Usuario.EstadoUsuario;
import org.example.models.Usuario.Permiso;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Panel de gestión de seguridad y usuarios del sistema MiniSO
 *
 * Esta clase implementa la interfaz gráfica para el módulo de seguridad,
 * proporcionando funcionalidades completas de:
 * - Autenticación de usuarios (login/logout)
 * - Administración de usuarios (crear, modificar, eliminar)
 * - Gestión de permisos y roles
 * - Auditoría y logs de seguridad
 * - Control de acceso basado en privilegios
 *
 * El panel se adapta dinámicamente según el tipo de usuario autenticado,
 * mostrando opciones administrativas solo a usuarios con privilegios suficientes.
 */
public class SeguridadPanel extends JPanel {

    // === COMPONENTES DE AUTENTICACIÓN ===

    /** Campo de texto para ingresar nombre de usuario */
    private JTextField usuarioField;

    /** Campo de contraseña para autenticación segura */
    private JPasswordField contraseñaField;

    /** Botón para iniciar sesión en el sistema */
    private JButton loginBtn;

    /** Botón para cerrar la sesión activa */
    private JButton logoutBtn;

    /** Label que muestra el estado actual de la sesión */
    private JLabel estadoSesionLabel;

    /** Label que muestra información del usuario actualmente autenticado */
    private JLabel usuarioActualLabel;

    // === PANEL DE ADMINISTRACIÓN (SOLO ADMINS) ===

    /** Panel principal de administración, visible solo para administradores */
    private JPanel adminPanel;

    /** Tabla que muestra todos los usuarios del sistema */
    private JTable usuariosTable;

    /** Modelo de datos para la tabla de usuarios */
    private DefaultTableModel modeloTablaUsuarios;

    /** Área de texto para mostrar logs del sistema */
    private JTextArea logArea;

    /** Label que muestra estadísticas de seguridad */
    private JLabel estadisticasLabel;

    // === BOTONES DE ADMINISTRACIÓN DE USUARIOS ===

    /** Botón para crear nuevos usuarios en el sistema */
    private JButton crearUsuarioBtn;

    /** Botón para modificar información de usuarios existentes */
    private JButton modificarUsuarioBtn;

    /** Botón para eliminar usuarios del sistema */
    private JButton eliminarUsuarioBtn;

    /** Botón para cambiar el estado de un usuario (activo/inactivo/bloqueado) */
    private JButton cambiarEstadoBtn;

    /** Botón para restablecer contraseñas de usuarios */
    private JButton resetPasswordBtn;

    /** Botón para ver el perfil completo de un usuario */
    private JButton verPerfilBtn;

    // === LÓGICA DE SEGURIDAD ===

    /** Gestor principal de seguridad que maneja toda la lógica de autenticación y autorización */
    private SeguridadManager seguridadManager;

    /**
     * Constructor principal del panel de seguridad
     *
     * Inicializa todos los componentes de la interfaz, configura el layout,
     * establece los manejadores de eventos y crea el gestor de seguridad.
     * Al finalizar, actualiza la vista para reflejar el estado inicial.
     */
    public SeguridadPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        initializeSeguridadManager();
        actualizarVista();
    }

    /**
     * Inicializa todos los componentes de la interfaz gráfica
     *
     * Configura los componentes con sus valores por defecto, dimensiones apropiadas
     * y estados iniciales. Establece la apariencia visual y comportamientos básicos.
     */
    private void initializeComponents() {
        // === CONFIGURACIÓN DE COMPONENTES DE LOGIN ===
        usuarioField = new JTextField(15);
        contraseñaField = new JPasswordField(15);
        loginBtn = new JButton("🔐 Iniciar Sesión");
        logoutBtn = new JButton("🚪 Cerrar Sesión");

        // === CONFIGURACIÓN DE LABELS DE ESTADO ===
        estadoSesionLabel = new JLabel("❌ Sin sesión activa");
        estadoSesionLabel.setFont(new Font("Arial", Font.BOLD, 12));

        usuarioActualLabel = new JLabel("Usuario: Ninguno");
        usuarioActualLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        // === INICIALIZACIÓN DEL PANEL ADMINISTRATIVO ===
        adminPanel = new JPanel(); // Inicialmente vacío, se configurará después

        // === CONFIGURACIÓN DE TABLA DE USUARIOS ===
        String[] columnNames = {"", "Usuario", "Nombre", "Tipo", "Estado", "Último Acceso"};
        modeloTablaUsuarios = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla de solo lectura para prevenir ediciones accidentales
            }
        };
        usuariosTable = new JTable(modeloTablaUsuarios);
        usuariosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Ajustar anchos de columnas para mejor visualización
        usuariosTable.getColumnModel().getColumn(0).setMaxWidth(30);  // Columna de icono
        usuariosTable.getColumnModel().getColumn(3).setMaxWidth(100); // Tipo de usuario
        usuariosTable.getColumnModel().getColumn(4).setMaxWidth(80);  // Estado

        // === CONFIGURACIÓN DEL ÁREA DE LOG ===
        logArea = new JTextArea(8, 50);
        logArea.setEditable(false); // Solo lectura
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11)); // Fuente monoespaciada
        logArea.setBackground(new Color(248, 248, 248)); // Fondo gris claro

        // === CONFIGURACIÓN DE BOTONES ADMINISTRATIVOS ===
        crearUsuarioBtn = new JButton("👤➕ Crear Usuario");
        modificarUsuarioBtn = new JButton("✏️ Modificar");
        eliminarUsuarioBtn = new JButton("🗑️ Eliminar");
        cambiarEstadoBtn = new JButton("🔄 Cambiar Estado");
        resetPasswordBtn = new JButton("🔑 Reset Password");
        verPerfilBtn = new JButton("📋 Ver Perfil");

        // === CONFIGURACIÓN DE ESTADÍSTICAS ===
        estadisticasLabel = new JLabel();
        estadisticasLabel.setFont(new Font("Arial", Font.BOLD, 11));

        // === ESTADOS INICIALES DE LA INTERFAZ ===
        logoutBtn.setEnabled(false);      // Deshabilitado hasta que haya sesión activa
        adminPanel.setVisible(false);     // Oculto hasta que se autentique un admin
    }

    /**
     * Configura el layout principal del panel
     *
     * Organiza los componentes en una estructura jerárquica usando BorderLayout.
     * El panel se divide en tres secciones principales: autenticación (norte),
     * administración (centro) y estadísticas (sur).
     */
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10)); // Margen externo uniforme

        // === PANEL SUPERIOR: AUTENTICACIÓN ===
        JPanel loginPanel = createLoginPanel();
        add(loginPanel, BorderLayout.NORTH);

        // === PANEL CENTRAL: ADMINISTRACIÓN ===
        setupAdminPanel(); // Configurar panel administrativo
        add(adminPanel, BorderLayout.CENTER);

        // === PANEL INFERIOR: ESTADÍSTICAS ===
        JPanel estadisticasPanel = createEstadisticasPanel();
        add(estadisticasPanel, BorderLayout.SOUTH);
    }

    /**
     * Crea el panel de autenticación
     *
     * Contiene los campos de login, botones de sesión y visualización
     * del estado actual de autenticación.
     *
     * @return JPanel configurado con componentes de autenticación
     */
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Autenticación"));

        // === SUBPANEL DE CAMPOS DE LOGIN ===
        JPanel camposPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        camposPanel.add(new JLabel("Usuario:"));
        camposPanel.add(usuarioField);
        camposPanel.add(new JLabel("Contraseña:"));
        camposPanel.add(contraseñaField);
        camposPanel.add(loginBtn);
        camposPanel.add(logoutBtn);

        // === SUBPANEL DE ESTADO DE SESIÓN ===
        JPanel estadoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        estadoPanel.add(estadoSesionLabel);
        estadoPanel.add(Box.createHorizontalStrut(20)); // Espaciador
        estadoPanel.add(usuarioActualLabel);

        // Ensamblar panel completo
        panel.add(camposPanel, BorderLayout.NORTH);
        panel.add(estadoPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Configura el panel administrativo principal
     *
     * Establece un layout complejo con divisiones horizontales para mostrar
     * la tabla de usuarios, logs del sistema y botones de administración.
     * Este panel solo es visible para usuarios con privilegios administrativos.
     */
    private void setupAdminPanel() {
        adminPanel.setLayout(new BorderLayout(10, 10));
        adminPanel.setBorder(new TitledBorder("Administración de Usuarios"));

        // === DIVISIÓN CENTRAL HORIZONTAL ===
        JSplitPane centralSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Panel izquierdo: Gestión de usuarios
        JPanel usuariosPanel = createUsuariosPanel();
        centralSplit.setLeftComponent(usuariosPanel);

        // Panel derecho: Log del sistema
        JPanel logPanel = createLogPanel();
        centralSplit.setRightComponent(logPanel);

        centralSplit.setDividerLocation(500); // Posición inicial del divisor
        adminPanel.add(centralSplit, BorderLayout.CENTER);

        // === PANEL INFERIOR: BOTONES DE ADMINISTRACIÓN ===
        JPanel botonesPanel = createBotonesAdminPanel();
        adminPanel.add(botonesPanel, BorderLayout.SOUTH);
    }

    /**
     * Crea el panel de gestión de usuarios
     *
     * Contiene la tabla que muestra todos los usuarios del sistema
     * con información relevante como estado, tipo y último acceso.
     *
     * @return JPanel con la tabla de usuarios
     */
    private JPanel createUsuariosPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Usuarios del Sistema"));

        // Tabla con scroll para manejar muchos usuarios
        JScrollPane scrollPane = new JScrollPane(usuariosTable);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Crea el panel del log del sistema
     *
     * Muestra los eventos de seguridad recientes y proporciona
     * un botón para actualizar la información.
     *
     * @return JPanel con el área de log y controles
     */
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Log del Sistema"));

        // Área de log con scroll automático
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane, BorderLayout.CENTER);

        // === BOTÓN DE ACTUALIZACIÓN ===
        JButton actualizarLogBtn = new JButton("🔄 Actualizar Log");
        actualizarLogBtn.addActionListener(e -> actualizarLog());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(actualizarLogBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Crea el panel de botones administrativos
     *
     * Organiza todos los botones de gestión de usuarios en un layout
     * horizontal para fácil acceso.
     *
     * @return JPanel con botones de administración
     */
    private JPanel createBotonesAdminPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        // Agregar todos los botones administrativos
        panel.add(crearUsuarioBtn);
        panel.add(modificarUsuarioBtn);
        panel.add(eliminarUsuarioBtn);
        panel.add(cambiarEstadoBtn);
        panel.add(resetPasswordBtn);
        panel.add(verPerfilBtn);

        return panel;
    }

    /**
     * Crea el panel de estadísticas de seguridad
     *
     * Muestra métricas importantes del sistema de seguridad como
     * número de usuarios, intentos de login fallidos, etc.
     *
     * @return JPanel con estadísticas del sistema
     */
    private JPanel createEstadisticasPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Estadísticas de Seguridad"));
        panel.add(estadisticasLabel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Configura todos los manejadores de eventos de la interfaz
     *
     * Establece los listeners para botones, campos de texto y eventos
     * de selección en tablas. Incluye atajos de teclado y validaciones.
     */
    private void setupEventHandlers() {
        // === MANEJADORES DE AUTENTICACIÓN ===
        loginBtn.addActionListener(e -> iniciarSesion());
        logoutBtn.addActionListener(e -> cerrarSesion());

        // === ATAJOS DE TECLADO PARA LOGIN ===
        // Enter en campo usuario pasa a contraseña
        usuarioField.addActionListener(e -> contraseñaField.requestFocus());
        // Enter en contraseña ejecuta login
        contraseñaField.addActionListener(e -> iniciarSesion());

        // === MANEJADOR DE SELECCIÓN EN TABLA ===
        usuariosTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarBotonesAdmin(); // Habilitar/deshabilitar botones según selección
            }
        });

        // === MANEJADORES DE BOTONES ADMINISTRATIVOS ===
        crearUsuarioBtn.addActionListener(e -> mostrarDialogoCrearUsuario());
        modificarUsuarioBtn.addActionListener(e -> modificarUsuarioSeleccionado());
        eliminarUsuarioBtn.addActionListener(e -> eliminarUsuarioSeleccionado());
        cambiarEstadoBtn.addActionListener(e -> cambiarEstadoUsuario());
        resetPasswordBtn.addActionListener(e -> resetearPassword());
        verPerfilBtn.addActionListener(e -> verPerfilUsuario());
    }

    /**
     * Inicializa el gestor de seguridad del sistema
     *
     * Crea una nueva instancia del SeguridadManager que manejará
     * toda la lógica de autenticación, autorización y auditoría.
     */
    private void initializeSeguridadManager() {
        seguridadManager = new SeguridadManager();
    }

    /**
     * Actualiza toda la vista del panel según el estado actual
     *
     * Método principal que coordina la actualización de todos los
     * componentes visuales para reflejar el estado actual del sistema.
     */
    private void actualizarVista() {
        actualizarEstadoSesion();       // Estado de login/logout
        actualizarTablaUsuarios();      // Lista de usuarios
        actualizarEstadisticas();       // Métricas del sistema
        actualizarVisibilidadAdmin();   // Mostrar/ocultar panel admin
    }

    /**
     * Actualiza la visualización del estado de sesión
     *
     * Modifica los componentes de autenticación para reflejar si hay
     * una sesión activa o no, y habilita/deshabilita controles apropiadamente.
     */
    private void actualizarEstadoSesion() {
        if (seguridadManager.haySesionActiva()) {
            // === ESTADO: SESIÓN ACTIVA ===
            Usuario usuario = seguridadManager.getUsuarioActual();

            // Actualizar indicadores visuales
            estadoSesionLabel.setText("✅ Sesión activa");
            estadoSesionLabel.setForeground(new Color(0, 128, 0)); // Verde
            usuarioActualLabel.setText("Usuario: " + usuario.getIcono() + " " +
                    usuario.getNombreUsuario() + " (" + usuario.getTipo().getCodigo() + ")");

            // Configurar botones y campos
            loginBtn.setEnabled(false);        // Deshabilitar login
            logoutBtn.setEnabled(true);        // Habilitar logout
            usuarioField.setEnabled(false);    // Deshabilitar campos
            contraseñaField.setEnabled(false);
        } else {
            // === ESTADO: SIN SESIÓN ===

            // Actualizar indicadores visuales
            estadoSesionLabel.setText("❌ Sin sesión activa");
            estadoSesionLabel.setForeground(Color.RED);
            usuarioActualLabel.setText("Usuario: Ninguno");

            // Configurar botones y campos
            loginBtn.setEnabled(true);         // Habilitar login
            logoutBtn.setEnabled(false);       // Deshabilitar logout
            usuarioField.setEnabled(true);     // Habilitar campos
            contraseñaField.setEnabled(true);
        }
    }

    /**
     * Actualiza la tabla de usuarios del sistema
     *
     * Carga la lista actualizada de usuarios desde el gestor de seguridad
     * y la muestra en la tabla, pero solo si el usuario actual tiene permisos suficientes.
     */
    private void actualizarTablaUsuarios() {
        modeloTablaUsuarios.setRowCount(0); // Limpiar tabla existente

        // Verificar permisos antes de cargar datos
        if (seguridadManager.haySesionActiva() &&
                seguridadManager.verificarPermiso(Permiso.VER_LOGS_SISTEMA)) {

            try {
                List<Usuario> usuarios = seguridadManager.getUsuarios();

                // Agregar cada usuario como fila en la tabla
                for (Usuario usuario : usuarios) {
                    Object[] rowData = {
                            usuario.getEstadoIcono(),                    // Icono de estado
                            usuario.getNombreUsuario(),                  // Nombre de usuario
                            usuario.getNombre(),                         // Nombre completo
                            usuario.getTipo().getCodigo(),              // Tipo de usuario
                            usuario.getEstado().toString(),             // Estado actual
                            usuario.getUltimoAcceso() != null ?         // Último acceso
                                    usuario.getUltimoAcceso().toLocalDate().toString() : "Nunca"
                    };
                    modeloTablaUsuarios.addRow(rowData);
                }
            } catch (SecurityException e) {
                // Usuario sin permisos suficientes - tabla permanece vacía
                // No mostrar error para mantener seguridad
            }
        }
    }

    /**
     * Actualiza las estadísticas de seguridad mostradas
     *
     * Obtiene y muestra métricas importantes del sistema de seguridad,
     * pero solo para usuarios con privilegios administrativos.
     */
    private void actualizarEstadisticas() {
        if (seguridadManager.haySesionActiva() &&
                seguridadManager.getUsuarioActual().esAdministrador()) {

            try {
                // Obtener estadísticas del gestor de seguridad
                estadisticasLabel.setText(seguridadManager.getEstadisticasSeguridad());
            } catch (SecurityException e) {
                estadisticasLabel.setText("Sin permisos para ver estadísticas");
            }
        } else {
            // Usuario no es administrador o no hay sesión activa
            estadisticasLabel.setText("Inicie sesión como administrador para ver estadísticas");
        }
    }

    /**
     * Controla la visibilidad del panel administrativo
     *
     * Muestra u oculta las funciones administrativas según los privilegios
     * del usuario actual. Solo los administradores pueden ver estas opciones.
     */
    private void actualizarVisibilidadAdmin() {
        // Determinar si el usuario actual es administrador
        boolean esAdmin = seguridadManager.haySesionActiva() &&
                seguridadManager.getUsuarioActual().esAdministrador();

        // Mostrar/ocultar panel administrativo
        adminPanel.setVisible(esAdmin);

        if (esAdmin) {
            // Usuario es admin: actualizar contenido administrativo
            actualizarBotonesAdmin();
            actualizarLog();
        }

        // Forzar redibujado de la interfaz
        revalidate();
        repaint();
    }

    /**
     * Actualiza el estado de los botones administrativos
     *
     * Habilita o deshabilita botones según la selección en la tabla
     * y aplica reglas de seguridad para proteger usuarios críticos.
     */
    private void actualizarBotonesAdmin() {
        int filaSeleccionada = usuariosTable.getSelectedRow();
        boolean haySeleccion = filaSeleccionada >= 0;

        // Estado básico basado en selección
        modificarUsuarioBtn.setEnabled(haySeleccion);
        eliminarUsuarioBtn.setEnabled(haySeleccion);
        cambiarEstadoBtn.setEnabled(haySeleccion);
        resetPasswordBtn.setEnabled(haySeleccion);
        verPerfilBtn.setEnabled(haySeleccion);

        // Aplicar reglas de seguridad especiales
        if (haySeleccion) {
            String nombreUsuario = (String) modeloTablaUsuarios.getValueAt(filaSeleccionada, 1);
            boolean esUsuarioActual = seguridadManager.getUsuarioActual().getNombreUsuario().equals(nombreUsuario);
            boolean esAdminPrincipal = "admin".equals(nombreUsuario);

            // No permitir eliminar el usuario actual o el admin principal
            eliminarUsuarioBtn.setEnabled(!esUsuarioActual && !esAdminPrincipal);
        }
    }

    /**
     * Actualiza el contenido del log del sistema
     *
     * Carga y muestra los eventos de seguridad más recientes,
     * pero solo si el usuario tiene permisos suficientes.
     */
    private void actualizarLog() {
        if (seguridadManager.haySesionActiva() &&
                seguridadManager.verificarPermiso(Permiso.VER_LOGS_SISTEMA)) {

            try {
                // Obtener logs recientes (últimas 20 entradas)
                List<String> logReciente = seguridadManager.getLogReciente(20);
                StringBuilder sb = new StringBuilder();

                // Construir texto del log
                for (String entrada : logReciente) {
                    sb.append(entrada).append("\n");
                }

                // Actualizar área de texto y hacer scroll al final
                logArea.setText(sb.toString());
                logArea.setCaretPosition(logArea.getDocument().getLength());

            } catch (SecurityException e) {
                logArea.setText("Sin permisos para ver el log del sistema");
            }
        } else {
            logArea.setText("Inicie sesión como administrador para ver el log");
        }
    }

    /**
     * Procesa el intento de inicio de sesión
     *
     * Valida las credenciales ingresadas, maneja errores de autenticación
     * y actualiza la interfaz según el resultado del login.
     */
    private void iniciarSesion() {
        // Obtener credenciales ingresadas
        String usuario = usuarioField.getText().trim();
        String contraseña = new String(contraseñaField.getPassword());

        // Validación básica de campos
        if (usuario.isEmpty() || contraseña.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "❌ Ingrese usuario y contraseña",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Intentar autenticación
        boolean exitoso = seguridadManager.iniciarSesion(usuario, contraseña);

        if (exitoso) {
            // === LOGIN EXITOSO ===
            JOptionPane.showMessageDialog(this,
                    "✅ Inicio de sesión exitoso",
                    "Bienvenido",
                    JOptionPane.INFORMATION_MESSAGE);

            // Limpiar campos sensibles
            usuarioField.setText("");
            contraseñaField.setText("");

            // Actualizar toda la vista
            actualizarVista();
        } else {
            // === LOGIN FALLIDO ===
            JOptionPane.showMessageDialog(this,
                    "❌ Usuario o contraseña incorrectos",
                    "Error de Autenticación",
                    JOptionPane.ERROR_MESSAGE);

            // Limpiar solo la contraseña y enfocar para reintentar
            contraseñaField.setText("");
            contraseñaField.requestFocus();
        }
    }

    /**
     * Procesa el cierre de sesión
     *
     * Solicita confirmación al usuario y procede a cerrar la sesión
     * actual, actualizando la interfaz apropiadamente.
     */
    private void cerrarSesion() {
        // Solicitar confirmación
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de cerrar la sesión?",
                "Confirmar Cierre de Sesión",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            // Ejecutar cierre de sesión
            seguridadManager.cerrarSesion();
            actualizarVista();

            // Notificar éxito
            JOptionPane.showMessageDialog(this,
                    "✅ Sesión cerrada exitosamente",
                    "Sesión Cerrada",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Muestra el diálogo para crear un nuevo usuario
     *
     * Presenta un formulario modal completo para ingresar todos los datos
     * necesarios para crear un nuevo usuario en el sistema.
     */
    private void mostrarDialogoCrearUsuario() {
        // Crear diálogo modal
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Crear Nuevo Usuario", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        // Panel principal con GridBagLayout para control preciso
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // === CAMPOS DEL FORMULARIO ===
        JTextField nombreUsuarioField = new JTextField(15);
        JPasswordField nuevaContraseñaField = new JPasswordField(15);
        JTextField nombreField = new JTextField(15);
        JTextField emailField = new JTextField(15);
        JComboBox<TipoUsuario> tipoCombo = new JComboBox<>(TipoUsuario.values());

        // === LAYOUT DEL FORMULARIO ===
        // Nombre de usuario
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Nombre de usuario:"), gbc);
        gbc.gridx = 1;
        panel.add(nombreUsuarioField, gbc);

        // Contraseña
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        panel.add(nuevaContraseñaField, gbc);

        // Nombre completo
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Nombre completo:"), gbc);
        gbc.gridx = 1;
        panel.add(nombreField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        // Tipo de usuario
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Tipo de usuario:"), gbc);
        gbc.gridx = 1;
        panel.add(tipoCombo, gbc);

        // === PANEL DE BOTONES ===
        JPanel buttonPanel = new JPanel();
        JButton crearButton = new JButton("Crear");
        JButton cancelarButton = new JButton("Cancelar");

        // Manejador del botón Crear
        crearButton.addActionListener(e -> {
            String nombreUsuario = nombreUsuarioField.getText().trim();
            String contraseña = new String(nuevaContraseñaField.getPassword());
            String nombre = nombreField.getText().trim();
            String email = emailField.getText().trim();
            TipoUsuario tipo = (TipoUsuario) tipoCombo.getSelectedItem();

            // Validación de campos obligatorios
            if (nombreUsuario.isEmpty() || contraseña.isEmpty() || nombre.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "❌ Complete todos los campos obligatorios",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Intentar crear el usuario
                boolean creado = seguridadManager.crearUsuario(nombreUsuario, contraseña, nombre, tipo);

                if (creado) {
                    // Configurar email si se proporcionó
                    if (!email.isEmpty()) {
                        seguridadManager.modificarUsuario(nombreUsuario, null, email);
                    }

                    JOptionPane.showMessageDialog(dialog,
                            "✅ Usuario creado exitosamente",
                            "Usuario Creado",
                            JOptionPane.INFORMATION_MESSAGE);

                    actualizarVista();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "❌ Error creando usuario. Verifique que el nombre no exista",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "❌ Error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Manejador del botón Cancelar
        cancelarButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(crearButton);
        buttonPanel.add(cancelarButton);

        // Agregar panel de botones al formulario
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    /**
     * Modifica el usuario seleccionado en la tabla
     *
     * Obtiene el usuario seleccionado y muestra el diálogo de modificación
     * correspondiente, verificando permisos antes de proceder.
     */
    private void modificarUsuarioSeleccionado() {
        int filaSeleccionada = usuariosTable.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String nombreUsuario = (String) modeloTablaUsuarios.getValueAt(filaSeleccionada, 1);

            try {
                Usuario usuario = seguridadManager.getUsuario(nombreUsuario);
                if (usuario != null) {
                    mostrarDialogoModificarUsuario(usuario);
                }
            } catch (SecurityException e) {
                JOptionPane.showMessageDialog(this,
                        "❌ Sin permisos para modificar usuarios",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Muestra el diálogo para modificar un usuario existente
     *
     * Presenta un formulario pre-poblado con los datos actuales del usuario
     * y permite modificar campos editables como nombre, email y tipo.
     *
     * @param usuario Usuario a modificar
     */
    private void mostrarDialogoModificarUsuario(Usuario usuario) {
        // Crear diálogo modal
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Modificar Usuario", true);
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // === CAMPOS EDITABLES PRE-POBLADOS ===
        JTextField nombreField = new JTextField(usuario.getNombre(), 15);
        JTextField emailField = new JTextField(usuario.getEmail(), 15);
        JComboBox<TipoUsuario> tipoCombo = new JComboBox<>(TipoUsuario.values());
        tipoCombo.setSelectedItem(usuario.getTipo());

        // === LAYOUT DEL FORMULARIO ===
        // Información del usuario (no editable)
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Usuario: " + usuario.getNombreUsuario()), gbc);

        // Nombre completo
        gbc.gridy = 1;
        panel.add(new JLabel("Nombre completo:"), gbc);
        gbc.gridx = 1;
        panel.add(nombreField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        // Tipo de usuario
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Tipo:"), gbc);
        gbc.gridx = 1;
        panel.add(tipoCombo, gbc);

        // === BOTONES DE ACCIÓN ===
        JPanel buttonPanel = new JPanel();
        JButton guardarButton = new JButton("Guardar");
        JButton cancelarButton = new JButton("Cancelar");

        // Manejador del botón Guardar
        guardarButton.addActionListener(e -> {
            String nuevoNombre = nombreField.getText().trim();
            String nuevoEmail = emailField.getText().trim();
            TipoUsuario nuevoTipo = (TipoUsuario) tipoCombo.getSelectedItem();

            try {
                // Aplicar modificaciones
                boolean modificado = seguridadManager.modificarUsuario(
                        usuario.getNombreUsuario(), nuevoNombre, nuevoEmail);

                boolean tipoActualizado = seguridadManager.cambiarTipoUsuario(
                        usuario.getNombreUsuario(), nuevoTipo);

                if (modificado || tipoActualizado) {
                    JOptionPane.showMessageDialog(dialog,
                            "✅ Usuario modificado exitosamente",
                            "Modificación Exitosa",
                            JOptionPane.INFORMATION_MESSAGE);

                    actualizarVista();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "❌ Error modificando usuario",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "❌ Error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Manejador del botón Cancelar
        cancelarButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(guardarButton);
        buttonPanel.add(cancelarButton);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    /**
     * Elimina el usuario seleccionado del sistema
     *
     * Solicita confirmación antes de proceder con la eliminación
     * y aplica reglas de seguridad para proteger usuarios críticos.
     */
    private void eliminarUsuarioSeleccionado() {
        int filaSeleccionada = usuariosTable.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String nombreUsuario = (String) modeloTablaUsuarios.getValueAt(filaSeleccionada, 1);

            // Solicitar confirmación con advertencia
            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de eliminar el usuario '" + nombreUsuario + "'?\n" +
                            "Esta acción no se puede deshacer.",
                    "Confirmar Eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirmacion == JOptionPane.YES_OPTION) {
                try {
                    boolean eliminado = seguridadManager.eliminarUsuario(nombreUsuario);

                    if (eliminado) {
                        JOptionPane.showMessageDialog(this,
                                "✅ Usuario eliminado exitosamente",
                                "Eliminación Exitosa",
                                JOptionPane.INFORMATION_MESSAGE);

                        actualizarVista();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "❌ Error eliminando usuario",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SecurityException e) {
                    JOptionPane.showMessageDialog(this,
                            "❌ Error: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Cambia el estado del usuario seleccionado
     *
     * Permite cambiar entre diferentes estados (activo, inactivo, bloqueado, suspendido)
     * mediante un diálogo de selección.
     */
    private void cambiarEstadoUsuario() {
        int filaSeleccionada = usuariosTable.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String nombreUsuario = (String) modeloTablaUsuarios.getValueAt(filaSeleccionada, 1);

            // Mostrar diálogo de selección de estado
            EstadoUsuario[] estados = EstadoUsuario.values();
            EstadoUsuario nuevoEstado = (EstadoUsuario) JOptionPane.showInputDialog(this,
                    "Seleccione el nuevo estado para " + nombreUsuario + ":",
                    "Cambiar Estado",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    estados,
                    estados[0]);

            if (nuevoEstado != null) {
                try {
                    boolean cambiado = seguridadManager.cambiarEstadoUsuario(nombreUsuario, nuevoEstado);

                    if (cambiado) {
                        JOptionPane.showMessageDialog(this,
                                "✅ Estado cambiado a: " + nuevoEstado,
                                "Estado Actualizado",
                                JOptionPane.INFORMATION_MESSAGE);

                        actualizarVista();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "❌ Error cambiando estado",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SecurityException e) {
                    JOptionPane.showMessageDialog(this,
                            "❌ Error: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Restablece la contraseña del usuario seleccionado
     *
     * Permite a un administrador cambiar la contraseña de cualquier usuario
     * mediante un diálogo simple de entrada de texto.
     */
    private void resetearPassword() {
        int filaSeleccionada = usuariosTable.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String nombreUsuario = (String) modeloTablaUsuarios.getValueAt(filaSeleccionada, 1);

            // Solicitar nueva contraseña
            String nuevaContraseña = JOptionPane.showInputDialog(this,
                    "Ingrese la nueva contraseña para " + nombreUsuario + ":",
                    "Restablecer Contraseña",
                    JOptionPane.QUESTION_MESSAGE);

            if (nuevaContraseña != null && !nuevaContraseña.trim().isEmpty()) {
                try {
                    boolean restablecido = seguridadManager.restablecerContraseña(nombreUsuario, nuevaContraseña);

                    if (restablecido) {
                        JOptionPane.showMessageDialog(this,
                                "✅ Contraseña restablecida exitosamente",
                                "Contraseña Actualizada",
                                JOptionPane.INFORMATION_MESSAGE);

                        actualizarVista();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "❌ Error restableciendo contraseña",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                            "❌ Error: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Muestra el perfil completo del usuario seleccionado
     *
     * Presenta toda la información detallada del usuario en un diálogo
     * con formato de texto, incluyendo permisos, historial y configuración.
     */
    private void verPerfilUsuario() {
        int filaSeleccionada = usuariosTable.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String nombreUsuario = (String) modeloTablaUsuarios.getValueAt(filaSeleccionada, 1);

            try {
                Usuario usuario = seguridadManager.getUsuario(nombreUsuario);
                if (usuario != null) {
                    // Crear área de texto con el perfil completo
                    JTextArea perfilArea = new JTextArea(usuario.getPerfilCompleto());
                    perfilArea.setEditable(false);
                    perfilArea.setRows(15);
                    perfilArea.setColumns(50);
                    perfilArea.setFont(new Font("Consolas", Font.PLAIN, 12));

                    // Mostrar en diálogo con scroll
                    JScrollPane scrollPane = new JScrollPane(perfilArea);
                    JOptionPane.showMessageDialog(this, scrollPane,
                            "Perfil de Usuario: " + nombreUsuario,
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SecurityException e) {
                JOptionPane.showMessageDialog(this,
                        "❌ Sin permisos para ver el perfil",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}