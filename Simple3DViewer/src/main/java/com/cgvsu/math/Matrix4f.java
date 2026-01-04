package com.cgvsu.math;

import com.cgvsu.math.Vector4f;

public class Matrix4f implements Matrix {
    private final float[][] data;

    public Matrix4f(float[][] data) {
        if (data.length != 4 || data[0].length != 4) {
            throw new IllegalArgumentException("Матрица должна быть строго 4x4");
        }
        this.data = data;
    }

    public float get(int row, int col) {
        return data[row][col];
    }

    public static Matrix4f identity() {
        return new Matrix4f(new float[][]{
                {1.0f, 0.0f, 0.0f, 0.0f},
                {0.0f, 1.0f, 0.0f, 0.0f},
                {0.0f, 0.0f, 1.0f, 0.0f},
                {0.0f, 0.0f, 0.0f, 1.0f}
        });
    }

    public static Matrix4f zero() {
        return new Matrix4f(new float[][]{
                {0.0f, 0.0f, 0.0f, 0.0f},
                {0.0f, 0.0f, 0.0f, 0.0f},
                {0.0f, 0.0f, 0.0f, 0.0f},
                {0.0f, 0.0f, 0.0f, 0.0f}
        });
    }

    @Override
    public Matrix4f add(Matrix other) {
        if (!(other instanceof Matrix4f)) {
            throw new IllegalArgumentException("Можем складывать только матрицу 4x4 с матрицей 4x4)");
        }
        Matrix4f otherMatrix = (Matrix4f) other;
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = this.data[i][j] + otherMatrix.data[i][j];
            }
        }
        return new Matrix4f(result);
    }

    @Override
    public Matrix4f subtract(Matrix other) {
        if (!(other instanceof Matrix4f)) {
            throw new IllegalArgumentException("Можем вычитать только матрицу 4x4 из матрицы 4x4)");
        }
        Matrix4f otherMatrix = (Matrix4f) other;
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = this.data[i][j] - otherMatrix.data[i][j];
            }
        }
        return new Matrix4f(result);
    }

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

    @Override
    public Matrix4f multiply(Matrix other) {
        if (!(other instanceof Matrix4f)) {
            throw new IllegalArgumentException("Можем умножать только матрицу 4x4 на матрицу 4x4)");
        }
        Matrix4f otherMatrix = (Matrix4f) other;
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    result[i][j] += this.data[i][k] * otherMatrix.data[k][j];
                }
            }
        }
        return new Matrix4f(result);
    }

    @Override
    public Matrix4f transpose() {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = this.data[j][i];
            }
        }
        return new Matrix4f(result);
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