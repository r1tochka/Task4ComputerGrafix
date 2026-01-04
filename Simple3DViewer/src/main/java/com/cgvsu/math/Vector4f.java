package com.cgvsu.math;

public class Vector4f implements Vector {
    private static final float EPS = 1e-7f; // порог для сравнения с нулём

    private final float x;
    private final float y;
    private final float z;
    private final float w;

    public Vector4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
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

    public float getW() {
        return w;
    }

    public Vector4f add(Vector4f other) {
        return new Vector4f(
                this.x + other.x,
                this.y + other.y,
                this.z + other.z,
                this.w + other.w
        );
    }

    public Vector4f subtract(Vector4f other) {
        return new Vector4f(
                this.x - other.x,
                this.y - other.y,
                this.z - other.z,
                this.w - other.w
        );
    }

    @Override
    public Vector4f multiply(float scalar) {
        return new Vector4f(
                this.x * scalar,
                this.y * scalar,
                this.z * scalar,
                this.w * scalar
        );
    }

    @Override
    public Vector4f divide(float scalar) {
        if (Math.abs(scalar) < EPS) {
            throw new IllegalArgumentException("Нельзя делить на ноль, помнишь?)");
        }
        return new Vector4f(
                this.x / scalar,
                this.y / scalar,
                this.z / scalar,
                this.w / scalar
        );
    }

    @Override
    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z + w * w);
    }

    @Override
    public Vector4f normalize() {
        float len = length();
        if (len < EPS) {
            throw new ArithmeticException("К сожалению, мы не можем нормализовывать нулевой вектор");
        }
        return new Vector4f(x / len, y / len, z / len, w / len);
    }

    public float dot(Vector4f other) {
        return this.x * other.x + this.y * other.y + this.z * other.z + this.w * other.w;
    }

    @Override
    public float[] toArray() {
        return new float[]{x, y, z, w};
    }

    @Override
    public String toString() {
        return String.format("Vector4f(%.2f, %.2f, %.2f, %.2f)", x, y, z, w);
    }
}
