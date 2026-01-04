package com.cgvsu.math;

public class Vector2f implements Vector {
    private static final float EPS = 1e-7f;

    private final float x;
    private final float y;

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Vector2f add(Vector2f other) {
        return new Vector2f(this.x + other.x, this.y + other.y);
    }

    public Vector2f subtract(Vector2f other) {
        return new Vector2f(this.x - other.x, this.y - other.y);
    }

    @Override
    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    @Override
    public Vector normalize() {
        float len = length();
        if (len < EPS) {
            throw new ArithmeticException("К сожалению, нормализовывать нулевой вектор мы не можем");
        }
        return new Vector2f(x / len, y / len);
    }

    @Override
    public Vector multiply(float scalar) {
        return new Vector2f(this.x * scalar, this.y * scalar);
    }

    @Override
    public Vector divide(float scalar) {
        if (Math.abs(scalar) < EPS) {
            throw new IllegalArgumentException("Нельзя делить на ноль, помнишь?)");
        }
        return new Vector2f(this.x / scalar, this.y / scalar);
    }

    public float dot(Vector2f other) {
        return this.x * other.x + this.y * other.y;
    }

    @Override
    public float[] toArray() {
        return new float[]{x, y};
    }

    @Override
    public String toString() {
        return String.format("Vector2(%.2f, %.2f)", x, y);
    }

}
