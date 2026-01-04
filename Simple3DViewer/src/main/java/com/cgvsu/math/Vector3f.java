package com.cgvsu.math;

public class Vector3f implements Vector {
    private static final float EPS = 1e-7f; // порог для сравнения с нулём

    private final float x;
    private final float y;
    private final float z;

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public Vector3f add(Vector3f other) {
        return new Vector3f(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3f subtract(Vector3f other) {
        return new Vector3f(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    @Override
    public Vector3f multiply(float scalar) {
        return new Vector3f(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    @Override
    public Vector3f divide(float scalar) {
        if (Math.abs(scalar) < EPS) {
            throw new IllegalArgumentException("Нельзя делить на ноль, помнишь?)");
        }
        return new Vector3f(this.x / scalar, this.y / scalar, this.z / scalar);
    }

    @Override
    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    @Override
    public Vector3f normalize() {
        float len = length();
        if (len < EPS) {
            throw new ArithmeticException("К сожалению, мы не можем нормализовывать нулевой вектор");
        }
        return new Vector3f(x / len, y / len, z / len);
    }

    public float dot(Vector3f other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Vector3f cross(Vector3f other) {
        return new Vector3f(
                this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x
        );
    }

    @Override
    public float[] toArray() {
        return new float[]{x, y, z};
    }

    @Override
    public String toString() {
        return String.format("Vector3f(%.2f, %.2f, %.2f)", x, y, z);
    }
}