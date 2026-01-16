package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import com.cgvsu.model.Model;
import com.cgvsu.render_engine.CameraManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.ArrayList;

// УДАЛЕНО: больше не используем rotateScaleTranslate, используем mesh.getModelMatrix()

public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final CameraManager camera,
            final Model mesh,
            final int width,
            final int height,
            final Texture texture,
            final Lighting lighting)
    {
        render(graphicsContext, camera, mesh, width, height, texture, lighting, Color.LIGHTGRAY);
    }

    public static void render(
            final GraphicsContext graphicsContext,
            final CameraManager camera,
            final Model mesh,
            final int width,
            final int height,
            final Texture texture,
            final Lighting lighting,
            final Color baseColor)
    {
        render(graphicsContext, camera, mesh, width, height, texture, lighting, baseColor, null);
    }

    public static void render(
            final GraphicsContext graphicsContext,
            final CameraManager camera,
            final Model mesh,
            final int width,
            final int height,
            final Texture texture,
            final Lighting lighting,
            final Color baseColor,
            final List<CameraManager> helperCameras)
    {
        render(graphicsContext, camera, mesh, width, height, texture, lighting, baseColor, helperCameras, new RenderingModes());
    }

    public static void render(
            final GraphicsContext graphicsContext,
            final CameraManager camera,
            final Model mesh,
            final int width,
            final int height,
            final Texture texture,
            final Lighting lighting,
            final Color baseColor,
            final List<CameraManager> helperCameras,
            final RenderingModes renderingModes)
    {

        // Используем матрицу модели из объекта mesh
        Matrix4f modelMatrix = mesh.getModelMatrix();
        Matrix4f viewMatrix = camera.getViewMatrix(); // система координат камеры
        Matrix4f projectionMatrix = camera.getProjectionMatrix(); // плоскость проецирования

        Matrix4f modelViewMatrix = viewMatrix.multiply(modelMatrix);
        Matrix4f modelViewProjectionMatrix = projectionMatrix.multiply(modelViewMatrix);

        ZBuffer zBuffer = new ZBuffer(width, height);

        Color wireColor = Color.BLACK;

        final int nPolygons = mesh.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            ArrayList<Integer> vertexIndices = mesh.polygons.get(polygonInd).getVertexIndices();
            final int nVerticesInPolygon = vertexIndices.size();

            if (nVerticesInPolygon < 3) {
                continue;
            }

            Vector3f v0World = mesh.vertices.get(vertexIndices.get(0));
            Vector3f v1World = mesh.vertices.get(vertexIndices.get(1));
            Vector3f v2World = mesh.vertices.get(vertexIndices.get(2));

            // Прямое умножение матрицы на вектор (векторы-столбцы)
            Vector3f v0View = modelViewMatrix.multiply(v0World);
            Vector3f v1View = modelViewMatrix.multiply(v1World);
            Vector3f v2View = modelViewMatrix.multiply(v2World);

            Vector3f edge1 = v1View.subtract(v0View);
            Vector3f edge2 = v2View.subtract(v0View);
            Vector3f faceNormal = edge1.cross(edge2);

            Vector3f toCamera = v0View.multiply(-1.0f);
            boolean frontFacing = faceNormal.dot(toCamera) > 0.0f;

            if (!frontFacing) {
                continue;
            }

            ArrayList<Integer> textureIndices = mesh.polygons.get(polygonInd).getTextureVertexIndices();
            ArrayList<Integer> normalIndices = mesh.polygons.get(polygonInd).getNormalIndices();

            boolean hasTextureCoords = !textureIndices.isEmpty() && textureIndices.size() == vertexIndices.size();
            boolean hasNormals = !normalIndices.isEmpty() && normalIndices.size() == vertexIndices.size();

            ArrayList<ScreenVertex> screenVertices = new ArrayList<>(nVerticesInPolygon);
            Vector3f cameraPosition = camera.getPosition();

            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                Vector3f modelVertex = mesh.vertices.get(vertexIndices.get(vertexInPolygonInd));

                // Прямое умножение матрицы на вектор (векторы-столбцы)
                Vector3f worldPosition = modelMatrix.multiply(modelVertex); // в мировых координатах

                // Преобразование в clip-space (4D), далее делим на w, чтобы получить NDC
                Vector4f clipPos = modelViewProjectionMatrix.multiply(
                    new Vector4f(modelVertex.getX(), modelVertex.getY(), modelVertex.getZ(), 1.0f));
                float w = clipPos.getW();
                float invW = (Math.abs(w) > 1e-7f) ? (1.0f / w) : 1.0f;

                // Нормализованный диапазон (NDC)
                Vector3f transformed = new Vector3f(
                        clipPos.getX() * invW,
                        clipPos.getY() * invW,
                        clipPos.getZ() * invW
                );

                Vector2f textureCoords = null;
                if (hasTextureCoords && textureIndices.get(vertexInPolygonInd) < mesh.textureVertices.size()) {
                    textureCoords = mesh.textureVertices.get(textureIndices.get(vertexInPolygonInd));
                }

                Vector3f worldNormal = null;
                if (hasNormals && normalIndices.get(vertexInPolygonInd) < mesh.normals.size()) {
                    Vector3f modelNormal = mesh.normals.get(normalIndices.get(vertexInPolygonInd));
                    // Для нормалей используем только rotation и scale (без translation)
                    Matrix4f rotationScaleMatrix = Matrix4f.modelMatrix(
                        new Vector3f(0, 0, 0), // no translation for normals
                        mesh.getRotation(),
                        mesh.getScale()
                    );
                    worldNormal = rotationScaleMatrix.multiply(modelNormal).normalize();
                }

                ScreenVertex screenVertex = toScreenVertex(
                        transformed,
                        width,
                        height,
                        invW,
                        textureCoords,
                        worldNormal,
                        worldPosition,
                        null
                );
                screenVertices.add(screenVertex);
            }

            // Отрисовка треугольников (заливка)
            // Wireframe не влияет на заливку - это дополнительная обводка поверх заливки
            for (int i = 1; i < nVerticesInPolygon - 1; ++i) {
                ScreenVertex sv0 = screenVertices.get(0);
                ScreenVertex sv1 = screenVertices.get(i);
                ScreenVertex sv2 = screenVertices.get(i + 1);

                // Используем расширенную версию только если включены текстура ИЛИ освещение
                if (renderingModes.isUseTexture() || renderingModes.isUseLighting()) {
                    TriangleRasterizer.fillTriangle(
                            graphicsContext,
                            zBuffer,
                            sv0,
                            sv1,
                            sv2,
                            width,
                            height,
                            renderingModes.isUseTexture() ? texture : null,
                            renderingModes.isUseLighting() ? lighting : null,
                            baseColor,
                            cameraPosition
                    );
                } else {
                    // Простая заливка базовым цветом
                    TriangleRasterizer.fillTriangle(
                            graphicsContext,
                            zBuffer,
                            sv0,
                            sv1,
                            sv2,
                            width,
                            height,
                            baseColor
                    );
                }
            }

            // Отрисовка полигональной сетки (если включена соответствующая галочка)
            if (frontFacing && renderingModes.isDrawWireframe()) {
                // Оценка коэффициента для depth bias в зависимости от угла поверхности к камере
                Vector3f normalViewNorm = faceNormal.normalize();
                Vector3f toCameraNorm = toCamera.normalize();
                float cosTheta = Math.abs(normalViewNorm.dot(toCameraNorm));
                // 0 — скользящий угол, 1 — фронтально к камере
                float grazing = 1.0f - cosTheta;

                // Нелинейно усиливаем bias только при реально скользящих углах,
                // чтобы при небольших поворотах он оставался ближе к базовому.
                double angleScale = 1.0 + 8.0 * Math.pow(grazing, 5.0);

                // factorr = 1/(1 + k*d)
                float distance = toCamera.length();
                double depthFactor = 1.0 / (1.0 + 0.15 * distance);

                double depthBiasScale = angleScale * depthFactor;

                for (int i = 0; i < nVerticesInPolygon; ++i) {
                    ScreenVertex a = screenVertices.get(i);
                    ScreenVertex b = screenVertices.get((i + 1) % nVerticesInPolygon);
                    LineRasterizer.drawLine(
                            graphicsContext,
                            zBuffer,
                            a,
                            b,
                            width,
                            height,
                            wireColor,
                            depthBiasScale
                    );
                }
            }
        }

        renderHelperCameras(graphicsContext, helperCameras, camera, projectionMatrix.multiply(viewMatrix), width, height);
    }


    public static void render(
            final GraphicsContext graphicsContext,
            final CameraManager camera,
            final Model mesh,
            final int width,
            final int height)
    {
        render(graphicsContext, camera, mesh, width, height, null, null, Color.LIGHTGRAY);
    }


    private static ScreenVertex toScreenVertex(
            final Vector3f vertex,
            final int width,
            final int height,
            final float invW,
            final Vector2f textureCoords,
            final Vector3f worldNormal,
            final Vector3f worldPosition,
            final Float lightingIntensity) {

        float ndcX = vertex.getX();
        float ndcY = vertex.getY();
        float ndcZ = vertex.getZ();

        //координаты на экране
        float halfWidth = (width - 1.0F) / 2.0F;
        float screenX = halfWidth * ndcX + halfWidth;
        float screenY = (1.0F - height) / 2.0F * ndcY + (height - 1.0F) / 2.0F;

        return new ScreenVertex(screenX, screenY, ndcZ, invW, textureCoords, worldNormal, worldPosition, lightingIntensity);
    }


    private static void renderHelperCameras(
            final GraphicsContext graphicsContext,
            final List<CameraManager> helperCameras,
            final CameraManager activeCamera,
            final Matrix4f viewProjectionMatrix,
            final int width,
            final int height) {
        if (helperCameras == null || helperCameras.isEmpty()) {
            return;
        }

        graphicsContext.setFill(Color.CORNFLOWERBLUE);
        for (CameraManager helper : helperCameras) {
            if (helper == activeCamera) {
                continue;
            }
            // Прямое умножение матрицы на вектор (векторы-столбцы)
            Vector3f clip = viewProjectionMatrix.multiply(helper.getPosition());
            if (clip.getZ() < -1.0f || clip.getZ() > 1.0f) {
                continue;
            }
            Vector3f ndc = clip;
            float screenX = ndc.getX() * width + width / 2.0F;
            float screenY = -ndc.getY() * height + height / 2.0F;
            graphicsContext.fillOval(screenX - 4, screenY - 4, 8, 8);
        }
    }
}
