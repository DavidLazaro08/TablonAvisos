package ui;
import DAO.*;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SwingMain extends JFrame {
    private final AutorDAO autorDAO = new AutorDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final AvisoDAO avisoDAO = new AvisoDAO();

    private JTable tablaAvisos;
    private DefaultTableModel modeloTabla;
    private JComboBox<Autor> comboAutores;
    private JComboBox<Categoria> comboCategorias;
    private JTextField campoTitulo;
    private JTextArea areaDescripcion;

    // Colores con tus gradientes preferidos
    private final Color BLUE_GRADIENT_START = new Color(40, 120, 255);
    private final Color BLUE_GRADIENT_END = new Color(25, 80, 200);
    private final Color RED_GRADIENT_START = new Color(220, 50, 50);
    private final Color RED_GRADIENT_END = new Color(180, 30, 30);

    public SwingMain() {
        setTitle("Tablón de Avisos - Swing");
        setSize(950, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        //setIconImage(new ImageIcon(getClass().getResource("/icons/notice.png")).getImage()); // Opcional: añade un icono

        // Panel principal con división
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(crearPanelTabla());
        splitPane.setBottomComponent(crearPanelFormulario());
        splitPane.setDividerLocation(380);
        splitPane.setResizeWeight(0.6);

        add(splitPane);
        cargarDatosIniciales();
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Avisos"));
        panel.setBackground(new Color(245, 247, 250));

        // Configurar tabla con mejor estilo
        String[] columnas = {"ID", "Título", "Autor", "Categoría", "Estado", "Fecha"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaAvisos = new JTable(modeloTabla) {
            // Colorear filas según estado
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    String estado = (String) getValueAt(row, 4);
                    if ("resuelto".equals(estado)) {
                        c.setBackground(new Color(230, 255, 230));
                    } else {
                        c.setBackground(new Color(255, 250, 230));
                    }
                }
                return c;
            }
        };
        tablaAvisos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaAvisos.setRowHeight(25);
        tablaAvisos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaAvisos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tablaAvisos.getTableHeader().setBackground(new Color(40, 120, 255));
        tablaAvisos.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(tablaAvisos);
        panel.add(scroll, BorderLayout.CENTER);

        // Botones de acción con tus gradientes
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JButton btnResolver = crearBotonGradiente("✓ Resuelto", BLUE_GRADIENT_START, BLUE_GRADIENT_END, Color.WHITE);
        btnResolver.addActionListener(e -> cambiarEstadoAviso("resuelto"));
        btnResolver.setPreferredSize(new Dimension(120, 35));

        JButton btnPendiente = crearBotonGradiente("⟳ Pendiente", new Color(255, 165, 0), new Color(220, 140, 0), Color.WHITE);
        btnPendiente.addActionListener(e -> cambiarEstadoAviso("pendiente"));
        btnPendiente.setPreferredSize(new Dimension(120, 35));

        JButton btnEliminar = crearBotonGradiente("🗑 Eliminar", RED_GRADIENT_START, RED_GRADIENT_END, Color.WHITE);
        btnEliminar.addActionListener(e -> eliminarAviso());
        btnEliminar.setPreferredSize(new Dimension(120, 35));

        panelBotones.add(btnResolver);
        panelBotones.add(btnPendiente);
        panelBotones.add(btnEliminar);
        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Nuevo Aviso"));
        panel.setBackground(new Color(250, 252, 255));

        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0;
        formulario.add(new JLabel("Título:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        campoTitulo = new JTextField(30);
        campoTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campoTitulo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 230), 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        formulario.add(campoTitulo, gbc);

        // Autor con botón de nuevo autor
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formulario.add(new JLabel("Autor:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.9;
        comboAutores = new JComboBox<>();
        comboAutores.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formulario.add(comboAutores, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.1;
        JButton btnNuevoAutor = crearBotonGradiente("+", BLUE_GRADIENT_START, BLUE_GRADIENT_END, Color.WHITE);
        btnNuevoAutor.setPreferredSize(new Dimension(40, 30));
        btnNuevoAutor.setToolTipText("Crear nuevo autor");
        btnNuevoAutor.addActionListener(e -> crearNuevoAutor());
        formulario.add(btnNuevoAutor, gbc);

        // Categoría con botón de nueva categoría
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formulario.add(new JLabel("Categoría:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.9;
        comboCategorias = new JComboBox<>();
        comboCategorias.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formulario.add(comboCategorias, gbc);

        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0.1;
        JButton btnNuevaCategoria = crearBotonGradiente("+", BLUE_GRADIENT_START, BLUE_GRADIENT_END, Color.WHITE);
        btnNuevaCategoria.setPreferredSize(new Dimension(40, 30));
        btnNuevaCategoria.setToolTipText("Crear nueva categoría");
        btnNuevaCategoria.addActionListener(e -> crearNuevaCategoria());
        formulario.add(btnNuevaCategoria, gbc);

        // Descripción
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3; gbc.weighty = 1.0;
        formulario.add(new JLabel("Descripción:"), gbc);

        gbc.gridy = 4;
        areaDescripcion = new JTextArea(5, 30);
        areaDescripcion.setLineWrap(true);
        areaDescripcion.setWrapStyleWord(true);
        areaDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        areaDescripcion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 230), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        JScrollPane scrollDesc = new JScrollPane(areaDescripcion);
        scrollDesc.setBorder(BorderFactory.createEmptyBorder());
        formulario.add(scrollDesc, gbc);

        // Botón crear con gradiente azul
        gbc.gridy = 5; gbc.gridwidth = 3; gbc.weighty = 0;
        JButton btnCrear = crearBotonGradiente("➕ CREAR AVISO", BLUE_GRADIENT_START, BLUE_GRADIENT_END, Color.WHITE);
        btnCrear.setFont(btnCrear.getFont().deriveFont(Font.BOLD, 15f));
        btnCrear.setPreferredSize(new Dimension(200, 45));
        btnCrear.addActionListener(e -> crearAviso());
        gbc.insets = new Insets(15, 0, 10, 0);
        formulario.add(btnCrear, gbc);

        // Panel inferior con gestión avanzada de catálogos
        JPanel panelCatalogos = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnGestionAutores = crearBotonGradiente("👥 Gestionar Autores", new Color(100, 140, 255), new Color(70, 110, 220), Color.WHITE);
        btnGestionAutores.addActionListener(e -> abrirGestionAutores());
        panelCatalogos.add(btnGestionAutores);

        JButton btnGestionCategorias = crearBotonGradiente("🏷 Gestionar Categorías", new Color(120, 160, 255), new Color(90, 130, 220), Color.WHITE);
        btnGestionCategorias.addActionListener(e -> abrirGestionCategorias());
        panelCatalogos.add(btnGestionCategorias);

        panel.add(formulario, BorderLayout.CENTER);
        panel.add(panelCatalogos, BorderLayout.SOUTH);
        return panel;
    }

    // Método para crear botones con gradiente (texto siempre visible)
    private JButton crearBotonGradiente(String texto, Color color1, Color color2, Color textColor) {
        JButton button = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Pintar gradiente SIN llamar a super.paintComponent() para evitar sobreposición
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // Pintar texto manualmente para garantizar visibilidad
                g2.setColor(textColor);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
            }
        };

        // Configuración crítica para evitar que Swing pinte encima
        button.setContentAreaFilled(false);  // ¡IMPORTANTE! Evita el fondo gris por defecto
        button.setFocusPainted(false);
        button.setBorderPainted(false);      // Sin borde por defecto (el gradiente ya tiene forma)
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Micro-interacción hover mejorada (solo brillo, sin cambiar texto)
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                // Efecto sutil de brillo
                button.repaint();
            }
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });

        return button;
    }

    private void cargarDatosIniciales() {
        try {
            // Cargar autores y categorías
            actualizarCombos();

            // Cargar avisos
            actualizarTabla();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar datos: " + e.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void actualizarCombos() throws SQLException {
        List<Autor> autores = autorDAO.obtenerTodos();
        List<Categoria> categorias = categoriaDAO.obtenerTodos();

        comboAutores.removeAllItems();
        for (Autor a : autores) comboAutores.addItem(a);

        comboCategorias.removeAllItems();
        for (Categoria c : categorias) comboCategorias.addItem(c);
    }

    private void actualizarTabla() {
        try {
            modeloTabla.setRowCount(0); // Limpiar tabla
            List<Aviso> avisos = avisoDAO.obtenerTodos();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");

            for (Aviso a : avisos) {
                String fecha = a.getFechaCreacion() != null
                        ? a.getFechaCreacion().format(formatter)
                        : "N/A";

                modeloTabla.addRow(new Object[]{
                        a.getId(),
                        a.getTitulo(),
                        a.getAutor().getNombre(),
                        a.getCategoria().getNombre(),
                        a.getEstado(),
                        fecha
                });
            }

            // Actualizar título con contador
            setTitle(String.format("Tablón de Avisos - Swing (%d avisos)", avisos.size()));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al actualizar tabla: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void crearAviso() {
        String titulo = campoTitulo.getText().trim();
        String descripcion = areaDescripcion.getText().trim();

        if (titulo.isEmpty() || descripcion.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Título y descripción son obligatorios",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Autor autor = (Autor) comboAutores.getSelectedItem();
        Categoria categoria = (Categoria) comboCategorias.getSelectedItem();

        if (autor == null || categoria == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un autor y una categoría válidos",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Aviso aviso = new Aviso();
            aviso.setTitulo(titulo);
            aviso.setDescripcion(descripcion);
            aviso.setEstado("pendiente");
            aviso.setAutor(autor);
            aviso.setCategoria(categoria);

            int id = avisoDAO.crear(aviso);
            if (id > 0) {
                JOptionPane.showMessageDialog(this,
                        "✅ Aviso creado correctamente (ID: " + id + ")",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                actualizarTabla();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al crear el aviso",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Error de base de datos", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void cambiarEstadoAviso(String nuevoEstado) {
        int fila = tablaAvisos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un aviso primero",
                    "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) modeloTabla.getValueAt(fila, 0);
        try {
            if (avisoDAO.actualizarEstado(id, nuevoEstado)) {
                String msg = "resuelto".equals(nuevoEstado) ? "✓ Aviso marcado como resuelto" : "⟳ Aviso marcado como pendiente";
                JOptionPane.showMessageDialog(this, msg, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                actualizarTabla();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se pudo actualizar el estado",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al actualizar estado: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void eliminarAviso() {
        int fila = tablaAvisos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un aviso primero",
                    "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) modeloTabla.getValueAt(fila, 0);
        String titulo = (String) modeloTabla.getValueAt(fila, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el aviso \"" + titulo + "\"?\nEsta acción no se puede deshacer.",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (avisoDAO.eliminar(id)) {
                    JOptionPane.showMessageDialog(this,
                            "🗑 Aviso eliminado correctamente",
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    actualizarTabla();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "No se pudo eliminar el aviso",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                if (e.getMessage().contains("foreign key")) {
                    JOptionPane.showMessageDialog(this,
                            "❌ No se puede eliminar: existen avisos asociados a este autor/categoría",
                            "Error de integridad", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error al eliminar: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                e.printStackTrace();
            }
        }
    }

    // === FUNCIONALIDADES ADICIONALES PARA AUTORES/CATEGORÍAS ===

    private void crearNuevoAutor() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre del nuevo autor:");
        if (nombre != null && !nombre.trim().isEmpty()) {
            try {
                Autor autor = new Autor();
                autor.setNombre(nombre.trim());
                int id = autorDAO.crear(autor);
                if (id > 0) {
                    JOptionPane.showMessageDialog(this, "✅ Autor creado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    actualizarCombos();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al crear autor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void crearNuevaCategoria() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre de la nueva categoría:");
        if (nombre != null && !nombre.trim().isEmpty()) {
            try {
                Categoria categoria = new Categoria();
                categoria.setNombre(nombre.trim());
                int id = categoriaDAO.crear(categoria);
                if (id > 0) {
                    JOptionPane.showMessageDialog(this, "✅ Categoría creada correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    actualizarCombos();
                }
            } catch (SQLException e) {
                if (e.getMessage().toLowerCase().contains("duplicate")) {
                    JOptionPane.showMessageDialog(this, "❌ Ya existe una categoría con ese nombre", "Error", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Error al crear categoría: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                e.printStackTrace();
            }
        }
    }

    private void abrirGestionAutores() {
        JDialog dialog = new JDialog(this, "Gestión de Autores", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Tabla de autores
        DefaultTableModel modeloAutores = new DefaultTableModel(new String[]{"ID", "Nombre"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tabla = new JTable(modeloAutores);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(25);

        try {
            List<Autor> autores = autorDAO.obtenerTodos();
            for (Autor a : autores) {
                modeloAutores.addRow(new Object[]{a.getId(), a.getNombre()});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JScrollPane scroll = new JScrollPane(tabla);
        panel.add(scroll, BorderLayout.CENTER);

        // Botones de acción
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton btnEditar = crearBotonGradiente("✏️ Editar", BLUE_GRADIENT_START, BLUE_GRADIENT_END, Color.WHITE);
        btnEditar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(dialog, "Selecciona un autor", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) modeloAutores.getValueAt(fila, 0);
            String nombreActual = (String) modeloAutores.getValueAt(fila, 1);
            String nuevoNombre = JOptionPane.showInputDialog(dialog, "Nuevo nombre:", nombreActual);
            if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                try {
                    Autor autor = new Autor(id, nuevoNombre.trim());
                    if (autorDAO.actualizar(autor)) {
                        modeloAutores.setValueAt(nuevoNombre.trim(), fila, 1);
                        actualizarCombos(); // Refrescar combos principales
                        JOptionPane.showMessageDialog(dialog, "✅ Autor actualizado", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnEliminar = crearBotonGradiente("🗑 Eliminar", RED_GRADIENT_START, RED_GRADIENT_END, Color.WHITE);
        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(dialog, "Selecciona un autor", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) modeloAutores.getValueAt(fila, 0);
            String nombre = (String) modeloAutores.getValueAt(fila, 1);

            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "¿Eliminar autor \"" + nombre + "\"?\n⚠️ Se eliminarán también todos sus avisos.",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (autorDAO.eliminar(id)) {
                        modeloAutores.removeRow(fila);
                        actualizarCombos();
                        JOptionPane.showMessageDialog(dialog, "✅ Autor eliminado", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnNuevo = crearBotonGradiente("➕ Nuevo", BLUE_GRADIENT_START, BLUE_GRADIENT_END, Color.WHITE);
        btnNuevo.addActionListener(e -> {
            String nombre = JOptionPane.showInputDialog(dialog, "Nombre del nuevo autor:");
            if (nombre != null && !nombre.trim().isEmpty()) {
                try {
                    Autor autor = new Autor();
                    autor.setNombre(nombre.trim());
                    int id = autorDAO.crear(autor);
                    if (id > 0) {
                        modeloAutores.addRow(new Object[]{id, nombre.trim()});
                        actualizarCombos();
                        JOptionPane.showMessageDialog(dialog, "✅ Autor creado", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnCerrar = crearBotonGradiente("Cerrar", new Color(150, 150, 150), new Color(120, 120, 120), Color.WHITE);
        btnCerrar.addActionListener(e -> dialog.dispose());

        botones.add(btnNuevo);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        botones.add(btnCerrar);

        panel.add(botones, BorderLayout.SOUTH);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void abrirGestionCategorias() {
        JDialog dialog = new JDialog(this, "Gestión de Categorías", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Tabla de categorías
        DefaultTableModel modeloCategorias = new DefaultTableModel(new String[]{"ID", "Nombre"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tabla = new JTable(modeloCategorias);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(25);

        try {
            List<Categoria> categorias = categoriaDAO.obtenerTodos();
            for (Categoria c : categorias) {
                modeloCategorias.addRow(new Object[]{c.getId(), c.getNombre()});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JScrollPane scroll = new JScrollPane(tabla);
        panel.add(scroll, BorderLayout.CENTER);

        // Botones de acción
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton btnEditar = crearBotonGradiente("✏️ Editar", BLUE_GRADIENT_START, BLUE_GRADIENT_END, Color.WHITE);
        btnEditar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(dialog, "Selecciona una categoría", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) modeloCategorias.getValueAt(fila, 0);
            String nombreActual = (String) modeloCategorias.getValueAt(fila, 1);
            String nuevoNombre = JOptionPane.showInputDialog(dialog, "Nuevo nombre:", nombreActual);
            if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                try {
                    Categoria categoria = new Categoria(id, nuevoNombre.trim());
                    if (categoriaDAO.actualizar(categoria)) {
                        modeloCategorias.setValueAt(nuevoNombre.trim(), fila, 1);
                        actualizarCombos();
                        JOptionPane.showMessageDialog(dialog, "✅ Categoría actualizada", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (SQLException ex) {
                    if (ex.getMessage().toLowerCase().contains("duplicate")) {
                        JOptionPane.showMessageDialog(dialog, "❌ Ya existe una categoría con ese nombre", "Error", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JButton btnEliminar = crearBotonGradiente("🗑 Eliminar", RED_GRADIENT_START, RED_GRADIENT_END, Color.WHITE);
        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(dialog, "Selecciona una categoría", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) modeloCategorias.getValueAt(fila, 0);
            String nombre = (String) modeloCategorias.getValueAt(fila, 1);

            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "¿Eliminar categoría \"" + nombre + "\"?\n⚠️ Solo se puede si no tiene avisos asociados.",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (categoriaDAO.eliminar(id)) {
                        modeloCategorias.removeRow(fila);
                        actualizarCombos();
                        JOptionPane.showMessageDialog(dialog, "✅ Categoría eliminada", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(dialog,
                                "❌ No se puede eliminar: existen avisos asociados a esta categoría",
                                "Error de integridad", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnNuevo = crearBotonGradiente("➕ Nuevo", BLUE_GRADIENT_START, BLUE_GRADIENT_END, Color.WHITE);
        btnNuevo.addActionListener(e -> {
            String nombre = JOptionPane.showInputDialog(dialog, "Nombre de la nueva categoría:");
            if (nombre != null && !nombre.trim().isEmpty()) {
                try {
                    Categoria categoria = new Categoria();
                    categoria.setNombre(nombre.trim());
                    int id = categoriaDAO.crear(categoria);
                    if (id > 0) {
                        modeloCategorias.addRow(new Object[]{id, nombre.trim()});
                        actualizarCombos();
                        JOptionPane.showMessageDialog(dialog, "✅ Categoría creada", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (SQLException ex) {
                    if (ex.getMessage().toLowerCase().contains("duplicate")) {
                        JOptionPane.showMessageDialog(dialog, "❌ Ya existe una categoría con ese nombre", "Error", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JButton btnCerrar = crearBotonGradiente("Cerrar", new Color(150, 150, 150), new Color(120, 120, 120), Color.WHITE);
        btnCerrar.addActionListener(e -> dialog.dispose());

        botones.add(btnNuevo);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        botones.add(btnCerrar);

        panel.add(botones, BorderLayout.SOUTH);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    // === UTILIDADES ===

    private void limpiarFormulario() {
        campoTitulo.setText("");
        areaDescripcion.setText("");
        if (comboAutores.getItemCount() > 0) comboAutores.setSelectedIndex(0);
        if (comboCategorias.getItemCount() > 0) comboCategorias.setSelectedIndex(0);
        campoTitulo.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Look and Feel moderno
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                // Opcional: para Windows 11/10 usar FlatLaf para mejor estilo
                // UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SwingMain().setVisible(true);
        });
    }
}