package modelviewer.controller;

import modelviewer.model.Model;
import modelviewer.model.Polygon;
import modelviewer.model.Vector3f;
import java.util.List;
import java.util.ArrayList;

public class ModelController {
    private Model model;
    private int selectedVertex = -1;
    private int selectedPolygon = -1;

    public ModelController(Model model) {
        this.model = model;
    }

    public Model getModel() {
        return model;
    }

    //удаление
    public void deleteSelectedVertex() {
        if (selectedVertex != -1) {
            model.removeVertex(selectedVertex);
            selectedVertex = -1;
        }
    }

    public void deleteSelectedPolygon() {
        if (selectedPolygon != -1) {
            model.removePolygon(selectedPolygon);
            selectedPolygon = -1;
        }
    }

    //выделение
    public boolean selectVertexAt(int screenX, int screenY, int panelWidth, int panelHeight) {
        List<Vector3f> vertices = model.getVertices();
        if (vertices.isEmpty()) return false;

        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

        for (Vector3f vertex : vertices) {
            minX = Math.min(minX, vertex.x);
            maxX = Math.max(maxX, vertex.x);
            minY = Math.min(minY, vertex.y);
            maxY = Math.max(maxY, vertex.y);
        }

        //масштаб
        float scaleX = (panelWidth - 40) / Math.max(maxX - minX, 1.0f);
        float scaleY = (panelHeight - 40) / Math.max(maxY - minY, 1.0f);
        float scale = Math.min(scaleX, scaleY) * 0.8f;

        float offsetX = panelWidth / 2.0f - (minX + maxX) * scale / 2.0f;
        float offsetY = panelHeight / 2.0f - (minY + maxY) * scale / 2.0f;

        int closestVertex = -1;
        double minDistance = 10.0;

        for (int i = 0; i < vertices.size(); i++) {
            Vector3f vertex = vertices.get(i);
            int vx = (int)(vertex.x * scale + offsetX);
            int vy = (int)(vertex.y * scale + offsetY);

            double distance = Math.sqrt(Math.pow(vx - screenX, 2) + Math.pow(vy - screenY, 2));

            if (distance < minDistance) {
                minDistance = distance;
                closestVertex = i;
            }
        }

        if (closestVertex != -1) {
            selectedVertex = closestVertex;
            selectedPolygon = -1; // Сбрасываем выбор полигона
            return true;
        }

        return false;
    }

    public boolean selectPolygonAt(int screenX, int screenY, int panelWidth, int panelHeight) {
        List<Vector3f> vertices = model.getVertices();
        List<Polygon> polygons = model.getPolygons();
        if (vertices.isEmpty() || polygons.isEmpty()) return false;

        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

        for (Vector3f vertex : vertices) {
            minX = Math.min(minX, vertex.x);
            maxX = Math.max(maxX, vertex.x);
            minY = Math.min(minY, vertex.y);
            maxY = Math.max(maxY, vertex.y);
        }

        float scaleX = (panelWidth - 40) / Math.max(maxX - minX, 1.0f);
        float scaleY = (panelHeight - 40) / Math.max(maxY - minY, 1.0f);
        float scale = Math.min(scaleX, scaleY) * 0.8f;

        float offsetX = panelWidth / 2.0f - (minX + maxX) * scale / 2.0f;
        float offsetY = panelHeight / 2.0f - (minY + maxY) * scale / 2.0f;

        for (int polyIndex = 0; polyIndex < polygons.size(); polyIndex++) {
            Polygon polygon = polygons.get(polyIndex);
            List<Integer> vertexIndices = polygon.getVertexIndices();

            if (vertexIndices.size() >= 3) {
                int[] xPoints = new int[vertexIndices.size()];
                int[] yPoints = new int[vertexIndices.size()];

                for (int i = 0; i < vertexIndices.size(); i++) {
                    Vector3f vertex = vertices.get(vertexIndices.get(i));
                    xPoints[i] = (int)(vertex.x * scale + offsetX);
                    yPoints[i] = (int)(vertex.y * scale + offsetY);
                }

                java.awt.Polygon awtPolygon = new java.awt.Polygon(xPoints, yPoints, vertexIndices.size());
                if (awtPolygon.contains(screenX, screenY)) {
                    selectedPolygon = polyIndex;
                    selectedVertex = -1;
                    return true;
                }
            }
        }

        return false;
    }

    public int getSelectedVertex() {
        return selectedVertex;
    }

    public int getSelectedPolygon() {
        return selectedPolygon;
    }

    public void clearSelection() {
        selectedVertex = -1;
        selectedPolygon = -1;
    }

    /*public void translate(float dx, float dy, float dz) {  //перемещение, но оно не сработало (курсор меняется,
        List<Vector3f> vertices = model.getVertices();     //но перемещение не происходит, не получилось)
        List<Vector3f> newVertices = new ArrayList<>();

        for (Vector3f vertex : vertices) {
            newVertices.add(new Vector3f(
                    vertex.x + dx,
                    vertex.y + dy,
                    vertex.z + dz
            ));
        }
        model.setVertices(newVertices);
    }*/

    public void scale(float sx, float sy, float sz) {   //масштаб
        List<Vector3f> vertices = model.getVertices();
        List<Vector3f> newVertices = new ArrayList<>();

        for (Vector3f vertex : vertices) {
            newVertices.add(new Vector3f(
                    vertex.x * sx,
                    vertex.y * sy,
                    vertex.z * sz
            ));
        }
        model.setVertices(newVertices);
    }

