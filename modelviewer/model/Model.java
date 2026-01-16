package modelviewer.model;

import modelviewer.io.ObjReaderException;
import java.util.ArrayList;
import java.util.List;

public class Model {
    private String name;
    private List<Vector3f> vertices;
    private List<Vector2f> textureVertices;
    private List<Vector3f> normals;
    private List<Polygon> polygons;

    public Model() {
        this("Unnamed Model");
    }

    public Model(String name) {
        this.name = name;
        vertices = new ArrayList<>();
        textureVertices = new ArrayList<>();
        normals = new ArrayList<>();
        polygons = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Vector3f> getVertices() {
        return new ArrayList<>(vertices);
    }

    public List<Vector2f> getTextureVertices() {
        return new ArrayList<>(textureVertices);
    }

    public List<Vector3f> getNormals() {
        return new ArrayList<>(normals);
    }

    public List<Polygon> getPolygons() {
        return new ArrayList<>(polygons);
    }

    public void setVertices(List<Vector3f> vertices) {
        this.vertices = new ArrayList<>(vertices);
    }

    public void setTextureVertices(List<Vector2f> textureVertices) {
        this.textureVertices = new ArrayList<>(textureVertices);
    }

    public void setNormals(List<Vector3f> normals) {
        this.normals = new ArrayList<>(normals);
    }

    public void setPolygons(List<Polygon> polygons) {
        this.polygons = new ArrayList<>(polygons);
    }

    public void addVertex(Vector3f vertex) {
        vertices.add(vertex);
    }

    public void addTextureVertex(Vector2f texVertex) {
        textureVertices.add(texVertex);
    }

    public void addNormal(Vector3f normal) {
        normals.add(normal);
    }

    public void addPolygon(Polygon polygon) {
        polygons.add(polygon);
    }

    //удаление
    public void removeVertex(int index) {
        if (index >= 0 && index < vertices.size()) {
            vertices.remove(index);
            try {
                updateIndicesAfterVertexRemoval(index);
            } catch (Exception e) {
                System.err.println("Error updating indices: " + e.getMessage());
            }
        }
    }

    public void removePolygon(int index) {
        if (index >= 0 && index < polygons.size()) {
            polygons.remove(index);
        }
    }

    private void updateIndicesAfterVertexRemoval(int removedIndex) throws Exception {
        List<Polygon> polygonsToRemove = new ArrayList<>();

        for (Polygon polygon : polygons) {
            List<Integer> newVertexIndices = new ArrayList<>();
            List<Integer> vertexIndices = polygon.getVertexIndices();

            for (int idx : vertexIndices) {
                if (idx > removedIndex) {
                    newVertexIndices.add(idx - 1);
                } else if (idx < removedIndex) {
                    newVertexIndices.add(idx);
                }
            }

            if (newVertexIndices.size() >= 3) {
                polygon.setVertexIndices(newVertexIndices);
            } else {
                polygonsToRemove.add(polygon);
            }
        }

        polygons.removeAll(polygonsToRemove);
    }

    public int getVertexCount() {
        return vertices.size();
    }

    public int getPolygonCount() {
        return polygons.size();
    }
}