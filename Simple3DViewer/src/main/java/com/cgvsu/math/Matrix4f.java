package com.cgvsu.math;

import com.cgvsu.math.base.AbstractMatrix;

public class Matrix4f extends AbstractMatrix<Matrix4f, Vector4f> {

    public Matrix4f(float[][] data) {
        super(data, 4);
    }

    @Override
    protected Matrix4f createNew(float[][] data) {
        return new Matrix4f(data);
    }

    public static Matrix4f identity() {
        return new Matrix4f(new float[][]{
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        });
    }

    public static Matrix4f zero() {
        return new Matrix4f(new float[4][4]);
    }

    // Умножение матрицы на вектор-столбец (правильное!)
    @Override
    public Vector4f multiply(Vector4f vector) {
        float x = data[0][0] * vector.getX() + data[0][1] * vector.getY() +
                  data[0][2] * vector.getZ() + data[0][3] * vector.getW();
        float y = data[1][0] * vector.getX() + data[1][1] * vector.getY() +
                  data[1][2] * vector.getZ() + data[1][3] * vector.getW();
        float z = data[2][0] * vector.getX() + data[2][1] * vector.getY() +
                  data[2][2] * vector.getZ() + data[2][3] * vector.getW();
        float w = data[3][0] * vector.getX() + data[3][1] * vector.getY() +
                  data[3][2] * vector.getZ() + data[3][3] * vector.getW();
        
        return new Vector4f(x, y, z, w);
    }

    // Умножение матрицы на Vector3f (расширяем до Vector4f с w=1)
    public Vector3f multiply(Vector3f vector) {
        Vector4f result = multiply(new Vector4f(vector.getX(), vector.getY(), vector.getZ(), 1.0f));
        if (Math.abs(result.getW()) > 1e-7f) {
            return new Vector3f(result.getX() / result.getW(), 
                              result.getY() / result.getW(), 
                              result.getZ() / result.getW());
        }
        return new Vector3f(result.getX(), result.getY(), result.getZ());
    }

    // Статические методы для аффинных преобразований
    public static Matrix4f scale(float sx, float sy, float sz) {
        return new Matrix4f(new float[][]{
                {sx, 0, 0, 0},
                {0, sy, 0, 0},
                {0, 0, sz, 0},
                {0, 0, 0, 1}
        });
    }

    public static Matrix4f translate(float tx, float ty, float tz) {
        return new Matrix4f(new float[][]{
                {1, 0, 0, tx},
                {0, 1, 0, ty},
                {0, 0, 1, tz},
                {0, 0, 0, 1}
        });
    }

    public static Matrix4f rotateX(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        return new Matrix4f(new float[][]{
                {1, 0, 0, 0},
                {0, cos, -sin, 0},
                {0, sin, cos, 0},
                {0, 0, 0, 1}
        });
    }

    public static Matrix4f rotateY(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        return new Matrix4f(new float[][]{
                {cos, 0, sin, 0},
                {0, 1, 0, 0},
                {-sin, 0, cos, 0},
                {0, 0, 0, 1}
        });
    }

    public static Matrix4f rotateZ(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        return new Matrix4f(new float[][]{
                {cos, -sin, 0, 0},
                {sin, cos, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        });
    }

    // Композиция аффинных преобразований: T * R * S (правильный порядок для вращения вокруг центра)
    // Для вращения вокруг центра модели: сначала масштаб, потом вращение, потом перенос
    public static Matrix4f modelMatrix(Vector3f translation, Vector3f rotation, Vector3f scale) {
        Matrix4f scaleMat = Matrix4f.scale(scale.getX(), scale.getY(), scale.getZ());
        Matrix4f rotX = Matrix4f.rotateX(rotation.getX());
        Matrix4f rotY = Matrix4f.rotateY(rotation.getY());
        Matrix4f rotZ = Matrix4f.rotateZ(rotation.getZ());
        Matrix4f transMat = Matrix4f.translate(translation.getX(), translation.getY(), translation.getZ());
        
        // Порядок: Translation * Rotation * Scale (для вращения вокруг центра после масштабирования)
        // Это правильный порядок для стандартных аффинных преобразований
        Matrix4f rotationMat = rotZ.multiply(rotY).multiply(rotX);
        return transMat.multiply(rotationMat).multiply(scaleMat);
    }

    // Заглушки для интерфейса (если понадобятся в будущем)
    @Override
    public float determinant() {
        throw new UnsupportedOperationException("Determinant calculation not implemented yet");
    }

    @Override
    public Matrix4f inverse() {
        throw new UnsupportedOperationException("Matrix inversion not implemented yet");
    }

    @Override
    public Vector4f solveLinearSystem(Vector4f vector) {
        throw new UnsupportedOperationException("Linear system solving not implemented yet");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append("[");
            for (int j = 0; j < 4; j++) {
                sb.append(String.format("%.2f", data[i][j]));
                if (j < 3) sb.append(", ");
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}