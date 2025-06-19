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

public class SeguridadPanel extends JPanel {

    // Componentes GUI
    private JTextField usuarioField;
    private JPasswordField contraseñaField;
    private JButton loginBtn;
    private JButton logoutBtn;
    private JLabel estadoSesionLabel;
    private JLabel usuarioActualLabel;

    // Panel de administración
    private JPanel adminPanel;
    private JTable usuariosTable;
    private DefaultTableModel modeloTablaUsuarios;
    private JTextArea logArea;
    private JLabel estadisticasLabel;

    // Botones de administración
    private JButton crearUsuarioBtn;
    private JButton modificarUsuarioBtn;
    private JButton eliminarUsuarioBtn;
    private JButton cambiarEstadoBtn;
    private JButton resetPasswordBtn;
    private JButton verPerfilBtn;

    // Lógica del sistema
    private SeguridadManager seguridadManager;

    public SeguridadPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        initializeSeguridadManager();
        actualizarVista();
    }

    private void initializeComponents() {
        // Componentes de login
        usuarioField = new JTextField(15);
        contraseñaField = new JPasswordField(15);
        loginBtn = new JButton("🔐 Iniciar Sesión");
        logoutBtn = new JButton("🚪 Cerrar Sesión");

        estadoSesionLabel = new JLabel("❌ Sin sesión activa");
        estadoSesionLabel.setFont(new Font("Arial", Font.BOLD, 12));

        usuarioActualLabel = new JLabel("Usuario: Ninguno");
        usuarioActualLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        // Panel de administración (inicialmente oculto)
        adminPanel = new JPanel();

        // Tabla de usuarios
        String[] columnNames = {"", "Usuario", "Nombre", "Tipo", "Estado", "Último Acceso"};
        modeloTablaUsuarios = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        usuariosTable = new JTable(modeloTablaUsuarios);
        usuariosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usuariosTable.getColumnModel().getColumn(0).setMaxWidth(30); // Columna de icono
        usuariosTable.getColumnModel().getColumn(3).setMaxWidth(100); // Tipo
        usuariosTable.getColumnModel().getColumn(4).setMaxWidth(80);  // Estado

        // Área de log
        logArea = new JTextArea(8, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setBackground(new Color(248, 248, 248));

        // Botones de administración
        crearUsuarioBtn = new JButton("👤➕ Crear Usuario");
        modificarUsuarioBtn = new JButton("✏️ Modificar");
        eliminarUsuarioBtn = new JButton("🗑️ Eliminar");
        cambiarEstadoBtn = new JButton("🔄 Cambiar Estado");
        resetPasswordBtn = new JButton("🔑 Reset Password");
        verPerfilBtn = new JButton("📋 Ver Perfil");

        // Label de estadísticas
        estadisticasLabel = new JLabel();
        estadisticasLabel.setFont(new Font("Arial", Font.BOLD, 11));

        // Estados iniciales
        logoutBtn.setEnabled(false);
        adminPanel.setVisible(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panel superior - Login
        JPanel loginPanel = createLoginPanel();
        add(loginPanel, BorderLayout.NORTH);

        // Panel central - Administración (condicionalmente visible)
        setupAdminPanel();
        add(adminPanel, BorderLayout.CENTER);

        // Panel inferior - Estadísticas
        JPanel estadisticasPanel = createEstadisticasPanel();
        add(estadisticasPanel, BorderLayout.SOUTH);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Autenticación"));

        // Panel de campos de login
        JPanel camposPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        camposPanel.add(new JLabel("Usuario:"));
        camposPanel.add(usuarioField);
        camposPanel.add(new JLabel("Contraseña:"));
        camposPanel.add(contraseñaField);
        camposPanel.add(loginBtn);
        camposPanel.add(logoutBtn);

        // Panel de estado
        JPanel estadoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        estadoPanel.add(estadoSesionLabel);
        estadoPanel.add(Box.createHorizontalStrut(20));
        estadoPanel.add(usuarioActualLabel);

        panel.add(camposPanel, BorderLayout.NORTH);
        panel.add(estadoPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void setupAdminPanel() {
        adminPanel.setLayout(new BorderLayout(10, 10));
        adminPanel.setBorder(new TitledBorder("Administración de Usuarios"));

        // Panel central - División horizontal
        JSplitPane centralSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Panel izquierdo - Lista de usuarios
        JPanel usuariosPanel = createUsuariosPanel();
        centralSplit.setLeftComponent(usuariosPanel);

        // Panel derecho - Log del sistema
        JPanel logPanel = createLogPanel();
        centralSplit.setRightComponent(logPanel);

        centralSplit.setDividerLocation(500);
        adminPanel.add(centralSplit, BorderLayout.CENTER);

        // Panel de botones
        JPanel botonesPanel = createBotonesAdminPanel();
        adminPanel.add(botonesPanel, BorderLayout.SOUTH);
    }

    private JPanel createUsuariosPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Usuarios del Sistema"));

        JScrollPane scrollPane = new JScrollPane(usuariosTable);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Log del Sistema"));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(scrollPane, BorderLayout.CENTER);

        // Botón para actualizar log
        JButton actualizarLogBtn = new JButton("🔄 Actualizar Log");
        actualizarLogBtn.addActionListener(e -> actualizarLog());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(actualizarLogBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBotonesAdminPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        panel.add(crearUsuarioBtn);
        panel.add(modificarUsuarioBtn);
        panel.add(eliminarUsuarioBtn);
        panel.add(cambiarEstadoBtn);
        panel.add(resetPasswordBtn);
        panel.add(verPerfilBtn);

        return panel;
    }

    private JPanel createEstadisticasPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Estadísticas de Seguridad"));
        panel.add(estadisticasLabel, BorderLayout.CENTER);
        return panel;
    }

    private void setupEventHandlers() {
        // Botones principales
        loginBtn.addActionListener(e -> iniciarSesion());
        logoutBtn.addActionListener(e -> cerrarSesion());

        // Enter en campos de login
        usuarioField.addActionListener(e -> contraseñaField.requestFocus());
        contraseñaField.addActionListener(e -> iniciarSesion());

        // Selección en tabla
        usuariosTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarBotonesAdmin();
            }
        });

        // Botones de administración
        crearUsuarioBtn.addActionListener(e -> mostrarDialogoCrearUsuario());
        modificarUsuarioBtn.addActionListener(e -> modificarUsuarioSeleccionado());
        eliminarUsuarioBtn.addActionListener(e -> eliminarUsuarioSeleccionado());
        cambiarEstadoBtn.addActionListener(e -> cambiarEstadoUsuario());
        resetPasswordBtn.addActionListener(e -> resetearPassword());
        verPerfilBtn.addActionListener(e -> verPerfilUsuario());
    }

    private void initializeSeguridadManager() {
        seguridadManager = new SeguridadManager();
    }

    private void actualizarVista() {
        actualizarEstadoSesion();
        actualizarTablaUsuarios();
        actualizarEstadisticas();
        actualizarVisibilidadAdmin();
    }

    private void actualizarEstadoSesion() {
        if (seguridadManager.haySesionActiva()) {
            Usuario usuario = seguridadManager.getUsuarioActual();
            estadoSesionLabel.setText("✅ Sesión activa");
            estadoSesionLabel.setForeground(new Color(0, 128, 0));
            usuarioActualLabel.setText("Usuario: " + usuario.getIcono() + " " +
                    usuario.getNombreUsuario() + " (" + usuario.getTipo().getCodigo() + ")");

            loginBtn.setEnabled(false);
            logoutBtn.setEnabled(true);
            usuarioField.setEnabled(false);
            contraseñaField.setEnabled(false);
        } else {
            estadoSesionLabel.setText("❌ Sin sesión activa");
            estadoSesionLabel.setForeground(Color.RED);
            usuarioActualLabel.setText("Usuario: Ninguno");

            loginBtn.setEnabled(true);
            logoutBtn.setEnabled(false);
            usuarioField.setEnabled(true);
            contraseñaField.setEnabled(true);
        }
    }

    private void actualizarTablaUsuarios() {
        modeloTablaUsuarios.setRowCount(0);

        if (seguridadManager.haySesionActiva() &&
                seguridadManager.verificarPermiso(Permiso.VER_LOGS_SISTEMA)) {

            try {
                List<Usuario> usuarios = seguridadManager.getUsuarios();

                for (Usuario usuario : usuarios) {
                    Object[] rowData = {
                            usuario.getEstadoIcono(),
                            usuario.getNombreUsuario(),
                            usuario.getNombre(),
                            usuario.getTipo().getCodigo(),
                            usuario.getEstado().toString(),
                            usuario.getUltimoAcceso() != null ?
                                    usuario.getUltimoAcceso().toLocalDate().toString() : "Nunca"
                    };
                    modeloTablaUsuarios.addRow(rowData);
                }
            } catch (SecurityException e) {
                // Usuario sin permisos para ver la lista
            }
        }
    }

    private void actualizarEstadisticas() {
        if (seguridadManager.haySesionActiva() &&
                seguridadManager.getUsuarioActual().esAdministrador()) {

            try {
                estadisticasLabel.setText(seguridadManager.getEstadisticasSeguridad());
            } catch (SecurityException e) {
                estadisticasLabel.setText("Sin permisos para ver estadísticas");
            }
        } else {
            estadisticasLabel.setText("Inicie sesión como administrador para ver estadísticas");
        }
    }

    private void actualizarVisibilidadAdmin() {
        boolean esAdmin = seguridadManager.haySesionActiva() &&
                seguridadManager.getUsuarioActual().esAdministrador();

        adminPanel.setVisible(esAdmin);

        if (esAdmin) {
            actualizarBotonesAdmin();
            actualizarLog();
        }

        revalidate();
        repaint();
    }

    private void actualizarBotonesAdmin() {
        int filaSeleccionada = usuariosTable.getSelectedRow();
        boolean haySeleccion = filaSeleccionada >= 0;

        modificarUsuarioBtn.setEnabled(haySeleccion);
        eliminarUsuarioBtn.setEnabled(haySeleccion);
        cambiarEstadoBtn.setEnabled(haySeleccion);
        resetPasswordBtn.setEnabled(haySeleccion);
        verPerfilBtn.setEnabled(haySeleccion);

        // No permitir eliminar el usuario actual o admin principal
        if (haySeleccion) {
            String nombreUsuario = (String) modeloTablaUsuarios.getValueAt(filaSeleccionada, 1);
            boolean esUsuarioActual = seguridadManager.getUsuarioActual().getNombreUsuario().equals(nombreUsuario);
            boolean esAdminPrincipal = "admin".equals(nombreUsuario);

            eliminarUsuarioBtn.setEnabled(!esUsuarioActual && !esAdminPrincipal);
        }
    }

    private void actualizarLog() {
        if (seguridadManager.haySesionActiva() &&
                seguridadManager.verificarPermiso(Permiso.VER_LOGS_SISTEMA)) {

            try {
                List<String> logReciente = seguridadManager.getLogReciente(20);
                StringBuilder sb = new StringBuilder();

                for (String entrada : logReciente) {
                    sb.append(entrada).append("\n");
                }

                logArea.setText(sb.toString());
                logArea.setCaretPosition(logArea.getDocument().getLength());

            } catch (SecurityException e) {
                logArea.setText("Sin permisos para ver el log del sistema");
            }
        } else {
            logArea.setText("Inicie sesión como administrador para ver el log");
        }
    }

    private void iniciarSesion() {
        String usuario = usuarioField.getText().trim();
        String contraseña = new String(contraseñaField.getPassword());

        if (usuario.isEmpty() || contraseña.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "❌ Ingrese usuario y contraseña",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean exitoso = seguridadManager.iniciarSesion(usuario, contraseña);

        if (exitoso) {
            JOptionPane.showMessageDialog(this,
                    "✅ Inicio de sesión exitoso",
                    "Bienvenido",
                    JOptionPane.INFORMATION_MESSAGE);

            // Limpiar campos
            usuarioField.setText("");
            contraseñaField.setText("");

            actualizarVista();
        } else {
            JOptionPane.showMessageDialog(this,
                    "❌ Usuario o contraseña incorrectos",
                    "Error de Autenticación",
                    JOptionPane.ERROR_MESSAGE);

            contraseñaField.setText("");
            contraseñaField.requestFocus();
        }
    }

    private void cerrarSesion() {
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de cerrar la sesión?",
                "Confirmar Cierre de Sesión",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            seguridadManager.cerrarSesion();
            actualizarVista();

            JOptionPane.showMessageDialog(this,
                    "✅ Sesión cerrada exitosamente",
                    "Sesión Cerrada",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void mostrarDialogoCrearUsuario() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Crear Nuevo Usuario", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Campos del formulario
        JTextField nombreUsuarioField = new JTextField(15);
        JPasswordField nuevaContraseñaField = new JPasswordField(15);
        JTextField nombreField = new JTextField(15);
        JTextField emailField = new JTextField(15);
        JComboBox<TipoUsuario> tipoCombo = new JComboBox<>(TipoUsuario.values());

        // Layout del formulario
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Nombre de usuario:"), gbc);
        gbc.gridx = 1;
        panel.add(nombreUsuarioField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        panel.add(nuevaContraseñaField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Nombre completo:"), gbc);
        gbc.gridx = 1;
        panel.add(nombreField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Tipo de usuario:"), gbc);
        gbc.gridx = 1;
        panel.add(tipoCombo, gbc);

        // Botones
        JPanel buttonPanel = new JPanel();
        JButton crearButton = new JButton("Crear");
        JButton cancelarButton = new JButton("Cancelar");

        crearButton.addActionListener(e -> {
            String nombreUsuario = nombreUsuarioField.getText().trim();
            String contraseña = new String(nuevaContraseñaField.getPassword());
            String nombre = nombreField.getText().trim();
            String email = emailField.getText().trim();
            TipoUsuario tipo = (TipoUsuario) tipoCombo.getSelectedItem();

            if (nombreUsuario.isEmpty() || contraseña.isEmpty() || nombre.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "❌ Complete todos los campos obligatorios",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
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

        cancelarButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(crearButton);
        buttonPanel.add(cancelarButton);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

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

    private void mostrarDialogoModificarUsuario(Usuario usuario) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Modificar Usuario", true);
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Campos editables
        JTextField nombreField = new JTextField(usuario.getNombre(), 15);
        JTextField emailField = new JTextField(usuario.getEmail(), 15);
        JComboBox<TipoUsuario> tipoCombo = new JComboBox<>(TipoUsuario.values());
        tipoCombo.setSelectedItem(usuario.getTipo());

        // Layout
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Usuario: " + usuario.getNombreUsuario()), gbc);

        gbc.gridy = 1;
        panel.add(new JLabel("Nombre completo:"), gbc);
        gbc.gridx = 1;
        panel.add(nombreField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Tipo:"), gbc);
        gbc.gridx = 1;
        panel.add(tipoCombo, gbc);

        // Botones
        JPanel buttonPanel = new JPanel();
        JButton guardarButton = new JButton("Guardar");
        JButton cancelarButton = new JButton("Cancelar");

        guardarButton.addActionListener(e -> {
            String nuevoNombre = nombreField.getText().trim();
            String nuevoEmail = emailField.getText().trim();
            TipoUsuario nuevoTipo = (TipoUsuario) tipoCombo.getSelectedItem();

            try {
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

        cancelarButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(guardarButton);
        buttonPanel.add(cancelarButton);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void eliminarUsuarioSeleccionado() {
        int filaSeleccionada = usuariosTable.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String nombreUsuario = (String) modeloTablaUsuarios.getValueAt(filaSeleccionada, 1);

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

    private void cambiarEstadoUsuario() {
        int filaSeleccionada = usuariosTable.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String nombreUsuario = (String) modeloTablaUsuarios.getValueAt(filaSeleccionada, 1);

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

    private void resetearPassword() {
        int filaSeleccionada = usuariosTable.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String nombreUsuario = (String) modeloTablaUsuarios.getValueAt(filaSeleccionada, 1);

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

    private void verPerfilUsuario() {
        int filaSeleccionada = usuariosTable.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String nombreUsuario = (String) modeloTablaUsuarios.getValueAt(filaSeleccionada, 1);

            try {
                Usuario usuario = seguridadManager.getUsuario(nombreUsuario);
                if (usuario != null) {
                    JTextArea perfilArea = new JTextArea(usuario.getPerfilCompleto());
                    perfilArea.setEditable(false);
                    perfilArea.setRows(15);
                    perfilArea.setColumns(50);
                    perfilArea.setFont(new Font("Consolas", Font.PLAIN, 12));

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