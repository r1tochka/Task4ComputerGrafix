package com.cgvsu.model;

import com.cgvsu.math.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class ModelProcessor {

    private ModelProcessor(){}

    public static void preprocess(Model model) {
        triangulate(model);
        recalculateNormals(model);
    }


    public static void triangulate(Model model) {
        List<Polygon> originalPolygons = model.polygons;
        List<Polygon> triangulated = new ArrayList<>(originalPolygons.size());

        for (Polygon polygon : originalPolygons) {
            List<Integer> v = polygon.getVertexIndices();
            List<Integer> t = polygon.getTextureVertexIndices();


            int vertexCount = v.size();
            if (vertexCount <= 3) {
                triangulated.add(polygon);
                continue;
            }

            for (int i = 1; i < vertexCount - 1; ++i) {
                Polygon triangle = new Polygon();

                ArrayList<Integer> triV = new ArrayList<>(3);
                triV.add(v.get(0));
                triV.add(v.get(i));
                triV.add(v.get(i + 1));
                triangle.setVertexIndices(triV);

                if (!t.isEmpty()) {
                    ArrayList<Integer> triT = new ArrayList<>(3);
                    triT.add(t.get(0));
                    triT.add(t.get(i));
                    triT.add(t.get(i + 1));
                    triangle.setTextureVertexIndices(triT);
                }

                triangle.setNormalIndices(new ArrayList<>());

                triangulated.add(triangle);
            }
        }

        model.polygons = new ArrayList<>(triangulated);
    }


    public static void recalculateNormals(Model model) {
        int vertexCount = model.vertices.size();
        if (vertexCount == 0 || model.polygons.isEmpty()) {
            model.normals.clear();
            return;
        }

        Vector3f[] normalSums = new Vector3f[vertexCount];
        for (int i = 0; i < vertexCount; ++i) {
            normalSums[i] = new Vector3f(0.0f, 0.0f, 0.0f);
        }

        for (Polygon polygon : model.polygons) {
            List<Integer> vIdx = polygon.getVertexIndices();
            if (vIdx.size() < 3) {
                continue;
            }

            int i0 = vIdx.get(0);
            int i1 = vIdx.get(1);
            int i2 = vIdx.get(2);

            Vector3f p0 = model.vertices.get(i0);
            Vector3f p1 = model.vertices.get(i1);
            Vector3f p2 = model.vertices.get(i2);

            Vector3f edge1 = p1.subtract(p0);
            Vector3f edge2 = p2.subtract(p0);

            Vector3f faceNormal = edge1.cross(edge2);

            if (faceNormal.length() < 1e-6f) {
                continue;
            }

            faceNormal = faceNormal.normalize();

            normalSums[i0] = normalSums[i0].add(faceNormal);
            normalSums[i1] = normalSums[i1].add(faceNormal);
            normalSums[i2] = normalSums[i2].add(faceNormal);
        }

        model.normals.clear();
        model.normals.ensureCapacity(vertexCount);

        for (int i = 0; i < vertexCount; ++i) {
            Vector3f sum = normalSums[i];
            if (sum.length() < 1e-6f) {

                model.normals.add(new Vector3f(0.0f, 0.0f, 1.0f));
            } else {

                Vector3f n = sum.normalize();
                model.normals.add(new Vector3f(n.getX(), n.getY(), n.getZ()));
            }
        }

        for (Polygon polygon : model.polygons) {
            List<Integer> vIdx = polygon.getVertexIndices();
            ArrayList<Integer> nIdx = new ArrayList<>(vIdx.size());
            nIdx.addAll(vIdx);
            polygon.setNormalIndices(nIdx);
        }
    }
}


