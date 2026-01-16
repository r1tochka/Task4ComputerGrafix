package modelviewer.io;

import modelviewer.model.*;
import java.util.*;

public class ObjReader {

    public static Model read(String fileContent, String fileName) throws ObjReaderException {
        Model model = new Model(fileName);

        Scanner scanner = new Scanner(fileContent);
        scanner.useLocale(Locale.ROOT);

        int lineIndex = 1;

        try {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    lineIndex++;
                    continue;
                }

                String[] tokens = line.split("\\s+");
                if (tokens.length == 0) {
                    lineIndex++;
                    continue;
                }

                String command = tokens[0];
                String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

                switch (command) {
                    case "v":
                        model.addVertex(parseVertex(args, lineIndex));
                        break;
                    case "vt":
                        model.addTextureVertex(parseTextureVertex(args, lineIndex));
                        break;
                    case "vn":
                        model.addNormal(parseNormal(args, lineIndex));
                        break;
                    case "f":
                        model.addPolygon(parsePolygon(args, model, lineIndex));
                        break;
                    default:
                        // Игнорируем неизвестные команды
                        break;
                }

                lineIndex++;
            }
        } catch (Exception e) {
            if (e instanceof ObjReaderException) {
                throw (ObjReaderException) e;
            } else {
                throw new ObjReaderException("Error on line " + lineIndex + ": " + e.getMessage(), lineIndex);
            }
        } finally {
            scanner.close();
        }

        if (model.getVertices().isEmpty()) {
            throw new ObjReaderException("Model contains no vertices", 0);
        }

        return model;
    }

    private static Vector3f parseVertex(String[] args, int lineIndex) throws ObjReaderException {
        if (args.length < 3) {
            throw new ObjReaderException("Vertex requires at least 3 coordinates", lineIndex);
        }

        try {
            float x = Float.parseFloat(args[0]);
            float y = Float.parseFloat(args[1]);
            float z = Float.parseFloat(args[2]);
            return new Vector3f(x, y, z);
        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid number format in vertex definition: " + e.getMessage(), lineIndex);
        }
    }

    private static Vector2f parseTextureVertex(String[] args, int lineIndex) throws ObjReaderException {
        if (args.length < 2) {
            throw new ObjReaderException("Texture vertex requires at least 2 coordinates", lineIndex);
        }

        try {
            float u = Float.parseFloat(args[0]);
            float v = Float.parseFloat(args[1]);
            return new Vector2f(u, v);
        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid number format in texture vertex definition: " + e.getMessage(), lineIndex);
        }
    }

    private static Vector3f parseNormal(String[] args, int lineIndex) throws ObjReaderException {
        if (args.length < 3) {
            throw new ObjReaderException("Normal requires 3 coordinates", lineIndex);
        }

        try {
            float x = Float.parseFloat(args[0]);
            float y = Float.parseFloat(args[1]);
            float z = Float.parseFloat(args[2]);
            return new Vector3f(x, y, z);
        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid number format in normal definition: " + e.getMessage(), lineIndex);
        }
    }

    private static Polygon parsePolygon(String[] args, Model model, int lineIndex) throws ObjReaderException {
        if (args.length < 3) {
            throw new ObjReaderException("Polygon must have at least 3 vertices", lineIndex);
        }

        Polygon polygon = new Polygon();
        List<Integer> vertexIndices = new ArrayList<>();
        List<Integer> textureIndices = new ArrayList<>();
        List<Integer> normalIndices = new ArrayList<>();

        for (String arg : args) {
            parseFaceElement(arg, vertexIndices, textureIndices, normalIndices, model, lineIndex);
        }

        polygon.setVertexIndices(vertexIndices);
        if (!textureIndices.isEmpty()) {
            polygon.setTextureVertexIndices(textureIndices);
        }
        if (!normalIndices.isEmpty()) {
            polygon.setNormalIndices(normalIndices);
        }

        return polygon;
    }

    private static void parseFaceElement(String element,
                                         List<Integer> vertexIndices,
                                         List<Integer> textureIndices,
                                         List<Integer> normalIndices,
                                         Model model, int lineIndex) throws ObjReaderException {
        String[] parts = element.split("/");

        if (parts.length == 0 || parts[0].isEmpty()) {
            throw new ObjReaderException("Invalid face element format: " + element, lineIndex);
        }

        try {
            int vIdx = parseIndex(parts[0], model.getVertices().size(), lineIndex);
            vertexIndices.add(vIdx);

            if (parts.length > 1 && !parts[1].isEmpty()) {
                int vtIdx = parseIndex(parts[1], model.getTextureVertices().size(), lineIndex);
                textureIndices.add(vtIdx);
            }

            if (parts.length > 2 && !parts[2].isEmpty()) {
                int vnIdx = parseIndex(parts[2], model.getNormals().size(), lineIndex);
                normalIndices.add(vnIdx);
            }
        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid number in face element: " + element + " - " + e.getMessage(), lineIndex);
        }
    }

    private static int parseIndex(String indexStr, int listSize, int lineIndex) throws ObjReaderException {
        try {
            int idx = Integer.parseInt(indexStr);

            if (idx == 0) {
                throw new ObjReaderException("Zero index is not allowed in OBJ format", lineIndex);
            }

            int actualIdx;
            if (idx > 0) {
                actualIdx = idx - 1;
            } else {
                actualIdx = listSize + idx;
            }

            if (actualIdx < 0 || actualIdx >= listSize) {
                throw new ObjReaderException("Index " + idx + " is out of bounds. List size: " + listSize, lineIndex);
            }

            return actualIdx;
        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid index format: " + indexStr, lineIndex);
        }
    }
}