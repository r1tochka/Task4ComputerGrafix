package modelviewer.ui;

import modelviewer.controller.SceneController;
import modelviewer.model.Model;
import modelviewer.io.ObjReader;
import modelviewer.controller.ModelController;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class MainWindow extends JFrame {
    private SceneController sceneController;
    private ModelTree modelTree;
    private ModelViewerPanel viewerPanel;
    private boolean darkMode = true;
    private JMenuItem themeToggleItem;
    private JLabel statusLabel;
    private JComboBox<String> modelSelector;
    private JLabel modelInfoLabel;

    public MainWindow() {
        super("3D Model Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        sceneController = new SceneController();
        initUI();
        initMenu();
        initKeyboardShortcuts();

        SwingUtilities.invokeLater(() -> updateModelSelector());
    }

    private void initUI() {
        applyDarkTheme();

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(300);
        mainSplitPane.setDividerSize(3);
        mainSplitPane.setBackground(darkMode ? new Color(45, 45, 50) : new Color(240, 240, 245));

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(darkMode ? new Color(50, 53, 55) : new Color(250, 250, 255));

        JPanel modelControlPanel = new JPanel(new BorderLayout(5, 5));
        modelControlPanel.setBackground(darkMode ? new Color(50, 53, 55) : new Color(250, 250, 255));
        modelControlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        modelSelector = new JComboBox<>();  //для выбора активной модели
        modelSelector.setBackground(darkMode ? new Color(60, 63, 65) : Color.WHITE);
        modelSelector.setForeground(darkMode ? Color.WHITE : Color.BLACK);
        modelSelector.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        modelSelector.addActionListener(e -> {
            int selectedIndex = modelSelector.getSelectedIndex();
            List<Model> models = sceneController.getAllModels();

            if (!models.isEmpty() && selectedIndex >= 0 && selectedIndex < models.size()) {
                sceneController.setActiveModel(selectedIndex);
                viewerPanel.repaint();
                updateModelInfo();
                showNotification("Selected model: " + sceneController.getActiveModel().getName());
            }
        });

        JPanel selectorPanel = new JPanel(new BorderLayout(5, 0));
        selectorPanel.setBackground(darkMode ? new Color(50, 53, 55) : new Color(250, 250, 255));
        selectorPanel.add(new JLabel("Active Model:"), BorderLayout.WEST);
        selectorPanel.add(modelSelector, BorderLayout.CENTER);

        modelInfoLabel = new JLabel("No models loaded");
        modelInfoLabel.setForeground(darkMode ? new Color(200, 200, 200) : new Color(80, 80, 80));
        modelInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        modelControlPanel.add(selectorPanel, BorderLayout.NORTH);
        modelControlPanel.add(modelInfoLabel, BorderLayout.SOUTH);

        modelTree = new ModelTree(sceneController);
        modelTree.setDarkMode(darkMode);

        leftPanel.add(modelControlPanel, BorderLayout.NORTH);
        leftPanel.add(modelTree, BorderLayout.CENTER);

        viewerPanel = new ModelViewerPanel(sceneController);
        viewerPanel.setDarkMode(darkMode);

        mainSplitPane.setLeftComponent(leftPanel);
        mainSplitPane.setRightComponent(viewerPanel);

        add(mainSplitPane);
        add(createTopToolbar(), BorderLayout.NORTH);
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private void applyDarkTheme() {
        darkMode = true;
        getContentPane().setBackground(new Color(35, 35, 40));

        if (modelTree != null) modelTree.setDarkMode(true);
        if (viewerPanel != null) viewerPanel.setDarkMode(true);
    }

    private void applyLightTheme() {
        darkMode = false;
        getContentPane().setBackground(new Color(240, 240, 245));

        if (modelTree != null) modelTree.setDarkMode(false);
        if (viewerPanel != null) viewerPanel.setDarkMode(false);
    }

    private JPanel createTopToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setBackground(darkMode ? new Color(40, 40, 45) : new Color(220, 220, 230));
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, darkMode ? new Color(60, 60, 65) : new Color(180, 180, 190)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JButton loadBtn = createActionButton("Load Model", new Color(66, 165, 245));
        JButton saveBtn = createActionButton("Save Model", new Color(102, 187, 106));
        JButton deleteModelBtn = createActionButton("Delete Model", new Color(239, 83, 80));
        JButton deleteVertexBtn = createActionButton("Delete Vertex", new Color(255, 112, 67));
        JButton deletePolygonBtn = createActionButton("Delete Polygon", new Color(255, 152, 0));
        JButton clearBtn = createActionButton("Clear All", new Color(224, 64, 251));
        JButton rotateMenuBtn = createActionButton("Rotate", new Color(255, 87, 34));
        JButton transformBtn = createActionButton("Transform", new Color(156, 39, 176));

        toolbar.add(loadBtn);
        toolbar.add(saveBtn);
        toolbar.add(deleteModelBtn);
        toolbar.add(createSeparator());
        toolbar.add(deleteVertexBtn);
        toolbar.add(deletePolygonBtn);
        toolbar.add(transformBtn);
        toolbar.add(clearBtn);
        toolbar.add(createSeparator());
        toolbar.add(rotateMenuBtn);

        loadBtn.addActionListener(e -> loadModel());
        saveBtn.addActionListener(e -> saveModel());
        deleteModelBtn.addActionListener(e -> removeSelectedModel());
        deleteVertexBtn.addActionListener(e -> deleteSelectedVertex());
        deletePolygonBtn.addActionListener(e -> deleteSelectedPolygon());
        clearBtn.addActionListener(e -> clearAllModels());

        JPopupMenu rotateMenu = createRotateMenu();
        rotateMenuBtn.addActionListener(e -> {
            rotateMenu.show(rotateMenuBtn, 0, rotateMenuBtn.getHeight());
        });

        JPopupMenu transformMenu = createTransformMenu();
        transformBtn.addActionListener(e -> {
            transformMenu.show(transformBtn, 0, transformBtn.getHeight());
        });

        return toolbar;
    }

    private JPopupMenu createRotateMenu() {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(darkMode ? new Color(50, 53, 55) : Color.WHITE);

        JMenuItem rotateXItem = new JMenuItem("Rotate X (15°)");
        JMenuItem rotateYItem = new JMenuItem("Rotate Y (15°)");
        JMenuItem rotateZItem = new JMenuItem("Rotate Z (15°)");
        menu.addSeparator();
        JMenuItem rotateCustomItem = new JMenuItem("Custom Rotation...");

        rotateXItem.addActionListener(e -> rotateModel(15, 0, 0));
        rotateYItem.addActionListener(e -> rotateModel(0, 15, 0));
        rotateZItem.addActionListener(e -> rotateModel(0, 0, 15));
        rotateCustomItem.addActionListener(e -> showCustomRotationDialog());

        menu.add(rotateXItem);
        menu.add(rotateYItem);
        menu.add(rotateZItem);
        menu.addSeparator();
        menu.add(rotateCustomItem);

        return menu;
    }

    private void showCustomRotationDialog() {
        if (sceneController.getActiveModel() == null) {
            ErrorDialog.showWarning("No active model to rotate", "Rotation Error");
            return;
        }

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBackground(darkMode ? new Color(60, 63, 65) : Color.WHITE);

        JTextField xField = new JTextField("0", 5);
        JTextField yField = new JTextField("0", 5);
        JTextField zField = new JTextField("0", 5);

        panel.add(new JLabel("Angle X (°):"));
        panel.add(xField);
        panel.add(new JLabel("Angle Y (°):"));
        panel.add(yField);
        panel.add(new JLabel("Angle Z (°):"));
        panel.add(zField);
        panel.add(new JLabel(""));
        JButton applyBtn = new JButton("Apply");
        panel.add(applyBtn);

        JDialog dialog = new JDialog(this, "Custom Rotation", true);
        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        applyBtn.addActionListener(e -> {
            try {
                float angleX = Float.parseFloat(xField.getText());
                float angleY = Float.parseFloat(yField.getText());
                float angleZ = Float.parseFloat(zField.getText());

                rotateModel(angleX, angleY, angleZ);
                dialog.dispose();
            } catch (NumberFormatException ex) {
                ErrorDialog.showError("Please enter valid numbers", "Input Error");
            }
        });

        dialog.setVisible(true);
    }

    private JButton createActionButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);

        button.setBackground(backgroundColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));

        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(backgroundColor.darker(), 2),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(brighterColor(backgroundColor, 1.15f));
                button.setForeground(Color.BLACK);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
                button.setForeground(Color.BLACK);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(darkerColor(backgroundColor, 1.15f));
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                button.setBackground(brighterColor(backgroundColor, 1.15f));
            }
        });

        return button;
    }

    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setBackground(darkMode ? new Color(80, 80, 85) : new Color(180, 180, 185));
        separator.setForeground(darkMode ? new Color(80, 80, 85) : new Color(180, 180, 185));
        separator.setMaximumSize(new Dimension(1, 30));
        return separator;
    }

    private Color brighterColor(Color color, float factor) {
        int r = Math.min(255, (int)(color.getRed() * factor));
        int g = Math.min(255, (int)(color.getGreen() * factor));
        int b = Math.min(255, (int)(color.getBlue() * factor));
        return new Color(r, g, b);
    }

    private Color darkerColor(Color color, float factor) {
        int r = Math.max(0, (int)(color.getRed() / factor));
        int g = Math.max(0, (int)(color.getGreen() / factor));
        int b = Math.max(0, (int)(color.getBlue() / factor));
        return new Color(r, g, b);
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(darkMode ? new Color(50, 53, 55) : new Color(230, 230, 240));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, darkMode ? new Color(70, 70, 75) : new Color(180, 180, 190)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(darkMode ? new Color(220, 220, 220) : new Color(50, 50, 50));
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel hintLabel = new JLabel(
                "<html><font color='" +
                        (darkMode ? "#CCCCCC" : "#555555") +
                        "'>Click vertex | Ctrl+Click polygon | Mouse wheel: zoom</font></html>"
        );
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(hintLabel, BorderLayout.EAST);

        return statusBar;
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.BLACK);
        menuBar.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        JMenu fileMenu = createBlackTextMenu("File", new Color(220, 220, 220));
        JMenuItem loadItem = createBlackTextMenuItem("Load OBJ...", "ctrl O");
        JMenuItem saveItem = createBlackTextMenuItem("Save Active Model...", "ctrl S");
        JMenuItem exitItem = createBlackTextMenuItem("Exit", "ctrl Q");

        loadItem.addActionListener(e -> loadModel());
        saveItem.addActionListener(e -> saveModel());
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(loadItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu editMenu = createBlackTextMenu("Edit", new Color(220, 220, 220));
        JMenuItem deleteVertexItem = createBlackTextMenuItem("Delete Vertex", "DELETE");
        JMenuItem deletePolygonItem = createBlackTextMenuItem("Delete Polygon", "shift DELETE");
        JMenuItem clearSelectionItem = createBlackTextMenuItem("Clear Selection", "ESC");
        JMenuItem transformItem = createBlackTextMenuItem("Transform Model...", "ctrl T");

        deleteVertexItem.addActionListener(e -> deleteSelectedVertex());
        deletePolygonItem.addActionListener(e -> deleteSelectedPolygon());
        clearSelectionItem.addActionListener(e -> clearSelection());
        transformItem.addActionListener(e -> showTransformDialog());

        editMenu.add(deleteVertexItem);
        editMenu.add(deletePolygonItem);
        editMenu.add(transformItem);
        editMenu.addSeparator();
        editMenu.add(clearSelectionItem);

        JMenu modelsMenu = createBlackTextMenu("Models", new Color(220, 220, 220));
        JMenuItem addExampleItem = createBlackTextMenuItem("Add Example Model", "ctrl E");
        JMenuItem clearAllItem = createBlackTextMenuItem("Clear All Models", "ctrl shift C");

        addExampleItem.addActionListener(e -> addExampleModel());
        clearAllItem.addActionListener(e -> clearAllModels());

        modelsMenu.add(addExampleItem);
        modelsMenu.addSeparator();
        modelsMenu.add(clearAllItem);

        JMenu viewMenu = createBlackTextMenu("View", new Color(220, 220, 220));
        JMenuItem zoomInItem = createBlackTextMenuItem("Zoom In", "ctrl EQUALS");
        JMenuItem zoomOutItem = createBlackTextMenuItem("Zoom Out", "ctrl MINUS");
        JMenuItem resetViewItem = createBlackTextMenuItem("Reset View", "ctrl R");

        zoomInItem.addActionListener(e -> zoom(1.2f));
        zoomOutItem.addActionListener(e -> zoom(0.8f));
        resetViewItem.addActionListener(e -> resetView());

        viewMenu.add(zoomInItem);
        viewMenu.add(zoomOutItem);
        viewMenu.addSeparator();
        viewMenu.add(resetViewItem);

        JMenu settingsMenu = createBlackTextMenu("Settings", new Color(220, 220, 220));
        themeToggleItem = createBlackTextMenuItem(darkMode ? "Switch to Light Theme" : "Switch to Dark Theme", null);
        themeToggleItem.addActionListener(e -> toggleTheme());

        settingsMenu.add(themeToggleItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(modelsMenu);
        menuBar.add(viewMenu);
        menuBar.add(settingsMenu);

        setJMenuBar(menuBar);
    }

    private void initKeyboardShortcuts() {   //горячие клавиши, вроде все работают
        InputMap inputMap = viewerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = viewerPanel.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("ctrl EQUALS"), "zoomIn");
        inputMap.put(KeyStroke.getKeyStroke("ctrl PLUS"), "zoomIn");
        actionMap.put("zoomIn", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                zoom(1.2f);
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ctrl MINUS"), "zoomOut");
        actionMap.put("zoomOut", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                zoom(0.8f);
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ctrl 0"), "resetView");
        actionMap.put("resetView", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                resetView();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ctrl T"), "transform");
        actionMap.put("transform", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showTransformDialog();
            }
        });
    }

    private JMenu createBlackTextMenu(String text, Color backgroundColor) {
        JMenu menu = new JMenu(text);
        menu.setForeground(Color.BLACK);
        menu.setBackground(backgroundColor);
        menu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        menu.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        menu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                menu.setBackground(new Color(240, 240, 240));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                menu.setBackground(backgroundColor);
            }
        });

        return menu;
    }

    private JMenuItem createBlackTextMenuItem(String text, String accelerator) {
        JMenuItem item = new JMenuItem(text);
        item.setForeground(Color.BLACK);
        item.setBackground(new Color(245, 245, 245));
        item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        item.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        if (accelerator != null) {
            item.setAccelerator(KeyStroke.getKeyStroke(accelerator));
        }

        item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                item.setBackground(new Color(230, 230, 230));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                item.setBackground(new Color(245, 245, 245));
            }
        });

        return item;
    }

    private void loadModel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "OBJ Files", "obj"));
        fileChooser.setDialogTitle("Load OBJ Model");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                Model model = sceneController.loadModel(file.getAbsolutePath());
                modelTree.updateTree();
                viewerPanel.repaint();
                updateModelSelector();
                showNotification("Model loaded: " + model.getName());
            } catch (Exception e) {
                ErrorDialog.showError("Failed to load file: " + e.getMessage(), "Load Error");
            }
        }
    }

    private void saveModel() {
        if (sceneController.getActiveModel() == null) {
            ErrorDialog.showWarning("No active model to save", "Save Error");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "OBJ Files", "obj"));
        fileChooser.setDialogTitle("Save Model As");
        fileChooser.setSelectedFile(new File(sceneController.getActiveModel().getName() + ".obj"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                sceneController.saveActiveModel(file.getAbsolutePath());
                showNotification("Model saved: " + file.getName());
            } catch (Exception e) {
                ErrorDialog.showError("Failed to save file: " + e.getMessage(), "Save Error");
            }
        }
    }

    private void addExampleModel() {
        try {
            String cubeObj =
                    "# Example Cube\n" +
                            "v -1.0 -1.0 -1.0\n" +
                            "v  1.0 -1.0 -1.0\n" +
                            "v  1.0  1.0 -1.0\n" +
                            "v -1.0  1.0 -1.0\n" +
                            "v -1.0 -1.0  1.0\n" +
                            "v  1.0 -1.0  1.0\n" +
                            "v  1.0  1.0  1.0\n" +
                            "v -1.0  1.0  1.0\n" +
                            "\n" +
                            "f 1 2 3 4\n" +
                            "f 5 8 7 6\n" +
                            "f 1 5 6 2\n" +
                            "f 2 6 7 3\n" +
                            "f 3 7 8 4\n" +
                            "f 5 1 4 8\n";

            Model cube = ObjReader.read(cubeObj, "Example Cube");
            sceneController.addModel(cube);
            modelTree.updateTree();
            viewerPanel.repaint();
            updateModelSelector();
            showNotification("Example cube added");
        } catch (Exception e) {
            ErrorDialog.showError("Failed to add example: " + e.getMessage(), "Error");
        }
    }

    private void removeSelectedModel() {
        int selectedIndex = modelSelector.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < sceneController.getAllModels().size()) {
            if (ErrorDialog.confirmAction(
                    "Delete selected model?",
                    "Confirm Delete")) {

                sceneController.removeModel(selectedIndex);
                modelTree.updateTree();
                viewerPanel.repaint();
                updateModelSelector();
                showNotification("Model deleted");
            }
        }
    }

    private void deleteSelectedVertex() {
        ModelController controller = sceneController.getActiveModelController();
        if (controller != null) {
            int selectedVertex = controller.getSelectedVertex();
            if (selectedVertex != -1) {
                if (ErrorDialog.confirmAction(
                        "Delete vertex " + selectedVertex + "?",
                        "Confirm Delete")) {

                    controller.deleteSelectedVertex();
                    modelTree.updateTree();
                    viewerPanel.repaint();
                    updateModelInfo();
                    showNotification("Vertex deleted");
                }
            } else {
                ErrorDialog.showWarning("No vertex selected. Click on a vertex to select it first.", "Delete Error");
            }
        } else {
            ErrorDialog.showWarning("No active model", "Delete Error");
        }
    }

    private void deleteSelectedPolygon() {
        ModelController controller = sceneController.getActiveModelController();
        if (controller != null) {
            int selectedPolygon = controller.getSelectedPolygon();
            if (selectedPolygon != -1) {
                if (ErrorDialog.confirmAction(
                        "Delete polygon " + selectedPolygon + "?",
                        "Confirm Delete")) {

                    controller.deleteSelectedPolygon();
                    modelTree.updateTree();
                    viewerPanel.repaint();
                    updateModelInfo();
                    showNotification("Polygon deleted");
                }
            } else {
                ErrorDialog.showWarning("No polygon selected. Ctrl+Click on a polygon to select it first.", "Delete Error");
            }
        } else {
            ErrorDialog.showWarning("No active model", "Delete Error");
        }
    }

    private void clearSelection() {
        ModelController controller = sceneController.getActiveModelController();
        if (controller != null) {
            controller.clearSelection();
            viewerPanel.repaint();
            updateModelInfo();
            showNotification("Selection cleared");
        }
    }

    private void clearAllModels() {
        if (!sceneController.getAllModels().isEmpty()) {
            if (ErrorDialog.confirmAction(
                    "Delete all models? This cannot be undone.",
                    "Confirm Delete All")) {

                while (!sceneController.getAllModels().isEmpty()) {
                    sceneController.removeModel(0);
                }
                modelTree.updateTree();
                viewerPanel.repaint();
                updateModelSelector();
                showNotification("All models cleared");
            }
        }
    }

    private void zoom(float factor) { //увеличение/уменьшение
        if (sceneController.getActiveModelController() != null && sceneController.getActiveModel() != null) {
            sceneController.getActiveModelController().scale(factor, factor, factor);
            viewerPanel.repaint();
            modelTree.updateTree();
            showNotification(factor > 1 ? "Zoomed in" : "Zoomed out");
        } else {
            showNotification("No active model to zoom");
        }
    }

    private void rotateModel(float angleX, float angleY, float angleZ) {
        if (sceneController.getActiveModelController() != null && sceneController.getActiveModel() != null) {
            if (angleX != 0) sceneController.getActiveModelController().rotateX((float)Math.toRadians(angleX));
            if (angleY != 0) sceneController.getActiveModelController().rotateY((float)Math.toRadians(angleY));
            if (angleZ != 0) sceneController.getActiveModelController().rotateZ((float)Math.toRadians(angleZ));

            viewerPanel.repaint();
            modelTree.updateTree();
            showNotification(String.format("Model rotated: X=%.1f°, Y=%.1f°, Z=%.1f°", angleX, angleY, angleZ));
        } else {
            showNotification("No active model to rotate");
        }
    }

    private void resetView() {
        if (sceneController.getActiveModelController() != null && sceneController.getActiveModel() != null) {
            viewerPanel.repaint();
            showNotification("View reset");
        }
    }

    private void toggleTheme() {
        if (darkMode) {
            applyLightTheme();
            themeToggleItem.setText("Switch to Dark Theme");
            showNotification("Switched to Light Theme");
        } else {
            applyDarkTheme();
            themeToggleItem.setText("Switch to Light Theme");
            showNotification("Switched to Dark Theme");
        }

        initMenu();
        repaint();
    }

    private void updateModelSelector() {
        modelSelector.removeAllItems();
        List<Model> models = sceneController.getAllModels();

        if (models.isEmpty()) {
            modelSelector.addItem("No models");
            modelSelector.setEnabled(false);
            modelInfoLabel.setText("No models loaded");
        } else {
            modelSelector.setEnabled(true);
            for (Model model : models) {
                modelSelector.addItem(model.getName() + " (" + model.getVertexCount() + " vertices)");
            }

            Model activeModel = sceneController.getActiveModel();
            int activeIndex = sceneController.getActiveModelIndex();

            if (activeModel != null && activeIndex >= 0 && activeIndex < modelSelector.getItemCount()) {
                modelSelector.setSelectedIndex(activeIndex);
            } else if (modelSelector.getItemCount() > 0) {
                modelSelector.setSelectedIndex(0);
                sceneController.setActiveModel(0);
            }

            updateModelInfo();
        }
    }

    private void updateModelInfo() {
        Model activeModel = sceneController.getActiveModel();
        if (activeModel != null) {
            modelInfoLabel.setText(String.format("Vertices: %d | Polygons: %d",
                    activeModel.getVertexCount(), activeModel.getPolygonCount()));
        } else {
            modelInfoLabel.setText("No active model");
        }
    };

     void showNotification(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);

            Timer timer = new Timer(3000, e -> {
                statusLabel.setText("Ready");
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    public ModelTree getModelTree() {
        return modelTree;
    }

    private JPopupMenu createTransformMenu() {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(darkMode ? new Color(50, 53, 55) : Color.WHITE);

        JMenuItem stretchXItem = new JMenuItem("Stretch X (150%)");
        JMenuItem stretchYItem = new JMenuItem("Stretch Y (150%)");
        JMenuItem stretchZItem = new JMenuItem("Stretch Z (150%)");
        menu.addSeparator();
        JMenuItem shrinkXItem = new JMenuItem("Shrink X (50%)");
        JMenuItem shrinkYItem = new JMenuItem("Shrink Y (50%)");
        JMenuItem shrinkZItem = new JMenuItem("Shrink Z (50%)");
        menu.addSeparator();
        JMenuItem customTransformItem = new JMenuItem("Custom Transformation...");

        stretchXItem.addActionListener(e -> transformModel(1.5f, 1.0f, 1.0f, 0, 0, 0));
        stretchYItem.addActionListener(e -> transformModel(1.0f, 1.5f, 1.0f, 0, 0, 0));
        stretchZItem.addActionListener(e -> transformModel(1.0f, 1.0f, 1.5f, 0, 0, 0));
        shrinkXItem.addActionListener(e -> transformModel(0.5f, 1.0f, 1.0f, 0, 0, 0));
        shrinkYItem.addActionListener(e -> transformModel(1.0f, 0.5f, 1.0f, 0, 0, 0));
        shrinkZItem.addActionListener(e -> transformModel(1.0f, 1.0f, 0.5f, 0, 0, 0));
        customTransformItem.addActionListener(e -> showTransformDialog());

        menu.add(stretchXItem);
        menu.add(stretchYItem);
        menu.add(stretchZItem);
        menu.addSeparator();
        menu.add(shrinkXItem);
        menu.add(shrinkYItem);
        menu.add(shrinkZItem);
        menu.addSeparator();
        menu.add(customTransformItem);

        return menu;
    }

    private void transformModel(float scaleX, float scaleY, float scaleZ,
                                float shearXY, float shearXZ, float shearYZ) {
        ModelController controller = sceneController.getActiveModelController();
        if (controller != null) {
            controller.transform(scaleX, scaleY, scaleZ, shearXY, shearXZ, shearYZ);
            viewerPanel.repaint();
            modelTree.updateTree();
            updateModelInfo();

            String message = "Model transformed: ";
            if (scaleX != 1.0f) message += String.format("X=%.1fx ", scaleX);
            if (scaleY != 1.0f) message += String.format("Y=%.1fx ", scaleY);
            if (scaleZ != 1.0f) message += String.format("Z=%.1fx ", scaleZ);
            if (shearXY != 0.0f) message += String.format("XY shear=%.2f ", shearXY);
            if (shearXZ != 0.0f) message += String.format("XZ shear=%.2f ", shearXZ);
            if (shearYZ != 0.0f) message += String.format("YZ shear=%.2f ", shearYZ);

            showNotification(message.trim());
        } else {
            ErrorDialog.showWarning("No active model to transform", "Transform Error");
        }
    }

    private void showTransformDialog() {
        if (sceneController.getActiveModel() == null) {
            ErrorDialog.showWarning("No active model to transform", "Transform Error");
            return;
        }

        JPanel panel = new JPanel(new GridLayout(7, 2, 5, 5));
        panel.setBackground(darkMode ? new Color(60, 63, 65) : Color.WHITE);

        JTextField scaleXField = new JTextField("1.0", 5);
        JTextField scaleYField = new JTextField("1.0", 5);
        JTextField scaleZField = new JTextField("1.0", 5);
        JTextField shearXYField = new JTextField("0.0", 5);
        JTextField shearXZField = new JTextField("0.0", 5);
        JTextField shearYZField = new JTextField("0.0", 5);

        panel.add(new JLabel("Scale X:"));
        panel.add(scaleXField);
        panel.add(new JLabel("Scale Y:"));
        panel.add(scaleYField);
        panel.add(new JLabel("Scale Z:"));
        panel.add(scaleZField);
        panel.add(new JLabel("Shear XY:"));
        panel.add(shearXYField);
        panel.add(new JLabel("Shear XZ:"));
        panel.add(shearXZField);
        panel.add(new JLabel("Shear YZ:"));
        panel.add(shearYZField);

        panel.add(new JLabel(""));
        JButton applyBtn = new JButton("Apply Transformation");
        panel.add(applyBtn);

        JDialog dialog = new JDialog(this, "Custom Transformation", true);
        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);

        JLabel hintLabel = new JLabel(
                "<html><font color='" + (darkMode ? "#CCCCCC" : "#555555") +
                        "'>Scale: 1.0 = no change, 2.0 = double, 0.5 = half<br>" +
                        "Shear: 0.0 = no shear, 0.5 = moderate shear</font></html>"
        );
        hintLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialog.add(hintLabel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);

        applyBtn.addActionListener(e -> {
            try {
                float scaleX = Float.parseFloat(scaleXField.getText());
                float scaleY = Float.parseFloat(scaleYField.getText());
                float scaleZ = Float.parseFloat(scaleZField.getText());
                float shearXY = Float.parseFloat(shearXYField.getText());
                float shearXZ = Float.parseFloat(shearXZField.getText());
                float shearYZ = Float.parseFloat(shearYZField.getText());

                if (scaleX <= 0 || scaleY <= 0 || scaleZ <= 0) {
                    ErrorDialog.showError("Scale factors must be positive numbers", "Input Error");
                    return;
                }

                transformModel(scaleX, scaleY, scaleZ, shearXY, shearXZ, shearYZ);
                dialog.dispose();
            } catch (NumberFormatException ex) {
                ErrorDialog.showError("Please enter valid numbers", "Input Error");
            }
        });

        dialog.setVisible(true);
    }
}