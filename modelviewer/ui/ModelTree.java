package modelviewer.ui;

import modelviewer.controller.SceneController;
import modelviewer.model.Model;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ModelTree extends JPanel {
    private SceneController sceneController;
    private JTree tree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private boolean darkMode = true;

    public ModelTree(SceneController sceneController) {
        this.sceneController = sceneController;
        setLayout(new BorderLayout());
        updateTheme();

        rootNode = new DefaultMutableTreeNode("Scene");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);

        tree.setCellRenderer(new ModelTreeCellRenderer());
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);

        updateTreeColors();

        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof Model) {
                int index = getModelIndex((Model) node.getUserObject());
                if (index != -1) {
                    sceneController.setActiveModel(index);
                }
            }
        });


        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setBackground(darkMode ? new Color(50, 53, 55) : Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        updateTheme();
        updateTreeColors();
        repaint();
    }

    private void updateTheme() {
        if (darkMode) {
            setBackground(new Color(50, 53, 55));
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(80, 80, 85), 2),
                    "Models"
            ));
        } else {
            setBackground(new Color(240, 240, 245));
            setForeground(Color.BLACK);
            setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(180, 180, 185), 2),
                    "Models"
            ));
        }
    }

    private void updateTreeColors() {
        if (darkMode) {
            tree.setBackground(new Color(60, 63, 65));
            tree.setForeground(Color.WHITE);

            UIManager.put("Tree.selectionBackground", new Color(66, 165, 245));
            UIManager.put("Tree.selectionForeground", Color.WHITE);
            UIManager.put("Tree.textBackground", new Color(60, 63, 65));
            UIManager.put("Tree.textForeground", Color.WHITE);
        } else {
            tree.setBackground(Color.WHITE);
            tree.setForeground(Color.BLACK);

            UIManager.put("Tree.selectionBackground", new Color(200, 220, 255));
            UIManager.put("Tree.selectionForeground", Color.BLACK);
            UIManager.put("Tree.textBackground", Color.WHITE);
            UIManager.put("Tree.textForeground", Color.BLACK);
        }

        SwingUtilities.updateComponentTreeUI(tree);
    }

    public void updateTree() {
        rootNode.removeAllChildren();
        List<Model> models = sceneController.getAllModels();

        for (Model model : models) {
            DefaultMutableTreeNode modelNode = new DefaultMutableTreeNode(model);

            DefaultMutableTreeNode verticesNode = new DefaultMutableTreeNode("Vertices (" + model.getVertexCount() + ")");
            List<modelviewer.model.Vector3f> vertices = model.getVertices();
            for (int i = 0; i < vertices.size(); i++) {
                verticesNode.add(new DefaultMutableTreeNode("Vertex " + i + ": " + vertices.get(i)));
            }
            modelNode.add(verticesNode);

            DefaultMutableTreeNode polygonsNode = new DefaultMutableTreeNode("Polygons (" + model.getPolygonCount() + ")");
            List<modelviewer.model.Polygon> polygons = model.getPolygons();
            for (int i = 0; i < polygons.size(); i++) {
                polygonsNode.add(new DefaultMutableTreeNode("Polygon " + i + ": " + polygons.get(i)));
            }
            modelNode.add(polygonsNode);

            rootNode.add(modelNode);
        }

        treeModel.reload();

        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    private int getModelIndex(Model model) {
        List<Model> models = sceneController.getAllModels();
        for (int i = 0; i < models.size(); i++) {
            if (models.get(i) == model) {
                return i;
            }
        }
        return -1;
    }

    private class ModelTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            if (darkMode) {
                setBackgroundNonSelectionColor(new Color(60, 63, 65));
                setTextNonSelectionColor(Color.WHITE);
                setBackgroundSelectionColor(new Color(66, 165, 245));
                setTextSelectionColor(Color.WHITE);
            } else {
                setBackgroundNonSelectionColor(Color.WHITE);
                setTextNonSelectionColor(Color.BLACK);
                setBackgroundSelectionColor(new Color(200, 220, 255));
                setTextSelectionColor(Color.BLACK);
            }

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();

            if (userObject instanceof Model) {
                Model model = (Model) userObject;
                setIcon(UIManager.getIcon("FileView.fileIcon"));
                setText(model.getName());
            } else if (userObject instanceof String) {
                String text = (String) userObject;
                if (text.startsWith("Vertices") || text.startsWith("Polygons")) {
                    setIcon(UIManager.getIcon("Tree.openIcon"));
                } else if (text.startsWith("Vertex") || text.startsWith("Polygon")) {
                    setIcon(UIManager.getIcon("Tree.leafIcon"));
                }
            }

            return this;
        }
    }
}