    public void rotateX(float angle) {  //вращение
        float cos = (float)Math.cos(angle);
        float sin = (float)Math.sin(angle);

        List<Vector3f> vertices = model.getVertices();
        List<Vector3f> newVertices = new ArrayList<>();

        for (Vector3f vertex : vertices) {
            float y = vertex.y * cos - vertex.z * sin;
            float z = vertex.y * sin + vertex.z * cos;
            newVertices.add(new Vector3f(vertex.x, y, z));
        }
        model.setVertices(newVertices);
    }

    public void rotateY(float angle) {
        float cos = (float)Math.cos(angle);
        float sin = (float)Math.sin(angle);

        List<Vector3f> vertices = model.getVertices();
        List<Vector3f> newVertices = new ArrayList<>();

        for (Vector3f vertex : vertices) {
            float x = vertex.x * cos + vertex.z * sin;
            float z = -vertex.x * sin + vertex.z * cos;
            newVertices.add(new Vector3f(x, vertex.y, z));
        }
        model.setVertices(newVertices);
    }

    public void rotateZ(float angle) {
        float cos = (float)Math.cos(angle);
        float sin = (float)Math.sin(angle);

        List<Vector3f> vertices = model.getVertices();
        List<Vector3f> newVertices = new ArrayList<>();

        for (Vector3f vertex : vertices) {
            float x = vertex.x * cos - vertex.y * sin;
            float y = vertex.x * sin + vertex.y * cos;
            newVertices.add(new Vector3f(x, y, vertex.z));
        }
        model.setVertices(newVertices);
    }

    public String getModelInfo() {
        return String.format("Model: %s | Vertices: %d | Polygons: %d",
                model.getName(),
                model.getVertexCount(),
                model.getPolygonCount());
    }

    public boolean hasSelection() {
        return selectedVertex != -1 || selectedPolygon != -1;
    }

    public String getSelectionType() {
        if (selectedVertex != -1) return "Vertex";
        if (selectedPolygon != -1) return "Polygon";
        return "None";
    }

    public int getSelectionIndex() {
        if (selectedVertex != -1) return selectedVertex;
        if (selectedPolygon != -1) return selectedPolygon;
        return -1;
    }


    public void scaleX(float factor) {
        List<Vector3f> vertices = model.getVertices();
        List<Vector3f> newVertices = new ArrayList<>();

        for (Vector3f vertex : vertices) {
            newVertices.add(new Vector3f(
                    vertex.x * factor,
                    vertex.y,
                    vertex.z
            ));
        }
        model.setVertices(newVertices);
    }

    public void scaleY(float factor) {
        List<Vector3f> vertices = model.getVertices();
        List<Vector3f> newVertices = new ArrayList<>();

        for (Vector3f vertex : vertices) {
            newVertices.add(new Vector3f(
                    vertex.x,
                    vertex.y * factor,
                    vertex.z
            ));
        }
        model.setVertices(newVertices);
    }

    public void scaleZ(float factor) {
        List<Vector3f> vertices = model.getVertices();
        List<Vector3f> newVertices = new ArrayList<>();

        for (Vector3f vertex : vertices) {
            newVertices.add(new Vector3f(
                    vertex.x,
                    vertex.y,
                    vertex.z * factor
            ));
        }
        model.setVertices(newVertices);
    }

    public void shearXY(float factor) {  //равномерное растяжение
        List<Vector3f> vertices = model.getVertices();
        List<Vector3f> newVertices = new ArrayList<>();

        for (Vector3f vertex : vertices) {
            newVertices.add(new Vector3f(
                    vertex.x + vertex.y * factor,
                    vertex.y,
                    vertex.z
            ));
        }
        model.setVertices(newVertices);
    }

    public void shearXZ(float factor) {
        List<Vector3f> vertices = model.getVertices();
        List<Vector3f> newVertices = new ArrayList<>();

        for (Vector3f vertex : vertices) {
            newVertices.add(new Vector3f(
                    vertex.x + vertex.z * factor,
                    vertex.y,
                    vertex.z
            ));
        }
        model.setVertices(newVertices);
    }

    public void shearYZ(float factor) {
        List<Vector3f> vertices = model.getVertices();
        List<Vector3f> newVertices = new ArrayList<>();

        for (Vector3f vertex : vertices) {
            newVertices.add(new Vector3f(
                    vertex.x,
                    vertex.y + vertex.z * factor,
                    vertex.z
            ));
        }
        model.setVertices(newVertices);
    }

    public void transform(float scaleX, float scaleY, float scaleZ,
                          float shearXY, float shearXZ, float shearYZ) {
        List<Vector3f> vertices = model.getVertices();
        List<Vector3f> newVertices = new ArrayList<>();

        for (Vector3f vertex : vertices) {
            float x = vertex.x * scaleX + vertex.y * shearXY + vertex.z * shearXZ;
            float y = vertex.x * shearXY + vertex.y * scaleY + vertex.z * shearYZ;
            float z = vertex.x * shearXZ + vertex.y * shearYZ + vertex.z * scaleZ;

            newVertices.add(new Vector3f(x, y, z));
        }
        model.setVertices(newVertices);
    }

}