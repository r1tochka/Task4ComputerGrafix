package modelviewer.render;

import modelviewer.model.*;
import java.awt.*;
import java.util.List;

public class SimpleRender {
    private static boolean darkMode = true;

    public static void setDarkMode(boolean darkMode) {
        SimpleRender.darkMode = darkMode;
    }

    public static void renderModel(Graphics2D g2d, Model model, int width, int height) {
        if (model == null || model.getVertices().isEmpty()) {
            return;
        }

        List<Vector3f> vertices = model.getVertices();
        List<modelviewer.model.Polygon> polygons = model.getPolygons();

        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;  //границы модели, использ в нескольких местах
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

        for (Vector3f vertex : vertices) {
            minX = Math.min(minX, vertex.x);
            maxX = Math.max(maxX, vertex.x);
            minY = Math.min(minY, vertex.y);
            maxY = Math.max(maxY, vertex.y);
        }

        float scaleX = (width - 40) / Math.max(maxX - minX, 1.0f);
        float scaleY = (height - 40) / Math.max(maxY - minY, 1.0f);
        float scale = Math.min(scaleX, scaleY) * 0.8f;

        float offsetX = width / 2.0f - (minX + maxX) * scale / 2.0f;
        float offsetY = height / 2.0f - (minY + maxY) * scale / 2.0f;

        //цвет модельки
        Color polygonColor = darkMode ? new Color(100, 150, 255, 100) : new Color(70, 130, 180, 100);
        Color outlineColor = darkMode ? new Color(80, 130, 210) : new Color(30, 60, 90);
        Color vertexColor = darkMode ? new Color(255, 100, 100) : Color.RED;

        g2d.setColor(polygonColor);
        g2d.setStroke(new BasicStroke(1.5f));

        for (modelviewer.model.Polygon polygon : polygons) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            if (vertexIndices.size() >= 3) {
                int[] xPoints = new int[vertexIndices.size()];
                int[] yPoints = new int[vertexIndices.size()];

                for (int i = 0; i < vertexIndices.size(); i++) {
                    Vector3f vertex = vertices.get(vertexIndices.get(i));
                    xPoints[i] = (int)(vertex.x * scale + offsetX);
                    yPoints[i] = (int)(vertex.y * scale + offsetY);
                }

                g2d.fillPolygon(xPoints, yPoints, vertexIndices.size());
                g2d.setColor(outlineColor); // Более темный для контура
                g2d.drawPolygon(xPoints, yPoints, vertexIndices.size());
                g2d.setColor(polygonColor);
            }
        }

        g2d.setColor(vertexColor);  //вершины
        for (Vector3f vertex : vertices) {
            int x = (int)(vertex.x * scale + offsetX);
            int y = (int)(vertex.y * scale + offsetY);
            g2d.fillOval(x - 3, y - 3, 6, 6);
            g2d.setColor(Color.WHITE);
            g2d.drawOval(x - 3, y - 3, 6, 6);
            g2d.setColor(vertexColor);
        }
    }
}