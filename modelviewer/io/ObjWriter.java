package modelviewer.io;

import modelviewer.model.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ObjWriter {

    public static void write(String fileName, List<Vector3> vertices, List<Polygon> polygons) throws IOException {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        if (vertices == null || polygons == null) {
            throw new IllegalArgumentException("Vertex and polygon lists cannot be null");
        }

        try (FileWriter writer = new FileWriter(fileName)) {
            writeVertices(writer, vertices);
            writer.write("\n");

            List<Vector2f> textureCoords = generateTextureCoordinates(vertices);
            writeTextureVertices(writer, textureCoords);

            writer.write("\n");

            List<Vector3> normals = calculateNormals(vertices, polygons);
            writeNormals(writer, normals);

            writer.write("\n");
            writePolygons(writer, polygons);
        }
    }

    private static void writeVertices(FileWriter writer, List<Vector3> vertices) throws IOException {
        writer.write("# Vertices\n");
        for (Vector3 vertex : vertices) {
            writer.write(String.format(Locale.US, "v %.6f %.6f %.6f\n",
                    vertex.getX(), vertex.getY(), vertex.getZ()));
        }
    }

    private static void writeTextureVertices(FileWriter writer, List<Vector2f> textureCoords) throws IOException {
        writer.write("# Texture coordinates\n");
        for (Vector2f texCoord : textureCoords) {
            writer.write(String.format(Locale.US, "vt %.6f %.6f\n",
                    texCoord.x, texCoord.y));
        }
    }

    private static void writeNormals(FileWriter writer, List<Vector3> normals) throws IOException {
        writer.write("# Normals\n");
        for (Vector3 normal : normals) {
            writer.write(String.format(Locale.US, "vn %.6f %.6f %.6f\n",
                    normal.getX(), normal.getY(), normal.getZ()));
        }
    }

    private static void writePolygons(FileWriter writer, List<Polygon> polygons) throws IOException {
        writer.write("# Polygons\n");
        for (Polygon polygon : polygons) {
            writer.write("f");
            List<Integer> vertexIndices = polygon.getVertexIndices();

            for (int i = 0; i < vertexIndices.size(); i++) {
                int vertexIndex = vertexIndices.get(i) + 1; // OBJ indices start from 1
                writer.write(String.format(" %d/%d/%d", vertexIndex, vertexIndex, vertexIndex));
            }
            writer.write("\n");
        }
    }

    private static List<Vector2f> generateTextureCoordinates(List<Vector3> vertices) {
        List<Vector2f> textureCoords = new ArrayList<>();

        if (vertices.isEmpty()) {
            return textureCoords;
        }

        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

        for (Vector3 vertex : vertices) {
            minX = Math.min(minX, vertex.getX());
            maxX = Math.max(maxX, vertex.getX());
            minY = Math.min(minY, vertex.getY());
            maxY = Math.max(maxY, vertex.getY());
            minZ = Math.min(minZ, vertex.getZ());
            maxZ = Math.max(maxZ, vertex.getZ());
        }

        float rangeX = maxX - minX;
        float rangeY = maxY - minY;

        for (Vector3 vertex : vertices) {
            float u = rangeX > 0 ? (vertex.getX() - minX) / rangeX : 0.5f;
            float v = rangeY > 0 ? (vertex.getY() - minY) / rangeY : 0.5f;
            textureCoords.add(new Vector2f(u, v));
        }

        return textureCoords;
    }

    private static List<Vector3> calculateNormals(List<Vector3> vertices, List<Polygon> polygons) {
        List<Vector3> normals = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            normals.add(new Vector3(0, 0, 0));
        }

        for (Polygon polygon : polygons) {
            List<Integer> vertexIndices = polygon.getVertexIndices();

            if (vertexIndices.size() >= 3) {
                Vector3 v0 = vertices.get(vertexIndices.get(0));
                Vector3 v1 = vertices.get(vertexIndices.get(1));
                Vector3 v2 = vertices.get(vertexIndices.get(2));

                Vector3 edge1 = new Vector3(
                        v1.getX() - v0.getX(),
                        v1.getY() - v0.getY(),
                        v1.getZ() - v0.getZ()
                );

                Vector3 edge2 = new Vector3(
                        v2.getX() - v0.getX(),
                        v2.getY() - v0.getY(),
                        v2.getZ() - v0.getZ()
                );

                Vector3 normal = crossProduct(edge1, edge2);
                Vector3 normalizedNormal = normal.normalize();

                for (int vertexIndex : vertexIndices) {
                    Vector3 currentNormal = normals.get(vertexIndex);
                    Vector3 newNormal = new Vector3(
                            currentNormal.getX() + normalizedNormal.getX(),
                            currentNormal.getY() + normalizedNormal.getY(),
                            currentNormal.getZ() + normalizedNormal.getZ()
                    );
                    normals.set(vertexIndex, newNormal);
                }
            }
        }

        List<Vector3> normalizedNormals = new ArrayList<>();
        for (Vector3 normal : normals) {
            normalizedNormals.add(normal.normalize());
        }

        return normalizedNormals;
    }

    private static Vector3 crossProduct(Vector3 a, Vector3 b) {
        return new Vector3(
                a.getY() * b.getZ() - a.getZ() * b.getY(),
                a.getZ() * b.getX() - a.getX() * b.getZ(),
                a.getX() * b.getY() - a.getY() * b.getX()
        );
    }

    public static String writeToString(List<Vector3> vertices, List<Polygon> polygons) {
        if (vertices == null || polygons == null) {
            throw new IllegalArgumentException("Vertex and polygon lists cannot be null");
        }

        StringBuilder sb = new StringBuilder();

        sb.append("# Vertices\n");
        for (Vector3 vertex : vertices) {
            sb.append(String.format(Locale.US, "v %.6f %.6f %.6f\n",
                    vertex.getX(), vertex.getY(), vertex.getZ()));
        }

        List<Vector2f> textureCoords = generateTextureCoordinates(vertices);
        sb.append("\n# Texture coordinates\n");
        for (Vector2f texCoord : textureCoords) {
            sb.append(String.format(Locale.US, "vt %.6f %.6f\n",
                    texCoord.x, texCoord.y));
        }

        List<Vector3> normals = calculateNormals(vertices, polygons);
        sb.append("\n# Normals\n");
        for (Vector3 normal : normals) {
            sb.append(String.format(Locale.US, "vn %.6f %.6f %.6f\n",
                    normal.getX(), normal.getY(), normal.getZ()));
        }

        sb.append("\n# Polygons\n");
        for (Polygon polygon : polygons) {
            sb.append("f");
            List<Integer> vertexIndices = polygon.getVertexIndices();

            for (int i = 0; i < vertexIndices.size(); i++) {
                int vertexIndex = vertexIndices.get(i) + 1;
                sb.append(String.format(" %d/%d/%d", vertexIndex, vertexIndex, vertexIndex));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}