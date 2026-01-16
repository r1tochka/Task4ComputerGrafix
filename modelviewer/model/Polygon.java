package modelviewer.model;

import modelviewer.io.ObjReaderException;
import java.util.ArrayList;
import java.util.List;

public class Polygon {
    private List<Integer> vertexIndices;
    private List<Integer> textureVertexIndices;
    private List<Integer> normalIndices;

    public Polygon() {
        vertexIndices = new ArrayList<>();
        textureVertexIndices = new ArrayList<>();
        normalIndices = new ArrayList<>();
    }

    public Polygon(List<Integer> vertexIndices) {
        this();
        this.vertexIndices.addAll(vertexIndices);
    }

    public void setVertexIndices(List<Integer> vertexIndices) throws ObjReaderException {
        if (vertexIndices.size() < 3) {
            throw new ObjReaderException("Polygon must have at least 3 vertices");
        }
        this.vertexIndices = new ArrayList<>(vertexIndices);
    }

    public void setTextureVertexIndices(List<Integer> textureVertexIndices) throws ObjReaderException {
        if (!textureVertexIndices.isEmpty() && textureVertexIndices.size() != vertexIndices.size()) {
            throw new ObjReaderException("Texture indices count must match vertex indices count");
        }
        this.textureVertexIndices = new ArrayList<>(textureVertexIndices);
    }

    public void setNormalIndices(List<Integer> normalIndices) throws ObjReaderException {
        if (!normalIndices.isEmpty() && normalIndices.size() != vertexIndices.size()) {
            throw new ObjReaderException("Normal indices count must match vertex indices count");
        }
        this.normalIndices = new ArrayList<>(normalIndices);
    }

    public List<Integer> getVertexIndices() {
        return new ArrayList<>(vertexIndices);
    }

    public List<Integer> getTextureVertexIndices() {
        return new ArrayList<>(textureVertexIndices);
    }

    public List<Integer> getNormalIndices() {
        return new ArrayList<>(normalIndices);
    }

    public void addVertexIndex(int index) {
        vertexIndices.add(index);
    }

    public int getVertexCount() {
        return vertexIndices.size();
    }

    @Override
    public String toString() {
        return "Polygon[" + vertexIndices + "]";
    }
}