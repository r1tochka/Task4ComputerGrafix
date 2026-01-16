package modelviewer.ui;

import modelviewer.controller.SceneController;
import modelviewer.model.Model;
import modelviewer.render.SimpleRender;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ModelViewerPanel extends JPanel {
    private SceneController sceneController;
    private int selectedVertex = -1;
    private int selectedPolygon = -1;
    private boolean darkMode = true;
    //private Point lastDragPoint; //для перемещения

    public ModelViewerPanel(SceneController sceneController) {
        this.sceneController = sceneController;
        updateTheme();
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(darkMode ? new Color(100, 100, 150) : new Color(150, 150, 200), 2),
                "3D Viewer"
        ));

        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)); //был HAND_CURSOR для перетаскивания

        //клики мыши
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // lastDragPoint = e.getPoint();
                handleMouseClick(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }
        });

        /*
        //перетаскивание
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && lastDragPoint != null && !e.isControlDown()) {
                    int dx = e.getX() - lastDragPoint.x;
                    int dy = e.getY() - lastDragPoint.y;

                    lastDragPoint = e.getPoint();

                    if (sceneController.getActiveModelController() != null) {
                        float scale = 0.005f;
                        sceneController.getActiveModelController().translate(dx * scale, -dy * scale, 0);
                        repaint();

                        if (getParent() != null) {
                            Component parent = getParent();
                            while (parent != null && !(parent instanceof MainWindow)) {
                                parent = parent.getParent();
                            }
                            if (parent instanceof MainWindow) {
                                ((MainWindow) parent).showNotification(
                                        String.format("Dragging: dx=%.2f, dy=%.2f", dx * scale, -dy * scale)
                                );
                            }
                        }
                    }
                }
            }
        });
        */

        //масштаб
        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (sceneController.getActiveModelController() != null) {
                    float scale = 1.0f + e.getWheelRotation() * 0.1f;
                    sceneController.getActiveModelController().scale(scale, scale, scale);
                    repaint();

                    String direction = e.getWheelRotation() > 0 ? "out" : "in";
                    if (getParent() != null) {
                        Component parent = getParent();
                        while (parent != null && !(parent instanceof MainWindow)) {
                            parent = parent.getParent();
                        }
                        if (parent instanceof MainWindow) {
                            ((MainWindow) parent).showNotification("Zoomed " + direction);
                        }
                    }
                }
            }
        });
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        updateTheme();
        repaint();
    }

    private void updateTheme() {
        if (darkMode) {
            setBackground(new Color(40, 40, 45));
            setForeground(Color.WHITE);
        } else {
            setBackground(new Color(250, 250, 255));
            setForeground(Color.BLACK);
        }

        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(darkMode ? new Color(100, 100, 150) : new Color(150, 150, 200), 2),
                "3D Viewer"
        ));
    }

    private void handleMouseClick(MouseEvent e) {  //выбор точки или полигона
        if (sceneController.getActiveModelController() == null) return;

        if (SwingUtilities.isLeftMouseButton(e)) {
            if (e.isControlDown()) {
                if (sceneController.getActiveModelController().selectPolygonAt(
                        e.getX(), e.getY(), getWidth(), getHeight())) {

                    selectedPolygon = sceneController.getActiveModelController().getSelectedPolygon();
                    selectedVertex = -1;
                    repaint();

                    showSelectionNotification("Polygon " + selectedPolygon + " selected");
                }
            }
            else {
                if (sceneController.getActiveModelController().selectVertexAt(
                        e.getX(), e.getY(), getWidth(), getHeight())) {

                    selectedVertex = sceneController.getActiveModelController().getSelectedVertex();
                    selectedPolygon = -1;
                    repaint();

                    showSelectionNotification("Vertex " + selectedVertex + " selected");
                }
            }
        }
        else if (SwingUtilities.isRightMouseButton(e)) {
            sceneController.getActiveModelController().clearSelection();
            selectedVertex = -1;
            selectedPolygon = -1;
            repaint();
            showSelectionNotification("Selection cleared");
        }
    }

    private void showSelectionNotification(String message) {
        if (getParent() != null) {
            Component parent = getParent();
            while (parent != null && !(parent instanceof MainWindow)) {
                parent = parent.getParent();
            }
            if (parent instanceof MainWindow) {
                ((MainWindow) parent).showNotification(message);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Model activeModel = sceneController.getActiveModel();
        if (activeModel != null) {
            SimpleRender.renderModel(g2d, activeModel, getWidth(), getHeight());

            //подсветка вершины
            if (selectedVertex != -1 && selectedVertex < activeModel.getVertexCount()) {
                g2d.setColor(Color.GREEN);
                g2d.setStroke(new BasicStroke(3.0f));

                modelviewer.model.Vector3f vertex = activeModel.getVertices().get(selectedVertex);

                float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
                float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
                for (modelviewer.model.Vector3f v : activeModel.getVertices()) {
                    minX = Math.min(minX, v.x);
                    maxX = Math.max(maxX, v.x);
                    minY = Math.min(minY, v.y);
                    maxY = Math.max(maxY, v.y);
                }

                float scaleX = (getWidth() - 40) / Math.max(maxX - minX, 1.0f);
                float scaleY = (getHeight() - 40) / Math.max(maxY - minY, 1.0f);
                float scale = Math.min(scaleX, scaleY) * 0.8f;
                float offsetX = getWidth() / 2.0f - (minX + maxX) * scale / 2.0f;
                float offsetY = getHeight() / 2.0f - (minY + maxY) * scale / 2.0f;

                int x = (int)(vertex.x * scale + offsetX);
                int y = (int)(vertex.y * scale + offsetY);

                g2d.drawOval(x - 10, y - 10, 20, 20);
                g2d.setColor(new Color(0, 255, 0, 100));
                g2d.fillOval(x - 10, y - 10, 20, 20);
            }

            //подсвечивание полигона
            if (selectedPolygon != -1 && selectedPolygon < activeModel.getPolygonCount()) {
                g2d.setColor(new Color(255, 255, 0, 100));

                modelviewer.model.Polygon polygon = activeModel.getPolygons().get(selectedPolygon);
                java.util.List<Integer> vertexIndices = polygon.getVertexIndices();

                float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
                float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
                for (modelviewer.model.Vector3f v : activeModel.getVertices()) {
                    minX = Math.min(minX, v.x);
                    maxX = Math.max(maxX, v.x);
                    minY = Math.min(minY, v.y);
                    maxY = Math.max(maxY, v.y);
                }

                float scaleX = (getWidth() - 40) / Math.max(maxX - minX, 1.0f);
                float scaleY = (getHeight() - 40) / Math.max(maxY - minY, 1.0f);
                float scale = Math.min(scaleX, scaleY) * 0.8f;
                float offsetX = getWidth() / 2.0f - (minX + maxX) * scale / 2.0f;
                float offsetY = getHeight() / 2.0f - (minY + maxY) * scale / 2.0f;

                int[] xPoints = new int[vertexIndices.size()];
                int[] yPoints = new int[vertexIndices.size()];

                for (int i = 0; i < vertexIndices.size(); i++) {
                    modelviewer.model.Vector3f vertex = activeModel.getVertices().get(vertexIndices.get(i));
                    xPoints[i] = (int)(vertex.x * scale + offsetX);
                    yPoints[i] = (int)(vertex.y * scale + offsetY);
                }

                g2d.fillPolygon(xPoints, yPoints, vertexIndices.size());

                g2d.setColor(Color.YELLOW.darker());
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawPolygon(xPoints, yPoints, vertexIndices.size());
            }

            drawModelInfo(g2d, activeModel);
        } else {
            drawNoModelMessage(g2d);
        }
    }

    private void drawModelInfo(Graphics2D g2d, Model model) {
        g2d.setColor(darkMode ? new Color(220, 220, 220) : new Color(60, 60, 60));
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));

        String info = String.format("%s | Vertices: %d | Polygons: %d",
                model.getName(), model.getVertexCount(), model.getPolygonCount());

        g2d.drawString(info, 10, 20);

        if (selectedVertex != -1) {
            g2d.setColor(Color.GREEN);
            g2d.drawString("Selected Vertex: " + selectedVertex, 10, 40);
        }
        if (selectedPolygon != -1) {
            g2d.setColor(Color.YELLOW.darker());
            g2d.drawString("Selected Polygon: " + selectedPolygon, 10, 60);
        }
    }

    private void drawNoModelMessage(Graphics2D g2d) {
        g2d.setColor(darkMode ? Color.GRAY : Color.DARK_GRAY);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        String message = "No model loaded. Use File > Load OBJ...";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(message)) / 2;
        int y = getHeight() / 2;
        g2d.drawString(message, x, y);

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        String hint = "Or use Models > Add Example Model";
        int hintX = (getWidth() - g2d.getFontMetrics().stringWidth(hint)) / 2;
        g2d.drawString(hint, hintX, y + 30);
    }

    public int getSelectedVertex() {
        return selectedVertex;
    }

    public int getSelectedPolygon() {
        return selectedPolygon;
    }

    public void setSelectedVertex(int vertexIndex) {
        this.selectedVertex = vertexIndex;
        repaint();
    }

    public void setSelectedPolygon(int polygonIndex) {
        this.selectedPolygon = polygonIndex;
        repaint();
    }
}