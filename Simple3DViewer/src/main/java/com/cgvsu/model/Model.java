package com.cgvsu.model;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Matrix4f;

import java.util.*;

public class Model {

    public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    public ArrayList<Polygon> polygons = new ArrayList<Polygon>();

    // Поля для аффинных преобразований
    private Vector3f translation = new Vector3f(0, 0, 0);
    private Vector3f rotation = new Vector3f(0, 0, 0);
    private Vector3f scale = new Vector3f(1, 1, 1);
    private Matrix4f modelMatrix = Matrix4f.identity();

    private final List<Vector3f> originalVertices = new ArrayList<>();

    /**
     * Обновляет матрицу модели на основе текущих трансформаций
     */
    public void updateModelMatrix() {
        modelMatrix = Matrix4f.modelMatrix(translation, rotation, scale);
    }

    /**
     * Возвращает текущую матрицу модели
     */
    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    /**
     * Сохраняет текущие вершины как оригинальные
     */
    public void setOriginalVertices() {
        originalVertices.clear();
        for (Vector3f vertex : vertices) {
            originalVertices.add(new Vector3f(vertex.getX(), vertex.getY(), vertex.getZ()));
        }
    }

    /**
     * Восстанавливает оригинальные вершины
     */
    public void resetToOriginal() {
        if (!originalVertices.isEmpty()) {
            vertices.clear();
            for (Vector3f vertex : originalVertices) {
                vertices.add(new Vector3f(vertex.getX(), vertex.getY(), vertex.getZ()));
            }
        }
    }

    // Геттеры и сеттеры для трансформаций
    public Vector3f getTranslation() {
        return new Vector3f(translation.getX(), translation.getY(), translation.getZ());
    }

    public void setTranslation(Vector3f translation) {
        this.translation = new Vector3f(translation.getX(), translation.getY(), translation.getZ());
        updateModelMatrix();
    }

    public Vector3f getRotation() {
        return new Vector3f(rotation.getX(), rotation.getY(), rotation.getZ());
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = new Vector3f(rotation.getX(), rotation.getY(), rotation.getZ());
        updateModelMatrix();
    }

    public Vector3f getScale() {
        return new Vector3f(scale.getX(), scale.getY(), scale.getZ());
    }

    public void setScale(Vector3f scale) {
        this.scale = new Vector3f(scale.getX(), scale.getY(), scale.getZ());
        updateModelMatrix();
    }

    // Методы для добавления трансформаций
    public void translate(Vector3f delta) {
        translation = translation.add(delta);
        updateModelMatrix();
    }

    public void rotate(Vector3f delta) {
        rotation = rotation.add(delta);
        updateModelMatrix();
    }

    public void scale(Vector3f factor) {
        scale = new Vector3f(
                scale.getX() * factor.getX(),
                scale.getY() * factor.getY(),
                scale.getZ() * factor.getZ()
        );
        updateModelMatrix();
    }

    // Методы для трансформаций по осям
    public void translateX(float delta) {
        translation = translation.add(new Vector3f(delta, 0, 0));
        updateModelMatrix();
    }

    public void translateY(float delta) {
        translation = translation.add(new Vector3f(0, delta, 0));
        updateModelMatrix();
    }

    public void translateZ(float delta) {
        translation = translation.add(new Vector3f(0, 0, delta));
        updateModelMatrix();
    }

    public void rotateX(float delta) {
        rotation = rotation.add(new Vector3f(delta, 0, 0));
        updateModelMatrix();
    }

    public void rotateY(float delta) {
        rotation = rotation.add(new Vector3f(0, delta, 0));
        updateModelMatrix();
    }

    public void rotateZ(float delta) {
        rotation = rotation.add(new Vector3f(0, 0, delta));
        updateModelMatrix();
    }

    public void scaleX(float factor) {
        scale = new Vector3f(scale.getX() * factor, scale.getY(), scale.getZ());
        updateModelMatrix();
    }

    public void scaleY(float factor) {
        scale = new Vector3f(scale.getX(), scale.getY() * factor, scale.getZ());
        updateModelMatrix();
    }

    public void scaleZ(float factor) {
        scale = new Vector3f(scale.getX(), scale.getY(), scale.getZ() * factor);
        updateModelMatrix();
    }

    /**
     * Сбрасывает все трансформации к начальным значениям
     */
    public void resetTransformations() {
        translation = new Vector3f(0, 0, 0);
        rotation = new Vector3f(0, 0, 0);
        scale = new Vector3f(1, 1, 1);
        updateModelMatrix();
    }
}
