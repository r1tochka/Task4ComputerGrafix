package com.cgvsu.math;

import com.cgvsu.math.Vector3f;

public class Matrix3f implements Matrix {
    private final float[][] data;

    public Matrix3f(float[][] data) {
        if (data.length != 3 || data[0].length != 3) {
            throw new IllegalArgumentException("Матрица должна быть размером 3x3");
        }
        this.data = data;
    }

    public float get(int row, int col) {
        return data[row][col];
    }

    public static Matrix3f identity() {
        return new Matrix3f(new float[][]{
                {1.0f, 0.0f, 0.0f},
                {0.0f, 1.0f, 0.0f},
                {0.0f, 0.0f, 1.0f}
        });
    }

    public static Matrix3f zero() {
        return new Matrix3f(new float[][]{
                {0.0f, 0.0f, 0.0f},
                {0.0f, 0.0f, 0.0f},
                {0.0f, 0.0f, 0.0f}
        });
    }

    @Override
    public Matrix3f add(Matrix other) {
        if (!(other instanceof Matrix3f)) {
            throw new IllegalArgumentException("Можем складывать только матрицу 3x3 с матрицей 3x3)");
        }
        Matrix3f otherMatrix = (Matrix3f) other;
        float[][] result = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = this.data[i][j] + otherMatrix.data[i][j];
            }
        }
        return new Matrix3f(result);
    }

    // Вычитание
    @Override
    public Matrix3f subtract(Matrix other) {
        if (!(other instanceof Matrix3f)) {
            throw new IllegalArgumentException("Можем вычитать только матрицу 3x3 из матрицы 3x3)");
        }
        Matrix3f otherMatrix = (Matrix3f) other;
        float[][] result = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = this.data[i][j] - otherMatrix.data[i][j];
            }
        }
        return new Matrix3f(result);
    }

    public Vector3f multiply(Vector3f vector) {
        float x = data[0][0] * vector.getX() + data[0][1] * vector.getY() + data[0][2] * vector.getZ();
        float y = data[1][0] * vector.getX() + data[1][1] * vector.getY() + data[1][2] * vector.getZ();
        float z = data[2][0] * vector.getX() + data[2][1] * vector.getY() + data[2][2] * vector.getZ();
        return new Vector3f(x, y, z);
    }

    @Override
    public Matrix3f multiply(Matrix other) {
        if (!(other instanceof Matrix3f)) {
            throw new IllegalArgumentException("Можем умножать только матрицу 3x3 с матрицей 3x3)");
        }
        Matrix3f otherMatrix = (Matrix3f) other;
        float[][] result = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    result[i][j] += this.data[i][k] * otherMatrix.data[k][j];
                }
            }
        }
        return new Matrix3f(result);
    }

    @Override
    public Matrix3f transpose() {
        float[][] result = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = this.data[j][i];
            }
        }
        return new Matrix3f(result);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append("[");
            for (int j = 0; j < 3; j++) {
                sb.append(String.format("%.2f", data[i][j]));
                if (j < 2) sb.append(", ");
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}
